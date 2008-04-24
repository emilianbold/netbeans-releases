package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_007 extends JemmyTest {

    public int runIt(Object obj) {
	
	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_007")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm =JFrameOperator.waitJFrame("Application_007", true, true);
	    
	    if(JTreeOperator.findJTree(frm, null, true, true, -1)       == null ||
	       JTableOperator.findJTable(frm, null, true, true, -1, -1) == null ||
	       JListOperator.findJList(frm, null, true, true, -1)       == null) {
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
