package com.itheima.reggie;

import org.junit.jupiter.api.Test;

public class FileNameTest {
	
	@Test
	public void FileNameTest1() {
		String originalFileName="java66.jpg";
		
		String suffix=originalFileName.substring(originalFileName.lastIndexOf("."));
		
		System.out.println(suffix);
	}
}
