/**
 * 
 * Created by Marco Mojana
 * Copyright (C) 2019 Marco Mojana
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 *  
 */
 
// Enable debug prints
//#define MY_DEBUG
#define MY_BAUD_RATE (9600ul)
#define MY_NODE_ID 10
#define MY_REPEATER_FEATURE
#define MY_RADIO_RFM69
#define MY_IS_RFM69HW
#define MY_RFM69_NEW_DRIVER 
#define MY_RFM69_FREQUENCY RFM69_433MHZ
#define MY_RFM69_RST_PIN 9
#define MY_RFM69_ENABLE_ENCRYPTION

#include <MySensors.h>  
#include <pca9634.h>

static const int      VALVES_NUM                   =       8;   // The number of valves controlled by the board
static const uint64_t HEARTBEAT_INTERVAL           = 3600000;   // (ms) How much time to wait before sending an heartbeat message
static const uint8_t  N_OE_PIN                     = PD3;       // The output that controls the (negated) OE pin of the PCA9634

static Pca9634 driver = Pca9634();

void presentation()  
{ 
  // Send the sketch info to the gateway
  sendSketchInfo("HeatingController", "1.0");

  // Present sensors as children to gateway
  char childDesc[16];
  for(int childId = 0;childId < VALVES_NUM;childId++) {
    sprintf(childDesc, "Valve%d", childId);
    present(childId, S_DIMMER, childDesc);  
  }

}

void setup()
{
  pinMode(N_OE_PIN, OUTPUT);
  digitalWrite(N_OE_PIN, LOW);
  driver.begin();
  driver.wakeup();
  driver.configureOutputs(true, STOP_COMMAND, TOTEM_POLE, ZERO);
}

void loop()      
{
  wait(HEARTBEAT_INTERVAL);
  sendHeartbeat();
}

void receive(const MyMessage &message) {
  if(message.type != V_PERCENTAGE) {
#ifdef MY_DEBUG
    Serial.print("Received an unknown message type=");
    Serial.println(message.type);
#endif
    return;
  }
  
  const uint8_t valve = message.sensor;
  if(valve >= VALVES_NUM) {
#ifdef MY_DEBUG
    Serial.print("Received an invalid valve=");
    Serial.println(valve);
#endif
    return;
  }

  const uint8_t percentage = message.getByte();
  if(percentage > 100) {
#ifdef MY_DEBUG
    Serial.print("Received an invalid percentage=");
    Serial.println(percentage);
#endif
    return;
  }

  const uint16_t brightness = 2.56 * percentage;
#ifdef MY_DEBUG
  Serial.print("Received a valid request to set the valve=");
  Serial.print(valve);
  Serial.print(" to percentage=");
  Serial.print(percentage);
  Serial.print(" i.e. brightness=");
  Serial.println(brightness);
#endif

  driver.setBrightness(valve, brightness);
}

