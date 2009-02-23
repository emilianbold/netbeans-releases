/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstraction of a single Kenai project.
 *
 * @author S. Aubrecht
 */
public abstract class ProjectHandle {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * The name of property which is fired when the list of builds for this project
     * has changed (builds added/removed/renamed).
     * The property value should ideally be the new list of BuildHandles.
     */
    public static final String PROP_BUILD_LIST = "buildList";
    /**
     * The name of property which is fired when the list of source repositories
     * for this project has changed (repos added/removed/renamed).
     * The property value should ideally be the new list of SourceHandles.
     */
    public static final String PROP_SOURCE_LIST = "sourceList";
    /**
     * The name of property which is fired when the list of queries
     * for this project has changed (queries added/removed/renamed).
     * The property value should ideally be the new list of QueryHandles.
     */
    public static final String PROP_QUERY_LIST = "queryList";

    /**
     *
     * @return Project's unique identification
     */
    public abstract String getId();

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    public final void addPropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.addPropertyChangeListener(l);
    }

    public final void removePropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.removePropertyChangeListener(l);
    }

    protected final void firePropertyChange( String propName, Object oldValue, Object newValue ) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
    }
}

