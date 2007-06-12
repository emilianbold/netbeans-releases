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

import java.util.HashMap;
import java.util.Map;

/**
 * Used for performance testing. This file is not intended to display any information to
 * users. So all strings should be marked NOI18N.
 *
 * @author gordon
 */
public abstract class GdbTimer {
    
    public enum TimerType {Dummy, Default, Custom};
    
    private static boolean enabled = Boolean.getBoolean("org.netbeans.modules.cnd.gdb.timer.GdbTimer");
    
    private static GdbTimer dummy = new GdbDummyTimer();
    
    private static GdbTimer default_instance;
    
    private static Map<String, GdbTimer> map = new HashMap();
    
    /** GdbTimer factory */
    public static GdbTimer getTimer(String name) {
        if (Boolean.getBoolean("org.netbeans.modules.cnd.gdb.timer.GdbTimer." + name)) {
            GdbTimer timer = map.get(name);
            
            if (timer == null) {
                timer = new GdbTimerImpl();
                map.put(name, timer);
            }
            return timer;
        } else {
            return dummy;
        }
    }
    
    public static void release(String name) {
        map.remove(name);
    }
        
    
    GdbTimer() {}
    
    /** Start the timer running */
    public abstract void start(String msg);
    
    /**
     * Start the timer running. This form of the command if for situations where start will be
     * called mulitple times but we want to ignore subsequent calls. For instance, if we're
     * timing multiple steps we start on the 1st one and ignore the next count start calls.
     *
     * @param msg The message to use as a label
     * @param count Number of starts to allow before throwing IllegalStateException
     */
    public abstract void start(String msg, int count);
    
    public abstract void reset();
    
    /** Mark an intermediate time */
    public abstract void mark(String msg);
    
    /** Stop the timer and mark the time */
    public abstract void stop(String msg);
    
    /** Restart the timer and mark the time */
    public abstract void restart(String msg);
    
    /** Signals timing is done and this timer can be reused */
    public abstract void free();
    
    /** Log the timer information */
    public abstract void report(String msg);
    
    /** Return the skip count */
    public abstract int getSkipCount();
}
