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
    
}
