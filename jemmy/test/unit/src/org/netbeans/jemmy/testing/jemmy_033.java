package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.io.PrintWriter;

import javax.swing.*;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.demo.Demonstrator;

public class jemmy_033 extends JemmyTest {

    JButtonOperator buttonOper;
    JLabel label;

    public int runIt(Object obj) {

	try {

	    if(getTimeouts().getTimeout("Waiter.WaitingTime") !=
	       getTimeouts().create("Waiter.WaitingTime").getValue()) {
		getOutput().printErrLine("Wrong value: " + getTimeouts().create("Waiter.WaitingTime").getValue());
		getOutput().printErrLine("Should be  : " + getTimeouts().getTimeout("Waiter.WaitingTime"));
		finalize();
		return(1);
	    }

	    (new ClassReference("org.netbeans.jemmy.testing.Application_033")).startApplication();

	    JFrameOperator winOper = new JFrameOperator("Application_033");

	    buttonOper = new JButtonOperator(winOper, "Button");

	    label = JLabelOperator.waitJLabel((Container)winOper.getSource(), "", false, false);

	    ComponentOperator buttonOper1 = ComponentOperator.createOperator(buttonOper.getSource());
	    getOutput().printErrLine("Operator: " + buttonOper1.toString());
	    if(!(buttonOper1 instanceof JButtonOperator) ||
	       buttonOper1.getSource() != buttonOper.getSource()) {
		getOutput().printErrLine("- wrong operator!");
		finalize();
		return(1);
	    }

	    ComponentOperator.addOperatorPackage("org.netbeans.jemmy.testing");

	    ComponentOperator buttonOper2 = ComponentOperator.createOperator(buttonOper.getSource());
	    getOutput().printErrLine("Operator: " + buttonOper2.toString());
	    if(!(buttonOper2 instanceof MyButtonOperator) ||
	       buttonOper2.getSource() != buttonOper.getSource()) {
		getOutput().printErrLine("- wrong operator!");
		finalize();
		return(1);
	    }

	    QueueTool qt = new QueueTool();

	    qt.lock();

	    qt.invoke(new Pusher(), "one");

	    getTimeouts().createDelta().sleep();

	    JLabel lbl;
	    if((lbl = JLabelOperator.findJLabel((Container)winOper.getSource(), "one", false, false)) != null) {
		getOutput().printErrLine("Label found: " + lbl.toString());
		finalize();
		return(1);
	    }

	    qt.unlock();
	    
	    if((lbl = JLabelOperator.waitJLabel((Container)winOper.getSource(), "one", false, false)) == null) {
		getOutput().printErrLine("Label was not find!");
		finalize();
		return(1);
	    }

	    qt.invoke(new Pusher(), "two");

	    if((lbl = JLabelOperator.waitJLabel((Container)winOper.getSource(), "two", false, false)) == null) {
		getOutput().printErrLine("Label was not find!");
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

    private class Pusher implements org.netbeans.jemmy.Action {
	public Pusher() {
	}
	public Object launch(Object param) {
	    getTimeouts().createDelta().sleep();
	    label.setText((String)param);
	    return(null);
	}
	public String getDescription() {
	    return("");
	}
    }
}
