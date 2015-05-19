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
package org.netbeans.modules.cnd.api.codemodel.visit;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMReferenceQueryImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMReferenceImplementation;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Kvashin
 */
public class CMReferenceQuery {

    public interface ReferenceCallback {

        /**
         * Called periodically to check whether indexing should be aborted.
         * @return Should return false to continue, and true to abort.
         */
        boolean isCancelled();

        /**
         * Is called before visiting references in the given index.
         * Visiting reference is ordered by index references belong to.
         * This method is called before visiting references in the given index.
         *
         * @param index index we are going to visit
         */
        void onIndex(CMIndex index);

        /**
         * Is called for each reference
         * @param reference
         */
        void onReference(CMReference reference);
    }


    /**
     * Flags that can be passed to query to modify its behavior.
     * Flags can be bitwise-OR'd together to provide multiple options
     */
    public static final class QueryFlags {

        /** Determines whether to find usages */
        public static final QueryFlags Usages = bitFlag(0x01);


        /** Determines whether to find method overrides */
        public static final QueryFlags Overrides = bitFlag(0x02);

        /** Determines whether to find method direct descendants */
        public static final QueryFlags DirectDescendants = bitFlag(0x04);

        /** Determines whether to find method direct descendants */
        public static final QueryFlags AllDescendants = bitFlag(0x08);

        //<editor-fold defaultstate="collapsed" desc="hidden impl">
        private static QueryFlags bitFlag(int oneBitValue) {
            assert oneBitValue == 0 || ((oneBitValue & (oneBitValue - 1)) == 0) : "must have only one bit set " + Integer.toBinaryString(oneBitValue);
            return new QueryFlags(oneBitValue);
        }
        private final byte value;

        private QueryFlags(int mask) {
            assert Byte.MIN_VALUE <= mask && mask <= Byte.MAX_VALUE : "mask " + mask;
            this.value = (byte) mask;
        }

        public static QueryFlags valueOf(QueryFlags... flags) {
            assert flags != null;
            if (flags.length == 1) {
                return flags[0];
            }
            byte bitOrValue = 0;
            for (QueryFlags f : flags) {
                bitOrValue |= f.value;
            }
            return new QueryFlags(bitOrValue);
        }

        public byte getValue() {
            return value;
        }
        //</editor-fold>
    }

    public static CMReference findReference(URI uri, int offset) {        
        if (uri == null) {
            CndUtils.assertTrue(false, "null URI"); //NOOI18N
            return null;
        }
        CMReferenceImplementation ref ;
        for (CMReferenceQueryImplementation q : queries.allInstances()) {
            ref = q.findReference(uri, offset);
            if (ref != null) {
               return CMReference.fromImpl(ref);
            }
        }
        return null;
    }

    public static CMReference findReference(URI uri, int line, int col) {
        CMReferenceImplementation ref ;
        for (CMReferenceQueryImplementation q : queries.allInstances()) {
            ref = q.findReference(uri, line, col);
            if (ref != null) {
               return CMReference.fromImpl(ref);
            }
        }
        return null;
    }

    /**
     * Visits all references to the specified entities.
     *
     * @param referencedEntities USRs of entities to visit
     * @param flags
     * @param callback
     */
    public static CMVisitResult visitReferences(
            Collection<CMUnifiedSymbolResolution> referencedEntities,
            Collection<CMIndex> indices,
            QueryFlags flags,
            final ReferenceCallback callback) {

        Collection<CMIndexImplementation> indexImpls = new ArrayList<>(indices.size());
        for (CMIndex idx : indices) {
            indexImpls.add(APIAccessor.get().getIndexImpl(idx));
        }

        CMReferenceQueryImplementation.ReferenceCallbackImplementation callbackImpl =
                new CMReferenceQueryImplementation.ReferenceCallbackImplementation() {

            @Override
            public boolean isCancelled() {
                return callback.isCancelled();
            }

            @Override
            public void onIndex(CMIndexImplementation index) {
                callback.onIndex(APIAccessor.get().createIndex(index));
            }

            @Override
            public void onReference(CMReferenceImplementation reference) {
                callback.onReference(CMReference.fromImpl(reference));
            }
        };

        for (CMReferenceQueryImplementation q : queries.allInstances()) {
            CMVisitResult res = q.visitReferences(referencedEntities, indexImpls, flags, callbackImpl);
            if (res != CMVisitResult.Success) {
                return res;
            }
        }
        return CMVisitResult.Success;
    }

    //<editor-fold defaultstate="collapsed" desc="hidden impl">

    private static final Lookup.Result<CMReferenceQueryImplementation> queries;

    private CMReferenceQuery() {}

    static {
        queries = Lookups.forPath(CMReferenceQueryImplementation.PATH).lookupResult(CMReferenceQueryImplementation.class);
    }

    //</editor-fold>
}
