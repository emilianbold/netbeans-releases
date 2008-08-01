package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.*;

import java.awt.*;

import java.io.PrintWriter;

import javax.swing.*;
import javax.swing.tree.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_027 extends JemmyTest {

    public int runIt(Object obj) {

	try {
	    (new ClassReference("org.netbeans.jemmy.testing.Application_027")).startApplication();

	    JFrame win =JFrameOperator.waitJFrame("Application_027", true, true);

	    JTabbedPaneOperator tpo = new JTabbedPaneOperator(JTabbedPaneOperator.
							      waitJTabbedPane(win, "Table Page", true, true, 0));

	    tpo.selectPage("List Page", true, true);

	    doSleep(100);

	    JListOperator listOper = new JListOperator(JListOperator.
						       waitJList(win, null, false, false, -1));

	    listOper.clickOnItem("0", true, true);

	    doSleep(100);

	    if(listOper.getRenderedComponent(0) == null) {
		getOutput().printError("FAILED \nComponent shouldn't be null");
		return(1);
	    }

	    if(!(listOper.getRenderedComponent(0) instanceof JPanel)) {
		getOutput().printError("FAILED \nComponent type: " +
				       listOper.getRenderedComponent(0).getClass().getName() +
				       "\nshould be javax.swing.JPanel");
		return(1);
	    }

	    if(new ComponentSearcher((Container)listOper.getRenderedComponent(0, true, true)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("!0!"));
		       }
		       public String getDescription() {
			   return("Label with \"!0!\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"!0!\" label");
		return(1);
	    }

	    if(new ComponentSearcher((Container)listOper.getRenderedComponent(1)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("1"));
		       }
		       public String getDescription() {
			   return("Label with \"1\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"1\" label");
		return(1);
	    }

	    tpo.selectPage("Table Page", true, true);

	    doSleep(100);

	    JTableOperator tabOper = new JTableOperator(JTableOperator.
							findJTable(win, null, false, false, -1, -1));

            tabOper.getHeaderOperator().moveColumn(0, 1);
            if(tabOper.findCellRow("04", 1, 0) != 4 ||
               tabOper.findCell("04", 0).x != 1 ||
               tabOper.findCellColumn("04", 4, 0) != 1 ||
               tabOper.findColumn("1") != 0) {
		getOutput().printError("Column was not moved");
		return(1);
            }
            tabOper.getHeaderOperator().moveColumn(1, 0);
            if(tabOper.findCellRow("14", 1, 0) != 4 ||
               tabOper.findCell("14", 0).x != 1 ||
               tabOper.findCellColumn("14", 4, 0) != 1 ||
               tabOper.findColumn("0") != 0) {
		getOutput().printError("Column was not moved");
		return(1);
            }

	    tabOper.clickOnCell(0, 0);

	    doSleep(100);

	    if(new ComponentSearcher((Container)tabOper.getRenderedComponent(0, 0)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("!00!"));
		       }
		       public String getDescription() {
			   return("Label with \"!00!\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"!00!\" label");
		return(1);
	    }

	    if(new ComponentSearcher((Container)tabOper.getRenderedComponent(1, 1)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("11"));
		       }
		       public String getDescription() {
			   return("Label with \"11\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"11\" label");
		return(1);
	    }

	    tabOper.clickOnCell(2, 2, 1);

	    tpo.selectPage("Tree Page", true, true);

	    JTreeOperator treeOper = new JTreeOperator(JTreeOperator.
						       findJTree(win, null, false, false, -1));
	    TreePath rootPath = treeOper.getPathForRow(treeOper.findRow("00", false, true));
	    TreePath midPath = treeOper.getPathForRow(treeOper.findRow("00", false, true));
	    TreePath lastPath = treeOper.findPath("40/44", "/", false, true);

	    treeOper.selectPath(lastPath);

	    if(new ComponentSearcher((Container)treeOper.getRenderedComponent(lastPath)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("!44!"));
		       }
		       public String getDescription() {
			   return("Label with \"!44!\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"!44!\" label");
		return(1);
	    }

	    if(new ComponentSearcher((Container)treeOper.getRenderedComponent(rootPath)).
	       findComponent(new org.netbeans.jemmy.ComponentChooser() {
		       public boolean checkComponent(Component comp) {
			   return(comp instanceof JLabel &&
				  ((JLabel)comp).getText().equals("00"));
		       }
		       public String getDescription() {
			   return("Label with \"00\" text");
		       }
		   }) == null) {
		getOutput().printError("FAILED \nComponent should has \"00\" label");
		return(1);
	    }
   

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();
	return(0);
    }

}
