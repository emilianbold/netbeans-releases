/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * MappingTable.java
 *
 * @author Ana von Klopp
 */
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList; 
import java.util.Iterator;

import javax.swing.BorderFactory;     
import javax.swing.JTable;     
import javax.swing.ListSelectionModel;     
import javax.swing.table.TableColumnModel;     
import javax.swing.table.TableModel;     
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;  

import org.openide.util.NbBundle;

class MappingTable extends JTable {

    private static final boolean debug = false; 

    // Handle resizing for larger fonts
    private boolean fontChanged = true;
    private int margin = 6; 

    private static final long serialVersionUID = 3482048644419079279L;
    
    MappingTable(String filterName, ArrayList filterMappings) { 

	super(); 
	if(debug) log("::Constructor"); //NOI18N
	if(debug) log("\tFilterName is " + filterName); //NOI18N
	this.setModel(new MappingTableModel(filterName, filterMappings)); 

	TableColumnModel tcm = this.getColumnModel();

	// The filter name - this one is never editable 
	tcm.getColumn(0).setPreferredWidth(72);

	// The pattern or servlet that we match to
	// This editor depends on whether the value of the other is
	// URL or Servlet
	tcm.getColumn(1).setPreferredWidth(72);
	this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	setColors(false); 
	setIntercellSpacing(new Dimension(margin, margin));
    }

    ArrayList getFilterMappings() { 
	return ((MappingTableModel)this.getModel()).getFilterMappings(); 
    }

    void setFilterName(String name) { 
	((MappingTableModel)this.getModel()).setFilterName(name);
	this.invalidate(); 
    } 

    void addRow(FilterMappingData fmd) { 
	this.invalidate(); 
	((MappingTableModel)getModel()).addRow(fmd); 
    }

    void addRow(int row, FilterMappingData fmd) { 
	this.invalidate(); 
	((MappingTableModel)getModel()).addRow(row, fmd); 
    }

    void setRow(int row, FilterMappingData fmd) { 
	this.invalidate(); 
	((MappingTableModel)getModel()).setRow(row, fmd);
    } 

    FilterMappingData getRow(int row) { 
	return 	((MappingTableModel)getModel()).getRow(row);
    }

    void moveRowUp(int row) { 
	((MappingTableModel)getModel()).moveRowUp(row); 
	getSelectionModel().setSelectionInterval(row-1, row-1); 
	this.invalidate(); 
    } 
    void moveRowDown(int row) { 
	((MappingTableModel)getModel()).moveRowUp(row+1);
	getSelectionModel().setSelectionInterval(row+1, row+1);  
	this.invalidate(); 
    } 

    void removeRow(int row) { 
	((MappingTableModel)getModel()).removeRow(row); 
	this.invalidate(); 
	return;
    }

    public void setValueAt(Object o, int row, int col) {
	if(debug) log("::setValueAt()"); //NOI18N
	return; 
    } 

    private void setColors(boolean editable) {
	Color bg;
	this.setBorder(BorderFactory.createLoweredBevelBorder());
	if (!editable) { 
	    bg = this.getBackground().darker();
	} else {
	    bg = Color.white;
	}
	this.setBackground(bg);
    }

    void addTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.addTableModelListener(tml);
	}
    }

    void removeTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.removeTableModelListener(tml);
	}
    }
    
    public void setFont(Font f) {
	if(debug) log("::setFont()"); //NOI18N
	fontChanged = true;
	super.setFont(f);
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    public void paint(Graphics g) {
	if(debug) log("::paint()"); //NOI18N

	if (fontChanged) {
	    
	    fontChanged = false; 

	    int height = 0; 
	    if(debug) log("\tGetting font height"); //NOI18N
	    FontMetrics fm = g.getFontMetrics(getFont());
	    height = fm.getHeight() + margin; 
	    if(height > rowHeight) rowHeight = height; 
	    if(debug) log("\trow height is " + rowHeight); //NOI18N
	    //triggers paint, just return afterwards
	    this.setRowHeight(rowHeight);
	    return;
	}
	super.paint(g);
    }

    private void log(String s) {
	System.out.println("MappingTable" + s);  //NOI18N
    }

    class MappingTableModel extends AbstractTableModel { 

	private final String[] colheaders = { 
	    NbBundle.getMessage(MappingTable.class, "LBL_filter_name"),
	    NbBundle.getMessage(MappingTable.class, "LBL_applies_to"),
	};

	private ArrayList filterMappings = null;
	private String filterName; 
	
        private static final long serialVersionUID = 2845252365404044474L;
        
	MappingTableModel(String filterName, ArrayList filterMappings) { 
	    this.filterName = filterName; 
	    this.filterMappings = filterMappings;
	}

	ArrayList getFilterMappings() { 
	    return filterMappings; 
	} 

	void setFilterName(String name) { 
	    Iterator i = filterMappings.iterator(); 
	    FilterMappingData fmd; 
	    while(i.hasNext()) {
		fmd = (FilterMappingData)(i.next()); 
		if(fmd.getName().equals(filterName))
		    fmd.setName(name); 
	    }
	    this.filterName = name; 
	} 

	public int getColumnCount() {
	    return colheaders.length;
	}

	public int getRowCount() {
	    return filterMappings.size(); 
	}

	public String getColumnName(int col) {
	    return colheaders[col];
	}

	public Object getValueAt(int row, int col) {
	    FilterMappingData fmd = (FilterMappingData)(filterMappings.get(row)); 
	    if(col == 0) return fmd.getName(); 
	    else return fmd.getPattern();
	} 

	public Class getColumnClass(int c) {
	    return String.class; 
	}

	public boolean isCellEditable(int row, int col) { 
	    return false; 
	} 
    
	public void setValueAt(Object value, int row, int col) {
	    if(debug) log("::setValueAt()"); //NOI18N
	    return;
	} 

	void addRow(int row, FilterMappingData fmd) {
	    filterMappings.add(row, fmd); 
	}

	void addRow(FilterMappingData fmd) {
	    filterMappings.add(fmd); 
	}

	FilterMappingData getRow(int row) { 
	    return (FilterMappingData)(filterMappings.get(row));
	}
	
	void setRow(int row, FilterMappingData fmd) { 
	    filterMappings.set(row, fmd);
	} 

	void moveRowUp(int row) { 
	    Object o = filterMappings.remove(row); 
	    filterMappings.add(row-1, o); 
	}

	void removeRow(int row) { 
	    filterMappings.remove(row); 
	}

	private void log(String s) { 
	    System.out.println("MappingTableModel" + s); //NOI18N
	} 
    } // MappingTableModel

} // MappingTable
