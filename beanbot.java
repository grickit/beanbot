import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class beanbot {
  public static void main(String[] args) throws IOException, InterruptedException {
    SocketChannel serverConnection = createConnection("chat.freenode.net",6667);
    ByteBuffer toServer = ByteBuffer.allocateDirect(1024);
    ByteBuffer fromServer = ByteBuffer.allocateDirect(1024);

    System.out.println("Logging in.");
    //TODO: Login

    while(true) {
      sleep(100);
      fromServer.clear();
      int numberBytesRead = serverConnection.read(fromServer);

      if (numberBytesRead == -1) {
	System.out.println("IRC connection died.");
	serverConnection = createConnection("chat.freenode.net",6667);
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
    Thread.sleep(millis);
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
}