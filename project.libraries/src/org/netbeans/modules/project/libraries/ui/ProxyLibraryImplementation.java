/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.libraries.ui;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.WeakListeners;
/**
 *
 * @author  tom
 */
public class ProxyLibraryImplementation implements LibraryImplementation, PropertyChangeListener  {
    
    private final LibraryImplementation original;
    private final LibrariesModel model;
    private Map newContents;
    private String newName;
    private String newDescription;
    private PropertyChangeSupport support;

    /** Creates a new instance of ProxyLibraryImplementation */
    public ProxyLibraryImplementation (LibraryImplementation original, LibrariesModel model) {
        assert original != null && model != null;
        this.original = original;
        this.model = model;
        this.original.addPropertyChangeListener((PropertyChangeListener)WeakListeners.create(PropertyChangeListener.class,this,this.original));
        this.support = new PropertyChangeSupport (this);
    }
    
    public LibraryImplementation getOriginal () {
        return this.original;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.support.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.support.removePropertyChangeListener(l);
    }
    
    public String getType() {
        return this.original.getType ();
    }
    
    
    public synchronized List getContent(String volumeType) throws IllegalArgumentException {
        List result = null;
        if (this.newContents == null || (result=(List)this.newContents.get(volumeType)) == null) {
            return this.original.getContent (volumeType);
        }
        else {
            return result;
        }
    }
    
    public synchronized String getDescription() {
        if (this.newDescription != null) {
            return this.newDescription;
        }
        else {
            return this.original.getDescription();
        }
    }
    
    public synchronized String getName() {
        if (this.newName != null) {
            return this.newName;
        }
        else {
            return this.original.getName ();
        }
    }
    
    public synchronized void setContent(String volumeType, List path) throws IllegalArgumentException {
        if (this.newContents == null) {
            this.newContents = new HashMap ();
        }
        this.newContents.put (volumeType, path);
        this.model.modifyLibrary(this);
        this.support.firePropertyChange(PROP_CONTENT,null,null);        //NOI18N
    }
    
    public synchronized void setDescription(String text) {
        String oldDescription = this.newDescription == null ? this.original.getDescription() : this.newDescription;
        this.newDescription = text;
        this.model.modifyLibrary(this);
        this.support.firePropertyChange(PROP_DESCRIPTION,oldDescription,this.newDescription);   //NOI18N
    }
    
    public synchronized void setName(String name) {
        String oldName = this.newName == null ? this.original.getName() : this.newName;
        this.newName = name;
        this.model.modifyLibrary(this);
        this.support.firePropertyChange(PROP_NAME,oldName,this.newName);       //NOI18N
    }


    public String getLocalizingBundle() {
        return this.original.getLocalizingBundle();
    }

    public void setLocalizingBundle(String resourceName) {
        throw new UnsupportedOperationException();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        this.support.firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
    }


    public final int hashCode() {
        return this.original.hashCode();
    }

    public final boolean equals(Object obj) {
        if (obj instanceof ProxyLibraryImplementation) {
            return this.original.equals(((ProxyLibraryImplementation)obj).getOriginal());
        }
        else
            return false;
    }

    public final String toString() {
        return "Proxy for: " + this.original.toString();    //NOI18N
    }

}
