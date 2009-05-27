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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public final class PathRecognizerRegistry {

    public static synchronized PathRecognizerRegistry getDefault () {
        if (instance == null) {
            instance = new PathRecognizerRegistry();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getSourceIds () {
        final Object [] data = getData();
        return (Set<String>) data[0];
    }

    @SuppressWarnings("unchecked")
    public Set<String> getLibraryIds () {
        final Object [] data = getData();
        return (Set<String>) data[1];
    }

    @SuppressWarnings("unchecked")
    public Set<String> getBinaryLibraryIds () {
        final Object [] data = getData();
        return (Set<String>) data[2];
    }

    @SuppressWarnings("unchecked")
    public Set<String> getMimeTypes() {
        final Object [] data = getData();
        return (Set<String>) data[3];
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PathRecognizerRegistry.class.getName());
    
    private static PathRecognizerRegistry instance;

    private final Lookup.Result<? extends PathRecognizer> lookupResult;
    private final LookupListener tracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            LOG.fine("resultChanged: reseting cached PathRecognizers"); //NOI18N
            synchronized (PathRecognizerRegistry.this) {
                cachedData = null;
            }
        }
    };

    private Object [] cachedData;

    private PathRecognizerRegistry() {
        lookupResult = Lookup.getDefault().lookupResult(PathRecognizer.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, tracker, lookupResult));
    }

    private synchronized Object [] getData () {
        if (cachedData == null) {
            Set<String> sourceIds = new HashSet<String>();
            Set<String> libraryIds = new HashSet<String>();
            Set<String> binaryLibraryIds = new HashSet<String>();
            Set<String> mimeTypes = new HashSet<String>();

            Collection<? extends PathRecognizer> recognizers = lookupResult.allInstances();
            for(PathRecognizer r : recognizers) {
                Set<String> sids = r.getSourcePathIds();
                if (sids != null) {
                    sourceIds.addAll(sids);
                }

                Set<String> lids = r.getLibraryPathIds();
                if (lids != null) {
                    libraryIds.addAll(lids);
                }

                Set<String> blids = r.getBinaryLibraryPathIds();
                if (blids != null) {
                    binaryLibraryIds.addAll(blids);
                }

                Set<String> mts = r.getMimeTypes();
                if (mts != null) {
                    mimeTypes.addAll(mts);
                }

                LOG.log(Level.FINE, "PathRecognizer {0} supplied sids={1}, lids={2}, blids={3}, mts={4}", new Object [] { //NOI18N
                    r.toString(), sids, lids, blids, mts
                });
            }

            cachedData = new Object [] {
                Collections.unmodifiableSet(sourceIds),
                Collections.unmodifiableSet(libraryIds),
                Collections.unmodifiableSet(binaryLibraryIds),
                Collections.unmodifiableSet(mimeTypes),
            };
        }

        return cachedData;
    }

}
