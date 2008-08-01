package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_009 extends JemmyTest {

    public int runIt(Object obj) {
	
	try {
	    Exception e = (Exception)(new ActionProducer(new org.netbeans.jemmy.Action() {
		    public Object launch(Object obj) {
			try {
			    (new ClassReference("org.netbeans.jemmy.testing.Application_009")).startApplication();
			} catch(Exception ex) {
			    return(ex);
			}
			return(null);
		    }
		    public String getDescription() {
			return("");
		    }
		}, false).produceAction(null));

	    if(e != null) {
		throw(e);
	    }

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm0 = JFrameOperator.waitJFrame("Application_009", false, true);

	    if(((Application_009)frm0).getIndex() != 0) {
		finalize();
		return(1);
	    }
	    
	    JFrame frm1 = JFrameOperator.waitJFrame("Application_009", false, true, 1);

	    if(((Application_009)frm1).getIndex() != 1) {
		finalize();
		return(1);
	    }
	    
	    JFrame frm2 = JFrameOperator.waitJFrame("Application_009", false, true, 2);

	    if(((Application_009)frm2).getIndex() != 2) {
		finalize();
		return(1);
	    }

	    final JFrameOperator frm2o = new JFrameOperator(frm2);
	    new Thread(new Runnable() {
		    public void run() {
			try {
			    Thread.sleep(1000);
			    frm2o.setTitle("New Title");
			} catch(InterruptedException e) {
			}
		    }
		}).start();
	    frm2o.waitTitle("New Title");

	    finalize();
	    return(0);
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }
}
