/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.indexing.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.openide.modules.OnStop;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
public class CndTextIndexManager {
    public static final String FIELD_IDS = "ids"; //NOI18N
    public static final String FIELD_UNIT_ID = "unitId"; //NOI18N
    private static final Map<CacheLocation, CndTextIndexImpl> indexMap = new HashMap<CacheLocation, CndTextIndexImpl>();
    private static final Object lock = new Object();

    @ServiceProvider(service = RepositoryListener.class, path = RepositoryListener.PATH)
    public static class Listener implements RepositoryListener {

        @Override
        public boolean unitOpened(int unitId, CharSequence unitName) {
            return true;
        }

        @Override
        public boolean repositoryOpened(int repositoryId, CacheLocation cacheLocation) {
            return CndTextIndexImpl.validate(cacheLocation);
        }

        @Override
        public void unitClosed(int unitId, CharSequence unitName) {
        }

        @Override
        public void unitRemoved(int unitId, CharSequence unitName) {
            if (unitId < 0) {
                return;
            }
            CacheLocation loc = RepositoryAccessor.getTranslator().getCacheLocation(unitId);
            synchronized (lock) {
                CndTextIndexImpl index = CndTextIndexManager.get(loc);
                if (index != null) {
                    index.unitRemoved(unitId, unitName);
                }
            }
        }

        @Override
        public void anExceptionHappened(int unitId, CharSequence unitName, RepositoryException exc) {
        }
    }

    @OnStop
    public static class Cleanup implements Runnable {
        @Override
        public void run() {
            for (CndTextIndexImpl idx : indexMap.values()) {
                idx.cleanup();
            }
        }
    }

    public static CndTextIndexImpl get(final CacheLocation location) {
        synchronized (lock) {
            CndTextIndexImpl index = indexMap.get(location);
            if (index == null) {
                try {                    
                    index = CndTextIndexImpl.create(location);
                } catch (IOException ex) {
                    Logger.getLogger(CndTextIndexManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                indexMap.put(location, index);
            }
            return index;
        }
    }
}
