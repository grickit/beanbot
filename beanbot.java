import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.lang.Number;
import java.lang.Process;
import java.lang.Runtime;
import java.util.regex.Pattern;
import java.lang.System;

public class beanbot {



//-----//-----//-----// Setup //-----//-----//-----//
  private static Runtime run_time = Runtime.getRuntime();
  private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

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
  public static Integer check_pipe_status(String pipeid) throws IOException { // Checks if a child pipe is still alive
    return readpipes.get(pipeid).available();
  }

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
    send_server_message("NICK Gambeanbot");
    send_server_message("USER Gambot 8 * :Java Gambot");
    send_server_message("JOIN ##Gambot");
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
    toServer.put((message + "\n").getBytes());
    toServer.flip();
    while(toServer.hasRemaining()) {
      serverConnection.write(toServer);
    }
    toServer.clear();
  }

  public static boolean check_pipe_exists(String pipeid) { // Checks if a child pipe exists
      return processes.containsKey(pipeid);
  }

  public static void kill_pipe(String pipeid) { // Kills a child pipe
    if(check_pipe_exists(pipeid) == true) {
      debug_output("Killing pipe named " + pipeid);
      processes.get(pipeid).destroy();
      processes.remove(pipeid);
      readpipes.remove(pipeid);
      writepipes.remove(pipeid);
    }
    else {
      error_output("Tried to kill a pipe named " + pipeid + " but no pipe exists with that name.");
    }
  }

  public static void run_command(String pipeid, String command) throws IOException { // Starts a child pipe with a system call
    if(check_pipe_exists(pipeid) == false) {
      debug_output("Starting a pipe named " + pipeid + " with the command: " + command);
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
    readpipes.put("main",System.in);
    writepipes.put("main",System.out);

    core.put("home_directory",new java.io.File("").getAbsolutePath());
    core.put("configuration_file","config.txt");
    core.put("message_count","0");
    core.put("verbose","1");
    core.put("debug","1");

    serverConnection = createConnection("chat.freenode.net",6667);
    login();

    while(sleep(10)) {

      //-----//-----// Read from server //-----//-----//
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
	  String[] lines = Pattern.compile("[\r\n]+").split(incoming);
	  for(int i = 0; i < lines.length; i++) {
	    normal_output("INCOMING",lines[i]);

	    Integer message_count = Integer.parseInt(core.get("message_count")) + 1;
	    core.put("message_count",message_count.toString());
	  }
	}
      }

      //-----//-----// Read from children //-----//-----//
      Enumeration children = readpipes.keys();
      while(children.hasMoreElements()) {
	String pipeid = children.nextElement().toString();
	Integer bytes_left = readpipes.get(pipeid).available();
	if(bytes_left > 0) {
	  byte[] bytes = new byte[bytes_left];
	  readpipes.get(pipeid).read(bytes,0,bytes_left);
	  String incoming = new String(bytes,"UTF-8");
	  send_server_message(incoming);
	}
      }
    }
  }
}
