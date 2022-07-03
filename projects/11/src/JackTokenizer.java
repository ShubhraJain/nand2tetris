import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.List;

public class JackTokenizer implements AutoCloseable {
  StringReader code;
  List<String> keywords = Arrays.asList(
                          "class", "constructor", "function", "method", "field",
                          "static", "var", "int", "char", "boolean", "void", "true", "false",
                          "null", "this", "let", "do", "if", "else", "return", "while");
  String symbols = "{}()[].,;+-*/&|<>=~";
  TokenizerState state;
  StringBuilder tempToken = new StringBuilder("");
  String currentToken = "";
  boolean tokenConsumed = false;
  TokenType tokenType = TokenType.TOKEN_NONE;

  private enum TokenizerState {
    START,
    IDENTIFIER,
    SYMBOL,
    INTEGER,
    SLASH,
    COMMENT,
    STRING,
    ERROR,
  }

  public JackTokenizer(File file) throws IOException {
    // read entire file into a string
    Scanner scanner = new Scanner(file);
    String text = scanner.useDelimiter("\\A").next();
    scanner.close(); 
    // Remove block comments
    String partialFiltered = text.replaceAll("/\\*([\\S\\s]+?)\\*/", "");
    // Remove line comments
    code = new StringReader(partialFiltered.replaceAll("//([\\S\\s]+?)\\n", ""));
    state = TokenizerState.START;
  }

  public boolean hasMoreTokens() throws IOException {
    code.mark(1000000);
    int c = 0;
    while ((c = code.read()) != -1) {
      if (!Character.isWhitespace((char) c) && ((char) c) != '\n') {
        code.reset();
        return true;
      }
    }
    return false;
  }

  private boolean isWhitespaceOrNewline(char c) {
    return Character.isWhitespace(c) || c == '\n';
  }

  private void handleStart(char c) {
    if (isWhitespaceOrNewline(c)) {
      return;
    }
    if (Character.isAlphabetic(c) || c == '_') {
      state = TokenizerState.IDENTIFIER;
      tempToken.append(c);
      return;
    }
    if (Character.isDigit(c)) {
      state = TokenizerState.INTEGER;
      tempToken.append(c);
    }
    if (symbols.indexOf(c) != -1) {
      state = TokenizerState.SYMBOL;
      tempToken.append(c);
    }
    if (c == '"') {
      state = TokenizerState.STRING;
    }
  }

  public String getToken() {
    return currentToken;
  }
  public TokenType getTokenType() {
    return tokenType;
  }

  public String getTypeText() {
    String typeText = "";
    if (getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      typeText = "<identifier> " + getToken() + " </identifier>";
    } else if (getTokenType() == TokenType.TOKEN_KEYWORD) {
      typeText = "<keyword> " + getToken() + " </keyword>";
    } else if (getTokenType() == TokenType.TOKEN_SYMBOL) {
      typeText = "<symbol> " + getSymboltext() + " </symbol>";
    } else if (getTokenType() == TokenType.TOKEN_INTEGER) {
      typeText = "<integerConstant> " + getToken() + " </integerConstant>";
    } else if (getTokenType() == TokenType.TOKEN_STRING) {
      typeText = "<stringConstant> " + getToken() + " </stringConstant>";
    } else {
      typeText = "Error: Wrong token passed";
    }
    return typeText;
  }

  private String getSymboltext() {
    if ("&".equals(getToken())) {
      return "&amp;";
    } else if ("<".equals(getToken())) {
      return "&lt;";
    } else if (">".equals(getToken())) {
      return "&gt;";
    } else {
      return getToken();
    }
  }

  private void finishToken(boolean resetCode) throws IOException {
    currentToken = tempToken.toString();
    tempToken.setLength(0);
    // set tokenType based on current state and currentToken
    setTokenType();
    if (resetCode) {
      try {
        code.reset();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    state = TokenizerState.START;
    tokenConsumed = true;
  }

  private void handleIdentifier(char c) throws IOException {
    if (Character.isLetterOrDigit(c) || c == '_') {
      tempToken.append(c);
      return;
    }
    if (isWhitespaceOrNewline(c) || symbols.indexOf(c) != -1) {
      finishToken(true);
    }
  }

  private void handleInteger(char c) throws IOException {
    if (Character.isDigit(c)) {
      tempToken.append(c);
      return;
    }
    if (isWhitespaceOrNewline(c)) {
      finishToken(true);
    }
    if (symbols.indexOf(c) != -1) {
      finishToken(true);
    }
  }

  private void handleSymbol(char c) throws IOException {
    if (c == '"' ) {
      finishToken(false);
      handleStart(c);
      return;
    }
    if (isWhitespaceOrNewline(c) || state == TokenizerState.SYMBOL) {
      finishToken(true);
    }
  }

  private void handleString(char c) throws IOException {
    if (c == '"') {
      finishToken(false);
      return;
    }
    tempToken.append(c);
  }

  private void consumeChar() throws IOException {
    code.mark(100);
    char c = (char) code.read();
    switch(state) {
      case START:
        handleStart(c);
        break;
      case IDENTIFIER:
        handleIdentifier(c);
        break;
      case INTEGER:
        handleInteger(c);
        break;
      case SYMBOL:
        handleSymbol(c);
        break;
      case STRING:
        handleString(c);
        break;
      default:
        break;
    }
  }

  public void advance() throws IOException {
    tokenConsumed = false;
    while (!tokenConsumed) {
      consumeChar();
    }
  }

  public void setTokenType() {
    switch (state) {
      case IDENTIFIER:
        tokenType = keywords.contains(currentToken) ? TokenType.TOKEN_KEYWORD : TokenType.TOKEN_IDENTIFIER;
        break;
      case INTEGER:
        tokenType = TokenType.TOKEN_INTEGER;
        break;
      case SYMBOL:
        tokenType = TokenType.TOKEN_SYMBOL;
        break;
      case STRING:
        tokenType = TokenType.TOKEN_STRING;
        break;
      default:
        tokenType = TokenType.TOKEN_NONE;
    }
  }

  public void close() {
    // scanner.close();
  }  
}
