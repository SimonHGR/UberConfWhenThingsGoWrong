package org.example;

record Message(String message) {}

public class Main {
  public static void main(String[] args) {
    Object m = new Message("Java 21");
    System.out.println(switch (m) {
      case Message(String msg) -> "Hello " + msg + " world!";
      default -> throw new IllegalStateException("Not expecting that!");
    });
  }
}