package dk.jankjr.janson.writers;

/**
 * Created by jankjr on 27/12/2016.
 */
public class StringBufferWriter implements Writer {
  private StringBuffer buff = new StringBuffer();

  @Override
  public void append(String src) {
    buff.append(src);
  }

  @Override
  public void append(int src) {
    buff.append(src);
  }

  @Override
  public void appendCodePoint(int src) {
    buff.appendCodePoint(src);
  }

  public StringBuffer getBuffer(){
    return buff;
  }
}
