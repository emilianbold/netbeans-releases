package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.accessibility.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.util.*;

import java.awt.*;

import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.tree.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_029 extends JemmyTest {

    JFrame win;

    public int runIt(Object obj) {

	try {

	    ((DefaultVisualizer)ComponentOperator.
	     getDefaultComponentVisualizer()).checkForModal(true);

	    (new ClassReference("org.netbeans.jemmy.testing.Application_029")).startApplication();

	    win =JFrameOperator.waitJFrame("Right one", true, true);
            JFrameOperator wino = new JFrameOperator(win);

	    if(JDialogOperator.getTopModalDialog() != null) {
		output.printLine(JDialogOperator.getTopModalDialog().toString());
		finalize();
		return(1);
	    }
	    
	    JButtonOperator bttOper = 
		new JButtonOperator(JButtonOperator.waitJButton(win, "Button", true, true));
	    
	    bttOper.push();
	    
	    JLabelOperator.waitJLabel(win, ComponentSearcher.getTrueChooser(""));

	    new JButtonOperator(new ContainerOperator(win), "Show").pushNoBlock();	    
	    
	    JDialog d = JDialogOperator.waitJDialog("Modal dialog", true, true);
            JDialog d1 = (JDialog)wino.findSubWindow(new DialogOperator.
                                                     DialogByTitleFinder("Modal dialog"));
            JDialog d2 = (JDialog)wino.waitSubWindow(new AccessibleNameChooser("Modal dialog"));
	    JDialogOperator do1 = new JDialogOperator("Modal dialog");
	    JDialogOperator do2 = new JDialogOperator(wino, "Modal dialog");
            JDialogOperator do3 = new JDialogOperator(new AccessibleNameChooser("Modal dialog"));
	    getOutput().printLine("By find    : " +               d.toString());
	    getOutput().printLine("By subfind : " +              d1.toString());
	    getOutput().printLine("By subwait : " +              d2.toString());
	    getOutput().printLine("As single  : " + do1.getSource().toString());
	    getOutput().printLine("As child   : " + do2.getSource().toString());
	    getOutput().printLine("by a11y    : " + do2.getSource().toString());
	    if(d != d1 ||
               d != d2 ||
               d != do1.getSource() ||
               d != do2.getSource()) {
		getOutput().printErrLine("Should be the same!");
		finalize();
		return(1);
	    }

	    if(JDialogOperator.getTopModalDialog() == null) {
		finalize();
		return(1);
	    }

	    JemmyInputException ex = null;
	    try {
		bttOper.push();
	    } catch(JemmyInputException e) {
		ex = e;
	    }

	    if(ex == null) {
		finalize();
		return(1);
	    }

	    if(JLabelOperator.findJLabel(win, ComponentSearcher.getTrueChooser(""), 1) != null) {
		finalize();
		return(1);
	    }

	    new JButtonOperator(do1, "").push();

	    JMenuBarOperator mbo = new JMenuBarOperator(new ContainerOperator(win));
	    mbo.pushMenuNoBlock("Menu|MenuItem", "|", false, false);

	    Thread.sleep(100);

	    JDialogOperator modal = new JDialogOperator("Modal");
	    new JButtonOperator(modal, "").push();
	    modal.waitClosed();

	    Thread.sleep(100);

	    mbo.pushMenuNoBlock("Menu|MenuItem", "|");

	    modal = new JDialogOperator("Modal");
	    new JButtonOperator(modal, "").push();
	    modal.waitClosed();

	    Thread.sleep(100);

	    mbo.pushMenuNoBlock(new String[] {"Menu", "MenuItem"}, false, false);

	    modal = new JDialogOperator("Modal");
	    new JButtonOperator(modal, "").push();
	    modal.waitClosed();

	    Thread.sleep(100);

	    mbo.pushMenuNoBlock(new String[] {"Menu", "MenuItem"});

	    modal = new JDialogOperator("Modal");
	    new JButtonOperator(modal, "").push();
	    modal.waitClosed();

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
