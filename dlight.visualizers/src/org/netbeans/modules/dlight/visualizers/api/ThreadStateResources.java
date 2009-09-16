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
package org.netbeans.modules.dlight.visualizers.api;

import java.awt.Color;
import java.util.ResourceBundle;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.openide.util.NbBundle;

public enum ThreadStateResources {

    THREAD_RUNNING(new Color(0xB2, 0xBC, 0x00), MSAState.Running),
    THREAD_RUNNING_USER(new Color(0xB2, 0xBC, 0x00), MSAState.RunningUser),
    THREAD_RUNNING_SYSTEM(new Color(0, 166, 80), MSAState.RunningSystemCall),
    THREAD_RUNNING_OTHER(new Color(0, 169, 157), MSAState.RunningOther),

    THREAD_BLOCKED(new Color(0xE7, 0x6F, 0x00), MSAState.Blocked),
    THREAD_SLEEP_USE_LOCK(new Color(0xE7, 0x6F, 0x00), MSAState.SleepingUserLock),

    THREAD_WAITING(new Color(0xFF, 0xC7, 0x26), MSAState.Waiting),
    THREAD_WAITING_CPU(new Color(0xFF, 0xC7, 0x26), MSAState.WaitingCPU),

    THREAD_SLEEPING(new Color(0x53, 0x82, 0xA1), MSAState.Sleeping),
    THREAD_SLEEPING_OTHER(new Color(0x53, 0x82, 0xA1), MSAState.SleepingOther),
    THREAD_SLEEPING_USER_DATA_PAGE_FAULT(new Color(247, 149, 29), MSAState.SleepingUserDataPageFault),
    THREAD_SLEEPING_USER_TEXT_PAGE_FAULT(new Color(231, 111, 0), MSAState.SleepingUserTextPageFault),
    THREAD_SLEEPING_KERNEL_PAGE_FAULT(new Color(114, 138, 132), MSAState.SleepingKernelPageFault),

    THREAD_THREAD_STOPPED(new Color(255, 242, 0), MSAState.ThreadStopped);


    public final Color color;
    public final String name;
    public final String tooltip;

    private ThreadStateResources(Color color, MSAState state) {
        this.color = color;
        ResourceBundle bundle = NbBundle.getBundle(getClass());
        name = bundle.getString("ThreadState" + state.name() + "Name"); // NOI18N
        tooltip = bundle.getString("ThreadState" + state.name() + "Tooltip"); // NOI18N
    }

    public static ThreadStateResources forState(MSAState threadState) {
        if (threadState == null){
            return THREAD_THREAD_STOPPED;
        }
        switch(threadState) {
            case Running: return THREAD_RUNNING;
            case RunningUser: return THREAD_RUNNING_USER;
            case RunningSystemCall: return THREAD_RUNNING_SYSTEM;
            case RunningOther: return THREAD_RUNNING_OTHER;

            case Blocked: return THREAD_BLOCKED;
            case SleepingUserLock: return THREAD_SLEEP_USE_LOCK;

            case Waiting: return THREAD_WAITING;
            case WaitingCPU: return THREAD_WAITING_CPU;

            case Sleeping: return THREAD_SLEEPING;
            case SleepingOther: return THREAD_SLEEPING_OTHER;
            case SleepingUserDataPageFault: return THREAD_SLEEPING_USER_DATA_PAGE_FAULT;
            case SleepingUserTextPageFault: return THREAD_SLEEPING_USER_TEXT_PAGE_FAULT;
            case SleepingKernelPageFault: return THREAD_SLEEPING_KERNEL_PAGE_FAULT;
            case Stopped: return THREAD_SLEEPING;

            case ThreadStopped: return THREAD_THREAD_STOPPED;
        }
        return null;
    }
}
