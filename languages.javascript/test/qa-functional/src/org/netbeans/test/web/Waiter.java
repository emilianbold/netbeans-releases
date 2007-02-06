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

package org.netbeans.test.web;

/**
 * A class taht may be used for waiting e.g. for a event.
 * <p>
 * For example:
 * <p><code><pre>
 * Observable obs;
 * final Waiter waiter = new Waiter();
 * final PropertyChangeListener pcl = new PropertyChangeListener() {
 *    public void propertyChange(PropertyChangeEvent evt) {
 *       if (evt.getPropertyName().equals("..")) {
 *          waiter.notifyFinished();
 *       }
 *    }
 * };
 * obs.addPropertyChangeListener(pcl);
 * // ...
 * waiter.waitFinished();
 * obs.removePropertyChangeListener(pcl);
 * </pre></code>
 * <p>
 *
 * @author ms113234
 */
public class Waiter {
    
    private boolean finished = false;
    
    /** Restarts Synchronizer.
     */
    public void init() {
        synchronized (this) {
            finished = false;
        }
    }
    
    /** Wait until the task is finished.
     */
    public void waitFinished() throws InterruptedException {
        synchronized (this) {
            while (!finished) {
                wait();
            }
        }
    }
    
    /** Wait until the task is finished, but only a given time.
     *  @param milliseconds time in milliseconds to wait
     *  @return true if the task is really finished, or false if the time out
     *     has been exceeded
     */
    public boolean waitFinished(long milliseconds) throws InterruptedException {
        synchronized (this) {
            if (finished) return true;
            long expectedEnd = System.currentTimeMillis() + milliseconds;
            for (;;) {
                wait(milliseconds);
                if (finished) return true;
                long now = System.currentTimeMillis();
                if (now >= expectedEnd) return false;
                milliseconds = expectedEnd - now;
            }
        }
    }
    
    /** Notify all waiters that this task has finished.
     */
    public void notifyFinished() {
        synchronized (this) {
            finished = true;
            notifyAll();
        }
    }
}
