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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Interface for core/startup and core/settings
 * to provide lookup over system filesystem.
 *
 * @author Jaroslav Tulach
 */
public abstract class NamedServicesProvider {

    private static final Map<String,Reference<Lookup>> map = Collections.synchronizedMap(new HashMap<String,Reference<Lookup>>());
    
    public abstract Lookup create(String path);
    
    public static Lookup find(String path) {
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        Reference<Lookup> ref = map.get(path);
        Lookup lkp = ref == null ? null : ref.get();
        if (lkp != null) {
            return lkp;
        }
        NamedServicesProvider prov = Lookup.getDefault().lookup(NamedServicesProvider.class);
        if (prov != null && /* avoid stack overflow during initialization */ !path.startsWith(URLStreamHandlerRegistrationProcessor.REGISTRATION_PREFIX)) {
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
