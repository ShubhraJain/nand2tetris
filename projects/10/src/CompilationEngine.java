import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CompilationEngine {
  private StringBuilder sb;
  private JackTokenizer tokenizer;
  TokenType tokenType = TokenType.TOKEN_NONE;
  private boolean alreadyAdvanced;
  List<String> expKeywords = Arrays.asList("true", "false", "null", "this"); 

  String nonTerminalSymbols = "{}()[.,+-*/&|<>=~";
  
  public CompilationEngine(JackTokenizer tokenizer) {
    this.tokenizer = tokenizer;
    sb = new StringBuilder();
  }

  public void startCompilation() throws IOException {
    while (tokenizer.hasMoreTokens()) {
    // tokenizer.advance();
    //   if (tokenizer.getTokenType() == TokenType.TOKEN_KEYWORD 
    //   && tokenizer.getToken().equals("class")) {
      compileStatement();
    }
    // }
  }

  public String getFinalXML() throws IOException {
    return sb.toString();
  }

  private void compileClass() throws IOException {
    tokenizer.advance();
    if (tokenizer.getTokenType() == TokenType.TOKEN_KEYWORD 
      && getToken().equals("class")) {
      openTag("class");
      appendTypeText();
      tokenizer.advance();
      if (tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in class name");
        return;
      }
      if (tokenizer.getTokenType() == TokenType.TOKEN_SYMBOL) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in class bracket");
        return;
      }
      compileClassVarDec();
      compileStatement();
      closeTag("class");
    } else {
      System.out.println("Class declaration is wrong");
      return;
    }
  }

  private void compileClassVarDec() throws IOException {
    while (getToken().equals("static") ||
           getToken().equals("field")) {
      openTag("classVarDec");
      appendTypeText();
      tokenizer.advance();
      if (getToken().equals("int") ||
          getToken(). equals("boolean") ||
          getToken().equals("char") ||
          tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
        appendTypeText();
      } else {
        System.out.println("Error in data type\n");
        return;
      }
      tokenizer.advance();
      if (tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in variable name\n");
        return;
      } 
    }
    while (tokenizer.getTokenType() == TokenType.TOKEN_SYMBOL) {
      if (getToken().equals(";")) {
        appendTypeText();
        tokenizer.advance();
        break;
      } else if (getToken().equals(",")) {
        appendTypeText();
        tokenizer.advance();
        if (tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
          appendTypeText();
          tokenizer.advance();
        } else {
          System.out.println("Error in variable declaration\n");
          return;
        }
      } else {
        System.out.println("Error in variable declaration");
        return;
      }
    }
    closeTag("classVarDec");
  }

  private void compileSubroutineDec() throws IOException {

  }

  private void compileStatement() throws IOException {
    advance(); // remove it once stitched together
    if (getToken().equals("let")) {
      compileLetStatement();
    } else if (getToken().equals("while")) {
      compileWhileStatement();
    } else if (getToken().equals("do")) {
      compileDoStatement();
    } else if (getToken().equals("return")) {
      compileReturnStatement();
    } else if (getToken().equals("if")) {
      compileIfStatement();
    } else {
      System.out.println("Invalid statement");
      return;
    }
  }

  private void compileStatements() throws IOException {
    if ("{".equals(getToken())) {
      appendTypeText();
      openTag("statements");
      advance();
      alreadyAdvanced = true;
      while (!"}".equals(getToken())) {
        compileStatement();
        advance();
        alreadyAdvanced = true;
      }
      closeTag("statements");
      if ("}".equals(getToken())) {
        appendTypeText();
        alreadyAdvanced = false;
      } else {
        System.out.println("Error in statement. Expected '}'");
      }
    } else {
      System.out.println("Error in statement. Expected '{'");
    }
  }

  private void compileLetStatement() throws IOException {
    openTag("letStatement");
    appendTypeText();
    advance();
    if (getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      appendTypeText();
    } else {
      System.out.println("Error in Let statement: invalid identifier");
      return;
    }
    advance();
    alreadyAdvanced = true;
    if ("[".equals(getToken())) {
      alreadyAdvanced = false;
      compileArrayOrParenExp();
    }
    advance();
    if (getToken().equals("=")) {
      appendTypeText();
    } else {
      System.out.println("Error in Let statement: Expected =");
    }
    compileExpression();
    advance();
    if (getToken().equals(";")) {
      appendTypeText();
    } else {
      System.out.println("Error in Let statement: Expected ';'");
    } 
    closeTag("letStatement");
  }

  private void compileIfStatement() throws IOException {
    openTag("ifStatement");
    appendTypeText();
    advance();
    compileArrayOrParenExp();
    advance();
    compileStatements();
    advance();
    alreadyAdvanced = true;
    if ("else".equals(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
      advance();
      compileStatements();
    }
    closeTag("ifStatement");
  }

  private void compileWhileStatement() throws IOException {
    openTag("whileStatement");
    appendTypeText();
    advance();
    compileArrayOrParenExp();
    advance();
    compileStatements();
    closeTag("whileStatement");
  }

  private void compileDoStatement() throws IOException {
    openTag("doStatement");
    appendTypeText();
    compileTerm();
    advance();
    if (";".equals(getToken())) {
      appendTypeText();
    } else {
      System.out.println("Error in do statement: Expected ';'");
    }
    closeTag("doStatement");
  }
  
  private void compileReturnStatement() throws IOException {
    openTag("returnStatement");
    appendTypeText();
    advance();
    alreadyAdvanced = true;
    if (!";".equals(getToken())) {
      compileExpression();
    }
    advance();
    if (";".equals(getToken())) {
      appendTypeText();
    } else {
      System.out.println("Error in Return statement: Expected ';'");
    }
    closeTag("returnStatement");
  }

/*  
  private void compileTerm() throws IOException {
    sb.append("<term>\n");
    if ("-~".contains(tokenizer.getToken())) {
      appendTypeText();
      tokenizer.advance();
      compileTerm();
    } else if (tokenizer.getToken().equals("(")) {
      appendTypeText();
      tokenizer.advance();
      compileExpression();
      if (!alreadyAdvanced) {
        tokenizer.advance();
      }
      if (tokenizer.getToken().equals(")")) {
        appendTypeText();
      } else {
        System.out.println("Exp Err: Expected )");
      }
    } else if (tokenizer.getTokenType() == TokenType.TOKEN_INTEGER ||
               tokenizer.getTokenType() == TokenType.TOKEN_STRING ||
               expKeywords.contains(tokenizer.getToken())) {
      appendTypeText();
    } else if (tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER ||
               tokenizer.getTokenType() == TokenType.TOKEN_KEYWORD) {
      appendTypeText();
      tokenizer.advance();
      if ("[.(".contains(tokenizer.getToken())) {
        alreadyAdvanced = false;
        if (tokenizer.getToken().equals("[")) {
          appendTypeText();
          tokenizer.advance();
          compileExpression();
          tokenizer.advance();
          if (tokenizer.getToken().equals("]")) {
            appendTypeText();
          } else {
            System.out.println("Exp Err: Expected ] to close the array");
          }
        } else if (tokenizer.getToken().equals(".")) {
          appendTypeText();
          tokenizer.advance();
          if (tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
            appendTypeText();
            tokenizer.advance();
            if (tokenizer.getToken().equals("(")) {
              appendTypeText();
              tokenizer.advance();
              compileSubroutineCall();
              System.out.println(tokenizer.getToken() + alreadyAdvanced);
            } else {
              System.out.println("Exp Err: Expected ( to start the expression list");
            }
          } else {
            System.out.println("Exp Err: Expected a subroutine name");
          }
        } else if (tokenizer.getToken().equals("(")) {
          appendTypeText();
          appendTypeText();
          tokenizer.advance();
          compileSubroutineCall();
        }
      } else {
        alreadyAdvanced = true;
      }
    }
    sb.append("</term>\n");
  }
*/

  private void compileTerm() throws IOException {
    openTag("term");
    advance();
    switch (getTokenType()) {
      case TOKEN_INTEGER:
        compileIntegerTerm();  
        break;

      case TOKEN_STRING:
        compileStringTerm();
        break;

      case TOKEN_KEYWORD:
        if (expKeywords.contains(getToken())) {
          compileKeywordTerm();    
        } else {
          System.out.println("'" + getToken() + "'" + " is not a valid keyword constant");
        }
        break;

      case TOKEN_SYMBOL:
        if ("-~".contains(getToken())) {
          compileUnaryOPTerm();
        } else if ("(".equals(getToken())) {
          compileArrayOrParenExp();
        } else {
          System.out.println("'" + getToken() + "'" + " is not a valid symbol");
        }
        break;
      
      case TOKEN_IDENTIFIER:
        compileVarTerm();
        advance();
        switch (getToken()) {
          case ";":
          case "]":
          case ")":
          case "+":
          case "-":
          case "*":
          case "/":
          case "&":
          case "|":
          case "<":
          case ">":
          case "=":
          case ",":
            alreadyAdvanced = true;
            break;
          case "(":
              compileFunctionTerm();
            break;
          case "[":
            compileArrayOrParenExp();
            break;
          case ".":
            compileMethodCall();
            break;
          default:
            System.out.println(getToken() + " is not a valid term");
        }
    }

    closeTag("term");
  }

  private void advance() throws IOException {
    if (!alreadyAdvanced) {
      tokenizer.advance();
    }
    alreadyAdvanced = false;
  }
  private String getToken() {
    return tokenizer.getToken();
  }
  private TokenType getTokenType() {
    return tokenizer.getTokenType();
  }

  private void compileExpression() throws IOException {
    openTag("expression");
    compileTerm();
    advance();
    alreadyAdvanced = true;
    while ("+-*/&|<>=".contains(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
      compileTerm();
    }
    closeTag("expression");
  }

  private void diagnose(String name) {
    System.out.print("Compiling "+name);
    System.out.print("  advanced="+alreadyAdvanced);
    System.out.println("   token="+getToken());
  }

  private void openTag(String name) throws IOException {
    sb.append("<"+name+">\n");
  }
  private void closeTag(String name) throws IOException {
    sb.append("</"+name+">\n");
  }

  public void close() throws IOException {
    // startCompilation();
    System.out.println(sb);
  }

  private void compileIntegerTerm() throws IOException {
    appendTypeText();
  } 
  
  private void compileStringTerm() throws IOException {
    appendTypeText();
  } 
  
  private void compileKeywordTerm() throws IOException {
    appendTypeText();
  }
  
  private void compileArrayOrParenExp() throws IOException {
    // token has already been advanced and is expected to be '[' or '('
    String closingCounterpart = null; // can a string be initialized to null?
    if ("(".equals(getToken())) {
      closingCounterpart = ")";
    } else if ("[".equals(getToken())) {
      closingCounterpart = "]";
    }
    appendTypeText();
    compileExpression();
    advance();
    if (closingCounterpart.equals(getToken())) {
      appendTypeText();
    } else {
      System.out.println("Error in term. Expected '" + closingCounterpart + "'.");
    }
  }

  private void compileMethodCall() throws IOException {
    // token has already been advanced and is expected to be '.'
    appendTypeText();
    advance();
    if (getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      appendTypeText();
      advance();
      if ("(".equals(getToken())) {
        compileFunctionTerm();
      } else {
        System.out.println("Error in method call. Expected '('.");
      }
    }
  }

  private void compileFunctionTerm() throws IOException {
    // token has already been advanced and is expected to be '('
    appendTypeText();
    openTag("expressionList");
    advance();
    alreadyAdvanced = true;
    while(!")".equals(getToken())) {
      compileExpression();
      advance();
      alreadyAdvanced = true;
      if (",".equals(getToken())) {
        appendTypeText();
        alreadyAdvanced = false;
      }
    }
    if (")".equals(getToken())) {
      closeTag("expressionList");
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in expression list");
    }
  } 

  private void compileUnaryOPTerm() throws IOException {
    // token has already been advanced and is expected to be an op term
    appendTypeText();
    compileTerm();
  }

  private void compileVarTerm() throws IOException {
    appendTypeText();
  }

  private void appendTypeText() throws IOException {
    sb.append(tokenizer.getTypeText() + "\n");
  }
}

/*
  private void compileArrayOrParenExpOrFuncTerm(boolean isExpressionList) throws IOException {
    // token has already been advanced and is expected to be '[' or '('
    String closingCounterpart = null; // can a string be initialized to null?
    if ("(".equals(getToken())) {
      closingCounterpart = ")";
    } else if ("[".equals(getToken())) {
      closingCounterpart = "]";
    }
    appendTypeText();
    if (isExpressionList) {
      compileExpressionList();
    } else {
      compileExpression();
    }
    advance();
    if (closingCounterpart.equals(getToken())) {
      appendTypeText();
    } else {
      System.out.println("Error in term. Expected '" + closingCounterpart + "'.");
    }
  }

  private void compileFunctionTerm() throws IOException {
    // token has already been advanced and is expected to be '('
    diagnose("functionTerm");
    appendTypeText();
    openTag("expressionList");
    advance();
    alreadyAdvanced = true;
    while (!")".equals(getToken())) {
      compileExpression();
      advance();
      alreadyAdvanced = true;
      if (",".equals(getToken())) {
        appendTypeText();
        alreadyAdvanced = false;
      }
    }
    if (")".equals(getToken())) {
      closeTag("expressionList");
      appendTypeText();
      alreadyAdvanced = false;
    }
  } 
  */
