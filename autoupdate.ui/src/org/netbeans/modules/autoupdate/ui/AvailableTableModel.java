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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class AvailableTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer container = Containers.forAvailable();
    private OperationContainer containerCustom = Containers.forCustomInstall ();
    
    /** Creates a new instance of AvailableTableModel */
    public AvailableTableModel (List<UpdateUnit> units) {
        setUnits(units);
    }
    
    public final void setUnits (List<UpdateUnit> units) {
        setData(Utilities.makeAvailableCategories (units, false));
    }
    
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        // second column is editable but doesn't want to edit its value
        if (isExpansionControlAtRow(row)) return;//NOI18N        
        if (col == 1) {
            return ;
        }
        super.setValueAt(anValue, row, col);
        if (anValue == null) {
            return ;
        }
        //assert getCategoryAtRow (row).isExpanded ();
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        boolean beforeMarked = u.isMarked();
        u.setMarked(!beforeMarked);
        if (u.isMarked() != beforeMarked) {
            fireButtonsChange();
        }
    }

    public Object getValueAt(int row, int col) {
        Object res = null;
        if (isExpansionControlAtRow(row)) return "";//NOI18N
        
        Unit.Available u = (Unit.Available) getUnitAtRow(row);
        switch (col) {
        case 0 :
            res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
            break;
        case 1 :
            res = u.getDisplayName();
            break;
        case 2 :
            res = u.getCategoryName();
            break;
        case 3 :
            res = u.getSourceCategory();
            break;
        }
        
        return res;
    }

    public int getColumnCount() {
        return 4;
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
            res = CATEGORY.class;
            break;
        }
        
        return res;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0 :
                return getBundle ("AvailableTableModel_Columns_Install");
            case 1 :
                return getBundle ("AvailableTableModel_Columns_Name");
            case 2 :
                return getBundle("AvailableTableModel_Columns_Category");
            case 3 :
                return getBundle ("AvailableTableModel_Source_Category");
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
        return UnitCategoryTableModel.Type.AVAILABLE;
    }
    
    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isInstall = getColumnName(0).equals(columnIdentifier);
        return isInstall  ? false : true;
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
                    return Unit.Available.compareSourceCategories(unit1, unit2);
                }                
                return 0;
            }
        };
    }

    @SuppressWarnings ("unchecked")
    public int getDownloadSize () {
        int res = 0;
        assert container != null || containerCustom != null: "OperationContainer found when asking for download size.";
        Set<OperationInfo> infos = new HashSet<OperationInfo> ();
        infos.addAll (container.listAll ());
        infos.addAll (containerCustom.listAll ());
        Set<UpdateElement> elements = new HashSet<UpdateElement> ();
        for (OperationInfo info : infos) {
            elements.add (info.getUpdateElement ());
            elements.addAll (info.getRequiredElements ());
        }
        for (UpdateElement el : elements) {
            res += el.getDownloadSize ();
        }
        return res;
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }
     
}
