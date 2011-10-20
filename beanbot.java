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
  private static Hashtable<String,String> core = new Hashtable<String,String>(); // Core values
  private static Hashtable<String,String> config = new Hashtable<String,String>(); // Values from the config file
  private static Hashtable<String,String> variables = new Hashtable<String,String>(); // Variables that children have asked to store
  private static Hashtable<String,String> persistent = new Hashtable<String,String>(); // Persistant variables that children have asked to store

  private static Hashtable<String,Process> processes = new Hashtable<String,Process>(); // Children processes
  private static Hashtable<String,InputStream> readpipes = new Hashtable<String,InputStream>(); // How we get messages from children
  private static Hashtable<String,OutputStream> writepipes = new Hashtable<String,OutputStream>(); // How we send messages to children



//-----//-----//-----// Config Methods //-----//-----//-----//
  public static void parse_arguments (String args[]) {
    for(Integer i = 0; i < args.length; i++) {
      String current_arg = args[i];
      if(current_arg.equals("-v") || current_arg.equals("--verbose")) { set_core_value("verbose","1"); }
      else if (current_arg.equals("--debug")) { set_core_value("debug","1"); }
      else if (current_arg.equals("--unlogged")) { set_core_value("unlogged","1"); }
      else if (current_arg.equals("--staydead")) { set_core_value("staydead","1"); }
      else if (current_arg.equals("--config")) { i++; set_core_value("config",args[i]); }
      else {
	System.out.println("Usage: perl Gambot.pl [OPTION]...");
	System.out.println("A flexible IRC bot framework that can be updated and fixed while running.\n");
	System.out.println("-v, --verbose	Prints all messages to the terminal.");
	System.out.println("		perl gambot.pl --verbose\n");
	System.out.println("--debug		Enables debug message logging");
	System.out.println("		perl gambot.pl --debug\n");
	System.out.println("--unlogged	Disables logging of messages to files.");
	System.out.println("		perl gambot.pl --unlogged\n");
	System.out.println("--config	The argument after this specifies the configuration file to use.");
	System.out.println("		These are stored in " + get_core_value("home_directory") + "/configurations/");
	System.out.println("		Only give a file name. Not a path.");
	System.out.println("		perl gambot.pl --config foo.txt\n");
	System.out.println("--staydead	The bot will not automatically reconnect.");
	System.out.println("		perl gambot.pl --staydead\n");
	System.out.println("--help		Displays this help.");
	System.out.println("		perl gambot.pl --help\n");
	System.out.println("Ordinarily Gambot will not print much output to the terminal, but will log everything to files.");
	System.out.println(get_core_value("home_directory") + "/configurations/config.txt is the default configuration file.\n");
	System.exit(0);
      }
    }
  }



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
    if(get_core_value("verbose") == "1") {
      stdout_output(prefix,message);
    }
  }

  public static void debug_output(String message) { // For deep debug logging. Logged if debug. Output if debug and verbose.
    if(get_core_value("debug") == "1") {
      log_output("BOTDEBUG",message);
      if(get_core_value("verbose") == "1") {
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
    set_core_value("message_count","0");
  }



//-----//-----//-----// API Methods //-----//-----//-----//
  public static String get_config_value(String name) { // Get a value fron config
    return (config.containsKey(name)) ? config.get(name) : "";
  }
  public static String get_core_value(String name) { // Get a value from core
    return (core.containsKey(name)) ? core.get(name) : "";
  }
  public static String get_variable_value(String name) { // Get a value from variables
    return (variables.containsKey(name)) ? variables.get(name) : "";
  }
  public static void set_config_value(String name, String value) {
    config.put(name,value);
  }
  public static void set_core_value(String name, String value) {
    core.put(name,value);
  }
  public static void set_variable_value(String name, String value) {
    variables.put(name,value);
  }

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

  public static void send_pipe_message(String pipeid, String message) throws IOException { // Sends a message to a child pipeid
    if(check_pipe_exists(pipeid) == true) {
      writepipes.get(pipeid).write((message + "\n").getBytes());
      writepipes.get(pipeid).flush();
    }
    else {
      error_output("Tried to send a message to pipe named " + pipeid + " but no pipe exists with that name.");
    }
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
      send_pipe_message(pipeid,pipeid);
    }
    else {
      error_output("Tried to start a pipe named " + pipeid + " but an existing pipe has that name.");
    }
  }



//-----//-----//-----// Main Loop //-----//-----//-----//
  public static void main(String[] args) throws IOException, InterruptedException {
    readpipes.put("main",System.in);
    writepipes.put("main",System.out);

    set_core_value("home_directory",new java.io.File("").getAbsolutePath());
    set_core_value("configuration_file","config.txt");
    set_core_value("message_count","0");
    parse_arguments(args);

    serverConnection = createConnection("chat.freenode.net",6667);
    login();

    while(sleep(10)) {

      //-----//-----// Read from server //-----//-----//
      fromServer.clear();
      int numberBytesRead = serverConnection.read(fromServer);

      if (numberBytesRead == -1) {
	error_output("IRC connection died.");
	if(get_core_value("staydead") != "1") { reconnect(); }
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
	    String pipeid = "fork" + get_core_value("message_count");
	    run_command(pipeid,"perl /home/derek/source/gambot/parsers/plugin_parser/jane.pl");
	    send_pipe_message(pipeid,"Gambeanbot");
	    send_pipe_message(pipeid,lines[i]);
	    Integer message_count = Integer.parseInt(core.get("message_count")) + 1;
	    set_core_value("message_count",message_count.toString());
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
	  System.out.println(incoming);
	}
      }
    }
  }
}