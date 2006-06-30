/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.operators;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JTable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;

import javax.swing.plaf.TableHeaderUI;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.Timeouts;

import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.OrderedListDriver;

/**
 * ComponentOperator.BeforeDragTimeout - time to sleep before column moving <BR>
 * ComponentOperator.AfterDragTimeout - time to sleep after column moving <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait component displayed <BR>.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JTableHeaderOperator extends JComponentOperator
implements Outputable, Timeoutable {

    private TestOut output;
    private Timeouts timeouts;

    private OrderedListDriver driver;

    /**
     * Constructor.
     * @param b a component
     */
    public JTableHeaderOperator(JTableHeader b) {
	super(b);
        driver = DriverManager.getOrderedListDriver(getClass());
    }

    /**
     * Constructs a JTableHeaderOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     * @param index an index between appropriate ones.
     */
    public JTableHeaderOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JTableHeader)cont.
             waitSubComponent(new JTableHeaderFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JTableHeaderOperator object.
     * @param cont a container
     * @param chooser a component chooser specifying searching criteria.
     */
    public JTableHeaderOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructs a JTableHeaderOperator object.
     * @param cont a container
     * @param index an index between appropriate ones.
     */
    public JTableHeaderOperator(ContainerOperator cont, int index) {
	this((JTableHeader)
	     waitComponent(cont, 
			   new JTableHeaderFinder(ComponentSearcher.
					    getTrueChooser("Any JTableHeader")),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructs a JTableHeaderOperator object.
     * @param cont a container
     */
    public JTableHeaderOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    public void setTimeouts(Timeouts times) {
	this.timeouts = times;
	super.setTimeouts(timeouts);
    }

    public Timeouts getTimeouts() {
	return(timeouts);
    }

    public void setOutput(TestOut out) {
	output = out;
	super.setOutput(output);
    }

    public TestOut getOutput() {
	return(output);
    }

    /**
     * Selects a column.
     * @param columnIndex an index of a column to select.
     */
    public void selectColumn(int columnIndex) {
        driver.selectItem(this, columnIndex);
    }

    /**
     * Selects some columns.
     * @param columnIndices indices of columns to select.
     */
    public void selectColumns(int[] columnIndices) {
        driver.selectItems(this, columnIndices);
    }

    /**
     * Moves a column to a different location.
     * @param moveColumn an original column index.
     * @param moveTo a desctination column index.
     */
    public void moveColumn(int moveColumn, int moveTo) {
        driver.moveItem(this, moveColumn, moveTo);
    }

    /**
     * Return a point to click on column header.
     * @param columnIndex an index of a column to click on.
     * @return the point to click.
     */
    public Point getPointToClick(int columnIndex) {
        Rectangle rect = getHeaderRect(columnIndex);
        return(new Point(rect.x + rect.width/2,
                         rect.y + rect.height/2));
    }

    public void copyEnvironment(Operator anotherOperator) {
	super.copyEnvironment(anotherOperator);
	driver = 
	    (OrderedListDriver)DriverManager.
	    getDriver(DriverManager.ORDEREDLIST_DRIVER_ID,
		      getClass(), 
		      anotherOperator.getProperties());
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JTableHeader.setTable(JTable)</code> through queue*/
    public void setTable(final JTable jTable) {
	runMapping(new MapVoidAction("setTable") {
		public void map() {
		    ((JTableHeader)getSource()).setTable(jTable);
		}});}

    /**Maps <code>JTableHeader.getTable()</code> through queue*/
    public JTable getTable() {
	return((JTable)runMapping(new MapAction("getTable") {
		public Object map() {
		    return(((JTableHeader)getSource()).getTable());
		}}));}

    /**Maps <code>JTableHeader.setReorderingAllowed(boolean)</code> through queue*/
    public void setReorderingAllowed(final boolean b) {
	runMapping(new MapVoidAction("setReorderingAllowed") {
		public void map() {
		    ((JTableHeader)getSource()).setReorderingAllowed(b);
		}});}

    /**Maps <code>JTableHeader.getReorderingAllowed()</code> through queue*/
    public boolean getReorderingAllowed() {
	return(runMapping(new MapBooleanAction("getReorderingAllowed") {
		public boolean map() {
		    return(((JTableHeader)getSource()).getReorderingAllowed());
		}}));}

    /**Maps <code>JTableHeader.setResizingAllowed(boolean)</code> through queue*/
    public void setResizingAllowed(final boolean b) {
	runMapping(new MapVoidAction("setResizingAllowed") {
		public void map() {
		    ((JTableHeader)getSource()).setResizingAllowed(b);
		}});}

    /**Maps <code>JTableHeader.getResizingAllowed()</code> through queue*/
    public boolean getResizingAllowed() {
	return(runMapping(new MapBooleanAction("getResizingAllowed") {
		public boolean map() {
		    return(((JTableHeader)getSource()).getResizingAllowed());
		}}));}

    /**Maps <code>JTableHeader.getDraggedColumn()</code> through queue*/
    public TableColumn getDraggedColumn() {
	return((TableColumn)runMapping(new MapAction("getDraggedColumn") {
		public Object map() {
		    return(((JTableHeader)getSource()).getDraggedColumn());
		}}));}

    /**Maps <code>JTableHeader.getDraggedDistance()</code> through queue*/
    public int getDraggedDistance() {
	return(runMapping(new MapIntegerAction("getDraggedDistance") {
		public int map() {
		    return(((JTableHeader)getSource()).getDraggedDistance());
		}}));}

    /**Maps <code>JTableHeader.getResizingColumn()</code> through queue*/
    public TableColumn getResizingColumn() {
	return((TableColumn)runMapping(new MapAction("getResizingColumn") {
		public Object map() {
		    return(((JTableHeader)getSource()).getResizingColumn());
		}}));}

    /**Maps <code>JTableHeader.setUpdateTableInRealTime(boolean)</code> through queue*/
    public void setUpdateTableInRealTime(final boolean b) {
	runMapping(new MapVoidAction("setUpdateTableInRealTime") {
		public void map() {
		    ((JTableHeader)getSource()).setUpdateTableInRealTime(b);
		}});}

    /**Maps <code>JTableHeader.getUpdateTableInRealTime()</code> through queue*/
    public boolean getUpdateTableInRealTime() {
	return(runMapping(new MapBooleanAction("getUpdateTableInRealTime") {
		public boolean map() {
		    return(((JTableHeader)getSource()).getUpdateTableInRealTime());
		}}));}

    /**Maps <code>JTableHeader.setDefaultRenderer(TableCellRenderer)</code> through queue*/
    public void setDefaultRenderer(final TableCellRenderer tableCellRenderer) {
	runMapping(new MapVoidAction("setDefaultRenderer") {
		public void map() {
		    ((JTableHeader)getSource()).setDefaultRenderer(tableCellRenderer);
		}});}

    /**Maps <code>JTableHeader.getDefaultRenderer()</code> through queue*/
    public TableCellRenderer getDefaultRenderer() {
	return((TableCellRenderer)runMapping(new MapAction("getDefaultRenderer") {
		public Object map() {
		    return(((JTableHeader)getSource()).getDefaultRenderer());
		}}));}

    /**Maps <code>JTableHeader.columnAtPoint(Point)</code> through queue*/
    public int columnAtPoint(final Point point) {
	return(runMapping(new MapIntegerAction("columnAtPoint") {
		public int map() {
		    return(((JTableHeader)getSource()).columnAtPoint(point));
		}}));}

    /**Maps <code>JTableHeader.getHeaderRect(int)</code> through queue*/
    public Rectangle getHeaderRect(final int i) {
	return((Rectangle)runMapping(new MapAction("getHeaderRect") {
		public Object map() {
		    return(((JTableHeader)getSource()).getHeaderRect(i));
		}}));}

    /**Maps <code>JTableHeader.getUI()</code> through queue*/
    public TableHeaderUI getUI() {
	return((TableHeaderUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JTableHeader)getSource()).getUI());
		}}));}

    /**Maps <code>JTableHeader.setUI(TableHeaderUI)</code> through queue*/
    public void setUI(final TableHeaderUI tableHeaderUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JTableHeader)getSource()).setUI(tableHeaderUI);
		}});}

    /**Maps <code>JTableHeader.setColumnModel(TableColumnModel)</code> through queue*/
    public void setColumnModel(final TableColumnModel tableColumnModel) {
	runMapping(new MapVoidAction("setColumnModel") {
		public void map() {
		    ((JTableHeader)getSource()).setColumnModel(tableColumnModel);
		}});}

    /**Maps <code>JTableHeader.getColumnModel()</code> through queue*/
    public TableColumnModel getColumnModel() {
	return((TableColumnModel)runMapping(new MapAction("getColumnModel") {
		public Object map() {
		    return(((JTableHeader)getSource()).getColumnModel());
		}}));}

    /**Maps <code>JTableHeader.columnAdded(TableColumnModelEvent)</code> through queue*/
    public void columnAdded(final TableColumnModelEvent tableColumnModelEvent) {
	runMapping(new MapVoidAction("columnAdded") {
		public void map() {
		    ((JTableHeader)getSource()).columnAdded(tableColumnModelEvent);
		}});}

    /**Maps <code>JTableHeader.columnRemoved(TableColumnModelEvent)</code> through queue*/
    public void columnRemoved(final TableColumnModelEvent tableColumnModelEvent) {
	runMapping(new MapVoidAction("columnRemoved") {
		public void map() {
		    ((JTableHeader)getSource()).columnRemoved(tableColumnModelEvent);
		}});}

    /**Maps <code>JTableHeader.columnMoved(TableColumnModelEvent)</code> through queue*/
    public void columnMoved(final TableColumnModelEvent tableColumnModelEvent) {
	runMapping(new MapVoidAction("columnMoved") {
		public void map() {
		    ((JTableHeader)getSource()).columnMoved(tableColumnModelEvent);
		}});}

    /**Maps <code>JTableHeader.columnMarginChanged(ChangeEvent)</code> through queue*/
    public void columnMarginChanged(final ChangeEvent changeEvent) {
	runMapping(new MapVoidAction("columnMarginChanged") {
		public void map() {
		    ((JTableHeader)getSource()).columnMarginChanged(changeEvent);
		}});}

    /**Maps <code>JTableHeader.columnSelectionChanged(ListSelectionEvent)</code> through queue*/
    public void columnSelectionChanged(final ListSelectionEvent listSelectionEvent) {
	runMapping(new MapVoidAction("columnSelectionChanged") {
		public void map() {
		    ((JTableHeader)getSource()).columnSelectionChanged(listSelectionEvent);
		}});}

    /**Maps <code>JTableHeader.resizeAndRepaint()</code> through queue*/
    public void resizeAndRepaint() {
	runMapping(new MapVoidAction("resizeAndRepaint") {
		public void map() {
		    ((JTableHeader)getSource()).resizeAndRepaint();
		}});}

    /**Maps <code>JTableHeader.setDraggedColumn(TableColumn)</code> through queue*/
    public void setDraggedColumn(final TableColumn tableColumn) {
	runMapping(new MapVoidAction("setDraggedColumn") {
		public void map() {
		    ((JTableHeader)getSource()).setDraggedColumn(tableColumn);
		}});}

    /**Maps <code>JTableHeader.setDraggedDistance(int)</code> through queue*/
    public void setDraggedDistance(final int i) {
	runMapping(new MapVoidAction("setDraggedDistance") {
		public void map() {
		    ((JTableHeader)getSource()).setDraggedDistance(i);
		}});}

    /**Maps <code>JTableHeader.setResizingColumn(TableColumn)</code> through queue*/
    public void setResizingColumn(final TableColumn tableColumn) {
	runMapping(new MapVoidAction("setResizingColumn") {
		public void map() {
		    ((JTableHeader)getSource()).setResizingColumn(tableColumn);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    /**
     * Checks component type.
     */
    public static class JTableHeaderFinder implements ComponentChooser {
	ComponentChooser subFinder;
        /**
         * Constructs JTableHeaderFinder.
         * @param sf other searching criteria.
         */
	public JTableHeaderFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JTableHeader) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
