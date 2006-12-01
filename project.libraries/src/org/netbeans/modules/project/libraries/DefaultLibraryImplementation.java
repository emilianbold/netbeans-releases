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

package org.netbeans.modules.project.libraries;

import java.net.URL;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public final class DefaultLibraryImplementation implements LibraryImplementation {

    private String description;

    private Map<String,List<URL>> contents;

    // library 'binding name' as given by user
    private String name;

    private String libraryType;

    private String localizingBundle;

    private List<PropertyChangeListener> listeners;

    /**
     * Create new LibraryImplementation supporting given <tt>library</tt>.
     */
    public DefaultLibraryImplementation (String libraryType, String[] volumeTypes) {
        assert libraryType != null && volumeTypes != null;
        this.libraryType = libraryType;
        this.contents = new HashMap<String,List<URL>>();
        for (String vtype : volumeTypes) {
            this.contents.put(vtype, Collections.<URL>emptyList());
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

    public List<URL> getContent(String contentType) throws IllegalArgumentException {
        List<URL> content = contents.get(contentType);
        if (content == null)
            throw new IllegalArgumentException ();
        return Collections.unmodifiableList (content);
    }

    public void setContent(String contentType, List<URL> path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException ();
        }
        if (this.contents.keySet().contains(contentType)) {
            this.contents.put(contentType, new ArrayList<URL>(path));
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
            this.listeners = new ArrayList<PropertyChangeListener>();
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

    private void firePropertyChange (String propName, Object oldValue, Object newValue) {
        List<PropertyChangeListener> ls;
        synchronized (this) {
            if (this.listeners == null)
                return;
            ls = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        for (PropertyChangeListener l : ls) {
            l.propertyChange(event);
        }
    }
}
