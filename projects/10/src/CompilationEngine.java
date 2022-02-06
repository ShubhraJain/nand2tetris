import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CompilationEngine {
  private StringBuilder sb;
  private JackTokenizer tokenizer;
  TokenType tokenType = TokenType.TOKEN_NONE;
  private boolean alreadyAdvanced;
  private boolean emptyStatements = false;
  private boolean isParameterList = false;
  private String className;
  List<String> expKeywords = Arrays.asList("true", "false", "null", "this"); 
  List<String> statementNames = Arrays.asList("let", "do", "while", "if", "return");
  String nonTerminalSymbols = "{}()[.,+-*/&|<>=~";
  
  public CompilationEngine(JackTokenizer tokenizer) {
    this.tokenizer = tokenizer;
    sb = new StringBuilder();
  }

  public void startCompilation() throws IOException {
    while (tokenizer.hasMoreTokens()) {
      compileClass();
    }
  }

  public String getFinalXML() throws IOException {
    return sb.toString();
  }

  private void compileClass() throws IOException {
    tokenizer.advance();
    if(getToken().equals("class")) {
      openTag("class");
      appendTypeText();
      tokenizer.advance();
      if(tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
        className = tokenizer.getToken();
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in class name");
        return;
      }
      if(getToken().equals("{")) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in class bracket. Expected {");
        return;
      }
      if(getToken().equals("static") || getToken().equals("field")) {
        compileClassVarDec();
      }
      if(getToken().equals("constructor") || 
      getToken().equals("function") || 
      getToken().equals("method")) {
        compileSubroutineDec();
      }
      if(getToken().equals("}")) {
        appendTypeText();
      } else {
        System.out.println("Error in class closing brace. Got " + getToken());
        return;
      }
      closeTag("class");
    } else {
      System.out.println("Class declaration is wrong");
      return;
    }
    return;
  }

  private boolean isValidType(String typeName) throws IOException {
    return (typeName.equals("int") ||
            typeName. equals("boolean") ||
            typeName.equals("char") ||
            typeName.equals(className) ||
            tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER );
  }

  private void compileClassVarDec() throws IOException {
    while (getToken().equals("static") ||
           getToken().equals("field")) {
      openTag("classVarDec");
      appendTypeText();
      tokenizer.advance();
      compileTypeAndVar();
      if(getToken().equals(";")) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in class variable declaration. Expected ;");
        return;
      }
      closeTag("classVarDec");
    }
    return;
  }

  private void compileSubroutineDec() throws IOException {
    while (getToken().equals("constructor") ||
        getToken().equals("function") ||
        getToken().equals("method")) {
      openTag("subroutineDec");
      appendTypeText();
      tokenizer.advance();
      if(isValidType(getToken()) || getToken().equals("void")) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in subroutine declaration");
        return;
      }
      if(tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
        appendTypeText();
        tokenizer.advance();
      } else {
        System.out.println("Error in subroutine declaration. Expected subroutineName");
        return;
      }
      if(getToken().equals("(")) {
        appendTypeText();
        tokenizer.advance();
        compileParameterList();
        if(getToken().equals(")")) {
          appendTypeText();
          tokenizer.advance();
        } else {
          System.out.println("Error in parameter list. Expected )");
          return;
        }
      } else {
        System.out.println("Error in subroutine declaration. Expected (");
        return;
      }
      if(getToken().equals("{")) {
        openTag("subroutineBody");
        appendTypeText();
        tokenizer.advance();
        while (getToken().equals("var")) {
          openTag("varDec");
          appendTypeText();
          tokenizer.advance();
          compileTypeAndVar();
          if(getToken().equals(";")) {
            appendTypeText();
            closeTag("varDec");
            tokenizer.advance();
          } else {
            System.out.println("Error in var declaration. Expected ;, got " + getToken());
            return;
          }
        }
        compileStatements();
        if(!alreadyAdvanced) {
          tokenizer.advance();
        }
        if(getToken().equals("}")) {
          appendTypeText();
          tokenizer.advance();
        } else {
          System.out.println("Error in subroutine body. Expected }, got " + getToken());
          return;
        }
        closeTag("subroutineBody");
      } else {
        System.out.println("Error in subroutine body. Expected {, got " + getToken());
        return;
      }
      closeTag("subroutineDec");
    }
    return;
  }

  private void compileTypeAndVar() throws IOException {
    if(isValidType(getToken())) {
      appendTypeText();
    } else {
      System.out.println("Error in data type");
      return;
    }
    tokenizer.advance();
    if(tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      compileVarTerm();
      tokenizer.advance();
      while (getToken().equals(",")) {
        appendTypeText();
        tokenizer.advance();
        if(isParameterList) {
          if(isValidType(getToken())) {
            appendTypeText();
            tokenizer.advance();
          } else {
            System.out.println("Error in data type in parameter list");
            return;
          }
        }
        compileVarTerm();
        tokenizer.advance();
      }
    } else {
      System.out.println("Error in variable name");
      return;
    }
    return;
  }

  private void compileParameterList() throws IOException {
    isParameterList = true;
    openTag("parameterList");
    while(!")".equals(getToken())) {
      compileTypeAndVar();
    }
    closeTag("parameterList");
    isParameterList = false;
    return;
  }

  private void compileCurlyBraces(String brace, String errorPlace) throws IOException {
    if(brace.equals(getToken())) {
      appendTypeText();
      if("{".equals(getToken())) {
        tokenizer.advance();
      }
    } else {
      System.out.println("Error in " + errorPlace + ". Expected " + brace);
    }
    return;
  }

  private void compileStatements() throws IOException {
    openTag("statements");
    if("}".equals(getToken())) {
      alreadyAdvanced = true;
    }
    while(statementNames.contains(getToken())) {
      compileStatement();
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      alreadyAdvanced = true;
    }
    closeTag("statements");
    return;
  }

  private void compileStatement() throws IOException {
    if(getToken().equals("let")) {
      compileLetStatement();
    } else if(getToken().equals("while")) {
      compileWhileStatement();
    } else if(getToken().equals("do")) {
      compileDoStatement();
    } else if(getToken().equals("return")) {
      compileReturnStatement();
    } else if(getToken().equals("if")) {
      compileIfStatement();
    } else {
      System.out.println("Invalid statement");
      return;
    }
    return;
  }

  private void compileLetStatement() throws IOException {
    alreadyAdvanced = false;
    openTag("letStatement");
    appendTypeText();
    tokenizer.advance();
    if(getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      appendTypeText();
      tokenizer.advance();
    } else {
      System.out.println("Error in Let statement: invalid identifier");
      return;
    }
    if("[".equals(getToken())) {
      compileArrayOrParenExp();
      tokenizer.advance();
    }
    if(getToken().equals("=")) {
      appendTypeText();
      tokenizer.advance();
    } else {
      System.out.println("Error in Let statement: Expected =");
      return;
    }
    compileExpression();
    if(getToken().equals(";")) {
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in Let statement: Expected ';'" + getToken());
      return;
    } 
    closeTag("letStatement");
    return;
  }

  private void compileIfStatement() throws IOException {
    openTag("ifStatement");
    appendTypeText();
    tokenizer.advance();
    compileArrayOrParenExp();
    tokenizer.advance();
    compileCurlyBraces("{", "if statement");
    compileStatements();
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    compileCurlyBraces("}", "if statement");
    tokenizer.advance();
    alreadyAdvanced = true;
    if("else".equals(getToken())) {
      appendTypeText();
      tokenizer.advance();
      compileCurlyBraces("{", "else statement");
      compileStatements();
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      compileCurlyBraces("}", "else statement");
      alreadyAdvanced = false;
    }
    closeTag("ifStatement");
    return;
  }

  private void compileWhileStatement() throws IOException {
    openTag("whileStatement");
    appendTypeText();
    tokenizer.advance();
    compileArrayOrParenExp();
    tokenizer.advance();
    compileCurlyBraces("{", "while statement");
    compileStatements();
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    compileCurlyBraces("}", "while statement");
    alreadyAdvanced = false;
    closeTag("whileStatement");
    return;
  }

  private void compileDoStatement() throws IOException {
    openTag("doStatement");
    appendTypeText();
    tokenizer.advance();
    compileTerm();
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    if(";".equals(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in do statement: Expected ';'");
    }
    closeTag("doStatement");
    return;
  }
  
  private void compileReturnStatement() throws IOException {
    openTag("returnStatement");
    appendTypeText();
    tokenizer.advance();
    alreadyAdvanced = true;
    if(!";".equals(getToken())) {
      compileExpression();
    }
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    if(";".equals(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in Return statement: Expected ';'");
    }
    closeTag("returnStatement");
    return;
  }

  private void compileTerm() throws IOException {
    switch (getTokenType()) {
      case TOKEN_INTEGER:
        compileIntegerTerm();  
        break;

      case TOKEN_STRING:
        compileStringTerm();
        break;

      case TOKEN_KEYWORD:
        if(expKeywords.contains(getToken())) {
          compileKeywordTerm();
          alreadyAdvanced = false;   
        } else {
          System.out.println("'" + getToken() + "'" + " is not a valid keyword constant");
        }
        break;

      case TOKEN_SYMBOL:
        if("-~".contains(getToken())) {
          compileUnaryOPTerm();
        } else if("(".equals(getToken())) {
          compileArrayOrParenExp();
        } else if("{".equals(getToken())) {
          compileExpression();
          tokenizer.advance();
          if("}".equals(getToken())) {
            appendTypeText();
          } else {
            System.out.println("Error in term. Expected }, got " + getToken());
          }
        } else {
          System.out.println("'" + getToken() + "'" + " is not a valid symbol");
        }
        break;
      
      case TOKEN_IDENTIFIER:
        compileVarTerm();
        tokenizer.advance();
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
            return;
        }
    }
  }

  private void advance() throws IOException {
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    alreadyAdvanced = false;
    return;
  }
  private String getToken() {
    return tokenizer.getToken();
  }
  private TokenType getTokenType() {
    return tokenizer.getTokenType();
  }

  private void compileExpression() throws IOException {
    openTag("expression");
    openTag("term");
    compileTerm();
    closeTag("term");
    if(!alreadyAdvanced) {
      tokenizer.advance();
      alreadyAdvanced = true;
    }
    while ("+-*/&|<>=".contains(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
      tokenizer.advance();
      openTag("term");
      compileTerm();
      closeTag("term");
      if(!alreadyAdvanced) {
        tokenizer.advance();
        alreadyAdvanced = true;
      }
    }
    closeTag("expression");
    return;
  }

  private void diagnose(String name) throws IOException {
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
    System.out.println(sb);
  }

  private void compileIntegerTerm() throws IOException {
    appendTypeText();
    return;
  } 
  
  private void compileStringTerm() throws IOException {
    appendTypeText();
    return;
  } 
  
  private void compileKeywordTerm() throws IOException {
    appendTypeText();
    return;
  }
  
  private void compileArrayOrParenExp() throws IOException {
    // token has already been advanced and is expected to be '[' or '('
    String closingCounterpart = null; // can a string be initialized to null?
    if("(".equals(getToken())) {
      closingCounterpart = ")";
    } else if("[".equals(getToken())) {
      closingCounterpart = "]";
    }
    appendTypeText();
    tokenizer.advance();
    compileExpression();
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    if(closingCounterpart.equals(getToken())) {
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in term. Expected '" + closingCounterpart + "'.");
    }
    return;
  }

  private void compileMethodCall() throws IOException {
    // token has already been advanced and is expected to be '.'
    appendTypeText();
    tokenizer.advance();
    if(getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      appendTypeText();
      tokenizer.advance();
      if("(".equals(getToken())) {
        compileFunctionTerm();
      } else {
        System.out.println("Error in method call. Expected '('. Got " + getToken());
        return;
      }
    }
    return;
  }

  private void compileFunctionTerm() throws IOException {
    // token has already been advanced and is expected to be '('
    appendTypeText();
    openTag("expressionList");
    tokenizer.advance();
    alreadyAdvanced = true;
    while(!")".equals(getToken())) {
      alreadyAdvanced = false;
      compileExpression();
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      if(",".equals(getToken())) {
        appendTypeText();
        alreadyAdvanced = false;
        tokenizer.advance();
      } else if(!")".equals(getToken())) {
        System.out.println("Error in expression list. Expected ', or )'. Got " + getToken());
        return;
      }
    }
    if(")".equals(getToken())) {
      closeTag("expressionList");
      appendTypeText();
      alreadyAdvanced = false;
    } else {
      System.out.println("Error in expression list");
      return;
    }
    return;
  } 

  private void compileUnaryOPTerm() throws IOException {
    // token has already advanced and is expected to be an op term
    appendTypeText();
    tokenizer.advance();
    openTag("term");
    compileTerm();
    closeTag("term");
    return;
  }

  private void compileVarTerm() throws IOException {
    appendTypeText();
    return;
  }

  private void appendTypeText() throws IOException {
    sb.append(tokenizer.getTypeText() + "\n");
  }
}
