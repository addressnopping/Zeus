package me.alpha432.oyvey.util;

import java.awt.Color;
import org.lwjgl.opengl.GL11;

public class ColourHolder {
  int r;
  
  int g;
  
  int b;
  
  int a;
  
  public ColourHolder(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = 255;
  }
  
  public ColourHolder(int r, int g, int b, int a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }
  
  public ColourHolder brighter() {
    return new ColourHolder(Math.min(this.r + 10, 255), Math.min(this.g + 10, 255), Math.min(this.b + 10, 255), getA());
  }
  
  public ColourHolder darker() {
    return new ColourHolder(Math.max(this.r - 10, 0), Math.max(this.g - 10, 0), Math.max(this.b - 10, 0), getA());
  }
  
  public void setGLColour() {
    setGLColour(-1, -1, -1, -1);
  }
  
  public void setGLColour(int dr, int dg, int db, int da) {
    GL11.glColor4f(((dr == -1) ? this.r : dr) / 255.0F, ((dg == -1) ? this.g : dg) / 255.0F, ((db == -1) ? this.b : db) / 255.0F, ((da == -1) ? this.a : da) / 255.0F);
  }
  
  public void becomeGLColour() {}
  
  public void becomeHex(int hex) {
    setR((hex & 0xFF0000) >> 16);
    setG((hex & 0xFF00) >> 8);
    setB(hex & 0xFF);
    setA(255);
  }
  
  public static ColourHolder fromHex(int hex) {
    ColourHolder n = new ColourHolder(0, 0, 0);
    n.becomeHex(hex);
    return n;
  }
  
  public static int toHex(int r, int g, int b) {
    return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
  }
  
  public int toHex() {
    return toHex(this.r, this.g, this.b);
  }
  
  public int getB() {
    return this.b;
  }
  
  public int getG() {
    return this.g;
  }
  
  public int getR() {
    return this.r;
  }
  
  public int getA() {
    return this.a;
  }
  
  public ColourHolder setR(int r) {
    this.r = r;
    return this;
  }
  
  public ColourHolder setB(int b) {
    this.b = b;
    return this;
  }
  
  public ColourHolder setG(int g) {
    this.g = g;
    return this;
  }
  
  public ColourHolder setA(int a) {
    this.a = a;
    return this;
  }
  
  public ColourHolder clone() {
    return new ColourHolder(this.r, this.g, this.b, this.a);
  }
  
  public Color toJavaColour() {
    return new Color(this.r, this.g, this.b, this.a);
  }
}
