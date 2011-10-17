import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class beanbot {

  private static SocketChannel serverConnection;
  private static ByteBuffer toServer;
  private static ByteBuffer fromServer;

  public static void main(String[] args) throws IOException, InterruptedException {

    toServer = ByteBuffer.allocateDirect(1024);
    fromServer = ByteBuffer.allocateDirect(1024);
    serverConnection = createConnection("chat.freenode.net",6667);
    login();

    while(true) {
      sleep(100);
      fromServer.clear();
      int numberBytesRead = serverConnection.read(fromServer);

      if (numberBytesRead == -1) {
	System.out.println("IRC connection died.");
	serverConnection = createConnection("chat.freenode.net",6667);
	login();
      }
      else {
	String incoming = "";
	fromServer.flip();
	byte[] bytes = new byte[fromServer.remaining()];
	fromServer.get(bytes);

	if(bytes.length > 0) {
	  String s = new String(bytes,"UTF-8");
	  incoming = incoming + s;
	  //TODO: Handle incoming message
	  System.out.println(incoming);
	}
      }
    }
  }

  public static void sleep(int millis) throws InterruptedException {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      System.out.println("Interrupted");
      System.exit(0);
    }
  }

  public static SocketChannel createConnection(String server, int port) throws IOException, InterruptedException {
    System.out.print("Connecting");
    SocketChannel socketConnection = SocketChannel.open();
    socketConnection.configureBlocking(false);
    socketConnection.connect(new InetSocketAddress(server, port));
    while(!socketConnection.finishConnect()) {
      System.out.print(".");
      System.out.flush();
      sleep(500);
    }
    System.out.print("\n");
    return socketConnection;
  }

  public static void sendServerMessage(String message) throws IOException {
    toServer.put(message.getBytes());
    toServer.flip();
    while(toServer.hasRemaining()) {
      serverConnection.write(toServer);
    }
    toServer.clear();
  }

  public static void login() throws IOException {
    System.out.println("Logging in.");
    sendServerMessage("NICK Gambeanbot\n");
    sendServerMessage("USER Gambot 8 * :Java Gambot\n");
    sendServerMessage("JOIN ##Gambot\n");
  }
}