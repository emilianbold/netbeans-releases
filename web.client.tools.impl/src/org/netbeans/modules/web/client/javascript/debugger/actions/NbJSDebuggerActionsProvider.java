/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.client.javascript.debugger.NbJSDebuggerEngineProvider;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * This is the ActionsProvider for NetBeans JavaScript debugger.
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public class NbJSDebuggerActionsProvider extends ActionsProviderSupport 
                                         implements PropertyChangeListener{
    static final String JAVASCRIPT_MIMETYPE = "text/javascript";
    static final String HTML_MIMETYPE = "text/html";    

    // Supported Actions
    private static final Set<Object> ACTIONS =
            Collections.unmodifiableSet(
                new HashSet<Object>(
                    Arrays.asList(new Object[] {
                        ActionsManager.ACTION_START,
                        ActionsManager.ACTION_KILL,
                        ActionsManager.ACTION_CONTINUE,
                        ActionsManager.ACTION_PAUSE,
                        ActionsManager.ACTION_STEP_INTO,
                        ActionsManager.ACTION_STEP_OVER,
                        ActionsManager.ACTION_STEP_OUT,
                        ActionsManager.ACTION_RUN_TO_CURSOR,
                    })));

    private NbJSContextProviderWrapper contextProviderWrapper;

    private NbJSDebugger debugger;

    private class JSDebuggerEventListenerImpl implements JSDebuggerEventListener {
        public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
            JSDebuggerState debuggerState = debuggerEvent.getDebuggerState();
            switch (debuggerState.getState()) {
            case STARTING:
                setEnabled(ActionsManager.ACTION_START, false);
                setEnabled(ActionsManager.ACTION_KILL, true);               
                setEnabled(ActionsManager.ACTION_CONTINUE, false);
                setEnabled(ActionsManager.ACTION_PAUSE, false);
                setEnabled(ActionsManager.ACTION_STEP_INTO, false);
                setEnabled(ActionsManager.ACTION_STEP_OVER, false);                
                setEnabled(ActionsManager.ACTION_STEP_OUT, false);
                break;
            case RUNNING:
                setEnabled(ActionsManager.ACTION_START, false);
                setEnabled(ActionsManager.ACTION_KILL, true);
                setEnabled(ActionsManager.ACTION_CONTINUE, false);
                setEnabled(ActionsManager.ACTION_PAUSE, true);
                setEnabled(ActionsManager.ACTION_STEP_INTO, false);
                setEnabled(ActionsManager.ACTION_STEP_OVER, false);
                setEnabled(ActionsManager.ACTION_STEP_OUT, false);
                break;
            case SUSPENDED:
                setEnabled(ActionsManager.ACTION_START, false);
                setEnabled(ActionsManager.ACTION_KILL, true);
                setEnabled(ActionsManager.ACTION_CONTINUE, true);
                setEnabled(ActionsManager.ACTION_PAUSE, false);
                setEnabled(ActionsManager.ACTION_STEP_INTO, true);
                setEnabled(ActionsManager.ACTION_STEP_OVER, true);
                setEnabled(ActionsManager.ACTION_STEP_OUT, true);
                break;
            case DISCONNECTED:
                if (debuggerState.getReason() == JSDebuggerState.Reason.NONE) {
                    contextProviderWrapper.getNbJSDebuggerEngineProvider().getDestructor().killEngine();
                    return;
                }
                setEnabled(ActionsManager.ACTION_START, false);
                setEnabled(ActionsManager.ACTION_KILL, false);
                setEnabled(ActionsManager.ACTION_CONTINUE, false);
                setEnabled(ActionsManager.ACTION_PAUSE, false);
                setEnabled(ActionsManager.ACTION_STEP_INTO, false);
                setEnabled(ActionsManager.ACTION_STEP_OVER, false);
                setEnabled(ActionsManager.ACTION_STEP_OUT, false);
                break;
            }
            handleRunToCursor();
        }
    }

    private JSDebuggerEventListener debuggerListener;

    public NbJSDebuggerActionsProvider(ContextProvider contextProvider) {
        contextProviderWrapper = NbJSContextProviderWrapper.getContextProviderWrapper(contextProvider);
        debugger = contextProviderWrapper.getNbJSDebugger();

        // Initially enabled actions - ACTION_START
        setEnabled(ActionsManager.ACTION_START, true);
        setEnabled(ActionsManager.ACTION_KILL, true);

        // Add listener to JSDebugger
        debuggerListener = new JSDebuggerEventListenerImpl();
        debugger.addJSDebuggerEventListener(WeakListeners.create(
                JSDebuggerEventListener.class,
                debuggerListener,
                debugger));
        EditorContextDispatcher ctxtDispatcher = EditorContextDispatcher.getDefault();
        ctxtDispatcher.addPropertyChangeListener(WeakListeners.propertyChange(this, ctxtDispatcher));
    }

    @Override
    public Set<Object> getActions() {
        return ACTIONS;
    }

    @Override
    public void doAction(final Object action) {
        if (action == ActionsManager.ACTION_START) {
            debugger.startJSDebugging();
        } else if (action == ActionsManager.ACTION_KILL) {
            NbJSDebuggerEngineProvider provider = contextProviderWrapper.getNbJSDebuggerEngineProvider();
            assert provider != null;
            debugger.finish(true, provider.getDestructor());
        } else if (action == ActionsManager.ACTION_CONTINUE) {
            debugger.resume();
        } else if (action == ActionsManager.ACTION_PAUSE) {
            debugger.pause();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(NbJSDebuggerActionsProvider.class, "MSG_WILL_PAUSE"));
            setEnabled(ActionsManager.ACTION_PAUSE, false);
        } else if (action == ActionsManager.ACTION_STEP_OVER) {
            debugger.stepOver();
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            debugger.stepInto();
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            debugger.stepOut();
        } else if (action == ActionsManager.ACTION_RUN_TO_CURSOR) {
            debugger.runToCursor();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        handleRunToCursor();
    }

    private void handleRunToCursor() {
        JEditorPane editorPane = EditorContextDispatcher.getDefault().getCurrentEditor();
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            editorPane != null &&
            getActionsManager().isEnabled(ActionsManager.ACTION_CONTINUE) &&
            EditorContextDispatcher.getDefault().getCurrentLineNumber () >= 0 && 
            ( editorPane.getContentType().equals(JAVASCRIPT_MIMETYPE) || 
              editorPane.getContentType().equals(HTML_MIMETYPE) )
        );
    }
    
    static ActionsManager getActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }
}
