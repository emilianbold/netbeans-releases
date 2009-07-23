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
package org.netbeans.modules.dlight.api.storage.threadmap;

/**
 *
 * @author Alexander Simon
 */
public interface ThreadState {

    /**
     * Sum of all MSA in any time.
     */
    static final int POINTS = 100;

    /**
     * Aggregated thread states.
     */
    static enum MSAState {
        // Short states list ...

        START_SHORT_LIST,
        Running, // on cpu and runn
        Sleeping, // sleep()
        Waiting, // pthread_join, for example
        Blocked, // on some mutex
        Stopped, // lwp_suspend()
        END_SHORT_LIST,
        // Long states list ...
        START_LONG_LIST,
        RunningUser, // running in user mode
        RunningSystemCall, // running in sys call or page fault
        RunningOther, // running in other trap
        SleepingUserTextPageFault, // asleep in user text page fault
        SleepingUserDataPageFault, // asleep in user data page fault
        SleepingKernelPageFault, // asleep in kernel page fault
        SleepingUserLock, // asleep waiting for user-mode lock
        SleepingOther, // asleep for any other reason
        WaitingCPU, // waiting for CPU (latency)
        ThreadStopped, // stopped (/proc, jobcontrol, lwp_suspend)
        END_LONG_LIST,
        ThreadFinished, // Thread is gone
    }

    /**
     * @return size of state
     */
    int size();

    /**
     * returns MSAState.
     *
     * @param index of state.
     * @return state ID by index.
     */
    MSAState getMSAState(int index, boolean full);

    /**
     * @param index of state.
     * @return value of state by index. Unit of value is 1%. I.e. sum of all values is 100.
     */
    byte getState(int index);

    /**
     * returns -1 if there are no stack avaliable.
     *
     * @param index interested state.
     * @return time in natural unit of state. It is guaranteed that exist stack damp on this time.
     */
    long getTimeStamp(int index);

    /**
     *
     * @return beginning time in natural unit of state.
     */
    long getTimeStamp();

    /**
     * returns index of state that represents "sampling" one.
     *
     * @return state ID by index.
     */
    int getSamplingStateIndex(boolean full);
}
