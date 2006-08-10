/*
 * UIHandler.java
 *
 * Created on 10. srpen 2006, 14:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
