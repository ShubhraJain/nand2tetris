import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class JackCompilerTest {

  @Test
  public void testTest() {
    assertTrue(true);
  }

  @Test
  public void testSeven() throws IOException {
    // give it a filepath + filename_prefix
    // should compile the specified file filename.jack
    // and compare the generated file with 
    // filepath/filename.expected.vm
    String fileNamePrefix = "/Users/s0j029r/Desktop/study/NANDTOTETRIS/projects/11/Seven/";
    File inputFile = new File(fileNamePrefix + "Main.jack");
    JackCompiler.translateFile(inputFile);
    String outputFileName = fileNamePrefix + "Main.vm";
    RunTextComparator.run(fileNamePrefix, outputFileName);
  }
  
}
