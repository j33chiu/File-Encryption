package main;
 
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
 
public class FileChooser extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	static private final String newline = "\n";
    JButton openButton;
    JTextArea log;
    JFileChooser fc;
    static JFrame frame;
    private static boolean chosen = false;
 
    public FileChooser() {
        super(new BorderLayout());
 
        //log area
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
 
        //Create a file chooser
        fc = new JFileChooser(System.getProperty("user.home") + "\\Desktop");
        fc.setMultiSelectionEnabled(true);
        fc.setPreferredSize(new Dimension(1000, 1400));
        /*FileFilter filter = new FileNameExtensionFilter("TXT file", "txt");
        fc.setFileFilter(filter);*/
        setFont(fc.getComponents());
        
        openButton = new JButton("Open a File...");
        openButton.addActionListener(this);
 
        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
 
        //Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }
 
    public void actionPerformed(ActionEvent e) {
 
        //Handle open button action.
        if (e.getSource() == openButton && chosen == false) {
            int returnVal = fc.showOpenDialog(FileChooser.this);
 
            if (returnVal == JFileChooser.APPROVE_OPTION) {/*
                File file = fc.getSelectedFile();
                if(file.length() <= 104857600) {
                	  @SuppressWarnings("unused")
                	  FileEncryption fe = new FileEncryption(file.getPath(), file.length(), file.getName().length());
                      //FileCopyPaste fcp = new FileCopyPaste(file.getPath(), file.getName().substring(file.getName().lastIndexOf(".") + 1));
                	  log.append("File chosen: " + file.getPath() + newline);
                }
                else {
                	log.append("File chosen was too large (exceeded 100 megabytes)." + newline);
                }*/
            	File[] files = fc.getSelectedFiles();
            	long totalSize = 0;
            	for(File f: files) totalSize += f.length();
            	int iter = 0;
            	for(File f: files) {
            		if(f.getName().contains("_EncryptedCopy")) 
            			iter += f.getName().length() - 14;
            		else if(f.getName().contains("_DecryptedCopy"))
            			iter += f.getName().length() - 14;
            		else iter += f.getName().length();
            	}
            	iter = iter/files.length;
            	if(totalSize < 104857600 || true) {
            		@SuppressWarnings("unused")
					MultiFileEncryption mfe = new MultiFileEncryption(iter, files, totalSize);
            		for(File f: files) {
            			log.append("File chosen: " + f.getPath() + newline);
            		}
            	}
            	/*else {
                	log.append("File chosen was too large (exceeded 100 megabytes)." + newline);
                }*/
            		
               
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
 
    private static void createAndShowGUI() {
        //Create and set up the window.
        frame = new JFrame("File Encryption");
        frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
        		if(chosen) {
        			frame.setVisible(false);
        		}else {
        			System.exit(0);
        		}
        	}
		});
        //Add content to the window.
        frame.add(new FileChooser());
 
        //Display the window.
        frame.pack();
        frame.setLocation(600, 0);
        frame.setSize(600, 200);
        frame.setVisible(true);
    }
 
    public static void main(String[] args)
    {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
    
    public static void setFont(Component[] comp) 
    {
    	for(int i = 0; i < comp.length; i++) {
    		if(comp[i] instanceof Container) {
    			setFont(((Container)comp[i]).getComponents());
    			try {comp[i].setFont( comp[i].getFont().deriveFont( comp[i].getFont().getSize() * 1f ));}
    			catch(Exception e) {}
    		}
    	}
    }
}
