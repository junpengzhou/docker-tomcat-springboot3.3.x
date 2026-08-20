FROM jeepzhou007/tomcat10-jdk21

# ============================================================
# 2. 额外 安装 FFmpeg（静态二进制，无系统依赖，全编解码器支持）
# ============================================================
RUN microdnf install -y xz && microdnf clean all \
    && curl -fsSL -o /tmp/ffmpeg.tar.xz https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz \
    && tar -xJf /tmp/ffmpeg.tar.xz -C /tmp \
    && cp /tmp/ffmpeg-*/ffmpeg /tmp/ffmpeg-*/ffprobe /usr/local/bin/ \
    && chmod +x /usr/local/bin/ffmpeg /usr/local/bin/ffprobe \
    && rm -rf /tmp/ffmpeg* \
    && ffmpeg -version

RUN mkdir -p /usr/local/ffmpeg/bin \
  && ln -s /usr/local/bin/ffmpeg /usr/local/ffmpeg/bin/ffmpeg \
  && ln -s /usr/local/bin/ffprobe /usr/local/ffmpeg/bin/ffprobe
