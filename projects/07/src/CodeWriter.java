import java.util.Arrays;

public class CodeWriter {
  private int eq_counter = 0;
  private int gt_counter = 0;
  private int lt_counter = 0;
  private StringBuilder sb;
  private int num_commands;
  String[] mem_segments = {"local", "this", "that", "argument", "temp"};

  public CodeWriter() {
    sb = new StringBuilder();
  }

  public String finalCode() {
    return sb.toString();
  }

  private void addCommand(String s) {
    sb.append(s+"\n");
    num_commands++;
  }
  private void addComment(String s) {
    sb.append(s+"\n");
  }

  // throw the exception or catch it?
  public void writePushPop(String command, String segment, int index) {
    if (command.equals("C_PUSH")) {
      addComment("// push " + segment + " " + index);
      if (segment.equals("constant")) {
        assignTheValueToSPAddress("SP", index);
        incrementStackPointer();
      } else if (Arrays.asList(mem_segments).contains(segment)) {
        computeTargetAddress(segment, index);
        addrToStack();
        incrementStackPointer();
      } else if (segment.equals("pointer") || segment.equals("static")) {
        staticOrPointerPushPop(segment, index, fileName, "push");
      }
    } else if (command.equals("C_POP")) {
      if (Arrays.asList(mem_segments).contains(segment)) {
        addComment("// pop " + segment + " " + index);
        computeTargetAddress(segment, index);
        stackToAddr();
      } else if (segment.equals("pointer") || segment.equals("static")) {
        staticOrPointerPushPop(segment, index, fileName, "pop");
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
    if (segment.equals("local")) {
      segment_name = "LCL";
    } else if (segment.equals("argument")) {
      segment_name = "ARG";
    } else if (segment.equals("this")) {
      segment_name = "THIS";
    } else if (segment.equals("that")) {
      segment_name = "THAT";
    } else if (segment.equals("temp")) {
      index += 5;
      segment_name = "temp";
      System.out.println("seg_name: " + segment_name);
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

  private void staticOrPointerPushPop(String segment, int index, String fileName, String operation) {
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
    addCommand("(" + label + ")");
    num_commands--;
  }

  public void writeIf(String label) {
    addComment("// if-goto " + label);
    addCommand("@SP");
    addCommand("A=M-1");
    addCommand("D=M");
    decrementStackPointer();
    addCommand("@" + label);
    addCommand("D;JNE");
  }
  
  public void writeGoto(String label) {
    addComment("// goto " + label);
    addCommand("@" + label);
    addCommand("0;JMP");
  }

  public void writeFunction(String funcName, int numVars) {
    addComment("// function " + funcName + numVars);
    for (int i = 0; i < numVars; i++) {
      addCommand("@SP");
      addCommand("A=M");
      addCommand("M=0");
      addCommand("@SP");
      addCommand("M=M+1");
    }
  }

  public void writeReturn() {
    addComment("// return");
    writePushPop("C_POP", "argument", 0, "");
    addCommand("@ARG");
    addCommand("D=M");
    addCommand("@SP");
    addCommand("M=D+1");
    restoreMemSegment("THAT");
    restoreMemSegment("THIS");
    restoreMemSegment("ARG");
    restoreMemSegment("LCL");
  }
  
  private void restoreMemSegment(String segName) {
    addCommand("@LCL");
    addCommand("AM=M-1");
    addCommand("D=M");
    addCommand("@" + segName);
    addCommand("M=D");
  }

  public void writeCall(String funcName, int numVars) {
    addComment("// call " + funcName + numVars);
  }
}
