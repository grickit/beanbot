import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MessageParser {
  private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private static String pipe_id = "";
    private static String bot_name = "";
    private static String incoming_message = "";

  public static void main(String[] args) throws IOException, InterruptedException {
    pipe_id = stdin.readLine().trim();
    bot_name = stdin.readLine().trim();
    incoming_message = stdin.readLine().trim();

    if(pipe_id.equals("fork20")) System.out.println("send_server_message>JOIN ##Gambot");

    String valid_nick_characters = "A-Za-z0-9[\\]\\`_^{}|-";
    String valid_chan_characters = "#A-Za-z0-9[\\]\\`_^{}|-";
    String valid_human_sender_regex = "([.A-Za-z0-9[\\]\\`_^{}|-]+)!~?([.A-Za-z0-9[\\]\\`_^{}|-]+)@(.+?)";

    Matcher matcher;

    if((matcher = Pattern.compile("^PING(.*)$").matcher(incoming_message)).matches()) {
      on_ping(matcher.group(1),"","","","","");
    }
    else if((matcher = Pattern.compile("^:([.A-Za-z0-9`_^{}|-]+)!(~?[.A-Za-z0-9`_^{}|-]+)@(.+?) .+$").matcher(incoming_message)).matches()) {
      String sender = matcher.group(1);
      String user = matcher.group(2);
      String address = matcher.group(3);
      System.out.println("send_server_message>PRIVMSG ##Gambot :sender: "+sender+" user: "+user+" address: "+address);
    }
  }

  public static void on_ping(String sender, String account, String hostname, String command, String target, String message) {
    System.out.println("send_server_message>PONG"+sender);
  }
  public static void on_private_message(String sender, String account, String hostname, String command, String target, String message) {
    //We do a bit of redirection on private messages so they can be processed the same as public ones.
    //The channel is the sender of the private message
    //The message is prefixed with the bot's name
    on_public_message(sender,account,hostname,command,sender,bot_name+": "+message);
  }
  public static void on_public_message(String sender, String account, String hostname, String command, String target, String message) {
    System.out.println("send_server_message>PRIVMSG "+target+" :"+message);
  }
  public static void on_private_notice(String sender, String account, String hostname, String command, String target, String message) {
    //Handle private notices just like private messages
    on_public_notice(sender,account,hostname,command,sender,bot_name+": "+message);
  }
  public static void on_public_notice(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_join(String sender, String account, String hostname, String command, String target) {

  }
  public static void on_part(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_quit(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_mode(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_nick(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_kick(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_server_message(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_error(String sender, String account, String hostname, String command, String target, String message) {

  }
}