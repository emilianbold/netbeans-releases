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

    private final HashMap<Long, HashMap<Long, Metrics>> funcCache = new HashMap<Long, HashMap<Long, Metrics>>();
    private final HashMap<Long, HashMap<Long, Metrics>> nodeCache = new HashMap<Long, HashMap<Long, Metrics>>();

    public void updateFunctionMetrics(long funcID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        updateMetrics(funcCache, funcID, bucket, duration, addIncl, addExcl);
    }

    public void updateNodeMetrics(long nodeID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        updateMetrics(nodeCache, nodeID, bucket, duration, addIncl, addExcl);
    }

    /**
     * Returnes currently accumulated methics for the specified function and
     * removes these metrics from the cache.
     *
     * @param funcID ID of the function to get metrics for
     * @param bucket ID of the time bucket to get metrics for
     * @return metrics for the specified function/time bucket
     */
    public Metrics getAndResetFunctionMetrics(long funcID, long bucket) {
        return getAndResetMetrics(funcCache, funcID, bucket);
    }

    /**
     * Returnes currently accumulated methics for the specified node and removes
     * these metrics from the cache.
     *
     * @param nodeID ID of the node to get metrics for
     * @param bucket ID of the time bucket to get metrics for
     * @return metrics for the specified node/time bucket
     */
    public Metrics getAndResetNodeMetrics(long nodeID, long bucket) {
        return getAndResetMetrics(nodeCache, nodeID, bucket);
    }

    private synchronized void updateMetrics(HashMap<Long, HashMap<Long, Metrics>> cache, long objectID, long bucket, long duration, boolean addIncl, boolean addExcl) {
        HashMap<Long, Metrics> objMetrics = cache.get(objectID);

        if (objMetrics == null) {
            objMetrics = new HashMap<Long, Metrics>();
            cache.put(objectID, objMetrics);
        }

        Metrics m = objMetrics.get(bucket);

        if (m == null) {
            m = new Metrics();
            objMetrics.put(bucket, m);
        }

        if (addIncl) {
            m.incl += duration;
        }

        if (addExcl) {
            m.excl += duration;
        }
    }

    private synchronized Metrics getAndResetMetrics(HashMap<Long, HashMap<Long, Metrics>> cache, long objectID, long bucket) {
        HashMap<Long, Metrics> objMetrics = cache.get(objectID);

        if (objMetrics == null || objMetrics.isEmpty()) {
            return null;
        }

        return objMetrics.remove(bucket);
    }

    public static class Metrics {

        long incl;
        long excl;
    }
}
