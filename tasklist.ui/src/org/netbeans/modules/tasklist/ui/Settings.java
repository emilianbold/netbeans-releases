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

package org.netbeans.modules.tasklist.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScanningScopeList;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbPreferences;

/**
 *
 * @author S. Aubrecht
 */
public class Settings {
    
    private static final String PREF_FOLDING_WIDTH = "folding_table_col_"; //NOI18N
    private static final String PREF_REGULAR_WIDTH = "table_col_"; //NOI18N
    
    private static Settings theInstance;
    
    private Preferences prefs;
    
    /** Creates a new instance of Settings */
    private Settings() {
        prefs = NbPreferences.forModule( Settings.class );
    }
    
    public static Settings getDefault() {
        if( null == theInstance )
            theInstance = new Settings();
        return theInstance;
    }
    
    public int getSortingColumn() {
        return prefs.getInt( "sortingColumn", -1 ); //NOI18N
    }
    
    public void setSortingColumn( int col ) {
        prefs.putInt( "sortingColumn", col ); //NOI18N        
    }
    
    public boolean isAscendingSort() {
        return prefs.getBoolean( "sortingAscending", true ); //NOI18N
    }
    
    public void setAscendingSort( boolean asc ) {
        prefs.putBoolean( "sortingAscending", asc ); //NOI18N
    }
    
    public float getPreferredColumnWidth( int col, boolean foldingTable, float defaultValue ) {
        return prefs.getFloat( (foldingTable ? PREF_FOLDING_WIDTH : PREF_REGULAR_WIDTH) +col, defaultValue );
    }
    
    public void setPreferredColumnWidth( int col, boolean foldingTable, float colWidth ) {
        prefs.putFloat( (foldingTable ? PREF_FOLDING_WIDTH : PREF_REGULAR_WIDTH)+col, colWidth );
    }
    
    public boolean isGroupExpanded( String groupName ) {
        return prefs.getBoolean( "expanded_"+groupName, true ); //NOI18N        
    }
    
    public void setGroupExpanded( String groupName, boolean expand ) {
        prefs.putBoolean( "expanded_"+groupName, expand ); //NOI18N        
    }
    
    public void setActiveScanningScope( TaskScanningScope scope ) {
        prefs.put( "activeScanningScope", scope.getClass().getName() ); //NOI18N
    }
    
    public TaskScanningScope getActiveScanningScope() {
        String clazzName = prefs.get( "activeScanningScope", null ); //NOI18N
        if( null != clazzName ) {
            for( TaskScanningScope scope : ScanningScopeList.getDefault().getTaskScanningScopes() ) {
                if( scope.getClass().getName().equals( clazzName ) )
                    return scope;
            }
        }
        return null;
    }
    
    public void setGroupTasksByCategory( boolean group ) {
        prefs.putBoolean( "groupTasksByCategory", group ); //NOI18N
    }
    
    public boolean getGroupTasksByCategory() {
        return prefs.getBoolean( "groupTasksByCategory", false ); //NOI18N
    }
}
