#include <SoftwareSerial.h>
char BluetoothText;

#define RxPin 10
#define TxPin 11

SoftwareSerial Bluetooth(TxPin,RxPin); // Rx=5, Tx=4

#define ENA1 2
#define IN1 3
#define IN2 4

#define ENA2 7
#define IN3 6
#define IN4 5

int wheelSpeed = 250;

void setup() {
  Serial.begin(9600);  
  Bluetooth.begin(9600); 
  
  pinMode(ENA1, OUTPUT);
  pinMode(IN1, OUTPUT); 
  pinMode(IN2, OUTPUT);

  pinMode(ENA2, OUTPUT);
  pinMode(IN3, OUTPUT); 
  pinMode(IN4, OUTPUT);
}

void loop() {
  
  if(Bluetooth.available() > 0)  
  {
    BluetoothText = Bluetooth.read();      
    handleCommand(BluetoothText); 
    // executeTest();             
  }


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
      digitalWrite(IN2, HIGH);
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, HIGH);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '3':
      // Left
      digitalWrite(IN1, LOW);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, HIGH);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    case '4':
      // Right
      digitalWrite(IN1, HIGH);
      digitalWrite(IN2, LOW);
      digitalWrite(IN3, LOW);
      digitalWrite(IN4, LOW);
      analogWrite (ENA1, wheelSpeed);
      analogWrite (ENA2, wheelSpeed);
      break;
    default:
      break;
  }
}
