/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.ui.models;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ColumnModel;
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
    public abstract static class AbstractColumn extends ColumnModel 
    implements Constants {
        
        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");

        
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
            return properties.getBoolean (getID () + ".visible", true);
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
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static class DefaultBreakpointsColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultBreakpointColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultBreakpointsColumn.class).
                getString ("CTL_BreakpointView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultBreakpointsColumn.class).
                getString ("CTL_BreakpointView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static class BreakpointEnabledColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return BREAKPOINT_ENABLED_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (BreakpointEnabledColumn.class).
                getString ("CTL_BreakpointView_Column_Enabled_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (BreakpointEnabledColumn.class).
                getString ("CTL_BreakpointView_Column_Enabled_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view 
     * representation.
     */
    public static class DefaultCallStackColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultCallStackColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultCallStackColumn.class).
                getString ("CTL_CallstackView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultCallStackColumn.class).
                getString ("CTL_CallstackView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class CallStackLocationColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return CALL_STACK_FRAME_LOCATION_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (CallStackLocationColumn.class).
                getString ("CTL_CallstackView_Column_Location_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (CallStackLocationColumn.class)
            .getString ("CTL_CallstackView_Column_Location_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", false);
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class DefaultLocalsColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultLocalsColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultLocalsColumn.class).
                getString ("CTL_LocalsView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultLocalsColumn.class).
                getString ("CTL_LocalsView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class LocalsToStringColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return LOCALS_TO_STRING_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return LOCALS_VALUE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (LocalsToStringColumn.class).
                getString ("CTL_LocalsView_Column_ToString_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (LocalsToStringColumn.class).
                getString ("CTL_LocalsView_Column_ToString_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", false);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class LocalsTypeColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return LOCALS_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return LOCALS_VALUE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (LocalsTypeColumn.class).
                getString ("CTL_LocalsView_Column_Type_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (LocalsTypeColumn.class).
                getString ("CTL_LocalsView_Column_Type_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class LocalsValueColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return LOCALS_VALUE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return LOCALS_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return LOCALS_TO_STRING_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (LocalsValueColumn.class).
                getString ("CTL_LocalsView_Column_Value_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (LocalsValueColumn.class).
                getString ("CTL_LocalsView_Column_Value_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class DefaultSessionColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultSessionColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultSessionColumn.class).
                getString ("CTL_SessionsView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultSessionColumn.class).
                getString ("CTL_SessionsView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class SessionHostNameColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return SESSION_HOST_NAME_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return SESSION_LANGUAGE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (SessionHostNameColumn.class).
                getString ("CTL_SessionsView_Column_HostName_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (SessionHostNameColumn.class).
                getString ("CTL_SessionsView_Column_HostName_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", false);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class SessionStateColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return SESSION_STATE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return SESSION_HOST_NAME_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (SessionStateColumn.class).
                getString ("CTL_SessionsView_Column_State_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (SessionStateColumn.class).
                getString ("CTL_SessionsView_Column_State_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table 
     * view representation.
     */
    public static class SessionLanguageColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return SESSION_LANGUAGE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return SESSION_STATE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return SESSION_HOST_NAME_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (SessionLanguageColumn.class).
                getString ("CTL_SessionsView_Column_Language_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (SessionLanguageColumn.class).
                getString ("CTL_SessionsView_Column_Language_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Session.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    
        /**
         * Returns {@link java.beans.PropertyEditor} to be used for 
         * this column. Default implementation returns <code>null</code> - 
         * means use default PropertyEditor.
         *
         * @return {@link java.beans.PropertyEditor} to be used for 
         *         this column
         */
        public PropertyEditor getPropertyEditor () {
            return new LanguagePropertyEditor ();
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class DefaultThreadColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultThreadColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultThreadColumn.class).
                getString ("CTL_ThreadsView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultThreadColumn.class).
                getString ("CTL_ThreadsView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class ThreadStateColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return THREAD_STATE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return THREAD_SUSPENDED_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (ThreadStateColumn.class).
                getString ("CTL_ThreadsView_Column_State_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (ThreadStateColumn.class).
                getString ("CTL_ThreadsView_Column_State_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class ThreadSuspendedColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
                return THREAD_SUSPENDED_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return THREAD_STATE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (ThreadSuspendedColumn.class).
                getString ("CTL_ThreadsView_Column_Suspended_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (ThreadSuspendedColumn.class).
                getString ("CTL_ThreadsView_Column_Suspended_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", false);
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class DefaultWatchesColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultWatchesColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DefaultWatchesColumn.class).
                getString ("CTL_WatchesView_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (DefaultWatchesColumn.class).
                getString ("CTL_WatchesView_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class WatchToStringColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return WATCH_TO_STRING_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return WATCH_VALUE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (WatchToStringColumn.class).
                getString ("CTL_WatchesView_Column_ToString_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (WatchToStringColumn.class).
                getString ("CTL_WatchesView_Column_ToString_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", false);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class WatchTypeColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return WATCH_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return WATCH_VALUE_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (WatchTypeColumn.class).
                getString ("CTL_WatchesView_Column_Type_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (WatchTypeColumn.class).
                getString ("CTL_WatchesView_Column_Type_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree 
     * table view representation.
     */
    public static class WatchValueColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return WATCH_VALUE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return WATCH_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return WATCH_TO_STRING_COLUMN_ID;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (WatchValueColumn.class).
                getString ("CTL_WatchesView_Column_Value_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return NbBundle.getBundle (WatchValueColumn.class).
                getString ("CTL_WatchesView_Column_Value_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return String.class;
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", true);
        }
    }
    
    public static class LanguagePropertyEditor extends PropertyEditorSupport {
        
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
