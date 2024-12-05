#include <SoftwareSerial.h>

char BluetoothText;

#define RxPin 2
#define TxPin 3

SoftwareSerial Bluetooth(TxPin,RxPin);

void setup() {
  Serial.begin(9600);  
  Bluetooth.begin(9600); 
}

void loop() {
  // Serial.print(Bluetooth.available()); 
  if(Bluetooth.available() > 0)  
  {
    Serial.print(Bluetooth.available());
    BluetoothText = Bluetooth.read();      
    Serial.println(BluetoothText);             
  }
}
