/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.project.libraries.ui;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.openide.util.WeakListeners;

/**
 *
 * @author  tom
 */
public class ProxyLibraryImplementation implements LibraryImplementation, PropertyChangeListener  {

    private final LibraryImplementation original;
    private final LibrariesModel model;
    Map<String,List<URL>> newContents;
    private String newName;
    private String newDescription;
    private PropertyChangeSupport support;

    private ProxyLibraryImplementation (LibraryImplementation original, LibrariesModel model) {
        assert original != null && model != null;
        this.original = original;
        this.model = model;
        this.original.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, this.original));
        this.support = new PropertyChangeSupport (this);
    }
    
    public static ProxyLibraryImplementation createProxy(LibraryImplementation original, LibrariesModel model) {
        if (original instanceof LibraryImplementation2) {
            return new ProxyLibraryImplementation2((LibraryImplementation2)original, model);
        } else {
            return new ProxyLibraryImplementation(original, model);
        }
    }

    protected LibrariesModel getModel() {
        return model;
    }

    protected PropertyChangeSupport getSupport() {
        return support;
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
    
    
    public synchronized List<URL> getContent(String volumeType) throws IllegalArgumentException {
        List<URL> result = null;
        if (newContents == null || (result = newContents.get(volumeType)) == null) {
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
    
    public synchronized void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
        if (this.newContents == null) {
            this.newContents = new HashMap<String,List<URL>>();
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


    public int hashCode() {
        return this.original.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ProxyLibraryImplementation) {
            return this.original.equals(((ProxyLibraryImplementation)obj).getOriginal());
        }
        else
            return false;
    }

    public @Override String toString() {
        return "Proxy[" + original + "]"; // NOI18N
    }

    static class ProxyLibraryImplementation2 extends ProxyLibraryImplementation implements LibraryImplementation2 {

        Map<String,List<URI>> newURIContents;
        
        public ProxyLibraryImplementation2(LibraryImplementation2 original, LibrariesModel model) {
            super(original, model);
        }

        LibraryImplementation2 getOriginal2() {
            return (LibraryImplementation2)getOriginal();
        }
        
        public List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
            List<URI> result = null;
            if (newURIContents == null || (result = newURIContents.get(volumeType)) == null) {
                return getOriginal2().getURIContent (volumeType);
            } else {
                return result;
            }
        }

        public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
            if (newURIContents == null) {
                newURIContents = new HashMap<String,List<URI>>();
            }
            newURIContents.put(volumeType, path);
            getModel().modifyLibrary(this);
            getSupport().firePropertyChange(PROP_CONTENT,null,null);
        }
        
        public final int hashCode() {
            return getOriginal().hashCode();
        }

        public final boolean equals(Object obj) {
            if (obj instanceof ProxyLibraryImplementation2) {
                return getOriginal().equals(((ProxyLibraryImplementation2)obj).getOriginal());
            }
            else
                return false;
        }

        public @Override String toString() {
            return "Proxy2[" + getOriginal() + "]"; // NOI18N
        }
    }
}
