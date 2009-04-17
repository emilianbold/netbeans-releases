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

package org.netbeans.modules.cnd.debugger.gdb.proxy;

import org.netbeans.modules.cnd.debugger.gdb.utils.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;

/**
 * This class is intended for gathering multiline responses to a single gdb command.
 *
 * @author gordonp
 */
public class CommandBuffer {
    
    // Static parts
    private static enum State {
        NONE, WAITING, TIMEOUT, OK, ERROR;
    }
    
    private static final int WAIT_TIME = 30000;
    private static final boolean timerOn = Boolean.getBoolean("gdb.proxy.timer"); // NOI18N
    
    // Instance parts
    private final StringBuilder buf = new StringBuilder();
    private final int token;
    private String err = null;
    private State state = State.NONE;
    private final Object lock = new Object();
    protected static final Logger log = Logger.getLogger("gdb.logger.cb"); // NOI18N
    private final GdbProxy gdb;
    
    public CommandBuffer(GdbProxy gdb, int token) {
        assert gdb != null;
        this.gdb = gdb;
        this.token = token;
    }
    
    /**
     * Block waiting for the command to complete. Can't be called on the GdbReaderRP
     * thread because thats where the command input gets read.
     * 
     * @return The response from a gdb command
     */
    String waitForCompletion() {
        assert !Thread.currentThread().getName().equals("GdbReaderRP");
        synchronized (lock) {
            if (state == State.NONE) {
                state = State.WAITING; // this will change unless we timeout
            }
            try {
                long tstart = System.currentTimeMillis();
                long tend = tstart;
                while (state == State.WAITING) {
                    lock.wait(WAIT_TIME);
                    tend = System.currentTimeMillis();
                    if ((tend - tstart) > WAIT_TIME) {
                        if (state == State.OK) {
                            log.finest("CB.postAndWait[" + token + "]: Timed out after Done [" + getResponse() + "]");
                        } else {
                            state = State.TIMEOUT;
                        }
                    }
                }
                if (state == State.TIMEOUT) {
                    log.warning("CB.postAndWait[" + token + "]: Timeout at " + tend + " on " + GdbUtils.threadId());
                } else if (log.isLoggable(Level.FINE)) {
                    if (state == State.ERROR &&
                            !Thread.currentThread().getName().equals("ToolTip-Evaluator")) { // NOI18N
                        log.fine("CB.postAndWait[" + token + "]: Error wait of " + (tend - tstart) + " ms on " +
                                GdbUtils.threadId());
                    } else if (state == State.OK) {
                        log.fine("CB.postAndWait[" + token + "]: OK wait of " + (tend - tstart) + " ms on " +
                                GdbUtils.threadId());
                    }
                }
                return getResponse();
            } catch (InterruptedException ex) {
                return "";
            } finally {
                gdb.removeCB(token);
            }
        }
    }

    public int getID() {
        return token;
    }
    
    public void append(String line) {
        buf.append(line);
    }
    
    public void done() {
        String time = getTimePrefix(timerOn && log.isLoggable(Level.FINEST));
        synchronized (lock) {
            state = State.OK;
            log.finest("CB.done[" + time + token + "]: Released lock on " + GdbUtils.threadId());
            //gdb.removeCB(token);
            lock.notifyAll();
        }
    }
    
    public void error(String msg) {
        String time = getTimePrefix(timerOn && log.isLoggable(Level.FINEST));
        synchronized (lock) {
            err = msg;
            state = State.ERROR;
            log.finest("CB.error[" + time + token + "]: Releasing lock on " + GdbUtils.threadId());
            //gdb.removeCB(token);
            lock.notifyAll();
        }
    }
    
    public String getError() {
        if (state == State.ERROR && err != null) {
            return err;
        }
        return null;
    }
    
    public boolean isTimedOut() {
        return state == State.TIMEOUT;
    }

    public boolean isError() {
        return state == State.ERROR;
    }

    public boolean isOK() {
        return state == State.OK;
    }

    public String getResponse() {
        return buf.toString();
    }

    @Override
    public String toString() {
        return "CommandBuffer(id=" + token + ", text=" + getResponse() + ", state=" + state + ", error=" + err + ")"; // NOI18N
    }

    /**
     * @param show - if true - return empty string
     * @return
     */
    public static String getTimePrefix(boolean show) {
        if (show) {
            return Long.toString(System.currentTimeMillis()) + ':';
        } else {
            return "";
        }
    }
}
