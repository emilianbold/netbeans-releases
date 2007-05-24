/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    GdbTimerImpl() {
    }
    
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
