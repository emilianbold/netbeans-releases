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
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstalledTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer<OperationSupport> enableContainer = Containers.forEnable();
    private OperationContainer<OperationSupport> disableContainer = Containers.forDisable();
    private OperationContainer<OperationSupport> uninstallContainer = Containers.forUninstall();
    
    private final Logger err = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.InstalledTableModel");
    
    /** Creates a new instance of InstalledTableModel */
    public InstalledTableModel(List<UnitCategory> categories) {
        super(categories);
    }
    
    @Override
    public void setValueAt (Object anValue, int row, int col) {
        super.setValueAt(anValue, row, col);
        if (! isCategoryAtRow(row)) {
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
            boolean beforeMarked = u.isMarked ();
            u.setMarked (! beforeMarked);
            if (u.isMarked () != beforeMarked) {
                fireButtonsChange ();
            } else {
                //TODO: message should contain spec.version
                String message = NbBundle.getMessage(UpdateTableModel.class,"NotificationAlreadyPreparedToIntsall",u.getDisplayName());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
            }
        }
    }
    
    public Object getValueAt(int row, int col) {
        Object res = null;
        
        if (isCategoryAtRow(row)) {
            res = col == 0 ? getCategoryAtRow(row) : null;
        } else {
            //assert getCategoryAtRow(row).isExpanded();
            Unit.Installed u = (Unit.Installed) getUnitAtRow(row);
            switch (col) {
            case 0 :
                res = u.isMarked() ? Boolean.TRUE : Boolean.FALSE;
                break;
            case 1 :
                res = u.getDisplayName();
                break;
            case 2 :
                res = u.getInstalledVersion();
                break;
            case 3 :
                res = u.getBackupVersion();
                break;
            case 4 :
                res = u.getMyRating();
                break;
            }
        }
        return res;
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
            res = Integer.class;
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
        case 2 :
            return getBundle ("InstalledTableModel_Columns_Installed");
        case 3 :
            return getBundle ("InstalledTableModel_Columns_Previous");
        case 4 :
            return getBundle ("InstalledTableModel_Columns_MyRating");
        }
        
        assert false;
        return super.getColumnName( column );
    }
    
    @Override
    public boolean isCellEditable (int row, int col) {
        if (! isCategoryAtRow (row)) {
            if (col == 1) { // XXX
                Unit.Installed u = (Unit.Installed) getUnitAtRow (row);
                return ! u.isNotEditable ();
            } else if (col == 0) {
                Unit.Installed u = (Unit.Installed) getUnitAtRow (row);
                return ! u.isNotEditable ();
            }
        }
        return super.isCellEditable (row, col);
    }
    
    public Type getType() {
        return UnitCategoryTableModel.Type.INSTALLED;
    }

    public boolean isSortAllowed(Object columnIdentifier) {
        boolean isUninstall = getColumnName(0).equals(columnIdentifier);
        boolean isPrevious = getColumnName(3).equals(columnIdentifier);                        
        boolean isRating = getColumnName(4).equals(columnIdentifier);                
        return isUninstall || isPrevious || isRating ? false : true;
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
                    return Unit.Installed.compareInstalledVersions(unit1, unit2);
                } else if (getColumnName(3).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                } else if (getColumnName(4).equals(columnIdentifier)) {
                    assert false : columnIdentifier.toString();
                }                
                return 0;
            }
        };
    }

    public OperationContainer getContainer() {
        return uninstallContainer;
    }
    
    private String getBundle (String key) {
        return NbBundle.getMessage (this.getClass (), key);
    }
}
