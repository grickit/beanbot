import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class MessageParser {
  private static BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  public static void main(String[] args) throws IOException, InterruptedException {
    String pipe_id = stdin.readLine();
    String name = stdin.readLine();
    String message = stdin.readLine();
  }
}