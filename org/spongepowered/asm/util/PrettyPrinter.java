package org.spongepowered.asm.util;

import com.google.common.base.Strings;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrettyPrinter {
  public static interface IPrettyPrintable {
    void print(PrettyPrinter param1PrettyPrinter);
  }
  
  static interface IVariableWidthEntry {
    int getWidth();
  }
  
  static interface ISpecialEntry {}
  
  class KeyValue implements IVariableWidthEntry {
    private final String key;
    
    private final Object value;
    
    public KeyValue(String key, Object value) {
      this.key = key;
      this.value = value;
    }
    
    public String toString() {
      return String.format(PrettyPrinter.this.kvFormat, new Object[] { this.key, this.value });
    }
    
    public int getWidth() {
      return toString().length();
    }
  }
  
  class HorizontalRule implements ISpecialEntry {
    private final char[] hrChars;
    
    public HorizontalRule(char... hrChars) {
      this.hrChars = hrChars;
    }
    
    public String toString() {
      return Strings.repeat(new String(this.hrChars), PrettyPrinter.this.width + 2);
    }
  }
  
  class CentredText {
    private final Object centred;
    
    public CentredText(Object centred) {
      this.centred = centred;
    }
    
    public String toString() {
      String text = this.centred.toString();
      return String.format("%" + ((PrettyPrinter.this.width - text.length()) / 2 + text.length()) + "s", new Object[] { text });
    }
  }
  
  public enum Alignment {
    LEFT, RIGHT;
  }
  
  static class Table implements IVariableWidthEntry {
    final List<PrettyPrinter.Column> columns = new ArrayList<PrettyPrinter.Column>();
    
    final List<PrettyPrinter.Row> rows = new ArrayList<PrettyPrinter.Row>();
    
    String format = "%s";
    
    int colSpacing = 2;
    
    boolean addHeader = true;
    
    void headerAdded() {
      this.addHeader = false;
    }
    
    void setColSpacing(int spacing) {
      this.colSpacing = Math.max(0, spacing);
      updateFormat();
    }
    
    Table grow(int size) {
      while (this.columns.size() < size)
        this.columns.add(new PrettyPrinter.Column(this)); 
      updateFormat();
      return this;
    }
    
    PrettyPrinter.Column add(PrettyPrinter.Column column) {
      this.columns.add(column);
      return column;
    }
    
    PrettyPrinter.Row add(PrettyPrinter.Row row) {
      this.rows.add(row);
      return row;
    }
    
    PrettyPrinter.Column addColumn(String title) {
      return add(new PrettyPrinter.Column(this, title));
    }
    
    PrettyPrinter.Column addColumn(PrettyPrinter.Alignment align, int size, String title) {
      return add(new PrettyPrinter.Column(this, align, size, title));
    }
    
    PrettyPrinter.Row addRow(Object... args) {
      return add(new PrettyPrinter.Row(this, args));
    }
    
    void updateFormat() {
      String spacing = Strings.repeat(" ", this.colSpacing);
      StringBuilder format = new StringBuilder();
      boolean addSpacing = false;
      for (PrettyPrinter.Column column : this.columns) {
        if (addSpacing)
          format.append(spacing); 
        addSpacing = true;
        format.append(column.getFormat());
      } 
      this.format = format.toString();
    }
    
    String getFormat() {
      return this.format;
    }
    
    Object[] getTitles() {
      List<Object> titles = new ArrayList();
      for (PrettyPrinter.Column column : this.columns)
        titles.add(column.getTitle()); 
      return titles.toArray();
    }
    
    public String toString() {
      int i;
      boolean nonEmpty = false;
      String[] titles = new String[this.columns.size()];
      for (int col = 0; col < this.columns.size(); col++) {
        titles[col] = ((PrettyPrinter.Column)this.columns.get(col)).toString();
        i = nonEmpty | (!titles[col].isEmpty() ? 1 : 0);
      } 
      return (i != 0) ? String.format(this.format, (Object[])titles) : null;
    }
    
    public int getWidth() {
      String str = toString();
      return (str != null) ? str.length() : 0;
    }
  }
  
  static class Column {
    private final PrettyPrinter.Table table;
    
    private PrettyPrinter.Alignment align = PrettyPrinter.Alignment.LEFT;
    
    private int minWidth = 1;
    
    private int maxWidth = Integer.MAX_VALUE;
    
    private int size = 0;
    
    private String title = "";
    
    private String format = "%s";
    
    Column(PrettyPrinter.Table table) {
      this.table = table;
    }
    
    Column(PrettyPrinter.Table table, String title) {
      this(table);
      this.title = title;
      this.minWidth = title.length();
      updateFormat();
    }
    
    Column(PrettyPrinter.Table table, PrettyPrinter.Alignment align, int size, String title) {
      this(table, title);
      this.align = align;
      this.size = size;
    }
    
    void setAlignment(PrettyPrinter.Alignment align) {
      this.align = align;
      updateFormat();
    }
    
    void setWidth(int width) {
      if (width > this.size) {
        this.size = width;
        updateFormat();
      } 
    }
    
    void setMinWidth(int width) {
      if (width > this.minWidth) {
        this.minWidth = width;
        updateFormat();
      } 
    }
    
    void setMaxWidth(int width) {
      this.size = Math.min(this.size, this.maxWidth);
      this.maxWidth = Math.max(1, width);
      updateFormat();
    }
    
    void setTitle(String title) {
      this.title = title;
      setWidth(title.length());
    }
    
    private void updateFormat() {
      int width = Math.min(this.maxWidth, (this.size == 0) ? this.minWidth : this.size);
      this.format = "%" + ((this.align == PrettyPrinter.Alignment.RIGHT) ? "" : "-") + width + "s";
      this.table.updateFormat();
    }
    
    int getMaxWidth() {
      return this.maxWidth;
    }
    
    String getTitle() {
      return this.title;
    }
    
    String getFormat() {
      return this.format;
    }
    
    public String toString() {
      if (this.title.length() > this.maxWidth)
        return this.title.substring(0, this.maxWidth); 
      return this.title;
    }
  }
  
  static class Row implements IVariableWidthEntry {
    final PrettyPrinter.Table table;
    
    final String[] args;
    
    public Row(PrettyPrinter.Table table, Object... args) {
      this.table = table.grow(args.length);
      this.args = new String[args.length];
      for (int i = 0; i < args.length; i++) {
        this.args[i] = args[i].toString();
        ((PrettyPrinter.Column)this.table.columns.get(i)).setMinWidth(this.args[i].length());
      } 
    }
    
    public String toString() {
      Object[] args = new Object[this.table.columns.size()];
      for (int col = 0; col < args.length; col++) {
        PrettyPrinter.Column column = this.table.columns.get(col);
        if (col >= this.args.length) {
          args[col] = "";
        } else {
          args[col] = (this.args[col].length() > column.getMaxWidth()) ? this.args[col].substring(0, column.getMaxWidth()) : this.args[col];
        } 
      } 
      return String.format(this.table.format, args);
    }
    
    public int getWidth() {
      return toString().length();
    }
  }
  
  private final HorizontalRule horizontalRule = new HorizontalRule(new char[] { '*' });
  
  private final List<Object> lines = new ArrayList();
  
  private Table table;
  
  private boolean recalcWidth = false;
  
  protected int width = 100;
  
  protected int wrapWidth = 80;
  
  protected int kvKeyWidth = 10;
  
  protected String kvFormat = makeKvFormat(this.kvKeyWidth);
  
  public PrettyPrinter() {
    this(100);
  }
  
  public PrettyPrinter(int width) {
    this.width = width;
  }
  
  public PrettyPrinter wrapTo(int wrapWidth) {
    this.wrapWidth = wrapWidth;
    return this;
  }
  
  public int wrapTo() {
    return this.wrapWidth;
  }
  
  public PrettyPrinter table() {
    this.table = new Table();
    return this;
  }
  
  public PrettyPrinter table(String... titles) {
    this.table = new Table();
    for (String title : titles)
      this.table.addColumn(title); 
    return this;
  }
  
  public PrettyPrinter table(Object... format) {
    this.table = new Table();
    Column column = null;
    for (Object entry : format) {
      if (entry instanceof String) {
        column = this.table.addColumn((String)entry);
      } else if (entry instanceof Integer && column != null) {
        int width = ((Integer)entry).intValue();
        if (width > 0) {
          column.setWidth(width);
        } else if (width < 0) {
          column.setMaxWidth(-width);
        } 
      } else if (entry instanceof Alignment && column != null) {
        column.setAlignment((Alignment)entry);
      } else if (entry != null) {
        column = this.table.addColumn(entry.toString());
      } 
    } 
    return this;
  }
  
  public PrettyPrinter spacing(int spacing) {
    if (this.table == null)
      this.table = new Table(); 
    this.table.setColSpacing(spacing);
    return this;
  }
  
  public PrettyPrinter th() {
    return th(false);
  }
  
  private PrettyPrinter th(boolean onlyIfNeeded) {
    if (this.table == null)
      this.table = new Table(); 
    if (!onlyIfNeeded || this.table.addHeader) {
      this.table.headerAdded();
      addLine(this.table);
    } 
    return this;
  }
  
  public PrettyPrinter tr(Object... args) {
    th(true);
    addLine(this.table.addRow(args));
    this.recalcWidth = true;
    return this;
  }
  
  public PrettyPrinter add() {
    addLine("");
    return this;
  }
  
  public PrettyPrinter add(String string) {
    addLine(string);
    this.width = Math.max(this.width, string.length());
    return this;
  }
  
  public PrettyPrinter add(String format, Object... args) {
    String line = String.format(format, args);
    addLine(line);
    this.width = Math.max(this.width, line.length());
    return this;
  }
  
  public PrettyPrinter add(Object[] array) {
    return add(array, "%s");
  }
  
  public PrettyPrinter add(Object[] array, String format) {
    for (Object element : array) {
      add(format, new Object[] { element });
    } 
    return this;
  }
  
  public PrettyPrinter addIndexed(Object[] array) {
    int indexWidth = String.valueOf(array.length - 1).length();
    String format = "[%" + indexWidth + "d] %s";
    for (int index = 0; index < array.length; index++) {
      add(format, new Object[] { Integer.valueOf(index), array[index] });
    } 
    return this;
  }
  
  public PrettyPrinter addWithIndices(Collection<?> c) {
    return addIndexed(c.toArray());
  }
  
  public PrettyPrinter add(IPrettyPrintable printable) {
    if (printable != null)
      printable.print(this); 
    return this;
  }
  
  public PrettyPrinter add(Throwable th) {
    return add(th, 4);
  }
  
  public PrettyPrinter add(Throwable th, int indent) {
    while (th != null) {
      add("%s: %s", new Object[] { th.getClass().getName(), th.getMessage() });
      add(th.getStackTrace(), indent);
      th = th.getCause();
    } 
    return this;
  }
  
  public PrettyPrinter add(StackTraceElement[] stackTrace, int indent) {
    String margin = Strings.repeat(" ", indent);
    for (StackTraceElement st : stackTrace) {
      add("%s%s", new Object[] { margin, st });
    } 
    return this;
  }
  
  public PrettyPrinter add(Object object) {
    return add(object, 0);
  }
  
  public PrettyPrinter add(Object object, int indent) {
    String margin = Strings.repeat(" ", indent);
    return append(object, indent, margin);
  }
  
  private PrettyPrinter append(Object object, int indent, String margin) {
    if (object instanceof String)
      return add("%s%s", new Object[] { margin, object }); 
    if (object instanceof Iterable) {
      for (Object entry : object)
        append(entry, indent, margin); 
      return this;
    } 
    if (object instanceof Map) {
      kvWidth(indent);
      return add((Map<?, ?>)object);
    } 
    if (object instanceof IPrettyPrintable)
      return add((IPrettyPrintable)object); 
    if (object instanceof Throwable)
      return add((Throwable)object, indent); 
    if (object.getClass().isArray())
      return add((Object[])object, indent + "%s"); 
    return add("%s%s", new Object[] { margin, object });
  }
  
  public PrettyPrinter addWrapped(String format, Object... args) {
    return addWrapped(this.wrapWidth, format, args);
  }
  
  public PrettyPrinter addWrapped(int width, String format, Object... args) {
    String indent = "";
    String line = String.format(format, args).replace("\t", "    ");
    Matcher indentMatcher = Pattern.compile("^(\\s+)(.*)$").matcher(line);
    if (indentMatcher.matches())
      indent = indentMatcher.group(1); 
    try {
      for (String wrappedLine : getWrapped(width, line, indent))
        addLine(wrappedLine); 
    } catch (Exception ex) {
      add(line);
    } 
    return this;
  }
  
  private List<String> getWrapped(int width, String line, String indent) {
    List<String> lines = new ArrayList<String>();
    while (line.length() > width) {
      int wrapPoint = line.lastIndexOf(' ', width);
      if (wrapPoint < 10)
        wrapPoint = width; 
      String head = line.substring(0, wrapPoint);
      lines.add(head);
      line = indent + line.substring(wrapPoint + 1);
    } 
    if (line.length() > 0)
      lines.add(line); 
    return lines;
  }
  
  public PrettyPrinter kv(String key, String format, Object... args) {
    return kv(key, String.format(format, args));
  }
  
  public PrettyPrinter kv(String key, Object value) {
    addLine(new KeyValue(key, value));
    return kvWidth(key.length());
  }
  
  public PrettyPrinter kvWidth(int width) {
    if (width > this.kvKeyWidth) {
      this.kvKeyWidth = width;
      this.kvFormat = makeKvFormat(width);
    } 
    this.recalcWidth = true;
    return this;
  }
  
  public PrettyPrinter add(Map<?, ?> map) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      String key = (entry.getKey() == null) ? "null" : entry.getKey().toString();
      kv(key, entry.getValue());
    } 
    return this;
  }
  
  public PrettyPrinter hr() {
    return hr('*');
  }
  
  public PrettyPrinter hr(char ruleChar) {
    addLine(new HorizontalRule(new char[] { ruleChar }));
    return this;
  }
  
  public PrettyPrinter centre() {
    if (!this.lines.isEmpty()) {
      Object lastLine = this.lines.get(this.lines.size() - 1);
      if (lastLine instanceof String)
        addLine(new CentredText(this.lines.remove(this.lines.size() - 1))); 
    } 
    return this;
  }
  
  private void addLine(Object line) {
    if (line == null)
      return; 
    this.lines.add(line);
    this.recalcWidth |= line instanceof IVariableWidthEntry;
  }
  
  public PrettyPrinter trace() {
    return trace(getDefaultLoggerName());
  }
  
  public PrettyPrinter trace(Level level) {
    return trace(getDefaultLoggerName(), level);
  }
  
  public PrettyPrinter trace(String logger) {
    return trace(System.err, LogManager.getLogger(logger));
  }
  
  public PrettyPrinter trace(String logger, Level level) {
    return trace(System.err, LogManager.getLogger(logger), level);
  }
  
  public PrettyPrinter trace(Logger logger) {
    return trace(System.err, logger);
  }
  
  public PrettyPrinter trace(Logger logger, Level level) {
    return trace(System.err, logger, level);
  }
  
  public PrettyPrinter trace(PrintStream stream) {
    return trace(stream, getDefaultLoggerName());
  }
  
  public PrettyPrinter trace(PrintStream stream, Level level) {
    return trace(stream, getDefaultLoggerName(), level);
  }
  
  public PrettyPrinter trace(PrintStream stream, String logger) {
    return trace(stream, LogManager.getLogger(logger));
  }
  
  public PrettyPrinter trace(PrintStream stream, String logger, Level level) {
    return trace(stream, LogManager.getLogger(logger), level);
  }
  
  public PrettyPrinter trace(PrintStream stream, Logger logger) {
    return trace(stream, logger, Level.DEBUG);
  }
  
  public PrettyPrinter trace(PrintStream stream, Logger logger, Level level) {
    log(logger, level);
    print(stream);
    return this;
  }
  
  public PrettyPrinter print() {
    return print(System.err);
  }
  
  public PrettyPrinter print(PrintStream stream) {
    updateWidth();
    printSpecial(stream, this.horizontalRule);
    for (Object line : this.lines) {
      if (line instanceof ISpecialEntry) {
        printSpecial(stream, (ISpecialEntry)line);
        continue;
      } 
      printString(stream, line.toString());
    } 
    printSpecial(stream, this.horizontalRule);
    return this;
  }
  
  private void printSpecial(PrintStream stream, ISpecialEntry line) {
    stream.printf("/*%s*/\n", new Object[] { line.toString() });
  }
  
  private void printString(PrintStream stream, String string) {
    if (string != null)
      stream.printf("/* %-" + this.width + "s */\n", new Object[] { string }); 
  }
  
  public PrettyPrinter log(Logger logger) {
    return log(logger, Level.INFO);
  }
  
  public PrettyPrinter log(Logger logger, Level level) {
    updateWidth();
    logSpecial(logger, level, this.horizontalRule);
    for (Object line : this.lines) {
      if (line instanceof ISpecialEntry) {
        logSpecial(logger, level, (ISpecialEntry)line);
        continue;
      } 
      logString(logger, level, line.toString());
    } 
    logSpecial(logger, level, this.horizontalRule);
    return this;
  }
  
  private void logSpecial(Logger logger, Level level, ISpecialEntry line) {
    logger.log(level, "/*{}*/", new Object[] { line.toString() });
  }
  
  private void logString(Logger logger, Level level, String line) {
    if (line != null)
      logger.log(level, String.format("/* %-" + this.width + "s */", new Object[] { line })); 
  }
  
  private void updateWidth() {
    if (this.recalcWidth) {
      this.recalcWidth = false;
      for (Object line : this.lines) {
        if (line instanceof IVariableWidthEntry)
          this.width = Math.min(4096, Math.max(this.width, ((IVariableWidthEntry)line).getWidth())); 
      } 
    } 
  }
  
  private static String makeKvFormat(int keyWidth) {
    return String.format("%%%ds : %%s", new Object[] { Integer.valueOf(keyWidth) });
  }
  
  private static String getDefaultLoggerName() {
    String name = (new Throwable()).getStackTrace()[2].getClassName();
    int pos = name.lastIndexOf('.');
    return (pos == -1) ? name : name.substring(pos + 1);
  }
  
  public static void dumpStack() {
    (new PrettyPrinter()).add(new Exception("Stack trace")).print(System.err);
  }
  
  public static void print(Throwable th) {
    (new PrettyPrinter()).add(th).print(System.err);
  }
}
