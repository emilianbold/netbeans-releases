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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class UnitCategoryTableModel extends AbstractTableModel {
    List<UnitCategory> data = Collections.emptyList ();
    private List<UpdateUnitListener> listeners = new ArrayList<UpdateUnitListener> ();
    private String filter = "";//NOI18N

    
    public static enum Type {
        INSTALLED,
        UPDATE,
        AVAILABLE,
        LOCAL
    }
    
    /** Creates a new instance of CategoryTableModel */
    public UnitCategoryTableModel (List<UnitCategory> categories) {
        this.data = categories;
    }
    
    public void setData (List<UnitCategory> data) {
        this.data = data;
        this.fireTableDataChanged ();
    }
    
    public List<Unit> getExposedUnits () {
        List<Unit> exposedList = new ArrayList<Unit> ();
        for (UnitCategory c : data) {
            exposedList.addAll (c.getUnits());
        }
        return exposedList;
    }
    
    public void setFilter(final String filter) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized(UnitCategoryTableModel.class) {
                    UnitCategoryTableModel.this.filter = filter.toLowerCase();
                }                
                //UnitCategoryTableModel.this.fireTableDataChanged ();
                fireUpdataUnitChange();
            }            
        });        
    }

    private List<UnitCategory> getCategories() {
        return new ArrayList<UnitCategory>(data);
    }
    
    private List<UnitCategory> getVisibleCategories() {
        String filter = getFilter();
        List<UnitCategory> retval = new ArrayList<UnitCategory>();
        for (UnitCategory unitCategory : data) {
            if (unitCategory.isVisible(filter)) {
                retval.add(unitCategory);
            }
        }
        return retval;
    }
    
    public String getFilter() {
        synchronized(UnitCategoryTableModel.class) {
            return this.filter == null ? "" : this.filter;
        }                
    }

        
    public void addUpdateUnitListener (UpdateUnitListener l) {
        listeners.add (l);
    }
    
    public void removeUpdateUnitListener (UpdateUnitListener l) {
        listeners.remove (l);
    }
    
    void fireUpdataUnitChange () {
        assert listeners != null : "UpdateUnitListener found.";
        for (UpdateUnitListener l : listeners) {
            l.updateUnitsChanged ();
        }
    }
    
    void fireButtonsChange () {
        assert listeners != null : "UpdateUnitListener found.";
        for (UpdateUnitListener l : listeners) {
            l.buttonsChanged ();
        }
    }
    
    public abstract Object getValueAt (int row, int col);
    public abstract Class getColumnClass (int c);
    public abstract Type getType ();
    
    public int getRowCount() {
        List<UnitCategory> data = getVisibleCategories();
        int size = data.size ();
        String filter = getFilter();
        for (UnitCategory c : data) {
            if (c.isExpanded () && c.isVisible(filter)) size += getUnits (c).size ();
        }
        
        return size;
    }

    public int getColumnCount () {
        return 5;
    }

    public int getRawItemCount () {
        int size = 0;                
        
        for (UnitCategory c : getCategories()) {
            size += c.getUnits().size();
        }        
        
        return size;
    }
    
    public int getItemCount () {
        int size = 0;                
        List<UnitCategory> data = getVisibleCategories();
        
        for (UnitCategory c : data) {
            size += getUnits(c).size();
        }        
        
        return size;
    }
        
    private List<Unit> getUnits(UnitCategory category) {
        String filter = getFilter();
        return filter.length() == 0 ? category.getUnits() :             
            category.getVisibleUnits(filter, !Type.INSTALLED.equals(getType()));
    }
        
    public Collection<Unit> getMarkedUnits() {
        List<Unit> units = new ArrayList<Unit> ();
        for (UnitCategory c : data) {
            for (Unit u : c.getUnits ()) {
                if (u.isMarked ()) {
                    units.add (u);
                }
            }
        };
        return units;
    }

    private int getCategorySize (UnitCategory c) {
        int retval = 0;
        String filter = getFilter();
        if (c.isVisible(filter)) {
            retval = c.isExpanded() ? getUnits(c).size() + 1 : 1;
        } else {
            retval = 0;
        }
        return retval;
    }
    
    protected UnitCategory getCategoryAtRow (int row) {
        int categoryRow = 0;
        List<UnitCategory> data = getVisibleCategories();
        
        for (UnitCategory c : data) {
            int size = getCategorySize (c);
            if (row >= categoryRow && row < categoryRow + size) {
                return c;
            }
            categoryRow += size;
        }
         
        assert false : "No UpdateCategory on row " + row;
        return null;
    }
    
    public Unit getUnitAtRow (int row) {
        int unitRow = 0;
        List<UnitCategory> data = getVisibleCategories();
        
        for (UnitCategory c : data) {
            int size = getCategorySize (c);
            if (row > unitRow && row <= unitRow + size) {
                if (! c.isExpanded ()) assert false : "No Unit at row " + row + ", but Category " + c;
                return getUnits (c).get (row - unitRow - 1);
            }
            unitRow += size;
        }
        
        assert false : "No Unit on row " + row;
        return null;
    }
    
    public void toggleCategoryExpanded (int row) {
        getCategoryAtRow (row).toggleExpanded ();
        fireTableDataChanged ();
    }
    
    public boolean isCategoryAtRow (int row) {
        return isCategoryAtRow (row, data);
    }
    
    private boolean isCategoryAtRow (int row,List<UnitCategory> data) {
        int accumulatedRow = 0;
        
        for (UnitCategory c : data) {
            int size = getCategorySize (c);
            if (row >= accumulatedRow && row < accumulatedRow + size) {
                return row == accumulatedRow;
            } else {
                accumulatedRow += size;
            }
        }
        
        return false;
    }
    
    public boolean isCellEditable (int row, int col) {
        if (isCategoryAtRow (row)) {
            return false;
        } else {
            return col == 0 || col == 1;
        }
    }

}
