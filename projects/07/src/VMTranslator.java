import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class VMTranslator {
  public static void main(String[] args) throws IOException{
    // Get the file name from args and create input and output file
    System.out.println(args[0]);
    String[] vmFileName = args[0].split(".vm");
    File inputFile = new File(Paths.get(System.getProperty("user.dir")) + "/" + args[0]);
    File outputFile = new File(Paths.get(System.getProperty("user.dir")) + "/" + vmFileName[0] + ".asm");
    CodeWriter codeWriter = new CodeWriter();
    String[] arr = vmFileName[0].split("/");
    String fileName = arr[arr.length - 1];
    try (Parser parser = new Parser(inputFile);
        FileWriter fw = new FileWriter(outputFile)) {
      while (parser.hasMoreCommands()) {
        parser.advance();
        if (parser.commandType().equals("C_PUSH") || parser.commandType().equals("C_POP")) {
          codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2(), fileName);
        } else if (parser.commandType().equals("C_ARITHMETIC")) {
          codeWriter.writeArithmetic(parser.arg1());
        } else if (parser.commandType().equals("C_LABEL")) {
          codeWriter.writeLabel(parser.arg1());
        } else if (parser.commandType().equals("C_IF")) {
          codeWriter.writeIf(parser.arg1());
        } else if (parser.commandType().equals("C_GOTO")) {
          codeWriter.writeGoto(parser.arg1());
        } else if (parser.commandType().equals("C_FUNCTION")) {
          codeWriter.writeFunction(parser.arg1(), parser.arg2());
        } else if (parser.commandType().equals("C_RETURN")) {
          codeWriter.writeReturn();
        }
      }
      codeWriter.addInfiniteLoopInTheEnd();
      fw.write(codeWriter.finalCode());
        
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
