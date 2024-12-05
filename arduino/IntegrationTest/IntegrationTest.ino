#include <SoftwareSerial.h>
char BluetoothText;

SoftwareSerial Bluetooth(5,4); // Rx=5, Tx=4

#define ENA1 9
#define IN1 7
#define IN2 8

#define ENA2 9
#define IN3 7
#define IN4 8

int wheelSpeed = 180;

void setup() {
  Serial.begin(9600);  
  Bluetooth.begin(9600); 
  
  pinMode(ENA1, OUTPUT);
  pinMode(IN1, OUTPUT); 
  pinMode(IN2, OUTPUT);
}

void loop() {
  
  if(Bluetooth.available() > 0)  
  {
    BluetoothText = Bluetooth.read();      
    Serial.print(BluetoothText); 
    handleCommand(BluetoothText);              
  }

//    executeTest();
}

void executeTest() {
  digitalWrite (IN1, HIGH);
  digitalWrite (IN2, LOW);
  analogWrite (ENA1, wheelSpeed);
  delay (5000);
  digitalWrite (IN1, LOW);
  digitalWrite (IN2, HIGH);
  analogWrite (ENA1, wheelSpeed);
  delay (5000);
}

void handleCommand(char commandCode) {
  switch(commandCode) {
    case '0':
      // Stop
      digitalWrite(IN1, LOW);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '1':
      // Forward
      digitalWrite(IN1, HIGH);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '2':
      // Backward
      digitalWrite(IN1, LOW);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '3':
      // Left
      digitalWrite(IN1, HIGH);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '4':
      // Right
      digitalWrite(IN1, LOW);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    default:
      break;
  }
}
