# Heating controller node software

TL:DR GOTO [installation chapter](#installation)

## Features

This is the Arduino source code for my heating controller, that:
- Configures the RFM69HW radio. Since this module will do constantly powered with a power supply, this node will act as a repeater, extending the existing MySensors network and/or providing a more energy-efficient rounting to the battery-powered nodes.
- Configures and uses [my library](https://github.com/mmojana/pca9634-arduino-library) to control the on-board PCA9634 IC.
- Sends periodic heartbeat messages to make sure that the gateway knows which nodes are online

## Requirements

In order to make this sketch compile, you have to install (Sketch => Include Library => Manage Libraries...) and/or include (Sketch => Include Library) the following Arduino libraries:
- [MySensors](https://github.com/mysensors/MySensors/tree/master)
- [PCA9634 library](https://github.com/mmojana/pca9634-arduino-library)

This code is meant to work with the board that I have published [here](../heatingcontroller-node-hardware/).

## Installation

This is the procedure to correctly flash the Arduino Pro Mini with the working code:
1. Make sure you have installed all the libraries listed in the [corresponding section](#requirements) of this guide.
2. The MySensors framework uses the EEPROM memory to store some settings. One of which is the AES key to be used to encrypt/decrypt the messages. There is a special sketch to be uploaded to the Arduino to write the settings in the correct memory offset and with the correct format. To do that, download the `SecurityPersonalizer.ino` sketch from <https://github.com/mysensors/MySensors/blob/master/examples/SecurityPersonalizer/SecurityPersonalizer.ino>. Then change/uncomment the following definitions:
```
#define MY_AES_KEY <your 128-bit AES key>
#define PERSONALIZE_SOFT
#define STORE_AES_KEY
```

Substitute `<your 128-bit AES key>` with a random sequence of bits that must be the same on all nodes and the MySensors gateway configuration file. Keep the same format (`0x12,0x34,...`) as in the uncustomized sketch.

3. Select (Tools => Board) the board `Arduino Pro or Pro Mini`
4. Select (Tools => Processor) the processor `ATmega328P (3.3V, 8 MHz)`
5. Select (Tools => Programmer) the programmer `AVRISP mkII`
6. Open the serial monitor, set the bitrate to 115200 and flash the board. You should expect a success message.
7. Load the `heatingcontroller.ino` sketch that you find in this repo.
8. Make sure the antenna is connected to the node board!
9. Open the serial monitor, set the bitrate to 9600 and flash the board. You should expect a banner and some periodic logs about sent and received messages. If the gateway is already running, the node will present itself.
10. For production, you can comment out the line:
```
#define MY_DEBUG
```
at the top of `heatingcontroller.ino`.

