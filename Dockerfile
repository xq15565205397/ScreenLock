FROM ubuntu:20.04

# 设置环境变量
ENV DEBIAN_FRONTEND=noninteractive

# 安装依赖
RUN apt-get update && apt-get install -y \
    git \
    curl \
    unzip \
    openjdk-11-jdk \
    python3 \
    python3-pip \
    python3-venv \
    build-essential \
    libssl-dev \
    libffi-dev \
    python3-dev \
    zlib1g-dev \
    libncurses5-dev \
    libgdbm-dev \
    libnss3-dev \
    libsqlite3-dev \
    libreadline-dev \
    libffi-dev \
    wget \
    libbz2-dev \
    && rm -rf /var/lib/apt/lists/*

# 安装Kivy和Buildozer
RUN pip3 install --upgrade pip \
    && pip3 install kivy buildozer

# 创建工作目录
WORKDIR /app

# 复制应用文件
COPY . /app

# 初始化Buildozer配置
RUN buildozer init

# 构建APK
CMD buildozer android debug
