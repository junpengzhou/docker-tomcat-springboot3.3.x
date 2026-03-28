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
# 定义应用目录
ENV APP_HOME=/opt/app
# 定义程序包的位置
ENV APP_PKGS_HOME=${APP_HOME}/pkgs

# ============================================================
# 2.1 安装常用工具：curl, vim, htop, telnet, procps, net-tools, cronolog 等
# ============================================================
RUN microdnf install -y \
        curl wget tar gzip unzip \
        vim-enhanced \
        htop \
        telnet \
        net-tools \
        procps-ng \
        findutils \
        shadow-utils \
        fontconfig \
        cronolog \
    && microdnf clean all

# ============================================================
# 2.2 配置常用 shell 别名（ll, la 等）
# ============================================================
RUN cat > /etc/profile.d/aliases.sh <<'EOF'
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
# 4. 创建应用目录
# ============================================================
RUN mkdir -p ${APP_HOME}/logs
RUN mkdir -p ${APP_HOME}/bin
RUN mkdir -p ${APP_PKGS_HOME}

# ============================================================
# 5. 优化 JVM 参数（适用于容器化部署的 JDK 21）
# ============================================================
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 \
-Xms512m \
-XX:MetaspaceSize=256m \
-XX:MaxMetaspaceSize=600m \
-XX:+UseG1GC \
-XX:+UseStringDeduplication \
-XX:AutoBoxCacheMax=100000 \
-Djava.security.egd=file:/dev/./urandom \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${APP_HOME}/logs/heap_dump.hprof"

# ============================================================
# 6. 安装 Arthas（阿里巴巴 Java 诊断工具）
# ============================================================

RUN mkdir -p "${ARTHAS_HOME}" \
    && curl -fsSL -o /tmp/arthas.zip \
        "https://arthas.aliyun.com/download/latest_version?mirror=aliyun" \
    && unzip -o /tmp/arthas.zip -d "${ARTHAS_HOME}" \
    && rm -f /tmp/arthas.zip \
    && chmod +x "${ARTHAS_HOME}/arthas-boot.jar" 2>/dev/null || true

# ============================================================
# 7. 创建我的自定义启动脚本
# ============================================================
RUN cat > "${APP_HOME}/bin/launch.sh" <<'EOF'
#!/bin/bash
# copyright (c) 2026, Frankzhou <frankzhou007@163.com>

# 1. Change to the app package home directory
cd ${APP_PKGS_HOME}

# 2. Find the JAR file in the app home directory
JAR_FILE=$(ls -1 *.jar 2>/dev/null | head -n 1)

# 3. Check if JAR file exists
if [ -z "${JAR_FILE}" ]; then
    echo "Error: No JAR file found in ${APP_PKGS_HOME}"
    exit 1
fi

# 4. Create fifo directory if not exists
mkdir -p ${APP_HOME}/fifo

# 5. Create FIFO for log rotation
rm -f ${APP_HOME}/fifo/catalina.fifo
mkfifo ${APP_HOME}/fifo/catalina.fifo

# 6. Use Cronolog to rotate logs from FIFO
cd ${APP_HOME}/logs && cronolog --symlink=./catalina.out \
    ./catalina.%Y-%m-%d.out < ${APP_HOME}/fifo/catalina.fifo &

# 7. Start Tomcat with log rotation fifo, and run in the pid 1
echo "Starting ${JAR_FILE} ..."
exec java ${JAVA_OPTS} -jar ${APP_PKGS_HOME}/${JAR_FILE} \
    >> ${APP_HOME}/fifo/catalina.fifo 2>&1
EOF
RUN chmod +x "${APP_HOME}/bin/launch.sh"
ENV PATH="${APP_HOME}/bin:${PATH}"

# ============================================================
# 8. 暴露端口 & 启动
# ============================================================
EXPOSE 8080

WORKDIR ${APP_HOME}
USER root

CMD ["launch.sh"]