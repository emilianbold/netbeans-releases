package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.io.*;

import javax.swing.*;

import org.netbeans.jemmy.demo.*;

public class jemmy_031 extends JemmyTest {
    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_031")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_031", true, true);
	    
	    JButtonOperator dotButtonOper = new JButtonOperator(JButtonOperator.findJButton(win, "...", true, true));

	    new ActionProducer(new org.netbeans.jemmy.Action() {
		    public Object launch(Object obj) {
			try {
			    ((JButtonOperator)obj).push();
			} catch(TimeoutExpiredException e) {
			    getOutput().printStackTrace(e);
			}
			return(null);
		    }
		    public String getDescription() {
			return("");
		    }
		}, false).produceAction(dotButtonOper);

	    JFileChooserOperator chooseoper = new JFileChooserOperator();

            chooseoper.selectFileType("All Files");
            chooseoper.selectFileType("No file");
            chooseoper.selectFileType("No directory");
            chooseoper.selectFileType("Nothing");

	    JFileChooser d = JFileChooserOperator.waitJFileChooser();
	    getOutput().printLine("By find       : " +                      d.toString());
	    getOutput().printLine("By comstructor: " + chooseoper.getSource().toString());
	    if(chooseoper.getSource() != d) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    new QueueTool().waitEmpty(100);

	    File curDir = new File(System.getProperty("user.dir"));

	    if(!chooseoper.getCurrentDirectory().equals(curDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseoper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.toString());
		finalize();
		return(1);
	    }

	    File[] allFiles = curDir.listFiles();
	    File firsFile = null;
	    for(int i = 0; i < allFiles.length; i++) {
		if(!allFiles[i].isDirectory()) {
		    firsFile = allFiles[i];
		    break;
		}
	    }
	    if(firsFile != null) {
		chooseoper.selectFileType("No directory", true, true);
		new QueueTool().waitEmpty(100);
		if(!chooseoper.checkFileDisplayed(firsFile.getName(), true, true)) {
		    getOutput().printErrLine("selectFileType does not work.");
		    finalize();
		    return(1);
		}
	    }
	    Thread.sleep(100);
	    File firsDir = null;
	    for(int i = 0; i < allFiles.length; i++) {
		if(allFiles[i].isDirectory()) {
		    firsDir = allFiles[i];
		    break;
		}
	    }
	    if(firsDir != null) {
		chooseoper.selectFileType("No file", true, true);
		new QueueTool().waitEmpty(100);
		chooseoper.waitFileDisplayed(firsDir.getName());
	    }
	    Thread.sleep(100);
	    chooseoper.selectFileType("Nothing", true, true);
	    chooseoper.waitFileCount(0);
	    Thread.sleep(100);
	    chooseoper.selectFileType("All", false, false);
	    chooseoper.waitFileCount(allFiles.length);
	    Thread.sleep(100);

	    chooseoper.goUpLevel();
	    new QueueTool().waitEmpty(100);

	    if(!chooseoper.getCurrentDirectory().equals(curDir.getParentFile())) {
		getOutput().printErrLine("Wrong current directory : " + chooseoper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.getParentFile().toString());
		finalize();
		return(1);
	    }

	    chooseoper.enterSubDir(curDir.getCanonicalPath().substring(curDir.getParentFile().getCanonicalPath().length() + 1),
				   true, true);
	    new QueueTool().waitEmpty(100);

	    if(!chooseoper.getCurrentDirectory().equals(curDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseoper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.toString());
		finalize();
		return(1);
	    }

	    chooseoper.selectPathDirectory(curDir.getParentFile().getName(), true, true);
	    new QueueTool().waitEmpty(100);

	    if(!chooseoper.getCurrentDirectory().equals(curDir.getParentFile())) {
		getOutput().printErrLine("Wrong current directory : " + chooseoper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + curDir.getParentFile().toString());
		finalize();
		return(1);
	    }

	    chooseoper.goHome();
	    new QueueTool().waitEmpty(100);

	    File homeDir = new File(System.getProperty("user.home"));

	    if(!chooseoper.getCurrentDirectory().equals(homeDir)) {
		getOutput().printErrLine("Wrong current directory : " + chooseoper.getCurrentDirectory().toString());
		getOutput().printErrLine("Should be               : " + homeDir.toString());
		finalize();
		return(1);
	    }

	    String file = allFiles[0].getCanonicalPath();
	    chooseoper.chooseFile(file);
	    JTextFieldOperator.waitJTextField(win, file, true, true);

	    if(!testJFileChooser(chooseoper)) {
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

}
