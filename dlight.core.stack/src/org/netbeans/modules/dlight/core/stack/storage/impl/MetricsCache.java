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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.storage.impl;

import java.util.HashMap;

/**
 *
 * @author ak119685
 */
final class MetricsCache {

    private final HashMap<HashKey, Metrics> nodeCache = new HashMap<HashKey, Metrics>();
    private final HashMap<HashKey, Metrics> funcCache = new HashMap<HashKey, Metrics>();

    public void updateFunctionMetrics(long funcID, long contextID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        updateMetrics(funcCache, funcID, contextID, bucket, duration, addIncl, addExcl);
    }

    public void updateNodeMetrics(long nodeID, long contextID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        updateMetrics(nodeCache, nodeID, contextID, bucket, duration, addIncl, addExcl);
    }

    /**
     * Returnes currently accumulated methics for the specified function and
     * removes these metrics from the cache.
     *
     * @param funcID ID of the function to get metrics for
     * @param bucket ID of the time bucket to get metrics for
     * @return metrics for the specified function/time bucket
     */
    public Metrics getAndResetFunctionMetrics(long funcID, long contextID, long bucket) {
        return getAndResetMetrics(funcCache, funcID, contextID, bucket);
    }

    /**
     * Returnes currently accumulated methics for the specified node and removes
     * these metrics from the cache.
     *
     * @param nodeID ID of the node to get metrics for
     * @param bucket ID of the time bucket to get metrics for
     * @return metrics for the specified node/time bucket
     */
    public Metrics getAndResetNodeMetrics(long nodeID, long contextID, long bucket) {
        return getAndResetMetrics(nodeCache, nodeID, contextID, bucket);
    }

    private synchronized void updateMetrics(HashMap<HashKey, Metrics> cache, long objectID, long contextID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        HashKey key = new HashKey(objectID, contextID, bucket);
        Metrics metrics = cache.get(key);

        if (metrics == null) {
            metrics = new Metrics();
            cache.put(key, metrics);
        }

        if (addIncl) {
            metrics.incl += duration;
        }

        if (addExcl) {
            metrics.excl += duration;
        }
    }

    private synchronized Metrics getAndResetMetrics(HashMap<HashKey, Metrics> cache, long objectID, long contextID, long bucket) {
        return cache.remove(new HashKey(objectID, contextID, bucket));
    }

    private static final class HashKey {

        private final long objID;
        private final long contextID;
        private final long bucket;

        public HashKey(long objID, long contextID, long bucket) {
            this.objID = objID;
            this.contextID = contextID;
            this.bucket = bucket;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HashKey)) {
                return false;
            }

            HashKey that = (HashKey) obj;

            return this.objID == that.objID
                    && this.contextID == that.contextID
                    && this.bucket == that.bucket;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (int) (this.objID ^ (this.objID >>> 32));
            hash = 97 * hash + (int) (this.contextID ^ (this.contextID >>> 32));
            hash = 97 * hash + (int) (this.bucket ^ (this.bucket >>> 32));
            return hash;
        }
    }

    public static final class Metrics {

        long incl;
        long excl;
    }
}
