package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_011 extends JemmyTest {

    JCheckBoxOperator boxOper;
    JRadioButtonOperator radioOper;
    JRadioButtonOperator radio1Oper;


    public int runIt(Object obj) {

	try {
	
	    (new ClassReference("org.netbeans.jemmy.testing.Application_011")).startApplication();

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm0 = JFrameOperator.waitJFrame("Application_011", false, true);
	    JFrameOperator frmo = new JFrameOperator(frm0);

	    String[][] btnattr = {{"getClass", "JButton"}};

	    //try to find by StringPropChooser

	    JButton button = JButtonOperator.findJButton(frm0, new StringPropChooser("getClass=JButton;getText=JButton",
										     false, true));

	    JLabel label = JLabelOperator.findJLabel(frm0, new StringPropChooser("getText=JLabel",
										 false, true));

	    String[] fields = {"getClientProperty"};
	    Object[][] params = {{"classname"}};
	    Class[][] classes = {{new Object().getClass()}};
	    Object[] results = {"JCheckBox"};

	    JCheckBox box = JCheckBoxOperator.findJCheckBox(frm0, new PropChooser(fields, params, classes, results));

	    JCheckBoxOperator bo0 = new JCheckBoxOperator(box);
	    JCheckBoxOperator bo1 = new JCheckBoxOperator(frmo);
	    JCheckBoxOperator bo2 = new JCheckBoxOperator(frmo, "JCheckBox");
	    if(bo1.getSource() != bo0.getSource() ||
	       bo2.getSource() != bo0.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(bo0.getSource().toString());
		getOutput().printErrLine(bo1.getSource().toString());
		getOutput().printErrLine(bo2.getSource().toString());
		finalize();
		return(1);
	    }

	    JRadioButton radioButton = JRadioButtonOperator.findJRadioButton(frm0, new StringPropChooser("getClientProperty=JRadioButton",
													 params, classes, false, true));

	    JRadioButton radioButton1 = JRadioButtonOperator.findJRadioButton(frm0, new StringPropChooser("getClientProperty=JRadioButton1",
													 params, classes, false, true));

	    JRadioButtonOperator rb0 = new JRadioButtonOperator(radioButton1);
	    JRadioButtonOperator rb1 = new JRadioButtonOperator(frmo, 1);
	    JRadioButtonOperator rb2 = new JRadioButtonOperator(frmo, "JRadioButton", 1);
	    if(rb1.getSource() != rb0.getSource() ||
	       rb2.getSource() != rb0.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(rb0.getSource().toString());
		getOutput().printErrLine(rb1.getSource().toString());
		getOutput().printErrLine(rb2.getSource().toString());
		finalize();
		return(1);
	    }

	    //check all
	    if(button != JButtonOperator.findJButton(frm0, null, true, true) ||
	       label != JLabelOperator.findJLabel(frm0,  null, true, true) ||
	       box != JCheckBoxOperator.findJCheckBox(frm0, null, true, true) ||
	       radioButton != JRadioButtonOperator.findJRadioButton(frm0, null, true, true)) {
		finalize();
		return(1);
	    }

	    boxOper = new JCheckBoxOperator(box);
	    radioOper = new JRadioButtonOperator(radioButton);
	    radio1Oper = new JRadioButtonOperator(radioButton1);

	    if(boxOper.isSelected() ||
	       !radioOper.isSelected() ||
	       radio1Oper.isSelected()) {
		getOutput().printErrLine("Wrong buttons are selected");
	    }

	    if(new ActionProducer(new org.netbeans.jemmy.Action() {
		public Object launch(Object obj) {
		    try {
			boxOper.requestFocus();
			if(!boxOper.hasFocus()) {
			    getOutput().printErrLine("Check box does not have focus");
			    return(null);
			}
			Thread.currentThread().sleep(3000);
			boxOper.push();
			radio1Oper.push();
		    } catch (InterruptedException e) {
			getOutput().printStackTrace(e);
			return(null);
		    } catch (TimeoutExpiredException e) {
			getOutput().printStackTrace(e);
			return(null);
		    }
		    return("");
		}
		public String getDescription() {
		    return("");
		}
	    }, true).produceAction(null) == null) {
		finalize();
		return(1);
	    }
	    
	    radio1Oper.waitHasFocus();

	    if(!boxOper.isSelected() ||
	       radioOper.isSelected() ||
	       !radio1Oper.isSelected()) {
		getOutput().printErrLine("Wrong buttons are selected");
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
}
