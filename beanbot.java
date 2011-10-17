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

  private static Hashtable<String,Process> processes = new Hashtable<String,Process>();
  private static Hashtable<String,InputStream> readpipes = new Hashtable<String,InputStream>();
  private static Hashtable<String,OutputStream> writepipes = new Hashtable<String,OutputStream>();



//-----//-----//-----// IO Methods //-----//-----//-----//
  public static String generate_timestamp() {
    Calendar calendar = Calendar.getInstance();
    return "" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE)  + ":" + calendar.get(Calendar.SECOND);
  }

  public static void log_output(String prefix, String message) {
    // TODO print logs
  }

  public static void stdout_output(String prefix, String message) {
    System.out.println(prefix + " " + generate_timestamp() + " " + message);
  }


  public static void error_output(String message) {
    log_output("BOTERROR",message);
    stdout_output("BOTERROR",message);
  }

  public static void event_output(String message) {
    log_output("BOTEVENT",message);
    stdout_output("BOTEVENT",message);
  }

  public static void normal_output(String prefix, String message) {
    log_output(prefix,message);
    if(core.get("verbose") == "1") {
      stdout_output(prefix,message);
    }
  }

  public static void debug_output(String message) {
    if(core.get("debug") == "1") {
      log_output("BOTDEBUG",message);
      if(core.get("verbose") == "1") {
	stdout_output("BOTDEBUG",message);
      }
    }
  }


//-----//-----//-----// Connection Methods //-----//-----//-----//
  public static SocketChannel createConnection(String server, int port) throws IOException, InterruptedException {
    event_output("Attempting to connect.");
    SocketChannel socketConnection = SocketChannel.open();
    socketConnection.configureBlocking(false);
    socketConnection.connect(new InetSocketAddress(server, port));
    while(!socketConnection.finishConnect()) { sleep(100); }
    return socketConnection;
  }

  public static void login() throws IOException {
    event_output("Attempting to log in.");
    sendServerMessage("NICK Gambeanbot\n");
    sendServerMessage("USER Gambot 8 * :Java Gambot\n");
    sendServerMessage("JOIN ##Gambot\n");
  }

  public static void reconnect() throws IOException, InterruptedException {
    serverConnection = createConnection("chat.freenode.net",6667);
    login();
    core.put("message_count","0");
  }



//-----//-----//-----// API Methods //-----//-----//-----//
  public static boolean sleep(int millis) throws InterruptedException {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      error_output("Interrupted during sleep. Shutting down.");
      System.exit(0);
    }
    return true;
  }

  public static void sendServerMessage(String message) throws IOException {
    toServer.put(message.getBytes());
    toServer.flip();
    while(toServer.hasRemaining()) {
      serverConnection.write(toServer);
    }
    toServer.clear();
  }

  public static boolean check_pipe_exists(String pipeid) {
    return processes.containsKey(pipeid);
  }

  public static void kill_pipe(String pipeid) {
    if(check_pipe_exists(pipeid) == true) {
      processes.get(pipeid).destroy();
      processes.remove(pipeid);
      readpipes.remove(pipeid);
      writepipes.remove(pipeid);
    }
    else {
      error_output("Tried to kill a pipe named " + pipeid + " but no pipe exists with that name.");
    }
  }

  public static void run_command(String pipeid, String command) throws IOException {
    if(check_pipe_exists(pipeid) == false) {
      Process new_process = run_time.exec(command);
      processes.put(pipeid, new_process);
      readpipes.put(pipeid, new_process.getInputStream());
      writepipes.put(pipeid, new_process.getOutputStream());
    }
    else {
      error_output("Tried to start a pipe named " + pipeid + " but an existing pipe has that name.");
    }
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
	error_output("IRC connection died. Reconnecting.");
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