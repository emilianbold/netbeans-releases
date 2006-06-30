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
    
    private static final LibraryProviderImpl DEFAULT = new LibraryProviderImpl();
    
    public static LibraryProviderImpl getDefault() {
        assert DEFAULT != null;
        return DEFAULT;
    }
    
    private LibraryProviderImpl() {
        support = new PropertyChangeSupport(this);
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
