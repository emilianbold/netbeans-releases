package org.netbeans.jemmy.testing;

import org.netbeans.jemmy.*;

import org.netbeans.jemmy.demo.Demonstrator;

import org.netbeans.jemmy.operators.*;

import org.netbeans.jemmy.util.*;

import java.awt.Component;

import java.io.PrintWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.*;

import java.lang.reflect.InvocationTargetException;

public class jemmy_024 extends JemmyTest {
    private boolean checkInside(ComponentOperator compOper, int x, int y, int width, int height) {
	Component comp = compOper.getSource();
	Component area = compOper.getContainer(new org.netbeans.jemmy.ComponentChooser() {
	    public boolean checkComponent(Component comp) {
		return(comp instanceof JScrollPane);
	    }
	    public String getDescription() {
		return("JScrollPane");
	    }
	});
	double compLeft = comp.getLocationOnScreen().getX() + x;
	double compTop = comp.getLocationOnScreen().getY() + y;
	double compRight = compLeft + width;
	double compBottom = compTop + height;
	double areaLeft = area.getLocationOnScreen().getX();
	double areaTop = area.getLocationOnScreen().getY();
	double areaRight = areaLeft + area.getWidth();
	double areaBottom = areaTop + area.getHeight();
	return(compLeft >= areaLeft && compRight <= areaRight &&
	       compTop >= areaTop && compBottom <=areaBottom);
    }

