import java.util.Hashtable;

public class SymbolTable {
  private Hashtable<String, SymbolAttributes> classSymbols;
  private Hashtable<String, SymbolAttributes> subroutineSymbols;
  private int lclCount;
  private int staticCount;
  private int fieldCount;
  private int argCount;

  public SymbolTable() {
    this.classSymbols = new Hashtable<String, SymbolAttributes>();
    this.subroutineSymbols = new Hashtable<String, SymbolAttributes>();
    lclCount = 0;
    staticCount = 0;
    fieldCount = 0;
    argCount = 0;
  }

  public void startSubroutine() {
    subroutineSymbols.clear();
    lclCount = 0;
    argCount = 0;
  }

  public void define(String name, String type, String kind) {
    SymbolAttributes attributes = new SymbolAttributes();
    attributes.type = type;
    attributes.kind = kind;
    switch (kind) {
      case "arg":
        attributes.index = argCount;
        subroutineSymbols.put(name, attributes);
        argCount++;
        break;
      case "var":
        attributes.index = lclCount;
        subroutineSymbols.put(name, attributes);
        lclCount++;
        break;
      case "static":
        attributes.index = staticCount;
        classSymbols.put(name, attributes);
        staticCount++;
        break;
      case "field":
        attributes.index = fieldCount;
        classSymbols.put(name, attributes);
        fieldCount++;
        break;
    }
  }

  public int varCount(String kind) {
    int count = 0;
    switch (kind) {
      case "arg":
        count = argCount;
        break;
      case "var":
        count = lclCount;
        break;
      case "static":
        count = staticCount;
        break;
      case "field":
        count = fieldCount;
        break;
    }
    return count;
  }

  public String kindOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return subroutineSymbols.get(name).kind;
    } else if (classSymbols.containsKey(name)) {
      return classSymbols.get(name).kind;
    } 
    return "NONE";
  }

  public String typeOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return subroutineSymbols.get(name).type;
    }
    return classSymbols.get(name).type;
  }

  public int indexOf(String name) {
    if (subroutineSymbols.containsKey(name)) {
      return subroutineSymbols.get(name).index;
    }
    if (classSymbols.containsKey(name)) {
      return classSymbols.get(name).index;
    }
    return 0;
  }
}