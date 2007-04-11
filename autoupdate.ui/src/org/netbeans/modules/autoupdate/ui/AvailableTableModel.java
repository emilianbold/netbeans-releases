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

import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;

/**
 *
 * @author Jiri Rechtacek
 */
public class AvailableTableModel extends UnitCategoryTableModel {
    //just prevents from gc, do not delete
    private OperationContainer<InstallSupport> container = Containers.forAvailable();
    
    /** Creates a new instance of InstalledTableModel */
    public AvailableTableModel (List<UnitCategory> categories) {
        super (categories);
    }
    
    @Override
    public void setValueAt(Object anValue, int row, int col) {
        // second column is editable but doesn't want to edit its value
        if (col == 1) {
            return ;
        }
        super.setValueAt (anValue, row, col);
        if (! isCategoryAtRow (row)) {
            if (anValue == null) {
                return ;
            }
            assert getCategoryAtRow (row).isExpanded ();
            Unit.Available u = (Unit.Available) getUnitAtRow (row);
            assert anValue instanceof Boolean : anValue + " must be instanceof Boolean.";
            boolean beforeMarked = u.isMarked();
            u.setMarked(!beforeMarked);
            if (u.isMarked() != beforeMarked) {
                fireButtonsChange ();
            }
        }
    }

    public Object getValueAt(int row, int col) {
        Object res = null;
        
        if (isCategoryAtRow (row)) {
            res = col == 0 ? getCategoryAtRow (row) : null;
        } else {
            assert getCategoryAtRow (row).isExpanded ();
            Unit.Available u = (Unit.Available) getUnitAtRow (row);
            switch (col) {
            case 0 :
                res = u.isMarked () ? Boolean.TRUE : Boolean.FALSE;
                break;
            case 1 :
                res = u.getDisplayName ();
                break;
            case 2 :
                res = u.getAvailableVersion ();
                break;
            case 3 :
                res = Unit.getSize (u.getCompleteSize ());
                break;
            case 4 :
                res = u.getMyRating ();
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

    public String getColumnName(int column) {
	//TODO I18N
        switch (column) {
            case 0 : //group icon
                return "Install";
            case 1 :
                return "Name";
            case 2 :
                return "Version";
            case 3 :
                return "Size";
            case 4 :
                return "Rating";
        }
        
        assert false;
        return super.getColumnName( column );
    }
    
    public Type getType () {
        return UnitCategoryTableModel.Type.AVAILABLE;
    }
    
}
