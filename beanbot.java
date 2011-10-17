import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.Process;

public class beanbot {

  private static SocketChannel serverConnection;
  private static ByteBuffer toServer = ByteBuffer.allocateDirect(1024);
  private static ByteBuffer fromServer = ByteBuffer.allocateDirect(1024);
  private static Hashtable<String,String> core = new Hashtable<String,String>();
  private static Hashtable<String,String> config = new Hashtable<String,String>();
  private static Hashtable<String,String> variables = new Hashtable<String,String>();
  private static Hashtable<String,String> persistent = new Hashtable<String,String>();

  private static Hashtable<String,Process> processes = new Hashtable<String,Process>();
  private static Hashtable<String,ByteBuffer> readpipes = new Hashtable<String,ByteBuffer>();
  private static Hashtable<String,ByteBuffer> writepipes = new Hashtable<String,ByteBuffer>();

  private static String[] pending_outgoing;
  private Date last_second = new Date();
  private int messages_this_second = 0;

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

  public static void reconnect() throws IOException, InterruptedException {
    serverConnection = createConnection("chat.freenode.net",6667);
    login();
    core.put("message_count","0");
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    core.put("home_directory",new java.io.File("").getAbsolutePath());
    core.put("configuration_file","config.txt");
    core.put("message_count","0");

    serverConnection = createConnection("chat.freenode.net",6667);
    login();

    while(true) {
      sleep(100);
      fromServer.clear();
      int numberBytesRead = serverConnection.read(fromServer);

      if (numberBytesRead == -1) {
	System.out.println("IRC connection died.");
	reconnect();
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
}