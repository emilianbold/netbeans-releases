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

package org.netbeans.spi.viewmodel;

import java.util.EventObject;


/**
 * Encapsulates information describing changes to a model, and
 * used to notify model listeners of the change.
 *
 * @author   Jan Jancura
 * @since 1.4
 */
public class ModelEvent extends EventObject {
    
    private ModelEvent (Object source) {
        super (source);
    }
    
    /**
     * Used to notify that whole content of tree has been changed.
     *
     * @since 1.4
     */
    public static class TreeChanged extends ModelEvent {
       
        /**
         * Creates a new instance of TreeChanged event.
         *
         * @param source a source if event.
         *
         * @since 1.4
         */
        public TreeChanged (Object source) {
            super (source);
        }
    }
    
    /**
     * Used to notify that one cell in table has been changed.
     *
     * @since 1.4
     */
    public static class TableValueChanged extends ModelEvent {
        
        private Object node;
        private String columnID;
        
        /**
         * Creates a new instance of TableValueChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance
         * @param columnID a changed column name
         *
         * @since 1.4
         */
        public TableValueChanged (
            Object source, 
            Object node,
            String columnID
        ) {
            super (source);
            this.node = node;
            this.columnID = columnID;
        }
        
        /**
         * Returns changed node instance.
         *
         * @return changed node instance
         *
         * @since 1.4
         */
        public Object getNode () {
            return node;
        }
        
        /**
         * Returns changed column name.
         *
         * @return changed column name
         *
         * @since 1.4
         */
        public String getColumnID () {
            return columnID;
        }
    }
    
    /**
     * Used to notify that one node has been changed (icon, displayName and 
     * children).
     *
     * @since 1.4
     */
    public static class NodeChanged extends ModelEvent {
        
        private Object node;
        
        /**
         * Creates a new instance of NodeChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance
         *
         * @since 1.4
         */
        public NodeChanged (
            Object source, 
            Object node
        ) {
            super (source);
            this.node = node;
        }
        
        /**
         * Returns changed node instance.
         *
         * @return changed node instance
         *
         * @since 1.4
         */
        public Object getNode () {
            return node;
        }
    }
}
