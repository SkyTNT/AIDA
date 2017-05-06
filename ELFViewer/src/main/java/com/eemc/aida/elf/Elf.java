package com.eemc.aida.elf;
import java.util.*;

public class Elf
{
	public Header hdr=new Header();
	public Vector<Segment> segments = new Vector<Segment>();
	public Vector<Section> sections = new Vector<Section>();
	
	public Elf() {
	}
}
