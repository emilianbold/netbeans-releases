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

public class jemmy_019 extends JemmyTest {

    public int runIt(Object obj) {

	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_019")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm =JFrameOperator.waitJFrame("Application_019", true, true);

	    JSplitPaneOperator split = new JSplitPaneOperator(JSplitPaneOperator.findJSplitPane(frm));

	    Demonstrator.setTitle("jemmy_019 test");

	    Demonstrator.nextStep("Move divider to bottom.");

	    split.moveDivider((double)1);

	    Demonstrator.nextStep("Move divider to top.");

	    split.moveDivider((double)0);

	    Demonstrator.nextStep("Move divider to center.");

	    split.moveDivider(0.5);

	    Demonstrator.nextStep("Collapce top.");

	    split.expandRight();

	    Demonstrator.nextStep("Collapse bottom.");

	    split.expandLeft();
	    split.expandLeft();

	    Demonstrator.nextStep("Restore divider position.");

	    split.expandRight();

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
