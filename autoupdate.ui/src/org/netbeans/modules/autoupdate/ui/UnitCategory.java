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
import java.util.List;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitCategory {
    final String name;
    boolean isExpanded = true;
    List<Unit> units = new ArrayList<Unit> ();

    /** Creates a new instance of UpdateCategory */
    public UnitCategory (String name) {
        //assert name != null;
        this.name = name != null ? name : "";
    }

    /** Creates a new instance of UpdateCategory */
    public UnitCategory (String name, List<Unit> units, boolean isExpanded) {
        this.name = name;
        this.units.addAll (units);
        this.isExpanded = isExpanded;
    }

    public final boolean isVisible(final String filter) {
        assert filter != null;
        assert getCategoryName () != null;
        boolean retval = filter.length() == 0 || getCategoryName ().toLowerCase().contains(filter);
        if (!retval) {
            List<Unit> allUnits = getUnits ();
            for (Unit unit : allUnits) {
                if (unit.isVisible(filter)) {
                    retval = true;
                    break;
                }
            }
        }
        return  retval;
    }

    public List<Unit> getVisibleUnits (String filter, boolean orMarked) {
        boolean categoryFilterMatch = filter.length() == 0 || getCategoryName ().toLowerCase().contains(filter);
        List<Unit> allUnits = getUnits ();
        List<Unit> visibleUnits = (categoryFilterMatch) ? new ArrayList<Unit> (allUnits) : new ArrayList<Unit>();
        if (!categoryFilterMatch) {
            for (Unit unit : allUnits) {
                if (unit.isVisible(filter) || (orMarked && unit.isMarked())) {
                    visibleUnits.add(unit);
                }
            }        
        }
        return visibleUnits;
    }

    public List<Unit> getMarkedUnits() {
        List<Unit> markedUnits = new ArrayList<Unit> ();        
        List<Unit> allUnits = getUnits ();
        for (Unit u : allUnits) {
            if (u.isMarked()) {
                markedUnits.add(u);
            }
        }
        return markedUnits;        
    }
    
    
    public String getCategoryName () {
        return name;
    }

    public boolean isExpanded () {
        return isExpanded;
    }

    public void setExpanded (boolean expanded) {
        isExpanded = expanded;
    }

    public boolean addUnit (Unit u) {
        return units.add (u);
    }

    public void addUnits (List<Unit> units) {
        for (Unit unit : units) {
            addUnit(unit);
        }
    }
    
    public boolean removeUnit (Unit u) {
        return units.remove (u);
    }
    
    public List<Unit> getUnits () {
        return units;
    }
    
    void toggleExpanded () {
        this.isExpanded = ! isExpanded;

//        Preferences prefs = NbPreferences.forModule( FoldingTaskListModel.class );
//        prefs.putBoolean( "expanded_"+tg.getName(), isExpanded );
//
//        int firstRow = 0;
//        int groupIndex = groups.indexOf( this );
//        for( int i=0; i<groupIndex; i++ ) {
//            firstRow += groups.get( i ).getRowCount();
//        }
//        int lastRow = firstRow + getTaskCount();
//        firstRow += 1;
//
//        if( isExpanded )
//            fireTableRowsInserted( firstRow, lastRow );
//        else
//            fireTableRowsDeleted( firstRow, lastRow );
//        fireTableCellUpdated( firstRow-1, COL_GROUP );
    }

}
