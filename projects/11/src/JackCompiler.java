import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FilenameFilter;

public class JackCompiler {

  private static class VMFileNameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".jack");
    }
  }

  public static void translateFile(File inputFile) throws IOException {
    try (VMWriter vmWriter = new VMWriter(inputFile)) {
      CompilationEngine compiler = new CompilationEngine(vmWriter, inputFile);
      compiler.startCompilation();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }

  public static void main(String[] args) throws IOException{
    File inputFile = new File(args[0]);
    File[] inputFiles;
    if (inputFile.isDirectory()) {
      inputFiles = inputFile.listFiles(new VMFileNameFilter());
      for (File f: inputFiles) {
        translateFile(f);
      }
    } else {
      String[] vmFileName = args[0].split(".jack");
      translateFile(inputFile);
    } 
  }
}
