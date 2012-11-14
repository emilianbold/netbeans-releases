/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.util.Parameters;

/**
 *
 * @author vita
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class ClusteredIndexables {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Creates new ClusteredIndexables
     * @param indexables, requires a list with fast {@link List#get(int)} as it heavily calls it.
     */
    public ClusteredIndexables(List<Indexable> indexables) {
        Parameters.notNull("indexables", indexables); //NOI18N  
        this.indexables = indexables;        
        this.sorted = new BitSet(indexables.size());
    }

    public Iterable<Indexable> getIndexablesFor(String mimeType) {
            if (mimeType == null) {
                mimeType = ALL_MIME_TYPES;
            }

            if (mimeType.length() == 0) {
                return Collections.unmodifiableList(indexables);
            }
            
            BitSet cluster = mimeTypeClusters.get(mimeType);
            if (cluster == null) {                
                cluster = new BitSet();
                // pick the indexables with the given mime type and add them to the cluster
                for (int i = sorted.nextClearBit(0); i < indexables.size(); i = sorted.nextClearBit(i+1)) {
                    final Indexable indexable = indexables.get(i);
                    if (SPIAccessor.getInstance().isTypeOf(indexable, mimeType)) {
                        cluster.set(i);
                        sorted.set(i);
                    }
                }
                mimeTypeClusters.put(mimeType, cluster);
            }
            
            return new BitSetIterable(cluster);
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private final List<Indexable> indexables;
    private final BitSet sorted;

    private final Map<String, BitSet> mimeTypeClusters = new HashMap<String, BitSet>();
    private static final String ALL_MIME_TYPES = ""; //NOI18N


    private class BitSetIterator implements Iterator<Indexable> {

        private final BitSet bs;
        private int index;

        BitSetIterator(@NonNull final BitSet bs) {
            this.bs = bs;
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return bs.nextSetBit(index) >= 0;
        }

        @Override
        public Indexable next() {
            int tmp = bs.nextSetBit(index);
            if (tmp < 0) {
                throw new NoSuchElementException();
            }
            index = tmp + 1;
            return indexables.get(tmp);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable type"); //NOI18N
        }

    }

    private class BitSetIterable implements Iterable<Indexable> {

        private final BitSet bs;

        BitSetIterable(@NonNull final BitSet bs) {
            this.bs = bs;
        }

        @Override
        public Iterator<Indexable> iterator() {
            return new BitSetIterator(bs);
        }

    }
}
