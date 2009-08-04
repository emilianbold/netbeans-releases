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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.module.dlight.threads.api.ThreadDump;
import org.netbeans.module.dlight.threads.api.ThreadSnapshot;
import org.netbeans.module.dlight.threads.api.storage.ThreadState;
import org.netbeans.module.dlight.threads.api.storage.ThreadState.MSAState;
import org.netbeans.module.dlight.threads.api.storage.ThreadStateColumn;

/**
 *
 * @author Alexander Simon
 */
public final class StackTraceDescriptor implements ThreadDump {

    private ThreadDump stackTrace;
    private final List<Integer> showThreads;
    private final long startTime;
    private long stackTimeStamp;

    public StackTraceDescriptor(ThreadState state, ThreadStateColumn stackProvider, List<Integer> showThreads, MSAState prefferedState, boolean isMSAMode, boolean isFullMode, long startTime) {
        this.startTime = startTime;
        this.showThreads = showThreads;
        int msaIndex = -1;
        if (isMSAMode) {
            for (int i = 0; i < state.size(); i++) {
                if (prefferedState.equals(state.getMSAState(i, isFullMode))) {
                    msaIndex = i;
                    break;
                }
            }
        } else {
            msaIndex = state.getSamplingStateIndex(isFullMode);
        }
        if (msaIndex >= 0) {
            stackTimeStamp = state.getTimeStamp(msaIndex);
            if (stackTimeStamp >= 0) {
                stackTrace = stackProvider.getStackTrace(stackTimeStamp);
            } else {
                stackTimeStamp = state.getTimeStamp();
            }
        }
    }

    public List<ThreadSnapshot> getThreadStates() {
        List<ThreadSnapshot> selectesStacks = new ArrayList<ThreadSnapshot>();
        if (stackTrace != null) {
            for (Integer info : showThreads) {
                for (ThreadSnapshot stack : stackTrace.getThreadStates()) {
                    if (info.intValue() == stack.getThreadInfo().getThreadId()) {
                        selectesStacks.add(stack);
                        break;
                    }
                }
            }
        }
        return selectesStacks;
    }

    public long getTimestamp(){
        return ThreadStateColumnImpl.timeStampToMilliSeconds(stackTimeStamp) - startTime;
    }
}
