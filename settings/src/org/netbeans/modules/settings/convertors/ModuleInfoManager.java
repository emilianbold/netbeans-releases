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

package org.netbeans.modules.settings.convertors;

import java.beans.*;
import java.util.*;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

/** Manager providing ModuleInfo of all modules presented in the system.
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
                Lookup lookup = org.netbeans.core.NbTopManager.getModuleLookup();
                modulesResult = lookup.
                    lookup(new Lookup.Template(ModuleInfo.class));
                modulesResult.addLookupListener(new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        Collection l = getModulesResult().allInstances();
                        List reloaded;
                        synchronized (this) {
                            fillModules(l);
                            reloaded = replaceReloadedModules();
                        }
                        notifyReloads(reloaded);
                    }
                });
            }
            return modulesResult;
        }
    }

    /** notify registered listeners about reloaded modules
     * @param l a list of PCLs of reloaded modules
     */
    private void notifyReloads(List l) {
        Iterator it = l.iterator();
        while (it.hasNext()) {
            PCL lsnr = (PCL) it.next();
            lsnr.notifyReload();
        }
    }

    /** recompute accessible modules.
     * @param l a collection of module infos
     */
    private void fillModules(Collection l) {
        HashMap m = new HashMap((l.size() << 2) / 3 + 1);
        Iterator it = l.iterator();
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            m.put(mi.getCodeNameBase(), mi);
        }
        modules = m;
    }
    
    /** replace old MIs of reloaded modules with new ones
     * @return the list of PCLs of reloaded modules
     */
    private List replaceReloadedModules() {
        if (mapOfListeners == null) return Collections.EMPTY_LIST;
        
        Iterator it = new ArrayList(mapOfListeners.keySet()).iterator();
        List reloaded = new ArrayList();
        
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            ModuleInfo miNew = (ModuleInfo) modules.get(mi.getCodeNameBase());
            if (mi != miNew && miNew != null) {
                PCL lsnr = (PCL) mapOfListeners.remove(mi);
                lsnr.setModuleInfo(miNew);
                reloaded.add(lsnr);
                mapOfListeners.put(miNew, lsnr);
            }
        }
        
        return reloaded;
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
     * @param sdc convertor
     * @param mi ModuleInfo for which the listener will be registered
     */
    public synchronized void registerPropertyChangeListener(SerialDataConvertor sdc, ModuleInfo mi) {
        if (mapOfListeners == null) {
            mapOfListeners = new HashMap(modules.size());
        }
        
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        if (lsnr == null) {
            lsnr = new PCL(mi);
            mapOfListeners.put(mi, lsnr);
        }
        PropertyChangeListener pcl = org.openide.util.WeakListeners.propertyChange(sdc, lsnr);
        lsnr.addPropertyChangeListener(sdc, pcl);
    }
    
    /** unregister listener
     * @param sdc convertor
     * @param mi ModuleInfo
     * @see #registerPropertyChangeListener
     */
    public synchronized void unregisterPropertyChangeListener(SerialDataConvertor sdc, ModuleInfo mi) {
        if (mapOfListeners == null) return;
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        if (lsnr != null) {
            lsnr.removePropertyChangeListener(sdc);
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
    
    /** find out if a module was reloaded (disable+enabled)
     * @param codeBaseName ModuleInfo's code base name of the queried module
     * @return reload status
     */
    public synchronized boolean isReloaded(String codeBaseName) {
        if (mapOfListeners == null) return false;
        return isReloaded(getModule(codeBaseName));
    }
    
    /** ModuleInfo status provider shared by registered listeners
     * @see #registerPropertyChangeListener
     */
    private static final class PCL implements PropertyChangeListener {
        /** a flag to be set to true when a module has been disabled */
        private boolean aModuleHasBeenChanged = false;
        private boolean wasModuleEnabled;
        private ModuleInfo mi;
        private PropertyChangeSupport changeSupport;
        /** map of registered listeners <SerialDataConvertor, PropertyChangeListener> */
        private Map origs;
        
        public PCL(ModuleInfo mi) {
            this.mi = mi;
            wasModuleEnabled = mi.isEnabled();
            mi.addPropertyChangeListener(this);
        }
        
        /** replace an old module info with a new one */
        void setModuleInfo(ModuleInfo mi) {
            this.mi.removePropertyChangeListener(this);
            aModuleHasBeenChanged = true;
            this.mi = mi;
            mi.addPropertyChangeListener(this);
        }
        
        /** notify listeners about a module reload */
        void notifyReload() {
            firePropertyChange();
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

        /** adds listener per convertor */
        public void addPropertyChangeListener(SerialDataConvertor sdc, PropertyChangeListener listener) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                    origs = new WeakHashMap();
                }
                
                PropertyChangeListener old = (PropertyChangeListener) origs.get(sdc);
                if (old != null) return;
                origs.put(sdc, listener);
            }
            changeSupport.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (changeSupport != null) {
                changeSupport.removePropertyChangeListener(listener);
            }
        }
        
        /** unregister listener registered per convertor */
        public void removePropertyChangeListener(SerialDataConvertor sdc) {
            synchronized (this) {
                if (origs == null) return;
                
                PropertyChangeListener pcl = (PropertyChangeListener) origs.remove(sdc);
                if (pcl != null) {
                    removePropertyChangeListener(pcl);
                }
            }
        }
        
        private void firePropertyChange() {
            if (changeSupport != null) {
                changeSupport.firePropertyChange(ModuleInfo.PROP_ENABLED, null, null);
            }
        }
    }
    
}
