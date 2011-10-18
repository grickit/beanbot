import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.lang.*;

public class beanbot {


//-----//-----//-----// Setup //-----//-----//-----//
  private static Runtime run_time = Runtime.getRuntime();

  private static SocketChannel serverConnection;
  private static ByteBuffer toServer = ByteBuffer.allocateDirect(1024);
  private static ByteBuffer fromServer = ByteBuffer.allocateDirect(1024);
  private static Hashtable<String,String> core = new Hashtable<String,String>();
  private static Hashtable<String,String> config = new Hashtable<String,String>();
  private static Hashtable<String,String> variables = new Hashtable<String,String>();
  private static Hashtable<String,String> persistent = new Hashtable<String,String>();



//-----//-----//-----// IO Methods //-----//-----//-----//
  public static String generate_timestamp() { // Returns a timestamp string
    Calendar calendar = Calendar.getInstance();
    return "" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)  + ":" + calendar.get(Calendar.SECOND);
  }

  public static void log_output(String prefix, String message) { // Logs a message to a file
    // TODO print logs
  }

  public static void stdout_output(String prefix, String message) { // Logs a message to STDOUT
    System.out.println(prefix + " " + generate_timestamp() + " " + message);
  }


  public static void error_output(String message) { // For errors. Always logged. Always output.
    log_output("BOTERROR",message);
    stdout_output("BOTERROR",message);
  }

  public static void event_output(String message) { // For important events. Always logged. Always output.
    log_output("BOTEVENT",message);
    stdout_output("BOTEVENT",message);
  }

  public static void normal_output(String prefix, String message) { // For general logging. Always logged. Output if verbose.
    log_output(prefix,message);
    if(core.get("verbose") == "1") {
      stdout_output(prefix,message);
    }
  }

  public static void debug_output(String message) { // For deep debug logging. Logged if debug. Output if debug and verbose.
    if(core.get("debug") == "1") {
      log_output("BOTDEBUG",message);
      if(core.get("verbose") == "1") {
	stdout_output("BOTDEBUG",message);
      }
    }
  }



//-----//-----//-----// Connection Methods //-----//-----//-----//
  public static SocketChannel createConnection(String server, int port) throws IOException, InterruptedException { // Creates a connection
    event_output("Attempting to connect.");
    SocketChannel socketConnection = SocketChannel.open();
    socketConnection.configureBlocking(false);
    socketConnection.connect(new InetSocketAddress(server, port));
    while(!socketConnection.finishConnect()) { sleep(100); }
    return socketConnection;
  }

  public static void login() throws IOException { // Send our credentials to the server
    event_output("Attempting to log in.");
    send_server_message("NICK Gambeanbot\n");
    send_server_message("USER Gambot 8 * :Java Gambot\n");
    send_server_message("JOIN ##Gambot\n");
  }

  public static void reconnect() throws IOException, InterruptedException { // Recreates the connection
    event_output("Reconnecting.");
    serverConnection = createConnection("chat.freenode.net",6667);
    login();
    core.put("message_count","0");
  }



//-----//-----//-----// API Methods //-----//-----//-----//
  public static boolean sleep(int millis) throws InterruptedException { // Sleeps
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      error_output("Interrupted during sleep. Shutting down.");
      System.exit(0);
    }
    return true;
  }

  public static void send_server_message(String message) throws IOException { // Sends a message to the IRC server
    toServer.put(message.getBytes());
    toServer.flip();
    while(toServer.hasRemaining()) {
      serverConnection.write(toServer);
    }
    toServer.clear();
  }



//-----//-----//-----// Main Loop //-----//-----//-----//
  public static void main(String[] args) throws IOException, InterruptedException {
    core.put("home_directory",new java.io.File("").getAbsolutePath());
    core.put("configuration_file","config.txt");
    core.put("message_count","0");
    core.put("verbose","1");
    core.put("debug","1");

    serverConnection = createConnection("chat.freenode.net",6667);
    login();

    while(sleep(10)) {
      fromServer.clear();
      int numberBytesRead = serverConnection.read(fromServer);

      if (numberBytesRead == -1) {
	error_output("IRC connection died.");
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
	  normal_output("INCOMING",incoming);
	}
      }
    }
  }
}