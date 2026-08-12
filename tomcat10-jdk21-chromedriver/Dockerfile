FROM jeepzhou007/tomcat10-jdk21:latest

# 额外 安装 chromedriver 依赖（之前缺的库）
RUN microdnf install -y nspr nss libxcb dbus-libs

# 添加 Chrome 仓库并安装 Chrome
RUN echo $'[google-chrome]\nname=google-chrome\nbaseurl=http://dl.google.com/linux/chrome/rpm/stable/$basearch\nenabled=1\ngpgcheck=1\ngpgkey=https://dl.google.com/linux/linux_signing_key.pub' > /etc/yum.repos.d/google-chrome.repo && \
    microdnf install -y google-chrome-stable && \
    microdnf clean all