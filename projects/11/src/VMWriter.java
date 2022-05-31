import java.io.File;
import java.io.IOException;
import java.io.FileWriter;

public class VMWriter implements AutoCloseable {
  private StringBuilder sb;
  File opFile;
  
  public VMWriter(File inputFile) {
    opFile = new File(inputFile.getParent() + "/" + inputFile.getName().split(".jack")[0] + ".vm");
    sb = new StringBuilder();
  }

  public void writeCommand(String s) {
    sb.append(s+"\n");
  }

  public void writePush(String segment, int index) {
    writeCommand("push " + segment + " " + index);
  }
  
  public void writePop(String segment, int index) {
    writeCommand("pop " + segment + " " + index);
  }

  public void writeArithmetic(String command) {
    writeCommand(command);
  }

  public void writeLabel(String label) {
    writeCommand("label " + label);
  }

  public void writeGoto(String label) {
    writeCommand("goto " + label);
  }

  public void writeIf(String label) {
    writeCommand("if-goto " + label);
  }
  
  public void writeCall(String name, int numOfArgs) {
    writeCommand("call " + name + " " + numOfArgs);
  }

  public void writeFunction(String name, int numOfLocals) {
    writeCommand("function " + name + " " + numOfLocals);
  }

  public void writeReturn() {
    writeCommand("return");
  }

  public void close() throws IOException {
    FileWriter fw = new FileWriter(opFile);
    fw.write(sb.toString());
    fw.close();
  }
}
