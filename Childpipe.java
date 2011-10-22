import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Process;
import java.lang.Runtime;

public class Childpipe {

  class monitor implements Runnable {
    Childpipe childpipe;
    public monitor(Childpipe foo) { childpipe = foo; }

    public void run() {
      try {
	String incoming = childpipe.reader().readLine();
	while(incoming != null) {
	  childpipe.push(incoming);
	  incoming = childpipe.reader().readLine();
	}
	childpipe.preKill();
      }
      catch (IOException e) { }
    }
  }

  private boolean alive = true;
  private Process process;
  private InputStream output;
  private OutputStream input;
  private BufferedReader reader;
  private String backlog = "";

  public Childpipe(String command) throws IOException {
    process = Runtime.getRuntime().exec(command);
    output = process.getInputStream();
    input = process.getOutputStream();
    reader = new BufferedReader(new InputStreamReader(output));
    (new Thread(new monitor(this))).start();
  }

  private void preKill() {
    alive = false;
  }

  public void kill() throws IOException {
    alive = false; // Probably not needed?
    output.close();
    input.close();
    process.destroy();
  }

  public void writeLine(String message) throws IOException {
    input.write((message + "\n").getBytes());
    input.flush();
  }

  private void push(String message) {
    backlog = backlog + message + "\n";
  }

  public boolean alive() {
    if(alive) return true;
    else if (backlog == "") return false;
    else return true;
  }

  public Process process() { return process; }

  public InputStream output() { return output; }

  public OutputStream input() { return input; }

  public BufferedReader reader() { return reader; }

  public String[] getLines() throws IOException {
    String foo = backlog;
    backlog = "";
    return foo.split("[\r\n]+");
  }
}