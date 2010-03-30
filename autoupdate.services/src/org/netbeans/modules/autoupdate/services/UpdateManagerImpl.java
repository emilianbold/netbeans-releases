/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

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
        return new ArrayList<UpdateUnit> (filterUnitsByAskedTypes (c.getUnits(), type2checkedList (types))) {
            Cache keepIt = c;
        };        
    }
        
    public Set<UpdateElement> getAvailableEagers () {
        final Cache c = getCache();        
        return new HashSet<UpdateElement> (c.getAvailableEagers()) {
            Cache keepIt = c;
        };        
    }
    

    public Set<UpdateElement> getInstalledEagers () {
        final Cache c = getCache();        
        return new HashSet<UpdateElement> (c.getInstalledEagers()) {
            Cache keepIt = c;
        };        
    }
    
    public Collection<ModuleInfo> getInstalledProviders (String token) {
        Collection<ModuleInfo> res = null;
        final Cache c = getCache ();
        Collection<ModuleInfo> providers = c.createMapToken2InstalledProviders ().get (token);
        if (providers == null || providers.isEmpty ()) {
            res = new HashSet<ModuleInfo> (0) {
                Cache keepIt = c;
            };
        } else {
            res = new HashSet<ModuleInfo> (providers) {
                Cache keepIt = c;
            };
        }
        return res;
    }
            
    public Collection<ModuleInfo> getAvailableProviders (String token) {
        Collection<ModuleInfo> res = null;
        final Cache c = getCache ();
        Collection<ModuleInfo> providers = c.createMapToken2AvailableProviders ().get (token);
        if (providers == null || providers.isEmpty ()) {
            res = new HashSet<ModuleInfo> (0) {
                Cache keepIt = c;
            };
        } else {
            res = new HashSet<ModuleInfo> (providers) {
                Cache keepIt = c;
            };
        }
        return res;
    }
            
    public UpdateUnit getUpdateUnit (String moduleCodeName) {
        if (moduleCodeName.indexOf('/') != -1) {
            int to = moduleCodeName.indexOf('/');
            moduleCodeName = moduleCodeName.substring(0, to);
        }        
        return getCache().getUpdateUnit(moduleCodeName);
    }
            
    public List<UpdateUnit> getUpdateUnits() {
        final Cache c = getCache();
        return new ArrayList<UpdateUnit> (c.getUnits()) {
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

    Reference<UpdateManagerImpl.Cache> getCacheReference() {        
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
        private Map<String, Collection<ModuleInfo>> token2installedProviders = null;
        private Map<String, Collection<ModuleInfo>> token2availableProviders = null;

        Cache() {
            units = UpdateUnitFactory.getDefault ().getUpdateUnits ();
        }        
        public synchronized Set<UpdateElement> getAvailableEagers() {
            if (availableEagers == null) {
                createMaps ();
            }
            assert availableEagers != null : "availableEagers initialized";
            return availableEagers;
        }
        public synchronized Set<UpdateElement> getInstalledEagers() {
            if (installedEagers == null) {
                createMaps ();
            }            
            assert installedEagers != null : "installedEagers initialized";
            return installedEagers;
        }                        
        public synchronized Map<String, Collection<ModuleInfo>> createMapToken2InstalledProviders () {
            if (token2installedProviders == null) {
                createMaps ();
            }            
            assert token2installedProviders != null : "token2installedProviders initialized";
            return token2installedProviders;
        }                        
        public synchronized Map<String, Collection<ModuleInfo>> createMapToken2AvailableProviders () {
            if (token2availableProviders == null) {
                createMaps ();
            }
            assert token2availableProviders != null : "token2availableProviders initialized";
            return token2availableProviders;
        }                        
        public Collection<UpdateUnit> getUnits() {
            return units.values();
        }
        public UpdateUnit getUpdateUnit (String moduleCodeName) {
            return units.get(moduleCodeName);
        }
        
        private void createMaps () {
            availableEagers = new HashSet<UpdateElement> (getUnits ().size ());
            installedEagers = new HashSet<UpdateElement> (getUnits ().size ());
            token2installedProviders = new HashMap<String, Collection<ModuleInfo>> (11);
            token2availableProviders = new HashMap<String, Collection<ModuleInfo>> (11);
            for (UpdateUnit unit : getUnits ()) {
                UpdateElement el;
                if ((el = unit.getInstalled ()) != null) {
                    if (Trampoline.API.impl (el).isEager ()) {
                        installedEagers.add (el);
                    }
                    for (ModuleInfo mi : Trampoline.API.impl (el).getModuleInfos ()) {
                        for (Dependency dep : mi.getDependencies ()) {
                            DependencyAggregator dec = DependencyAggregator.getAggregator (dep);
                            dec.addDependee (mi);
                        }
                        String[] provs = mi.getProvides ();
                        if (provs == null || provs.length == 0) {
                            continue;
                        }
                        for (String token : provs) {
                            if (token2installedProviders.get (token) == null) {
                                token2installedProviders.put (token, new HashSet<ModuleInfo> ());
                            }
                            token2installedProviders.get (token).add (mi);
                        }
                    }
                }
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    el = unit.getAvailableUpdates ().get (0);
                    if (Trampoline.API.impl (el).isEager ()) {
                        availableEagers.add (el);
                    }
                    for (ModuleInfo mi : Trampoline.API.impl (el).getModuleInfos ()) {
                        for (Dependency dep : mi.getDependencies ()) {
                            DependencyAggregator dec = DependencyAggregator.getAggregator (dep);
                            dec.addDependee (mi);
                        }
                        String[] provs = mi.getProvides ();
                        if (provs == null || provs.length == 0) {
                            continue;
                        }
                        for (String token : provs) {
                            if (token2availableProviders.get (token) == null) {
                                token2availableProviders.put (token, new HashSet<ModuleInfo> ());
                            }
                            token2availableProviders.get (token).add (mi);
                        }
                    }
                }
            }
        }
    }
}
    
