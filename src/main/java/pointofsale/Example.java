package pointofsale;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

class ModemDidNotConnectException extends Exception {

}

class Modem {
  public static void dialModem(int number) throws ModemDidNotConnectException {

  }
}


// Domain (semantic) exceptions
// NoMoneyException
// FraudAlertException
// StolenException
// IOException
// IN every case, keep the original as the cause
public class Example {
  public static boolean useModem = false;

  //  public static void util2() throws ModemDidNotConnectException, UnknownHostException, IOException {
  public static void util2() throws IOException {
    int retries = 3;
    while (retries-- > 0) {
      try {
        if (useModem) {
          Modem.dialModem(12345678);
        } else {
          Socket s = new Socket("127.0.0.1", 8080);
        }
      } catch (ModemDidNotConnectException me) {
        if (retries == 0) {
//          throw me;
          throw new IOException(me);
        }
      }
    }
  }

  public static void util1() throws ModemDidNotConnectException, UnknownHostException, IOException {
    util2();
  }

  public void sellStuff() {
    // step 1
    // step 2

    try {
      util1();
    } catch (IOException | ModemDidNotConnectException me) {
      // involve human
    }
  }
}
