package org.netbeans.jemmy.testing;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.drivers.*;
import org.netbeans.jemmy.drivers.menus.*;
import org.netbeans.jemmy.testing.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.demo.Demonstrator;

import java.lang.reflect.InvocationTargetException;

public class jemmy_005 extends JemmyTest {

    JListOperator lo;
    TreePath[] paths;
    ListChecker checker;
    
    public int runIt(Object obj) {

	try {
	    try {
		(new ClassReference("org.netbeans.jemmy.testing.Application_005")).startApplication();
	    } catch(ClassNotFoundException e) {
		getOutput().printStackTrace(e);
	    } catch(InvocationTargetException e) {
		getOutput().printStackTrace(e);
	    } catch(NoSuchMethodException e) {
		getOutput().printStackTrace(e);
	    }

	    EventDispatcher.waitQueueEmpty();

	    JFrame frm =JFrameOperator.waitJFrame("Application_005", true, true);

	    JTreeOperator to = new JTreeOperator(JTreeOperator.findJTree(frm, null, true, true, -1));

	    TreePath pth = to.findPath("node00", "|");
	    printLine(pth);
	    printLine(pth.getLastPathComponent());
	    if(to.getChildCount(pth) != 2 ||
	       to.getChildCount(pth.getLastPathComponent()) != 2) {
		getOutput().printErrLine("get children JTreeOperator functionality does not work!");
		getOutput().printErrLine("to.getChildCount(pth) = " + to.getChildCount(pth));
		getOutput().printErrLine("to.getChildCount(pth.getLastPathComponent()) = " + to.getChildCount(pth.getLastPathComponent()));
		finalize();
		return(1);
	    }
	    if(to.getChildPath(pth, 0).getLastPathComponent() != 
	       to.getChild(pth.getLastPathComponent(), 0)) {
		getOutput().printErrLine("get children JTreeOperator functionality does not work!");
		getOutput().printErrLine("to.getChildPath(pth, 0).getLastPathComponent() = " + to.getChildPath(pth, 0).getLastPathComponent().toString());
		getOutput().printErrLine("to.getChild(pth.getLastPathComponent() = " + to.getChild(pth.getLastPathComponent(), 0).toString());
		finalize();
		return(1);
	    }
	    if(to.getChildPath(pth, 1).getLastPathComponent() != 
	       to.getChild(pth.getLastPathComponent(), 1)) {
		getOutput().printErrLine("get children JTreeOperator functionality does not work!");
		getOutput().printErrLine("to.getChildPath(pth, 1).getLastPathComponent() = " + to.getChildPath(pth, 1).getLastPathComponent().toString());
		getOutput().printErrLine("to.getChild(pth.getLastPathComponent(), 1) = " + to.getChild(pth.getLastPathComponent(), 1).toString());
		finalize();
		return(1);
	    }
	    if(to.getChildren(pth.getLastPathComponent())[0] !=
	       to.getChildPaths(pth)[0].getLastPathComponent()) {
		getOutput().printErrLine("get children JTreeOperator functionality does not work!");
		getOutput().printErrLine("to.getChildren(pth.getLastPathComponent())[0] = " + to.getChildren(pth.getLastPathComponent())[0].toString());
		getOutput().printErrLine("to.getChildPaths(pth)[0].getLastPathComponent() = " + to.getChildPaths(pth)[0].getLastPathComponent().toString());
		finalize();
		return(1);
	    }
	    if(to.getChildren(pth.getLastPathComponent())[1] !=
	       to.getChildPaths(pth)[1].getLastPathComponent()) {
		getOutput().printErrLine("get children JTreeOperator functionality does not work!");
		getOutput().printErrLine("to.getChildren(pth.getLastPathComponent())[1] = " + to.getChildren(pth.getLastPathComponent())[1].toString());
		getOutput().printErrLine("to.getChildPaths(pth)[1].getLastPathComponent() = " + to.getChildPaths(pth)[1].getLastPathComponent().toString());
		finalize();
		return(1);
	    }

	    lo = new JListOperator(JListOperator.findJList(frm, null, true, true, -1));
	    JPopupMenuOperator pmo;
	    String[] strPaths = {"", "node00", "node00|node000", "node00|node001", "node01"};
	    paths = new TreePath[strPaths.length];
	    checker = new ListChecker();

	    Demonstrator.setTitle("jemmy_005 test");

	    JSplitPaneOperator split = new JSplitPaneOperator(new JFrameOperator(frm));

	    Demonstrator.nextStep("Move divider to center.");

	    split.moveDivider(0.5);

	    new JCheckBoxOperator(new JFrameOperator(frm), "Huge Popup").changeSelection(true);

	    for(int i = 0; i < strPaths.length; i++) {
		Demonstrator.nextStep("Call popup on \"" + strPaths[i] + "\" path");
		paths[i] = to.findPath(strPaths[i], "|", true, true);
		to.callPopupOnPath(paths[i]);

                pmo = JPopupMenuOperator.waitJPopupMenu("XXX");

		if(i == 0) {
		    if(!testJPopupMenu(pmo)) {
			finalize();
			return(1);
		    }
		}

                if(pmo.showMenuItems("", "|").length != 1) {
                    finalize();
                    return(1);
                }
                
                ComponentChooser[] choosers1 = {new MenuItemChooser("XXX"), new MenuItemChooser("submenu")};
                if(pmo.showMenuItems(choosers1).length != 2) {
                    finalize();
                    return(1);
                }

                if(!pmo.showMenuItem(choosers1).getText().equals("submenu")) {
                    finalize();
                    return(1);
                }

                if(!pmo.showMenuItem("XXX", "|").getText().equals("XXX")) {
                    finalize();
                    return(1);
                }
                
                if(!pmo.showMenuItem("XXX|submenu|subsubmenu|menuItem", "|").getText().equals("menuItem")) {
                    finalize();
                    return(1);
                }
                
                if(!pmo.showMenuItem("XXX|submenu|subsubmenu", "|").getText().equals("subsubmenu")) {
                    finalize();
                    return(1);
                }

		pmo.pushMenu("XXX|submenu|subsubmenu|menuItem", "|", true, true);
		TreePath[] pths = {paths[i]};
		(new Waiter(checker)).waitAction(pths);
                Thread.sleep(100);
	    }

            pth = to.findPath(new String[] {"node", "node"},
                              new int[]    {1,      1});
            to.callPopupOnPath(pth);
            pmo = JPopupMenuOperator.waitJPopupMenu("XXX");
            pmo.pushMenu("XXX|submenu|subsubmenu|menuItem", "|", true, true);
            (new Waiter(checker)).waitAction(new TreePath[] {pth});
            Thread.sleep(100);

	    new JCheckBoxOperator(new JFrameOperator(frm), "Huge Popup").changeSelection(false);

	    for(int i = 0; i < strPaths.length; i++) {
		for(int j = i + 1; j < strPaths.length; j++) {
		    for(int k = j + 1; k < strPaths.length; k++) {
			for(int l = k + 1; l < strPaths.length; l++) {
			    Demonstrator.nextStep("Call popup on " + 
						  "\"" + strPaths[i] + "\", " +
						  "\"" + strPaths[j] + "\", " +
						  "\"" + strPaths[k] + "\", " +
						  "\"" + strPaths[l] + "\" " +
						  " pathes");
			    TreePath[] pths = {paths[i], paths[j], paths[k], paths[l]};
			    pmo = new JPopupMenuOperator(to.callPopupOnPaths(pths));
			    pmo.pushMenu("XXX|submenu|subsubmenu|menuItem", "|", true, true);
			    (new Waiter(checker)).waitAction(pths);
                            Thread.sleep(100);
			}
		    }
		}
	    }

	    Demonstrator.showFinalComment("Test passed");
	    
	    if(!testJList(lo)) {
		finalize();
		return(1);
	    }

	    if(!testJSplitPane(split)) {
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

    public static class MenuItemChooser implements ComponentChooser {
        String text;
        public MenuItemChooser(String text) {
            this.text = text;
        }
        public boolean checkComponent(Component comp) {
            return(comp instanceof JMenuItem &&
                    Operator.getDefaultStringComparator().equals(text, ((JMenuItem)comp).getText()));
        }
        public String getDescription() {
            return("MenuItem with \"" + text + "\" text");
        }
    }
    class ListChecker implements Waitable {
	public Object actionProduced(Object obj) {
	    TreePath[] pths = (TreePath[])obj;
	    if(lo.getModel().getSize() != pths.length) {
		return(null);
	    }
	    for(int i = 0; i < pths.length; i++) {
		if(!lo.getModel().getElementAt(i).toString().equals(pths[i].toString())) {
		    return(null);
		}
	    }
	    return(lo);
	}
	public String getDescription() {
	    return("");
	}
    }
    
    public boolean testJList(JListOperator jListOperator) {
	if(((JList)jListOperator.getSource()).getAnchorSelectionIndex() == jListOperator.getAnchorSelectionIndex()) {
	    printLine("getAnchorSelectionIndex does work");
	} else {
	    printLine("getAnchorSelectionIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getAnchorSelectionIndex());
	    printLine(jListOperator.getAnchorSelectionIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getCellRenderer() == null &&
	   jListOperator.getCellRenderer() == null ||
	   ((JList)jListOperator.getSource()).getCellRenderer().equals(jListOperator.getCellRenderer())) {
	    printLine("getCellRenderer does work");
	} else {
	    printLine("getCellRenderer does not work");
	    printLine(((JList)jListOperator.getSource()).getCellRenderer());
	    printLine(jListOperator.getCellRenderer());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getFirstVisibleIndex() == jListOperator.getFirstVisibleIndex()) {
	    printLine("getFirstVisibleIndex does work");
	} else {
	    printLine("getFirstVisibleIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getFirstVisibleIndex());
	    printLine(jListOperator.getFirstVisibleIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getFixedCellHeight() == jListOperator.getFixedCellHeight()) {
	    printLine("getFixedCellHeight does work");
	} else {
	    printLine("getFixedCellHeight does not work");
	    printLine(((JList)jListOperator.getSource()).getFixedCellHeight());
	    printLine(jListOperator.getFixedCellHeight());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getFixedCellWidth() == jListOperator.getFixedCellWidth()) {
	    printLine("getFixedCellWidth does work");
	} else {
	    printLine("getFixedCellWidth does not work");
	    printLine(((JList)jListOperator.getSource()).getFixedCellWidth());
	    printLine(jListOperator.getFixedCellWidth());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getLastVisibleIndex() == jListOperator.getLastVisibleIndex()) {
	    printLine("getLastVisibleIndex does work");
	} else {
	    printLine("getLastVisibleIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getLastVisibleIndex());
	    printLine(jListOperator.getLastVisibleIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getLeadSelectionIndex() == jListOperator.getLeadSelectionIndex()) {
	    printLine("getLeadSelectionIndex does work");
	} else {
	    printLine("getLeadSelectionIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getLeadSelectionIndex());
	    printLine(jListOperator.getLeadSelectionIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getMaxSelectionIndex() == jListOperator.getMaxSelectionIndex()) {
	    printLine("getMaxSelectionIndex does work");
	} else {
	    printLine("getMaxSelectionIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getMaxSelectionIndex());
	    printLine(jListOperator.getMaxSelectionIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getMinSelectionIndex() == jListOperator.getMinSelectionIndex()) {
	    printLine("getMinSelectionIndex does work");
	} else {
	    printLine("getMinSelectionIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getMinSelectionIndex());
	    printLine(jListOperator.getMinSelectionIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getModel() == null &&
	   jListOperator.getModel() == null ||
	   ((JList)jListOperator.getSource()).getModel().equals(jListOperator.getModel())) {
	    printLine("getModel does work");
	} else {
	    printLine("getModel does not work");
	    printLine(((JList)jListOperator.getSource()).getModel());
	    printLine(jListOperator.getModel());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getPreferredScrollableViewportSize() == null &&
	   jListOperator.getPreferredScrollableViewportSize() == null ||
	   ((JList)jListOperator.getSource()).getPreferredScrollableViewportSize().equals(jListOperator.getPreferredScrollableViewportSize())) {
	    printLine("getPreferredScrollableViewportSize does work");
	} else {
	    printLine("getPreferredScrollableViewportSize does not work");
	    printLine(((JList)jListOperator.getSource()).getPreferredScrollableViewportSize());
	    printLine(jListOperator.getPreferredScrollableViewportSize());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getPrototypeCellValue() == null &&
	   jListOperator.getPrototypeCellValue() == null ||
	   ((JList)jListOperator.getSource()).getPrototypeCellValue().equals(jListOperator.getPrototypeCellValue())) {
	    printLine("getPrototypeCellValue does work");
	} else {
	    printLine("getPrototypeCellValue does not work");
	    printLine(((JList)jListOperator.getSource()).getPrototypeCellValue());
	    printLine(jListOperator.getPrototypeCellValue());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getScrollableTracksViewportHeight() == jListOperator.getScrollableTracksViewportHeight()) {
	    printLine("getScrollableTracksViewportHeight does work");
	} else {
	    printLine("getScrollableTracksViewportHeight does not work");
	    printLine(((JList)jListOperator.getSource()).getScrollableTracksViewportHeight());
	    printLine(jListOperator.getScrollableTracksViewportHeight());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getScrollableTracksViewportWidth() == jListOperator.getScrollableTracksViewportWidth()) {
	    printLine("getScrollableTracksViewportWidth does work");
	} else {
	    printLine("getScrollableTracksViewportWidth does not work");
	    printLine(((JList)jListOperator.getSource()).getScrollableTracksViewportWidth());
	    printLine(jListOperator.getScrollableTracksViewportWidth());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectedIndex() == jListOperator.getSelectedIndex()) {
	    printLine("getSelectedIndex does work");
	} else {
	    printLine("getSelectedIndex does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectedIndex());
	    printLine(jListOperator.getSelectedIndex());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectedValue() == null &&
	   jListOperator.getSelectedValue() == null ||
	   ((JList)jListOperator.getSource()).getSelectedValue().equals(jListOperator.getSelectedValue())) {
	    printLine("getSelectedValue does work");
	} else {
	    printLine("getSelectedValue does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectedValue());
	    printLine(jListOperator.getSelectedValue());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectionBackground() == null &&
	   jListOperator.getSelectionBackground() == null ||
	   ((JList)jListOperator.getSource()).getSelectionBackground().equals(jListOperator.getSelectionBackground())) {
	    printLine("getSelectionBackground does work");
	} else {
	    printLine("getSelectionBackground does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectionBackground());
	    printLine(jListOperator.getSelectionBackground());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectionForeground() == null &&
	   jListOperator.getSelectionForeground() == null ||
	   ((JList)jListOperator.getSource()).getSelectionForeground().equals(jListOperator.getSelectionForeground())) {
	    printLine("getSelectionForeground does work");
	} else {
	    printLine("getSelectionForeground does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectionForeground());
	    printLine(jListOperator.getSelectionForeground());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectionMode() == jListOperator.getSelectionMode()) {
	    printLine("getSelectionMode does work");
	} else {
	    printLine("getSelectionMode does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectionMode());
	    printLine(jListOperator.getSelectionMode());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getSelectionModel() == null &&
	   jListOperator.getSelectionModel() == null ||
	   ((JList)jListOperator.getSource()).getSelectionModel().equals(jListOperator.getSelectionModel())) {
	    printLine("getSelectionModel does work");
	} else {
	    printLine("getSelectionModel does not work");
	    printLine(((JList)jListOperator.getSource()).getSelectionModel());
	    printLine(jListOperator.getSelectionModel());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getUI() == null &&
	   jListOperator.getUI() == null ||
	   ((JList)jListOperator.getSource()).getUI().equals(jListOperator.getUI())) {
	    printLine("getUI does work");
	} else {
	    printLine("getUI does not work");
	    printLine(((JList)jListOperator.getSource()).getUI());
	    printLine(jListOperator.getUI());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getValueIsAdjusting() == jListOperator.getValueIsAdjusting()) {
	    printLine("getValueIsAdjusting does work");
	} else {
	    printLine("getValueIsAdjusting does not work");
	    printLine(((JList)jListOperator.getSource()).getValueIsAdjusting());
	    printLine(jListOperator.getValueIsAdjusting());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).getVisibleRowCount() == jListOperator.getVisibleRowCount()) {
	    printLine("getVisibleRowCount does work");
	} else {
	    printLine("getVisibleRowCount does not work");
	    printLine(((JList)jListOperator.getSource()).getVisibleRowCount());
	    printLine(jListOperator.getVisibleRowCount());
	    return(false);
	}
	if(((JList)jListOperator.getSource()).isSelectionEmpty() == jListOperator.isSelectionEmpty()) {
	    printLine("isSelectionEmpty does work");
	} else {
	    printLine("isSelectionEmpty does not work");
	    printLine(((JList)jListOperator.getSource()).isSelectionEmpty());
	    printLine(jListOperator.isSelectionEmpty());
	    return(false);
	}
	return(true);
    }

    public boolean testJSplitPane(JSplitPaneOperator jSplitPaneOperator) {
	if(((JSplitPane)jSplitPaneOperator.getSource()).getBottomComponent() == null &&
	   jSplitPaneOperator.getBottomComponent() == null ||
	   ((JSplitPane)jSplitPaneOperator.getSource()).getBottomComponent().equals(jSplitPaneOperator.getBottomComponent())) {
	    printLine("getBottomComponent does work");
	} else {
	    printLine("getBottomComponent does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getBottomComponent());
	    printLine(jSplitPaneOperator.getBottomComponent());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getDividerLocation() == jSplitPaneOperator.getDividerLocation()) {
	    printLine("getDividerLocation does work");
	} else {
	    printLine("getDividerLocation does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getDividerLocation());
	    printLine(jSplitPaneOperator.getDividerLocation());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getDividerSize() == jSplitPaneOperator.getDividerSize()) {
	    printLine("getDividerSize does work");
	} else {
	    printLine("getDividerSize does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getDividerSize());
	    printLine(jSplitPaneOperator.getDividerSize());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getLastDividerLocation() == jSplitPaneOperator.getLastDividerLocation()) {
	    printLine("getLastDividerLocation does work");
	} else {
	    printLine("getLastDividerLocation does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getLastDividerLocation());
	    printLine(jSplitPaneOperator.getLastDividerLocation());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getLeftComponent() == null &&
	   jSplitPaneOperator.getLeftComponent() == null ||
	   ((JSplitPane)jSplitPaneOperator.getSource()).getLeftComponent().equals(jSplitPaneOperator.getLeftComponent())) {
	    printLine("getLeftComponent does work");
	} else {
	    printLine("getLeftComponent does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getLeftComponent());
	    printLine(jSplitPaneOperator.getLeftComponent());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getMaximumDividerLocation() == jSplitPaneOperator.getMaximumDividerLocation()) {
	    printLine("getMaximumDividerLocation does work");
	} else {
	    printLine("getMaximumDividerLocation does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getMaximumDividerLocation());
	    printLine(jSplitPaneOperator.getMaximumDividerLocation());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getMinimumDividerLocation() == jSplitPaneOperator.getMinimumDividerLocation()) {
	    printLine("getMinimumDividerLocation does work");
	} else {
	    printLine("getMinimumDividerLocation does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getMinimumDividerLocation());
	    printLine(jSplitPaneOperator.getMinimumDividerLocation());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getOrientation() == jSplitPaneOperator.getOrientation()) {
	    printLine("getOrientation does work");
	} else {
	    printLine("getOrientation does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getOrientation());
	    printLine(jSplitPaneOperator.getOrientation());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getRightComponent() == null &&
	   jSplitPaneOperator.getRightComponent() == null ||
	   ((JSplitPane)jSplitPaneOperator.getSource()).getRightComponent().equals(jSplitPaneOperator.getRightComponent())) {
	    printLine("getRightComponent does work");
	} else {
	    printLine("getRightComponent does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getRightComponent());
	    printLine(jSplitPaneOperator.getRightComponent());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getTopComponent() == null &&
	   jSplitPaneOperator.getTopComponent() == null ||
	   ((JSplitPane)jSplitPaneOperator.getSource()).getTopComponent().equals(jSplitPaneOperator.getTopComponent())) {
	    printLine("getTopComponent does work");
	} else {
	    printLine("getTopComponent does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getTopComponent());
	    printLine(jSplitPaneOperator.getTopComponent());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).getUI() == null &&
	   jSplitPaneOperator.getUI() == null ||
	   ((JSplitPane)jSplitPaneOperator.getSource()).getUI().equals(jSplitPaneOperator.getUI())) {
	    printLine("getUI does work");
	} else {
	    printLine("getUI does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).getUI());
	    printLine(jSplitPaneOperator.getUI());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).isContinuousLayout() == jSplitPaneOperator.isContinuousLayout()) {
	    printLine("isContinuousLayout does work");
	} else {
	    printLine("isContinuousLayout does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).isContinuousLayout());
	    printLine(jSplitPaneOperator.isContinuousLayout());
	    return(false);
	}
	if(((JSplitPane)jSplitPaneOperator.getSource()).isOneTouchExpandable() == jSplitPaneOperator.isOneTouchExpandable()) {
	    printLine("isOneTouchExpandable does work");
	} else {
	    printLine("isOneTouchExpandable does not work");
	    printLine(((JSplitPane)jSplitPaneOperator.getSource()).isOneTouchExpandable());
	    printLine(jSplitPaneOperator.isOneTouchExpandable());
	    return(false);
	}
	return(true);
    }

    public boolean testJPopupMenu(JPopupMenuOperator jPopupMenuOperator) {
	if(((JPopupMenu)jPopupMenuOperator.getSource()).getInvoker() == null &&
	   jPopupMenuOperator.getInvoker() == null ||
	   ((JPopupMenu)jPopupMenuOperator.getSource()).getInvoker().equals(jPopupMenuOperator.getInvoker())) {
	    printLine("getInvoker does work");
	} else {
	    printLine("getInvoker does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).getInvoker());
	    printLine(jPopupMenuOperator.getInvoker());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).getLabel() == null &&
	   jPopupMenuOperator.getLabel() == null ||
	   ((JPopupMenu)jPopupMenuOperator.getSource()).getLabel().equals(jPopupMenuOperator.getLabel())) {
	    printLine("getLabel does work");
	} else {
	    printLine("getLabel does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).getLabel());
	    printLine(jPopupMenuOperator.getLabel());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).getMargin() == null &&
	   jPopupMenuOperator.getMargin() == null ||
	   ((JPopupMenu)jPopupMenuOperator.getSource()).getMargin().equals(jPopupMenuOperator.getMargin())) {
	    printLine("getMargin does work");
	} else {
	    printLine("getMargin does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).getMargin());
	    printLine(jPopupMenuOperator.getMargin());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).getSelectionModel() == null &&
	   jPopupMenuOperator.getSelectionModel() == null ||
	   ((JPopupMenu)jPopupMenuOperator.getSource()).getSelectionModel().equals(jPopupMenuOperator.getSelectionModel())) {
	    printLine("getSelectionModel does work");
	} else {
	    printLine("getSelectionModel does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).getSelectionModel());
	    printLine(jPopupMenuOperator.getSelectionModel());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).getUI() == null &&
	   jPopupMenuOperator.getUI() == null ||
	   ((JPopupMenu)jPopupMenuOperator.getSource()).getUI().equals(jPopupMenuOperator.getUI())) {
	    printLine("getUI does work");
	} else {
	    printLine("getUI does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).getUI());
	    printLine(jPopupMenuOperator.getUI());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).isBorderPainted() == jPopupMenuOperator.isBorderPainted()) {
	    printLine("isBorderPainted does work");
	} else {
	    printLine("isBorderPainted does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).isBorderPainted());
	    printLine(jPopupMenuOperator.isBorderPainted());
	    return(false);
	}
	if(((JPopupMenu)jPopupMenuOperator.getSource()).isLightWeightPopupEnabled() == jPopupMenuOperator.isLightWeightPopupEnabled()) {
	    printLine("isLightWeightPopupEnabled does work");
	} else {
	    printLine("isLightWeightPopupEnabled does not work");
	    printLine(((JPopupMenu)jPopupMenuOperator.getSource()).isLightWeightPopupEnabled());
	    printLine(jPopupMenuOperator.isLightWeightPopupEnabled());
	    return(false);
	}
	return(true);
    }

}
