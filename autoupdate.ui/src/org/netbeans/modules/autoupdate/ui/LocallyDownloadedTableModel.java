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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class LocallyDownloadedTableModel extends UnitCategoryTableModel {
    private OperationContainer<InstallSupport> availableNbmsContainer = Containers.forAvailableNbms();
    private OperationContainer<InstallSupport> updateNbmsContainer = Containers.forUpdateNbms();
    private LocalDownloadSupport localDownloadSupport = null;
    private List<UpdateUnit> installed = new ArrayList<UpdateUnit>();
        
    /** Creates a new instance of InstalledTableModel */
    public LocallyDownloadedTableModel (LocalDownloadSupport localDownloadSupport) {        
        this.localDownloadSupport = localDownloadSupport;
    }
    
    public final void setUnits(final List<UpdateUnit> unused) {
        List<UpdateUnit> units = getLocalDownloadSupport().getUpdateUnits();
        List<Unit> oldUnits = getUnitData();        
        setData(makeCategories(units));
        computeInstalled(units, oldUnits);
    }
    
    protected boolean isMarkedAsDefault() {
        return true;
    }
    
    List<UpdateUnit> getAlreadyInstalled() {
        return installed;
    }
            
    private void computeInstalled(List<UpdateUnit> units, List<Unit> oldUnits) {
        installed.clear();
        installed.addAll(units);
        List<Unit> newUnits = getUnitData();
        List<UpdateUnit> newUpdateUnits = new ArrayList<UpdateUnit>();
        for (Unit unit : newUnits) {
            newUpdateUnits.add(unit.updateUnit);
        }
        installed.removeAll(newUpdateUnits);
        removeUpdateUnits(installed);        
    }
        
        
    private void removeUpdateUnits(List<UpdateUnit> units) {
        for (UpdateUnit updateUnit : units) {
            getLocalDownloadSupport().remove(updateUnit);
        }
    }
    
        
    private List<UnitCategory> makeCategories(List<UpdateUnit> units) {
        final List<UnitCategory> categories = new ArrayList<UnitCategory>();        
        categories.addAll(Utilities.makeAvailableCategories(units, true));
        categories.addAll(Utilities.makeUpdateCategories(units, true));
        return categories;
    }
    
    LocalDownloadSupport getLocalDownloadSupport() {
        return localDownloadSupport;
    }
        
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        super.setValueAt (anValue, row, col);
        if (! isCategoryAtRow (row)) {
            if (anValue == null) {
                return ;
            }
            if (! (anValue instanceof Boolean)) {
                return ;
            }
            //assert getCategoryAtRow (row).isExpanded ();
            Unit u = getUnitAtRow (row);
            if (u != null) {
                assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
                boolean beforeMarked = u.isMarked();
                u.setMarked(!beforeMarked);
                if (u.isMarked() != beforeMarked) {
                    fireButtonsChange ();
                } else {
                    assert false : u.getDisplayName();
                }
            }
        }
    }

    
    public Object getValueAt(int row, int col) {
        Object res = null;
        
        if (isCategoryAtRow (row)) {
            res = col == 0 ? getCategoryAtRow (row) : null;
        } else {
            //assert getCategoryAtRow (row).isExpanded ();
            Unit u = getUnitAtRow (row);
            boolean isAvailable = (u instanceof Unit.Available);
            switch (col) {
            case 0 :
                res = u.isMarked () ? Boolean.TRUE : Boolean.FALSE;
                break;
            case 1 :
                res = u.getDisplayName ();
                break;
            case 2 :
                res = u.getCategoryName();
                break;                                
            case 3 :
                if (isAvailable) {
                    res = ((Unit.Available)u).getAvailableVersion ();
                } else {
                    res = ((Unit.Update)u).getAvailableVersion ();
                }
                break;
            case 4 :
                if (isAvailable) {
                    res = ((Unit.Available)u).getSize();
                } else {
                    res = ((Unit.Update)u).getSize();
                }                
                break;
            }
        }
        return res;
    }
    
    public int getColumnCount() {
        return 3;
    }

    public Class getColumnClass(int c) {
        Class res = null;
        
        switch (c) {
        case 0 :
            res = Boolean.class;
            break;
        case 1 :
            res = String.class;
            break;
        case 2 :
            res = String.class;
            break;
        case 3 :
            res = String.class;
            break;
        case 4 :
            res = String.class;
            break;
        }
        
        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Install");
            case 1 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Name");
            case 2 :
                return getBundle ("InstalledTableModel_Columns_Category");                                
            case 3 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Version");
            case 4 :
                return getBundle ("LocallyDownloadedTableModel_Columns_Size");
        }
        
        assert false;
        return super.getColumnName( column );
    }

    public int getPreferredWidth(JTableHeader header, int col) {
        switch (col) {
                case 1:
            return super.getMinWidth(header, col)*4;
        case 2:
            return super.getMinWidth(header, col)*2;
        }
        return super.getMinWidth(header, col);
    }
    
    public Type getType () {
        return UnitCategoryTableModel.Type.LOCAL;
    }
    
    public class DisplayName {
        public DisplayName (String name) {
            
        }
    }
    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isInstall = getColumnName(0).equals(columnIdentifier);
        boolean isSize = getColumnName(4).equals(columnIdentifier);                        
        return isInstall || isSize ? false : true;
    }

    protected Comparator<Unit> getComparator(final Object columnIdentifier, final boolean sortAscending) {
        return new Comparator<Unit>(){
            public int compare(Unit o1, Unit o2) {
                Unit unit1 = sortAscending ? o1 : o2;
                Unit unit2 = sortAscending ? o2 : o1;
                if (getColumnName(0).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                } else if (getColumnName(1).equals(columnIdentifier)) {
                    return Unit.compareDisplayNames(unit1, unit2);
                } else if (getColumnName(2).equals(columnIdentifier)) {
                    return Unit.compareCategories(unit1, unit2);
                } else if (getColumnName(3).equals(columnIdentifier)) {
                    return Unit.compareDisplayVersions(unit1, unit2);
                } else if (getColumnName(4).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                }                
                return 0;
            }
        };
    }

    public OperationContainer getContainer() {
        int available = Containers.forAvailableNbms().listAll().size();
        int updates = Containers.forUpdateNbms().listAll().size();        
        return (updates > available) ? Containers.forUpdateNbms() : Containers.forAvailableNbms();
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }
}
