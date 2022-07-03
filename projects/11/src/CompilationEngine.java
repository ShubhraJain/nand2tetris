import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CompilationEngine {
  private StringBuilder sb;
  private JackTokenizer tokenizer;
  private VMWriter vmWriter;
  private SymbolTable symbolTable;
  TokenType tokenType = TokenType.TOKEN_NONE;
  private boolean alreadyAdvanced;
  private boolean emptyStatements = false;
  private boolean isParameterList = false;
  private String className;
  List<String> expKeywords = Arrays.asList("true", "false", "null", "this"); 
  List<String> statementNames = Arrays.asList("let", "do", "while", "if", "return");
  String nonTerminalSymbols = "{}()[.,+-*/&|<>=~";
  private static final String IF_LABEL = "IF_TRUE";
  private static final String ELSE_LABEL = "IF_FALSE";
  private static final String ENDIF_LABEL = "IF_END";
  private static final String WHILE_LABEL = "WHILE_EXP";
  private static final String ENDWHILE_LABEL = "WHILE_END";
  private int ifCount = -1;
  private int whileCount = -1;

  public CompilationEngine(VMWriter vmWriter, File inputFile) throws IOException{
    this.vmWriter = vmWriter;
    this.tokenizer = new JackTokenizer(inputFile);
    this.symbolTable = new SymbolTable();
  }

  public void startCompilation() throws IOException {
    while (tokenizer.hasMoreTokens()) {
      compileClass();
    }
  }

  private String getToken() {
    return tokenizer.getToken();
  }
  private TokenType getTokenType() {
    return tokenizer.getTokenType();
  }

  private void compileClass() throws IOException {
    tokenizer.advance();
    if(getToken().equals("class")) {
      tokenizer.advance();
      className = getToken();
      // if(tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER) {
      //   className = tokenizer.getToken();
      //   appendTypeText();
        tokenizer.advance();
      // } else {
      //   System.out.println("Error in class name");
      //   return;
      // }
      // if(getToken().equals("{")) {
      //   appendTypeText();
        tokenizer.advance();
      // } else {
      //   System.out.println("Error in class bracket. Expected {");
      //   return;
      }
      if(getToken().equals("static") || getToken().equals("field")) {
        compileClassVarDec();
      }
      if(getToken().equals("constructor") || 
      getToken().equals("function") || 
      getToken().equals("method")) {
        compileSubroutineDec();
      }
    //   if(getToken().equals("}")) {
    //     appendTypeText();
    //   } else {
    //     System.out.println("Error in class closing brace. Got " + getToken());
    //     return;
    //   }
    //   closeTag("class");
    // } else {
    //   System.out.println("Class declaration is wrong");
    //   return;
    // }
    return;
  }

  // private boolean isValidType(String typeName) throws IOException {
  //   return (typeName.equals("int") ||
  //           typeName. equals("boolean") ||
  //           typeName.equals("char") ||
  //           typeName.equals(className) ||
  //           tokenizer.getTokenType() == TokenType.TOKEN_IDENTIFIER );
  // }

  private void compileClassVarDec() throws IOException {
    while (getToken().equals("static") ||
           getToken().equals("field")) {
      String kind = getToken();
      tokenizer.advance();
      String type = getToken();
      compileTypeAndVar(false, kind);
      // if(getToken().equals(";")) {
      //   appendTypeText();
        tokenizer.advance();
      // } else {
      //   System.out.println("Error in class variable declaration. Expected ;");
      //   return;
      // }
      // closeTag("classVarDec");
    }
    return;
  }

  private void compileSubroutineDec() throws IOException {
    while (getToken().equals("constructor") ||
        getToken().equals("function") ||
        getToken().equals("method")) {
      symbolTable.startSubroutine();
      String type = getToken();
      tokenizer.advance(); // void|type
      String kind = getToken();  
      tokenizer.advance(); // subroutineName
      String name = getToken();
      tokenizer.advance(); // (
      tokenizer.advance(); // type or )
      if (!getToken().equals(")")) {
        // System.out.println("COMPILING PARAM LIST");
        compileParameterList(type.equals("method"));
      }
      tokenizer.advance();
      // if(getToken().equals("{")) {
      //   openTag("subroutineBody");
      //   appendTypeText();
      tokenizer.advance();
      while (getToken().equals("var")) {
        kind = "local";
        tokenizer.advance(); // type
        // type = getToken();
        // tokenizer.advance(); // varName
        // symbolTable.define(getToken(), type, kind);
        // tokenizer.advance(); // , or ;
        compileTypeAndVar(type.equals("method"), kind);
        // if(getToken().equals(";")) {
        tokenizer.advance();
      }
      if (type.equals("function")) {
        // System.out.println("TYPE IS FUNCTION");
        vmWriter.writeFunction(className + "." + name, symbolTable.varCount("var"));
      } else if (type.equals("method")) {
        System.out.println("TYPE IS METHOD");
        vmWriter.writeFunction(className + "." + name, symbolTable.varCount("var"));
        vmWriter.writePush("argument", 0);
        vmWriter.writePop("pointer", 0);
      } else if (type.equals("contructor")) {
        System.out.println("TYPE IS CONSTRUCTOR");
        vmWriter.writeFunction(className + "." + name, 0);
        vmWriter.writePush("constant", symbolTable.varCount("field"));
        vmWriter.writeCall("Memory.alloc", 1);
        vmWriter.writePop("pointer", 0);
      }
      compileStatements();
      System.out.println("CT AND AA " + getToken() + " " + alreadyAdvanced);
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      // tokenizer.advance();
    }
    return;
  }

  private void compileTypeAndVar(boolean isMethod, String kind) throws IOException {
    String type = getToken();
    tokenizer.advance(); // varname
    symbolTable.define(getToken(), type, kind);
    tokenizer.advance(); // , or ;
    while (getToken().equals(",")) {
      // tokenizer.advance();
      tokenizer.advance();
      symbolTable.define(getToken(), type, kind);
      tokenizer.advance();
    }
    return;
  }

  private void compileParameterList(boolean isMethod) throws IOException {
    isParameterList = true;
    String type = getToken();
    String kind = "arg";
    if (isMethod) {
      symbolTable.define("this", type, kind);
    }
    while(!")".equals(getToken())) {
      compileTypeAndVar(isMethod, kind);
    }
    // closeTag("parameterList");
    isParameterList = false;
    return;
  }

  // private void compileCurlyBraces(String brace, String errorPlace) throws IOException {
  //   if(brace.equals(getToken())) {
  //     appendTypeText();
  //     if("{".equals(getToken())) {
  //       tokenizer.advance();
  //     }
  //   } else {
  //     System.out.println("Error in " + errorPlace + ". Expected " + brace);
  //   }
  //   return;
  // }

  private void compileStatements() throws IOException {
    if("}".equals(getToken())) {
      alreadyAdvanced = true;
    }
    while(statementNames.contains(getToken())) {
      compileStatement();
      // System.out.println("CURR TOKEN " + getToken() + " " + alreadyAdvanced);
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      // System.out.println("CURR TOKEN " + getToken());
      alreadyAdvanced = true;
    }
    return;
  }

  private void compileStatement() throws IOException {
    if(getToken().equals("let")) {
      // System.out.println("LET STATEMENT");
      compileLetStatement();
    } else if(getToken().equals("while")) {
      // System.out.println("WHILE STATEMENT");
      compileWhileStatement();
    } else if(getToken().equals("do")) {
      // System.out.println("DO STATEMENT");
      compileDoStatement();
    } else if(getToken().equals("return")) {
      // System.out.println("RETURN STATEMENT");
      compileReturnStatement();
    } else if(getToken().equals("if")) {
      // System.out.println("IF STATEMENT");
      compileIfStatement();
    }
    return;
  }

  private void compileLetStatement() throws IOException {
    tokenizer.advance();
    // System.out.println("LET CURR TOKEN " + getToken());
    String varName = getToken(); // variable name
    tokenizer.advance();
    alreadyAdvanced = true;
    if (getToken().equals("[")) {
      tokenizer.advance();
      compileExpression();
      tokenizer.advance();
      vmWriter.writePush(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
      vmWriter.writeArithmetic("add");
      tokenizer.advance();
      compileExpression();
      tokenizer.advance();
      vmWriter.writePop("temp", 0);
      vmWriter.writePop("pointer", 1);
      vmWriter.writePush("temp", 0);
      vmWriter.writePop("that", 0);
    } else {
      alreadyAdvanced = false;
      tokenizer.advance();
      // System.out.println("getToken " + getToken());
      compileExpression();
      tokenizer.advance();
      // tokenizer.advance();
      alreadyAdvanced = true;
      vmWriter.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
    }
  }

  private void compileIfStatement() throws IOException {
    int index = ifCount;
    tokenizer.advance(); // (
    tokenizer.advance(); // expression
    compileExpression();
    tokenizer.advance(); // )
    vmWriter.writeIf(IF_LABEL + index);
    vmWriter.writeGoto(ELSE_LABEL + index);
    vmWriter.writeLabel(IF_LABEL + index);
    System.out.println("IF 1st " + getToken());
    tokenizer.advance(); // {
    tokenizer.advance(); // statements
    System.out.println("IF 2nd " + getToken());
    compileStatements();
    tokenizer.advance(); // }
    if (getToken().equals("else")) {
      vmWriter.writeGoto(ENDIF_LABEL + index);
      vmWriter.writeLabel(ELSE_LABEL + index);
      tokenizer.advance();
      compileStatements();
      tokenizer.advance();
      vmWriter.writeLabel(ENDIF_LABEL + index);
    } else {
      vmWriter.writeLabel(ELSE_LABEL + index);
    }
  }

  private void compileWhileStatement() throws IOException {
    int index = whileCount;
    vmWriter.writeLabel(WHILE_LABEL + index);
    tokenizer.advance();
    tokenizer.advance();
    compileExpression();
    vmWriter.writeArithmetic("not");
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    vmWriter.writeIf(ENDWHILE_LABEL + index);
    tokenizer.advance();
    // System.out.println("COMPILING WHILE STATEMENT " + alreadyAdvanced + " " + getToken());
    compileStatements();
    tokenizer.advance();
    vmWriter.writeGoto(WHILE_LABEL + index);
    vmWriter.writeLabel(ENDWHILE_LABEL + index);
  }

  private void compileDoStatement() throws IOException {
    tokenizer.advance();
    String name = getToken();
    tokenizer.advance(); //. or (
    int numOfArgs = 0;
    if((name + getToken()).matches("[a-zA-Z].*")) {
      // System.out.println("IN DO's IF ");
      name += getToken();
      tokenizer.advance();
      name += getToken();
      tokenizer.advance();
      // System.out.println("COMPILING DO STATEMENT " +  getToken());
    } else {
      numOfArgs++;
      // System.out.println(" IN DO's ELSE");
      if (getToken().equals(".")) {
        vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
        name = symbolTable.typeOf(name);
        name += getToken();
        tokenizer.advance();
        name += getToken();
        tokenizer.advance();
      } else {
        vmWriter.writePush("pointer", 0);
        name = className+"."+name;
      }
    }
    tokenizer.advance(); // 1
    // tokenizer.advance();
    numOfArgs += compileExpressionList();
    // System.out.println("nArgs: " + numOfArgs);
    vmWriter.writeCall(name, numOfArgs);
    tokenizer.advance();
    alreadyAdvanced = false;
    // System.out.println("DO curr token: " + getToken());
    vmWriter.writePop("temp", 0);
  }

  private void compileReturnStatement() throws IOException {
    tokenizer.advance();
    alreadyAdvanced = true;
    if(";".equals(getToken())) {
      vmWriter.writePush("constant", 0);
      alreadyAdvanced = false;
    } else {
      compileExpression();
    }
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    vmWriter.writeReturn();
    // tokenizer.advance();
  }

  private void writeString(String str) {
    int length = str.length();
    vmWriter.writePush("constant", length);
    vmWriter.writeCall("String.new", 1);
    for (int i = 0; i < length; i++) {
      vmWriter.writePush("constant", (int) str.charAt(i));
      vmWriter.writeCall("String.appendChar", 2);
    }
  }

  private void compileTerm() throws IOException {
    switch (getTokenType()) {
      case TOKEN_INTEGER:
        vmWriter.writePush("constant", Integer.parseInt(getToken()));
        break;

      case TOKEN_STRING:
        writeString(getToken());
        break;

      case TOKEN_KEYWORD:
        // if(expKeywords.contains(getToken())) {
        //   compileKeywordTerm();
        //   alreadyAdvanced = false;   
        // } else {
        //   System.out.println("'" + getToken() + "'" + " is not a valid keyword constant");
        // }
        vmWriter.writePush("constant", 0);
        if(getToken().equals("true")) {
          vmWriter.writeArithmetic("not");
        }
        alreadyAdvanced = false;
        break;

      case TOKEN_SYMBOL:
        if("-".contains(getToken())) {
          tokenizer.advance();
          compileTerm();
          vmWriter.writeArithmetic("neg");
        } else if("~".equals(getToken())) {
          tokenizer.advance();
          compileTerm();
          vmWriter.writeArithmetic("not");
        } else if("(".equals(getToken())) {
          tokenizer.advance();
          compileExpression();
          tokenizer.advance();
        }
        break;
      
      case TOKEN_IDENTIFIER:
        String name = getToken();
        tokenizer.advance();
        alreadyAdvanced = true;
        int numOfArgs;
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
            // System.out.println("TOKEN " + getToken());
            break;
          case "(":
            tokenizer.advance();
            numOfArgs = compileExpressionList();
            tokenizer.advance();
            vmWriter.writeCall(name, numOfArgs);
            break;
          case "[":
            tokenizer.advance();
            compileExpression();
            tokenizer.advance();
            vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
            vmWriter.writeArithmetic("add");
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("that", 0);
            break;
          case ".":
            alreadyAdvanced = false;
            // System.out.println("ALREADY ADVANCED IN . ");
            vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
            name += getToken();
            tokenizer.advance();
            name += getToken();
            tokenizer.advance();
            tokenizer.advance();
            numOfArgs = compileExpressionList();
            tokenizer.advance();
            vmWriter.writeCall(name, numOfArgs);
            break;
          default:
            System.out.println(getToken() + " is not a valid term");
            return;
        }
    }
  }

  // private void advance() throws IOException {
  //   if(!alreadyAdvanced) {
  //     tokenizer.advance();
  //   }
  //   alreadyAdvanced = false;
  //   return;
  // }
  // private String getToken() {
  //   return tokenizer.getToken();
  // }
  // private TokenType getTokenType() {
  //   return tokenizer.getTokenType();
  // }

  private void compileExpression() throws IOException {
    compileTerm();
    String op = "";
    while ("+-*/&|<>=".contains(getToken())) {
      alreadyAdvanced = false;
      switch (getToken()) {
        case "+":
          op = "add";
          break;
        case "-":
          op = "sub";
          break;
        case "*":
          op = "Math.multiply";
          break;
        case "/":
          op = "Math.divide";
          break;
        case "&":
          op = "and";
          break;
        case "|":
          op = "or";
          break;
        case "<":
          op = "lt";
          break;
        case ">":
          op = "gt";
          break;
        case "=":
          op = "eq";
          break;
        case "~":
          op = "not";
          break;
      }
      // System.out.println("ABOUT TO ADVANCE " + getToken());
      tokenizer.advance();
      // System.out.println("BEFORE COMPILING TERM " + getToken());
      compileTerm();
      if (op.equals("Math.multiply") || op.equals("Math.divide")) {
        vmWriter.writeCall(op, 2);
      } else {
        vmWriter.writeArithmetic(op);
      }
      tokenizer.advance();
      alreadyAdvanced = true;
    }
    if(!alreadyAdvanced) {
      tokenizer.advance();
      alreadyAdvanced = true;
    }
  }

  private void diagnose(String name) throws IOException {
    System.out.print("Compiling "+name);
    System.out.print("  advanced="+alreadyAdvanced);
    System.out.println("   token="+getToken());
  }

  // private void openTag(String name) throws IOException {
  //   sb.append("<"+name+">\n");
  // }
  // private void closeTag(String name) throws IOException {
  //   sb.append("</"+name+">\n");
  // }

  public void close() throws IOException {
    System.out.println(sb);
  }
  
  // private void compileArrayOrParenExp() throws IOException {
  //   // token has already been advanced and is expected to be '[' or '('
  //   tokenizer.advance();
  //   compileExpression();
  //   if(!alreadyAdvanced) {
  //     tokenizer.advance();
  //   }
  //   if(closingCounterpart.equals(getToken())) {
  //     appendTypeText();
  //     alreadyAdvanced = false;
  //   } else {
  //     System.out.println("Error in term. Expected '" + closingCounterpart + "'.");
  //   }
  //   return;
  // }

  // private void compileMethodCall() throws IOException {
  //   // token has already been advanced and is expected to be '.'
  //   appendTypeText();
  //   tokenizer.advance();
  //   if(getTokenType() == TokenType.TOKEN_IDENTIFIER) {
  //     appendTypeText();
  //     tokenizer.advance();
  //     if("(".equals(getToken())) {
  //       compileFunctionTerm();
  //     } else {
  //       System.out.println("Error in method call. Expected '('. Got " + getToken());
  //       return;
  //     }
  //   }
  //   return;
  // }

  // private void compileFunctionTerm() throws IOException {
  //   // token has already been advanced and is expected to be '('
  //   appendTypeText();
  //   openTag("expressionList");
  //   tokenizer.advance();
  //   alreadyAdvanced = true;
  //   while(!")".equals(getToken())) {
  //     alreadyAdvanced = false;
  //     compileExpression();
  //     if(!alreadyAdvanced) {
  //       tokenizer.advance();
  //     }
  //     if(",".equals(getToken())) {
  //       appendTypeText();
  //       alreadyAdvanced = false;
  //       tokenizer.advance();
  //     } else if(!")".equals(getToken())) {
  //       System.out.println("Error in expression list. Expected ', or )'. Got " + getToken());
  //       return;
  //     }
  //   }
  //   if(")".equals(getToken())) {
  //     closeTag("expressionList");
  //     appendTypeText();
  //     alreadyAdvanced = false;
  //   } else {
  //     System.out.println("Error in expression list");
  //     return;
  //   }
  //   return;
  // } 

  // private void compileUnaryOPTerm() throws IOException {
  //   // token has already advanced and is expected to be an op term
  //   appendTypeText();
  //   tokenizer.advance();
  //   openTag("term");
  //   compileTerm();
  //   closeTag("term");
  //   return;
  // }

  private int compileExpressionList() throws IOException {
    int nArgs = 0;
    while(!")".equals(getToken())) {
      compileExpression();
      // System.out.println("IN COMPILE EXP LIST " + getToken());
      while(getToken().equals(",")) {
        alreadyAdvanced = false;
        tokenizer.advance();
        compileExpression();
        nArgs++;
      }
      nArgs++;
    }
    // System.out.println("ARGS " + nArgs);
    return nArgs;
  }
}

