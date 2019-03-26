## DEPRECATION NOTICE
TronStudio project is now DEPRECATED and does not support the new [TRC10 token smart contract functions](https://developers.tron.network/docs/trc10-transfer-in-smart-contracts). Please use [TronBox](https://github.com/tronprotocol/tron-box) for smart contract deployment. 

## Documentation

[Tron Studio Documentation](https://developers.tron.network/docs/tron-studio-intro)

## Tron Studio
TronStudio is an IDE to develop/deploy/debug smart contract based on TVM.

## Disable auto complete:
change studio.autocomplete to false in src/main/resources/studio.properties
and re-build amd run

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
## Compile contract Nullpointer quick fix:
 https://github.com/tronprotocol/tron-studio/issues/8 
