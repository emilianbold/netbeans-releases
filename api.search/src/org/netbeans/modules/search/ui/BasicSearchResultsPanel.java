/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search.ui;

import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.swing.outline.Outline;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author jhavlin
 */
public class BasicSearchResultsPanel extends AbstractSearchResultsPanel {

    private ResultModel resultModel;
    private ResultsNode resultsNode;

    public BasicSearchResultsPanel(ResultModel resultModel,
            BasicComposition composition) {

        super(composition, composition.getSearchProviderPresenter());
        this.resultsNode = new ResultsNode();
        getExplorerManager().setRootContext(resultsNode);
        this.resultModel = resultModel;

        if (resultModel.canHaveDetails()) {
            getOutlineView().addPropertyColumn(
                    "detailsCount", UiUtils.getText( //NOI18N
                    "BasicSearchResultsPanel.outline.detailsCount"));   //NOI18N
        }
        getOutlineView().addPropertyColumn("path", UiUtils.getText(
                "BasicSearchResultsPanel.outline.path"));               //NOI18N
        getOutlineView().addPropertyColumn("size", UiUtils.getText(
                "BasicSearchResultsPanel.outline.size"));               //NOI18N
        getOutlineView().addPropertyColumn("lastModified", UiUtils.getText(
                "BasicSearchResultsPanel.outline.lastModified"));       //NOI18N
        getOutlineView().getOutline().setAutoResizeMode(
                Outline.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        if (resultModel.canHaveDetails()) {
            sizeColumn(0, 80, 10000); // file name, matching lines
            sizeColumn(1, 20, 500); // details count
            sizeColumn(2, 40, 5000); // path
            sizeColumn(3, 20, 500); // size
            sizeColumn(4, 20, 500); // last modified
        } else {
            sizeColumn(0, 80, 2000); // file name, matching lines
            sizeColumn(1, 40, 5000); // path
            sizeColumn(2, 20, 500); // size
            sizeColumn(3, 20, 500); // last modified
        }
    }

    private void sizeColumn(int index, int min, int max) {
        Object id = getOutlineView().getOutline().getColumnModel().getColumn(
                index).getIdentifier();
        getOutlineView().getOutline().getColumn(id).setMinWidth(min);
        getOutlineView().getOutline().getColumn(id).setMaxWidth(max);
    }

    public void update() {
        resultsNode.update();
    }

    /**
     * Class for representation of the root node.
     */
    private class ResultsNode extends AbstractNode {

        private ResultChildren children;

        public ResultsNode() {
            super(new ResultChildren());
            this.children = (ResultChildren) this.getChildren();
        }

        void update() {
            setDisplayName(resultModel.size() + " matching objects found.");       //TODO
            children.update();
        }
    }

    /**
     * Children of the main results node.
     *
     * Shows list of matching data objects.
     */
    private class ResultChildren extends Children.Keys<MatchingObject> {

        @Override
        protected Node[] createNodes(MatchingObject key) {
            Node delegate;
            if (key.getDataObject() == null) {
                return new Node[0];
            }
            delegate = key.getDataObject().getNodeDelegate();
            Children children;
            if (key.getTextDetails() == null
                    || key.getTextDetails().isEmpty()) {
                children = Children.LEAF;
            } else {
                children = key.getDetailsChildren();
            }
            Node n = new MatchingObjectNode(delegate, children, key);
            return new Node[]{n};
        }

        private synchronized void update() {
            setKeys(resultModel.getMatchingObjects());
        }
    }
}
