<p align="center">
  <a href="https://discord.gg/GsRgsTD">
    <img src="https://img.shields.io/badge/chat-on%20discord-brightgreen.svg">
  </a>
  
  <a href="https://github.com/tronprotocol/tron-studio/issues">
    <img src="https://img.shields.io/github/issues/tronprotocol/tron-studio.svg">
  </a>
  
  <a href="https://github.com/tronprotocol/tron-studio/pulls">
    <img src="https://img.shields.io/github/issues-pr/tronprotocol/tron-studio.svg">
  </a>
  
  <a href="https://github.com/tronprotocol/tron-studio/graphs/contributors"> 
    <img src="https://img.shields.io/github/contributors/tronprotocol/tron-studio.svg">
  </a>
  
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/tronprotocol/tron-studio.svg">
  </a>
</p>


## Tron Studio
TronStudio is an IDE to develop/deploy/debug smart contract based on TVM.

## Documentation

[Tron Studio Documentation](https://developers.tron.network/docs/tron-studio-intro)

## Screenshot
![](image/screenshot.png)

## System Requirement
Oracle JDK 1.8

- Windows 64Bit
- Linux 64Bit
- Mac


## Compile & Run
```
./gradlew build -x test -x check
cd  build/libs
java -jar TronStudio.jar
```
