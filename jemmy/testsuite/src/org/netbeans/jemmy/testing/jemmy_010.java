package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_010 extends JemmyTest {

    public int runIt(Object obj) {
	
	try {
	    Exception e = (Exception)(new ActionProducer(new org.netbeans.jemmy.Action() {
		    public Object launch(Object obj) {
			try {
			    (new ClassReference("org.netbeans.jemmy.testing.Application_010")).startApplication();
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

	    JDialog frm0 = JDialogOperator.waitJDialog("Application_010", false, true);

	    JDialogOperator fo = new JDialogOperator(frm0);
	    JDialogOperator fo2 = new JDialogOperator();
	    DialogOperator fo3 = new DialogOperator();
	    if(fo2.getSource() != fo.getSource() ||
	       fo3.getSource() != fo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(fo.getSource().toString());
		getOutput().printErrLine(fo2.getSource().toString());
		getOutput().printErrLine(fo3.getSource().toString());
		finalize();
		return(1);
	    }

	    if(((Application_010)frm0).getIndex() != 0) {
		finalize();
		return(1);
	    }
	    
	    JDialog frm1 = JDialogOperator.waitJDialog("Application_010", false, true, 1);

	    if(((Application_010)frm1).getIndex() != 1) {
		finalize();
		return(1);
	    }
	    
	    JDialog frm2 = JDialogOperator.waitJDialog("Application_010", false, true, 2);

	    fo = new JDialogOperator(frm2);
	    fo2 = new JDialogOperator(2);
	    fo3 = new DialogOperator(2);
	    if(fo2.getSource() != fo.getSource() ||
	       fo3.getSource() != fo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(fo.getSource().toString());
		getOutput().printErrLine(fo2.getSource().toString());
		getOutput().printErrLine(fo3.getSource().toString());
		finalize();
		return(1);
	    }

	    if(((Application_010)frm2).getIndex() != 2) {
		finalize();
		return(1);
	    }

	    if(!testDialog(new JDialogOperator(frm2))) {
		finalize();
		return(1);
	    }

	    if(!testJDialog(new JDialogOperator(frm2))) {
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
public boolean testDialog(DialogOperator dialogOperator) {
    if(((Dialog)dialogOperator.getSource()).getTitle() == null &&
       dialogOperator.getTitle() == null ||
       ((Dialog)dialogOperator.getSource()).getTitle().equals(dialogOperator.getTitle())) {
        printLine("getTitle does work");
    } else {
        printLine("getTitle does not work");
        printLine(((Dialog)dialogOperator.getSource()).getTitle());
        printLine(dialogOperator.getTitle());
        return(false);
    }
    if(((Dialog)dialogOperator.getSource()).isModal() == dialogOperator.isModal()) {
        printLine("isModal does work");
    } else {
        printLine("isModal does not work");
        printLine(((Dialog)dialogOperator.getSource()).isModal());
        printLine(dialogOperator.isModal());
        return(false);
    }
    if(((Dialog)dialogOperator.getSource()).isResizable() == dialogOperator.isResizable()) {
        printLine("isResizable does work");
    } else {
        printLine("isResizable does not work");
        printLine(((Dialog)dialogOperator.getSource()).isResizable());
        printLine(dialogOperator.isResizable());
        return(false);
    }
    return(true);
}

public boolean testJDialog(JDialogOperator jDialogOperator) {
    if(((JDialog)jDialogOperator.getSource()).getAccessibleContext() == null &&
       jDialogOperator.getAccessibleContext() == null ||
       ((JDialog)jDialogOperator.getSource()).getAccessibleContext().equals(jDialogOperator.getAccessibleContext())) {
        printLine("getAccessibleContext does work");
    } else {
        printLine("getAccessibleContext does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getAccessibleContext());
        printLine(jDialogOperator.getAccessibleContext());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getContentPane() == null &&
       jDialogOperator.getContentPane() == null ||
       ((JDialog)jDialogOperator.getSource()).getContentPane().equals(jDialogOperator.getContentPane())) {
        printLine("getContentPane does work");
    } else {
        printLine("getContentPane does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getContentPane());
        printLine(jDialogOperator.getContentPane());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getDefaultCloseOperation() == jDialogOperator.getDefaultCloseOperation()) {
        printLine("getDefaultCloseOperation does work");
    } else {
        printLine("getDefaultCloseOperation does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getDefaultCloseOperation());
        printLine(jDialogOperator.getDefaultCloseOperation());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getGlassPane() == null &&
       jDialogOperator.getGlassPane() == null ||
       ((JDialog)jDialogOperator.getSource()).getGlassPane().equals(jDialogOperator.getGlassPane())) {
        printLine("getGlassPane does work");
    } else {
        printLine("getGlassPane does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getGlassPane());
        printLine(jDialogOperator.getGlassPane());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getJMenuBar() == null &&
       jDialogOperator.getJMenuBar() == null ||
       ((JDialog)jDialogOperator.getSource()).getJMenuBar().equals(jDialogOperator.getJMenuBar())) {
        printLine("getJMenuBar does work");
    } else {
        printLine("getJMenuBar does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getJMenuBar());
        printLine(jDialogOperator.getJMenuBar());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getLayeredPane() == null &&
       jDialogOperator.getLayeredPane() == null ||
       ((JDialog)jDialogOperator.getSource()).getLayeredPane().equals(jDialogOperator.getLayeredPane())) {
        printLine("getLayeredPane does work");
    } else {
        printLine("getLayeredPane does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getLayeredPane());
        printLine(jDialogOperator.getLayeredPane());
        return(false);
    }
    if(((JDialog)jDialogOperator.getSource()).getRootPane() == null &&
       jDialogOperator.getRootPane() == null ||
       ((JDialog)jDialogOperator.getSource()).getRootPane().equals(jDialogOperator.getRootPane())) {
        printLine("getRootPane does work");
    } else {
        printLine("getRootPane does not work");
        printLine(((JDialog)jDialogOperator.getSource()).getRootPane());
        printLine(jDialogOperator.getRootPane());
        return(false);
    }
    return(true);
}

}
