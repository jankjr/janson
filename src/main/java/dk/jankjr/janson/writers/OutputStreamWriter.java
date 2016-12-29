package dk.jankjr.janson.writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jankjr on 27/12/2016.
 */
public class OutputStreamWriter implements Writer{
  private final BufferedWriter out;

  public OutputStreamWriter(OutputStream out) {
    this.out = new BufferedWriter(new java.io.OutputStreamWriter(out));
  }

  public void flush(){
    try {
      out.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void append(String src) {
    try {
      out.write(src);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void append(int src) {
    try {
      out.write(Integer.toString(src));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void appendCodePoint(int src) {
    try {
      out.write(src);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
