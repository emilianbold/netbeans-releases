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
 * Software is Sun Microsystems, Inc. 
 *
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Interface for core/startup and core/settings
 * to provide lookup over system filesystem.
 *
 * @author Jaroslav Tulach
 */
public abstract class NamedServicesProvider {
    private static volatile Map<String,Reference<Lookup>> map = new HashMap<String,Reference<Lookup>>();
    
    
    public abstract Lookup create(String path);
    
    public static synchronized Lookup find(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        Reference<Lookup> ref = map.get(path);
        Lookup lkp = ref == null ? null : ref.get();
        if (lkp != null) {
            return lkp;
        }
        NamedServicesProvider prov = Lookup.getDefault().lookup(NamedServicesProvider.class);
        if (prov != null) {
            lkp = prov.create(path);
        } else {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
                if (l == null) {
                    l = NamedServicesProvider.class.getClassLoader();
                }
            }
            lkp = Lookups.metaInfServices(l, "META-INF/namedservices/" + path);
        }
        
        map.put(path, new WeakReference<Lookup>(lkp));
        return lkp;
    }
    
}
