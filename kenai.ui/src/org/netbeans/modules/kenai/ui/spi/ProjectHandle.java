/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.kenai.api.Kenai;

/**
 * Abstraction of a single Kenai project.
 *
 * @author S. Aubrecht
 */
public abstract class ProjectHandle implements Comparable<ProjectHandle> {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * The name of property which when fired will force a complete refresh of
     * all project related info.
     * The property value is undefined.
     */
    public static final String PROP_CONTENT = "content"; // NOI18N
    /**
     * The name of property which is fired when the list of builds for this project
     * has changed (builds added/removed/renamed).
     * The property value should ideally be the new list of BuildHandles.
     */
    public static final String PROP_BUILD_LIST = "buildList"; // NOI18N
    /**
     * The name of property which is fired when the list of source repositories
     * for this project has changed (repos added/removed/renamed).
     * The property value should ideally be the new list of SourceHandles.
     */
    public static final String PROP_SOURCE_LIST = "sourceList"; // NOI18N
    /**
     * The name of property which is fired when the list of queries
     * for this project has changed (queries added/removed/renamed).
     * The property value should ideally be the new list of QueryHandles.
     */
    public static final String PROP_QUERY_LIST = "queryList"; // NOI18N

    /**
     * The name of property which is fired when the nonmember project is removed
     * from dashboard.
     * This event is not fired, when user logs out.
     * Value is undefined (null)
     * @see Kenai#PROP_LOGIN
     */
    public static final String PROP_CLOSE = "close"; // NOI18N


    
    private final String id;

    protected ProjectHandle( String id ) {
        this.id = id;
    }

    /**
     *
     * @return Project's unique identification
     */
    public final String getId() {
        return id;
    }

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     * Is this project private?
     * @return 
     */
    public abstract boolean isPrivate();

    public final void addPropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.addPropertyChangeListener(l);
    }

    public final void removePropertyChangeListener( PropertyChangeListener l ) {
        changeSupport.removePropertyChangeListener(l);
    }

    public final void firePropertyChange( String propName, Object oldValue, Object newValue ) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
    }

    public int compareTo( ProjectHandle other ) {
        return getDisplayName().compareToIgnoreCase(other.getDisplayName());
    }

    @Override
    public boolean equals(Object obj) {
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final ProjectHandle other = (ProjectHandle) obj;
        if( (this.id == null) ? (other.id != null) : !this.id.equals(other.id) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getId();
    }
}

