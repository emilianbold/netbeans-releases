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

public class jemmy_022 extends JemmyTest {

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_022")).startApplication();

	    JFrame frame = JFrameOperator.findJFrame("Application_022", false, true);

	    JInternalFrameOperator frame1Oper = 
		new JInternalFrameOperator(new JFrameOperator(frame), "Frame 1");

	    JInternalFrameOperator frame2Oper = 
		new JInternalFrameOperator(new JFrameOperator(frame), "Frame 2");

	    JInternalFrameOperator fo = new JInternalFrameOperator(new JFrameOperator(frame));
	    if(fo.getSource() != frame1Oper.getSource() &&
	       fo.getSource() != frame2Oper.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(frame1Oper.getSource().toString());
		getOutput().printErrLine(frame2Oper.getSource().toString());
		getOutput().printErrLine(fo.getSource().toString());
		finalize();
		return(1);
	    }

	    Demonstrator.setTitle("jemmy_022 test");

	    Demonstrator.nextStep("Deminimize \"Frame 1\"");

	    frame1Oper.deiconify();

	    Demonstrator.nextStep("Push \"Button 2\"");

	    new JButtonOperator(new ContainerOperator(frame2Oper.getRootPane())).push();

	    Demonstrator.nextStep("Push \"Button 1\"");

	    new JButtonOperator(new ContainerOperator(frame1Oper.getRootPane())).push();

	    Demonstrator.nextStep("Minimize \"Frame 1\"");

	    frame1Oper.iconify();

	    Demonstrator.nextStep("Deminimize \"Frame 1\"");

	    frame1Oper.deiconify();

	    Demonstrator.nextStep("Maximize \"Frame 1\"");

	    frame1Oper.maximize();

	    Demonstrator.nextStep("Demaximize \"Frame 1\"");

	    frame1Oper.demaximize();

	    Demonstrator.nextStep("Move  \"Frame 1\" to (100, 100)");

	    frame1Oper.move(100, 100);

	    Demonstrator.nextStep("Resize  \"Frame 1\" to (150, 150)");

	    frame1Oper.resize(150, 150);

	    Demonstrator.nextStep("Minimize \"Frame 2\"");

	    frame2Oper.iconify();

	    Demonstrator.nextStep("Deminimize \"Frame 2\"");

	    frame2Oper.deiconify();

	    Demonstrator.nextStep("Maximize \"Frame 2\"");

	    frame2Oper.maximize();

	    Demonstrator.nextStep("Demaximize \"Frame 2\"");

	    frame2Oper.demaximize();

	    Demonstrator.nextStep("Move  \"Frame 2\" to (100, 100)");

	    frame2Oper.move(100, 100);

	    Demonstrator.nextStep("Resize  \"Frame 2\" to (250, 250)");

	    frame2Oper.resize(250, 250);

	    Demonstrator.showFinalComment("Test passed");


	    if(!testJInternalFrame(frame2Oper)) {
		finalize();
		return(1);
	    }

