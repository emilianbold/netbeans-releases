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
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadInfo;
import org.netbeans.modules.dlight.threadmap.support.spi.ThreadState;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadData {

    public static final byte THREAD_STATUS_UNKNOWN = -1; // Thread status is unknown.
    public static final byte THREAD_STATUS_ZOMBIE = 0; // Thread is waiting to die. Also used for "doesn't exist yet" and "dead"
    public static final byte THREAD_STATUS_RUNNING = 1; // Thread is runnable. Note that we unfortunately don't know whether it'
                                                        // s actually running or pre-empted by another thread...
    public static final byte THREAD_STATUS_SLEEPING = 2; // Thread is sleeping - Thread.sleep() or JVM_Sleep() was called
    public static final byte THREAD_STATUS_MONITOR = 3; // Thread is waiting on a java monitor
    public static final byte THREAD_STATUS_WAIT = 4; // Thread is waiting - Thread.wait() or JVM_MonitorWait() was called

    /** Thread status is unknown. */
    public static final java.awt.Color THREAD_STATUS_UNKNOWN_COLOR = java.awt.Color.LIGHT_GRAY;
    /** Thread is waiting to die. Also used for "doesn't exist yet" and "dead" */
    public static final Color THREAD_STATUS_ZOMBIE_COLOR = Color.BLACK;
    /** Thread is runnable. Note that we unfortunately don't know whether it's actually running or
     * pre-empted by another thread...*/
    public static final java.awt.Color THREAD_STATUS_RUNNING_COLOR = new java.awt.Color(58, 228, 103);
    /** Thread is sleeping - Thread.sleep() or JVM_Sleep() was called */
    public static final java.awt.Color THREAD_STATUS_SLEEPING_COLOR = new java.awt.Color(155, 134, 221);
    /** Thread is waiting on a java monitor */
    public static final java.awt.Color THREAD_STATUS_MONITOR_COLOR = new java.awt.Color(255, 114, 102);
    /** Thread is waiting - Thread.wait() or JVM_MonitorWait() was called */
    public static final java.awt.Color THREAD_STATUS_WAIT_COLOR = new java.awt.Color(255, 228, 90);

    static final byte NO_STATE = 127;

    // I18N String constants
    static final ResourceBundle messages = NbBundle.getBundle(ThreadData.class);
    public static final String THREAD_STATUS_UNKNOWN_STRING = messages.getString("CommonConstants_ThreadStatusUnknownString"); // NOI18N 
    public static final String THREAD_STATUS_ZOMBIE_STRING = messages.getString("CommonConstants_ThreadStatusZombieString"); // NOI18N
    public static final String THREAD_STATUS_RUNNING_STRING = messages.getString("CommonConstants_ThreadStatusRunningString"); // NOI18N
    public static final String THREAD_STATUS_SLEEPING_STRING = messages.getString("CommonConstants_ThreadStatusSleepingString"); // NOI18N;
    public static final String THREAD_STATUS_MONITOR_STRING = messages.getString("CommonConstants_ThreadStatusMonitorString"); // NOI18N
    public static final String THREAD_STATUS_WAIT_STRING = messages.getString("CommonConstants_ThreadStatusWaitString"); // NOI18N

    static Color getThreadStateColor(int threadState) {
        switch(threadState) {
            case THREAD_STATUS_UNKNOWN: return THREAD_STATUS_UNKNOWN_COLOR;
            case THREAD_STATUS_ZOMBIE: return THREAD_STATUS_ZOMBIE_COLOR;
            case THREAD_STATUS_RUNNING: return THREAD_STATUS_RUNNING_COLOR;
            case THREAD_STATUS_SLEEPING: return THREAD_STATUS_SLEEPING_COLOR;
            case THREAD_STATUS_MONITOR: return THREAD_STATUS_MONITOR_COLOR;
            case THREAD_STATUS_WAIT: return THREAD_STATUS_WAIT_COLOR;
        }
        return THREAD_STATUS_UNKNOWN_COLOR;
    }

    private final ThreadInfo info;
    private final List<ThreadState> list = new ArrayList<ThreadState>();

    ThreadData(ThreadInfo info) {
        this.info = info;
    }

    int size() {
        return list.size();
    }

    long getTimeStampAt(int index) {
        return list.get(index).getTimeStamp(index);
    }

    boolean isAlive(int index) {
        return !list.get(index).getStateName(0).equals(ThreadState.ShortThreadState.NotExist.name());
    }
    
    Color getThreadStateColorAt(int index){
        return getThreadStateColor(1);
    }

    ThreadState getThreadStateAt(int index){
        return list.get(index);
    }

    ThreadState getLastState() {
        return list.get(list.size()-1);
    }

    boolean isAlive() {
        return !list.get(list.size()-1).getStateName(0).equals(ThreadState.ShortThreadState.NotExist.name());
    }


    void add(ThreadState state) {
        list.add(state);
    }

    String getName(){
        return info.getThreadName();
    }

    void clearStates() {
        
    }
}
