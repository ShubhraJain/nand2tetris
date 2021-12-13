import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

public class JackTokenizer implements AutoCloseable {
  boolean tokenFound, charFound, pairNotFound;
  Scanner scanner;
  String token = "", currentToken = "";
  FileReader fr;
  BufferedReader br;
  List<String> keywords = Arrays.asList(
                          "class", "constructor", "function", "method", "field",
                          "static", "var", "int", "char", "boolean", "void", "true", "false",
                          "null", "this", "let", "do", "if", "else", "return", "while");

  List<String> symbols = Arrays.asList("{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*",
                          "/", "&", "|", "<", ">", "=", "~");
  char character;
  enum TokenType {
    KEYWORD,
    SYMBOL,
    INTEGERCONSTANT,
    STRINGCONSTANT,
    IDENTIFIER
  }

  public JackTokenizer(File file) throws IOException {
    fr = new FileReader(file); 
    br = new BufferedReader(fr); 
  }

  // private void removeBlockComments(String line) {
  //   while (line.endsWith("*/")) {
  //     line = scanner.nextLine().trim();
  //   }
  // }

  public boolean hasMoreTokens() throws IOException{
    int c = 0;
    while(!charFound && ((c = br.read()) != -1)) {
      character = (char) c;
      charFound = true;
    }
    return charFound;
  }


  public void advance() {
    String ch = Character.toString(character);
    if (ch.equals("\n") || ch.equals(" ")) {
      currentToken = token;
      token = "";
    } else if (symbols.contains(ch)) {
      currentToken = token;
      token = ch;
    // } else if (character == '"' || stringProcessing) {
      
    } else {
      token += ch;
    }
    charFound = false;
    // if (currentToken.length() > 0) {
    //   System.out.println(currentToken);
    // }
    // System.out.println(tokenType());
    // tokenType();
    // currentToken = "";
  }

  public String tokenType() {
    if (currentToken.length() > 0) {

      Pattern identifierPattern = Pattern.compile("[a-zA-Z_][\\w]*");
      Matcher identifierMatcher = identifierPattern.matcher(currentToken);
      boolean identifierFound = identifierMatcher.find();

      Pattern integerPattern = Pattern.compile("[0-9][0-9]*");
      Matcher integerMatcher = integerPattern.matcher(currentToken);
      boolean integerFound = integerMatcher.find();
      
      if (symbols.contains(currentToken)) {
        return TokenType.SYMBOL.name();
      } else if (identifierFound) {
        return TokenType.IDENTIFIER.name();
      } else if (integerFound) {
        return TokenType.INTEGERCONSTANT.name();
      }
    }
    return "";
  }

  public void close() throws IOException{
    br.close();
  }
}




    // while(!tokenFound && scanner.hasNextLine() && !tokenRemainingInLine) {
    //   String programLine = scanner.nextLine().trim();
    //   if (programLine.startsWith("/**")) {
    //     removeBlockComments(programLine);
    //   }
    //   if (!programLine.isEmpty() && !programLine.startsWith("//")) {
    //     while (scanner.hasNext()) {
    //       System.out.println(scanner.next());
    //     }
        // if (scanner.hasNext()) {
        //   token = scanner.next();
        //   tokenRemainingInLine = true;
        // }
        // tokenFound = true;
    //   }
    // }
    // if (tokenRemainingInLine) {
    //   token = scanner.next();
    // }
    // return tokenFound;
  // }