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
package org.netbeans.spi.project.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author mkleint
 * @since org.netbeans.modules.projectapi 1.12
 */
public final class LookupProviderSupport {
    
    /** Creates a new instance of LookupProviderSupport */
    private LookupProviderSupport() {
    }
    
    /**
     * Creates a project lookup instance that combines the content from multiple sources. 
     * A convenience factory method for implementors of Project
     * 
     * @param baseLookup initial, base content of the project lookup created by the project owner
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition.
     *        The content of the folder is assumed to be {@link org.netbeans.spi.project.LookupProvider} instances
     * @return a lookup to be used in project
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, folderPath);
    }
    
    /**
     * Factory method for creating {@link org.netbeans.spi.project.LookupMerger} instance that merges
     * {@link org.netbeans.api.project.Sources} instances in the project lookup. 
     * Allows to compose the {@link org.netbeans.api.project.Sources}
     * content from multiple sources.
     * @return instance to include in project lookup
     */
    public static LookupMerger createSourcesMerger() {
        return new SourcesMerger();
    }
    
    //TODO maybe have just one single instance for a given path?
    private static Lookup createLookup(String folderPath) {
        FileObject root = Repository.getDefault().getDefaultFileSystem().findResource(folderPath);
        if (root != null) {
            DataFolder folder = DataFolder.findFolder(root);
            return new FolderLookup(folder).getLookup();
        } else { // #87544
            return Lookup.EMPTY;
        }
    }
    
    static class DelegatingLookupImpl extends ProxyLookup implements LookupListener {
        private Lookup baseLookup;
        private Lookup.Result<LookupProvider> providerResult;
        private LookupListener providerListener;
        private List<LookupProvider> old = Collections.emptyList();
        private List<Lookup> currentLookups;
        
        private Lookup.Result<LookupMerger> mergers;
        private Reference<LookupListener> listenerRef;
        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private List<Lookup.Result<?>> results = new ArrayList<Lookup.Result<?>>();
        
        public DelegatingLookupImpl(Lookup base, String path) {
            this(base, createLookup(path));
        }
        
        public DelegatingLookupImpl(Lookup base, Lookup providerLookup) {
            super();
            assert base != null;
            baseLookup = base;
            providerResult = providerLookup.lookup(new Lookup.Template<LookupProvider>(LookupProvider.class));
            doDelegate(providerResult.allInstances());
            providerListener = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    doDelegate(providerResult.allInstances());
                }
            };
            providerResult.addLookupListener(providerListener);
        }
        
        
        public void resultChanged(LookupEvent ev) {
            doDelegate(providerResult.allInstances());
        }
        
        
        private synchronized void doDelegate(Collection<? extends LookupProvider> providers) {
            //unregister listeners from the old results:
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
            }
            
            List<Lookup> newLookups = new ArrayList<Lookup>();
            for (LookupProvider elem : providers) {
                if (old.contains(elem)) {
                    int index = old.indexOf(elem);
                    newLookups.add(currentLookups.get(index));
                } else {
                    Lookup newone = elem.createAdditionalLookup(baseLookup);
                    assert newone != null;
                    LookupMerger merg = newone.lookup(LookupMerger.class);
                    if (merg != null) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "LookupProvider " + elem.getClass().getName() + " provides LookupMerger for " +
                                merg.getMergeableClass().getName() + 
                                ". That can cause project behaviour changes not anticipated by the project type owner." +
                                "Please consider making the LookupMerger a contract of the project type." );
                    }
                    newLookups.add(newone);
                }
            }
            old = new ArrayList<LookupProvider>(providers);
            currentLookups = newLookups;
            newLookups.add(baseLookup);
            Lookup lkp = new ProxyLookup(newLookups.toArray(new Lookup[newLookups.size()]));
            
            //merge:
            List<Class<?>> filteredClasses = new ArrayList<Class<?>>();
            List<Object> mergedInstances = new ArrayList<Object>();
            LookupListener l = listenerRef != null ? listenerRef.get() : null;
            if (l != null) {
                mergers.removeLookupListener(l);
            }
            mergers = lkp.lookupResult(LookupMerger.class);
            l = WeakListeners.create(LookupListener.class, this, mergers);
            listenerRef = new WeakReference<LookupListener>(l);
            mergers.addLookupListener(l);
            for (LookupMerger lm : mergers.allInstances()) {
                Class<?> c = lm.getMergeableClass();
                if (filteredClasses.contains(c)) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                            "Two LookupMerger registered for class " + c +
                            ". Only first one will be used"); // NOI18N
                    continue;
                }
                filteredClasses.add(c);
                mergedInstances.add(lm.merge(lkp));
                
                Lookup.Result<?> result = lkp.lookupResult(c);
                
                result.addLookupListener(this);
                results.add(result);
            }
            lkp = Lookups.exclude(lkp, filteredClasses.toArray(new Class<?>[filteredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(fixed, lkp);
        }
    }
    
    
    private static class SourcesMerger implements LookupMerger<Sources> {
        private SourcesImpl merger;
        
        public Class<Sources> getMergeableClass() {
            return Sources.class;
        }

        public Sources merge(Lookup lookup) {
            if (merger == null) {
                merger = new SourcesImpl();
            } 
            merger.setLookup(lookup);
            return merger;
        }
    }
    
    private static class SourcesImpl implements Sources, ChangeListener, LookupListener {
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private Lookup.Result<Sources> delegates;
        private Collection<Sources> currentDelegates = new ArrayList<Sources>();
        
        public SourcesImpl() {
        }

        private void setLookup(Lookup lookup) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            if (delegates != null) {
                delegates.removeLookupListener(this);
            }
            Lookup.Result<Sources> srcs = lookup.lookupResult(Sources.class);
            for (Sources ns : srcs.allInstances()) {
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            srcs.addLookupListener(this);
            delegates = srcs;
            fireChange();
        }

        public SourceGroup[] getSourceGroups(String type) {
            assert delegates != null;
            Collection<SourceGroup> result = new ArrayList<SourceGroup>();
            for (Sources ns : delegates.allInstances()) {
                SourceGroup[] grps = ns.getSourceGroups(type);
                if (grps != null) {
                    result.addAll(Arrays.asList(grps));
                }
            }
            return result.toArray(new SourceGroup[result.size()]);
        }

        public synchronized void addChangeListener(ChangeListener listener) {
            listeners.add(listener);
        }

        public synchronized void removeChangeListener(ChangeListener listener) {
            listeners.remove(listener);
        }

        public void stateChanged(ChangeEvent e) {
            fireChange();
        }

        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            for (ChangeListener listener : list) {
                listener.stateChanged(new ChangeEvent(this));
            }
        }

        public void resultChanged(LookupEvent ev) {
            if (currentDelegates.size() > 0) {
                for (Sources old : currentDelegates) {
                    old.removeChangeListener(this);
                }
                currentDelegates.clear();
            }
            for (Sources ns : delegates.allInstances()) {
                ns.addChangeListener(this);
                currentDelegates.add(ns);
            }
            fireChange();
        }
    }
    
}
