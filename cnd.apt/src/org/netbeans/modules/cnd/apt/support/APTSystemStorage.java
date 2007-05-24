/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.apt.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.impl.support.APTSystemMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTSystemStorage {
    private final Map<String, APTMacroMap> allMacroMaps = new HashMap<String, APTMacroMap>();
    private final APTIncludePathStorage includesStorage = new APTIncludePathStorage();
    private final static String baseNewName = "#SYSTEM MACRO MAP# "; // NOI18N
    private final static APTSystemStorage instance = new APTSystemStorage();
    
    private APTSystemStorage() {
    }
    
    public static APTSystemStorage getDefault() {
        return instance;
    }
    
    // it's preferable to use getMacroMap(String configID, List sysMacros)
    public APTMacroMap getMacroMap(List<String> sysMacros) {
        synchronized (allMacroMaps) {
            // look for the equal map in values
            APTMacroMap map = new APTSystemMacroMap();
            APTMacroUtils.fillMacroMap(map, sysMacros);            
            for (Iterator<APTMacroMap> it = allMacroMaps.values().iterator(); it.hasNext();) {
                APTMacroMap curMap = it.next();
                if (map.equals(curMap)) {
                    return curMap;
                }
            }
            allMacroMaps.put(baseNewName+allMacroMaps.size(), map);        
            return map;
        }
    }
    
    public APTMacroMap getMacroMap(String configID, List<String> sysMacros) {
        synchronized (allMacroMaps) {
            APTMacroMap map = allMacroMaps.get(configID);
            if (map == null) {
                // create new one and put in map
                map = new APTSystemMacroMap();
                APTMacroUtils.fillMacroMap(map, sysMacros);
                allMacroMaps.put(configID, map);
                APTUtils.LOG.log(Level.FINE, 
                        "new system macro map was added\n {0}", // NOI18N
                        new Object[] { map });
            }
            return map;
        }
    }
    
    // it's preferable to use getIncludes(String configID, List sysIncludes)
    public List<String> getIncludes(List<String> sysIncludes) {
        return includesStorage.get(sysIncludes);
    }    
    
    public List<String> getIncludes(String configID, List<String> sysIncludes) {
        return includesStorage.get(configID, sysIncludes);
    }   
    
    public void dispose() {
        synchronized (allMacroMaps) {
            allMacroMaps.clear();
        }
        includesStorage.dispose();
    }
}
