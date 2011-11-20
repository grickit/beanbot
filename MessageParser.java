import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MessageParser {
  private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  public static void main(String[] args) throws IOException, InterruptedException {
    String pipe_id = stdin.readLine().trim();
    String bot_name = stdin.readLine().trim();
    String incoming_message = stdin.readLine().trim();

    if(pipe_id.equals("fork20")) System.out.println("send_server_message>JOIN ##Gambot");

    String valid_nick_characters = "A-Za-z0-9[\\]\\`_^{}|-";
    String valid_chan_characters = "#"+valid_nick_characters;
    String valid_human_sender_regex = "([."+valid_nick_characters+"]+)!~?([."+valid_nick_characters+"]+)@(.+?)";

    Matcher matcher;

    if((matcher = Pattern.compile("^PING(.*)$").matcher(incoming_message)).matches()) {
      on_ping(matcher.group(1),"","","","","");
    }

  }

  public static void on_ping(String sender, String account, String hostname, String command, String target, String message) {
    System.out.println("send_server_message>PONG"+sender);
  }
  public static void on_private_message(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_public_message(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_private_notice(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_public_notice(String sender, String account, String hostname, String command, String target, String message) {

  }
  public static void on_join(String sender, String account, String hostname, String command, String target, String message) {

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