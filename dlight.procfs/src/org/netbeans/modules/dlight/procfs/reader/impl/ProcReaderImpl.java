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
package org.netbeans.modules.dlight.procfs.reader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.dlight.procfs.api.LWPUsage;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.dlight.procfs.api.PUsage;
import org.netbeans.modules.dlight.procfs.api.SamplingData;
import org.netbeans.modules.dlight.procfs.reader.api.ProcReader;

/**
 *
 * @author ak119685
 */
public abstract class ProcReaderImpl implements ProcReader,
        ProcessStatusProvider,
        ProcessUsageProvider,
        ThreadsInfoProvider {

    private final static int MAXFILELENGTH = 512;
    // LWPID => prev MSAInfo
    private final HashMap<Integer, MSAInfoImpl> prevMSAData = new HashMap<Integer, MSAInfoImpl>();
    // LWPID => prev Timestamp
    private final HashMap<Integer, Long> prevTSData = new HashMap<Integer, Long>();
    private final ReusableByteBuffer buffer = new ReusableByteBuffer(MAXFILELENGTH, 10);
    protected final ByteOrder byteOrder;
    protected final DataModel dataModel;

    public ProcReaderImpl(final ByteOrder byteOrder, final DataModel dataModel) {
        this.byteOrder = byteOrder;
        this.dataModel = dataModel;
    }

    public abstract PStatus getProcessStatus();

    public abstract PUsage getProcessUsage() throws IOException;

    public abstract List<LWPUsage> getThreadsInfo();

    protected PStatus getProcessStatus(final InputStream is) throws IOException {
        DataReader reader = null;

        try {
            reader = newReader(is);

            reader.seek(4);
            final int nlwp = reader._int();
            reader.seek(8);
            final int pid = reader._int();
            reader.seek(268);
            final int nzomb = reader._int();

            final PStatus.ThreadsInfo ti = new PStatus.ThreadsInfo(nlwp, nzomb) {
            };

            final PStatus.PIDInfo pi = new PStatus.PIDInfo(pid) {
            };

            return new PStatus() {

                public ThreadsInfo getThreadInfo() {
                    return ti;
                }

                public PIDInfo getPIDInfo() {
                    return pi;
                }
            };
        } finally {
            if (reader != null) {
                reader.releaseBuffer();
            }
        }
    }

    protected LWPUsage getProcessUsage(final InputStream is) throws IOException {
        DataReader reader = null;

        try {
            reader = newReader(is, LWPUsage.FILESIZE);

            reader.seek(0);
            final int lwpid = reader._int();
            final int count = reader._int();
            final long timestamp = reader._time();
            final long create = reader._time();
            final long term = reader._time();
            final long rtime = reader._time();

            final PUsage.UsageInfo ui = new PUsage.UsageInfo(lwpid, count, timestamp, create, term, rtime) {
            };

            final MSAInfoImpl prevMSAInfo = prevMSAData.containsKey(lwpid) ? prevMSAData.get(lwpid) : MSAInfoImpl.nullInfo;

            final long ts = prevTSData.containsKey(lwpid) ? prevTSData.get(lwpid) : create;
            final long sample = timestamp - ts;

            prevTSData.put(lwpid, timestamp);

            final SamplingData sd = new SamplingData(ts, sample);

            final long utime = reader._time();
            final long stime = reader._time();
            final long ttime = reader._time();
            final long tftime = reader._time();
            final long dftime = reader._time();
            final long kftime = reader._time();
            final long ltime = reader._time();
            final long slptime = reader._time();
            final long wtime = reader._time();
            final long stoptime = reader._time();

            final MSAInfoImpl mi = new MSAInfoImpl(prevMSAInfo, utime, stime, ttime, tftime, dftime, kftime, ltime, slptime, wtime, stoptime);

            prevMSAData.put(lwpid, mi);

            return new LWPUsage() {

                public SamplingData getSamplingData() {
                    return sd;
                }

                public UsageInfo getUsageInfo() {
                    return ui;
                }

                public MSAInfo getMSAInfo() {
                    return mi;
                }
            };
        } finally {
            if (reader != null) {
                reader.releaseBuffer();
            }
        }
    }

    protected List<LWPUsage> getThreadsInfo(final InputStream is) throws IOException {
        return null;
    }

    private DataReader newReader(InputStream is) throws IOException {
        return newReader(is, MAXFILELENGTH);
    }

    private DataReader newReader(InputStream is, int limit) throws IOException {
        limit = Math.min(limit, MAXFILELENGTH);
        
        int offset = 0;
        try {
            offset = buffer.getAndLockOffset();
            int read_total = 0;
            int read = 0;

            while (read >= 0 && read_total < limit) {
                read = is.read(buffer.buffer, offset + read_total, limit - read_total);
                read_total += read;
            }
        } finally {
            is.close();
        }

        return new DataReader(buffer, offset, byteOrder);
    }
}
