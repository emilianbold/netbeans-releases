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

/**
 *
 * @author gordon
 */
public class TimerRecord {
    
    enum RecordType {Title, Start, Mark, Stop, Restart};
    
    private RecordType type;
    private String tname;
    private long time;
    private String message;
    
    /** Creates a new instance of TimerRecord */
    public TimerRecord(RecordType type, String message) {
        this.type = type;
        tname = Thread.currentThread().getName();
        time = System.currentTimeMillis();
        if (message != null && message.length() > 0) {
            this.message = message + ": ";
        } else {
            this.message = "";
        }
    }
    
    public long getTime() {
        return time;
    }
    
    public String toString() {
        return toString(-1);
    }
    
    public String toString(long relativeTo) {
        StringBuilder s = new StringBuilder();
        
        if (type == RecordType.Title) {
            s.append("\n\nGdbTimer Report: " + message);
        } else {
            s.append("    ");
            s.append(message);
            if (type == RecordType.Start) {
                s.append("Started at ");
            } else if (type == RecordType.Mark) {
                s.append("Marked at ");
            } else if (type == RecordType.Stop) {
                s.append("Stopped at ");
            } else if (type == RecordType.Restart) {
                s.append("Restarteded at ");
            }
            if (relativeTo < 0) {
                s.append(time);
            } else {
                s.append(time - relativeTo);
                s.append(" ms");
        }
        }
        return s.toString();
    }
}
