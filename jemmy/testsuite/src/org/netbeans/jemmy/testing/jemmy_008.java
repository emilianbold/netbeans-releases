package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

public class jemmy_008 implements Scenario {

    public int runIt(Object obj) {

	Timeouts oldTimeouts = JemmyProperties.getCurrentTimeouts();
	
	try {
	
	    Timeouts times1 = new Timeouts();
	    Timeouts times2 = new Timeouts();

	    //setCurrentTimeouts/getCurrentTimeouts
	    JemmyProperties.setCurrentTimeouts(times1);
	    if(JemmyProperties.getCurrentTimeouts() != times1) {
		throw(new TestCompletedException(1, ""));
	    }
	    if(JemmyProperties.setCurrentTimeouts(times2) != times1) {
		throw(new TestCompletedException(1, ""));
	    }

	    //setCurrentTimeout/getCurrentTimeout
	    if(JemmyProperties.setCurrentTimeout("timeout1", 1) != -1) {
		throw(new TestCompletedException(1, ""));
	    }
	    if(JemmyProperties.getCurrentTimeout("timeout1") != 1) {
		throw(new TestCompletedException(1, ""));
	    }
	    if(JemmyProperties.setCurrentTimeout("timeout1", 2) != 1) {
		throw(new TestCompletedException(1, ""));
	    }

	    //initCurrentTimeout
	    if(JemmyProperties.initCurrentTimeout("timeout2", 1) != -1) {
		throw(new TestCompletedException(1, ""));
	    }
	    if(JemmyProperties.initCurrentTimeout("timeout2", 2) != 1) {
		throw(new TestCompletedException(1, ""));
	    }
	    if(JemmyProperties.getCurrentTimeout("timeout2") != 1) {
		throw(new TestCompletedException(1, ""));
	    }

	    JemmyProperties.setCurrentTimeouts(oldTimeouts);
	    return(0);
	} catch(Exception e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    JemmyProperties.setCurrentTimeouts(oldTimeouts);
	    return(1);
	}
    }
}
