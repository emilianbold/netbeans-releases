/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public final class DefaultLibraryImplementation implements LibraryImplementation {

    private String description;

    // map<ContentType, FileSet>
    private Map contents;

    // library 'binding name' as given by user
    private String name;

    private String libraryType;

    private String localizingBundle;


    //Listeners support
    private ArrayList listeners;

    /**
     * Create new LibraryImplementation supporting given <tt>library</tt>.
     */
    public DefaultLibraryImplementation (String libraryType, String[] volumeTypes) {
        assert libraryType != null && volumeTypes != null;
        this.libraryType = libraryType;
        this.contents = new HashMap();
        for (int i=0; i<volumeTypes.length; i++) {
            this.contents.put (volumeTypes[i],Collections.EMPTY_LIST);
        }
    }


    public String getType() {
        return libraryType;
    }

    public void setName(final String name) throws UnsupportedOperationException {
        String oldName = this.name;
        this.name = name;
        this.firePropertyChange (PROP_NAME, oldName, this.name);
    }

    public String getName() {
        return name;
    }

    public List getContent (String contentType) throws IllegalArgumentException {
        List content = (List) contents.get(contentType);
        if (content == null)
            throw new IllegalArgumentException ();
        return Collections.unmodifiableList (content);
    }

    public void setContent (String contentType, List path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException ();
        }
        if (this.contents.keySet().contains(contentType)) {
            this.contents.put (contentType, new ArrayList(path));
            this.firePropertyChange(PROP_CONTENT,null,null);
        } else {
            throw new IllegalArgumentException ("Volume '"+contentType+
                "' is not support by this library. The only acceptable values are: "+contents.keySet());
        }
    }

    public String getDescription () {
            return this.description;
    }

    public void setDescription (String text) {
        String oldDesc = this.description;
        this.description = text;
        this.firePropertyChange (PROP_DESCRIPTION, oldDesc, this.description);
    }

    public String getLocalizingBundle() {
        return this.localizingBundle;
    }

    public void setLocalizingBundle(String resourceName) {
        this.localizingBundle = resourceName;
    }

    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        if (this.listeners == null)
            this.listeners = new ArrayList ();
        this.listeners.add (l);
    }

    public synchronized void removePropertyChangeListener (PropertyChangeListener l) {
        if (this.listeners == null)
            return;
        this.listeners.remove (l);
    }

    public String toString () {
        return "LibraryImplementation[Name="+this.name+"]"; //NOI18N
    }

    public boolean equals (Object o) {
        if (o instanceof DefaultLibraryImplementation) {
            DefaultLibraryImplementation other = (DefaultLibraryImplementation) o;
            return this.name == null ? other.name == null : this.name.equals(other.name);
        }
        return false;
    }

    public int hashCode () {
        return this.name == null ? 0 : this.name.hashCode();
    }

    private void firePropertyChange (String propName, Object oldValue, Object newValue) {
        Iterator it;
        synchronized (this) {
            if (this.listeners == null)
                return;
            it = ((List)this.listeners.clone()).iterator();
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        while (it.hasNext()) {
            ((PropertyChangeListener)it.next()).propertyChange (event);
        }
    }
}
