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

package org.netbeans.modules.project.libraries.ui;


import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;


import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.modules.project.libraries.WritableLibraryProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.ErrorManager;

/**
 *
 * @author  tom
 */
class LibrariesModel extends javax.swing.AbstractListModel implements PropertyChangeListener, LookupListener {

    private List<LibraryImplementation> actualLibraries;
    private List<LibraryImplementation> addedLibraries;
    private List<LibraryImplementation> removedLibraries;
    private List<ProxyLibraryImplementation> changedLibraries;
    private Collection<? extends LibraryProvider> currentStorages;
    private Map<LibraryImplementation,LibraryProvider> storageByLib;
    private Lookup.Result<LibraryProvider> lresult;
    private WritableLibraryProvider writableProvider;

    /** Creates a new instance of LibrariesModel */
    public LibrariesModel () {
        this.addedLibraries = new ArrayList<LibraryImplementation>();
        this.removedLibraries = new ArrayList<LibraryImplementation>();
        this.changedLibraries = new ArrayList<ProxyLibraryImplementation>();
        this.currentStorages = Collections.emptySet();
        this.storageByLib = new HashMap<LibraryImplementation,LibraryProvider>();
        this.getLibraries ();
    }
    
    public Object getElementAt(int index) {
        if (index < 0 || index >= this.actualLibraries.size())
            return null;
        return actualLibraries.get(index);
    }    
    
    public int getSize() {
        return this.actualLibraries.size();
    }

    public void addLibrary (LibraryImplementation impl) {
        this.addedLibraries.add (impl);
        int index=0;
        Comparator<LibraryImplementation> c = new LibrariesComparator();
        for (; index < this.actualLibraries.size(); index++) {
            LibraryImplementation tmp = this.actualLibraries.get(index);
            if (c.compare(impl,tmp)<0)
                break;
        }
        this.actualLibraries.add (index, impl);
        this.fireIntervalAdded (this,index,index);
    }

    public void removeLibrary (LibraryImplementation impl) {
        if (this.addedLibraries.contains(impl)) {
            this.addedLibraries.remove(impl);
        }
        else {
            this.removedLibraries.add (((ProxyLibraryImplementation)impl).getOriginal());
        }
        int index = this.actualLibraries.indexOf(impl);
        this.actualLibraries.remove(index);
        this.fireIntervalRemoved(this,index,index);
    }

    public void modifyLibrary(ProxyLibraryImplementation impl) {
        if (!this.addedLibraries.contains (impl) && !this.changedLibraries.contains(impl)) {
            this.changedLibraries.add(impl);
        }
        int index = this.actualLibraries.indexOf (impl);
        this.fireContentsChanged(this,index,index);
    }

    public boolean isLibraryEditable (LibraryImplementation impl) {
        if (this.addedLibraries.contains(impl))
            return true;
        LibraryProvider provider = storageByLib.get
                (((ProxyLibraryImplementation)impl).getOriginal());
        //Todo: Currently just one WritableLibraryProvider
        //Todo: if changed, must be rewritten to handle it
        return (provider == this.writableProvider);
    }

    public void apply () throws IOException {
        //Todo: Currently just one WritableLibraryProvider
        //Todo: if changed, must be rewritten to handle it
        for (LibraryImplementation impl : removedLibraries) {
            LibraryProvider storage = storageByLib.get(impl);
            if (storage == this.writableProvider) {
                this.writableProvider.removeLibrary (impl);
            }
            else {
                ErrorManager.getDefault().log ("Can not find storage for library: "+impl.getName());    //NOI18N
            }
        }
        if (this.writableProvider != null) {
            for (LibraryImplementation impl : addedLibraries) {
                writableProvider.addLibrary(impl);
            }
        }
        else {
            ErrorManager.getDefault().log("Cannot add libraries, no WritableLibraryProvider."); //NOI18N
        }
        for (ProxyLibraryImplementation proxy : changedLibraries) {
            LibraryProvider storage = storageByLib.get(proxy.getOriginal());
            if (storage == this.writableProvider) {
                this.writableProvider.updateLibrary (proxy.getOriginal(), proxy);
            }
            else {
                ErrorManager.getDefault().log ("Can not find storage for library: "+proxy.getOriginal().getName());  //NOI18N
            }
        }
        cleanUp ();
    }

    public void cancel() {
        cleanUp ();
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        this.storagesChanged();
    }

    public void resultChanged(LookupEvent ev) {
        this.storagesChanged();
    }

    public void storagesChanged () {
        int oldSize;
        synchronized (this) {
            oldSize = this.actualLibraries == null ? 0 : this.actualLibraries.size();
            getLibraries();
        }
        this.fireContentsChanged(this, 0, Math.max(oldSize,this.actualLibraries.size()));
    }

    private LibraryImplementation findModified (LibraryImplementation impl) {
        for (Iterator it = changedLibraries.iterator(); it.hasNext();) {
            ProxyLibraryImplementation proxy = (ProxyLibraryImplementation) it.next();
            if (proxy.getOriginal().equals (impl)) {
                return proxy;
            }
        }
        return null;
    }

    private synchronized void cleanUp () {
        this.addedLibraries.clear();
        this.removedLibraries.clear();
        this.changedLibraries.clear();
        for (LibraryProvider p : currentStorages) {
            p.removePropertyChangeListener (this);
        }
        this.currentStorages = Collections.emptySet();
    }

    private synchronized void getLibraries () {
        List<LibraryImplementation> libraries = new ArrayList<LibraryImplementation>();
        if (this.lresult == null) {
            //First time
            this.lresult = Lookup.getDefault().lookupResult(LibraryProvider.class);
            this.lresult.addLookupListener (this);
        }
        Collection<? extends LibraryProvider> instances = this.lresult.allInstances();
        Collection<LibraryProvider> toAdd = new HashSet<LibraryProvider>(instances);
        toAdd.removeAll(this.currentStorages);
        Collection<LibraryProvider> toRemove = new HashSet<LibraryProvider>(this.currentStorages);
        toRemove.removeAll (instances);
        this.currentStorages = instances;
        this.storageByLib.clear();
        for (LibraryProvider storage : instances) {
            //TODO: in case of more WritableLibraryProvider must be changed
            if (this.writableProvider == null && storage instanceof WritableLibraryProvider) {
                this.writableProvider = (WritableLibraryProvider) storage;
            }
            for (LibraryImplementation lib : storage.getLibraries()) {
                LibraryImplementation proxy = null;
                if (removedLibraries.contains(lib)) {
                    this.storageByLib.put (lib,storage);
                }
                else if ((proxy = findModified (lib))!=null) {
                    libraries.add (proxy);
                    this.storageByLib.put (lib,storage);
                }
                else {
                    libraries.add(new ProxyLibraryImplementation(lib,this));
                    this.storageByLib.put (lib,storage);
                }
            }
        }
        libraries.addAll (this.addedLibraries);
        Collections.sort(libraries, new LibrariesComparator());

        for (LibraryProvider p : toRemove) {
            p.removePropertyChangeListener(this);
        }
        for (LibraryProvider p : toAdd) {
            p.addPropertyChangeListener(this);
        }
        this.actualLibraries = libraries;
    }

    private static class LibrariesComparator implements Comparator<LibraryImplementation> {
        public int compare(LibraryImplementation lib1, LibraryImplementation lib2) {
            String name1 = LibrariesCustomizer.getLocalizedString(lib1.getLocalizingBundle(), lib1.getName());
            String name2 = LibrariesCustomizer.getLocalizedString(lib2.getLocalizingBundle(), lib2.getName());
            return name1.compareToIgnoreCase(name2);
        }        
    }

}
