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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.filesystems.FileSystem;
import org.openide.modules.OnStop;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
public class CndTextIndexManager {
    public static final String FIELD_IDS = "ids"; //NOI18N
    private static final Map<String, CndTextIndexImpl> indexMap = new HashMap<String, CndTextIndexImpl>();
    private static final File indexLocation = new File(CacheLocation.DEFAULT.getLocation(), "text_index"); //NOI18N
    
    @OnStop
    public static class Cleanup implements Runnable {
        @Override
        public void run() {
            for (CndTextIndexImpl idx : indexMap.values()) {
                idx.store();
            }
        }
    }
    
    public static synchronized CndTextIndexImpl get(FileSystem fs) {
        String fsKey = "local"; //NOI18N
        if (!CndFileUtils.isLocalFileSystem(fs)) {
            fsKey = fs.getDisplayName();
        }
        CndTextIndexImpl index = indexMap.get(fsKey);
        if (index == null) {
            final File indexRoot = new File(indexLocation, fsKey); //NOI18N
            indexRoot.mkdirs();

            try {
                index = new CndTextIndexImpl(IndexManager.createDocumentIndex(indexRoot));
            } catch (IOException ex) {
                Logger.getLogger(CndIndexingFilterProviderImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            indexMap.put(fsKey, index);
        }
        return index;
    }
}
