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

package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadStateColumn;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState.MSAState;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadsDataManager.MergedThreadInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadStateColumnImpl implements ThreadStateColumn {
    /** Thread status is unknown. */
    public static final Color THREAD_STATUS_UNKNOWN_COLOR = Color.LIGHT_GRAY;
    /** Thread is waiting to die. Also used for "doesn't exist yet" and "dead" */
    public static final Color THREAD_STATUS_ZOMBIE_COLOR = Color.BLACK;

    // I18N String constants
    static final ResourceBundle messages = NbBundle.getBundle(ThreadStateColumnImpl.class);

    public static final StateResources THREAD_RUNNING = new StateResources(new Color(84, 185, 72), MSAState.Running);
    public static final StateResources THREAD_RUNNING_USER = new StateResources(new Color(84, 185, 72), MSAState.RunningUser);
    public static final StateResources THREAD_RUNNING_SYSTEM = new StateResources(new Color(0, 166, 80), MSAState.RunningSystemCall);
    public static final StateResources THREAD_RUNNING_OTHER = new StateResources(new Color(0, 169, 157), MSAState.RunningOther);

    public static final StateResources THREAD_BLOCKED = new StateResources(new Color(238, 29, 37), MSAState.Blocked);
    public static final StateResources THREAD_SLEEP_USE_LOCK = new StateResources(new Color(238, 29, 37), MSAState.SleepingUserLock);

    public static final StateResources THREAD_WAITING = new StateResources(new Color(83, 130, 161), MSAState.Waiting);
    public static final StateResources THREAD_WAITING_CPU = new StateResources(new Color(83, 130, 161), MSAState.WaitingCPU);

    public static final StateResources THREAD_SLEEPING = new StateResources(new Color(255, 199, 38), MSAState.Sleeping);
    public static final StateResources THREAD_SLEEPING_OTHER = new StateResources(new Color(255, 199, 38), MSAState.SleepingOther);
    public static final StateResources THREAD_SLEEPING_USER_DATA_PAGE_FAULT = new StateResources(new Color(247, 149, 29), MSAState.SleepingUserDataPageFault);
    public static final StateResources THREAD_SLEEPING_USER_TEXT_PAGE_FAULT = new StateResources(new Color(231, 111, 0), MSAState.SleepingUserTextPageFault);
    public static final StateResources THREAD_SLEEPING_KERNEL_PAGE_FAULT = new StateResources(new Color(114, 138, 132), MSAState.SleepingKernelPageFault);

    public static final StateResources THREAD_THREAD_STOPPED = new StateResources(new Color(255, 242, 0), MSAState.ThreadStopped);

    static Color getThreadStateColor(MSAState threadState) {
        switch(threadState) {
            case ThreadFinished: return THREAD_STATUS_ZOMBIE_COLOR;
            
            case Running: return THREAD_RUNNING.color;
            case RunningUser: return THREAD_RUNNING_USER.color;
            case RunningSystemCall: return THREAD_RUNNING_SYSTEM.color;
            case RunningOther: return THREAD_RUNNING_OTHER.color;

            case Blocked: return THREAD_BLOCKED.color;
            case SleepingUserLock: return THREAD_SLEEP_USE_LOCK.color;

            case Waiting: return THREAD_WAITING.color;
            case WaitingCPU: return THREAD_WAITING_CPU.color;

            case Sleeping: return THREAD_SLEEPING.color;
            case SleepingOther: return THREAD_SLEEPING_OTHER.color;
            case SleepingUserDataPageFault: return THREAD_SLEEPING_USER_DATA_PAGE_FAULT.color;
            case SleepingUserTextPageFault: return THREAD_SLEEPING_USER_TEXT_PAGE_FAULT.color;
            case SleepingKernelPageFault: return THREAD_SLEEPING_KERNEL_PAGE_FAULT.color;

            case Stopped: return THREAD_SLEEPING.color;
            case ThreadStopped: return THREAD_THREAD_STOPPED.color;
        }
        return THREAD_STATUS_UNKNOWN_COLOR;
    }

    static Color getThreadStateColor(ThreadState threadStateColor, int msa) {
        return getThreadStateColor(threadStateColor.getMSAState(msa, false));
    }

    static long timeStampToMilliSeconds(long timeStamp) {
        return TimeUnit.NANOSECONDS.toMillis(timeStamp);
    }

    private final MergedThreadInfo info;
    private final List<ThreadState> list = new ArrayList<ThreadState>();
    private final AtomicInteger comparable = new AtomicInteger();

    ThreadStateColumnImpl(MergedThreadInfo info) {
        this.info = info;
    }

    public void setSummary(int sum) {
        comparable.set(sum);
    }

    public int getSummary() {
        return comparable.get();
    }

    public String getName(){
        return info.getThreadName();
    }

    public int size() {
        return list.size();
    }

    public boolean isAlive(int index) {
        return !list.get(index).getMSAState(0, false).equals(MSAState.ThreadFinished);
    }
    
    public ThreadState getThreadStateAt(int index){
        return list.get(index);
    }

    public boolean isAlive() {
        return !list.get(list.size()-1).getMSAState(0, false).equals(MSAState.ThreadFinished);
    }

    void add(ThreadState state) {
        list.add(state);
    }

    void clearStates() {
        list.clear();
    }

    int getThreadID() {
        return info.getThreadId();
    }
    long getThreadStartTimeStamp() {
        return info.getStartTimeStamp();
    }

    public static final class StateResources {
        final Color color;
        final String name;
        final String tooltip;
        StateResources(Color color, MSAState state){
            this.color = color;
            name = messages.getString("ThreadState"+state.name()+"Name"); // NOI18N
            tooltip = messages.getString("ThreadState"+state.name()+"Tooltip"); // NOI18N
        }
    }
}
