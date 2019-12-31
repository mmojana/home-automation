/**
 * 
 * Adapted by Marco Mojana
 * From https://github.com/mysensors/MySensorsArduinoExamples/blob/master/examples/Si7021TemperatureAndHumiditySensor/Si7021TemperatureAndHumiditySensor.ino by Henrik Ekblad
 * Copyright (C) 2019 Marco Mojana
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *  
 */
 
// Enable debug prints
#define MY_DEBUG
#define MY_BAUD_RATE (9600ul)
#define MY_NODE_ID 4
#define MY_RADIO_RFM69
#define MY_IS_RFM69HW
#define MY_RFM69_NEW_DRIVER 
#define MY_RFM69_FREQUENCY RFM69_433MHZ
#define MY_RFM69_RST_PIN 9
#define MY_RFM69_ENABLE_ENCRYPTION
#define CHILD_ID_HUM  0
#define CHILD_ID_TEMP 1


#include <MySensors.h>  
#include <Adafruit_Sensor.h>
#include <Adafruit_BME280.h>


static const int      BATTERY_SENSE_PIN            = A7;        // Selects the input pin for the battery sense point
static const float    BATTERY_HIGHEST_V            =       4.2; // (V) The highest expected battery voltage.
static const float    BATTERY_LOWEST_V             =       3.0; // (V) The lowest expected battery voltage.
static const int      BATTERY_READING_AT_HIGHEST_V =     956;   // The ADC reading when powering the board from a BATTERY_HIGHEST_V source
static const uint64_t UPDATE_INTERVAL              =   60000;   // (ms) Sleep time between sensor updates
static const uint64_t HEARTBEAT_INTERVAL           = 3600000;   // (ms) How much time to wait before sending an heartbeat message


static bool            metric         = true;
static int             oldBatteryPcnt =   0;
static float           oldTemperature = 100.0;
static float           oldHumidity    = 100.0;
static uint64_t        lastMessageAgo =   0;
static Adafruit_BME280 bme;


void presentation()  
{ 
  // Send the sketch info to the gateway
  sendSketchInfo("TemperatureAndHumidity", "1.0");

  // Present sensors as children to gateway
  present(CHILD_ID_HUM, S_HUM,   "Humidity");
  present(CHILD_ID_TEMP, S_TEMP, "Temperature");

  metric = getControllerConfig().isMetric;
}

void setup()
{
#ifdef MY_DEBUG
  Serial.println(F("setup"));
#endif
  analogReference(INTERNAL);
  while(!bme.begin())
  {
#ifdef MY_DEBUG
    Serial.println(F("Sensor not detected!"));
#endif
    delay(5000);
  }
  bme.setSampling(Adafruit_BME280::MODE_FORCED, 
                    Adafruit_BME280::SAMPLING_X1,   // temperature 
                    Adafruit_BME280::SAMPLING_NONE, // pressure 
                    Adafruit_BME280::SAMPLING_X1,   // humidity 
                    Adafruit_BME280::FILTER_OFF   ); 
}

float roundTemperature(float temperature) {
  return round(temperature * 10.0) * 0.1;
}

float roundHumidity(float humidity) {
  return round(humidity);
}

void loop()      
{  
  // Read temperature & humidity from sensor.
  bme.takeForcedMeasurement();
  float temperature = bme.readTemperature();
  if(!metric) {
    temperature = temperature * 1.8 + 32.0;
  }
  float humidity = bme.readHumidity();

  // Read battery voltage
  const int sensorValue = analogRead(BATTERY_SENSE_PIN);
  // TODO Find a better estimate!
  int batteryPcnt = (int) (100.0 * (BATTERY_HIGHEST_V * (sensorValue / (float)BATTERY_READING_AT_HIGHEST_V) - BATTERY_LOWEST_V) / (BATTERY_HIGHEST_V - BATTERY_LOWEST_V));

#ifdef MY_DEBUG
  Serial.print(F("Temp:            "));
  Serial.print(temperature);
  Serial.println(metric ? 'C' : 'F');
  Serial.print(F("Hum:             "));
  Serial.println(humidity);
  Serial.print("Battery reading: ");
  Serial.println(sensorValue);
  Serial.print("Battery Voltage: ");
  float batteryV  = BATTERY_HIGHEST_V * sensorValue / BATTERY_READING_AT_HIGHEST_V;
  Serial.print(batteryV);
  Serial.println(" V");
  Serial.print("Battery percentage: ");
  Serial.println(batteryPcnt);
#endif

  static MyMessage msgHum( CHILD_ID_HUM,  V_HUM );
  static MyMessage msgTemp(CHILD_ID_TEMP, V_TEMP);

  lastMessageAgo += UPDATE_INTERVAL;
  bool messageSent = false;
  
  if(fabs(temperature - oldTemperature) >= 0.1) {
    send(msgTemp.set(roundTemperature(temperature), 2));
    oldTemperature = temperature;
    messageSent = true;
  }

  if(fabs(humidity - oldHumidity) >= 1.0) {
    send(msgHum.set(roundHumidity(humidity), 2));
    oldHumidity = humidity;
    messageSent = true;
  }

  if(batteryPcnt != oldBatteryPcnt) {
      sendBatteryLevel(batteryPcnt);
      oldBatteryPcnt = batteryPcnt;
      messageSent = true;
  }

  if(messageSent) {
    lastMessageAgo = 0;
  }

  if(lastMessageAgo > HEARTBEAT_INTERVAL) {
    sendHeartbeat();
    lastMessageAgo = 0;
  }

  sleep(UPDATE_INTERVAL); 

}
