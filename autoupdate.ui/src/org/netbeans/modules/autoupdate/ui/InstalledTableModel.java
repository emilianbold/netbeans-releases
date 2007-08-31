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

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.table.JTableHeader;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */

public class InstalledTableModel extends UnitCategoryTableModel {
    static final String STATE_ENABLED = NbBundle.getMessage(UpdateTableModel.class,"InstalledTableModel_State_Enabled");
    static final String STATE_DISABLED = NbBundle.getMessage(UpdateTableModel.class,"InstalledTableModel_State_Disabled");
            
    //just prevents from gc, do not delete
    private OperationContainer<OperationSupport> enableContainer = Containers.forEnable();
    private OperationContainer<OperationSupport> disableContainer = Containers.forDisable();
    private OperationContainer<OperationSupport> uninstallContainer = Containers.forUninstall();
    
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.InstalledTableModel");
    
    /** Creates a new instance of InstalledTableModel */
    public InstalledTableModel(List<UpdateUnit> units) {
        setUnits(units);
    }

    public final void setUnits (List<UpdateUnit> units) {    
        setData(Utilities.makeInstalledCategories (units));
    }

    @Override
    public String getToolTipText(int row, int col) {
        if (col == 3) {
            boolean isEnabled = (Boolean)getValueAt (row, 3);            
            String key = null;
            if (isEnabled) {
                key = "InstallTab_Active_Tooltip";
            } else {
                key = "InstallTab_InActive_Tooltip";
            }
            return (key != null) ? getBundle(key) : null;
        }
        return super.getToolTipText(row, col);
    }

    
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        super.setValueAt(anValue, row, col);
        
        if (col == 1) {
            // second column handles buttons
            return ;
        }
        assert col == 0 : "First column.";
        if (anValue == null) {
            return ;
        }
        //assert getCategoryAtRow(row).isExpanded();
        Unit.Installed u = (Unit.Installed) getUnitAtRow(row);
        assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
        boolean beforeMarked = u.isMarked();
        u.setMarked(! beforeMarked);
        if (u.isMarked() != beforeMarked) {
            fireButtonsChange();
        } else {
            //TODO: message should contain spec.version
            String message = NbBundle.getMessage(UpdateTableModel.class,"NotificationAlreadyPreparedToIntsall",u.getDisplayName());
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
        }
        
    }
    
    public Object getValueAt(int row, int col) {
        Object res = null;
        
        Unit.Installed u = (Unit.Installed) getUnitAtRow(row);
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
            res = u.getRelevantElement().isEnabled();
            break;
        case 4 :
            res = u.getInstalledVersion();
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
            res = Boolean.class;
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
            return getBundle ("InstalledTableModel_Columns_Uninstall");
        case 1 :
            return getBundle ("InstalledTableModel_Columns_Name");
        case 2:
            return getBundle("InstalledTableModel_Columns_Category");
        case 3 :
            return getBundle ("InstalledTableModel_Columns_Enabled");                        
        case 4 :
            return getBundle ("InstalledTableModel_Columns_Installed");
        }
        
        assert false;
        return super.getColumnName( column );
    }

    @Override
    public int getMinWidth(JTableHeader header, int col) {
        return super.getMinWidth(header, col);
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
    
    
    public Type getType() {
        return UnitCategoryTableModel.Type.INSTALLED;
    }

    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isUninstall = getColumnName(0).equals(columnIdentifier);
        return isUninstall ? false : true;
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
                    return Unit.Installed.compareEnabledState(unit1, unit2);
                } else if (getColumnName(4).equals(columnIdentifier)) {
                    return Unit.Installed.compareInstalledVersions(unit1, unit2);
                }                 
                return 0;
            }
        };
    }

    public int getDownloadSize () {
        // no need to download anything in Installed tab
        return 0;
    }
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        Unit.Installed u = (Unit.Installed)getUnitAtRow(row);
        return (col == 0) ? u != null && u.canBeMarked() : super.isCellEditable(row, col);
    }

}
