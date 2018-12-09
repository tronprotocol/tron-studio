## Documentation

[Tron Studio Documentation](https://developers.tron.network/docs/tron-studio-intro)

## Tron Studio
TronStudio is an IDE to develop/deploy/debug smart contract based on TVM.

<<<<<<< HEAD

## Screenshot
![](image/screenshot.png)
=======
<p align="center">
  <a href="https://discord.gg/GsRgsTD">
    <img src="https://img.shields.io/badge/chat-on%20discord-brightgreen.svg">
  </a>
    
  <a href="https://travis-ci.org/tronprotocol/java-tron">
    <img src="https://travis-ci.org/tronprotocol/java-tron.svg?branch=develop">
  </a>
  
  <a href="https://codecov.io/gh/tronprotocol/java-tron">
    <img src="https://codecov.io/gh/tronprotocol/java-tron/branch/develop/graph/badge.svg" />
  </a>
  
  <a href="https://github.com/tronprotocol/java-tron/issues">
    <img src="https://img.shields.io/github/issues/tronprotocol/java-tron.svg">
  </a>
  
  <a href="https://github.com/tronprotocol/java-tron/pulls">
    <img src="https://img.shields.io/github/issues-pr/tronprotocol/java-tron.svg">
  </a>
  
  <a href="https://github.com/tronprotocol/java-tron/graphs/contributors"> 
    <img src="https://img.shields.io/github/contributors/tronprotocol/java-tron.svg">
  </a>
  
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/tronprotocol/java-tron.svg">
  </a>
</p>

<p align="center">
  <a href="#how-to-build">How to Build</a> •
  <a href="#running">How to Run</a> •
  <a href="#links">Links</a> •
  <a href="http://developers.tron.network">Documentation</a> •
  <a href="CONTRIBUTING.md">Contributing</a> •
  <a href="#community">Community</a>
</p>
>>>>>>> Odyssey-v3.2.1.2

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
<<<<<<< HEAD
=======

</details>

* In IntelliJ IDEA
  
<details>
<summary>

Open the configuration panel:

</summary>

![](docs/images/program_configure.png)

</details>  

<details>
<summary>

In the `Program arguments` option, fill in `--witness`:

</summary>

![](docs/images/set_witness_param.jpeg)

</details> 
  
Then, run `FullNode::main()` again.

# Quick Start

Read the [Quick Start](https://developers.tron.network/docs/getting-started-1).

# Advanced Configurations

Read the [Advanced Configurations](src/main/java/org/tron/core/config/README.md).

# Developer Community

* [Discord](https://discord.gg/GsRgsTD) Please join our Developer Discord
* [Gitter](https://gitter.im/tronprotocol/allcoredev) the core dev gitter

# Links

* [Website](https://tron.network/)
* [Documentation](http://developers.tron.network)
* [Blog](https://tronprotocol.github.io/tron-blog/)

# Projects

* [TRON Protocol](https://github.com/tronprotocol/protocol)
* [Wallet Client](https://github.com/tronprotocol/wallet-cli)
* [Wallet Web](https://github.com/tronprotocol/wallet-web)
>>>>>>> Odyssey-v3.2.1.2
