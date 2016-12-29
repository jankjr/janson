package dk.jankjr.janson.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jankjr on 27/12/2016.
 */
public class InputStreamReader implements Reader {
  public final int BUFF_SIZE;

  private final BufferedReader inp;
  private final char [] buffer;
  private int positionInBuffer;
  private int sizeOfBuffer;

  public InputStreamReader(InputStream inp, int bufferSize) {
    BUFF_SIZE = bufferSize;
    buffer = new char [BUFF_SIZE / 2];
    positionInBuffer = BUFF_SIZE;
    sizeOfBuffer = BUFF_SIZE;
    this.inp = new BufferedReader(new java.io.InputStreamReader(inp));
  }

  public InputStreamReader(InputStream inp) {
    this(inp, 1 << 12);
  }
  private void readSomeMore() throws IOException {
    if(positionInBuffer < buffer.length){
      return;
    }
    inp.read(buffer);
    positionInBuffer = 0;

  }

  public int nextCodepoint() throws IOException {
    readSomeMore();
    int chr = Character.codePointAt(buffer, positionInBuffer);
    positionInBuffer += Character.charCount(chr);
    return chr;
  }

  public char next() throws IOException {
    readSomeMore();
    return buffer[positionInBuffer++];
  }

  public char peek() throws IOException {
    readSomeMore();
    return buffer[positionInBuffer];
  }

  public void advance() throws IOException {
    readSomeMore();
    positionInBuffer ++;
  }
}
