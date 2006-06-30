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
