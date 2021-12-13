import java.util.Arrays;

public class CodeWriter {
  private int eq_counter = 0;
  private int gt_counter = 0;
  private int lt_counter = 0;
  private StringBuilder sb;
  private int num_commands;
  // private static final Set<String> mem_segments = Set.of("local", "this", "that", "argument", "temp");
  private static final String[] mem_segments = {"local", "this", "that", "argument", "temp"};
  private String fileName;
  private String functionName = "";
  private int labelNum = 0;
  private boolean skipComments = false;

  public CodeWriter() {
    this(false);
  }
  public CodeWriter(boolean skipComments) {
    this.skipComments = skipComments;
    sb = new StringBuilder();
  }

  public void setFileName(String fname) {
    fileName = fname;
  }

  public String finalCode() {
    return sb.toString();
  }

  private void addCommand(String s) {
    sb.append(s+"\n");
    num_commands++;
  }
  private void addComment(String s) {
    if (!skipComments) {
      sb.append(s+"\n");
    }
  }

  public void writePushPop(String command, String segment, int index) {
    if (command.equals("C_PUSH")) {
      addComment("// push " + segment + " " + index);
      if (segment.equals("constant")) {
        assignTheValueToSPAddress("SP", index);
        incrementStackPointer();
      } else if (Arrays.asList(CodeWriter.mem_segments).contains(segment)) {
        computeTargetAddress(segment, index);
        addrToStack();
        incrementStackPointer();
      } else if (segment.equals("pointer") || segment.equals("static")) {
        staticOrPointerPushPop(segment, index, "push");
      }
    } else if (command.equals("C_POP")) {
      if (Arrays.asList(CodeWriter.mem_segments).contains(segment)) {
        addComment("// pop " + segment + " " + index);
        computeTargetAddress(segment, index);
        stackToAddr();
      } else if (segment.equals("pointer") || segment.equals("static")) {
        staticOrPointerPushPop(segment, index, "pop");
      }
    }
  }

  public void writeArithmetic(String command) {
    addComment("// " + command);
    switch(command) {
      case "add":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=D+M");
        addCommand("M=D");
        decrementStackPointer();
        break;
        
      case "sub":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M-D");
        addCommand("M=D");
        decrementStackPointer();
        break;
      
      case "neg":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=-M");
        addCommand("M=D");
        break;

      case "and":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M&D");
        addCommand("M=D");
        decrementStackPointer();
        break;
        
      case "or":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M|D");
        addCommand("M=D");
        decrementStackPointer();
        break;

      case "not":
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=!M");
        addCommand("M=D");
        break;
    
      case "eq":
        eq_counter++;
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M-D");
        addCommand("@EQ_LBL" + eq_counter);
        addCommand("D;JEQ");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=0");
        addCommand("@AFTR_EQ" + eq_counter);
        addCommand("0;JMP");
        addCommand("(EQ_LBL" + eq_counter + ")");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=-1");
        addCommand("(AFTR_EQ" + eq_counter + ")");
        break;
        
      case "lt":
        lt_counter++;
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M-D");
        addCommand("@LT_LBL" + lt_counter);
        addCommand("D;JLT");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=0");
        addCommand("@AFTR_LT" + lt_counter);
        addCommand("0;JMP");
        addCommand("(LT_LBL" + lt_counter + ")");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=-1");
        addCommand("(AFTR_LT" + lt_counter + ")");
        break;

      case "gt":
        gt_counter++;
        addCommand("@SP");
        addCommand("A=M-1");
        addCommand("D=M");
        addCommand("A=A-1");
        addCommand("D=M-D");
        addCommand("@GT_LBL" + gt_counter);
        addCommand("D;JGT");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=0");
        addCommand("@AFTR_GT" + gt_counter);
        addCommand("0;JMP");
        addCommand("(GT_LBL" + gt_counter + ")");
        addCommand("@SP");
        addCommand("M=M-1");
        addCommand("A=M-1");
        addCommand("M=-1");
        addCommand("(AFTR_GT" + gt_counter + ")");
        break;
    }
  }

  private void computeTargetAddress(String segment, int index) {
    String segment_name = "";
    switch (segment) {
      case "local": segment_name = "LCL"; break;
      case "argument": segment_name = "ARG"; break;
      case "this": segment_name = "THIS"; break;
      case "that": segment_name = "THAT"; break;
      case "temp": segment_name = "temp"; index += 5; break;
    }
    addCommand("@" + index);
    addCommand("D=A");
    addCommand("@" + segment_name);
    addCommand("D=D+M"); // Now D contains addr
  }

  private void assignTheValueToSPAddress(String toPointer, int index) {
    String value = String.valueOf(index);
      addCommand("@" + value);
      addCommand("D=A");
      addCommand("@" + toPointer);
      addCommand("A=M");
      addCommand("M=D");
  }

  private void incrementStackPointer() {
    addCommand("@SP");
    addCommand("M=M+1");
  }
  
