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

package org.netbeans.api.project.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.modules.project.libraries.LibraryAccessor;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Library models typed bundle of typed volumes.
 * <p>
 * Library volumes are typed and query-able by their type. The type is
 * represented by type string. Strictly speaking volumes are
 * named rather then typed but the name express their type.
 * The volume is a list of resoruces.
 * <p>
 * For more details see <a href="package-summary.html">libraries overview</a>.
 * @author Petr Kuzel, Tomas Zezula
 */
public final class Library {
    
    public static final String PROP_NAME = "name";                  //NOI18N
    public static final String PROP_DESCRIPTION = "description";    //NOI18N
    public static final String PROP_CONTENT = "content";            //NOI18N

    // delegating peer
    private LibraryImplementation impl;

    private List<PropertyChangeListener> listeners;

    /**
     * Creates new library instance
     *
     */
    private Library (LibraryImplementation impl) {
        this.impl = impl;
        this.impl.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                Library.this.fireChange (propName,evt.getOldValue(),evt.getNewValue());
            }
        });
    } // end create

    /**
     * Access typed but raw library data.
     * <p>
     * The contents are defined by SPI providers and identified
     * by the <a href="package-summary.html#volumeType">volume types</a>. For example the j2se library supports the following
     * volume types: classpath - the library classpath roots, src - the library sources, javadoc - the library javadoc.
     * Your module must have contract with a particular provider's module to be able to query it effectively.
     * </p>
     *
     * @param volumeType which resources to return.
     * @return path of URLs of given type (possibly empty but never <code>null</code>)
     */
    public List<URL> getContent(final String volumeType) {
        return this.impl.getContent (volumeType);
    } // end getContent



    /**
     * Get library binding name. The name identifies library
     * in scope of one libraries storage.
     * <p>
     *
     * @return String with library name
     */
    public String getName() {
        return impl.getName();
    } // end getName


    /**
     * Returns description of the library.
     * The description provides more detailed information about the library.
     * @return String the description or null if the description is not available
     */
    public String getDescription () {
        return this.getLocalizedString(this.impl.getLocalizingBundle(),this.impl.getDescription());
    }


    /**
     * Returns the display name of the library.
     * The display name is either equal to the name or
     * is a localized version of the name.
     * @return String the display name, never returns null.
     */
    public String getDisplayName () {
        return this.getLocalizedString(this.impl.getLocalizingBundle(),this.impl.getName());
    }


    /**
     * Gets the type of library. The library type identifies
     * the provider which has created the library and implies
     * the volues contained in it.
     * @return String (e.g. j2se for J2SE library)
     */
    public String getType () {
        return this.impl.getType();
    }


    // delegated identity
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Library) {
            Library peer = (Library) obj;
            return peer.impl.equals(impl);
        }
        return false;
    }

    // delegated identity
    @Override
    public int hashCode() {
        return impl.hashCode();
    }
    
    @Override
    public String toString () {
        return this.getName();
    }

    /**
     * Adds PropertyChangeListener
     * @param listener
     */
    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        if (this.listeners == null)
            this.listeners = new ArrayList<PropertyChangeListener>();
        this.listeners.add (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public synchronized void removePropertyChangeListener (PropertyChangeListener listener) {
        if (this.listeners == null)
            return;
        this.listeners.remove (listener);
    }


    LibraryImplementation getLibraryImplementation () {
        return this.impl;
    }

    private void fireChange (String propertyName, Object oldValue, Object newValue) {
        List<PropertyChangeListener> ls;
        synchronized (this) {
            if (this.listeners == null)
                return;
            ls = new ArrayList<PropertyChangeListener>(listeners);
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propertyName, oldValue, newValue);
        for (PropertyChangeListener l : ls) {
            l.propertyChange(event);
        }
    }


    private String getLocalizedString (String bundleName, String key) {
        if (key == null) {
            return null;
        }
        if (bundleName == null) {
            return key;
        }
        ResourceBundle bundle;
        try {
            bundle = NbBundle.getBundle(bundleName);
        } catch (MissingResourceException mre) {
            // Bogus bundle.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mre);
            return key;
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException mre) {
            // OK, not required to be there.
            return key;
        }
    }
    
    static {
        LibraryAccessor.DEFAULT = new LibraryAccessor () {
            public Library createLibrary (LibraryImplementation impl) {
                return new Library (impl);
            }
        };
    }

} // end Library

