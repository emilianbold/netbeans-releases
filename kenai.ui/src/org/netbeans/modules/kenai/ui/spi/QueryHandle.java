/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.beans.PropertyChangeListener;

/**
 * Abstraction of a single issue query (a line in Issues section).
 *
 * @author S. Aubrecht
 */
public abstract class QueryHandle {

    /**
     * The name of property which is fired when any/all query results have changed.
     * The property value should be ideally the list of new QueryResultHandles.
     */
    public static final String PROP_QUERY_RESULT = "queryResult"; // NOI18N

    /**
     * 
     * @return Display name (the text before parenthesis in ui spec)
     */
    public abstract String getDisplayName();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
