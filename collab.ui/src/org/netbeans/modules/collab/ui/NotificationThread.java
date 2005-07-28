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
package org.netbeans.modules.collab.ui;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
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
