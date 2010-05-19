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
package org.netbeans.modules.dlight.indicators.graph;

import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.dlight.indicators.Aggregation;
import org.netbeans.modules.dlight.util.Range;

/**
 * Data structure for storing time series data.
 * Stores data aggregated in buckets. <code>i</code>-th bucket contains
 * aggregated data with timestamps between <code>i*bucketSize</code>
 * and <code>(i+1)*bucketSize</code>.
 *
 * @author Alexey Vladykin
 */
public final class TimeSeriesDataContainer {

    private final long bucketSize;
    private final Aggregation aggr;
    private final int arraySize;
    private final ArrayList<Bucket> list;
    private final boolean lastNonNull;
    private TimeSeriesPlot plot;

    /**
     * Creates new instance.
     *
     * @param bucketSize  size of aggregation bucket
     * @param aggr  aggregation used in each bucket
     * @param arraySize  size of float arrays stored in this container
     * @param lastNonNull  if <code>true</code> and current bucket is null,
     *      {@link #get(int)} will return data from previous non-null bucket;
     *      if if <code>false</code>, {@link #get(int)} will return zero data
     * @throws IllegalArgumentException  if any of the sizes is not positive
     * @throws NullPointerException  if aggr is null
     */
    public TimeSeriesDataContainer(long bucketSize, Aggregation aggr, int arraySize, boolean lastNonNull) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must be positive"); // NOI18N
        }
        if (aggr == null) {
            throw new NullPointerException("aggregation can't be null"); // NOI18N
        }
        if (arraySize <= 0) {
            throw new IllegalArgumentException("arraySize must be positive"); // NOI18N
        }

        this.bucketSize = bucketSize;
        this.aggr = aggr;
        this.arraySize = arraySize;
        this.list = new ArrayList<Bucket>();
        this.lastNonNull = lastNonNull;
    }

    /**
     * Adds new data to the container. A bucket is selected based on the timestamp,
     * and new data is added and aggregated in that bucket.
     *
     * @param timestamp  data timestamp
     * @param newData  the data itself
     * @throws IllegalArgumentException if timestamp is negative or
     *      data size does not match expected size
     */
    public synchronized void put(long timestamp, float[] newData) {
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp can't be negative"); // NOI18N
        }
        if (newData.length != arraySize) {
            throw new IllegalArgumentException("Wrong data size"); // NOI18N
        }
        int bucketId = (int) Math.ceil((double) timestamp / (double) bucketSize);
        grow(bucketId + 1);
        Bucket bucket = list.get(bucketId);
        if (bucket == null) {
            list.set(bucketId, bucket = new Bucket(aggr, newData));
        } else {
            bucket.put(newData);
        }
        if (plot != null) {
            int oldLimit = plot.getUpperLimit();
            int newLimit = plot.calculateUpperLimit(bucket.get());
            while (oldLimit < newLimit) {
                oldLimit *= 2;
            }
            plot.setUpperLimit(oldLimit);
        }
    }

    /**
     * Retrieves aggregated data from the bucket.
     *
     * @param bucketId  bucket id
     * @return data from bucket or <code>null</code> if it is empty
     * @throws IllegalArgumentException
     */
    public synchronized float[] get(int bucketId) {
        if (0 <= bucketId && bucketId < list.size()) {
            Bucket bucket = list.get(bucketId);
            if (bucket == null) {
                if (lastNonNull) {
                    for (int i = bucketId - 1; 0 <= i; --i) {
                        Bucket prevBucket = list.get(i);
                        if (prevBucket != null) {
                            return prevBucket.get();
                        }
                    }
                }
                return new float[arraySize];
            } else {
                return bucket.get();
            }
        } else {
            throw new IllegalArgumentException("No such bucket"); // NOI18N
        }
    }

    /**
     * Increase container size with empty (null) buckets.
     *
     * @param timestamp
     */
    public synchronized void grow(int size) {
        if (list.size() < size) {
            list.ensureCapacity(size);
            list.addAll(Collections.<Bucket>nCopies(size - list.size(), null));
            if (plot != null) {
                plot.getViewportModel().setLimits(new Range<Long>(0L, 1000000000L * size));
            }
        }
    }

    /**
     * Returns container size
     *
     * @return size of this container
     */
    public int size() {
        return list.size();
    }

    public void setTimeSeriesPlot(TimeSeriesPlot plot) {
        this.plot = plot;
    }

    private static class Bucket {

        private final Aggregation aggr;
        private float[] data;
        private int count;

        public Bucket(Aggregation aggr, float[] data) {
            this.aggr = aggr;
            this.data = data;
            this.count = 1;
        }

        public void put(float[] data) {
            switch (aggr) {
                case FIRST:
                    // do nothing
                    break;
                case LAST:
                    this.data = data;
                    break;
                case SUM:
                    for (int i = 0; i < data.length; ++i) {
                        this.data[i] += data[i];
                    }
                    break;
                case AVERAGE:
                    for (int i = 0; i < data.length; ++i) {
                        this.data[i] = (count * this.data[i] + data[i]) / ++count;
                    }
                    break;
            }
        }

        public float[] get() {
            return data;
        }
    }
}
