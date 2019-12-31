# Thermostat node software

TL:DR GOTO [installation chapter](#installation)

## Features

This is the Arduino source code for my thermostats, that takes care of:
- Configure the RFM69HW radio
- Configure the BME280 temperature and humidity sensor for minimum energy consumption
- Monitors the battery voltage and reports it back to the gateway
- Supports both metric and imperial units
- Uses deep sleep to save battery life.
- Minimizes the number of messages by sending the readings only when they change
- Sends heartbeat messages to make sure that the gateway knows which sensors are online

## Requirements

In order to make this sketch compile, you have to install (Sketch => Include Library => Manage Libraries...) and/or include (Sketch => Include Library) the following Arduino libraries:
- [MySensors](https://github.com/mysensors/MySensors/tree/master)
- [Adafruit Unified Sensor](https://github.com/adafruit/Adafruit_Sensor)
- [Adafruit BME Library](https://github.com/adafruit/Adafruit_BME280_Library)
- This code is meant to work with the board that I have published [here](../hardware).

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
7. Load the `thermostat.ino` sketch that you find in this repo.
8. Make sure the antenna is connected to the node board!
9. Open the serial monitor, set the bitrate to 9600 and flash the board. You should expect a banner and some periodic logs about sent and received messages. If the gateway is already running, the node will present itself.
10. For production, you can comment out the line:
```
#define MY_DEBUG
```
at the top of `thermostat.ino`.

## Credits

Adapted from <https://github.com/mysensors/MySensorsArduinoExamples/blob/master/examples/Si7021TemperatureAndHumiditySensor/Si7021TemperatureAndHumiditySensor.ino>