  private void decrementStackPointer() {
    addCommand("@SP");
    addCommand("M=M-1");
  }

  // addr is assumed to be in D
  private void stackToAddr() {
    addCommand("@addr");
    addCommand("M=D");
    decrementStackPointer();
    addCommand("A=M");
    addCommand("D=M");
    addCommand("@addr");
    addCommand("A=M");
    addCommand("M=D");
  }
  
  // addr is assumed to be in D
  private void addrToStack() {
    addCommand("A=D");
    addCommand("D=M");
    addCommand("@SP");
    addCommand("A=M");
    addCommand("M=D");
  }

  private void staticOrPointerPushPop(String segment, int index, String operation) {
    String seg = "";
    if (segment.equals("static")) {
      seg = fileName + "." + String.valueOf(index);
    } else {
      if (index == 0) {
        seg = "THIS";
      } else {
        seg = "THAT";
      }
    }
    if (operation.equals("push")) {
      addCommand("@" + seg);
      addCommand("D=M");
      addCommand("@SP");
      addCommand("A=M");
      addCommand("M=D");
      incrementStackPointer();
    } else if (operation.equals("pop")){
      decrementStackPointer();
      addCommand("A=M");
      addCommand("D=M");
      addCommand("@" + seg);
      addCommand("M=D");
    }
  }
  
  public void addInfiniteLoopInTheEnd() {
    addCommand("@" + num_commands);
    addCommand("0;JMP");
  }

  public void writeLabel(String label) {
    addComment("// label " + label);
    addCommand("(" + functionName + "$" + label + ")");
    num_commands--;
  }

  public void writeIf(String label) {
    addComment("// if-goto " + label);
    addCommand("@SP");
    addCommand("A=M-1");
    addCommand("D=M");
    decrementStackPointer();
    addCommand("@" + functionName + "$" + label);
    addCommand("D;JNE");
  }
  
  public void writeGoto(String label) {
    addComment("// goto " + label);
    addCommand("@" + functionName + "$" + label);
    addCommand("0;JMP");
  }

  public void writeFunction(String funcName, int numVars) {
    functionName = funcName;
    addComment("// function " + functionName + " " + numVars);
    addCommand("(" + functionName + ")");
    for (int i = 0; i < numVars; i++) {
      writePushPop("C_PUSH", "constant", 0);
    }
  }

  public void writeReturn() {
    addComment("// return");
    addCommand("@LCL");
    addCommand("D=M");
    addCommand("@endFrame");
    addCommand("M=D");
    addCommand("@5");
    addCommand("D=D-A");
    addCommand("A=D");
    addCommand("D=M");
    addCommand("@retAddr");
    addCommand("M=D");
    writePushPop("C_POP", "argument", 0);
    addCommand("@ARG");
    addCommand("D=M");
    addCommand("@SP");
    addCommand("M=D+1");
    restoreMemSegment("endFrame", "THAT");
    restoreMemSegment("endFrame", "THIS");
    restoreMemSegment("endFrame", "ARG");
    restoreMemSegment("endFrame", "LCL");
    addCommand("@retAddr");
    addCommand("A=M");
    addCommand("0;JMP");
  }
  
  private void restoreMemSegment(String frame, String segName) {
    addCommand("@" + frame);
    addCommand("D=M-1");
    addCommand("M=M-1"); // @endFrame -= 1
    addCommand("A=D");
    addCommand("D=M");
    addCommand("@" + segName);
    addCommand("M=D");
  }

  public void writeCall(String funcName, int numVars) {
    String retLabel = "RETURN_LABEL" + labelNum;
    labelNum++;
    addComment("// call " + funcName + " " + numVars);
    pushRetAddress(retLabel);
    pushMemSegment("LCL");
    pushMemSegment("ARG");
    pushMemSegment("THIS");
    pushMemSegment("THAT");
    addCommand("@SP");
    addCommand("D=M");
    addCommand("@" + (5+numVars));
    addCommand("D=D-A");
    addCommand("@ARG");
    addCommand("M=D");
    addCommand("@SP");
    addCommand("D=M");
    addCommand("@LCL");
    addCommand("M=D");
    addCommand("@" + funcName);
    addCommand("0;JMP");
    addCommand("(" + retLabel + ")");
  }

  private void pushRetAddress(String retLabel) {
    addCommand("@"+retLabel);
    addCommand("D=A");
    addCommand("@SP");
    addCommand("A=M");
    addCommand("M=D");
    incrementStackPointer();
  }

  private void pushMemSegment(String segName) {
    addCommand("@" + segName);
    addCommand("D=M");
    addCommand("@SP");
    addCommand("A=M");
    addCommand("M=D");
    incrementStackPointer();
  }

  public void writeInit() {
    addComment("// Bootstrap code");
    addCommand("@256");
    addCommand("D=A");
    addCommand("@SP");
    addCommand("M=D");
    writeCall("Sys.init", 0);
  }
}
