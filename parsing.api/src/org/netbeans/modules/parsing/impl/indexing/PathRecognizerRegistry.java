/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Tomas Zezula
 */
public class PathRecognizerRegistry implements LookupListener {

    private static PathRecognizerRegistry instance;

    private final Lookup.Result<? extends PathRecognizer> pathRecognizers;
    private String[] mimeTypes;
    private Map<String,Set<String>> idMap;

    private PathRecognizerRegistry() {
        pathRecognizers = Lookup.getDefault().lookupResult(PathRecognizer.class);
        pathRecognizers.addLookupListener(this);
        assert pathRecognizers != null;
    }


    public void collectIds (final Set<String> sourceIds, final Set<String> binaryIds) {
        Map<String,Set<String>> idMap = getIdMap();
        for (Map.Entry<String,Set<String>> e : idMap.entrySet()) {
            sourceIds.add(e.getKey());
            binaryIds.addAll(e.getValue());
        }
    }
    //where
    private synchronized Map<String,Set<String>> getIdMap () {
        if (idMap == null) {
            final Map<String,Set<String>> map = new HashMap<String, Set<String>>();
            for (PathRecognizer f : pathRecognizers.allInstances()) {
                Set<String> sids = f.getSourcePathIds();
                Set<String> bids = f.getBinaryPathIds();
                for (String sid : sids) {
                    map.put(sid, new HashSet<String>(bids));    //Defensive copy
                }
            }
            idMap = map;
        }
        return idMap;
    }
    

    public synchronized String[] getMimeTypes() {
        if (mimeTypes == null) {
            final Set<String> data = new HashSet<String>();
            for (PathRecognizer f : pathRecognizers.allInstances()) {
                data.addAll(f.getMimeType());
            }
            mimeTypes = data.toArray(new String[data.size()]);
        }
        return mimeTypes;
    }

    public Set<String> getBinaryIdsForSourceId (final String sourceId) {
        assert sourceId != null;
        Map<String,Set<String>> idMap = getIdMap();
        return idMap.get(sourceId);
    }

    public static synchronized PathRecognizerRegistry getDefault () {
        if (instance == null) {
            instance = new PathRecognizerRegistry();
        }
        return instance;
    }

    public void resultChanged(LookupEvent ev) {
        synchronized (this) {
            mimeTypes = null;
            idMap = null;
        }
    }

}
