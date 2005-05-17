/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * DDTable.java
 *
 * @author Ana von Klopp
 * @version
 */
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent; 
import java.awt.event.KeyListener; 

//import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor; 
import javax.swing.BorderFactory;     
import javax.swing.JTable;     
import javax.swing.JTextField;     
import javax.swing.ListSelectionModel;     
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;     
import javax.swing.event.TableModelEvent; 

import org.openide.util.NbBundle;

class DDTable extends JTable implements KeyListener {

    private static final boolean debug = false;

    private String titleKey; 
    private Editable editable;
    private String[] headers; 
    private final static int margin = 6; 

    // Handle resizing for larger fonts
    private boolean fontChanged = true;
    private boolean addedRow = true;
    private int rowHeight = 23; 
    
    private static final long serialVersionUID = -155464225493968935L;
    
    DDTable(String[] headers, String titleKey) {
	this(headers, titleKey, Editable.BOTH); 
    }
    
    DDTable(String[] headers, String titleKey, Editable editable) { 
	
	super(new Object[0][headers.length], headers);

	this.headers = headers; 
	this.titleKey = titleKey; 
	this.editable = editable; 

	setModel(new DDTableModel(headers, editable)); 
	setColors(editable); 
	this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	setIntercellSpacing(new Dimension(margin, margin));
	DefaultCellEditor dce = new DefaultCellEditor(new CellText(this)); 
	dce.setClickCountToStart(1); 
	getColumnModel().getColumn(0).setCellEditor(dce); 
	getColumnModel().getColumn(1).setCellEditor(dce); 
    }

    void setEditable(Editable editable) { 
	this.editable = editable; 
	setColors(editable); 
    } 

    Editable getEditable() { 
	return this.editable; 
    } 

    int addRow(String[] values) { 
	int i = ((DDTableModel)getModel()).addRow(values);  
	if(i == 0) fontChanged = true; 
	addedRow = true; 
	this.invalidate(); 
	return i; 
    }

    void removeRow(int row) {         
        if (isEditing())
            getCellEditor().cancelCellEditing();
        
	((DDTableModel)getModel()).removeRow(row);
	this.invalidate();
        
        int maxSelectedRow = getRowCount() - 1;
        if (getSelectedRow() > maxSelectedRow) {
            if (maxSelectedRow >= 0)
                setRowSelectionInterval(maxSelectedRow, maxSelectedRow);
            else
                clearSelection();
        }
    }

    String getColumnKey(int col) { 
	return headers[col]; 
    }

    private void setColors(Editable editable) {
	Color bg;
	this.setBorder(BorderFactory.createLoweredBevelBorder());
	if (editable == Editable.NEITHER) { 
	    bg = this.getBackground().darker();
	} else {
	    bg = Color.white;
	}
	this.setBackground(bg);
    }

    /**
     * Override the getter for the cell editors, so that customized
     * cell editors will show up.

    public TableCellEditor getCellEditor(int row, int col) {
	TableCellEditor e = super.getCellEditor(row, col); 
	Component c = e.getTableCellEditorComponent(this, 
						    getValueAt(row, col),
						    true, row, col); 
	c.addKeyListener(this); 
	return e; 
    } 
     */     


    /**
     * Override the getter for the cell editors, so that customized
     * cell editors will show up.
     */
    public TableCellRenderer getCellRenderer(int row, int col) {
	return super.getCellRenderer(row, col); 
    }


    // This method is used by the edit button of the InitParamTable
    void setData(String name, String value, int row) { 
	if(getEditingRow() == row) { 
	    int col = getEditingColumn(); 
	    getCellEditor(row, col).cancelCellEditing(); 
	}
	((DDTableModel)getModel()).setData(name, value, row); 
    }

    /**
     * Checks whether the cells are editable 
     */
    public boolean isCellEditable(int row, int col) {
	if(editable == Editable.NEITHER) { return false; } 
	if(editable == Editable.VALUE && col == 0) { return false; } 
	else return true; 
    } 

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    public void paint(Graphics g) {

	if(debug) log("::paint()"); //NOI18N

	if (fontChanged) {
	    
	    if(debug) log("\tFont changed"); 
	    fontChanged = false; 

	    int height = 0; 
	    if(debug) log("\tGetting font height"); //NOI18N
	    FontMetrics fm = g.getFontMetrics(getFont());
	    // Add 2 for button border
	    // height = fm.getHeight() + 2 + margin;
	    height = fm.getHeight() + margin;
	    if(height > rowHeight) rowHeight = height; 

	    if(debug) log("\trow height is " + rowHeight); //NOI18N

	    //triggers paint, just return afterwards
	    this.setRowHeight(rowHeight);
	    return;
	}

	if(addedRow) { 
	    addedRow = false; 
	    if(debug) log("\tAdded row"); 
	    int row = getModel().getRowCount() - 1;
	    this.editCellAt(row, 0); 
	    Component c = getCellEditor(row, 0)
		.getTableCellEditorComponent(this, getValueAt(row, 0), 
					    true, row, 0); 
	    if(c instanceof JTextField) {
		if(debug) log("\tTrying to request focus"); 
		((JTextField)c).requestFocus(); 
	    }
	} 
	super.paint(g);
    }
    
