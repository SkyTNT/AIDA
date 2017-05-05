package com.eemc.aida.elf;
import java.util.*;

public class segment
{
	public int type;
	public int offset;
	public int vaddr;
	public int paddr;
	public int filesz;
	public int memsz;
	public int flags;
	public int align;
	public Vector<section>sections=new Vector<section>();
}
