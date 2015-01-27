/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.storage;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIStorageAccessor;
import org.netbeans.modules.cnd.spi.codemodel.storage.CMStorageFactory;
import org.netbeans.modules.cnd.spi.codemodel.storage.CMStorageImplementation;
import org.openide.util.Lookup;

/**
 *
 * @author mtishkov
 */
public final class CMStorageManager {

    private static final Object lock = "StoragesLock";
    private static final ConcurrentHashMap<String, CMStorage> storages = new ConcurrentHashMap<String, CMStorage>();

    public static CMStorage getInstance(String storageName, String scheme) {
        synchronized (lock) {
            CMStorage storage = storages.get(storageName);
            if (storage != null) {
                return storage;
            }
            Collection<? extends CMStorageFactory> factories = Lookup.getDefault().lookupAll(CMStorageFactory.class);
            for (CMStorageFactory cmStorageFactory : factories) {
                if (cmStorageFactory.supports(scheme)) {
                    CMStorageImplementation storageImpl = cmStorageFactory.create(storageName);
                    storage = APIStorageAccessor.get().createStorage(storageImpl);
                    CMStorage prev = storages.putIfAbsent(storageName, storage);
                    if (prev != null) {
                        storage = prev;
                    }
                    storages.putIfAbsent(storageName, storage);
                    return storage;
                    
                }
            }
            return null;

        }
    }

    public static void testShutdown(CMStorage db) {
        synchronized (lock) {
            for (Map.Entry<String, CMStorage> entry : storages.entrySet()) {
                if (entry.getValue() == db) {
                    storages.remove(entry.getKey());
                    break;
                }
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="hidden impl">
    static {
        APIStorageAccessor.register(new APIAccessorImpl());
    }
    
    private static final class APIAccessorImpl extends APIStorageAccessor {

      @Override
      public CMStorage createStorage(CMStorageImplementation impl) {
        return new CMStorage(impl);
      }
      
    }
    //</editor-fold>
}
