FROM alpine:3.19

# gost v2.12.1, 轻量级代理工具, 支持 HTTP / SOCKS5 + 用户认证
ARG GOST_VERSION=2.12.1
RUN apk add --no-cache curl ca-certificates && \
    curl -sL "https://github.com/ginuerzh/gost/releases/download/v${GOST_VERSION}/gost_${GOST_VERSION}_linux_amd64.tar.gz" \
    | tar xz -C /usr/local/bin/ gost && \
    chmod +x /usr/local/bin/gost

# 默认 HTTP 代理模式, 支持 basic auth 认证
# PROXY_MODE 可选: http (推荐, OkHttp 原生支持) 或 socks5
# 密码不要包含 @ : / 等特殊字符, 否则 gost URL 解析会异常
ENV PROXY_MODE=http
ENV PROXY_USER=admin
ENV PROXY_PASS=changeMeInProdUse@2026
ENV PROXY_PORT=1080

EXPOSE 1080
ENTRYPOINT ["sh", "-c", "gost -L ${PROXY_MODE}://${PROXY_USER}:${PROXY_PASS}@:${PROXY_PORT}"]
