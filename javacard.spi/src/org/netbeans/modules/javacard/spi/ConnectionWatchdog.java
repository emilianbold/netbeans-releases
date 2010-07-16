/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.spi;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Utility class for use by Card implementations which need to poll a port,
 * make an http connection, run some executable, or otherwise poll to determine
 * the card's connected status.
 * <p>
 * To use, simply create an instance in your Card implementation's constructor,
 * and hold a reference to it;  implement ConnectionWatchdog.Callback to
 * handle long-running I/O and updating your Card instance.
 * <p>
 * What this class does in particular is handle the IDE's idle state
 * correctly:  When the user is using another application (i.e. no window
 * in the IDE has input focus), polling is shut down, to avoid interfering
 * with the performance of other applications when the user is doing
 * something else.
 * <p>
 * It also manages lifecycle correctly:  The passed Card instance is
 * weakly referenced.  Polling will stop if the Card instance is garbage
 * collected, and listeners on the main window which track activation
 * will be removed, thus avoiding memory leaks.
 *
 * @author Tim Boudreau
 */
public final class ConnectionWatchdog<CardImpl extends Card> {
    private final R r = new R();
    private final Reference<CardImpl> cardRef;
    private volatile boolean attached;
    private volatile boolean applicationIsActive;
    private static final RequestProcessor rp = new RequestProcessor("Java Card " + //NOI18N
            "connection polling thread pool", 1); //NOI18N
    private final RequestProcessor.Task task = rp.create(r);
    private static final Logger LOGGER = Logger.getLogger(ConnectionWatchdog.class.getName());
    private boolean done;
    private final Callback<CardImpl> callback;
    private int POLL_FREQUENCY = 30000; //Poll every 30 seconds
    private int delayAfterActivation = 200;
    private boolean enabled = true;

    /**
     * Create a new ConnectionWatchdog for the specified card, with a
     * specific polling frequency and initial delay.
     *
     * @param card The card.
     * @param callback A callback to invoke on a background thread,
     * to update the card's state, every pollFrequency milliseconds while
     * the IDE main window is active
     * @param pollFrequency How frequently polling should occur, in milliseconds
     * @param delayAfterActivation How soon after the main window is reactivated
     * polling should occur
     */
    public ConnectionWatchdog(CardImpl card, Callback<CardImpl> callback, int pollFrequency, int delayAfterActivation) {
        assert pollFrequency > 0 : "Negative or 0 polling frequency: " + pollFrequency; //NOI18N
        assert delayAfterActivation > 0 : "Negative or 0 delay: " + delayAfterActivation; //NOI18N
        Parameters.notNull("card", card); //NOI18N
        Parameters.notNull("callback", callback); //NOI18N
        this.POLL_FREQUENCY = pollFrequency;
        this.delayAfterActivation = delayAfterActivation;
        this.cardRef = new WeakReference<CardImpl>(card);
        this.callback = callback;
        EventQueue.invokeLater(r);
    }

    /**
     * Create a new ConnectionWatchdog for the specified card, with a default
     * polling frequency of 30 seconds and initial delay of 200 milliseconds.
     *
     * @param card The card
     * @param callback A callback to invoke on a background thread,
     * to update the card's state, every pollFrequency milliseconds while
     * the IDE main window is active
     */
    public ConnectionWatchdog(CardImpl card, Callback<CardImpl> callback) {
        Parameters.notNull("card", card); //NOI18N
        Parameters.notNull("callback", callback); //NOI18N
        this.cardRef = new WeakReference<CardImpl>(card);
        this.callback = callback;
        EventQueue.invokeLater(r);
    }

    /**
     * Instruct this CardWatchdog to refresh its state as soon as
     * possible, rather than waiting for the next polling interval.
     * Does nothing if the IDE does not have any active window.
     */
    public final void refreshNow() {
        if (applicationIsActive) {
            task.schedule(1);
        }
    }

    /**
     * Enable/disable polling.  Some implementations may need to
     * disable polling during certain activities, such as while loading
     * onto a card.
     *
     * @param val enabled or not
     */
    public void setEnabled (boolean val) {
        enabled = val;
    }

    /**
     * Determine whether polling is currently enabled.  The default return
     * value is true.
     * @return Whether or not this ConnectionWatchdog is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * A callback which will be invoked to update the state of a card
     * periodically.
     * @param <T> The type of the Card instance which will be passed to the
     * poll() method
     */
    public interface Callback<T extends Card> {
        /**
         * Update the state of the card, based on some network or other
         * operation.  This method is guaranteed never to be called on the
         * AWT event theread.
         *
         * @param card The card
         * @throws Exception if something goes wrong.  Note that throwing an
         * exception from this method is treated as in indication that the
         * owning ConnectionWatchdog should shut down and do no further
         * polling.
         */
        public void poll(T card) throws Exception;
    }

    private void done() {
        if (!done) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, super.toString() + " shutting down"); //NOI18N
            }
            task.cancel();
            EventQueue.invokeLater(r);
            done = true;
        }
    }

    private class R extends WindowAdapter implements Runnable {
        public void run() {
            if (EventQueue.isDispatchThread()) {
                if (!attached && !done) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Attaching window listener for polling " + //NOI18N
                                this);
                    }
                    Frame f = WindowManager.getDefault().getMainWindow();
                    f.addWindowListener(this);
                    setApplicationActive(f.isActive() || KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != null);
                } else {
                    WindowManager.getDefault().getMainWindow().removeWindowListener(this);
                }
            } else {
                if (enabled) {
                    updateCardStatus();
                }
            }
        }

        @Override
        public void windowActivated(WindowEvent e) {
            setApplicationActive(true);
        }

        @Override
        public void windowClosed(WindowEvent e) {
            super.windowClosed(e);
            done();
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            setApplicationActive(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != null);
        }
    }

    void setApplicationActive(boolean val) {
        if (applicationIsActive != val) {
            applicationIsActive = val;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, this + " application active " + val); //NOI18N
            }
            if (applicationIsActive) {
                task.schedule(delayAfterActivation);
            } else {
                task.cancel();
            }
        }
    }

    private CardImpl getCard() {
        CardImpl result = cardRef.get();
        if (result == null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, super.toString() + " Lost reference to card"); //NOI18N
            }
            done();
        }
        return result;
    }

    private void updateCardStatus() {
        assert !EventQueue.isDispatchThread();
        CardImpl card = getCard();
        if (card != null) {
            try {
                callback.poll(card);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                done();
            } finally {
                if (!done) {
                    task.schedule(POLL_FREQUENCY);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        CardImpl card = getCard();
        if (card != null) {
            sb.append (" for "); //NOI18N
            sb.append (card);
            sb.append (" at "); //NOI18N
            try {
                sb.append(callback);
            } catch (Exception ex) {
                sb.append (ex.getMessage());
            }
        } else {
            sb.append (" dead card: " + callback); //NOI18N
        }
        return sb.toString();
    }
}
