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

public class jemmy_023 extends JemmyTest {

    public int runIt(Object obj) {

	try {

	    JemmyProperties.setCurrentTimeout("JustATimeoutTimeout", 222222222);

	    if(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout") != 222222222) {
		getOutput().printLine("Properties was not set!");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
		getOutput().printLine("Expected            = " + 
				      Long.toString(222222222));
		return(1);
	    } else {
		getOutput().printLine("Properties was set.");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
	    }

	    JemmyProperties.push();

	    JemmyProperties.setCurrentTimeout("JustATimeoutTimeout", 111111111);

	    if(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout") != 111111111) {
		getOutput().printLine("Properties was not set!");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
		getOutput().printLine("Expected            = " + 
				      Long.toString(111111111));
		return(1);
	    } else {
		getOutput().printLine("Properties was set.");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
	    }

	    Timeouts.setDefault("DefaultTimeoutTimeout", 111111111);

	    if(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout") != 111111111) {
		getOutput().printLine("Properties was not set!");
		getOutput().printLine("DefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout")));
		getOutput().printLine("Expected              = " + 
				      Long.toString(111111111));
		return(1);
	    } else {
		getOutput().printLine("Properties was set.");
		getOutput().printLine("DefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout")));
	    }

	    JemmyProperties.setCurrentTimeout("NonDefaultTimeoutTimeout", 111111111);

	    if(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout") != 111111111) {
		getOutput().printLine("Properties was not set!");
		getOutput().printLine("NonDefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout")));
		getOutput().printLine("Expected                 = " + 
				      Long.toString(111111111));
		return(1);
	    } else {
		getOutput().printLine("Properties was set.");
		getOutput().printLine("NonDefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout")));
	    }

	    JemmyProperties.pop();

	    if(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout") != 222222222) {
		getOutput().printLine("Properties was not restored!");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
		getOutput().printLine("Expected            = " + 
				      Long.toString(222222222));
		return(1);
	    } else {
		getOutput().printLine("Properties was restored.");
		getOutput().printLine("JustATimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("JustATimeoutTimeout")));
	    }

	    if(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout") != -1) {
		getOutput().printLine("Properties was not restored!");
		getOutput().printLine("NonDefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout")));
		getOutput().printLine("Expected                 = " + 
				      Long.toString(-1));
		return(1);
	    } else {
		getOutput().printLine("Properties was restored.");
		getOutput().printLine("NonDefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("NonDefaultTimeoutTimeout")));
	    }

	    if(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout") != 111111111) {
		getOutput().printLine("Properties was not restored!");
		getOutput().printLine("DefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout")));
		getOutput().printLine("Expected              = " + 
				      Long.toString(111111111));
		return(1);
	    } else {
		getOutput().printLine("Properties was restored.");
		getOutput().printLine("DefaultTimeoutTimeout = " + 
				      Long.toString(JemmyProperties.getCurrentTimeout("DefaultTimeoutTimeout")));
	    }

	    return(0);
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }

}
