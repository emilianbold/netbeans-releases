/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.SourcePath;
import org.netbeans.modules.debugger.jpda.ui.debugging.FiltersDescriptor;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;


/**
 * @author   Martin Entlicher
 */
public class DebuggingActionsProvider implements NodeActionsProvider {

    private JPDADebugger debugger;
    private Session session;
    private RequestProcessor requestProcessor;
    private Action POP_TO_HERE_ACTION;
    private Action MAKE_CURRENT_ACTION;
    private Action SUSPEND_ACTION;
    private Action RESUME_ACTION;
    private Action INTERRUPT_ACTION;
    private Action COPY_TO_CLBD_ACTION;
    private Action LANGUAGE_SELECTION;


    public DebuggingActionsProvider (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        SUSPEND_ACTION = createSUSPEND_ACTION(requestProcessor);
        RESUME_ACTION = createRESUME_ACTION(requestProcessor);
        INTERRUPT_ACTION = createINTERRUPT_ACTION(requestProcessor);
        COPY_TO_CLBD_ACTION = createCOPY_TO_CLBD_ACTION(requestProcessor);
        POP_TO_HERE_ACTION = createPOP_TO_HERE_ACTION(requestProcessor);
        LANGUAGE_SELECTION = new LanguageSelection(session);
    }
    

    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_MakeCurrent_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDAThread) {
                    return debugger.getCurrentThread () != node;
                }
                if (node instanceof CallStackFrame) {
                    CallStackFrame f = (CallStackFrame) node;
                    return !DebuggingTreeModel.isMethodInvoking(f.getThread()) &&//f.getThread() == debugger.getCurrentThread() &&
                           !f.equals(debugger.getCurrentCallStackFrame());
                }
                return false;
            }
            
            public void run (Object[] nodes) {
                if (nodes.length == 0) return ;
                if (nodes[0] instanceof JPDAThread) {
                    ((JPDAThread) nodes [0]).makeCurrent ();
                    goToSource((JPDAThread) nodes [0]);
                }
                if (nodes[0] instanceof CallStackFrame) {
                    CallStackFrame f = (CallStackFrame) nodes[0];
                    JPDAThread thread = f.getThread();
                    if (debugger.getCurrentThread() != thread) {
                        thread.makeCurrent();
                    }
                    f.makeCurrent ();
                    goToSource(f);
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    }

    private Action createCOPY_TO_CLBD_ACTION(RequestProcessor requestProcessor) {
        return  Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_CallstackAction_Copy2CLBD_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof JPDAThread) {
                    return !DebuggingTreeModel.isMethodInvoking((JPDAThread) node);
                } else if (node instanceof CallStackFrame) {
                    return !DebuggingTreeModel.isMethodInvoking(((CallStackFrame) node).getThread());
                }
                return true;
            }
            public void run (Object[] nodes) {
                List<JPDAThread> threads = new ArrayList<JPDAThread>(nodes.length);
                for (Object node : nodes) {
                    if (node instanceof JPDAThread) {
                        threads.add((JPDAThread) node);
                    }
                    if (node instanceof CallStackFrame) {
                        JPDAThread t = ((CallStackFrame) node).getThread();
                        if (!threads.contains(t)) {
                            threads.add(t);
                        }
                    }
                }
                if (threads.isEmpty()) {
                    threads.add(debugger.getCurrentThread());
                }
                stackToCLBD (threads);
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    }

    static Action GO_TO_SOURCE_ACTION = Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_GoToSource_Label"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                if (!(node instanceof CallStackFrame)) {
                    return false;
                } else if (DebuggingTreeModel.isMethodInvoking(((CallStackFrame) node).getThread())) {
                    return false;
                }
                return isGoToSourceSupported ((CallStackFrame) node);
            }
            
            public void perform (Object[] nodes) {
                goToSource((CallStackFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );

    static final Action createPOP_TO_HERE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            NbBundle.getBundle(ThreadsActionsProvider.class).getString("CTL_CallstackAction_PopToHere_Label"),
            new Models.ActionPerformer () {
                public boolean isEnabled (Object node) {
                    // TODO: Check whether this frame is deeper then the top-most
                    if (node instanceof CallStackFrame) {
                        return !DebuggingTreeModel.isMethodInvoking(((CallStackFrame) node).getThread());
                    }
                    return true;
                }
                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        public void run() {
                            popToHere ((CallStackFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );
    }

    static abstract class LazyActionPerformer implements Models.ActionPerformer {

        private RequestProcessor rp;

        public LazyActionPerformer(RequestProcessor rp) {
            this.rp = rp;
        }

        public abstract boolean isEnabled (Object node);

        public final void perform (final Object[] nodes) {
            rp.post(new Runnable() {
                public void run() {
                    LazyActionPerformer.this.run(nodes);
                }
            });
        }

        public abstract void run(Object[] nodes);
    }

    private Action createSUSPEND_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Suspend_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                //if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).originalThread;
                if (node instanceof JPDAThread) {
                    return !((JPDAThread) node).isSuspended ();
                }
                if (node instanceof JPDAThreadGroup) {
                    return true;
                }
                return false;
            }

            public void run(Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread)
                        ((JPDAThread) node).suspend ();
                    else
                        ((JPDAThreadGroup) node).suspend ();
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    );
    }

    private Action createRESUME_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Resume_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                //if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).originalThread;
                if (node instanceof JPDAThread) {
                    return ((JPDAThread) node).isSuspended ();
                }
                if (node instanceof JPDAThreadGroup) {
                    return true;
                }
                return false;
            }
            
            public void run (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread)
                        ((JPDAThread) node).resume ();
                    else
                        ((JPDAThreadGroup) node).resume ();
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    
    );
    }
        
    private Action createINTERRUPT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        NbBundle.getBundle(DebuggingActionsProvider.class).getString("CTL_ThreadAction_Interrupt_Label"),
        new LazyActionPerformer (requestProcessor) {
            public boolean isEnabled (Object node) {
                if (node instanceof MonitorModel.ThreadWithBordel) node = ((MonitorModel.ThreadWithBordel) node).getOriginalThread();
                if (node instanceof JPDAThread)
                    return !((JPDAThread) node).isSuspended ();
                else
                    return false;
            }
            
            public void run (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++) {
                    Object node = (nodes[i] instanceof MonitorModel.ThreadWithBordel) ?
                            ((MonitorModel.ThreadWithBordel) nodes[i]).getOriginalThread() : nodes[i];
                    if (node instanceof JPDAThread) {
                        ((JPDAThread) node).interrupt();
                    }
                }
            }
        },
        Models.MULTISELECTION_TYPE_ALL
    
    );
    }
        
    private static class LanguageSelection extends AbstractAction implements Presenter.Popup {

        private Session session;

        public LanguageSelection(Session session) {
            this.session = session;
        }

        public void actionPerformed(ActionEvent e) {
        }

        public JMenuItem getPopupPresenter() {
            JMenu displayAsPopup = new JMenu(NbBundle.getMessage(DebuggingActionsProvider.class, "CTL_Session_Popup_Language"));

            String [] languages = session.getSupportedLanguages();
            String currentLanguage = session.getCurrentLanguage();
            for (int i = 0; i < languages.length; i++) {
                final String language = languages[i];
                JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(new AbstractAction(language) {
                    public void actionPerformed(ActionEvent e) {
                        session.setCurrentLanguage(language);
                    }
                });
                if (currentLanguage.equals(language)) langItem.setSelected(true);
                displayAsPopup.add(langItem);
            }
            return displayAsPopup;
        }
    }


        
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            Action[] sa = getSessionActions();
            Action[] fa = FiltersDescriptor.getInstance().getFilterActions();
            Action[] a = new Action[sa.length + 1 + fa.length];
            System.arraycopy(sa, 0, a, 0, sa.length);
            a[sa.length] = null;
            System.arraycopy(fa, 0, a, sa.length + 1, fa.length);
            return a;
        }
        if (node instanceof JPDAThreadGroup) {
            return new Action [] {
                RESUME_ACTION,
                SUSPEND_ACTION,
            };
        } else
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            boolean suspended = t.isSuspended ();
            Action a = null;
            if (suspended)
                a = RESUME_ACTION;
            else
                a = SUSPEND_ACTION;
            return new Action [] {
                MAKE_CURRENT_ACTION,
                a,
                INTERRUPT_ACTION // ,
                //GO_TO_SOURCE_ACTION,
            };
        } else
        if (node instanceof CallStackFrame) {
            boolean popToHere = debugger.canPopFrames ();
            if (popToHere) {
                return new Action [] {
                    MAKE_CURRENT_ACTION,
                    POP_TO_HERE_ACTION,
                    GO_TO_SOURCE_ACTION,
                    COPY_TO_CLBD_ACTION,
                };
            } else {
                return new Action [] {
                    MAKE_CURRENT_ACTION,
                    GO_TO_SOURCE_ACTION,
                    COPY_TO_CLBD_ACTION,
                };
            }
        } else
        throw new UnknownTypeException (node);
    }

    private Action[] getSessionActions() {
        return new Action[] { LANGUAGE_SELECTION };
    }
    
    public void performDefaultAction (final Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return;
        }
        if (node instanceof JPDAThread || node instanceof CallStackFrame) {
            requestProcessor.post(new Runnable() {
                public void run() {
                    if (node instanceof JPDAThread) {
                        ((JPDAThread) node).makeCurrent ();
                    } else if (node instanceof CallStackFrame) {
                        CallStackFrame f = (CallStackFrame) node;
                        JPDAThread thread = f.getThread();
                        if (debugger.getCurrentThread() != thread) {
                            thread.makeCurrent();
                        }
                        f.makeCurrent();
                        goToSource(f);
                    }
                }
            });
            return ;
        } else if (node instanceof JPDAThreadGroup) {
            return;
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }

    private static void popToHere (final CallStackFrame frame) {
        try {
            JPDAThread t = frame.getThread ();
            CallStackFrame[] stack = t.getCallStack ();
            int i, k = stack.length;
            if (k < 2) return ;
            for (i = 0; i < k; i++)
                if (stack [i].equals (frame)) {
                    if (i > 0) {
                        stack [i - 1].popFrame ();
                    }
                    return;
                }
        } catch (AbsentInformationException ex) {
        }
    }

    static void appendStackInfo(StringBuffer frameStr, JPDAThread t) {
            CallStackFrame[] stack;
            try {
                stack = t.getCallStack ();
            } catch (AbsentInformationException ex) {
                frameStr.append(NbBundle.getMessage(CallStackActionsProvider.class, "MSG_NoSourceInfo"));
                stack = null;
            }
            if (stack != null) {
                int i, k = stack.length;

                for (i = 0; i < k; i++) {
                    frameStr.append(stack[i].getClassName());
                    frameStr.append(".");
                    frameStr.append(stack[i].getMethodName());
                    try {
                        String sourceName = stack[i].getSourceName(null);
                        frameStr.append("(");
                        frameStr.append(sourceName);
                        int line = stack[i].getLineNumber(null);
                        if (line > 0) {
                            frameStr.append(":");
                            frameStr.append(line);
                        }
                        frameStr.append(")");
                    } catch (AbsentInformationException ex) {
                        //frameStr.append(NbBundle.getMessage(CallStackActionsProvider.class, "MSG_NoSourceInfo"));
                        // Ignore, do not provide source name.
                    }
                    if (i != k - 1) frameStr.append('\n');
                }
            }
    }

    static void stackToCLBD(List<JPDAThread> threads) {
        StringBuffer frameStr = new StringBuffer(512);
        for (JPDAThread t : threads) {
            if (frameStr.length() > 0) {
                frameStr.append('\n');
            }
            frameStr.append("\"");
            frameStr.append(t.getName());
            frameStr.append("\"\n");
            appendStackInfo(frameStr, t);
        }
        Clipboard systemClipboard = getClipboard();
        Transferable transferableText =
                new StringSelection(frameStr.toString());
        systemClipboard.setContents(
                transferableText,
                null);
    }

    static Clipboard getClipboard() {
        Clipboard clipboard = org.openide.util.Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    private static boolean isGoToSourceSupported (CallStackFrame f) {
        String language = DebuggerManager.getDebuggerManager ().
            getCurrentSession ().getCurrentLanguage ();
        SourcePath sp = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, SourcePath.class);
        return sp.sourceAvailable (f, language);
    }
    
    private static void goToSource(final CallStackFrame frame) {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session == null) return ;
        String language = session.getCurrentLanguage ();
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine == null) return ;
        SourcePath sp = engine.lookupFirst(null, SourcePath.class);
        sp.showSource (frame, language);
    }
    
    private static void goToSource(final JPDAThread thread) {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session == null) return ;
        String language = session.getCurrentLanguage ();
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine == null) return ;
        SourcePath sp = engine.lookupFirst(null, SourcePath.class);
        sp.showSource (thread, language);
    }

}
