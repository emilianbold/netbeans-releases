/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.beans.*;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import org.openide.util.Lookup;
import org.openide.modules.ModuleInfo;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

/** Manager providing ModuleInfo of all modules presented in the system.
 * !!! KEEP CODE SYNCHRONOUS WITH org.netbeans.core.projects.ModuleInfoManager !!!
 *
 * @author  Jan Pokorsky
 */
final class ModuleInfoManager {
    private final static ModuleInfoManager mim = new ModuleInfoManager();;
    
    /** all modules <code bas name, ModuleInfo> */
    private HashMap modules = null;
    /** lookup query to find out all modules */
    private Lookup.Result modulesResult = null;
    /** <ModuleInfo, PCL> */
    private HashMap mapOfListeners;
    /** Creates a new instance of ModuleInfoManager */
    private ModuleInfoManager() {
    }
    
    public final static ModuleInfoManager getDefault() {
        return mim;
    }
    
    /** find module info.
     * @param codeBaseName module code base name (without revision)
     * @return module info or null
     */
    public ModuleInfo getModule(String codeBaseName) {
        Collection l = null;
        if (modules == null) {
            l = getModulesResult().allInstances();
        }
        synchronized (this) {
            if (modules == null) fillModules(l);
            return (ModuleInfo) modules.get(codeBaseName);
        }
    }

    private Lookup.Result getModulesResult() {
        synchronized (this) {
            if (modulesResult == null) {
                modulesResult = Lookup.getDefault().
                    lookup(new Lookup.Template(ModuleInfo.class));
                modulesResult.addLookupListener(new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        Collection l = getModulesResult().allInstances();
                        synchronized (this) {
                            fillModules(l);
                        }
                    }
                });
            }
            return modulesResult;
        }
    }


    /** recompute accessible modules. */
    private void fillModules(Collection l) {
        HashMap m = new HashMap((l.size() << 2) / 3 + 1);
        Iterator it = l.iterator();
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            m.put(mi.getCodeNameBase(), mi);
        }
        modules = m;
    }

    /** look up ModuleInfo according to clazz
     * @param clazz class used in the look up query
     * @return module info of the module which clazz was loaded from
     */
    public ModuleInfo getModuleInfo(Class clazz) {
        Iterator it = getModulesResult().allInstances().iterator();
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            if (mi.owns(clazz)) return mi;
        }
        return null;
    }
    
    /** register listener to be notified about changes of mi
     * @param pcl listener
     * @param mi ModuleInfo for which the listener will be registered
     */
    public synchronized void registerPropertyChangeListener(PropertyChangeListener pcl, ModuleInfo mi) {
        if (mapOfListeners == null) {
            mapOfListeners = new HashMap(modules.size());
        }
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        if (lsnr == null) {
            lsnr = new PCL(mi);
            mapOfListeners.put(mi, lsnr);
        }
        lsnr.addPropertyChangeListener(pcl);
    }
    
    /** unregister listener
     * @param pcl listener
     * @param mi ModuleInfo
     * @see #registerPropertyChangeListener
     */
    public synchronized void unregisterPropertyChangeListener(PropertyChangeListener pcl, ModuleInfo mi) {
        if (mapOfListeners == null) return;
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        if (lsnr != null) {
            lsnr.removePropertyChangeListener(pcl);
            // do not try to discard lsnr to allow to track reloading of a module
        }
    }
    
    /** find out if a module was reloaded (disable+enabled)
     * @param mi ModuleInfo of the queried module
     * @return reload status
     */
    public synchronized boolean isReloaded(ModuleInfo mi) {
        if (mapOfListeners == null) return false;
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        return lsnr != null && lsnr.isReloaded();
    }
    
    /** ModuleInfo status provider shared by registered listeners
     * @see #registerPropertyChangeListener
     */
    private final class PCL implements PropertyChangeListener {
        /** a flag to be set to true when a module has been disabled */
        private boolean aModuleHasBeenChanged = false;
        private boolean wasModuleEnabled;
        private ModuleInfo mi;
        private PropertyChangeSupport changeSupport;
        
        public PCL(ModuleInfo mi) {
            this.mi = mi;
            wasModuleEnabled = mi.isEnabled();
            mi.addPropertyChangeListener(this);
        }
        
        boolean isReloaded() {
            return aModuleHasBeenChanged;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if(ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {

                boolean change;

                if (!Boolean.TRUE.equals (evt.getNewValue ())) {
                    // a module has been disabled, use full checks
                    aModuleHasBeenChanged = true;

                    // if wasModuleEnabled was true, we changed state
                    change = wasModuleEnabled;
                } else {
                    // a module was enabled, if wasModuleEnabled was false
                    // we changed state
                    change = !wasModuleEnabled;
                }

                // update wasModuleEnabled to current state of the module
                wasModuleEnabled = mi.isEnabled();

                if (change) {
                    //instanceCookieChanged(null);
                    firePropertyChange();
                }
            }
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            synchronized (this) {
                if (changeSupport == null)
                    changeSupport = new PropertyChangeSupport(this);
            }
            changeSupport.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (changeSupport != null)
                changeSupport.removePropertyChangeListener(listener);
        }
        
        private void firePropertyChange() {
            if (changeSupport != null)
                changeSupport.firePropertyChange(ModuleInfo.PROP_ENABLED, null, null);
        }
    }
    
}
