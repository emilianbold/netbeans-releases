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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState;

public class ThreadStateImpl implements ThreadState {

    private final byte[] stateIDs;
    private final byte[] statePercentage;
    private final int size;
    private final long timestamp;
    static FullThreadState[] collectedStates = new FullThreadState[]{
        FullThreadState.RunningUser,
        FullThreadState.RunningUser,
        FullThreadState.RunningUser,
        FullThreadState.RunningUser,
        FullThreadState.RunningSystemCall,
        FullThreadState.RunningOther,
        FullThreadState.SleepingUserTextPageFault,
        FullThreadState.SleepingUserDataPageFault,
        FullThreadState.SleepingKernelPageFault,
        FullThreadState.WaitingCPU,
        FullThreadState.Stopped,
        FullThreadState.SleepingUserSynchronization,
        FullThreadState.SleepingOther,
        FullThreadState.SOBJ_Mutex,
        FullThreadState.SOBJ_RWLock,
        FullThreadState.SOBJ_CV,
        FullThreadState.SOBJ_Sema,
        FullThreadState.SOBJ_User,
    };

    public ThreadStateImpl(long timestamp, int[] stat) {
        int count = 0;
        byte[] states = new byte[stat.length];

        for (int i = 3; i < stat.length; i++) {
            if (stat[i] > 0) {
                states[count++] = (byte) i;
            }
        }

        size = count;
        stateIDs = new byte[size];
        statePercentage = new byte[size];

        for (int i = 0; i < size; i++) {
            stateIDs[i] = states[i];
            statePercentage[i] = (byte) (stat[stateIDs[i]] / stat[0] / 10);
        }

        this.timestamp = timestamp;
    }

    public int size() {
        return size;
    }

    public String getStateName(int index) {
        try {
        switch (collectedStates[stateIDs[index]]) {
            case RunningUser:
            case RunningSystemCall:
            case RunningOther:
                return ShortThreadState.Running.name();
            case SOBJ_Sema:
            case SOBJ_RWLock:
            case SOBJ_Mutex:
            case SOBJ_User:
            case SleepingSemafore:
            case SleepingSystemSynchronization:
            case SleepingUserSynchronization:
                return ShortThreadState.Blocked.name();
            case SleepingUserTextPageFault:
            case SleepingKernelPageFault:
            case SleepingOther:
                return ShortThreadState.Sleeping.name();
            case SleepingConditionalVariable:
            case SOBJ_CV:
            case WaitingCPU:
                return ShortThreadState.Waiting.name();
            case Stopped:
            default:
                System.out.println("!!!!!!!!!!!! " + collectedStates[stateIDs[index]].name());
                return ShortThreadState.NotExist.name();
        }
        } catch (Throwable th) {

        }
        return ShortThreadState.Sleeping.name();
    }

    public byte getState(int index) {
        return statePercentage[index];
    }

    public long getTimeStamp(int index) {
        return -1;
    }

    public long getTimeStamp() {
        return TimeUnit.NANOSECONDS.toMillis(timestamp);
    }

    @Override
    public String toString() {
        return Arrays.toString(statePercentage);
    }
}
