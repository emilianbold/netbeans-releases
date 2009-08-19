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

package org.netbeans.modules.dlight.core.stack.api;

import java.util.List;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;

/**
 * The query to be used to get ThreadDump
 * 
 * @author Maria Tishkova
 */
public final class ThreadDumpQuery {
    private final long threadID;
    private final ThreadState threadState;
    private final List<Integer> showThreads;
    private final MSAState prefferedState;
    private final boolean isMSAMode;
    private final boolean isFullMode;
    private final long startTime;


    /**
     * The query to filter out the data for stacks
     * @param threadID thread id
     * @param threadState thread state (to get timestamp)
     * @param showThreads the list of the threads to get thread dump for
     * @param prefferedState the state we would like to get stack for
     * @param isMSAMode 
     * @param isFullMode
     * @param startTime start time
     */
    public ThreadDumpQuery(long threadID, ThreadState threadState, List<Integer> showThreads, MSAState prefferedState, boolean isMSAMode, boolean isFullMode, long startTime){
        this.threadID = threadID;
        this.threadState = threadState;
        this.showThreads = showThreads;
        this.prefferedState = prefferedState;
        this.isMSAMode = isMSAMode;
        this.isFullMode = isFullMode;
        this.startTime = startTime;
    }

    public boolean isFullMode() {
        return isFullMode;
    }

    public boolean isMSAMode() {
        return isMSAMode;
    }

    public MSAState getPrefferedState() {
        return prefferedState;
    }

    public List<Integer> getShowThreads() {
        return showThreads;
    }

    public long getStartTime() {
        return startTime;
    }

    public ThreadState getThreadState() {
        return threadState;
    }

    public long getThreadID() {
        return threadID;
    }

    
}
