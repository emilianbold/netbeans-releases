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
package org.netbeans.modules.dlight.threadmap.storage;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.core.stack.api.support.ThreadStateMapper;


public final class ThreadStateImpl implements ThreadState {

    private final byte[] stateIDs;
    private final byte[] statePercentage;
    private final int size;
    private final long timestamp;
    private final long samplePeriod;

    static MSAState[] collectedStates = new MSAState[]{
        null,
        null,
        null,
        MSAState.RunningUser,
        MSAState.RunningSystemCall,
        MSAState.RunningOther,
        MSAState.SleepingUserTextPageFault,
        MSAState.SleepingUserDataPageFault,
        MSAState.SleepingKernelPageFault,
        MSAState.WaitingCPU,
        MSAState.ThreadStopped,
        MSAState.SleepingUserLock,
        MSAState.SleepingOther,
        null,
        null,
        null,
        null,
        null
    };

    public ThreadStateImpl(long timestamp, long samplePeriod, int[] stat) {
        int count = 0;
        this.samplePeriod = samplePeriod;
        
        byte[] states = new byte[stat.length];

        for (int i = 3; i < stat.length; i++) {
            if (stat[i] > 0) {
                states[count++] = (byte) i;
            }
        }

        size = count;
        stateIDs = new byte[size];
        statePercentage = new byte[size];
        int factor = stat[0] * 10;
        for (int i = 0; i < size; i++) {
            stateIDs[i] = states[i];
            statePercentage[i] = (byte) ((stat[stateIDs[i]]+factor/2) / factor);
        }

        this.timestamp = timestamp;
    }

    public int size() {
        return size;
    }

    public MSAState getMSAState(final int index, final boolean full) {
        if (index >= stateIDs.length) {
            return MSAState.ThreadFinished;
        }

        final int stateIdx = stateIDs[index];

        assert stateIdx > 2;

        final MSAState fullState = collectedStates[stateIdx];
        return (full) ? fullState : ThreadStateMapper.toSimpleState(fullState);
    }

    public byte getState(int index) {
        return statePercentage[index];
    }

    public long getTimeStamp(int index) {
        return -1;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("MSA " + timestamp); // NOI18N
        buf.append(" has " + size); // NOI18N
        buf.append(" states\n\tMSA:"); // NOI18N
        for(int i = 0; i < this.size(); i++) {
            buf.append(" "); // NOI18N
            buf.append(getMSAState(i, true));
            buf.append("="+getState(i)); // NOI18N
        }
        return buf.toString();
    }

    public int getSamplingStateIndex(boolean full) {
        EnumMap<MSAState, AtomicInteger> map = new EnumMap<MSAState, AtomicInteger>(MSAState.class);
        for (int i = 0; i < size; i++) {
            MSAState msa = getMSAState(i, full);
            if (msa != null) {
                AtomicInteger value = map.get(msa);
                if (value == null) {
                    value = new AtomicInteger(getState(i));
                    map.put(msa, value);
                } else {
                    value.addAndGet(getState(i));
                }
            }
        }
        MSAState res = null;
        int max = 0;
        for (Map.Entry<MSAState, AtomicInteger> entry : map.entrySet()) {
            if (entry.getValue().get() > max) {
                max = entry.getValue().get();
                res = entry.getKey();
            }
        }
        if (res != null) {
            for (int i = 0; i < size; i++) {
                MSAState msa = getMSAState(i, full);
                if (res.equals(msa)) {
                    return i;
                }
            }
        }

        return 0;
    }

    public long getMSASamplePeriod() {
        return samplePeriod;
    }
}
