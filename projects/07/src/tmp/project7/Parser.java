import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Parser implements Closeable {
  enum CommandType {
    C_ARITHMETIC,
    C_PUSH,
    C_POP
  };
  List<String> arithmeticCommands = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
  Scanner scanner;
  boolean commandFound = false;
  String command;
  String[] commandParts;
  
  // Adds each instruction into an arraylist
  public Parser(File file) throws IOException {
    this.scanner = new Scanner(file);
  }

  public void close() throws IOException {
    scanner.close();
  }
  
  public boolean hasMoreCommands() {
    /* continue looking for next line until you find a line which has an instruction
    * when such a line if found, store it in temporary location.
    * when advance() is called: use the instruction which was saved by hasMoreCommands() 
    * and clear that value after using it.
    */
    while(!commandFound && scanner.hasNextLine()) {
      String tempCommand = scanner.nextLine().trim();
      if (!tempCommand.isEmpty() && !tempCommand.startsWith("//")) {
        command = tempCommand;
        commandFound = true;
      }
    }
    return commandFound;
  }
  
  public void advance() {
    commandFound = false;
    commandParts = command.split(" ");
  }

  // returns constant representing type of current command
  public String commandType() {
    if (arithmeticCommands.contains(commandParts[0])) {
      return CommandType.C_ARITHMETIC.name();
    } else if (commandParts[0].equals("push")) {
      return CommandType.C_PUSH.name();
    } else if (commandParts[0].equals("pop")) {
      return CommandType.C_POP.name();
    }
    return "";
  }

  public String arg1() {
    if (this.commandType().equals(CommandType.C_ARITHMETIC.name())) {
      return commandParts[0];
    }
    return commandParts[1];
  }

  public int arg2() {
    return Integer.parseInt(commandParts[2]);
  }
}
