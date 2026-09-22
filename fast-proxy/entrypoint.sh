#!/bin/sh
# gost 代理启动脚本, 自动对密码做 URL 编码
# 解决密码包含 @ : / # 等特殊字符导致 gost URL 解析异常的问题

# URL 编码密码中的特殊字符
ENCODED_PASS=$(echo "$PROXY_PASS" | sed \
    -e 's/@/%40/g' \
    -e 's/:/%3A/g' \
    -e 's#/#%2F#g' \
    -e 's/#/%23/g' \
    -e 's/ /%20/g' \
    -e 's/!/%21/g' \
    -e 's/\$/%24/g' \
    -e 's/&/%26/g' \
    -e 's/+/%2B/g' \
    -e 's/=/%3D/g' \
    -e 's/?/%3F/g')

echo "Starting gost proxy: mode=${PROXY_MODE}, port=${PROXY_PORT}, user=${PROXY_USER}, auth=enabled"

# exec 替换当前进程, 让 gost 成为 PID 1
exec gost -L ${PROXY_MODE}://${PROXY_USER}:${ENCODED_PASS}@:${PROXY_PORT}
