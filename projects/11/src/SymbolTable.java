import java.util.Hashtable;

public class SymbolTable {
  private Hashtable<String, Object[]> classSymbols;
  private Hashtable<String, Object[]> subroutineSymbols;
  private int lclCount;
  private int staticCount;
  private int fieldCount;
  private int argCount;

  public SymbolTable() {
    this.classSymbols = new Hashtable<String, Object[]>();
    this.subroutineSymbols = new Hashtable<String, Object[]>();
    lclCount = 0;
    staticCount = 0;
    fieldCount = 0;
    argCount = 0;
  }

  private void startSubroutine() {
    subroutineSymbols.clear();
    lclCount = 0;
    argCount = 0;
  }

  private void define(String name, String type, String kind) {
    Object[] values = new Object[3];
    values[0] = type;
    values[1] = kind;
    switch (kind) {
      case "ARG":
        values[2] = argCount;
        subroutineSymbols.put(name, values);
        argCount++;
        break;
      case "VAR":
        values[2] = lclCount;
        subroutineSymbols.put(name, values);
        lclCount++;
        break;
      case "STATIC":
        values[2] = staticCount;
        classSymbols.put(name, values);
        staticCount++;
        break;
      case "FIELD":
        values[2] = fieldCount;
        classSymbols.put(name, values);
        fieldCount++;
        break;
    }
  }

  private int varCount(String kind) {
    int count = 0;
    switch (kind) {
      case "ARG":
        count = argCount;
        break;
      case "VAR":
        count = lclCount;
        break;
      case "STATIC":
        count = staticCount;
        break;
      case "FIELD":
        count = fieldCount;
        break;
    }
    return count;
  }

  private String kindOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return subroutineSymbols.get(name)[1].toString();
    } else if (classSymbols.containsKey(name)) {
      return classSymbols.get(name)[1].toString();
    } 
    return "NONE";
  }

  private String typeOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return subroutineSymbols.get(name)[0].toString();
    }
    return classSymbols.get(name)[0].toString();
  }

  private int indexOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return (Integer) subroutineSymbols.get(name)[2];
    }
    if (classSymbols.containsKey(name)) {
      return (Integer) classSymbols.get(name)[2];
    }
    return 0;
  }
}