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

  public void flush() throws IOException {
    out.flush();
  }

  @Override
  public void append(String src) throws IOException {
    out.write(src);
  }

  @Override
  public void append(int src) throws IOException {
    out.write(Integer.toString(src));
  }

  @Override
  public void appendCodePoint(int src) throws IOException {
    out.write(src);
  }
}
