package main;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class FileEncryption extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static AES aes = null;
	private static byte[] key = new byte[16];
	private static byte[] content;
	private static String filePath;
	private File inputFile;
	private int iter = 1;
	private boolean hasPassword = false;
	
	private static Container container;
	
	public FileEncryption(String path, long size, int defaultIter){
		iter = defaultIter;
		//aes = new AES(key);
		addWindowListener(new WindowAdapter(){
  			public void windowClosing(WindowEvent e) {
  				if(aes != null) aes.keyErase();
  				Arrays.fill(content, (byte)0);
  				Arrays.fill(key, (byte)0);
          		
          	}
  		});
		filePath = path;
		inputFile = new File(filePath);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("File Encryption and Decryption");
        setResizable(false);
        setLocation(550,0);
        container = new Container();
        container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
        add(container);
        
        JPanel passwordPanel = new JPanel();
        JPasswordField unlock = new JPasswordField(20);
        JTextField passwordLBL = new JTextField("Password:");
        passwordLBL.setEditable(false);
        JButton passwordBTN = new JButton("Enter");
        
        Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				char[] input = unlock.getPassword();
				unlock.setEditable(false);
				int count = 0;
				for(char c: input) {
					key[count] = (byte) c;
					count++;
				}
				if(aes != null) aes.keyErase();
				key[15] = 13;
				aes = new AES(key);
				//System.out.println(Arrays.toString(key));
				hasPassword = true;
				Arrays.fill(input, '0');
			}
        };
        
        passwordBTN.addActionListener(action);
        unlock.addActionListener(action);
        
        passwordPanel.add(passwordLBL);
        passwordPanel.add(unlock);
        passwordPanel.add(passwordBTN);
        
        try {
			readFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
 
        
        //buttons:
        JPanel buttons = new JPanel();
        JButton encryptBTN = new JButton("Encrypt File");
        encryptBTN.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(hasPassword) {
					for(int i = 0; i < iter; i++) {
						encryptFile();
					}
				}
				else 
					JOptionPane.showMessageDialog(null, "Password Required", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
        JButton decryptBTN = new JButton("Decrypt File");
        decryptBTN.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(hasPassword) {
					for(int i = 0; i < iter; i++) {
						decryptFile();
					}
				}	
				else 
					JOptionPane.showMessageDialog(null, "Password Required", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
        
        JTextField iterations = new JTextField(4);
        iterations.setText(Integer.toString(defaultIter));
        iterations.getDocument().addDocumentListener(new DocumentListener(){
        	public void changedUpdate(DocumentEvent e) {
        		try {
        			iter = Integer.parseInt(iterations.getText());
        		}catch(Exception ex) {
        			iterations.setText(Integer.toString(defaultIter));
        			iter = defaultIter;
        		}
	    	}
			public void removeUpdate(DocumentEvent e) {
				try {
        			iter = Integer.parseInt(iterations.getText());
        		}catch(Exception ex) {
        			iterations.setText(Integer.toString(defaultIter));
        			iter = defaultIter;
        		}
			}
			public void insertUpdate(DocumentEvent e) {
				try {
        			iter = Integer.parseInt(iterations.getText());
        		}catch(Exception ex) {
        			iterations.setText(Integer.toString(defaultIter));
        			iter = defaultIter;
        		}
			}
        });
        
        buttons.add(encryptBTN);
        buttons.add(decryptBTN);
        buttons.add(iterations);
            
        container.add(passwordPanel);
        container.add(buttons);
        
        //final things:
        pack();
		setVisible(true);
		unlock.requestFocus();
	}

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		window = new FileEncryption();
		window.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				if(aes != null) aes.keyErase();
        		System.exit(0);
        	}
		});
	}	*/

	private void encryptFile(){
		byte[] cipherText = aes.encrypt(content);
		if(cipherText.length == 0) JOptionPane.showMessageDialog(null, "Error occurred when encrypting file");
		try {
			write(cipherText);
			readFile();
			Arrays.fill(cipherText, (byte)0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error occured when writing to file", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void decryptFile() {
		byte[] plainText = aes.decrypt(content);
		if(plainText.length == 0) JOptionPane.showMessageDialog(null, "Error occurred when decrypting");
		try {
			write(plainText);
			readFile();
			Arrays.fill(plainText, (byte)0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error occured when writing to file", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void readFile() throws IOException{
		content = Files.readAllBytes(inputFile.toPath());	
		//System.out.println(Arrays.toString(content));
	}
	
	public void write(byte[] out) throws IOException { 
		FileOutputStream fos = new FileOutputStream(filePath);
		fos.write(out);
		fos.close(); 
		Arrays.fill(out, (byte)0);
	}
}
