package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.JListOperator.NoSuchItemException;

import java.io.PrintWriter;

import javax.swing.JList;
import javax.swing.JFrame;
import javax.swing.JButton;

import java.lang.reflect.InvocationTargetException;

public class jemmy_016 extends JemmyTest {
    public int runIt(Object obj) {

	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_016")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    JFrame win =JFrameOperator.waitJFrame("APPLICATION", false, false);

	    JTabbedPaneOperator tpo = new JTabbedPaneOperator(new JFrameOperator(win), "Page1");

	    Demonstrator.setTitle("jemmy_016 test");
	    Demonstrator.nextStep("Check that button on the \"Page1\" page can be found and\n" +
				  "button on the \"Page2\" page can't");

	    if(JButtonOperator.findJButton(win, "BUTTON1", true, false) == null ||
	       JButtonOperator.findJButton(win, "button2", true, true)  != null) {
		output.printErrLine("BUTTON1 was not found or button2 was");
		finalize();
		return(1);
	    }

	    JButton btt1 = JButtonOperator.findJButton(win, "BUTTON1", true, false);
	    JButtonOperator btt1o = new JButtonOperator(btt1);
	    if(!btt1o.isVisible() || !btt1o.isShowing()) {
		output.printErrLine("either isVisible or isShowing did not return positive values");
		finalize();
		return(1);
	    } else {
		output.printErrLine("both isVisible and isShowing did return positive values");
	    }

	    btt1.setVisible(false);
	    if(btt1o.isVisible()) {
		output.printErrLine("isVisible did not return negative value");
		finalize();
		return(1);
	    } else {
		output.printErrLine("isVisible did return negative value");
	    }
	    btt1.setVisible(true);

	    Demonstrator.nextStep("Check that unexisting page \"Page3\" cannot be selected");

	    if(tpo.selectPage("Page3", true, true) != null) {
		output.printErrLine("Page3 was selected");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Select existing page \"Page2\"");

	    if(tpo.selectPage("Page2", true, true) == null) {
		output.printErrLine("Page2 was not selected");
		finalize();
		return(1);
	    }

	    if(!btt1o.isVisible()) {
		output.printErrLine("isVisible did not return positive value");
		finalize();
		return(1);
	    } else {
		output.printErrLine("isVisible returned positive value");
	    }

	    if(btt1o.isShowing()) {
		output.printErrLine("isShowing did not return negative value");
		finalize();
		return(1);
	    } else {
		output.printErrLine("isShowing returned negative value");
	    }

	    Demonstrator.nextStep("Check that button on the \"Page2\" page can be found and\n" +
				  "button on the \"Page1\" page can't");

	    if(JButtonOperator.findJButton(win, "BUTTON2", true, false) == null ||
	       JButtonOperator.findJButton(win, "button1", true, true)  != null) {
		output.printErrLine("BUTTON2 was not found or button1 was not");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Select page \"List Page\"");
	    
	    if(tpo.selectPage("List Page", true, true) == null) {
		output.printErrLine("List Page was not selected");
		finalize();
		return(1);
	    }

	    JListOperator lo = new JListOperator(JListOperator.
						 findJList(win, null, true, true, 0));

	    Demonstrator.nextStep("Click 1 time on the second list item");

	    if(lo.clickOnItem(1, 1) == null) {
		output.printErrLine("1 was not clicked");		
		finalize();
		return(1);
	    }

	    getOutput().printLine("getSelectedIndex returned " + Integer.toString(lo.getSelectedIndex()));
	    if(lo.getSelectedIndex() != 1) {
		getOutput().printLine("Expected                   1");
		finalize();
		return(1);
	    }

	    getOutput().printLine("getSelectedValue returned " + lo.getSelectedValue().toString());
	    if(!lo.getSelectedValue().toString().equals("two")) {
		getOutput().printLine("Expected                   two");
		finalize();
		return(1);
	    }

	    if(lo.getSelectionMode() != ((JList)lo.getSource()).getSelectionMode() ||
	       lo.getSelectionModel() != ((JList)lo.getSource()).getSelectionModel()) {
		getOutput().printLine("Either getSelectionModel or getSelectionMode does not work correctly");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Check that fourth item cannot be clicked");

	    if(lo.clickOnItem(3, 1) != null) {
		output.printErrLine("3 was clicked");		
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Click 1 time on the \"two\" list item");

	    if(lo.clickOnItem("two", true, true, 1) == null) {
		output.printErrLine("3 was not clicked");		
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Check that \"four\" item cannot be clicked");

	    try {
		lo.clickOnItem("four", true, true, 1);
		output.printErrLine("four was clicked");		
		finalize();
		return(1);
	    } catch(NoSuchItemException e) {
	    }

	    Demonstrator.showFinalComment("Test passed");

	} catch(JemmyException e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
