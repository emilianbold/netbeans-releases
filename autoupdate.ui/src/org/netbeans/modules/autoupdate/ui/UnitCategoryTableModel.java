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
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public abstract class UnitCategoryTableModel extends AbstractTableModel {
    List<UnitCategory> data = Collections.emptyList ();
    private List<Unit> unitData = Collections.emptyList ();
    
    private List<UpdateUnitListener> listeners = new ArrayList<UpdateUnitListener> ();
    private String filter = "";//NOI18N    
    private Comparator<Unit> unitCmp;
    private Comparator<UnitCategory> categoryCmp;
    private boolean showCategories = true;

    
    public static enum Type {
        INSTALLED,
        UPDATE,
        AVAILABLE,
        LOCAL
    }
    
    /** Creates a new instance of CategoryTableModel */
    public UnitCategoryTableModel (List<UnitCategory> categories) {
        setData(categories);
    }

    public abstract Object getValueAt (int row, int col);
    public abstract Class getColumnClass (int c);
    public abstract Type getType ();
    public abstract boolean isSortAllowed (Object columnIdentifier);    
    protected abstract Comparator<Unit> getComparator(final Object columnIdentifier, final boolean sortAscending);

    protected Comparator<Unit> getDefaultComparator() {
        return new Comparator<Unit>(){
            public int compare(Unit o1, Unit o2) {
                return Unit.compareDisplayNames(o1, o2);
            }
        };
    }
    
    public final void sort(Object columnIdentifier, boolean sortAscending) {
        if (columnIdentifier == null) {
            setUnitComparator(getDefaultComparator(), true);
        } else {
            setUnitComparator(getComparator(columnIdentifier, sortAscending), false);
        }
        fireTableDataChanged();
    }
    
    public final void setData (List<UnitCategory> data, boolean showCategories, Comparator<UnitCategory> categoryCmp, Comparator<Unit> unitCmp) {
        //TODO: if null cmpput in default one
        this.categoryCmp = categoryCmp;
        this.unitCmp = unitCmp;
        this.showCategories = showCategories;
        this.data = data;
        this.unitData = Collections.emptyList();
        if (showCategories) {
            if (categoryCmp != null) {
                Collections.sort(this.data,categoryCmp);
            }
            if (unitCmp != null) {
                for (UnitCategory unitCategory : data) {
                    Collections.sort(unitCategory.units,unitCmp);
                }
            }
        } else {
            this.unitData = new ArrayList<Unit>();
            for (UnitCategory unitCategory : data) {
                this.unitData.addAll(unitCategory.getUnits());
            }
            if (unitCmp != null) {
                Collections.sort(this.unitData,unitCmp);
            }
        }
        this.fireTableDataChanged ();
    }
    
    public void setUnitComparator(Comparator<Unit> comparator, boolean showCategories) {
        setData(data, showCategories, categoryCmp,comparator);
    }
    
    public void setUnitComparator(Comparator<Unit> comparator) {
        setUnitComparator(comparator, showCategories);
    }
    

    public void setCategoryComparator(Comparator<UnitCategory> comparator, boolean showCategories) {
        setData(data, showCategories, comparator,unitCmp);
    }
    
    public void setShowCategoriesEnabled(boolean showCategories) {
        setData(data, showCategories, categoryCmp,unitCmp);
    }
    
    public final void setData (List<UnitCategory> data) {
        setData(data, showCategories, categoryCmp, unitCmp);
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
    
    public String getFilter() {
        synchronized(UnitCategoryTableModel.class) {
            return this.filter == null ? "" : this.filter;
        }                
    }
    
    public void expandAll() {
        if (showCategories) {
            List<UnitCategory> data = getCategories();
            for (UnitCategory unitCategory : data) {
                unitCategory.setExpanded(true);
            }
        }
    }
    
    public void collapseAll() {
        if (showCategories) {
            List<UnitCategory> data = getCategories();
            for (UnitCategory unitCategory : data) {
                unitCategory.setExpanded(false);
            }        
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

    public int getColumnCount () {
        return 5;
    }
    
    private List<UnitCategory> getCategories() {
        assert showCategories;
        return new ArrayList<UnitCategory>(data);
    }
    
    private List<Unit> getVisibleUnits() {
        assert !showCategories;
        String filter = getFilter();
        List<Unit> retval = new ArrayList<Unit>();
        for (Unit unit : unitData) {
            if (unit.isVisible(filter)) {
                retval.add(unit);
            }
        }
        return retval;
    }
    
    private List<UnitCategory> getVisibleCategories() {
        assert showCategories;
        String filter = getFilter();
        List<UnitCategory> retval = new ArrayList<UnitCategory>();
        for (UnitCategory unitCategory : data) {
            if (unitCategory.isVisible(filter)) {
                retval.add(unitCategory);
            }
        }
        return retval;
    }        
    
    public int getRowCount() {
        if (showCategories) {
            List<UnitCategory> data = getVisibleCategories();
            int size = data.size();
            String filter = getFilter();
            for (UnitCategory c : data) {
                if (c.isExpanded() && c.isVisible(filter)) size += getUnits(c).size();
            }
            return size;
        }
        return getVisibleUnits().size();        
    }

    public int getRawItemCount () {
        int size = 0;                
        if (showCategories) {
            for (UnitCategory c : getCategories()) {
                size += c.getUnits().size();
            }
        } else {
            size = unitData.size();
        }
        
        return size;
    }
    
    public int getItemCount () {
        int size = 0;        
        if (showCategories) {
            List<UnitCategory> data = getVisibleCategories();
            
            for (UnitCategory c : data) {
                size += getUnits(c).size();
            }
        } else {
            size = getVisibleUnits().size();
        }
        
        return size;
    }
        
    private List<Unit> getUnits(UnitCategory category) {
        assert showCategories;
        String filter = getFilter();
        return filter.length() == 0 ? category.getUnits() :             
            category.getVisibleUnits(filter, !Type.INSTALLED.equals(getType()));
    }
        
    public Collection<Unit> getMarkedUnits() {
        List<Unit> units = new ArrayList<Unit> ();
        if (showCategories) {
            for (UnitCategory c : data) {
                for (Unit u : c.getUnits()) {
                    if (u.isMarked()) {
                        units.add(u);
                    }
                }
            }
        } else {
            for (Unit u : unitData) {
                if (u.isMarked()) {
                    units.add(u);
                }
            }
        }
        return units;
    }

    private int getCategorySize (UnitCategory c) {
        assert showCategories;
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
        if (showCategories) {
            int categoryRow = 0;
            List<UnitCategory> data = getVisibleCategories();
            
            for (UnitCategory c : data) {
                int size = getCategorySize(c);
                if (row >= categoryRow && row < categoryRow + size) {
                    return c;
                }
                categoryRow += size;
            }
            assert false : "No UpdateCategory on row " + row;
        }                 
        return null;
    }
    
    public Unit getUnitAtRow (int row) {
        if (showCategories) {
            int unitRow = 0;
            List<UnitCategory> data = getVisibleCategories();
            
            for (UnitCategory c : data) {
                int size = getCategorySize(c);
                if (row > unitRow && row <= unitRow + size) {
                    if (! c.isExpanded()) assert false : "No Unit at row " + row + ", but Category " + c;
                    int idx = row - unitRow - 1;                    
                    return (idx < getUnits(c).size()) ? getUnits(c).get(idx) : null;
                }
                unitRow += size;
            }
            
            return null;
        }
        return getVisibleUnits().size() <= row ? null : getVisibleUnits().get(row);
    }
    
    public void toggleCategoryExpanded (int row) {
        assert showCategories;
        getCategoryAtRow (row).toggleExpanded ();
        fireTableDataChanged ();
    }
    
    public boolean isCategoryAtRow (int row) {
        return showCategories ? isCategoryAtRow (row, data) : false;
    }
    
    private boolean isCategoryAtRow (int row,List<UnitCategory> data) {
        int accumulatedRow = 0;
        if (showCategories) {
            for (UnitCategory c : data) {
                int size = getCategorySize(c);
                if (row >= accumulatedRow && row < accumulatedRow + size) {
                    return row == accumulatedRow;
                } else {
                    accumulatedRow += size;
                }
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
