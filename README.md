## Tron Studio
TronStudio是用于开发、部署、调试基于TVM的智能合约的工具。

## Screenshot
![](image/screenshot.png)

## 运行环境要求
安装Oracle JDK 1.8

- Windows 64位系统
- Linux 64位系统
- Mac


## 编译运行
```
./gradlew build -x test -x check
cd  build/libs
java -jar TronStudio.jar
```