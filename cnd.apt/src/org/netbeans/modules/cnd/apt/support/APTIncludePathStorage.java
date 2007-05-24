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

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTIncludePathStorage {
    private final Map<String, List<String>> allIncludes = new HashMap<String, List<String>>();
    private static String baseNewName = "#INCLUDES# "; // NOI18N
    
    public APTIncludePathStorage() {
    }

    public List<String> get(List<String> includeList) {
        synchronized (allIncludes) {
            // look for equal list in values
            for (Iterator<List<String>> it = allIncludes.values().iterator(); it.hasNext();) {
                List<String> list = it.next();
                if (list.equals(includeList)) {
                    return list;
                }
            }
            // not found => add new labled one
            return get(baseNewName+allIncludes.size(), includeList);
        }
    }

    public List<String> get(String configID, List<String> sysIncludes) {
        synchronized (allIncludes) {
            List<String> list = allIncludes.get(configID);
            if (list == null) {
                // create new one and put in map
                list = sysIncludes;            
                allIncludes.put(configID, list);
            }
            return sysIncludes;
        }
    }
    
    public void dispose() {
        synchronized (allIncludes) {
            allIncludes.clear();
        }
    }
    
}
