/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.truffle.vars.models;

import java.net.URL;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ResultsView", types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ToolTipView", types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/WatchesView", types=NodeActionsProviderFilter.class),
})
public class TruffleVariablesActionsProviderFilter implements NodeActionsProviderFilter {
    
    public TruffleVariablesActionsProviderFilter(ContextProvider contextProvider) {
        System.err.println("new TruffleVariablesActionsProviderFilter()");
    }

    @NbBundle.Messages("CTL_GoToSource=Go to source")
    private final Action GO_TO_VALUE_SOURCE_ACTION = Models.createAction (
        Bundle.CTL_GoToSource(),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (final Object[] nodes) {
                TruffleVariable var = (TruffleVariable) nodes[0];
                showSource(var.getValueSource());
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    @NbBundle.Messages("CTL_GoToTypeSource=Go to type")
    private final Action GO_TO_TYPE_SOURCE_ACTION = Models.createAction (
        Bundle.CTL_GoToTypeSource(),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                return true;
            }
            @Override
            public void perform (final Object[] nodes) {
                TruffleVariable var = (TruffleVariable) nodes[0];
                showSource(var.getTypeSource());
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        boolean shown = false;
        if (node instanceof TruffleVariable) {
            TruffleVariable var = (TruffleVariable) node;
            SourcePosition source = var.getValueSource();
            if (source == null) {
                source = var.getTypeSource();
            }
            if (source != null) {
                showSource(source);
                shown = true;
            }
        }
        if (!shown) {
            original.performDefaultAction(node);
        }
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        UnknownTypeException originalUTEx = null;
        Action [] actions;
        try {
            actions = original.getActions (node);
        } catch (UnknownTypeException utex) {
            originalUTEx = utex;
            actions = new Action[0];
        }
        if (node instanceof TruffleVariable) {
            TruffleVariable var = (TruffleVariable) node;
            SourcePosition valueSource = var.getValueSource();
            SourcePosition typeSource = var.getTypeSource();
            if (valueSource != null || typeSource != null) {
                int l = actions.length;
                if (valueSource != null) {
                    l++;
                }
                if (typeSource != null) {
                    l++;
                }
                Action[] newActions = new Action[l];
                System.arraycopy(actions, 0, newActions, 0, actions.length);
                l = actions.length;
                if (valueSource != null) {
                    newActions[l++] = GO_TO_VALUE_SOURCE_ACTION;
                }
                if (typeSource != null) {
                    newActions[l++] = GO_TO_TYPE_SOURCE_ACTION;
                }
                actions = newActions;
            }
        } else if (originalUTEx != null) {
            throw originalUTEx;
        }
        return actions;
    }

    @NbBundle.Messages({"# {0} - The file path", "MSG_NoSourceFile=Cannot find source file {0}."})
    private void showSource(SourcePosition source) {
        URL url = source.getSource().getUrl();
        int lineNumber = source.getLine();
        SwingUtilities.invokeLater (() -> {
            boolean success = EditorContextBridge.getContext().showSource(url.toExternalForm(), lineNumber, null);
            if (!success) {
                NotifyDescriptor d = new NotifyDescriptor.Message(Bundle.MSG_NoSourceFile(url.toExternalForm()), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(d);
            }
        });
    }
}
