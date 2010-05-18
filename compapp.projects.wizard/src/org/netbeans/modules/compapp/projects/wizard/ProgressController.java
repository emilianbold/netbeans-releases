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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;

/**
 * An adapter for ProgressHandle objects.  A ProgressController is a management
 * object that is used to start/stop the task, and to report its progress.
 *
 * @see org.netbeans.modules.compapp.projects.wizard.ProgressDialogFactory
 * @see org.netbeans.api.progress.ProgressHandle
 *
 * @author  Tientien Li
 */
public class ProgressController {
    
    ProgressController(ProgressHandle handle, Component uiComponent) {
        if (handle == null) {
            throw new NullPointerException("handle");
        }
        if (uiComponent == null) {
            throw new NullPointerException("uiComponent");
        }
        progress = handle;
        target = uiComponent;
    }
    
    /**
     * Stop the task. Calling this method causes the associated presentation
     * component to be made not visible.
     */
    public void finish() {
        avoidEventDispatchThreadExecution();
        
        synchronized (this) {
            progress.finish();
            finished = true;
        }

        Thread job = new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            target.firePropertyChange(
                                    ProgressDialogFactory.FINISH_OPTION, 0, 1);
                        }
                    });
                    Thread.sleep(PAUSE);
                } catch (InterruptedException ex) {
                    ;
                } catch (InvocationTargetException ex) {
                    ;
                }
            }
        });
        
        job.start();
        try {
            job.join();
        } catch (InterruptedException ex) {
            ;
        }
        showUIComponent(false);
    }

    /**
     * Indicate the cancelation of the task. Calling this method causes the
     * associated presentation component to be made not visible.
     */
    public void cancel() {
        avoidEventDispatchThreadExecution();
        
        synchronized (this) {
            progress.finish();
            finished = true;
            canceled = true;
        }
        
        Thread job = new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            target.firePropertyChange(
                                    ProgressDialogFactory.CANCEL_OPTION, 0, 1);
                        }
                    });
                    Thread.sleep(PAUSE);
                } catch (InterruptedException ex) {
                    ;
                } catch (InvocationTargetException ex) {
                    ;
                }
            }
        });
        
        job.start();
        try {
            job.join();
        } catch (InterruptedException ex) {
            ;
        }
        showUIComponent(false);
    }
    
    /**
     * Indicates if {@link #cancel} has been called on this object.
     */
    public synchronized boolean isCanceled() {
        return canceled;
    }
    
    /**
     * Indicates if {@link #finish} has been called on this object.
     */
    public synchronized boolean isFinished() {
        return finished;
    }

    /**
     * Indicate progress made in the task.
     *
     * @param workunit Cumulative number of workunits completed so far
     */
    public void progress(int workunit) {
        progress.progress(workunit);
    }
    
    /**
     * Disables the ability to cancel progress tracking.  Use this to indicate
     * to the controller object that all further progress workunits are
     * irrevocable.
     */
    public void lockout() {
        avoidEventDispatchThreadExecution();
        
        Thread job = new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            target.firePropertyChange(
                                    ProgressDialogFactory.CANCEL_LOCKOUT_OPTION, 0, 1);
                        }
                    });
                } catch (InterruptedException ex) {
                    ;
                } catch (InvocationTargetException ex) {
                    ;
                }
            }
        });
        
        job.start();
        try {
            job.join();
        } catch (InterruptedException ex) {
            ;
        }
    }
    
    /**
     * Indicate progress made in the task.
     *
     * @param message  Status of the task
     * @param workunit Cumulative number of workunits completed so far
     */
    public void progress(String message, int workunit) {
        avoidEventDispatchThreadExecution();
        progress.progress(message, workunit);
        updateProgressMessage(message);
    }
    
    /**
     * Indicate progress made in the task.  This method does not cause the
     * visual progress indicator to advance.
     *
     * @param message Status of the task
     */
    public void progress(String message) {
        avoidEventDispatchThreadExecution();
        progress.progress(message);
        updateProgressMessage(message);
    }
    
    /**
     * Start the progress indication for an indeterminate task. Calling this
     * method causes the associated presentation component to be made visible.
     *
     * @throws IllegalStateException if the progress this controller is
     *         tracking has already been started, finished, or canceled.
     */
    public synchronized void start() {
        avoidEventDispatchThreadExecution();
        if (used) {
            throw new IllegalStateException("Task already started");
        }
        showUIComponent(true);
        progress.start();
        used = true;
    }
    
    /**
     * Start the progress indication for a task with a known number of steps.
     * Calling this method causes the associated presentation component to be
     * made visible.
     *
     * @throws IllegalStateException if the progress this controller is
     *         tracking has already been started, finished, or canceled.
     */
    public synchronized void start(int workunits) {
        avoidEventDispatchThreadExecution();
        if (used) {
            throw new IllegalStateException("Task already started");
        }
        showUIComponent(true);
        used = true;
        progress.start(workunits);
    }
    
    /**
     * Discard the tracker. Its associated presentation component is made
     * not visible.
     *
     * @throws IllegalStateException if the progress this controller is
     *         tracking has not yet been finished or canceled.
     */
    public synchronized void dispose() {
        avoidEventDispatchThreadExecution();
        if (!finished) {
            throw new IllegalStateException("Task not yet finished/canceled");
        }
        showUIComponent(false);
    }
    
    synchronized void setCanceled() {
        finished = true;
        canceled = true;
    }

    private void showUIComponent(final boolean show) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    target.setVisible(show);
                }
            });
        } catch (InterruptedException ex) {
            ;
        } catch (InvocationTargetException ex) {
            ;
        }
    }
    
    private void updateProgressMessage(String message) {
        final PropertyChangeListener[] listeners =
                target.getPropertyChangeListeners();
        
        final PropertyChangeEvent evt = new PropertyChangeEvent(
                this,
                ProgressDialogFactory.UPDATE_OPTION,
                "",
                message
        );
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (int i = 0; i < listeners.length; ++i) {
                    listeners[i].propertyChange(evt);
                }
            }
        });
    }
    
    private void avoidEventDispatchThreadExecution() throws RuntimeException {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new RuntimeException("AWT Event Dispatch Thread used!");
        }
    }
    
    private static final long PAUSE = 1000L;
    private final ProgressHandle progress;
    private final Component target;
    private boolean used;
    private boolean canceled;
    private boolean finished;
}
