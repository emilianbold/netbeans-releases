/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uihandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Jaroslav Tulach
 */
public class UIHandler extends Handler {
    private final Queue<LogRecord> logs;
    private final boolean exceptionOnly;

    private static int MAX_LOGS = 10000;
    
    public UIHandler(Queue<LogRecord> l, boolean exceptionOnly) {
        setLevel(Level.FINEST);
        logs = l;
        this.exceptionOnly = exceptionOnly;
    }

    public void publish(LogRecord record) {
        if (exceptionOnly && record.getThrown() == null) {
            return;
        }
        
        synchronized (UIHandler.class) {
            if (logs.size() > MAX_LOGS) {
                for (int i = 0; i < 100; i++) {
                    logs.poll();
                }
            }
            if (!logs.contains(record)) {
                logs.add(record);
            }
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
    }
    
}
