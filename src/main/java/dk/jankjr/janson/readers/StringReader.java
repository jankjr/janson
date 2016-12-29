package dk.jankjr.janson.readers;

/**
 * Created by jankjr on 23/12/2016.
 */
public class StringReader implements Reader {
  public final char [] input;
  public int position = 0;

  public StringReader(String input) {this.input = input.toCharArray();}

  public int nextCodepoint() {
    int chr = Character.codePointAt(input, position);
    position += Character.charCount(chr);
    return chr;
  }

  public char next() {
    return input[position++];
  }

  public char peek() {
    return input[position];
  }

  public void advance() {
    position ++;
  }

}
