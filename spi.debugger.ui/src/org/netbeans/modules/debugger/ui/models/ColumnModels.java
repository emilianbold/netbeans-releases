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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.models;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * Defines model for one table view column. Can be used together with
 * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
 * view representation.
 *
 * @author   Jan Jancura
 */
public class ColumnModels {
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    private static class AbstractColumn extends ColumnModel {
        
        private String id;
        private String displayName;
        private String shortDescription;
        private Class type;
        private boolean defaultVisible;
        private PropertyEditor propertyEditor;
        
        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type) {
            this(id, displayName, shortDescription, type, true);
        }
        
        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible) {
            this(id, displayName, shortDescription, type, defaultVisible, null);
        }
        
        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible,
                              PropertyEditor propertyEditor) {
            this.id = id;
            this.displayName = displayName;
            this.shortDescription = shortDescription;
            this.type = type;
            this.defaultVisible = defaultVisible;
            this.propertyEditor = propertyEditor;
        }
        
        public String getID() {
            return id;
        }
        
        public String getDisplayName() {
            return NbBundle.getBundle (ColumnModels.class).getString(displayName);
        }

        public Character getDisplayedMnemonic() {
            return new Character(NbBundle.getBundle(ColumnModels.class).
                    getString(displayName+"_Mnc").charAt(0));   // NOI18N
        }
        
        public String getShortDescription() {
            return NbBundle.getBundle (ColumnModels.class).getString(shortDescription);
        }
        
        public Class getType() {
            return type;
        }
        
        /**
         * Set true if column is visible.
         *
         * @param visible set true if column is visible
         */
        public void setVisible (boolean visible) {
            properties.setBoolean (getID () + ".visible", visible);
        }

        /**
         * Set true if column should be sorted by default.
         *
         * @param sorted set true if column should be sorted by default 
         */
        public void setSorted (boolean sorted) {
            properties.setBoolean (getID () + ".sorted", sorted);
        }

        /**
         * Set true if column should be sorted by default in descending order.
         *
         * @param sortedDescending set true if column should be 
         *        sorted by default in descending order
         */
        public void setSortedDescending (boolean sortedDescending) {
            properties.setBoolean (
                getID () + ".sortedDescending", 
                sortedDescending
             );
        }
    
        /**
         * Should return current order number of this column.
         *
         * @return current order number of this column
         */
        public int getCurrentOrderNumber () {
            return properties.getInt (getID () + ".currentOrderNumber", -1);
        }

        /**
         * Is called when current order number of this column is changed.
         *
         * @param newOrderNumber new order number
         */
        public void setCurrentOrderNumber (int newOrderNumber) {
            properties.setInt (
                getID () + ".currentOrderNumber",
                newOrderNumber
            );
        }

        /**
         * Return column width of this column.
         *
         * @return column width of this column
         */
        public int getColumnWidth () {
            return properties.getInt (getID () + ".columnWidth", 150);
        }

        /**
         * Is called when column width of this column is changed.
         *
         * @param newColumnWidth a new column width
         */
        public void setColumnWidth (int newColumnWidth) {
            properties.setInt (getID () + ".columnWidth", newColumnWidth);
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", defaultVisible);
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean isSorted () {
            return properties.getBoolean (getID () + ".sorted", false);
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending 
         * order
         */
        public boolean isSortedDescending () {
            return properties.getBoolean (
                getID () + ".sortedDescending", 
                false
            );
        }
        
        /**
         * Returns {@link java.beans.PropertyEditor} to be used for 
         * this column. Default implementation returns <code>null</code> - 
         * means use default PropertyEditor.
         *
         * @return {@link java.beans.PropertyEditor} to be used for 
         *         this column
         */
        public PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static ColumnModel createDefaultBreakpointsColumn() {
        return new AbstractColumn("DefaultBreakpointColumn",
                "CTL_BreakpointView_Column_Name_Name",
                "CTL_BreakpointView_Column_Name_Desc",
                null);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static ColumnModel createBreakpointEnabledColumn() {
        return new AbstractColumn(Constants.BREAKPOINT_ENABLED_COLUMN_ID,
                "CTL_BreakpointView_Column_Enabled_Name",
                "CTL_BreakpointView_Column_Enabled_Desc",
                Boolean.TYPE);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static ColumnModel createDefaultCallStackColumn() {
        return new AbstractColumn("DefaultCallStackColumn",
                "CTL_CallstackView_Column_Name_Name",
                "CTL_CallstackView_Column_Name_Desc",
                null);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createCallStackLocationColumn() {
        return new AbstractColumn(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID,
                "CTL_CallstackView_Column_Location_Name",
                "CTL_CallstackView_Column_Location_Desc",
                String.class,
                false);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createDefaultLocalsColumn() {
        return new AbstractColumn("DefaultLocalsColumn",
                "CTL_LocalsView_Column_Name_Name",
                "CTL_LocalsView_Column_Name_Desc",
                null);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createLocalsToStringColumn() {
        return new AbstractColumn(Constants.LOCALS_TO_STRING_COLUMN_ID,
                "CTL_LocalsView_Column_ToString_Name",
                "CTL_LocalsView_Column_ToString_Desc",
                String.class,
                false);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createLocalsTypeColumn() {
        return new AbstractColumn(Constants.LOCALS_TYPE_COLUMN_ID,
                "CTL_LocalsView_Column_Type_Name",
                "CTL_LocalsView_Column_Type_Desc",
                String.class,
                true);
    }
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createLocalsValueColumn() {
        return new AbstractColumn(Constants.LOCALS_VALUE_COLUMN_ID,
                "CTL_LocalsView_Column_Value_Name",
                "CTL_LocalsView_Column_Value_Desc",
                String.class,
                true);
    }
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createDefaultSessionColumn() {
        return new AbstractColumn("DefaultSessionColumn",
                "CTL_SessionsView_Column_Name_Name",
                "CTL_SessionsView_Column_Name_Desc",
                null);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createSessionHostNameColumn() {
        return new AbstractColumn(Constants.SESSION_HOST_NAME_COLUMN_ID,
                "CTL_SessionsView_Column_HostName_Name",
                "CTL_SessionsView_Column_HostName_Desc",
                String.class,
                false);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createSessionStateColumn () {
        return new AbstractColumn(Constants.SESSION_STATE_COLUMN_ID,
                "CTL_SessionsView_Column_State_Name",
                "CTL_SessionsView_Column_State_Desc",
                String.class,
                true);
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static ColumnModel createSessionLanguageColumn () {
        return new AbstractColumn(Constants.SESSION_LANGUAGE_COLUMN_ID,
                "CTL_SessionsView_Column_Language_Name",
                "CTL_SessionsView_Column_Language_Desc",
                Session.class,
                true,
                new LanguagePropertyEditor ());
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createDefaultThreadColumn() {
        return new AbstractColumn("DefaultThreadColumn",
                "CTL_ThreadsView_Column_Name_Name",
                "CTL_ThreadsView_Column_Name_Desc",
                null);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createThreadStateColumn() {
        return new AbstractColumn(Constants.THREAD_STATE_COLUMN_ID,
                "CTL_ThreadsView_Column_State_Name",
                "CTL_ThreadsView_Column_State_Desc",
                String.class,
                true);
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createThreadSuspendedColumn() {
        return new AbstractColumn(Constants.THREAD_SUSPENDED_COLUMN_ID,
                "CTL_ThreadsView_Column_Suspended_Name",
                "CTL_ThreadsView_Column_Suspended_Desc",
                Boolean.TYPE,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createDefaultWatchesColumn() {
        return new AbstractColumn("DefaultWatchesColumn",
                "CTL_WatchesView_Column_Name_Name",
                "CTL_WatchesView_Column_Name_Desc",
                null);
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createWatchToStringColumn() {
        return new AbstractColumn(Constants.WATCH_TO_STRING_COLUMN_ID,
                "CTL_WatchesView_Column_ToString_Name",
                "CTL_WatchesView_Column_ToString_Desc",
                String.class,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createWatchTypeColumn() {
        return new AbstractColumn(Constants.WATCH_TYPE_COLUMN_ID,
                "CTL_WatchesView_Column_Type_Name",
                "CTL_WatchesView_Column_Type_Desc",
                String.class,
                true);
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static ColumnModel createWatchValueColumn() {
        return new AbstractColumn(Constants.WATCH_VALUE_COLUMN_ID,
                "CTL_WatchesView_Column_Value_Name",
                "CTL_WatchesView_Column_Value_Desc",
                String.class,
                true);
    }

    private static class LanguagePropertyEditor extends PropertyEditorSupport {
        
        public void setValue(Object value) {
            if (value != null && !(value instanceof Session)) {
                ErrorManager.getDefault().notify(
                        new IllegalArgumentException("Value "+value+" is not an instance of Session!"));
            }
            super.setValue(value);
        }

        public String[] getTags () {
            if (getValue () == null) return new String [0];
            String[] s = ((Session) getValue ()).getSupportedLanguages ();
            return s;
        }
        
        public String getAsText () {
            String s = ((Session) getValue ()).getCurrentLanguage ();
            return s;
        }
        
        public void setAsText (String text) {
            ((Session) getValue ()).setCurrentLanguage (text);
        }
    }
}
