package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.demo.Demonstrator;

import java.lang.reflect.InvocationTargetException;

public class jemmy_020 extends JemmyTest {

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_020")).startApplication();

	    String allChars = " !\"#$%&'()*,-./0123456789:;<>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm =JFrameOperator.waitJFrame("Application_020", true, true);

	    JTextAreaOperator to = new JTextAreaOperator(JTextAreaOperator.findJTextArea(frm, null, false, false));

	    Demonstrator.setTitle("jemmy_020 test");

	    Demonstrator.nextStep("Type all possible chars:\n" + allChars);

	    to.typeText(allChars);

	    if(!to.getText().equals(allChars)) {
		getOutput().printErrLine("Wrong text typed: " + to.getText());
		getOutput().printErrLine("Expected        : " + allChars);
		return(1);
	    }

	    Demonstrator.nextStep("Set text to empty string");

	    to.setText("");

	    if(!to.getText().equals("")) {
		getOutput().printErrLine("Wrong text typed: " + to.getText());
		getOutput().printErrLine("Expected        : " + "");
		return(1);
	    }

	    Demonstrator.nextStep("Set text to all possible chars:\n" + allChars);

	    to.setText(allChars);

	    if(!to.getText().equals(allChars)) {
		getOutput().printErrLine("Wrong text typed: " + to.getText());
		getOutput().printErrLine("Expected        : " + allChars);
		return(1);
	    }

	    Demonstrator.nextStep("Clear text by select clearing mode");

	    to.setClearingMode(to.SELECT_AND_DELETE_CLEARING_MODE);
	    to.clearText();

	    Demonstrator.showFinalComment("Test passed");

	    finalize();
	    return(0);
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }
}
