# ============================================================
# 基础镜像：AlmaLinux 10（RHEL 兼容，支持 dnf/yum）
# ============================================================
FROM almalinux:10-minimal AS base

# ============================================================
# 维护信息公告
# ============================================================
LABEL maintainer="Frankzhou <frankzhou007@163.com>"

# ============================================================
# 1. 配置阿里云 yum 源
# ============================================================
RUN cat > /etc/yum.repos.d/aliyun-base.repo <<'EOF'
[baseos]
name=AlmaLinux 10 - BaseOS - Aliyun
baseurl=https://mirrors.aliyun.com/almalinux/10/BaseOS/$basearch/os/
gpgcheck=0
enabled=1

[appstream]
name=AlmaLinux 10 - AppStream - Aliyun
baseurl=https://mirrors.aliyun.com/almalinux/10/AppStream/$basearch/os/
gpgcheck=0
enabled=1

[epel]
name=Extra Packages for Enterprise Linux 10 - Aliyun
baseurl=https://mirrors.aliyun.com/epel/10/Everything/$basearch/
gpgcheck=0
enabled=1
EOF

# 需要有共享的变量提到最上面来了
ENV ARTHAS_HOME=/opt/arthas

# ============================================================
# 2.1 安装常用工具：curl, vim, htop, telnet, procps, net-tools, logrotate 等
# ============================================================
RUN microdnf install -y \
        curl wget tar gzip unzip \
        vim-enhanced \
        iputils \
        htop \
        telnet \
        net-tools \
        procps-ng \
        findutils \
        shadow-utils \
        fontconfig \
        logrotate \
        nspr nss libxcb dbus-libs \
    && microdnf clean all

# ============================================================
# 2.1 配置常用 shell 别名（ll, la 等）
# ============================================================
RUN cat > /etc/profile.d/aliases.sh <<EOF
alias ll='ls -lh --color=auto'
alias la='ls -lAh --color=auto'
alias l='ls -CF --color=auto'
alias ls='ls --color=auto'
alias grep='grep --color=auto'
alias df='df -h'
alias du='du -h'
alias free='free -h'
alias cls='clear'
alias arthas='java -jar ${ARTHAS_HOME}/arthas-boot.jar'
EOF

# ============================================================
# 3. 安装 JDK 21（Eclipse Temurin / Adoptium）
# ============================================================
ENV JAVA_VERSION=21
ENV JAVA_HOME=/opt/java/jdk-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN set -eux; \
    ARCH=$(uname -m); \
    JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.9%2B10/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz"; \
    curl -fsSL -o /tmp/jdk.tar.gz "${JDK_URL}" \
    && mkdir -p /opt/java \
    && tar -zxf /tmp/jdk.tar.gz -C /opt/java \
    && mv /opt/java/jdk-21* "${JAVA_HOME}" \
    && rm -f /tmp/jdk.tar.gz \
    && java -version

# ============================================================
# 4. 安装 Tomcat 10.1.x（适配 Spring Boot 3.3.x / Jakarta EE 10）
# ============================================================
ENV CATALINA_HOME=/opt/tomcat
ENV CATALINA_BASE="${CATALINA_HOME}"
ENV PATH="${CATALINA_HOME}/bin:${PATH}"

COPY tomcat.tar.gz /tmp/tomcat.tar.gz

RUN set -eux; \
    mkdir -p "${CATALINA_HOME}" \
    && tar -zxf /tmp/tomcat.tar.gz --strip-components=1 -C "${CATALINA_HOME}" \
    && rm -f /tmp/tomcat.tar.gz \
    && rm -rf "${CATALINA_HOME}/webapps/ROOT" \
               "${CATALINA_HOME}/webapps/docs" \
               "${CATALINA_HOME}/webapps/examples" \
               "${CATALINA_HOME}/webapps/host-manager" \
               "${CATALINA_HOME}/webapps/manager" \
               "${CATALINA_HOME}/temp/"* \
    && chmod +x "${CATALINA_HOME}/bin/"*.sh

