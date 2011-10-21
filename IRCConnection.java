import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class IRCConnection {
  private boolean alive = true;
  private Socket connection;
  private InputStream output;
  private OutputStream input;
  private BufferedReader reader;

  public IRCConnection(String server, int port) throws IOException, InterruptedException {
    connection = new Socket(server,port);
    while(!connection.isConnected()) { }
    output = connection.getInputStream();
    input = connection.getOutputStream();
    reader = new BufferedReader(new InputStreamReader(output));
  }

  public void writeLine(String message) throws IOException {
    input.write((message + "\n").getBytes());
    input.flush();
  }

  public boolean alive() { return !(connection.isClosed()); }

  public InputStream output() { return output; }

  public OutputStream input() { return input; }

  public BufferedReader reader() { return reader; }

  public String readLine() throws IOException {
    if(reader.ready() == true) { return reader.readLine(); }
    else { return null; }
  }

  public void login(String nick, String user) throws IOException {
    this.writeLine("NICK " + nick + "\n");
    this.writeLine("USER " + user + " 8 * :Java Beanbot" + "\n");
  }
}