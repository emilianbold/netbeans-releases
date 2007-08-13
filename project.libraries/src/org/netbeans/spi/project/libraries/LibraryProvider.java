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
package org.netbeans.spi.project.libraries;

import java.beans.PropertyChangeListener;

/**
 * Provider interface for implementing the read only library storage.
 * Library storage is a source of libraries used by LibraryManager.
 * LibraryManager allows existence of multiple LibraryProviders registered in
 * the default lookup.
 */
public interface LibraryProvider {

    /**
     * Name of libraries property
     */
    public static final String PROP_LIBRARIES = "libraries";        //NOI18N

    /**
     * Returns libraries provided by the implemented provider.
     * @return LibraryImplementation[] never return null, may return empty array.
     */
    public LibraryImplementation[] getLibraries();

    /**
     * Adds property change listener, the listener is notified when the libraries changed
     * @param listener
     */
    public void addPropertyChangeListener (PropertyChangeListener listener);

    /**
     * Removes property change listener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener);
}
