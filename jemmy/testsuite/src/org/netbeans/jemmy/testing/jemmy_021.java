package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import javax.swing.text.*;
import java.awt.event.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.demo.Demonstrator;

import java.lang.reflect.InvocationTargetException;

public class jemmy_021 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    (new ClassReference("org.netbeans.jemmy.testing.Application_021")).startApplication();
	    
	    String allChars = "0123456789\n0123456789\n0123456789\n0123456789";
	    
	    EventDispatcher.waitQueueEmpty();
	    
	    JFrame frm =JFrameOperator.waitJFrame("Application_021", true, true);
	    JFrameOperator frmo = new JFrameOperator(frm);

	    //4420394
	    if(new JFrameOperator(frm).getContainers().length != 0) {
		getOutput().printErrLine("4420394 BUG!");
		finalize();
		return(1);
	    } else {
		getOutput().printLine("Containers were found correctly");
	    }
	    
	    JTabbedPaneOperator tp = 
		new JTabbedPaneOperator(JTabbedPaneOperator.
					findJTabbedPane(frm, null, false, false, -1));
	    
	    Demonstrator.setTitle("jemmy_021 test");

	    Demonstrator.nextStep("Play with JEditorPane");

	    tp.selectPage("JSplitPane", false, true);
	    
	    JEditorPaneOperator to = 
		new JEditorPaneOperator(JEditorPaneOperator.
					findJEditorPane(frm, null, false, false));

	    JEditorPaneOperator t1 = new JEditorPaneOperator(frmo);
	    if(t1.getSource() != to.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(to.getSource().toString());
		getOutput().printErrLine(t1.getSource().toString());
		finalize();
		return(1);
	    }

	    //4420389
	    if(JEditorPaneOperator.findJEditorPane(frm, ComponentSearcher.getTrueChooser("JEditorPane")) !=
	       to.getSource()) {
		getOutput().printErrLine("4420389 BUG!");
		finalize();
		return(1);
	    } else {
		getOutput().printLine("Editor was found correctly");
	    }	   
	    
	    Demonstrator.nextStep("Type text:\n" + allChars);
	    
	    to.typeText(allChars);
	    
	    if(!to.getText().equals(allChars)) {
		getOutput().printErrLine("Wrong text typed: " + to.getText());
		getOutput().printErrLine("Expected        : " + allChars);
		finalize();
		return(1);
	    }
	    
	    Demonstrator.nextStep("Select first twenty simbols\n" + 
				  "Note: caret return is a simbol");
	    
	    to.selectText(0, 20);
	    checkSelectedText(to, "0123456789\n012345678");

	    Demonstrator.nextStep("Select last twenty simbols\n" + 
				  "Note: caret return is a simbol");

	    to.selectText(allChars.length(), allChars.length() - 20);
	    checkSelectedText(to, "123456789\n0123456789");
	    
	    Demonstrator.nextStep("Select \"234\" text");
	    
	    to.selectText("234");
	    checkSelectedText(to, "234");
	    
	    Demonstrator.nextStep("Select fourth \"567\" text");
	    
	    to.selectText("567", 3);
	    checkSelectedText(to, "567");
	    
	    Demonstrator.nextStep("Replace some text");
	    
	    to.selectText(3, 37);
	    
	    to.typeText("3");
	    
	    if(JEditorPaneOperator.waitJEditorPane(frm, "0123456789", true, true) == null) {
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Clear text");

	    to.setClearingMode(to.DELETE_CLEARING_MODE);
	    
	    to.clearText();

	    if(JEditorPaneOperator.waitJEditorPane(frm, "", true, true) == null) {
		finalize();
		return(1);
	    }

	    if(!testJEditorPane(to)) {
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Play with JTextArea");
	    
	    tp.selectPage("JTextArea", false, true);
	    
	    JTextAreaOperator tao = new JTextAreaOperator(JTextAreaOperator.findJTextArea(frm, null, false, false));
	    
	    Demonstrator.nextStep("Type text:\n" + allChars);

	    tao.typeText(allChars);

	    if(!tao.getText().equals(allChars)) {
		getOutput().printErrLine("Wrong text typed: " + tao.getText());
		getOutput().printErrLine("Expected        : " + allChars);
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Select first twenty simbols\n" + 
				  "Note: caret return is a simbol");

	    tao.selectText(0, 20);
	    checkSelectedText(tao, "0123456789\n012345678");

	    Demonstrator.nextStep("Select last twenty simbols\n" + 
				  "Note: caret return is a simbol");

	    tao.selectText(allChars.length(), allChars.length() - 20);
	    checkSelectedText(tao, "123456789\n0123456789");

	    Demonstrator.nextStep("Select \"234\" text");

	    tao.selectText("234");
	    checkSelectedText(tao, "234");

	    Demonstrator.nextStep("Select fourth \"567\" text");
	    
	    tao.selectText("567", 3);
	    checkSelectedText(tao, "567");

	    Demonstrator.nextStep("Select second and third lines");
	    
	    tao.selectLines(1, 2);
	    checkSelectedText(tao, "0123456789\n0123456789\n");

	    Demonstrator.nextStep("Replace some text");
	    
	    tao.selectText(0, 3, 3, 4);

	    tao.typeText("3");
	    
	    if(JTextAreaOperator.waitJTextArea(frm, "0123456789", true, true) == null) {
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Clear text");

	    tao.setClearingMode(tao.BACK_SPACE_CLEARING_MODE);
	    
	    tao.clearText();

	    if(JTextAreaOperator.waitJTextArea(frm, "", true, true) == null) {
		finalize();
		return(1);
	    }

	    Demonstrator.showFinalComment("Test passed");

	    if(!testJTabbedPane(tp)) {
		finalize();
		return(1);
	    }

	    if(!testJTextArea(tao)) {
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
	    if(tco.getSelectedText() != null &&
	       tco.getSelectedText().equals(eta)) {
		return("");
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return("Wait \"" + eta + "\" text selected");
	}
    }

public boolean testJEditorPane(JEditorPaneOperator jEditorPaneOperator) {
    if(((JEditorPane)jEditorPaneOperator.getSource()).getContentType() == null &&
       jEditorPaneOperator.getContentType() == null ||
       ((JEditorPane)jEditorPaneOperator.getSource()).getContentType().equals(jEditorPaneOperator.getContentType())) {
        printLine("getContentType does work");
    } else {
        printLine("getContentType does not work");
        printLine(((JEditorPane)jEditorPaneOperator.getSource()).getContentType());
        printLine(jEditorPaneOperator.getContentType());
        return(false);
    }
    if(((JEditorPane)jEditorPaneOperator.getSource()).getEditorKit() == null &&
       jEditorPaneOperator.getEditorKit() == null ||
       ((JEditorPane)jEditorPaneOperator.getSource()).getEditorKit().equals(jEditorPaneOperator.getEditorKit())) {
        printLine("getEditorKit does work");
    } else {
        printLine("getEditorKit does not work");
        printLine(((JEditorPane)jEditorPaneOperator.getSource()).getEditorKit());
        printLine(jEditorPaneOperator.getEditorKit());
        return(false);
    }
    if(((JEditorPane)jEditorPaneOperator.getSource()).getPage() == null &&
       jEditorPaneOperator.getPage() == null ||
       ((JEditorPane)jEditorPaneOperator.getSource()).getPage().equals(jEditorPaneOperator.getPage())) {
        printLine("getPage does work");
    } else {
        printLine("getPage does not work");
        printLine(((JEditorPane)jEditorPaneOperator.getSource()).getPage());
        printLine(jEditorPaneOperator.getPage());
        return(false);
    }
    return(true);
}

public boolean testJTabbedPane(JTabbedPaneOperator jTabbedPaneOperator) {
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getModel() == null &&
       jTabbedPaneOperator.getModel() == null ||
       ((JTabbedPane)jTabbedPaneOperator.getSource()).getModel().equals(jTabbedPaneOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getModel());
        printLine(jTabbedPaneOperator.getModel());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getSelectedComponent() == null &&
       jTabbedPaneOperator.getSelectedComponent() == null ||
       ((JTabbedPane)jTabbedPaneOperator.getSource()).getSelectedComponent().equals(jTabbedPaneOperator.getSelectedComponent())) {
        printLine("getSelectedComponent does work");
    } else {
        printLine("getSelectedComponent does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getSelectedComponent());
        printLine(jTabbedPaneOperator.getSelectedComponent());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getSelectedIndex() == jTabbedPaneOperator.getSelectedIndex()) {
        printLine("getSelectedIndex does work");
    } else {
        printLine("getSelectedIndex does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getSelectedIndex());
        printLine(jTabbedPaneOperator.getSelectedIndex());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabCount() == jTabbedPaneOperator.getTabCount()) {
        printLine("getTabCount does work");
    } else {
        printLine("getTabCount does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabCount());
        printLine(jTabbedPaneOperator.getTabCount());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabPlacement() == jTabbedPaneOperator.getTabPlacement()) {
        printLine("getTabPlacement does work");
    } else {
        printLine("getTabPlacement does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabPlacement());
        printLine(jTabbedPaneOperator.getTabPlacement());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabRunCount() == jTabbedPaneOperator.getTabRunCount()) {
        printLine("getTabRunCount does work");
    } else {
        printLine("getTabRunCount does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getTabRunCount());
        printLine(jTabbedPaneOperator.getTabRunCount());
        return(false);
    }
    if(((JTabbedPane)jTabbedPaneOperator.getSource()).getUI() == null &&
       jTabbedPaneOperator.getUI() == null ||
       ((JTabbedPane)jTabbedPaneOperator.getSource()).getUI().equals(jTabbedPaneOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JTabbedPane)jTabbedPaneOperator.getSource()).getUI());
        printLine(jTabbedPaneOperator.getUI());
        return(false);
    }
    return(true);
}

public boolean testJTextArea(JTextAreaOperator jTextAreaOperator) {
    if(((JTextArea)jTextAreaOperator.getSource()).getColumns() == jTextAreaOperator.getColumns()) {
        printLine("getColumns does work");
    } else {
        printLine("getColumns does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getColumns());
        printLine(jTextAreaOperator.getColumns());
        return(false);
    }
    if(((JTextArea)jTextAreaOperator.getSource()).getLineCount() == jTextAreaOperator.getLineCount()) {
        printLine("getLineCount does work");
    } else {
        printLine("getLineCount does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getLineCount());
        printLine(jTextAreaOperator.getLineCount());
        return(false);
    }
    if(((JTextArea)jTextAreaOperator.getSource()).getLineWrap() == jTextAreaOperator.getLineWrap()) {
        printLine("getLineWrap does work");
    } else {
        printLine("getLineWrap does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getLineWrap());
        printLine(jTextAreaOperator.getLineWrap());
        return(false);
    }
    if(((JTextArea)jTextAreaOperator.getSource()).getRows() == jTextAreaOperator.getRows()) {
        printLine("getRows does work");
    } else {
        printLine("getRows does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getRows());
        printLine(jTextAreaOperator.getRows());
        return(false);
    }
    if(((JTextArea)jTextAreaOperator.getSource()).getTabSize() == jTextAreaOperator.getTabSize()) {
        printLine("getTabSize does work");
    } else {
        printLine("getTabSize does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getTabSize());
        printLine(jTextAreaOperator.getTabSize());
        return(false);
    }
    if(((JTextArea)jTextAreaOperator.getSource()).getWrapStyleWord() == jTextAreaOperator.getWrapStyleWord()) {
        printLine("getWrapStyleWord does work");
    } else {
        printLine("getWrapStyleWord does not work");
        printLine(((JTextArea)jTextAreaOperator.getSource()).getWrapStyleWord());
        printLine(jTextAreaOperator.getWrapStyleWord());
        return(false);
    }
    return(true);
}
}
