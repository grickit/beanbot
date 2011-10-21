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
  private monitor thread;

  public void childpipe(String command) throws IOException {
    process = Runtime.getRuntime().exec(command);
    output = process.getInputStream();
    input = process.getOutputStream();
    thread = new monitor();
  }

  public void kill() throws IOException {
    alive = false;
    output.close();
    input.close();
    process.destroy();
  }

  public void message(String message) throws IOException {
    input.write((message + "\n").getBytes());
    input.flush();
  }

  public boolean alive() { return alive; }

  public Process process() { return process; }

  public InputStream output() { return output; }

  public OutputStream input() { return input; }
}