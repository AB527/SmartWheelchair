#include <SoftwareSerial.h>

char BluetoothText;

#define RxPin 10
#define TxPin 11

SoftwareSerial Bluetooth(TxPin,RxPin);

void setup() {
  Serial.begin(9600);  
  Bluetooth.begin(9600); 
}

void loop() {
  if(Bluetooth.available() > 0)  
  {
    BluetoothText = Bluetooth.read();      
    Serial.println(BluetoothText);             
  }
}