    public int runIt(Object obj) {

	try {
	    long time = System.currentTimeMillis();
	    (new ClassReference("org.netbeans.jemmy.testing.Application_024")).startApplication();

	    ComponentOperator.setDefaultComponentVisualizer(new EmptyVisualizer());

	    JFrame win =JFrameOperator.waitJFrame("Application_024", true, true);
	    JFrameOperator wino = new JFrameOperator(win);

	    JTabbedPaneOperator tpo = new JTabbedPaneOperator(JTabbedPaneOperator.
							      findJTabbedPane(win, "Table Page", true, true, 0));

	    JTabbedPaneOperator tp1 = new JTabbedPaneOperator(wino);
	    JTabbedPaneOperator tp2 = new JTabbedPaneOperator(wino, "Table");
	    JTabbedPaneOperator tp3 = new JTabbedPaneOperator(wino, "Tree", 1, 0);
	    if(tp1.getSource() != tpo.getSource() ||
	       tp2.getSource() != tpo.getSource() ||
	       tp3.getSource() != tpo.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(tpo.getSource().toString());
		getOutput().printErrLine(tp1.getSource().toString());
		getOutput().printErrLine(tp2.getSource().toString());
		getOutput().printErrLine(tp3.getSource().toString());
		finalize();
		return(1);
	    }


	    Demonstrator.setTitle("jemmy_024 test");

	    JTableOperator tabOper = new JTableOperator(JTableOperator.
							findJTable(win, null, false, false, -1, -1));
	    
	    tabOper.clickOnCell(0, 0);
	    JTableOperator tbl1 = new JTableOperator(wino);
	    JTableOperator tbl2 = new JTableOperator(wino, "00");
	    JTableOperator tbl3 = new JTableOperator(wino, "4949", 49, 49);
	    if(tbl1.getSource() != tabOper.getSource() ||
	       tbl2.getSource() != tabOper.getSource() ||
	       tbl3.getSource() != tabOper.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(tabOper.getSource().toString());
		getOutput().printErrLine(tbl1.getSource().toString());
		getOutput().printErrLine(tbl2.getSource().toString());
		getOutput().printErrLine(tbl3.getSource().toString());
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Change text of the \"4949\" cell to \"-1-1\"");

	    tabOper.changeCellObject(49, 49, "-1-1");
	    tabOper.waitCell("-1-1", 49, 49);

	    if(!checkInside(tabOper, 
			    tabOper.getSource().getWidth() - 10, 
			    tabOper.getSource().getHeight() - 10,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		return(1);
	    }

	    if(!tabOper.getValueAt(49, 49).toString().equals("-1-1")) {
		getOutput().printErrLine("Wrong text in the (49, 49) cell: " + tabOper.getValueAt(49, 49).toString());
		getOutput().printErrLine("Expected                       : " + "-1-1");
		finalize();
		return(1);
	    }


	    Demonstrator.nextStep("Scroll table to \"2424\" cell");

	    tabOper.scrollToCell(tabOper.findCellRow("2424"),
				 tabOper.findCellColumn("2424"));

	    if(!checkInside(tabOper, 
			    tabOper.getSource().getWidth() / 2, 
			    tabOper.getSource().getHeight() / 2,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }
	    
	    Demonstrator.nextStep("Check 4478876 bugfix: set value of (24,24) cell to null");

	    tabOper.getModel().setValueAt(null, 24, 24);
	    tabOper.getSource().repaint();

	    Demonstrator.nextStep("Scroll table to \"00\" cell");
	    
	    tabOper.scrollToCell(tabOper.findCellRow("00", true, true),
				 tabOper.findCellColumn("00", true, true));

	    if(!checkInside(tabOper, 
			    0, 
			    0,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Check 4478876 bugfix: scroll to cell with \"-1-1\" text");

	    tabOper.scrollToCell(tabOper.findCellRow("-1-1", true, true),
				 tabOper.findCellColumn("-1-1", true, true));

	    Demonstrator.nextStep("Scroll tree to \"49\" node");

	    tpo.selectPage("Tree Page", true, true);

	    JTreeOperator treeOper = new JTreeOperator(new JFrameOperator(win));

	    treeOper.clickOnPath(treeOper.getPathForRow(0));
	    JTreeOperator tro1 = new JTreeOperator(wino);
	    JTreeOperator tro2 = new JTreeOperator(wino, "-1");
	    JTreeOperator tro3 = new JTreeOperator(wino, "49", 50, 0);
	    if(tro1.getSource() != treeOper.getSource() ||
	       tro2.getSource() != treeOper.getSource() ||
	       tro3.getSource() != treeOper.getSource()) {
		getOutput().printError("Wrong");
		getOutput().printErrLine(treeOper.getSource().toString());
		getOutput().printErrLine(tro1.getSource().toString());
		getOutput().printErrLine(tro2.getSource().toString());
		getOutput().printErrLine(tro3.getSource().toString());
		finalize();
		return(1);
	    }

	    TreePath path49 = treeOper.findPath("49", "/", true, true);
	    treeOper.scrollToPath(path49);

	    if(!checkInside(treeOper, 
			    treeOper.getSource().getWidth() - 10, 
			    treeOper.getSource().getHeight() - 10,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Expand \"49\" node");

	    treeOper.doExpandPath(path49);

	    Demonstrator.nextStep("Change \"49/4949\" path text to \"-1-1\"");

	    TreePath path4949 = treeOper.findPath("49/4949", "/", true, true);
	    treeOper.waitRow("4949", 100);

	    treeOper.changePathObject(path4949, "-1-1");

	    TreePath path_1_1 = treeOper.findPath("49/-1-1", "/", true, true);

	    treeOper.scrollToPath(path_1_1);

	    if(!checkInside(treeOper, 
			    treeOper.getSource().getWidth() - 10, 
			    treeOper.getSource().getHeight() - 10,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Scroll tree to \"-1\" node");

	    TreePath path0 = treeOper.findPath("", "/", true, true);
	    treeOper.scrollToPath(path0);

	    if(!checkInside(treeOper, 
			    0, 
			    0,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Scroll list to \"49\" item");

	    tpo.selectPage("List Page", true, true);

	    JListOperator listOper = new JListOperator(new JFrameOperator(win));
	    listOper.scrollToItem(49);

	    if(!checkInside(listOper, 
			    listOper.getSource().getWidth() - 10, 
			    listOper.getSource().getHeight() - 10,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Scroll list to \"0\" item");

	    listOper.scrollToItem(0);

	    if(!checkInside(listOper, 
			    0, 
			    0,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Scroll text to end");

	    tpo.selectPage("Text Page", true, true);


	    JTextAreaOperator textOper = new JTextAreaOperator(wino);

	    textOper.scrollToPosition(textOper.getText().length());

	    if(!checkInside(textOper, 
			    textOper.getSource().getWidth() - 10, 
			    textOper.getSource().getHeight() - 10,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Scroll text to start");

	    textOper.scrollToPosition(0);

	    if(!checkInside(textOper, 
			    0, 
			    0,
			    10, 
			    10)) {
		getOutput().printErrLine("Was not scrolled!");
		finalize();
		return(1);
	    }

	    Demonstrator.nextStep("Clear text");

	    textOper.getTimeouts().setTimeout("JTextComponentOperator.ChangeCaretPositionTimeout",
					      300000);

	    textOper.clearText();

	    if(textOper.getText().length() > 0) {
		getOutput().printErrLine("Was not cleared!");
		finalize();
		return(1);
	    }

	    Demonstrator.showFinalComment("Test passed");

	    if(!testJTree(treeOper)) {
		finalize();
		return(1);
	    }

	    if(!testJTable(tabOper)) {
		finalize();
		return(1);
	    }

	    time = System.currentTimeMillis() - time;
	    getOutput().printLine("TOTAL TIME = " + time);

	} catch(Exception e) {
	    finalize();
	    throw(new TestCompletedException(1, e));
	}

	finalize();

	return(0);
    }

public boolean testJTable(JTableOperator jTableOperator) {
    if(((JTable)jTableOperator.getSource()).getAutoCreateColumnsFromModel() == jTableOperator.getAutoCreateColumnsFromModel()) {
        printLine("getAutoCreateColumnsFromModel does work");
    } else {
        printLine("getAutoCreateColumnsFromModel does not work");
        printLine(((JTable)jTableOperator.getSource()).getAutoCreateColumnsFromModel());
        printLine(jTableOperator.getAutoCreateColumnsFromModel());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getAutoResizeMode() == jTableOperator.getAutoResizeMode()) {
        printLine("getAutoResizeMode does work");
    } else {
        printLine("getAutoResizeMode does not work");
        printLine(((JTable)jTableOperator.getSource()).getAutoResizeMode());
        printLine(jTableOperator.getAutoResizeMode());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getCellEditor() == null &&
       jTableOperator.getCellEditor() == null ||
       ((JTable)jTableOperator.getSource()).getCellEditor().equals(jTableOperator.getCellEditor())) {
        printLine("getCellEditor does work");
    } else {
        printLine("getCellEditor does not work");
        printLine(((JTable)jTableOperator.getSource()).getCellEditor());
        printLine(jTableOperator.getCellEditor());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getCellSelectionEnabled() == jTableOperator.getCellSelectionEnabled()) {
        printLine("getCellSelectionEnabled does work");
    } else {
        printLine("getCellSelectionEnabled does not work");
        printLine(((JTable)jTableOperator.getSource()).getCellSelectionEnabled());
        printLine(jTableOperator.getCellSelectionEnabled());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getColumnCount() == jTableOperator.getColumnCount()) {
        printLine("getColumnCount does work");
    } else {
        printLine("getColumnCount does not work");
        printLine(((JTable)jTableOperator.getSource()).getColumnCount());
        printLine(jTableOperator.getColumnCount());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getColumnModel() == null &&
       jTableOperator.getColumnModel() == null ||
       ((JTable)jTableOperator.getSource()).getColumnModel().equals(jTableOperator.getColumnModel())) {
        printLine("getColumnModel does work");
    } else {
        printLine("getColumnModel does not work");
        printLine(((JTable)jTableOperator.getSource()).getColumnModel());
        printLine(jTableOperator.getColumnModel());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getColumnSelectionAllowed() == jTableOperator.getColumnSelectionAllowed()) {
        printLine("getColumnSelectionAllowed does work");
    } else {
        printLine("getColumnSelectionAllowed does not work");
        printLine(((JTable)jTableOperator.getSource()).getColumnSelectionAllowed());
        printLine(jTableOperator.getColumnSelectionAllowed());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getEditingColumn() == jTableOperator.getEditingColumn()) {
        printLine("getEditingColumn does work");
    } else {
        printLine("getEditingColumn does not work");
        printLine(((JTable)jTableOperator.getSource()).getEditingColumn());
        printLine(jTableOperator.getEditingColumn());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getEditingRow() == jTableOperator.getEditingRow()) {
        printLine("getEditingRow does work");
    } else {
        printLine("getEditingRow does not work");
        printLine(((JTable)jTableOperator.getSource()).getEditingRow());
        printLine(jTableOperator.getEditingRow());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getEditorComponent() == null &&
       jTableOperator.getEditorComponent() == null ||
       ((JTable)jTableOperator.getSource()).getEditorComponent().equals(jTableOperator.getEditorComponent())) {
        printLine("getEditorComponent does work");
    } else {
        printLine("getEditorComponent does not work");
        printLine(((JTable)jTableOperator.getSource()).getEditorComponent());
        printLine(jTableOperator.getEditorComponent());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getGridColor() == null &&
       jTableOperator.getGridColor() == null ||
       ((JTable)jTableOperator.getSource()).getGridColor().equals(jTableOperator.getGridColor())) {
        printLine("getGridColor does work");
    } else {
        printLine("getGridColor does not work");
        printLine(((JTable)jTableOperator.getSource()).getGridColor());
        printLine(jTableOperator.getGridColor());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getIntercellSpacing() == null &&
       jTableOperator.getIntercellSpacing() == null ||
       ((JTable)jTableOperator.getSource()).getIntercellSpacing().equals(jTableOperator.getIntercellSpacing())) {
        printLine("getIntercellSpacing does work");
    } else {
        printLine("getIntercellSpacing does not work");
        printLine(((JTable)jTableOperator.getSource()).getIntercellSpacing());
        printLine(jTableOperator.getIntercellSpacing());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getModel() == null &&
       jTableOperator.getModel() == null ||
       ((JTable)jTableOperator.getSource()).getModel().equals(jTableOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JTable)jTableOperator.getSource()).getModel());
        printLine(jTableOperator.getModel());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getPreferredScrollableViewportSize() == null &&
       jTableOperator.getPreferredScrollableViewportSize() == null ||
       ((JTable)jTableOperator.getSource()).getPreferredScrollableViewportSize().equals(jTableOperator.getPreferredScrollableViewportSize())) {
        printLine("getPreferredScrollableViewportSize does work");
    } else {
        printLine("getPreferredScrollableViewportSize does not work");
        printLine(((JTable)jTableOperator.getSource()).getPreferredScrollableViewportSize());
        printLine(jTableOperator.getPreferredScrollableViewportSize());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getRowCount() == jTableOperator.getRowCount()) {
        printLine("getRowCount does work");
    } else {
        printLine("getRowCount does not work");
        printLine(((JTable)jTableOperator.getSource()).getRowCount());
        printLine(jTableOperator.getRowCount());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getRowHeight() == jTableOperator.getRowHeight()) {
        printLine("getRowHeight does work");
    } else {
        printLine("getRowHeight does not work");
        printLine(((JTable)jTableOperator.getSource()).getRowHeight());
        printLine(jTableOperator.getRowHeight());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getRowMargin() == jTableOperator.getRowMargin()) {
        printLine("getRowMargin does work");
    } else {
        printLine("getRowMargin does not work");
        printLine(((JTable)jTableOperator.getSource()).getRowMargin());
        printLine(jTableOperator.getRowMargin());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getRowSelectionAllowed() == jTableOperator.getRowSelectionAllowed()) {
        printLine("getRowSelectionAllowed does work");
    } else {
        printLine("getRowSelectionAllowed does not work");
        printLine(((JTable)jTableOperator.getSource()).getRowSelectionAllowed());
        printLine(jTableOperator.getRowSelectionAllowed());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getScrollableTracksViewportHeight() == jTableOperator.getScrollableTracksViewportHeight()) {
        printLine("getScrollableTracksViewportHeight does work");
    } else {
        printLine("getScrollableTracksViewportHeight does not work");
        printLine(((JTable)jTableOperator.getSource()).getScrollableTracksViewportHeight());
        printLine(jTableOperator.getScrollableTracksViewportHeight());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getScrollableTracksViewportWidth() == jTableOperator.getScrollableTracksViewportWidth()) {
        printLine("getScrollableTracksViewportWidth does work");
    } else {
        printLine("getScrollableTracksViewportWidth does not work");
        printLine(((JTable)jTableOperator.getSource()).getScrollableTracksViewportWidth());
        printLine(jTableOperator.getScrollableTracksViewportWidth());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectedColumn() == jTableOperator.getSelectedColumn()) {
        printLine("getSelectedColumn does work");
    } else {
        printLine("getSelectedColumn does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectedColumn());
        printLine(jTableOperator.getSelectedColumn());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectedColumnCount() == jTableOperator.getSelectedColumnCount()) {
        printLine("getSelectedColumnCount does work");
    } else {
        printLine("getSelectedColumnCount does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectedColumnCount());
        printLine(jTableOperator.getSelectedColumnCount());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectedRow() == jTableOperator.getSelectedRow()) {
        printLine("getSelectedRow does work");
    } else {
        printLine("getSelectedRow does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectedRow());
        printLine(jTableOperator.getSelectedRow());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectedRowCount() == jTableOperator.getSelectedRowCount()) {
        printLine("getSelectedRowCount does work");
    } else {
        printLine("getSelectedRowCount does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectedRowCount());
        printLine(jTableOperator.getSelectedRowCount());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectionBackground() == null &&
       jTableOperator.getSelectionBackground() == null ||
       ((JTable)jTableOperator.getSource()).getSelectionBackground().equals(jTableOperator.getSelectionBackground())) {
        printLine("getSelectionBackground does work");
    } else {
        printLine("getSelectionBackground does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectionBackground());
        printLine(jTableOperator.getSelectionBackground());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectionForeground() == null &&
       jTableOperator.getSelectionForeground() == null ||
       ((JTable)jTableOperator.getSource()).getSelectionForeground().equals(jTableOperator.getSelectionForeground())) {
        printLine("getSelectionForeground does work");
    } else {
        printLine("getSelectionForeground does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectionForeground());
        printLine(jTableOperator.getSelectionForeground());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getSelectionModel() == null &&
       jTableOperator.getSelectionModel() == null ||
       ((JTable)jTableOperator.getSource()).getSelectionModel().equals(jTableOperator.getSelectionModel())) {
        printLine("getSelectionModel does work");
    } else {
        printLine("getSelectionModel does not work");
        printLine(((JTable)jTableOperator.getSource()).getSelectionModel());
        printLine(jTableOperator.getSelectionModel());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getShowHorizontalLines() == jTableOperator.getShowHorizontalLines()) {
        printLine("getShowHorizontalLines does work");
    } else {
        printLine("getShowHorizontalLines does not work");
        printLine(((JTable)jTableOperator.getSource()).getShowHorizontalLines());
        printLine(jTableOperator.getShowHorizontalLines());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getShowVerticalLines() == jTableOperator.getShowVerticalLines()) {
        printLine("getShowVerticalLines does work");
    } else {
        printLine("getShowVerticalLines does not work");
        printLine(((JTable)jTableOperator.getSource()).getShowVerticalLines());
        printLine(jTableOperator.getShowVerticalLines());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getTableHeader() == null &&
       jTableOperator.getTableHeader() == null ||
       ((JTable)jTableOperator.getSource()).getTableHeader().equals(jTableOperator.getTableHeader())) {
        printLine("getTableHeader does work");
    } else {
        printLine("getTableHeader does not work");
        printLine(((JTable)jTableOperator.getSource()).getTableHeader());
        printLine(jTableOperator.getTableHeader());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).getUI() == null &&
       jTableOperator.getUI() == null ||
       ((JTable)jTableOperator.getSource()).getUI().equals(jTableOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JTable)jTableOperator.getSource()).getUI());
        printLine(jTableOperator.getUI());
        return(false);
    }
    if(((JTable)jTableOperator.getSource()).isEditing() == jTableOperator.isEditing()) {
        printLine("isEditing does work");
    } else {
        printLine("isEditing does not work");
        printLine(((JTable)jTableOperator.getSource()).isEditing());
        printLine(jTableOperator.isEditing());
        return(false);
    }
    return(true);
}

public boolean testJTree(JTreeOperator jTreeOperator) {
    if(((JTree)jTreeOperator.getSource()).getCellEditor() == null &&
       jTreeOperator.getCellEditor() == null ||
       ((JTree)jTreeOperator.getSource()).getCellEditor().equals(jTreeOperator.getCellEditor())) {
        printLine("getCellEditor does work");
    } else {
        printLine("getCellEditor does not work");
        printLine(((JTree)jTreeOperator.getSource()).getCellEditor());
        printLine(jTreeOperator.getCellEditor());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getCellRenderer() == null &&
       jTreeOperator.getCellRenderer() == null ||
       ((JTree)jTreeOperator.getSource()).getCellRenderer().equals(jTreeOperator.getCellRenderer())) {
        printLine("getCellRenderer does work");
    } else {
        printLine("getCellRenderer does not work");
        printLine(((JTree)jTreeOperator.getSource()).getCellRenderer());
        printLine(jTreeOperator.getCellRenderer());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getEditingPath() == null &&
       jTreeOperator.getEditingPath() == null ||
       ((JTree)jTreeOperator.getSource()).getEditingPath().equals(jTreeOperator.getEditingPath())) {
        printLine("getEditingPath does work");
    } else {
        printLine("getEditingPath does not work");
        printLine(((JTree)jTreeOperator.getSource()).getEditingPath());
        printLine(jTreeOperator.getEditingPath());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getInvokesStopCellEditing() == jTreeOperator.getInvokesStopCellEditing()) {
        printLine("getInvokesStopCellEditing does work");
    } else {
        printLine("getInvokesStopCellEditing does not work");
        printLine(((JTree)jTreeOperator.getSource()).getInvokesStopCellEditing());
        printLine(jTreeOperator.getInvokesStopCellEditing());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getLastSelectedPathComponent() == null &&
       jTreeOperator.getLastSelectedPathComponent() == null ||
       ((JTree)jTreeOperator.getSource()).getLastSelectedPathComponent().equals(jTreeOperator.getLastSelectedPathComponent())) {
        printLine("getLastSelectedPathComponent does work");
    } else {
        printLine("getLastSelectedPathComponent does not work");
        printLine(((JTree)jTreeOperator.getSource()).getLastSelectedPathComponent());
        printLine(jTreeOperator.getLastSelectedPathComponent());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getLeadSelectionPath() == null &&
       jTreeOperator.getLeadSelectionPath() == null ||
       ((JTree)jTreeOperator.getSource()).getLeadSelectionPath().equals(jTreeOperator.getLeadSelectionPath())) {
        printLine("getLeadSelectionPath does work");
    } else {
        printLine("getLeadSelectionPath does not work");
        printLine(((JTree)jTreeOperator.getSource()).getLeadSelectionPath());
        printLine(jTreeOperator.getLeadSelectionPath());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getLeadSelectionRow() == jTreeOperator.getLeadSelectionRow()) {
        printLine("getLeadSelectionRow does work");
    } else {
        printLine("getLeadSelectionRow does not work");
        printLine(((JTree)jTreeOperator.getSource()).getLeadSelectionRow());
        printLine(jTreeOperator.getLeadSelectionRow());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getMaxSelectionRow() == jTreeOperator.getMaxSelectionRow()) {
        printLine("getMaxSelectionRow does work");
    } else {
        printLine("getMaxSelectionRow does not work");
        printLine(((JTree)jTreeOperator.getSource()).getMaxSelectionRow());
        printLine(jTreeOperator.getMaxSelectionRow());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getMinSelectionRow() == jTreeOperator.getMinSelectionRow()) {
        printLine("getMinSelectionRow does work");
    } else {
        printLine("getMinSelectionRow does not work");
        printLine(((JTree)jTreeOperator.getSource()).getMinSelectionRow());
        printLine(jTreeOperator.getMinSelectionRow());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getModel() == null &&
       jTreeOperator.getModel() == null ||
       ((JTree)jTreeOperator.getSource()).getModel().equals(jTreeOperator.getModel())) {
        printLine("getModel does work");
    } else {
        printLine("getModel does not work");
        printLine(((JTree)jTreeOperator.getSource()).getModel());
        printLine(jTreeOperator.getModel());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getPreferredScrollableViewportSize() == null &&
       jTreeOperator.getPreferredScrollableViewportSize() == null ||
       ((JTree)jTreeOperator.getSource()).getPreferredScrollableViewportSize().equals(jTreeOperator.getPreferredScrollableViewportSize())) {
        printLine("getPreferredScrollableViewportSize does work");
    } else {
        printLine("getPreferredScrollableViewportSize does not work");
        printLine(((JTree)jTreeOperator.getSource()).getPreferredScrollableViewportSize());
        printLine(jTreeOperator.getPreferredScrollableViewportSize());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getRowCount() == jTreeOperator.getRowCount()) {
        printLine("getRowCount does work");
    } else {
        printLine("getRowCount does not work");
        printLine(((JTree)jTreeOperator.getSource()).getRowCount());
        printLine(jTreeOperator.getRowCount());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getRowHeight() == jTreeOperator.getRowHeight()) {
        printLine("getRowHeight does work");
    } else {
        printLine("getRowHeight does not work");
        printLine(((JTree)jTreeOperator.getSource()).getRowHeight());
        printLine(jTreeOperator.getRowHeight());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getScrollableTracksViewportHeight() == jTreeOperator.getScrollableTracksViewportHeight()) {
        printLine("getScrollableTracksViewportHeight does work");
    } else {
        printLine("getScrollableTracksViewportHeight does not work");
        printLine(((JTree)jTreeOperator.getSource()).getScrollableTracksViewportHeight());
        printLine(jTreeOperator.getScrollableTracksViewportHeight());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getScrollableTracksViewportWidth() == jTreeOperator.getScrollableTracksViewportWidth()) {
        printLine("getScrollableTracksViewportWidth does work");
    } else {
        printLine("getScrollableTracksViewportWidth does not work");
        printLine(((JTree)jTreeOperator.getSource()).getScrollableTracksViewportWidth());
        printLine(jTreeOperator.getScrollableTracksViewportWidth());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getScrollsOnExpand() == jTreeOperator.getScrollsOnExpand()) {
        printLine("getScrollsOnExpand does work");
    } else {
        printLine("getScrollsOnExpand does not work");
        printLine(((JTree)jTreeOperator.getSource()).getScrollsOnExpand());
        printLine(jTreeOperator.getScrollsOnExpand());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getSelectionCount() == jTreeOperator.getSelectionCount()) {
        printLine("getSelectionCount does work");
    } else {
        printLine("getSelectionCount does not work");
        printLine(((JTree)jTreeOperator.getSource()).getSelectionCount());
        printLine(jTreeOperator.getSelectionCount());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getSelectionModel() == null &&
       jTreeOperator.getSelectionModel() == null ||
       ((JTree)jTreeOperator.getSource()).getSelectionModel().equals(jTreeOperator.getSelectionModel())) {
        printLine("getSelectionModel does work");
    } else {
        printLine("getSelectionModel does not work");
        printLine(((JTree)jTreeOperator.getSource()).getSelectionModel());
        printLine(jTreeOperator.getSelectionModel());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getSelectionPath() == null &&
       jTreeOperator.getSelectionPath() == null ||
       ((JTree)jTreeOperator.getSource()).getSelectionPath().equals(jTreeOperator.getSelectionPath())) {
        printLine("getSelectionPath does work");
    } else {
        printLine("getSelectionPath does not work");
        printLine(((JTree)jTreeOperator.getSource()).getSelectionPath());
        printLine(jTreeOperator.getSelectionPath());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getShowsRootHandles() == jTreeOperator.getShowsRootHandles()) {
        printLine("getShowsRootHandles does work");
    } else {
        printLine("getShowsRootHandles does not work");
        printLine(((JTree)jTreeOperator.getSource()).getShowsRootHandles());
        printLine(jTreeOperator.getShowsRootHandles());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getUI() == null &&
       jTreeOperator.getUI() == null ||
       ((JTree)jTreeOperator.getSource()).getUI().equals(jTreeOperator.getUI())) {
        printLine("getUI does work");
    } else {
        printLine("getUI does not work");
        printLine(((JTree)jTreeOperator.getSource()).getUI());
        printLine(jTreeOperator.getUI());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).getVisibleRowCount() == jTreeOperator.getVisibleRowCount()) {
        printLine("getVisibleRowCount does work");
    } else {
        printLine("getVisibleRowCount does not work");
        printLine(((JTree)jTreeOperator.getSource()).getVisibleRowCount());
        printLine(jTreeOperator.getVisibleRowCount());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isEditable() == jTreeOperator.isEditable()) {
        printLine("isEditable does work");
    } else {
        printLine("isEditable does not work");
        printLine(((JTree)jTreeOperator.getSource()).isEditable());
        printLine(jTreeOperator.isEditable());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isEditing() == jTreeOperator.isEditing()) {
        printLine("isEditing does work");
    } else {
        printLine("isEditing does not work");
        printLine(((JTree)jTreeOperator.getSource()).isEditing());
        printLine(jTreeOperator.isEditing());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isFixedRowHeight() == jTreeOperator.isFixedRowHeight()) {
        printLine("isFixedRowHeight does work");
    } else {
        printLine("isFixedRowHeight does not work");
        printLine(((JTree)jTreeOperator.getSource()).isFixedRowHeight());
        printLine(jTreeOperator.isFixedRowHeight());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isLargeModel() == jTreeOperator.isLargeModel()) {
        printLine("isLargeModel does work");
    } else {
        printLine("isLargeModel does not work");
        printLine(((JTree)jTreeOperator.getSource()).isLargeModel());
        printLine(jTreeOperator.isLargeModel());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isRootVisible() == jTreeOperator.isRootVisible()) {
        printLine("isRootVisible does work");
    } else {
        printLine("isRootVisible does not work");
        printLine(((JTree)jTreeOperator.getSource()).isRootVisible());
        printLine(jTreeOperator.isRootVisible());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).isSelectionEmpty() == jTreeOperator.isSelectionEmpty()) {
        printLine("isSelectionEmpty does work");
    } else {
        printLine("isSelectionEmpty does not work");
        printLine(((JTree)jTreeOperator.getSource()).isSelectionEmpty());
        printLine(jTreeOperator.isSelectionEmpty());
        return(false);
    }
    if(((JTree)jTreeOperator.getSource()).stopEditing() == jTreeOperator.stopEditing()) {
        printLine("stopEditing does work");
    } else {
        printLine("stopEditing does not work");
        printLine(((JTree)jTreeOperator.getSource()).stopEditing());
        printLine(jTreeOperator.stopEditing());
        return(false);
    }
    return(true);
}

}
