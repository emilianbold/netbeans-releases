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
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ColumnModel;


/**
 * Defines model for one table view column. Can be used together with 
 * {@link TreeModel} for tree table view representation.
 *
 * @author   Jan Jancura
 */
public class ColumnModels {
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class BreakpointEnabledColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return BREAKPOINT_ENABLED_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "Enabled";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Disables / enables breakpoint";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class CallStackLocationColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return CALL_STACK_FRAME_LOCATION_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
        }

        /**
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "Location";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Location of callstack frame";
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
        public boolean initiallyVisible () {
            return false;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class LocalsToStringColumn extends ColumnModel implements Constants {

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
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "toString ()";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Returns value of toString () method call";
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
        public boolean initiallyVisible () {
            return false;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class LocalsTypeColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return LOCALS_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
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
            return "Type";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Type of variable";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class LocalsValueColumn extends ColumnModel implements Constants {

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
            return "Value";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Value of variable";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class SessionHostNameColumn extends ColumnModel implements Constants {

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
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "Host Name";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Computer name this session is running on";
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
        public boolean initiallyVisible () {
            return false;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class SessionStateColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return SESSION_STATE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
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
            return "State";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Describes state of session";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class SessionLanguageColumn extends ColumnModel implements Constants {

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
            return "Language";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Current language of session";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
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
     * {@link TreeModel} for tree table view representation.
     */
    public static class ThreadStateColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return THREAD_STATE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
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
            return "State";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Describes state of thread";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class ThreadSuspendedColumn extends ColumnModel implements Constants {

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
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "Suspended";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Is thread suspended by debugger?";
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
        public boolean initiallyVisible () {
            return false;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class WatchToStringColumn extends ColumnModel implements Constants {

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
         * Returns ID of column next to this one.
         *
         * @return ID of column next to this one
         */
        public String getNextColumnID () {
            return null;
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return "toString ()";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Returns value of toString () method call";
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
        public boolean initiallyVisible () {
            return false;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class WatchTypeColumn extends ColumnModel implements Constants {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return WATCH_TYPE_COLUMN_ID;
        }

        /**
         * Returns ID of column previous to this one.
         *
         * @return ID of column previous to this one
         */
        public String getPreviuosColumnID () {
            return null;
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
            return "Type";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Type of variable";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }

    /**
     * Defines model for one table view column. Can be used together with 
     * {@link TreeModel} for tree table view representation.
     */
    public static class WatchValueColumn extends ColumnModel implements Constants {

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
            return "Value";
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        public String getShortDescription () {
            return "Value of variable";
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
        public boolean initiallyVisible () {
            return true;
        }

        /**
         * True if column can be sorted.
         *
         * @return true if column can be sorted
         */
        public boolean isSortable () {
            return true;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        public boolean initiallySorted () {
            return false;
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending order
         */
        public boolean initiallySortedDescending () {
            return false;
        }
    }
    
    public static class LanguagePropertyEditor extends PropertyEditorSupport {
        
        public String[] getTags () {
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
