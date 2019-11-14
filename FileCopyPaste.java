package main;

import java.io.*;
import java.nio.file.*;

public class FileCopyPaste {
	private String filePath;
	private final String output = "C:\\Users\\jonat\\Desktop\\PasteTo.txt";
	private File inputFile;
	private byte[] contents;
	private byte[] key = new byte[16];
	private AES aes;
	
	public FileCopyPaste(String path, String fileType) {
		key = new byte[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		aes = new AES(key);
		this.filePath = path;
		inputFile = new File(filePath);
		System.out.println(inputFile.length());
		try {
			getData();
		
			byte[] cipherText = aes.encrypt(contents);
			byte[] plainText = aes.decrypt(cipherText);
			contents = plainText;
			
			write();
		}catch(IOException ioe) {
			
		}
	}
	
	public void getData() throws IOException {
		contents = Files.readAllBytes(inputFile.toPath());	
		System.out.println(contents.length);
		//System.out.println(Arrays.toString(contents));
	}
	
	public void write() throws IOException { 
		FileOutputStream fos = new FileOutputStream(output);
		fos.write(contents);
		fos.write(contents);
		fos.close(); 
	}
} 
