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

package org.netbeans.modules.worklist.editor.nodes.actions;

import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.dataloader.WorklistSourceMultiViewDescription;
import org.netbeans.modules.worklist.editor.nodes.WLMNode;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author anjeleevich
 */
public class GoToSourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        
        SwingUtilities.invokeLater(new GoToSourceRunnable((WLMNode) nodes[0]));
    }

    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            return false;
        }

        if (nodes.length == 1) {
            return (nodes[0] instanceof WLMNode);
        }

        if (nodes.length == 2 && nodes[0] == nodes[1]) {
            return (nodes[0] instanceof WLMNode);
        }

        Set<Node> nodeSet = new HashSet<Node>();
        for (int i = 0; i < nodes.length; i++) {
            nodeSet.add(nodes[i]);
        }

        return (nodeSet.size() == 1)
                && (nodeSet.iterator().next() instanceof WLMNode);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(),
                "GO_TO_SOURCE_ACTION_NAME"); // NOI18N;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private static class GoToSourceRunnable implements Runnable {
        private WLMNode node;

        GoToSourceRunnable(WLMNode node) {
            this.node = node;
        }

        public void run() {
            WLMComponent wlmComponent = node.getGoToSourceWLMComponent();
            WLMModel model = wlmComponent.getModel();
            ModelSource modelSource = model.getModelSource();

            DocumentComponent documentComponent = (DocumentComponent) wlmComponent;
            StyledDocument document = modelSource.getLookup()
                    .lookup(StyledDocument.class);

            int documentComponentPosition = documentComponent.findPosition();

            int lineNumber = -1;
            int lineColumn = -1;

            try {
                lineNumber = NbDocument.findLineNumber(document,
                    documentComponentPosition);
                lineColumn = NbDocument.findLineColumn(document,
                    documentComponentPosition);
            } catch (Exception ex) {
                // do nothing
                // will use default values
            }

            EditCookie editCookie = node.getLookup().lookup(EditCookie.class);
            LineCookie lineCookie = node.getLookup().lookup(LineCookie.class);

            if (editCookie != null) {
                editCookie.edit();
            }

            TopComponent topComponent = WindowManager.getDefault().getRegistry()
                    .getActivated();

            MultiViewHandler multiViewHandler = (topComponent == null) ? null
                    : MultiViews.findMultiViewHandler(topComponent);

            MultiViewPerspective[] perspectives = (multiViewHandler == null)
                    ? null : multiViewHandler.getPerspectives();

            if (perspectives != null) {
                for (int i = 0; i < perspectives.length; i++) {
                    if (WorklistSourceMultiViewDescription.PREFERRED_ID
                            .equals(perspectives[i].preferredID()))
                    {
                        multiViewHandler.requestVisible(perspectives[i]);
                        multiViewHandler.requestActive(perspectives[i]);

                        if (lineCookie != null && lineNumber >= 0) {
                            try {
                                Line.Set lineSet = lineCookie.getLineSet();
                                Line line = lineSet.getCurrent(lineNumber);
                                line.show(Line.ShowOpenType.OPEN,
                                       Line.ShowVisibilityType.FOCUS,
                                       lineColumn);
                            } catch (Exception ex) {
                                // do nothing
                            }
                        }

                        break;
                    }
                }
            }
        }
    }
}
