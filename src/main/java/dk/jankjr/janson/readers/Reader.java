package dk.jankjr.janson.readers;

/**
 * Created by jankjr on 23/12/2016.
 */
public interface Reader {
  static void skipWhiteSpace(Reader stream){
    while(Character.isWhitespace(stream.peek())) stream.advance();
  }

  static boolean advanceIf(Reader stream, char c){
    if(stream.peek() == c){
      stream.advance();
      return true;
    }
    return false;
  }

  static void failIfNot(Reader stream, char c){
    if(!advanceIf(stream, c)){
      throw new RuntimeException("Invalid json input");
    }
  }

  static boolean isDigit(Reader stream){
    return stream.peek() >= '0' && stream.peek() <= '9';
  }

  static int readHex(Reader stream) {
    if(stream.peek() >= 'a' && stream.peek() <= 'f') return stream.next() - 'a';
    if(stream.peek() >= 'A' && stream.peek() <= 'F') return stream.next() - 'A';
    if(stream.peek() >= '0' && stream.peek() <= '9') return stream.next() - '0';
    throw new RuntimeException("Invalid json format");
  }

  int nextCodepoint();
  char next();

  char peek();
  void advance();
}
