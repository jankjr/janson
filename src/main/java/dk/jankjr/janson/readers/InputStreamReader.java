package dk.jankjr.janson.readers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * Created by jankjr on 27/12/2016.
 */
public class InputStreamReader implements Reader {
  public final int BUFF_SIZE;
  public final ByteOrder order;

  private final InputStream inp;
  private final char [] buffer;
  private int positionInBuffer;
  private int sizeOfBuffer;

  public InputStreamReader(InputStream inp, int bufferSize, ByteOrder order) {
    BUFF_SIZE = bufferSize;
    this.order = order;
    buffer = new char [BUFF_SIZE / 2];
    positionInBuffer = BUFF_SIZE;
    sizeOfBuffer = BUFF_SIZE;
    this.inp = inp;
  }

  public InputStreamReader(InputStream inp) {
    this(inp, 1 << 12, ByteOrder.BIG_ENDIAN);
  }
  private void readSomeMore() {
    if(positionInBuffer < sizeOfBuffer) return;
    positionInBuffer = 0;
    sizeOfBuffer = 0;
    for(int i = 0 ; i < BUFF_SIZE ; i += 2){
      final int b0, b1;
      try {
        b0 = inp.read();
        b1 = inp.read();
        if(b0 == -1 || b1 == -1) {
          break;
        }
        if(order == ByteOrder.LITTLE_ENDIAN) {
          buffer[i] = (char) ((b1 << 8) | b0);
        } else {
          buffer[i] = (char) ((b0 << 8) | b1);
        }
        sizeOfBuffer ++;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public int nextCodepoint() {
    readSomeMore();
    int chr = Character.codePointAt(buffer, positionInBuffer);
    positionInBuffer += Character.charCount(chr);
    return chr;
  }

  public char next() {
    readSomeMore();
    return buffer[positionInBuffer++];
  }

  public char peek() {
    readSomeMore();
    return buffer[positionInBuffer];
  }

  public void advance() {
    readSomeMore();
    positionInBuffer ++;
  }
}
