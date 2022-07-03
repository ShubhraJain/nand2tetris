import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
public class CompilationEngineInitial {
  private StringBuilder sb;
  private JackTokenizer tokenizer;
  private VMWriter vmWriter;
  private SymbolTable symbolTable;
  TokenType tokenType = TokenType.TOKEN_NONE;
  private boolean emptyStatements = false;
  private boolean isParameterList = false;
  private boolean alreadyAdvanced = false;
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

  public CompilationEngineInitial(VMWriter vmWriter, File inputFile) throws IOException{
    this.vmWriter = vmWriter;
    this.tokenizer = new JackTokenizer(inputFile);
    this.symbolTable = new SymbolTable();
  }

  public void startCompilation() throws IOException {
    while (tokenizer.hasMoreTokens()) {
      System.out.println("HAS MORE TOKENS");
      compileClass();
    }
  }
  
  private String getToken() {
    return tokenizer.getToken();
  }
  private TokenType getTokenType() {
    return tokenizer.getTokenType();
  }
  
  public void compileClass() throws IOException {
    tokenizer.advance(); // class
    // System.out.println("COMPILING CLASS 1" + getToken());
    tokenizer.advance(); // className
    // System.out.println("COMPILING CLASS 2" + getToken());
    className = getToken();
    tokenizer.advance(); // {
    tokenizer.advance(); // static|field|constructor|function|method
    // System.out.println("COMPILING CLASS 3" + getToken());
    if(getToken().equals("static") || getToken().equals("field")) {
      compileClassVarDec();
    }
    if(getToken().equals("constructor") || 
    getToken().equals("function") || 
    getToken().equals("method")) {
      compileSubroutineDec();
    }
    // tokenizer.advance();
    System.out.println("COMPILING CLASS 4" + getToken());
    return;
  }

  private void compileClassVarDec() throws IOException {
    while (getToken().equals("static") ||
        getToken().equals("field")) {
      System.out.println("COMPILING CLASSVARDEC");
      String kind = getToken();
      tokenizer.advance();
      String type = getToken();
      tokenizer.advance();
      String name = getToken();
      symbolTable.define(name, type, kind);
      tokenizer.advance();
      while (getToken().equals(",")) {
        tokenizer.advance();
        name = getToken();
        symbolTable.define(name, type, kind);
        tokenizer.advance();
      }
      tokenizer.advance();
    }
  }

  private void compileSubroutineDec() throws IOException {
    while (getToken().equals("constructor") ||
        getToken().equals("function") ||
        getToken().equals("method")) {
      // System.out.println("COMPILING SUBROUTINE " + getToken());
      symbolTable.startSubroutine();
      String type = getToken();
      tokenizer.advance(); // void|type
      String kind = getToken();
      tokenizer.advance(); // subroutineName
      String name = getToken();
      tokenizer.advance(); // (
      tokenizer.advance(); // type or )
      compileParameterList(type.equals("method"));
      tokenizer.advance(); // { subroutineBody starts
      tokenizer.advance(); // var or statements
      // System.out.println("charAt: " + (int) getToken().charAt(1));
      // alreadyAdvanced = true;
      while(getToken().equals("var")) {
        compileVarDec();
      }
      System.out.println("EVALUATING TYPE " + type);
      if (type.equals("function")) {
        System.out.println("TYPE IS FUNCTION");
        vmWriter.writeFunction(className + "." + name, symbolTable.varCount("var"));
        // alreadyAdvanced = false;
      } else if (type.equals("method")) {
        System.out.println("TYPE IS METHOD");
        vmWriter.writeFunction(className + "." + name, symbolTable.varCount("var"));
        vmWriter.writePush("argument", 0);
        vmWriter.writePop("pointer", 0);
        // alreadyAdvanced = false;
      } else if (type.equals("contructor")) {
        System.out.println("TYPE IS CONSTRUCTOR");
        vmWriter.writeFunction(className + "." + name, 0);
        vmWriter.writePush("constant", symbolTable.varCount("field"));
        vmWriter.writeCall("Memory.alloc", 1);
        vmWriter.writePop("pointer", 0);
        // alreadyAdvanced = false;
      }
      compileStatements();
      tokenizer.advance();
      System.out.println("AFTER COMPILING STATEMENT SUBROUTINE" + getToken());
    }
  }

  private void compileParameterList(boolean isMethod) throws IOException {
    String type = getToken();
    String kind = "arg";
    if (isMethod) {
      symbolTable.define("this", type, kind);
    }
    while(!")".equals(getToken())) {
      tokenizer.advance(); // varName
      symbolTable.define(getToken(), type, kind);
      tokenizer.advance(); // , or )
      while(",".equals(getToken())) {
        tokenizer.advance();
        tokenizer.advance();
        symbolTable.define(getToken(), type, kind);
        tokenizer.advance();
      }
    }
  }

  private void compileVarDec() throws IOException {
    String kind = "local";
    tokenizer.advance(); // type
    String type = getToken();
    tokenizer.advance(); // varName
    symbolTable.define(getToken(), type, kind);
    System.out.println("COMPILING VAR DEC " + getToken());
    tokenizer.advance(); // , or ;
    while(getToken().equals(",")) {
      // tokenizer.advance(); // type
      // type = getToken();
      tokenizer.advance(); // varName
      symbolTable.define(getToken(), type, kind);
      tokenizer.advance(); // , or ;
    }
    alreadyAdvanced = false;
  }

