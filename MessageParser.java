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

    String sender, account, hostname, command, target, message;

    if((matcher = Pattern.compile("^PING(.*)$").matcher(incoming_message)).matches()) {
      System.out.println("send_server_message>PONG"+matcher.group(1));
      sender = account = hostname = command = target = message = "";
      //server_ping event
    }

  }
}