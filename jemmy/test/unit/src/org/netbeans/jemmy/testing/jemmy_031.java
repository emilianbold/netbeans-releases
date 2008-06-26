package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import org.netbeans.jemmy.demo.*;

public class jemmy_031 extends JemmyTest {
    JButtonOperator dotButtonOper;
    JFileChooserOperator chooseOper;
    File curDir;
    JFrameOperator winop;

    public int runIt(Object obj) {

	try {

            //JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();

	    (new ClassReference("org.netbeans.jemmy.testing.Application_031")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_031", true, true);
            winop = new JFrameOperator(win);
	    
	    dotButtonOper = new JButtonOperator(JButtonOperator.findJButton(win, "...", true, true));

	    curDir = new File(System.getProperty("user.dir"));

            invoke();
            cancel();

            invoke();

            chooseOper.selectFileType("All Files");
            chooseOper.selectFileType("No file");
            chooseOper.selectFileType("No directory");
            chooseOper.selectFileType("Nothing");

            approve();
            invoke();

	    JFileChooser d = JFileChooserOperator.waitJFileChooser();
	    getOutput().printLine("By find       : " +                      d.toString());
	    getOutput().printLine("By comstructor: " + chooseOper.getSource().toString());
	    if(chooseOper.getSource() != d) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

            approve();
            invoke();

	    if(!chooseOper.getCurrentDirectory().equals(curDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseOper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.toString());
		finalize();
		return(1);
	    }

            approve();
            invoke();

	    File[] allFiles = curDir.listFiles();
	    File firsFile = null;
	    for(int i = 0; i < allFiles.length; i++) {
		if(!allFiles[i].isDirectory()) {
		    firsFile = allFiles[i];
		    break;
		}
	    }

            approve();
            invoke();

	    if(firsFile != null) {
		chooseOper.selectFileType("No directory", true, true);
		if(!chooseOper.checkFileDisplayed(firsFile.getName(), true, true)) {
		    getOutput().printErrLine("selectFileType does not work.");
		    finalize();
		    return(1);
		}
	    }

            approve();
            invoke();

	    File firsDir = null;

	    allFiles = curDir.listFiles();
	    for(int i = 0; i < allFiles.length; i++) {
		if(allFiles[i].isDirectory()) {
		    firsDir = allFiles[i];
		    break;
		}
	    }

            approve();
            invoke();

	    if(firsDir != null) {
		chooseOper.selectFileType("No file", true, true);
		new QueueTool().waitEmpty(100);
		chooseOper.waitFileDisplayed(firsDir.getName());
	    }

            approve();
            invoke();

	    chooseOper.selectFileType("Nothing", true, true);
	    chooseOper.waitFileCount(0);
	    allFiles = curDir.listFiles();
            try {
                chooseOper.selectFileType("All", false, false);
                chooseOper.waitFileCount(allFiles.length);
            } catch(TimeoutExpiredException e) {
                getOutput().printErrLine("Wrong file count: " + chooseOper.getFileCount());
                getOutput().printErrLine("Expected:         " + allFiles.length);
                for(int i = 0; i < allFiles.length; i++) {
                    getOutput().printErrLine(allFiles[i].getCanonicalPath());
                }
                throw(e);
            }

	    chooseOper.goUpLevel();

	    if(!chooseOper.getCurrentDirectory().equals(curDir.getParentFile())) {
		getOutput().printErrLine("Wrong current directory : " + chooseOper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.getParentFile().toString());
		finalize();
		return(1);
	    }

	    chooseOper.enterSubDir(curDir.getCanonicalPath().substring(curDir.getParentFile().getCanonicalPath().length() + 1),
				   true, true);

	    if(!chooseOper.getCurrentDirectory().equals(curDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseOper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.toString());
		finalize();
		return(1);
	    }

            approve();
            invoke();

	    chooseOper.selectPathDirectory(curDir.getParentFile().getName(), true, true);

	    if(!chooseOper.getCurrentDirectory().equals(curDir.getParentFile())) {
		getOutput().printErrLine("Wrong current directory : " + chooseOper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.getParentFile().toString());
		finalize();
		return(1);
	    }

            approve();
            invoke();

	    chooseOper.goHome();

	    File homeDir = new File(System.getProperty("user.home"));

	    if(!chooseOper.getCurrentDirectory().equals(homeDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseOper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + homeDir.toString());
		finalize();
		return(1);
	    }

            approve();

	    if(!testJFileChooser(chooseOper)) {
		finalize();
		return(1);
	    }

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

    void cancel() {
        chooseOper.cancel();
    }

    void approve() throws IOException {
        String file = curDir.listFiles()[0].getCanonicalPath();
        chooseOper.chooseFile(file);
        new JTextFieldOperator(winop, file);
    }

    void invoke() {
        doSleep(2000);
        dotButtonOper.pushNoBlock();
        chooseOper = new JFileChooserOperator();
    }

public boolean testJFileChooser(JFileChooserOperator jFileChooserOperator) {
    if(((JFileChooser)jFileChooserOperator.getSource()).getAcceptAllFileFilter() == null &&
       jFileChooserOperator.getAcceptAllFileFilter() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getAcceptAllFileFilter().equals(jFileChooserOperator.getAcceptAllFileFilter())) {
        printLine("getAcceptAllFileFilter does work");
    } else {
        printLine("getAcceptAllFileFilter does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getAcceptAllFileFilter());
        printLine(jFileChooserOperator.getAcceptAllFileFilter());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getAccessory() == null &&
       jFileChooserOperator.getAccessory() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getAccessory().equals(jFileChooserOperator.getAccessory())) {
        printLine("getAccessory does work");
    } else {
        printLine("getAccessory does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getAccessory());
        printLine(jFileChooserOperator.getAccessory());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonMnemonic() == jFileChooserOperator.getApproveButtonMnemonic()) {
        printLine("getApproveButtonMnemonic does work");
    } else {
        printLine("getApproveButtonMnemonic does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonMnemonic());
        printLine(jFileChooserOperator.getApproveButtonMnemonic());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonText() == null &&
       jFileChooserOperator.getApproveButtonText() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonText().equals(jFileChooserOperator.getApproveButtonText())) {
        printLine("getApproveButtonText does work");
    } else {
        printLine("getApproveButtonText does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonText());
        printLine(jFileChooserOperator.getApproveButtonText());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonToolTipText() == null &&
       jFileChooserOperator.getApproveButtonToolTipText() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonToolTipText().equals(jFileChooserOperator.getApproveButtonToolTipText())) {
        printLine("getApproveButtonToolTipText does work");
    } else {
        printLine("getApproveButtonToolTipText does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getApproveButtonToolTipText());
        printLine(jFileChooserOperator.getApproveButtonToolTipText());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getCurrentDirectory() == null &&
       jFileChooserOperator.getCurrentDirectory() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getCurrentDirectory().equals(jFileChooserOperator.getCurrentDirectory())) {
        printLine("getCurrentDirectory does work");
    } else {
        printLine("getCurrentDirectory does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getCurrentDirectory());
        printLine(jFileChooserOperator.getCurrentDirectory());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getDialogTitle() == null &&
       jFileChooserOperator.getDialogTitle() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getDialogTitle().equals(jFileChooserOperator.getDialogTitle())) {
        printLine("getDialogTitle does work");
    } else {
        printLine("getDialogTitle does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getDialogTitle());
        printLine(jFileChooserOperator.getDialogTitle());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getDialogType() == jFileChooserOperator.getDialogType()) {
        printLine("getDialogType does work");
    } else {
        printLine("getDialogType does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getDialogType());
        printLine(jFileChooserOperator.getDialogType());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getFileFilter() == null &&
       jFileChooserOperator.getFileFilter() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getFileFilter().equals(jFileChooserOperator.getFileFilter())) {
        printLine("getFileFilter does work");
    } else {
        printLine("getFileFilter does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getFileFilter());
        printLine(jFileChooserOperator.getFileFilter());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getFileSelectionMode() == jFileChooserOperator.getFileSelectionMode()) {
        printLine("getFileSelectionMode does work");
    } else {
        printLine("getFileSelectionMode does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getFileSelectionMode());
        printLine(jFileChooserOperator.getFileSelectionMode());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getFileSystemView() == null &&
       jFileChooserOperator.getFileSystemView() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getFileSystemView().equals(jFileChooserOperator.getFileSystemView())) {
        printLine("getFileSystemView does work");
    } else {
        printLine("getFileSystemView does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getFileSystemView());
        printLine(jFileChooserOperator.getFileSystemView());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getFileView() == null &&
       jFileChooserOperator.getFileView() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getFileView().equals(jFileChooserOperator.getFileView())) {
        printLine("getFileView does work");
    } else {
        printLine("getFileView does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getFileView());
        printLine(jFileChooserOperator.getFileView());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getSelectedFile() == null &&
       jFileChooserOperator.getSelectedFile() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getSelectedFile().equals(jFileChooserOperator.getSelectedFile())) {
        printLine("getSelectedFile does work");
    } else {
        printLine("getSelectedFile does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getSelectedFile());
        printLine(jFileChooserOperator.getSelectedFile());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).getUI() == null &&
       jFileChooserOperator.getUI() == null ||
       ((JFileChooser)jFileChooserOperator.getSource()).getUI().equals(jFileChooserOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).getUI());
        printLine(jFileChooserOperator.getUI());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).isDirectorySelectionEnabled() == jFileChooserOperator.isDirectorySelectionEnabled()) {
        printLine("isDirectorySelectionEnabled does work");
    } else {
        printLine("isDirectorySelectionEnabled does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).isDirectorySelectionEnabled());
        printLine(jFileChooserOperator.isDirectorySelectionEnabled());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).isFileHidingEnabled() == jFileChooserOperator.isFileHidingEnabled()) {
        printLine("isFileHidingEnabled does work");
    } else {
        printLine("isFileHidingEnabled does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).isFileHidingEnabled());
        printLine(jFileChooserOperator.isFileHidingEnabled());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).isFileSelectionEnabled() == jFileChooserOperator.isFileSelectionEnabled()) {
        printLine("isFileSelectionEnabled does work");
    } else {
        printLine("isFileSelectionEnabled does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).isFileSelectionEnabled());
        printLine(jFileChooserOperator.isFileSelectionEnabled());
        return(false);
    }
    if(((JFileChooser)jFileChooserOperator.getSource()).isMultiSelectionEnabled() == jFileChooserOperator.isMultiSelectionEnabled()) {
        printLine("isMultiSelectionEnabled does work");
    } else {
        printLine("isMultiSelectionEnabled does not work");
        printLine(((JFileChooser)jFileChooserOperator.getSource()).isMultiSelectionEnabled());
        printLine(jFileChooserOperator.isMultiSelectionEnabled());
        return(false);
    }
    return(true);
}

    class MListener implements ActionListener, MouseListener {
        public void actionPerformed(ActionEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("actionPerformed" + e.toString());
        }
        public void mouseClicked(MouseEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("mouseClicked" + e.toString());
        }
        public void mouseEntered(MouseEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("mouseEntered" + e.toString());
        }
        public void mouseExited(MouseEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("mouseExited" + e.toString());
        }
        public void mousePressed(MouseEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("mousePressed" + e.toString());
        }
        public void mouseReleased(MouseEvent e) {
            org.netbeans.jemmy.JemmyProperties.getCurrentOutput().printLine("mouseReleased" + e.toString());
        }
    }
}
