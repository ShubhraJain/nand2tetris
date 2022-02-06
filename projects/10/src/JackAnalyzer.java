import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FilenameFilter;

public class JackAnalyzer {
  private static class VMFileNameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".jack");
    }
  }

  private static void translateFile(File inputFile, File outputFile) throws IOException {
    FileWriter fw = new FileWriter(outputFile);
    try (JackTokenizer tokenizer = new JackTokenizer(inputFile)) {
      CompilationEngine compiler = new CompilationEngine(tokenizer);
      compiler.startCompilation();
      fw.write(compiler.getFinalXML());
      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("error: " + e);
    }
  }

  public static void main(String[] args) throws IOException{
    String name = args[0];
    File inputFile = new File(args[0]);
    File[] inputFiles;
    File opFile;
    if (inputFile.isDirectory()) {
      inputFiles = inputFile.listFiles(new VMFileNameFilter());
      for (File f: inputFiles) {
        opFile = new File(f.getParent() + "/" + f.getName().replace(".jack", "") + ".xml");
        translateFile(f, opFile);
      }
    } else {
      String[] vmFileName = args[0].split(".jack");
      opFile = new File(vmFileName[0] + ".xml");
      translateFile(inputFile, opFile);
    } 
  }
}
