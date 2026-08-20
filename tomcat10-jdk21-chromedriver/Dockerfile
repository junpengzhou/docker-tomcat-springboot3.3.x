FROM jeepzhou007/tomcat10-jdk21

# Runtime libraries required by Chrome/ChromeDriver, plus tools used during the build.
RUN microdnf install -y \
        ca-certificates \
        curl \
        dbus-libs \
        libxcb \
        nspr \
        nss \
        unzip \
    && microdnf clean all \
    && rm -rf /var/cache/yum

# Install the stable Google Chrome RPM.
RUN printf '%s\n' \
        '[google-chrome]' \
        'name=google-chrome' \
        'baseurl=https://dl.google.com/linux/chrome/rpm/stable/$basearch' \
        'enabled=1' \
        'gpgcheck=1' \
        'gpgkey=https://dl.google.com/linux/linux_signing_key.pub' \
    > /etc/yum.repos.d/google-chrome.repo \
    && microdnf install -y google-chrome-stable \
    && microdnf clean all \
    && rm -rf /var/cache/yum

# Download a ChromeDriver matching the build version of the installed Chrome.
# /usr/local/bin is on PATH, while /data/driver is kept for applications that use
# the historical absolute path.
ENV PATH="/usr/local/bin:${PATH}"

RUN set -eux; \
    chrome_version="$(google-chrome --product-version)"; \
    chrome_major="${chrome_version%%.*}"; \
    chrome_build="${chrome_version%.*}"; \
    driver_version="$(curl -fsSL --retry 3 "https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${chrome_build}")" \
        || driver_version="$(curl -fsSL --retry 3 "https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${chrome_major}")"; \
    driver_major="${driver_version%%.*}"; \
    test "${chrome_major}" = "${driver_major}"; \
    mkdir -p /tmp/chromedriver /data/driver; \
    curl -fsSL --retry 3 \
        -o /tmp/chromedriver/chromedriver.zip \
        "https://storage.googleapis.com/chrome-for-testing-public/${driver_version}/linux64/chromedriver-linux64.zip"; \
    unzip -q /tmp/chromedriver/chromedriver.zip -d /tmp/chromedriver; \
    install -Dm755 \
        /tmp/chromedriver/chromedriver-linux64/chromedriver \
        /usr/local/bin/chromedriver; \
    ln -sf /usr/local/bin/chromedriver /data/driver/chromedriver; \
    google-chrome --version; \
    chromedriver --version; \
    /data/driver/chromedriver --version; \
    rm -rf /tmp/chromedriver