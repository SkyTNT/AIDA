package com.eemc.aida.elf;

public class header
{
	public byte[] ident = new byte[16];
	public int type ;
	public int machine ;
	public int version;
	public int entry;
	public int phoff;
	public int shoff;
	public int flags;
	public int ehsize;
	public int phentsize;
	public int phnum;
	public int shentsize;
	public int shnum;
	public int shstrndx;
}
