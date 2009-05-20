/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.api.project.libraries;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import org.netbeans.modules.project.libraries.LibraryAccessor;
import org.netbeans.modules.project.libraries.ui.LibrariesModel;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

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

    private static final Logger LOG = Logger.getLogger(Library.class.getName());

    // delegating peer
    private LibraryImplementation impl;

    private List<PropertyChangeListener> listeners;

    private final LibraryManager manager;

    private final PropertyChangeListener listener;

    Library(LibraryImplementation impl, LibraryManager manager) {
        this.impl = impl;
        this.listener = new PropertyChangeListener () {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                Library.this.fireChange (propName,evt.getOldValue(),evt.getNewValue());
            }
        };
        this.impl.addPropertyChangeListener (WeakListeners.propertyChange(listener, this.impl));
        this.manager = manager;
    } // end create

    /**
     * Gets the associated library manager.
     * @return the manager (may be the "default" global manager, or a local manager)
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public LibraryManager getManager() {
        return manager;
    }

    /**
     * Access typed raw library data as URLs.
     * <p>
     * The contents are defined by SPI providers and identified
     * by the <a href="package-summary.html#volumeType">volume types</a>. For example the j2se library supports the following
     * volume types: classpath - the library classpath roots, src - the library sources, javadoc - the library javadoc.
     * Your module must have contract with a particular provider's module to be able to query it effectively.
     * </p>
     *
     * @param volumeType which resources to return.
     * @return list of URLs of given volume type (possibly empty but never <code>null</code>)
     */
    public List<URL> getContent(final String volumeType) {
        return impl.getContent (volumeType);
    } // end getContent

    /**
     * Access typed raw library data as possibly relative URIs.
     * <p>
     * The contents are defined by SPI providers and identified
     * by the <a href="package-summary.html#volumeType">volume types</a>. For example the j2se library supports the following
     * volume types: classpath - the library classpath roots, src - the library sources, javadoc - the library javadoc.
     * Your module must have contract with a particular provider's module to be able to query it effectively.
     * </p>
     *
     * @param volumeType which resources to return.
     * @return list of URIs of given volume type (possibly empty but never <code>null</code>)
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public List<URI> getURIContent(final String volumeType) {
        if (impl instanceof LibraryImplementation2) {
            return ((LibraryImplementation2)impl).getURIContent(volumeType);
        } else {
            return LibrariesModel.convertURLsToURIs(impl.getContent(volumeType));
        }
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
            LOG.warning("No such bundle " + bundleName + " for " + getName());
            return key;
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException mre) {
            LOG.warning("No such key " + key + " in " + bundleName + " for " + getName());
            return key;
        }
    }

    @Override
    public String toString() {
        return "Library[" + getName() + "]"; // NOI18N
    }
    
    static {
        LibraryAccessor.setInstance( new LibraryAccessor () {
            public Library createLibrary (LibraryImplementation impl) {
                return new Library(impl, LibraryManager.getDefault());
            }
        });
    }

} // end Library

