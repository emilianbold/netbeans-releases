package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.io.PrintWriter;

import javax.swing.*;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.jemmy.demo.Demonstrator;

public class jemmy_034 extends JemmyTest {

    JButtonOperator buttonOper;
    JLabel label;

    public int runIt(Object obj) {

	QueueTool qt = new QueueTool();
	qt.getTimeouts().setTimeout("QueueTool.MaximumLockingTime", 2000);

	QueueTool qt2 = new QueueTool();
	qt2.getTimeouts().setTimeout("QueueTool.LockTimeout", 1000);

	qt.lock();
	doSleep(1000);
	qt.unlock();
	
	if(qt.wasLockingExpired()) {
	    getOutput().printLine("Lock was somehow expired! 8-[ ]");
	    return(1);
	}

	qt.lock();
	doSleep(2000);
	qt.unlock();
	
	if(!qt.wasLockingExpired()) {
	    getOutput().printLine("Lock was not expired! 8-[ ]");
	    return(1);
	}

	try {
	    qt.lock();
	    qt2.lock();
	    qt.unlock();
	} catch(TimeoutExpiredException e) {
	    getOutput().printLine("Next is really good! 8-)");
	    getOutput().printStackTrace(e);
	    return(0);
	}
	
	getOutput().printLine("TimeoutExpiredException was not throught! 8-[ ]");
	return(1);

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
