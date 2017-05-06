package com.eemc.aida.elf;
import java.util.*;

public class Segment
{
	public int type;
	public int offset;
	public int vaddr;
	public int paddr;
	public int filesz;
	public int memsz;
	public int flags;
	public int align;
	public Vector<Section>sections=new Vector<Section>();
}
