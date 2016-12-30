package dk.jankjr.janson.readers;

import java.io.IOException;

/**
 * Created by jankjr on 23/12/2016.
 */
public interface Reader {
  static void skipWhiteSpace(Reader stream) throws IOException {
    while(Character.isWhitespace(stream.peek())) stream.advance();
  }

  static boolean advanceIf(Reader stream, char c) throws IOException {
    if(stream.peek() == c){
      stream.advance();
      return true;
    }
    return false;
  }

  static void failIfNot(Reader stream, char c) throws IOException {
    if(!advanceIf(stream, c)){
      throw new RuntimeException("Invalid json input");
    }
  }

  static boolean isDigit(Reader stream) throws IOException {
    return stream.peek() >= '0' && stream.peek() <= '9';
  }

  static int readHex(Reader stream) throws IOException {
    if(stream.peek() >= 'a' && stream.peek() <= 'f') return stream.next() - 'a';
    if(stream.peek() >= 'A' && stream.peek() <= 'F') return stream.next() - 'A';
    if(stream.peek() >= '0' && stream.peek() <= '9') return stream.next() - '0';
    throw new RuntimeException("Invalid json format");
  }

  int nextCodepoint() throws IOException;
  char next() throws IOException;

  char peek() throws IOException;
  void advance() throws IOException;

  static boolean isNextDelimiter(Reader stream) throws IOException {
    char c = stream.peek();
    return Character.isWhitespace(c) || c == ',' || c == '}' || c == ']';
  }
}
