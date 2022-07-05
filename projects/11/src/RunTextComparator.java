import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunTextComparator {
  public static void run(String file1, String file2) throws IOException {
    Process process = Runtime.getRuntime().exec("/Users/s0j029r/Desktop/study/NANDTOTETRIS/tools/TextComparer.sh " + file1 + " " + file2);
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line = "";
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
  }
}
