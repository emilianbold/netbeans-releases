/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.j2ee.core.api.support.progress;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.utilities.ProgressPanel;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Andrei Badea
 */
public class ProgressSupportTest extends NbTestCase {

    // this class uses Mutex.EVENT.readAccess(Mutex.Action) in several places
    // instead of the more obvious SwingUtilities.invokeAndWait()
    // just because the former method has simpler exception handling

    public ProgressSupportTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testInvoke() {
        final ProgressSupport progressSupport = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        // incremented each time an action is invoked
        final int[] invokeCount = new int[1];
        // incremented each time an action is invoked in the correct thread
        // (EDT for sync actions, !EDT for async actions)
        final int[] correctThreadCount = new int[1];
        // incremented each time the progress panel state is correct during an action invocation
        // (closed for sync actions, open for async actions)
        final int[] correctPanelStateCount = new int[1];

        actions.add(new ProgressSupport.SynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount[0] += isEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isClosed(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
            }
        });

        actions.add(new ProgressSupport.SynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount[0] += isEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isClosed(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
            }
        });

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.getProgress().progress("Testing an asynchronous action.");

                correctThreadCount[0] += isNotEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isOpen(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.getProgress().progress("Testing the second asynchronous action.");

                correctThreadCount[0] += isNotEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isOpen(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        actions.add(new ProgressSupport.SynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                correctThreadCount[0] += isEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isClosed(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
            }
        });

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.getProgress().progress("Testing the third asynchronous action.");

                correctThreadCount[0] += isNotEventDispatchThread();
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        correctPanelStateCount[0] += isOpen(actionContext.getProgress().getPanel());
                        return null;
                    }
                });
                invokeCount[0]++;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        });

        progressSupport.invoke(actions);
        assertEquals(6, invokeCount[0]);
        assertEquals(6, correctThreadCount[0]);
        assertEquals(6, correctPanelStateCount[0]);

        progressSupport.invoke(actions);
        assertEquals(12, invokeCount[0]);
        assertEquals(12, correctThreadCount[0]);
        assertEquals(12, correctPanelStateCount[0]);
    }

    private int isEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread() ? 1 : 0;
    }

    private int isNotEventDispatchThread() {
        return SwingUtilities.isEventDispatchThread() ? 0 : 1;
    }

    private int isOpen(ProgressPanel panel) {
        return panel.isOpen() ? 1 : 0;
    }

    private int isClosed(ProgressPanel panel) {
        return panel.isOpen() ? 0 : 1;
    }

    public void testProgressMessage() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final String[] progressMessage = new String[1];

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.getProgress().progress("Progress message");

                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getProgress().getPanel();
                        progressMessage[0] = progressPanel.getText();
                        return null;
                    }
                });
            }
        });

        erp.invoke(actions);
        assertEquals("Progress message", progressMessage[0]);
    }

    public void testDisabledActionDoesNotCauseAnInfiniteLoop() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final Object sync = new Object();
        final boolean[] ran = new boolean[1];

        actions.add(new ProgressSupport.SynchronousAction() {
            public void run(ProgressSupport.Context actionContext) {
                synchronized (sync) {
                    ran[0] = true;
                    sync.notifyAll();
                }
            }

            public boolean isEnabled() {
                return false;
            }
        });

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();
                synchronized (sync) {
                    while (!ran[0]) {
                        if (System.currentTimeMillis() - startTime >= 5 * 1000) {
                            // hmm, anything better?
                            System.exit(1);
                        }
                        try {
                            sync.wait(500);
                        } catch (InterruptedException e) { }
                    }
                }
            }
        });

        erp.invoke(actions);
    }

    public void testExceptionInSyncActionPropagates() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        actions.add(new ProgressSupport.SynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                throw new RuntimeException("Error");
            }
        });

        try {
            erp.invoke(actions);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
        }

        assertNull(erp.actionInvoker);
    }

    public void testExceptionInAsyncActionPropagatesAndProgressPanelCloses() {
        final ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final ProgressPanel progressPanel[] = new ProgressPanel[1];
        final Boolean panelOpen[] = new Boolean[1];

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.getProgress().progress("Asynchronous");

                // get the panel
                progressPanel[0] = actionContext.getProgress().getPanel();

                // and simulate an error
                throw new AssertionError("Error");
            }
        });

        try {
            erp.invoke(actions);
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof AssertionError);
        }

        assertFalse(progressPanel[0].isOpen());

        assertNull(erp.actionInvoker);
    }

    public void testNoCancelButtonWhenNonCancellableInvocation() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] cancelVisible = new boolean[1];

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getProgress().getPanel();
                        cancelVisible[0] = progressPanel.isCancelVisible();
                        return null;
                    }
                });
            }
        });

        erp.invoke(actions);
        assertFalse(cancelVisible[0]);
    }

    public void testCancelButtonWhenCancellableInvocation() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] cancelVisible = new boolean[2];
        final boolean[] cancelEnabled = new boolean[2];

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getProgress().getPanel();
                        cancelVisible[0] = progressPanel.isCancelVisible();
                        cancelEnabled[0] = progressPanel.isCancelEnabled();
                        return null;
                    }
                });
            }
        });

        actions.add(new ProgressSupport.CancellableAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        ProgressPanel progressPanel = actionContext.getProgress().getPanel();
                        cancelVisible[1] = progressPanel.isCancelVisible();
                        cancelEnabled[1] = progressPanel.isCancelEnabled();
                        return null;
                    }
                });
            }

            public boolean isEnabled() {
                return true;
            }

            public boolean getRunInEventThread() {
                return false;
            }

            public boolean cancel() {
                return true;
            }
        });

        // the actions are cancellable, thus the cancel button is always visible
        // invoke() should return true, meaning all actions were invoked
        assertTrue(erp.invoke(actions, true));

        // the first action was not cancellable
        assertTrue(cancelVisible[0]);
        assertFalse(cancelEnabled[0]);

        // the second action was cancellable
        assertTrue(cancelVisible[1]);
        assertTrue(cancelEnabled[1]);
    }

    public void testCancelWorks() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] secondActionInvoked = new boolean[1];
        final boolean[] cancelInvoked = new boolean[1];
        final boolean[] cancelInvokedInEDT = new boolean[1];

        actions.add(new ProgressSupport.CancellableAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        actionContext.getProgress().getPanel().getCancelButton().doClick();
                        return null;
                    }
                });
                // at this point cancel() should already have been called
            }

            public boolean isEnabled() {
                return true;
            }

            public boolean getRunInEventThread() {
                return false;
            }

            public boolean cancel() {
                cancelInvokedInEDT[0] = SwingUtilities.isEventDispatchThread();
                cancelInvoked[0] = true;
                return true;
            }
        });

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(ProgressSupport.Context actionContext) {
                secondActionInvoked[0] = true;
            }
        });

        assertFalse(erp.invoke(actions, true));
        assertFalse(secondActionInvoked[0]);
        assertTrue(cancelInvokedInEDT[0]);
        assertTrue(cancelInvoked[0]);
    }

    public void testNoCancelWhenTheCancelMethodReturnsFalse() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] secondActionInvoked = new boolean[1];

        actions.add(new ProgressSupport.CancellableAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        actionContext.getProgress().getPanel().getCancelButton().doClick();
                        return null;
                    }
                });
                // at this point cancel() should already have been called
            }

            public boolean isEnabled() {
                return true;
            }

            public boolean getRunInEventThread() {
                return false;
            }

            public boolean cancel() {
                return false;
            }
        });

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(ProgressSupport.Context actionContext) {
                secondActionInvoked[0] = true;
            }
        });

        assertTrue(erp.invoke(actions, true));
        assertTrue(secondActionInvoked[0]);
    }

    public void testEscapeDoesNotCloseDialogForAsyncNonCancellableActions() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] panelOpen = new boolean[1];

        actions.add(new ProgressSupport.AsynchronousAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        // fake an escape key press
                        JRootPane rootPane = actionContext.getProgress().getPanel().getRootPane();
                        KeyEvent event = new KeyEvent(rootPane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                        rootPane.dispatchEvent(event);

                        panelOpen[0] = actionContext.getProgress().getPanel().isOpen();
                        return null;
                    }
                });
            }
        });

        erp.invoke(actions);
        assertTrue(panelOpen[0]);
    }

    public void testEscapeCancelsCancellableActions() {
        ProgressSupport erp = new ProgressSupport();
        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        final boolean[] panelOpen = new boolean[1];
        final boolean[] cancelEnabled = new boolean[1];

        actions.add(new ProgressSupport.CancellableAction() {
            public void run(final ProgressSupport.Context actionContext) {
                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        // fake an escape key press
                        JRootPane rootPane = actionContext.getProgress().getPanel().getRootPane();
                        KeyEvent event = new KeyEvent(rootPane, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                        rootPane.dispatchEvent(event);

                        panelOpen[0] = actionContext.getProgress().getPanel().isOpen();
                        cancelEnabled[0] = actionContext.getProgress().getPanel().getCancelButton().isEnabled();
                        return null;
                    }
                });
            }

            public boolean isEnabled() {
                return true;
            }

            public boolean getRunInEventThread() {
                return false;
            }

            public boolean cancel() {
                return true;
            }
        });

        erp.invoke(actions, true);
        assertTrue(panelOpen[0]);
        assertFalse(cancelEnabled[0]);
    }
}
