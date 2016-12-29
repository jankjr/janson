package dk.jankjr.janson.writers;

/**
 * Created by jankjr on 27/12/2016.
 */
public interface Writer {
  void append(String src);
  void append(int src);

  void appendCodePoint(int src);
}
