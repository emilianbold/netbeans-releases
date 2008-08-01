package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.demo.Demonstrator;

import java.lang.reflect.InvocationTargetException;

public class jemmy_006 extends JemmyTest {

    TreeChecker checker;
    JTree tree;
    JTreeOperator to;
    
    public int runIt(Object obj) {
	
	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_006")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    new QueueTool().waitEmpty(100);

	    JFrame frm =JFrameOperator.waitJFrame("Application_006", true, true);
	    
	    tree = JTreeOperator.findJTree(frm, null, true, true, -1);
	    to = new JTreeOperator(tree);

	    TreeChecker checker = new TreeChecker();

	    Demonstrator.setTitle("jemmy_006 test");
	    Demonstrator.nextStep("Select root");
	    
	    to.selectRow(0);
	    to.waitSelected(0);

	    Demonstrator.nextStep("Expand first node00 tree path");
	
	    to.doExpandPath(to.findPath("node00", "|", true, true));
	    (new Waiter(checker)).waitAction("first expanded");

	    Demonstrator.nextStep("Expand second node00 tree path");
	
	    to.doExpandPath(to.findPath("node00", "1", "|", true, true));
	    (new Waiter(checker)).waitAction("second expanded");

	    Demonstrator.nextStep("Collapse first node00 tree path");

	    to.collapsePath(to.findPath("node00", "|", true, true));
	    (new Waiter(checker)).waitAction("first collapsed");
	
	    Demonstrator.nextStep("Collapse first node00 tree path");

	    to.collapsePath(to.findPath("node00", "1", "|", true, true));
	    (new Waiter(checker)).waitAction("second collapsed");
	
	    Demonstrator.nextStep("Expand second row");
	
	    to.doExpandRow(1);
	    (new Waiter(checker)).waitAction("first expanded");

	    Demonstrator.nextStep("Expand fifth row");
	
	    to.doExpandRow(4);
	    (new Waiter(checker)).waitAction("second expanded");

	    Demonstrator.nextStep("Collapse secont row");

	    to.collapseRow(1);
	    (new Waiter(checker)).waitAction("first collapsed");
	
	    Demonstrator.nextStep("Collapse third row");

	    to.collapseRow(2);
	    (new Waiter(checker)).waitAction("second collapsed");
	
	    Demonstrator.nextStep("Change second node00 text to node01");

	    TreePath pathy = to.findPath("node00", "1", "|");

	    to.selectPath(pathy);
	    to.selectPath(pathy);
	    if(to.isEditing()) {
		getOutput().printErrLine("JTree turned into editing mode after" +
					 " two path selecting");
		finalize();
		return(1);
	    }

	    to.changePathText(pathy, "node01");

	    Demonstrator.showFinalComment("Test passed");

	    finalize();
	    return(0);
	} catch(Exception e) {
	    getOutput().printStackTrace(e);
	    finalize();
	    return(1);
	}
    }

    class TreeChecker implements Waitable {
	public Object actionProduced(Object obj) {
	    TreePath path = null;
	    try {
		if(((String)obj).startsWith("first")) {
		    path = (new JTreeOperator(tree)).findPath("node00", "0", "|", true, true);
		} else {
		    path = (new JTreeOperator(tree)).findPath("node00", "1", "|", true, true);
		}
	    } catch (TimeoutExpiredException e) {
		getOutput().printStackTrace(e);
		return(null);
	    }
	    if(((String)obj).endsWith("expanded")  && to.isExpanded (path) && to.isExpanded (to.getRowForPath(path)) ||
	       ((String)obj).endsWith("collapsed") && to.isCollapsed(path) && to.isCollapsed(to.getRowForPath(path))) {
		return(this);
	    } else {
		return(null);
	    }
	}
	public String getDescription() {
	    return("");
	}
    }
    
}
