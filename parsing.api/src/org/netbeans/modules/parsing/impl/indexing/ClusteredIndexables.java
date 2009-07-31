/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.util.Parameters;

/**
 *
 * @author vita
 */
public final class ClusteredIndexables {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public ClusteredIndexables(Collection<IndexableImpl> indexables) {
        Parameters.notNull("indexables", indexables);
        this.indexables = new LinkedList<IndexableImpl>(indexables);
    }

    public Iterable<Indexable> getIndexablesFor(String mimeType) {
        synchronized (mimeTypeClusters) {
            if (mimeType == null) {
                mimeType = ALL_MIME_TYPES; //NOI18N
            }
            
            List<Indexable> cluster = mimeTypeClusters.get(mimeType);
            if (cluster == null) {
                cluster = new LinkedList<Indexable>();
                
                if (mimeType.length() == 0) {
                    // add all the remaining indexables to the ALL_MIME_TYPES cluster
                    for(IndexableImpl iimpl : indexables) {
                        cluster.add(SPIAccessor.getInstance().create(iimpl));
                    }
                } else {
                    // pick the indexables with the given mime type and add them to the cluster
                    boolean resolved = false;
                    
                    for(ListIterator<IndexableImpl> it = indexables.listIterator(); it.hasNext(); ) {
                        IndexableImpl iimpl = it.next();
                        if (iimpl.isTypeOf(mimeType)) {
                            it.remove();
                            cluster.add(SPIAccessor.getInstance().create(iimpl));
                            resolved = true;
                        }
                    }

                    if (resolved) {
                        // if we picked some indexables remove the cached ALL_MIME_TYPES cluster,
                        // because its content is now different
                        mimeTypeClusters.remove(ALL_MIME_TYPES);
                    }
                }

                mimeTypeClusters.put(mimeType, cluster);
            }

            if (mimeType.length() == 0) {
                return new ProxyIterable<Indexable>(mimeTypeClusters.values());
            } else {
                return cluster;
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private final List<IndexableImpl> indexables;
    private final Map<String, List<Indexable>> mimeTypeClusters = new HashMap<String, List<Indexable>>();
    private static final String ALL_MIME_TYPES = ""; //NOI18N
}