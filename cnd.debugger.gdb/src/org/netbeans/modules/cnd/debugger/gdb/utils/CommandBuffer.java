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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended for gathering multiline responses to a single gdb command.
 * 
 * @author gordonp
 */
public class CommandBuffer {
    
    // Static parts
    public static final int STATE_NONE = 0;
    public static final int STATE_WAITING = 1;
    public static final int STATE_COMMAND_TIMEDOUT = 2;
    public static final int STATE_OK = 3;
    public static final int STATE_ERROR = 4;
    private final int WAIT_TIME = 30000;
    private boolean timerOn = Boolean.getBoolean("gdb.proxy.timer"); // NOI18N
    
    private static Map<Integer, CommandBuffer> map = new HashMap<Integer, CommandBuffer>();
    
    public static CommandBuffer getCommandBuffer(Integer id) {
        return map.get(id);
    }
    
    // Instance parts
    private StringBuilder buf;
    private Integer token;
    private String err;
    private int state;
    private Object lock;
    protected static Logger log = Logger.getLogger("gdb.logger.cb"); // NOI18N
    
    public CommandBuffer() {
        buf = new StringBuilder();
        token = null;
        state = STATE_NONE;
        err = null;
        lock = new Object();
    }
    
    /**
     * Block waiting for the command to complete. Can't be called on the GdbReaderRP
     * thread because thats where the command input gets read.
     * 
     * @return The response from a gdb command
     */
    public String waitForCompletion() {
        assert !Thread.currentThread().getName().equals("GdbReaderRP");
        synchronized (lock) {
            if (state == STATE_NONE) {
                state = STATE_WAITING; // this will change unless we timeout
            }
            try {
                long tstart = System.currentTimeMillis();
                long tend = tstart;
                while (state == STATE_WAITING) {
                    lock.wait(WAIT_TIME);
                    tend = System.currentTimeMillis();
                    if ((tend - tstart) > WAIT_TIME) {
                        if (state == STATE_OK) {
                            log.finest("CB.postAndWait[" + token + "]: Timed out after Done [" + toString() + "]");
                        } else {
                            state = STATE_COMMAND_TIMEDOUT;
                        }
                    }
                }
                if (state == STATE_COMMAND_TIMEDOUT) {
                    log.warning("CB.postAndWait[" + token + "]: Timeout at " + tend + " on " + GdbUtils.threadId());
                } else if (log.isLoggable(Level.FINE)) {
                    if (state == STATE_ERROR && 
                            !Thread.currentThread().getName().equals("ToolTip-Evaluator")) { // NOI18N
                        log.fine("CB.postAndWait[" + token + "]: Error wait of " + (tend - tstart) + " ms on " +
                                GdbUtils.threadId());
                    } else if (state == STATE_OK) {
                        log.fine("CB.postAndWait[" + token + "]: OK wait of " + (tend - tstart) + " ms on " +
                                GdbUtils.threadId());
                    }
                }
                map.remove(token);
                return toString();
            } catch (InterruptedException ex) {
                map.remove(token);
                return "";
            }
        }
    }
    
    public Integer getID() {
        return token;
    }
    
    public void setID(int token) {
        synchronized (lock) {
            this.token = Integer.valueOf(token);
            map.put(this.token, this);
        }
    }
    
    public int getState() {
        return state;
    }
    
    public void append(String line) {
        buf.append(line);
    }
    
    public void done() {
        String time;
        if (timerOn && log.isLoggable(Level.FINEST)) {
            time = Long.toString(System.currentTimeMillis()) + ':';
        } else {
            time = "";
        }
        synchronized (lock) {
            state = STATE_OK;
            lock.notifyAll();
            log.finest("CB.done[" + time + token + "]: Released lock on " + GdbUtils.threadId());
        }
    }
    
    public void error(String msg) {
        String time;
        if (timerOn && log.isLoggable(Level.FINEST)) {
            time = Long.toString(System.currentTimeMillis()) + ':';
        } else {
            time = "";
        }
        synchronized (lock) {
            err = msg;
            state = STATE_ERROR;
            log.finest("CB.error[" + time + token + "]: Releasing lock on " + GdbUtils.threadId());
            lock.notifyAll();
        }
    }
    
    public String getError() {
        if (state == STATE_ERROR && err != null) {
            return err;
        }
        return null;
    }
    
    public boolean timedOut() {
        return state == STATE_COMMAND_TIMEDOUT;
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
