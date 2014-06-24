/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=NodeActionsProviderFilter.class,
                             position=21000)
public class DebuggingTruffleActionsProvider implements NodeActionsProviderFilter {
    
    private final JPDADebugger debugger;
    
    private Action MAKE_CURRENT_ACTION;
    private Action GO_TO_SOURCE_ACTION;
    
    public DebuggingTruffleActionsProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RequestProcessor requestProcessor = lookupProvider.lookupFirst(null, RequestProcessor.class);
        MAKE_CURRENT_ACTION = createMAKE_CURRENT_ACTION(requestProcessor);
        GO_TO_SOURCE_ACTION = createGO_TO_SOURCE_ACTION(requestProcessor);
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                TruffleStackFrame f = (TruffleStackFrame) node;
                currentPCInfo.setSelectedStackFrame(f);
                goToSource(f);
            }
        } else {
            original.performDefaultAction(node);
        }
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                GO_TO_SOURCE_ACTION,
            };
        } else {
            return original.getActions(node);
        }
    }
    
    private static void goToSource(final TruffleStackFrame f) {
        final SourcePosition sourcePosition = f.getSourcePosition();
        SwingUtilities.invokeLater (new Runnable () {
            @Override
            public void run () {
                EditorContextBridge.getContext().showSource (
                    sourcePosition.getSource().getUrl().toExternalForm(),
                    sourcePosition.getLine(),
                    f.getDebugger()
                );
            }
        });
    }
    
    @NbBundle.Messages("CTL_StackFrameAction_MakeCurrent_Label=Make Current")
    private Action createMAKE_CURRENT_ACTION(RequestProcessor requestProcessor) {
        return Models.createAction (
        Bundle.CTL_StackFrameAction_MakeCurrent_Label(),
        new LazyActionPerformer (requestProcessor) {
            @Override
            public boolean isEnabled (Object node) {
                if (node instanceof TruffleStackFrame) {
                    TruffleStackFrame f = (TruffleStackFrame) node;
                    CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
                    if (currentPCInfo != null) {
                        TruffleStackFrame topFrame = currentPCInfo.getTopFrame();
                        if (topFrame == null) {
                            return f.getDepth() > 0;
                        } else {
                            return f != topFrame;
                        }
                    }
                }
                return false;
            }
            
            @Override
            public void run (Object[] nodes) {
                if (nodes.length == 0) return ;
                if (nodes[0] instanceof TruffleStackFrame) {
                    TruffleStackFrame f = (TruffleStackFrame) nodes[0];
                    CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
                    if (currentPCInfo != null) {
                        currentPCInfo.setSelectedStackFrame(f);
                    }
                    goToSource(f);
                }
            }

        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    
    );
    }
    
    @NbBundle.Messages("CTL_StackFrameAction_GoToSource_Label=Go To Source")
    static final Action createGO_TO_SOURCE_ACTION(final RequestProcessor requestProcessor) {
        return Models.createAction (
            Bundle.CTL_StackFrameAction_GoToSource_Label(),
            new Models.ActionPerformer () {
                @Override
                public boolean isEnabled (Object node) {
                    if (!(node instanceof TruffleStackFrame)) {
                        return false;
                    }
                    //return isGoToSourceSupported ((TruffleStackFrame) node);
                    return true;
                }

                @Override
                public void perform (final Object[] nodes) {
                    // Do not do expensive actions in AWT,
                    // It can also block if it can not procceed for some reason
                    requestProcessor.post(new Runnable() {
                        @Override
                        public void run() {
                            goToSource((TruffleStackFrame) nodes [0]);
                        }
                    });
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE

        );
    }

    static abstract class LazyActionPerformer implements Models.ActionPerformer {

        private final RequestProcessor rp;

        public LazyActionPerformer(RequestProcessor rp) {
            this.rp = rp;
        }

        @Override
        public abstract boolean isEnabled (Object node);

        @Override
        public final void perform (final Object[] nodes) {
            rp.post(new Runnable() {
                @Override
                public void run() {
                    LazyActionPerformer.this.run(nodes);
                }
            });
        }

        public abstract void run(Object[] nodes);
    }

}
