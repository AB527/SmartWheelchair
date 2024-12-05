#include <SoftwareSerial.h>
// Pin definitions
const int motorEnable1 = 2; // ENA (PWM pin)
const int motorIn1 = 3;    // IN1
const int motorIn2 = 4;  
const int motorEnable2 = 7; // ENA (PWM pin)
const int motorIn3 = 6;    // IN1
const int motorIn4 = 5;   // IN2

void setup() {
  // Set pins as output
  pinMode(motorEnable1, OUTPUT);
  pinMode(motorIn1, OUTPUT);
  pinMode(motorIn2, OUTPUT);
  pinMode(motorEnable2, OUTPUT);
  pinMode(motorIn3, OUTPUT);
  pinMode(motorIn4, OUTPUT);
}

void loop() {
  // Run motor forward at half speed
  digitalWrite(motorIn1, HIGH);
  digitalWrite(motorIn2, LOW);
  analogWrite(motorEnable1, 128); // Half speed (0-255)
  delay(2000); // Run for 2 seconds

  digitalWrite(motorIn1, LOW);
  digitalWrite(motorIn2, LOW);
  analogWrite(motorEnable1, 128);
}