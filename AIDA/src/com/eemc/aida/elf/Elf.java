package com.eemc.aida.elf;
import java.util.*;

public class Elf
{
	public header hdr=new header();
	public Vector<segment> segments = new Vector<segment>();
	public Vector<section> sections = new Vector<section>();
	
	public Elf() {
	}
}
