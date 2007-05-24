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

/** An instance of GdbTimer which does nothing. This should be called when timing isn't enabled */
class GdbDummyTimer extends GdbTimer {
    
    GdbDummyTimer() {}
    
    public void reset() {}
    
    /** Start the timer running */
    public void start(String msg) {}
    
    /** Start the timer running */
    public void start(String msg, int count) {}

    /** Mark an intermediate time */
    public void mark(String msg) {}

    /** Stop the timer and mark the time */
    public void stop(String msg) {}

    /** Restart the timer and mark the time */
    public void restart(String msg) {}

    /** Signals timing is done and this timer can be reused */
    public void free() {}

    /** Log the timer information */
    public void report(String msg) {}
    
    /** Return the skip count */
    public int getSkipCount() {
        return 0;
    }
}