# ============================================================
# 5. 优化 server.xml
#    - 使用 NIO2 协议（高性能非阻塞 I/O）
#    - 调整线程池、连接超时、Keep-Alive 等参数
#    - 禁用 AJP 连接器
#    - 关闭自动部署（生产环境安全加固）
# ============================================================
RUN cat > "${CATALINA_HOME}/conf/server.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<Server port="8005" shutdown="SHUTDOWN">

  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <Service name="Catalina">

    <!-- HTTP/1.1 NIO2 Connector: 高性能非阻塞 -->
    <Connector port="8080"
               protocol="org.apache.coyote.http11.Http11Nio2Protocol"
               maxThreads="1000"
               minSpareThreads="50"
               maxSpareThreads="300"
               acceptCount="1000"
               connectionTimeout="15000"
               keepAliveTimeout="30000"
               maxKeepAliveRequests="200"
               maxConnections="10000"
               enableLookups="false"
               URIEncoding="UTF-8"
               compression="on"
               compressionMinSize="2048"
               compressibleMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml"
               relaxedQueryChars="[]|{}^&#x5c;&#x60;&lt;&gt;"
               />

    <Engine name="Catalina" defaultHost="localhost">

      <!-- Access Log -->
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="false">

        <Valve className="org.apache.catalina.valves.AccessLogValve"
               directory="logs"
               prefix="access_log"
               suffix=".log"
               pattern="%h %l %u %t &quot;%r&quot; %s %b %D"
               rotatable="true"
               maxDays="7"
        />
      </Host>
    </Engine>
  </Service>
</Server>
EOF

# ============================================================
# 6. 优化 JVM 参数（适用于容器化部署的 JDK 21）
# ============================================================
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 \
-Dstdout.encoding=UTF-8 \
-Dstderr.encoding=UTF-8 \
-Xms512m \
-XX:MetaspaceSize=256m \
-XX:MaxMetaspaceSize=600m \
-XX:+UseG1GC \
-XX:+UseStringDeduplication \
-XX:AutoBoxCacheMax=100000 \
-Djava.security.egd=file:/dev/./urandom \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${CATALINA_HOME}/logs/heap_dump.hprof"

# 写入 setenv.sh（Tomcat 启动时自动加载）
# 注意：使用 <<SETENV（无引号）让变量在构建时展开，heredoc的用法
RUN cat > "${CATALINA_HOME}/bin/setenv.sh" <<EOF
#!/bin/bash
export JAVA_OPTS="${JAVA_OPTS}"
export CATALINA_PID="${CATALINA_HOME}/tomcat.pid"
export CATALINA_OUT="${CATALINA_HOME}/logs/catalina.out"
EOF
RUN chmod +x "${CATALINA_HOME}/bin/setenv.sh"

# ============================================================
# 7. 安装 Arthas（阿里巴巴 Java 诊断工具）
# ============================================================

RUN mkdir -p "${ARTHAS_HOME}" \
    && curl -fsSL -o /tmp/arthas.zip \
        "https://arthas.aliyun.com/download/latest_version?mirror=aliyun" \
    && unzip -o /tmp/arthas.zip -d "${ARTHAS_HOME}" \
    && rm -f /tmp/arthas.zip \
    && chmod +x "${ARTHAS_HOME}/arthas-boot.jar" 2>/dev/null || true

# ============================================================
# 8. 创建我的自定义启动脚本
# ============================================================
RUN cat > "${CATALINA_HOME}/bin/launch.sh" <<EOF
#!/bin/bash
# copyright (c) 2026, Frankzhou <frankzhou007@163.com>

# 1. Create logrotate config for catalina.out
cat > /etc/logrotate.d/catalina <<LOGROTATE_CONF
${CATALINA_HOME}/logs/catalina.out {
    daily
    missingok
    notifempty
    copytruncate
    dateext
    dateformat .%Y-%m-%d
    extension .out
    rotate 30
}
LOGROTATE_CONF

# 2. Background logrotate trigger loop (check every 1 hour)
(
    while true; do
        sleep 3600
        /usr/sbin/logrotate /etc/logrotate.d/catalina --state ${CATALINA_HOME}/logs/.logrotate.state
    done
) &

# 3. Start Tomcat, write logs to catalina.out, and run as PID 1
exec catalina.sh run >> ${CATALINA_HOME}/logs/catalina.out 2>&1
EOF
RUN chmod +x "${CATALINA_HOME}/bin/launch.sh"

# ============================================================
# 9. 暴露端口 & 启动
# ============================================================
EXPOSE 8080

WORKDIR ${CATALINA_HOME}
USER root

CMD ["launch.sh"]
