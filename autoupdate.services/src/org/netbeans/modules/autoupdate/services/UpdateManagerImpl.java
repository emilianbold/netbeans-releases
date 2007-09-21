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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class UpdateManagerImpl extends Object {
    private static final UpdateManagerImpl INSTANCE = new UpdateManagerImpl();
    private static final UpdateManager.TYPE [] DEFAULT_TYPES = new UpdateManager.TYPE [] {  UpdateManager.TYPE.KIT_MODULE };
    
    private Reference<Cache> cacheReference = null;            
    private Logger logger = null;
    
    // package-private for tests only
    
    public static UpdateManagerImpl getInstance() {
        return INSTANCE;
    }
    
    /** Creates a new instance of UpdateManagerImpl */
    private UpdateManagerImpl () {}

    public void clearCache () {
        synchronized(UpdateManagerImpl.Cache.class) {
            cacheReference = null;
        }
    }    
    
    public static List<UpdateUnit> getUpdateUnits (UpdateProvider provider, UpdateManager.TYPE... types) {
        return filterUnitsByAskedTypes (UpdateUnitFactory.getDefault().getUpdateUnits (provider).values (), type2checkedList (types));
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateManager.TYPE... types) {                
        final Cache c = getCache();        
        return new ArrayList(filterUnitsByAskedTypes (c.getUnits(), type2checkedList (types))) {
            Cache keepIt = c;
        };        
    }
        
    public Set<UpdateElement> getAvailableEagers () {
        final Cache c = getCache();        
        return new HashSet(c.getAvailableEagers()) {
            Cache keepIt = c;
        };        
    }
    

    public Set<UpdateElement> getInstalledEagers () {
        final Cache c = getCache();        
        return new HashSet(c.getInstalledEagers()) {
            Cache keepIt = c;
        };        
    }
            
    public UpdateUnit getUpdateUnit (String moduleCodeName) {
        if (moduleCodeName.indexOf('/') != -1) {
            int to = moduleCodeName.indexOf('/');
            moduleCodeName = moduleCodeName.substring(0, to);
        }        
        return getCache().getUpdateUnit(moduleCodeName);
    }
            
    private Logger getLogger () {
        if (logger == null) {
            logger = Logger.getLogger (UpdateManagerImpl.class.getName ());
        }
        return logger;
    }
    
    public List<UpdateUnit> getUpdateUnits() {
        final Cache c = getCache();
        return new ArrayList(c.getUnits()) {
            Cache keepIt = c; 
        };
    }
    
   private static List<UpdateUnit> filterUnitsByAskedTypes (Collection<UpdateUnit> units, List<UpdateManager.TYPE> types) {
        List<UpdateUnit> askedUnits = new ArrayList<UpdateUnit> ();

        //hotfix for #113193 - reevaluate and probably fix better
        List<UpdateManager.TYPE> tmpTypes =  new ArrayList<UpdateManager.TYPE>(types);
        if (tmpTypes.contains (UpdateManager.TYPE.MODULE) && !tmpTypes.contains (UpdateManager.TYPE.KIT_MODULE)) {
            tmpTypes.add (UpdateManager.TYPE.KIT_MODULE);
        }
        
        for (UpdateUnit unit : units) {
            UpdateUnitImpl impl = Trampoline.API.impl (unit);
            if (tmpTypes.contains (impl.getType ())) {
                askedUnits.add (unit);
            }
        }

        return askedUnits;
    } 
   
    private static List<UpdateManager.TYPE> type2checkedList (UpdateManager.TYPE... types) {
        List<UpdateManager.TYPE> l = Arrays.asList (types);
        if (types != null && types.length > 1) {
            if (l.contains (UpdateManager.TYPE.MODULE) && l.contains (UpdateManager.TYPE.KIT_MODULE)) {
                throw new IllegalArgumentException ("Cannot mix types MODULE and KIT_MODULE into once list.");
            }
        } else if (types == null || types.length == 0) {
            l = Arrays.asList (DEFAULT_TYPES);
        }
        return l;
    }

    private UpdateManagerImpl.Cache getCache() {
        Reference<UpdateManagerImpl.Cache> ref =  getCacheReference();
        UpdateManagerImpl.Cache retval = (ref != null) ? ref.get() : null;
        if (retval == null) {
            retval = new Cache();
            initCache(retval);
        }
        return retval;
    }        

    public Reference<UpdateManagerImpl.Cache> getCacheReference() {        
        synchronized(UpdateManagerImpl.Cache.class) {        
            return cacheReference;
        }
    }    
    
    private void initCache(UpdateManagerImpl.Cache c) {
        synchronized(UpdateManagerImpl.Cache.class) {        
            cacheReference = new WeakReference<UpdateManagerImpl.Cache>(c);
        }        
    }
    
    private class Cache {
        private Map<String, UpdateUnit> units;
        private Set<UpdateElement> availableEagers = null;
        private Set<UpdateElement> installedEagers = null;

        Cache() {
            units = UpdateUnitFactory.getDefault ().getUpdateUnits ();
        }        
        public Set<UpdateElement> getAvailableEagers() {
            if (availableEagers == null) {
                availableEagers = new HashSet<UpdateElement>();
                for (UpdateUnit unit : getUnits()) {
                    if (!unit.getAvailableUpdates().isEmpty()) {
                        UpdateElement el = unit.getAvailableUpdates().get(0);
                        if (Trampoline.API.impl(el).isEager()) {
                            availableEagers.add(el);
                        }
                    }
                }
            }
            return availableEagers;
        }
        public Set<UpdateElement> getInstalledEagers() {
            if (installedEagers == null) {
                installedEagers = new HashSet<UpdateElement>();
                for (UpdateUnit unit : getUnits()) {
                    UpdateElement el;
                    if ((el = unit.getInstalled()) != null) {
                        if (Trampoline.API.impl(el).isEager()) {
                            installedEagers.add(el);
                        }
                    }
                }
            }            
            return installedEagers;
        }                        
        public Collection<UpdateUnit> getUnits() {
            return units.values();
        }
        public UpdateUnit getUpdateUnit (String moduleCodeName) {
            return units.get(moduleCodeName);
        }        
    }    
}
