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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.openide.*;

import com.sun.collablet.CollabManager;
import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.CollabSession;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.CollabSettings;

/**
 * Listens to the AWT event queue and determines when the user is idle after
 * an inactive interval.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IdleDetectionListener extends Object implements AWTEventListener, ActionListener {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    protected static final long EVENT_MASK = AWTEvent.ACTION_EVENT_MASK | AWTEvent.ADJUSTMENT_EVENT_MASK// | AWTEvent.COMPONENT_EVENT_MASK 
        // | AWTEvent.CONTAINER_EVENT_MASK 
        // | AWTEvent.FOCUS_EVENT_MASK 
        // | AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK 
        // | AWTEvent.HIERARCHY_EVENT_MASK 
         | AWTEvent.INPUT_METHOD_EVENT_MASK | AWTEvent.INVOCATION_EVENT_MASK | AWTEvent.ITEM_EVENT_MASK |
        AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
        AWTEvent.MOUSE_WHEEL_EVENT_MASK// | AWTEvent.PAINT_EVENT_MASK 
         | AWTEvent.TEXT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK// | AWTEvent.WINDOW_FOCUS_EVENT_MASK 
         | AWTEvent.WINDOW_STATE_EVENT_MASK;
    public static final int DEFAULT_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    private static IdleDetectionListener instance;

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    protected final Object TIMER_LOCK = new Object();
    private Timer timer;

    /**
     *
     *
     */
    public IdleDetectionListener() {
        super();
    }

    /**
     *
     *
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     *
     *
     */
    protected void setTimer(Timer value) {
        if (timer != null) {
            timer.stop();
        }

        timer = value;
    }

    /**
     *
     *
     */
    public void eventDispatched(AWTEvent event) {
        synchronized (TIMER_LOCK) {
            if (!acceptEvent(event)) {
                return;
            }

            // Some events indicate immediately that the user's status has 
            // changed.  Handle these
            if (handleSpecialEvent(event)) {
                Debug.log(IdleDetectionListener.class, "Handled special event: " + event); // NOI18N

                return;
            }

            // If no timer is currently running (meaning the user is idle),
            // start a new timer.  Otherwise, just refresh the current timer.
            if (getTimer() == null) {
                // Set the sessions to online status
                setSessionsStatus(CollabPrincipal.STATUS_ONLINE, ""); // NOI18N

                // Get the user's timeout setting
                int timeout = DEFAULT_TIMEOUT;
                Integer timeoutSetting = CollabSettings.getDefault().getIdleTimeout();

                if (timeoutSetting != null) {
                    // Turn off the timer if the value is a non-sensical value
                    if (timeoutSetting.intValue() <= 0) {
                        return;
                    }

                    // Calculate the timeout. Note, the setting is in minutes.
                    timeout = Math.abs(timeoutSetting.intValue()) * 60 * 1000;
                }

                setTimer(new Timer(timeout, this));
                getTimer().setRepeats(false);
                getTimer().start();
            } else {
                Debug.log(IdleDetectionListener.class, "Restarting idle timeout timer: " + event); // NOI18N

                // Simply refresh the current timer
                getTimer().restart();
            }
        }
    }

    /**
     *
     *
     */
    protected boolean acceptEvent(AWTEvent event) {
        if (event instanceof MouseEvent) {
            // Ignore all non-click mouse events
            return ((MouseEvent) event).getClickCount() > 0;
        } else if (event instanceof WindowEvent) {
            // Ignore events on dialogs (like conversation invitation)
            if (event.getSource() instanceof Dialog) {
                return false;
            }

            // Ignore certain window events.  This is necessary to allow special
            // handling of window iconification (otherwise, these other events 
            // occurs after the window iconified event and cause the status
            // to change to online).
            WindowEvent windowEvent = (WindowEvent) event;

            switch (windowEvent.getID()) {
            case WindowEvent.WINDOW_STATE_CHANGED:
            case WindowEvent.WINDOW_LOST_FOCUS:
            case WindowEvent.WINDOW_DEACTIVATED:
                return false;

            default:
                return true;
            }
        }

        return true;
    }

    /**
     *
     *
     */
    protected boolean handleSpecialEvent(AWTEvent event) {
        if (event instanceof WindowEvent) {
            // If the window is iconified, immediately mark sessions as idle.
            // When the window is deiconified, it will be marked active through
            // the normal means.
            if (((WindowEvent) event).getID() == WindowEvent.WINDOW_ICONIFIED) {
                synchronized (TIMER_LOCK) {
                    setTimer(null);

                    // Set the sessions to away status
                    setSessionsStatus(CollabPrincipal.STATUS_IDLE, ""); // NOI18N
                }

                return true;
            }
        }

        return false;
    }

    /**
     *
     *
     */
    public void actionPerformed(ActionEvent event) {
        synchronized (TIMER_LOCK) {
            setTimer(null);

            // Set the sessions to away status
            setSessionsStatus(CollabPrincipal.STATUS_IDLE, ""); // NOI18N
        }
    }

    /**
     * Must be called within scope of TIMER_LOCK's monitor
     *
     */
    private void setSessionsStatus(final int status, final String message) {
        Debug.log(
            this,
            "Automatically setting session status to " + // NOI18N
            ((status == CollabPrincipal.STATUS_IDLE) ? "IDLE" : "ONLINE") + // NOI18N
            " at " + new java.util.Date()
        ); // NOI18N

        // Set the sessions to away status
        CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            final Exception ex = new Exception("Additional trace information");

            CollabSession[] sessions = manager.getSessions();

            for (int i = 0; i < sessions.length; i++) {
                final CollabSession session = sessions[i];
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            try {
                                // There could be a race condition here; the 
                                // session might go out of scope before this code
                                // is invoked
                                if (session.isValid()) {
                                    session.publishStatus(status, message);
                                }
                            } catch (Exception e) {
                                Debug.debugNotify(ErrorManager.getDefault().annotate(e, ex));
                            }
                        }
                    }
                );
            }
        }
    }

    /**
     * Attaches the default idle detection listener
     *
     */
    public static synchronized void attach() {
        if (instance == null) {
            Debug.log(IdleDetectionListener.class, "Attaching idle listener to " + // NOI18N
                "AWT event queue"
            ); // NOI18N

            instance = new IdleDetectionListener();
            Toolkit.getDefaultToolkit().addAWTEventListener(instance, EVENT_MASK);
        }
    }

    /**
     * Detatches the default idle detection listener
     *
     */
    public static synchronized void detatch() {
        if (instance != null) {
            Debug.log(IdleDetectionListener.class, "Detaching idle listener from " + // NOI18N
                "AWT event queue"
            ); // NOI18N

            if (instance.getTimer() != null) {
                instance.getTimer().stop();
                instance.setTimer(null);
            }

            Toolkit.getDefaultToolkit().removeAWTEventListener(instance);
            instance = null;
        }
    }

    /**
     *
     *
     */
    public static synchronized IdleDetectionListener getInstance() {
        return instance;
    }
}
