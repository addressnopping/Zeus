package org.spongepowered.asm.util;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionNumber implements Comparable<VersionNumber>, Serializable {
  private static final long serialVersionUID = 1L;
  
  public static final VersionNumber NONE = new VersionNumber();
  
  private static final Pattern PATTERN = Pattern.compile("^(\\d{1,5})(?:\\.(\\d{1,5})(?:\\.(\\d{1,5})(?:\\.(\\d{1,5}))?)?)?(-[a-zA-Z0-9_\\-]+)?$");
  
  private final long value;
  
  private final String suffix;
  
  private VersionNumber() {
    this.value = 0L;
    this.suffix = "";
  }
  
  private VersionNumber(short[] parts) {
    this(parts, null);
  }
  
  private VersionNumber(short[] parts, String suffix) {
    this.value = pack(parts);
    this.suffix = (suffix != null) ? suffix : "";
  }
  
  private VersionNumber(short major, short minor, short revision, short build) {
    this(major, minor, revision, build, null);
  }
  
  private VersionNumber(short major, short minor, short revision, short build, String suffix) {
    this.value = pack(new short[] { major, minor, revision, build });
    this.suffix = (suffix != null) ? suffix : "";
  }
  
  public String toString() {
    short[] parts = unpack(this.value);
    return String.format("%d.%d%3$s%4$s%5$s", new Object[] { Short.valueOf(parts[0]), 
          Short.valueOf(parts[1]), ((this.value & 0x7FFFFFFFL) > 0L) ? 
          String.format(".%d", new Object[] { Short.valueOf(parts[2]) }) : "", ((this.value & 0x7FFFL) > 0L) ? 
          String.format(".%d", new Object[] { Short.valueOf(parts[3]) }) : "", this.suffix });
  }
  
  public int compareTo(VersionNumber other) {
    if (other == null)
      return 1; 
    long delta = this.value - other.value;
    return (delta > 0L) ? 1 : ((delta < 0L) ? -1 : 0);
  }
  
  public boolean equals(Object other) {
    if (!(other instanceof VersionNumber))
      return false; 
    return (((VersionNumber)other).value == this.value);
  }
  
  public int hashCode() {
    return (int)(this.value >> 32L) ^ (int)(this.value & 0xFFFFFFFFL);
  }
  
  private static long pack(short... shorts) {
    return shorts[0] << 48L | shorts[1] << 32L | (shorts[2] << 16) | shorts[3];
  }
  
  private static short[] unpack(long along) {
    return new short[] { (short)(int)(along >> 48L), (short)(int)(along >> 32L & 0x7FFFL), (short)(int)(along >> 16L & 0x7FFFL), (short)(int)(along & 0x7FFFL) };
  }
  
  public static VersionNumber parse(String version) {
    return parse(version, NONE);
  }
  
  public static VersionNumber parse(String version, String defaultVersion) {
    return parse(version, parse(defaultVersion));
  }
  
  private static VersionNumber parse(String version, VersionNumber defaultVersion) {
    if (version == null)
      return defaultVersion; 
    Matcher versionNumberPatternMatcher = PATTERN.matcher(version);
    if (!versionNumberPatternMatcher.matches())
      return defaultVersion; 
    short[] parts = new short[4];
    for (int pos = 0; pos < 4; pos++) {
      String part = versionNumberPatternMatcher.group(pos + 1);
      if (part != null) {
        int value = Integer.parseInt(part);
        if (value > 32767)
          throw new IllegalArgumentException("Version parts cannot exceed 32767, found " + value); 
        parts[pos] = (short)value;
      } 
    } 
    return new VersionNumber(parts, versionNumberPatternMatcher.group(5));
  }
}
