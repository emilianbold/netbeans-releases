/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    private boolean isVisible;
    private String filter;

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
        if (this.filter != null && this.filter.equals(filter)) {
            return isVisible;
        } else {
            this.filter = filter;
            isVisible = filter.length() == 0 || getCategoryName().toLowerCase().contains(filter);
            if (!isVisible) {
                List<Unit> allUnits = getUnits();
                for (Unit unit : allUnits) {
                    if (unit.isVisible(filter)) {
                        isVisible = true;
                        break;
                    }
                }
            }
        }
        return  isVisible;
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
