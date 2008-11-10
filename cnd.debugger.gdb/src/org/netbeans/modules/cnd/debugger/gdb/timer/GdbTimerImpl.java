/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.debugger.gdb.timer;

import java.util.LinkedList;
import java.util.Queue;
import org.netbeans.modules.cnd.debugger.gdb.timer.TimerRecord.RecordType;

/**
 * An instance of GdbTimer performs timing. This should be called when
 * timing is enabled and the default timer is requested. Its also used for custom
 * timing instances.
 */
class GdbTimerImpl extends GdbTimer {
    
    private Queue<TimerRecord> queue = new LinkedList();
    private int skipCount = 0;
    private boolean in_use = false;
    
    public void reset() {
        skipCount = 0;
        in_use = false;
    }
    
    /** Start the timer running */
    public void start(String msg) {
        start(msg, 0);
    }
    
    /**
     * Start the timer running. This form of the command if for situations where start will be
     * called mulitple times but we want to ignore subsequent calls. For instance, if we're
     * timing multiple steps we start on the 1st one and ignore the next skipCount start calls.
     *
     * @param msg The message to use as a label
     * @param skipCount Number of starts to skip
     */
    public void start(String msg, int skipCount) {
        if (queue.isEmpty() && this.skipCount == 0 && !in_use) {
            queue.add(new TimerRecord(RecordType.Start, msg));
            this.skipCount = skipCount;
            in_use = true;
        } else {
            this.skipCount--;
        }
    }

    /** Mark an intermediate time */
    public void mark(String msg) {
        if (in_use) {
            queue.add(new TimerRecord(RecordType.Mark, msg));
        }
    }

    /** Stop the timer and mark the time */
    public void stop(String msg) {
        if (in_use) {
            queue.add(new TimerRecord(RecordType.Stop, msg));
        }
    }

    /** Restart the timer and mark the time */
    public void restart(String msg) {
        if (in_use) {
            queue.add(new TimerRecord(RecordType.Restart, msg));
        }
    }

    /** Signals timing is done and this timer can be reused */
    public void free() {
        if (in_use) {
            queue.clear();
        }
    }

    /** Log the timer information */
    public void report(String msg) {
        if (in_use) {
            long starttime = -1;

            if (!queue.isEmpty()) {
                TimerRecord first = queue.peek();
                starttime = first.getTime();
            }
            for (TimerRecord record : queue) {
                System.err.println(record.toString(starttime));
            }
            free();
        }
    }
    
    /** Return the skipCount count */
    public int getSkipCount() {
        return skipCount;
    }
}
