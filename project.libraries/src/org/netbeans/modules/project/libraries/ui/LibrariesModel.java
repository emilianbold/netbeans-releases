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


import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;


import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.modules.project.libraries.WriteableLibraryProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.ErrorManager;

/**
 *
 * @author  tom
 */
class LibrariesModel extends javax.swing.AbstractListModel implements PropertyChangeListener, LookupListener {

    private List actualLibraries;
    private List addedLibraries;
    private List removedLibraries;
    private List changedLibraries;
    private Collection currentStorages;
    private Map storageByLib;
    private Lookup.Result lresult;
    private WriteableLibraryProvider writeableProvider;

    /** Creates a new instance of LibrariesModel */
    public LibrariesModel () {
        this.addedLibraries = new ArrayList ();
        this.removedLibraries = new ArrayList ();
        this.changedLibraries = new ArrayList ();
        this.currentStorages = Collections.EMPTY_SET;
        this.storageByLib = new HashMap ();
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
        Comparator c = new LibrariesComparator ();
        for (; index < this.actualLibraries.size(); index++) {
            Object tmp = this.actualLibraries.get (index);
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

    public void modifyLibrary (LibraryImplementation impl) {
        if (!this.addedLibraries.contains (impl) && !this.changedLibraries.contains(impl)) {
            this.changedLibraries.add (impl);
        }
        int index = this.actualLibraries.indexOf (impl);
        this.fireContentsChanged(this,index,index);
    }

    public boolean isLibraryEditable (LibraryImplementation impl) {
        if (this.addedLibraries.contains(impl))
            return true;
        LibraryProvider provider = (LibraryProvider) this.storageByLib.get
                (((ProxyLibraryImplementation)impl).getOriginal());
        //Todo: Currently just one WriteableLibraryProvider
        //Todo: if changed, must be rewritten to handle it
        return (provider == this.writeableProvider);
    }

    public void apply () throws IOException {
        //Todo: Currently just one WriteableLibraryProvider
        //Todo: if changed, must be rewritten to handle it
        for (Iterator it = this.removedLibraries.iterator(); it.hasNext();) {
            LibraryImplementation impl = (LibraryImplementation)it.next();
            LibraryProvider storage = (LibraryProvider) this.storageByLib.get (impl);
            if (storage == this.writeableProvider) {
                this.writeableProvider.removeLibrary (impl);
            }
            else {
                ErrorManager.getDefault().log ("Can not find storage for library: "+impl.getName());    //NOI18N
            }
        }
        if (this.writeableProvider != null) {
            for (Iterator it = this.addedLibraries.iterator(); it.hasNext();) {
                this.writeableProvider.addLibrary((LibraryImplementation)it.next());
            }
        }
        else {
            ErrorManager.getDefault().log ("Can not add libraries, no WriteableProvider."); //NOI18N
        }
        for (Iterator it = this.changedLibraries.iterator(); it.hasNext();) {
            ProxyLibraryImplementation proxy = (ProxyLibraryImplementation) it.next ();
            LibraryProvider storage = (LibraryProvider) this.storageByLib.get (proxy.getOriginal());
            if (storage == this.writeableProvider) {
                this.writeableProvider.removeLibrary (proxy.getOriginal());
                this.writeableProvider.addLibrary (proxy);
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
        for (Iterator it = this.currentStorages.iterator(); it.hasNext();) {
            ((LibraryProvider)it.next()).removePropertyChangeListener (this);
        }
        this.currentStorages = Collections.EMPTY_SET;
    }

    private synchronized void getLibraries () {
        List libraries = new ArrayList();
        if (this.lresult == null) {
            //First time
            this.lresult = Lookup.getDefault().lookup (new Lookup.Template(LibraryProvider.class));
            this.lresult.addLookupListener (this);
        }
        Collection instances = this.lresult.allInstances();
        Collection toAdd = new HashSet (instances);
        toAdd.removeAll(this.currentStorages);
        Collection toRemove = new HashSet (this.currentStorages);
        toRemove.removeAll (instances);
        this.currentStorages = instances;
        this.storageByLib.clear();
        for (Iterator it = instances.iterator(); it.hasNext();) {
            LibraryProvider storage = (LibraryProvider) it.next ();
            //TODO: in case of more WriteableLibraryProvider must be changed
            if (this.writeableProvider == null && storage instanceof WriteableLibraryProvider) {
                this.writeableProvider = (WriteableLibraryProvider) storage;
            }
            LibraryImplementation[] impls = storage.getLibraries();
            for (int i = 0; i < impls.length; i++) {
                LibraryImplementation lib = impls[i];
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

        for (Iterator it = toRemove.iterator(); it.hasNext();) {
            ((LibraryProvider)it.next()).removePropertyChangeListener (this);
        }
        for (Iterator it = toAdd.iterator(); it.hasNext();) {
            ((LibraryProvider)it.next()).addPropertyChangeListener (this);
        }
        this.actualLibraries = libraries;
    }




    private static class LibrariesComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            assert (o1 instanceof LibraryImplementation) && (o2 instanceof LibraryImplementation);
            LibraryImplementation lib1 = (LibraryImplementation) o1;
            LibraryImplementation lib2 = (LibraryImplementation) o2;
            String name1 = LibrariesCustomizer.getLocalizedString(lib1.getLocalizingBundle(), lib1.getName());
            String name2 = LibrariesCustomizer.getLocalizedString(lib2.getLocalizingBundle(), lib2.getName());
            return name1.compareToIgnoreCase(name2);
        }        
    }

}
