package dk.jankjr.janson.writers;

import java.io.IOException;

/**
 * Created by jankjr on 27/12/2016.
 */
public interface Writer {
  void append(String src) throws IOException;
  void append(int src) throws IOException;
  void appendCodePoint(int src) throws IOException;
}
