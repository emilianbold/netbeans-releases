/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.multiview.test.util;

/**
 * @author pfiala
 */
public abstract class StepIterator {
    private Exception error = null;
    private long startTime;
    private long duration;
    private boolean success = false;

    public StepIterator() {
        this(1000, 20000);
    }

    public StepIterator(int stepDuration, int timeout) {
        iterate(stepDuration, timeout);
    }

    public abstract boolean step() throws Exception;

    public void finalCheck() {
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }

    public long getDuration() {
        return duration;
    }

    public void iterate(long stepDuration, long timeout) {
        startTime = System.currentTimeMillis();
        for (; ;) {
            try {
                error = null;
                success = step();
                if (success) {
                    break;
                }
            } catch (Exception e) {
                error = e;
            }
            duration = System.currentTimeMillis() - startTime;
            if (duration > timeout) {
                break;
            }
            try {
                Thread.sleep(stepDuration);
            } catch (InterruptedException ex) {
            }
        }
        finalCheck();
    }
}
