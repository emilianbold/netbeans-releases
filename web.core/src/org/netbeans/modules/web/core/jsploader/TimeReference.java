/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.jsploader;

/** Reference that holds a value that may expire, because it is outdated.
 *
 * @author Petr Jiricka
 */
public class TimeReference {
    
    private Object value;
    private long timestamp;
    
    /** Creates a new instance of TimeBasedCache */
    public TimeReference() {
        value = null;
        timestamp = -1;
    }
    
    /** Returns the value, if it is still up to date.
     * If the value is not set, or if it is out of date, returns null.
     */
    public synchronized Object get(long currentTimestamp) {
        if (currentTimestamp > timestamp) {
            // out of date
            value = null;
        }
        return value;
    }
    
    /** Puts the given value to the reference,
     * or updates the value if it already exists. As the 
     * timestamp of the value it uses the supplied long value.
     */
    public synchronized void put(Object value, long newTimestamp) {
        if (newTimestamp > timestamp) {
            this.timestamp = newTimestamp;
            this.value = value;
        }
    }
    
}
