package main;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class MultiFileEncryption extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private static AES aes = null;
	private static byte[] key = new byte[16];//16;
	
	private int percentFinished = 0;
	
	private int iter = 1;
	private boolean hasPassword = false;
	
	private static Container container;
	private JProgressBar progressBar;
	private JCheckBox copyCheckbox;
	private JButton encryptBTN;
	private JButton decryptBTN;
	
	private File[] files;
	private long[] sizes;
	private EncryptTask et;
	private DecryptTask dt;
	
	public MultiFileEncryption(int defaultIter, File[] files, long fileSizes){
		this.files = files;
		sizes = new long[files.length];
		for(int i = 0; i < files.length; i++) {
			sizes[i] = files[i].length();
		}
		progressBar = new JProgressBar(0, 100);
		iter = defaultIter;
		addWindowListener(new WindowAdapter(){
  			public void windowClosing(WindowEvent e) {
  				if(aes != null) aes.keyErase();
  				
  				Arrays.fill(key, (byte)0);
          		
          	}
  		});
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
        
        //buttons:
        JPanel buttons = new JPanel();
        copyCheckbox = new JCheckBox("Make copy of file");
        encryptBTN = new JButton("Encrypt File");
        encryptBTN.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				encryptBTN.setEnabled(false);
				et = new EncryptTask();
				et.execute();
			}
		});
        decryptBTN = new JButton("Decrypt File");
        decryptBTN.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				decryptBTN.setEnabled(false);
				dt = new DecryptTask();
				dt.execute();
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
        buttons.add(copyCheckbox);
        buttons.add(encryptBTN);
        buttons.add(decryptBTN);
        buttons.add(iterations);
            
        container.add(passwordPanel);
        container.add(buttons);
        
       // JPanel loadingBar = new JPanel();

        progressBar.setStringPainted(true);
        progressBar.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (name.equals("progress")) {
                    int progress = (int) evt.getNewValue();
                    progressBar.setValue(progress);
                    repaint();
                } 
            }

        });
        container.add(progressBar);
        
        
        //final things:
        pack();
		setVisible(true);
		unlock.requestFocus();
	}

	private void encryptFile(File f, boolean makeCopy){
		byte[] content;
		byte[] cipherText;
		try { 
			content = Files.readAllBytes(f.toPath()); 
			cipherText = aes.encrypt(content);
			if(cipherText.length == 0) JOptionPane.showMessageDialog(null, "Error occurred when encrypting file");
			if(makeCopy) {
				String fileName = f.getPath();
				String first = fileName.substring(0, fileName.lastIndexOf("."));
				String second = fileName.substring(fileName.lastIndexOf("."));
				writeFile(cipherText, new File(first + "_EncryptedCopy" + second));
			}
			else writeFile(cipherText, f);
			Arrays.fill(cipherText, (byte)0);
			Arrays.fill(content, (byte)0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error occured when writing to file", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void decryptFile(File f, boolean makeCopy) {
		byte[] content;
		byte[] plainText;
		try {
			content = Files.readAllBytes(f.toPath());
			plainText = aes.decrypt(content);
			if(plainText.length == 0) JOptionPane.showMessageDialog(null, "Error occurred when decrypting");
			if(makeCopy) {
				String fileName = f.getPath();
				String first = fileName.substring(0, fileName.lastIndexOf("."));
				String second = fileName.substring(fileName.lastIndexOf("."));
				writeFile(plainText, new File(first + "_DecryptedCopy" + second));
			}
			else writeFile(plainText, f);
			Arrays.fill(plainText, (byte)0);
			Arrays.fill(content, (byte)0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Error occured when writing to file", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	
	private void writeFile(byte[] out, File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f.getPath());
		fos.write(out);
		fos.close(); 
		Arrays.fill(out, (byte)0);
	}
	
	private class EncryptTask extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
        	if(hasPassword) {
        		long total = 0;
				for(int i = 0; i < sizes.length; i++) {
					for(int j = 0; j < iter; j++) {
						total += sizes[i];
					}
				}
				long done = 0;
				for(int i = 0; i < files.length; i++) {
					for(int j = 0; j < iter; j++) {
						if(copyCheckbox.isSelected()) encryptFile(files[i], true);
						else encryptFile(files[i], false);
						done += sizes[i];
						percentFinished = (int)(100*((double)done/(double)total));
						progressBar.setValue(percentFinished);
					}
				}
			}
			else 
				JOptionPane.showMessageDialog(null, "Password Required", "Error", JOptionPane.ERROR_MESSAGE);
        	return null;
        }
        @Override
        public void done() {
          Toolkit.getDefaultToolkit().beep();
          setCursor(null); // turn off the wait cursor
          encryptBTN.setEnabled(true);
        }
    }
	private class DecryptTask extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
        	if(hasPassword) {
        		long total = 0;
				for(int i = 0; i < sizes.length; i++) {
					for(int j = 0; j < iter; j++) {
						total += sizes[i];
					}
				}
				long done = 0;
				for(int i = 0; i < files.length; i++) {
					for(int j = 0; j < iter; j++) {
						if(copyCheckbox.isSelected()) decryptFile(files[i], true);
						else decryptFile(files[i], false);
						done += sizes[i];
						percentFinished = (int)(100*((double)done/(double)total));
						progressBar.setValue(percentFinished);
					}
				}
			}	
			else 
				JOptionPane.showMessageDialog(null, "Password Required", "Error", JOptionPane.ERROR_MESSAGE);
        	return null;
        }
        @Override
        public void done() {
          Toolkit.getDefaultToolkit().beep();
          setCursor(null); // turn off the wait cursor
          decryptBTN.setEnabled(true);
        }
    }
}
