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

	    tabOper.setTableCellEditor(new JTableOperator.CellEditor() {
		    public void makeBeingEdited(JTableOperator oper, int row, int column)
			throws TimeoutExpiredException {
			oper.clickOnCell(row, column, 2);
		    }
		    public boolean checkCellEditor(JTableOperator oper, Component comp, int row, int column) {
			if(comp instanceof JPanel) {
			    ComponentSearcher cs = new ComponentSearcher((Container)comp);
			    cs.setOutput(TestOut.getNullOutput());
			    return(cs.findComponent(new org.netbeans.jemmy.ComponentChooser() {
				    public boolean checkComponent(Component comp) {
					return(comp instanceof JComboBox);
				    }
				    public String getDescription() {
					return("Second combobox");
				    }
				}, 1) != null);
			} else {
			    return(false);
			}
		    }
		    public void enterNewValue(JTableOperator oper, Component editor, int row, int column, Object value)
			throws TimeoutExpiredException {
			org.netbeans.jemmy.ComponentChooser chooser = new org.netbeans.jemmy.ComponentChooser() {
				public boolean checkComponent(Component comp) {
				    return(comp instanceof JComboBox);
				}
				public String getDescription() {
				    return("Second combobox");
				}
			    };
			ComponentSearcher cs = new ComponentSearcher((Container)editor);
			cs.setOutput(TestOut.getNullOutput());
			JComboBoxOperator fComboOper = new JComboBoxOperator((JComboBox)cs.findComponent(chooser, 0));
			fComboOper.setOutput(oper.getOutput());
			fComboOper.selectItem(((String)value).substring(0, 1), true, true);
			JComboBoxOperator sComboOper = new JComboBoxOperator((JComboBox)cs.findComponent(chooser, 1));
			sComboOper.setOutput(oper.getOutput());
			sComboOper.selectItem(((String)value).substring(1, 2), true, true);
		    }
		    public String getDescription() {
			return("Two combo value editor");
		    }
		});

	    for(int i = 0; i < 5; i++) {
		tabOper.changeCellText(i, i, "00");
		if(!((String)tabOper.getValueAt(i, i)).equals("00")) {
		    getOutput().printError("Wrong (" + i + "," + i +
					   ") cell value :\"" +
					   (String)tabOper.getValueAt(i, i) +
					   "\" should be \"00\"");
		}
	    }

	    for(int i = 0; i < 5; i++) {
		tabOper.changeCellText(i, 5 - i - 1, "44");
		if(!((String)tabOper.getValueAt(i, 5 - i - 1)).equals("44")) {
		    getOutput().printError("Wrong (" + i + "," + (5 - i - 1) +
					   ") cell value :\"" +
					   (String)tabOper.getValueAt(i, 5 - i - 1) +
					   "\" should be \"44\"");
		}
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
   
	    treeOper.setTreeCellEditor(new JTreeOperator.CellEditor() {
		    public void makeBeingEdited(JTreeOperator oper, TreePath path)
			throws TimeoutExpiredException {
			oper.clickOnPath(path, 2);
		    }
		    public boolean checkCellEditor(JTreeOperator oper, Component comp, TreePath path) {
			if(comp instanceof JPanel) {
			    ComponentSearcher cs = new ComponentSearcher((Container)comp);
			    cs.setOutput(TestOut.getNullOutput());
			    return(cs.findComponent(new org.netbeans.jemmy.ComponentChooser() {
				    public boolean checkComponent(Component comp) {
					return(comp instanceof JComboBox);
				    }
				    public String getDescription() {
					return("Second combobox");
				    }
				}, 1) != null);
			} else {
			    return(false);
			}
		    }
		    public void enterNewValue(JTreeOperator oper, Component editor, TreePath path, Object value)
			throws TimeoutExpiredException {
			org.netbeans.jemmy.ComponentChooser chooser = new org.netbeans.jemmy.ComponentChooser() {
				public boolean checkComponent(Component comp) {
				    return(comp instanceof JComboBox);
				}
				public String getDescription() {
				    return("Second combobox");
				}
			    };
			ComponentSearcher cs = new ComponentSearcher((Container)editor);
			cs.setOutput(TestOut.getNullOutput());
			JComboBoxOperator fComboOper = new JComboBoxOperator((JComboBox)cs.findComponent(chooser, 0));
			fComboOper.setOutput(oper.getOutput());
			fComboOper.selectItem(((String)value).substring(0, 1), true, true);
			JComboBoxOperator sComboOper = new JComboBoxOperator((JComboBox)cs.findComponent(chooser, 1));
			sComboOper.setOutput(oper.getOutput());
			sComboOper.selectItem(((String)value).substring(1, 2), true, true);
		    }
		    public String getDescription() {
			return("Two combo value editor");
		    }
		});
	    
	    treeOper.changePathText(rootPath, "44");
	    treeOper.changePathText(lastPath, "00");
	    treeOper.selectPath(midPath);
	    
	    if(!treeOper.findPath("", "/", false, true).
	       getLastPathComponent().toString().equals("44")) {
		getOutput().printError("Root should have \"44\" text");
		return(1);
	    }

	    lastPath = treeOper.findPath("40/00", "/", false, true);

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

}
