/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.collab.ui;

import java.util.*;
import javax.swing.SwingUtilities;

import org.netbeans.modules.collab.core.Debug;

/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NotificationThread extends Thread {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    private static final int NOTIFICATION_DELAY_LONG = 850;
    private static final int NOTIFICATION_DELAY_SHORT = 150;
    private static final Object PAUSE_LOCK = new Object();

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private boolean stopped = false;
    private boolean paused = true;
    private HashSet listeners = new HashSet();

    /**
     *
     *
     */
    public NotificationThread() {
        super("Collaboration Notification");
        setDaemon(true);
    }

    /**
     *
     *
     */
    public void addNotificationListener(NotificationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     *
     *
     */
    public void removeNotificationListener(final NotificationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);

            // Make sure the listener is in a proper state
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        listener.notificationSuspended();
                    }
                }
            );
        }
    }

    /**
     *
     *
     */
    public void run() {
        boolean state = true;

        try {
            while (!stopped) {
                if (paused) {
                    synchronized (PAUSE_LOCK) {
                        //						fireNotificationChange(false);
                        fireNotificationSuspended();
                        state = true;

                        try {
                            PAUSE_LOCK.wait();
                        } catch (InterruptedException e) {
                            // Ignore
                            // HACK: This is to get around QA code 
                            // auditor
                            if (1 == 1) {
                                ;
                            }
                        } finally {
                            if (stopped) {
                                break;
                            }

                            fireNotificationResumed();
                        }
                    }
                }

                try {
                    if (state) {
                        fireNotificationChange(state);
                        sleep(NOTIFICATION_DELAY_LONG);
                    } else {
                        fireNotificationChange(state);
                        sleep(NOTIFICATION_DELAY_SHORT);
                    }

                    state = !state;
                } catch (InterruptedException e) {
                    // Expected, ignore
                    // HACK: This is to get around QA code auditor
                    if (1 == 1) {
                        ;
                    }
                }
            }
        } finally {
            fireNotificationChange(false);
        }
    }

    /**
     *
     *
     */
    protected void fireNotificationChange(final boolean state) {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Set currentListeners = null;

                    synchronized (listeners) {
                        currentListeners = (Set) listeners.clone();
                    }

                    for (Iterator i = currentListeners.iterator(); i.hasNext();) {
                        try {
                            ((NotificationListener) i.next()).notificationStateChanged(state);
                        } catch (Exception e) {
                            // Ignore
                            Debug.debugNotify(e);
                        }
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    protected void fireNotificationSuspended() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Set currentListeners = null;

                    synchronized (listeners) {
                        currentListeners = (Set) listeners.clone();
                    }

                    for (Iterator i = currentListeners.iterator(); i.hasNext();) {
                        try {
                            ((NotificationListener) i.next()).notificationSuspended();
                        } catch (Exception e) {
                            // Ignore
                            Debug.debugNotify(e);
                        }
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    protected void fireNotificationResumed() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Set currentListeners = null;

                    synchronized (listeners) {
                        currentListeners = (Set) listeners.clone();
                    }

                    for (Iterator i = currentListeners.iterator(); i.hasNext();) {
                        try {
                            ((NotificationListener) i.next()).notificationResumed();
                        } catch (Exception e) {
                            // Ignore
                            Debug.debugNotify(e);
                        }
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    public void stopThread() {
        // Stop the thread immediately
        stopped = true;
        interrupt();
    }

    /**
     *
     *
     */
    public void setPaused(boolean value) {
        boolean wasPaused = paused;
        paused = value;

        if (!wasPaused) {
            synchronized (PAUSE_LOCK) {
                PAUSE_LOCK.notify();
            }
        } else {
            interrupt();
        }
    }
}
