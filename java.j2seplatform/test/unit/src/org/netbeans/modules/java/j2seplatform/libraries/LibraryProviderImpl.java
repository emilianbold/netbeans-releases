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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;

/**
 * Implementation of library provider for unit testing.
 *
 * @author  David Konecny
 */
public class LibraryProviderImpl implements org.netbeans.spi.project.libraries.LibraryProvider {
    
    private ArrayList libs = new ArrayList();
    private PropertyChangeSupport support;
    
    private static LibraryProviderImpl DEFAULT;
    
    public static LibraryProviderImpl getDefault() {
        assert DEFAULT != null;
        return DEFAULT;
    }
    
    public LibraryProviderImpl() {
        support = new PropertyChangeSupport(this);
        DEFAULT = this;
    }
    
    public void addLibrary(LibraryImplementation library) throws IOException {
        libs.add(library);
        fireChange();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public LibraryImplementation[] getLibraries() {
        return (LibraryImplementation[])libs.toArray(new LibraryImplementation[libs.size()]);
    }
    
    public void init() throws IOException {
    }
    
    public void removeLibrary(LibraryImplementation library) throws IOException {
        boolean res = libs.remove(library);
        assert res : "Removing library which is not in this provider "+library;
        fireChange();
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void fireChange() {
        support.firePropertyChange("xxx", null, null);
    }
    
}
