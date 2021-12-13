import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
// import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FilenameFilter;

public class VMTranslator {

  public static class VMFileNameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".vm");
    }
  }

  private static void translateSingleFile(File inputFile, CodeWriter codeWriter) {
    try (Parser parser = new Parser(inputFile)) {
      while (parser.hasMoreCommands()) {
        parser.advance();
        if (parser.commandType().equals("C_PUSH") || parser.commandType().equals("C_POP")) {
          codeWriter.writePushPop(parser.commandType(), parser.arg1(), parser.arg2());
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
        } else if (parser.commandType().equals("C_CALL")) {
          codeWriter.writeCall(parser.arg1(), parser.arg2());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }

  public static void main(String[] args) throws IOException{
    String name = args[0];
    File inputFile = new File(args[0]);
    boolean skipComments = false;
    if (args.length > 1) {
      skipComments = Boolean.parseBoolean(args[1]);
    }
    File[] inputFiles;
    File outputFile;  
    CodeWriter codeWriter = new CodeWriter(skipComments);

    if (inputFile.isDirectory()) {
      inputFiles = inputFile.listFiles(new VMFileNameFilter());
      outputFile = Paths.get(args[0], inputFile.getName()+".asm").toFile();
      codeWriter.writeInit();
      for (File f: inputFiles) {
        codeWriter.setFileName(f.getName());
        translateSingleFile(f, codeWriter);
      }
    } else {
      String[] vmFileName = args[0].split(".vm");
      outputFile = new File(vmFileName[0] + ".asm");
      codeWriter.setFileName(outputFile.getName().replace(".asm", ""));
      translateSingleFile(inputFile, codeWriter);
      codeWriter.addInfiniteLoopInTheEnd();
    }
    FileWriter fw = new FileWriter(outputFile);
    fw.write(codeWriter.finalCode());
    fw.close();


    // create array of all input files
    // create output file writer
    // for each file f in array:
    //   parseSingleFile(f)
    //   appendCode to file writer
  }

}
