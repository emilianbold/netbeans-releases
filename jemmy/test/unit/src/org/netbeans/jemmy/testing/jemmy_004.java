package org.netbeans.jemmy.testing;

import javax.swing.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_004 extends JemmyTest {
    
    public int runIt(Object obj) {

	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_004")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm =JFrameOperator.waitJFrame("Application_004", true, true);

	    if(frm != null) {
		finalize();
		return(0);
	    } else {
		finalize();
		return(1);
	    }
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }
    
}
