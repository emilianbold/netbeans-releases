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
        
        /**
         * The mask for display name change.
         * @since 1.6
         */
        public static final int DISPLAY_NAME_MASK = 1;
        /**
         * The mask for icon change.
         * @since 1.6
         */
        public static final int ICON_MASK = 2;
        /**
         * The mask for short description change.
         * @since 1.6
         */
        public static final int SHORT_DESCRIPTION_MASK = 4;
        /**
         * The mask for children change.
         * @since 1.6
         */
        public static final int CHILDREN_MASK = 8;
        
        private Object node;
        private int change;
        
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
            this (source, node, 0xFFFFFFFF);
        }
        
        /**
         * Creates a new instance of NodeChanged event.
         *
         * @param source a source if event.
         * @param node a changed node instance.
         * @param change one of the *_MASK constant or their aggregation.
         *
         * @since 1.6
         */
        public NodeChanged(Object source, Object node, int change) {
            super (source);
            this.node = node;
            this.change = change;
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
         * Get the change mask.
         *
         * @return the change mask, one of the *_MASK constant or their aggregation.
         * @since 1.6
         */
        public int getChange() {
            return change;
        }
    }
}
