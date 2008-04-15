package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import javax.swing.filechooser.*;

import org.netbeans.jemmy.explorer.*;
import org.netbeans.jemmy.operators.*;

public class Application_031 extends TestFrame {

    JTextField tf;
    JButton btn;
    JFileChooser chooser;

    public Application_031() {
	super("Application_031");

	tf = new JTextField("");
	btn = new JButton("...");
	chooser = new JFileChooser();
	chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
	chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	btn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    tf.setText("");
                    org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("In actionPerformed");
                    int respond = chooser.showDialog(btn, "---");
                    org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("Out actionPerformed");
                    org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("");
		    if(respond == JFileChooser.APPROVE_OPTION) {
                        if(chooser.getSelectedFile() != null) {
                            tf.setText(chooser.getSelectedFile().getAbsolutePath());
                        } else {
                            tf.setText("");
                        }
		    }
		}
	    });
	chooser.addChoosableFileFilter(new NoDirFilter());
	chooser.addChoosableFileFilter(new NothingFilter());
	chooser.addChoosableFileFilter(new NoFileFilter());
	chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(btn, BorderLayout.EAST);
	getContentPane().add(tf, BorderLayout.CENTER);

	setSize(400, 100);
    }

    public static void main(String[] argv) {
	Application_031 app = new Application_031();
	app.show();
    }
    
    class NoFileFilter extends javax.swing.filechooser.FileFilter {
	public NoFileFilter() {
	    super();
	}
	public boolean accept(File f) {
	    return(f.isDirectory());
	}
	public String getDescription() {
	    return("No file");
	}
    }

    class NoDirFilter extends javax.swing.filechooser.FileFilter {
	public NoDirFilter() {
	    super();
	}
	public boolean accept(File f) {
	    return(!f.isDirectory());
	}
	public String getDescription() {
	    return("No directory");
	}
    }

    class NothingFilter extends javax.swing.filechooser.FileFilter {
	public NothingFilter() {
	    super();
	}
	public boolean accept(File f) {
	    return(false);
	}
	public String getDescription() {
	    return("Nothing");
	}
    }

}
