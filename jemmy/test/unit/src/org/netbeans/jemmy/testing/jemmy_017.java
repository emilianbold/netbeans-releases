package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.util.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import java.lang.reflect.InvocationTargetException;

import java.util.Vector;

public class jemmy_017 extends JemmyTest {

    public int runIt(Object obj) {
	
	try {

            ComponentOperator.
                setDefaultComponentVisualizer(new MouseVisualizer(MouseVisualizer.TOP, .5, 5, false));

	    JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitComponentTimeout", 20000);

	    Exception e = (Exception)(new ActionProducer(new org.netbeans.jemmy.Action() {
		    public Object launch(Object obj) {
			try {
			    (new ClassReference("org.netbeans.jemmy.testing.Application_017")).startApplication();
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

	    WindowManager.addJob(new WindowProcessor());

	    JFrame frm0 = JFrameOperator.waitJFrame("Application_017/0", false, true);

	    if(JLabelOperator.waitJLabel(frm0, "has been processed", true, true) == null) {
		getOutput().printErrLine("0 frame was not processed");
		finalize();
		return(1);
	    }

	    JFrame frm1 = JFrameOperator.waitJFrame("Application_017/1", false, true);

	    if(JLabelOperator.waitJLabel(frm1, "has been processed", true, true) == null) {
		getOutput().printErrLine("1 frame was not processed");
		finalize();
		return(1);
	    }

	    JFrame frm2 = JFrameOperator.waitJFrame("Application_017/2", false, true);

	    if(JLabelOperator.waitJLabel(frm2, "has been processed", true, true) == null) {
		getOutput().printErrLine("2 frame was not processed");
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

    class WindowProcessor implements WindowJob {
	Vector processed;
	public WindowProcessor() {
	    processed = new Vector();
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JFrame &&
	       processed.indexOf(comp) == -1) {
		return(((JFrame)comp).getTitle().startsWith("Application_017"));
	    } else {
		return(false);
	    }
	}
	public Object launch(Object obj) {
	    try {
		new JButtonOperator(JButtonOperator.
				    waitJButton((JFrame)obj,
						"process", false, true)).push();
		processed.add(obj);
	    } catch(TimeoutExpiredException e) {
		getOutput().printStackTrace(e);
	    }
	    return(null);
	}
	public String getDescription() {
	    return("Application_017 closer");
	}
    }
}