  private void compileStatements() throws IOException {
    // if (!alreadyAdvanced) {
    //   tokenizer.advance();
    // }
    System.out.println("COMPILING STATEMENTS " + getToken());
    while(!";}".contains(getToken())) {
    //  System.out.println("COMPILING STATEMENTS " + getToken());
      switch (getToken()) {
        case "do":
          compileDoStatement();
          break;
        case "let":
          compileLetStatement();
          break;
        case "if":
          ifCount++;
          compileIfStatement();
          break;
        case "while":
          whileCount++;
          compileWhileStatement();
          break;
        case "return":
          compileReturnStatement();
          break;
        default:
          break;  
      }
      if(!alreadyAdvanced) {
        tokenizer.advance();
      }
      // System.out.println("COMPILING STATEMENTS " + getToken() + " " + alreadyAdvanced);
    }
  }

  private void compileDoStatement() throws IOException {
    tokenizer.advance();
    String name = getToken();
    tokenizer.advance(); //. or (
    int numOfArgs = 0;
    if(name.matches("[A-Z].*")) {
      System.out.println("IN DO's IF");
      name += getToken();
      tokenizer.advance();
      name += getToken();
      tokenizer.advance();
      System.out.println("COMPILING DO STATEMENT " +  getToken());
    } else {
      numOfArgs++;
      System.out.println(" IN DO's ELSE");
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
    System.out.println("DO curr token: " + getToken());
    vmWriter.writePop("temp", 0);
  }

  private void compileLetStatement() throws IOException {
    tokenizer.advance();
    String varName = getToken(); // variable name
    tokenizer.advance(); // =
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
      tokenizer.advance();
      compileExpression();
      tokenizer.advance();
      // tokenizer.advance();
      alreadyAdvanced = true;
      vmWriter.writePop(symbolTable.kindOf(varName), symbolTable.indexOf(varName));
    }
    System.out.println("COMPILING LET STATEMENT " + getToken());
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
    tokenizer.advance();
    vmWriter.writeLabel(WHILE_LABEL + index);
    compileExpression();
    vmWriter.writeArithmetic("not");
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    vmWriter.writeIf(ENDWHILE_LABEL + index);
    tokenizer.advance();
    System.out.println("COMPILING WHILE STATEMENT " + alreadyAdvanced + " " + getToken());
    compileStatements();
    tokenizer.advance();
    vmWriter.writeGoto(WHILE_LABEL + index);
    vmWriter.writeLabel(ENDWHILE_LABEL + index);
  }

  private void compileReturnStatement() throws IOException {
    tokenizer.advance();
    alreadyAdvanced = true;
    System.out.println("COMPILING RETURN STATEMENT " + getToken());
    if(!";".equals(getToken())) {
      compileExpression();
    }
    if(!alreadyAdvanced) {
      tokenizer.advance();
    }
    if(";".equals(getToken())) {
      vmWriter.writePush("constant", 0);
      alreadyAdvanced = false;
    }
    vmWriter.writeReturn();
    tokenizer.advance();
  }

  private void compileTerm() throws IOException {
    switch (getTokenType()) {
      case TOKEN_INTEGER:
        vmWriter.writePush("constant", Integer.parseInt(getToken()));
        tokenizer.advance();
        alreadyAdvanced = true;
        break;
      case TOKEN_STRING:
        writeString(getToken());
        tokenizer.advance();
        break;
      case TOKEN_KEYWORD:
        switch (getToken()) {
          case "true":
            vmWriter.writePush("constant", 0);
            vmWriter.writeArithmetic("not");
            break;
          case "false":
            vmWriter.writePush("constant", 0);
            break;
          case "null":
            vmWriter.writePush("constant", 0);
            break;
          case "this":
            vmWriter.writePush("constant", 0);
            break;
        }
        tokenizer.advance();
        break;
      case TOKEN_SYMBOL:
        switch (getToken()) {
          case "-":
            tokenizer.advance();
            compileTerm();
            vmWriter.writeArithmetic("neg");
            break;
          case "~":
            tokenizer.advance();
            compileTerm();
            vmWriter.writeArithmetic("not");
            break;
          case "(":
            tokenizer.advance();
            compileExpression();
            tokenizer.advance(); // (
            break;
        }
        break;
      case TOKEN_IDENTIFIER:
        String name = getToken();
        tokenizer.advance();
        alreadyAdvanced = true;
        if (getToken().equals(".")) {
          vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
          name += getToken();
          tokenizer.advance();
          name += getToken();
          tokenizer.advance();
          tokenizer.advance();
          int numOfArgs = compileExpressionList();
          tokenizer.advance();
          vmWriter.writeCall(name, numOfArgs);
        } else if (getToken().equals("(")) {
          tokenizer.advance();
          int numOfArgs = compileExpressionList();
          tokenizer.advance();
          vmWriter.writeCall(name, numOfArgs);
        } else if (getToken().equals("[")) {
          tokenizer.advance();
          compileExpression();
          tokenizer.advance();
          vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
          vmWriter.writeArithmetic("add");
          vmWriter.writePop("pointer", 1);
          vmWriter.writePush("that", 0);
        } else {
          vmWriter.writePush(symbolTable.kindOf(name), symbolTable.indexOf(name));
        }
        break;
    }
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

  private void compileExpression() throws IOException {
    compileTerm();
    if(!alreadyAdvanced) {
      alreadyAdvanced = false;
      tokenizer.advance();
    }
    String op = "";
    while ("+-*/&|<>=".contains(getToken())) {
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
      tokenizer.advance();
      compileTerm();
      if (op.equals("Math.multiply") || op.equals("Math.divide")) {
        vmWriter.writeCall(op, 2);
      } else {
        vmWriter.writeArithmetic(op);
      }
    }
  }

  private int compileExpressionList() throws IOException {
    int nArgs = 0;
    while(!")".equals(getToken())) {
      compileExpression();
      while(getToken().equals(",")) {
        tokenizer.advance();
        compileExpression();
        nArgs++;
      }
      nArgs++;
    }
    return nArgs;
  }
}