	    finalize();
	    return(0);
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }

    private void checkSelectedText(JTextComponentOperator tco, String eta)
	throws Exception {
	try {
	    Waiter waiter = new Waiter(new SelectedTextChecker(tco, eta));
	    Timeouts times = JemmyProperties.getCurrentTimeouts();
	    times.setTimeout("Waiter.WaitingTime", 5000);
	    waiter.setTimeouts(times);
	    waiter.waitAction(null);
	} catch(TimeoutExpiredException e) {
	    throw(new Exception("\nWrong text selected: " + tco.getSelectedText() +"\n" +
				  "Expected           : " + eta));
	}
    }

    class SelectedTextChecker implements Waitable {
	JTextComponentOperator tco;
	String eta;
	public SelectedTextChecker(JTextComponentOperator tco, String eta) {
	    this.tco = tco;
	    this.eta = eta;
	}
	public Object actionProduced(Object obj) {
	    if(tco.getSelectedText().equals(eta)) {
		return("");
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return("Wait \"" + eta + "\" text selected");
	}
    }
public boolean testJInternalFrame(JInternalFrameOperator jInternalFrameOperator) {
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getContentPane() == null &&
       jInternalFrameOperator.getContentPane() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getContentPane().equals(jInternalFrameOperator.getContentPane())) {
        printLine("getContentPane does work");
    } else {
        printLine("getContentPane does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getContentPane());
        printLine(jInternalFrameOperator.getContentPane());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getDefaultCloseOperation() == jInternalFrameOperator.getDefaultCloseOperation()) {
        printLine("getDefaultCloseOperation does work");
    } else {
        printLine("getDefaultCloseOperation does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getDefaultCloseOperation());
        printLine(jInternalFrameOperator.getDefaultCloseOperation());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopIcon() == null &&
       jInternalFrameOperator.getDesktopIcon() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopIcon().equals(jInternalFrameOperator.getDesktopIcon())) {
        printLine("getDesktopIcon does work");
    } else {
        printLine("getDesktopIcon does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopIcon());
        printLine(jInternalFrameOperator.getDesktopIcon());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopPane() == null &&
       jInternalFrameOperator.getDesktopPane() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopPane().equals(jInternalFrameOperator.getDesktopPane())) {
        printLine("getDesktopPane does work");
    } else {
        printLine("getDesktopPane does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getDesktopPane());
        printLine(jInternalFrameOperator.getDesktopPane());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getFrameIcon() == null &&
       jInternalFrameOperator.getFrameIcon() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getFrameIcon().equals(jInternalFrameOperator.getFrameIcon())) {
        printLine("getFrameIcon does work");
    } else {
        printLine("getFrameIcon does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getFrameIcon());
        printLine(jInternalFrameOperator.getFrameIcon());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getGlassPane() == null &&
       jInternalFrameOperator.getGlassPane() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getGlassPane().equals(jInternalFrameOperator.getGlassPane())) {
        printLine("getGlassPane does work");
    } else {
        printLine("getGlassPane does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getGlassPane());
        printLine(jInternalFrameOperator.getGlassPane());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getJMenuBar() == null &&
       jInternalFrameOperator.getJMenuBar() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getJMenuBar().equals(jInternalFrameOperator.getJMenuBar())) {
        printLine("getJMenuBar does work");
    } else {
        printLine("getJMenuBar does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getJMenuBar());
        printLine(jInternalFrameOperator.getJMenuBar());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getLayer() == jInternalFrameOperator.getLayer()) {
        printLine("getLayer does work");
    } else {
        printLine("getLayer does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getLayer());
        printLine(jInternalFrameOperator.getLayer());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getLayeredPane() == null &&
       jInternalFrameOperator.getLayeredPane() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getLayeredPane().equals(jInternalFrameOperator.getLayeredPane())) {
        printLine("getLayeredPane does work");
    } else {
        printLine("getLayeredPane does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getLayeredPane());
        printLine(jInternalFrameOperator.getLayeredPane());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getTitle() == null &&
       jInternalFrameOperator.getTitle() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getTitle().equals(jInternalFrameOperator.getTitle())) {
        printLine("getTitle does work");
    } else {
        printLine("getTitle does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getTitle());
        printLine(jInternalFrameOperator.getTitle());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getUI() == null &&
       jInternalFrameOperator.getUI() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getUI().equals(jInternalFrameOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getUI());
        printLine(jInternalFrameOperator.getUI());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).getWarningString() == null &&
       jInternalFrameOperator.getWarningString() == null ||
       ((JInternalFrame)jInternalFrameOperator.getSource()).getWarningString().equals(jInternalFrameOperator.getWarningString())) {
        printLine("getWarningString does work");
    } else {
        printLine("getWarningString does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).getWarningString());
        printLine(jInternalFrameOperator.getWarningString());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isClosable() == jInternalFrameOperator.isClosable()) {
        printLine("isClosable does work");
    } else {
        printLine("isClosable does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isClosable());
        printLine(jInternalFrameOperator.isClosable());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isClosed() == jInternalFrameOperator.isClosed()) {
        printLine("isClosed does work");
    } else {
        printLine("isClosed does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isClosed());
        printLine(jInternalFrameOperator.isClosed());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isIcon() == jInternalFrameOperator.isIcon()) {
        printLine("isIcon does work");
    } else {
        printLine("isIcon does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isIcon());
        printLine(jInternalFrameOperator.isIcon());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isIconifiable() == jInternalFrameOperator.isIconifiable()) {
        printLine("isIconifiable does work");
    } else {
        printLine("isIconifiable does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isIconifiable());
        printLine(jInternalFrameOperator.isIconifiable());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isMaximizable() == jInternalFrameOperator.isMaximizable()) {
        printLine("isMaximizable does work");
    } else {
        printLine("isMaximizable does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isMaximizable());
        printLine(jInternalFrameOperator.isMaximizable());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isMaximum() == jInternalFrameOperator.isMaximum()) {
        printLine("isMaximum does work");
    } else {
        printLine("isMaximum does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isMaximum());
        printLine(jInternalFrameOperator.isMaximum());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isResizable() == jInternalFrameOperator.isResizable()) {
        printLine("isResizable does work");
    } else {
        printLine("isResizable does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isResizable());
        printLine(jInternalFrameOperator.isResizable());
        return(false);
    }
    if(((JInternalFrame)jInternalFrameOperator.getSource()).isSelected() == jInternalFrameOperator.isSelected()) {
        printLine("isSelected does work");
    } else {
        printLine("isSelected does not work");
        printLine(((JInternalFrame)jInternalFrameOperator.getSource()).isSelected());
        printLine(jInternalFrameOperator.isSelected());
        return(false);
    }
    return(true);
}

}
