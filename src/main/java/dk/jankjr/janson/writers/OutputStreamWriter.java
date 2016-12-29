package dk.jankjr.janson.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Created by jankjr on 27/12/2016.
 */
public class OutputStreamWriter implements Writer{
  private final OutputStream out;
  private final ByteOrder order;

  public OutputStreamWriter(OutputStream out, ByteOrder order) {
    this.out = out;
    this.order = order;
  }
  public OutputStreamWriter(OutputStream out) {
    this(out, ByteOrder.BIG_ENDIAN);
  }

  @Override
  public void append(String src) {
    try {
      out.write(src.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void append(int src) {
    try {
      if(order != ByteOrder.BIG_ENDIAN){
        out.write(src >>> 8);
        out.write(src);
      } else  {
        out.write(src);
        out.write(src >>> 8);
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void appendCodePoint(int src) {
    try {

      if(order != ByteOrder.BIG_ENDIAN){
        out.write(src >>> 24);
        out.write(src >>> 16);
        out.write(src >>> 8);
        out.write(src);
      } else  {
        out.write(src);
        out.write(src >>> 8);
        out.write(src >>> 16);
        out.write(src >>> 24);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
