import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class VMWriter {
  private String[] segments = {"CONST", "ARG", "LOCAL", "STATIC", "THIS", "THAT", "POINTER", "TEMP"};
  private StringBuilder sb;
  File opFile;
  
  public VMWriter(File inputFile) {
    opFile = new File(inputFile.getParent() + "/" + inputFile.getName().split(".jack")[0] + ".vm");
    sb = new StringBuilder();
  }

  private void writeCommand(String s) {
    sb.append(s+"\n");
  }

  private void writePush(String segment, int index) {
    writeCommand("PUSH " + segment + " " + index);
  }
  
  private void writePop(String segment, int index) {
    writeCommand("POP " + segment + " " + index);
  }

  private void writeArithmetic(String command) {
    writeCommand(command);
  }

  private void writeLabel(String label) {
    writeCommand("label " + label);
  }

  private void writeGoto(String label) {
    writeCommand("goto " + label);
  }

  private void writeIf(String label) {
    writeCommand("if-goto " + label);
  }
  
  private void writeCall(String name, int numOfArgs) {
    writeCommand("call " + name + numOfArgs);
  }

  private void writeFunction(String name, int numOfLocals) {
    writeCommand("function " + name + numOfLocals);
  }

  private void writeReturn() {
    writeCommand("return");
  }

  private void close() throws IOException {
    FileWriter fw = new FileWriter(opFile);
    fw.write(sb.toString());
    fw.close();
  }
}