    public void keyPressed(KeyEvent keyEvent) {
	if(debug) log("\tKey pressed");
    }
		
    public void keyReleased(KeyEvent keyEvent) {

	if(debug) log("::keyReleased()"); 

	Object o = keyEvent.getSource(); 
	String s = null; 
	if(o instanceof JTextField) { 
	    if(debug) log("\tFound text field"); 
	    s = ((JTextField)o).getText().trim(); 
	} 
	
	int row = getEditingRow();
	int col = getEditingColumn(); 
	if(debug) log("\trow=" + row + ", col=" + col); 

	setValueAt(s, row, col); 
    }
		
    public void keyTyped (KeyEvent keyEvent) {
	if(debug) log("\tKey typed");
    }

    private void log(String s) {
	System.out.println("DDTable" + s);  //NOI18N
    }


    class DDTableModel extends AbstractTableModel { 
    
	private String[] colheaders = null; 
	private Object[][] data = null;
	private Editable editable; 
	private int numCols; 
	private int numRows=0; 
	
        private static final long serialVersionUID = -5044296029944667379L;
        
	DDTableModel(String[] headers, Editable editable) { 

	    this.colheaders = headers; 
	    this.editable = editable;
	    numCols = colheaders.length; 
	    data = new Object[numRows][numCols];
	}
    
	public String getColumnName(int col) { 
	    String key = "LBL_".concat(colheaders[col]); 
	    return NbBundle.getMessage(DDTable.class, key); 
	}
	
	public int getRowCount() { return data.length; }
	public int getColumnCount() { return numCols; }

	public Object getValueAt(int row, int col) { 
	    return data[row][col];
	}

	public int addRow(String[] values) { 

	    Object[][] data2 = new Object[numRows+1][numCols]; 
	    int i=0, j=0; 

	    if(numRows > 0) { 
		for(j=0; j<numRows; ++j) 
		    data2[j] = data[j]; 
	    }

	    for(i=0; i<values.length; ++i) 
		data2[j][i] = values[i];

	    data = data2; 
	    numRows++;
	    return j; 
	}

	public void removeRow(int row) { 

	    if(debug) { 
		log("::removeRow()"); //NOI18N
		log("row is " + row); //NOI18N
		log("numRows is " + numRows); //NOI18N
	    }

	    Object[][] data2 = new Object[numRows-1][numCols]; 
	    int newRowIndex = 0; 
	    for(int i=0; i<numRows; ++i) { 
		if(debug) log("\tExamining row " + i); //NOI18N 
		if(i==row) continue; 
		if(debug) log("\tKeep this row"); //NOI18N
		data2[newRowIndex]=data[i]; 
		newRowIndex++;
		if(debug) log("\tnewRowIndex is " + newRowIndex); //NOI18N
	    }
	    data = data2; 
	    numRows = --numRows; 
	}

	void setData(String name, String value, int row) { 
	    data[row][0] = name;
	    data[row][1] = value; 
	    fireTableChanged(new TableModelEvent(this, row)); 
	} 

	public void setValueAt(Object value, int row, int col) {

	    if(debug) 
		log("::setValueAt(): value = " + value + //NOI18N
		    " at " + row + ", " + col); //NOI18N

	    data[row][col] = value;
	    
	    if(debug) { 
		for(int i=0; i<data.length; ++i) { 
		    for(int j=0; j<numCols; ++j) { 
			log("\t" + String.valueOf(i) + "," + //NOI18N
			    String.valueOf(j) + ": " + data[i][j]); //NOI18N
		    }
		}
	    } 
	    // Commenting this out since the value is set twice. 
	    fireTableCellUpdated(row, col);
	}

	private void log(String s) { 
	    System.out.println("DDTableModel" + s); //NOI18N
	} 

    } // DDTableModel


    class CellText extends JTextField {
        
        private static final long serialVersionUID = 2674682216176560005L;
        
	public CellText(DDTable table) { 
	    super(); 
	    addKeyListener(table);
	    getAccessibleContext().setAccessibleName(this.getText()); // NOI18N
	    getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DDTable.class, "ACSD_ipcell")); // NOI18N
	} 
    }
}
