package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.drivers.*;
import org.netbeans.jemmy.drivers.trees.*;

import org.netbeans.jemmy.demo.Demonstrator;

import java.lang.reflect.InvocationTargetException;

public class jemmy_041 extends JemmyTest {

    JTree tree;
    JTreeOperator to;
    
    public int runIt(Object obj) {
	
	try {

	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_041")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    new QueueTool().waitEmpty(100);

            JFrameOperator foper = new JFrameOperator("41");
            foper.maximize();

            JButtonOperator start = new JButtonOperator(foper);

            JTreeOperator tree = new JTreeOperator(foper);

            start.push();

            for(int i = 0; i < 10; i++) {
                doSleep(30);
                tree.selectPath(tree.findPath("node" + i, "|"));
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
