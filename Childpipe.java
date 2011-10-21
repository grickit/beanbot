import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Process;
import java.lang.Runtime;

class monitor implements Runnable {
  Childpipe childpipe;
  public void monitor(Childpipe foo) { childpipe = foo; }

  public void run() {
    try { childpipe.process().waitFor(); }
    catch (InterruptedException e) { }
  }

  public void main() throws IOException {
    childpipe.kill();
  }
}

public class Childpipe {
  private boolean alive = true;
  private Process process;
  private InputStream output;
  private OutputStream input;
  private BufferedReader reader;
  private monitor thread;

  public void childpipe(String command) throws IOException {
    process = Runtime.getRuntime().exec(command);
    output = process.getInputStream();
    input = process.getOutputStream();
    reader = new BufferedReader(new InputStreamReader(output));
    thread = new monitor();
  }

  public void kill() throws IOException {
    alive = false;
    output.close();
    input.close();
    process.destroy();
  }

  public void writeLine(String message) throws IOException {
    input.write((message + "\n").getBytes());
    input.flush();
  }

  public boolean alive() { return alive; }

  public Process process() { return process; }

  public InputStream output() { return output; }

  public OutputStream input() { return input; }

  public BufferedReader reader() { return reader; }

  public String readLine() throws IOException {
    if(reader.ready() == true) { return reader.readLine(); }
    else { return null; }
  }
}