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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.TextDetail;
import org.netbeans.swing.outline.Outline;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jhavlin
 */
public class BasicSearchResultsPanel extends AbstractSearchResultsPanel {

    private static final String NEXT_ICON =
            "org/netbeans/modules/search/res/next.png";                 //NOI18N
    private static final String PREV_ICON =
            "org/netbeans/modules/search/res/prev.png";                 //NOI18N
    private static final String EXPAND_ICON =
            "org/netbeans/modules/search/res/expandTree.png";           //NOI18N
    private static final String COLLAPSE_ICON =
            "org/netbeans/modules/search/res/colapseTree.png";          //NOI18N
    private ResultModel resultModel;
    private ResultsNode resultsNode;
    private JButton nextButton;
    private JButton prevButton;
    private JToggleButton expandButton;
    private boolean replacing;
    private boolean details;

    public BasicSearchResultsPanel(ResultModel resultModel,
            BasicComposition composition, boolean replacing, boolean details) {

        super(composition, composition.getSearchProviderPresenter());
        this.replacing = replacing;
        this.details = details;
        this.resultsNode = new ResultsNode();
        getExplorerManager().setRootContext(resultsNode);
        this.resultModel = resultModel;
        setOutlineColumns(resultModel);
        initSelectionListeners();
    }

    private void initSelectionListeners() {
        getExplorerManager().addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(
                                "selectedNodes")) {                     //NOI18N
                            updateShiftButtons();
                        }
                    }
                });
    }

    private void setOutlineColumns(ResultModel resultModel) {
        if (details) {
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
        if (details) {
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
        if (details && !expandButton.isEnabled()) {
            expandButton.setEnabled(resultModel.size() > 0);
        }
        updateShiftButtons();
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

    private void updateShiftButtons() {
        if (details) {
            prevButton.setEnabled(findShifNode(-1) != null);
            nextButton.setEnabled(findShifNode(1) != null);
        }
    }

    @Override
    protected AbstractButton[] createButtons() {
        if (!details) {
            return new AbstractButton[] {};
        }
        prevButton = new JButton();
        prevButton.setEnabled(false);
        prevButton.setIcon(ImageUtilities.loadImageIcon(PREV_ICON, true));
        prevButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_PREV_MATCH"));                             //NOI18N
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shift(-1);
            }
        });
        nextButton = new JButton();
        nextButton.setEnabled(false);
        nextButton.setIcon(ImageUtilities.loadImageIcon(NEXT_ICON, true));
        nextButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_NEXT_MATCH"));                             //NOI18N
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shift(1);
            }
        });
        expandButton = new JToggleButton();
        expandButton.setEnabled(false);
        expandButton.setIcon(ImageUtilities.loadImageIcon(EXPAND_ICON, true));
        expandButton.setSelectedIcon(ImageUtilities.loadImageIcon(
                COLLAPSE_ICON, true));
        expandButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_EXPAND"));                                 //NOI18N
        expandButton.setSelected(false);
        expandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleExpand(expandButton.isSelected());
            }
        });
         return new AbstractButton[]{prevButton, nextButton, expandButton};
    }

    private void shift(int direction) {

        Node next = findShifNode(direction);
        if (next == null) {
            return;
        }
        try {
            getExplorerManager().setSelectedNodes(new Node[]{next});
            TextDetail textDetail = next.getLookup().lookup(
                    TextDetail.class);
            if (textDetail != null) {
                textDetail.showDetail(TextDetail.DH_GOTO);
            }
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Node findShifNode(int direction) {
        Node[] selected = getExplorerManager().getSelectedNodes();
        Node n = null;
        if (selected == null || selected.length == 0) {
            n = getExplorerManager().getRootContext();
        } else if (selected.length == 1) {
            n = selected[0];
        }
        return n == null ? null : findTextDetailNode(n, direction);
    }

    static Node findTextDetailNode(Node fromNode, int direction) {
        return findUp(fromNode, direction,
                isTextDetailNode(fromNode) || direction < 0 ? direction : 0);
    }

    /**
     * Start finding for next or previous occurance, from a node or its previous
     * or next sibling of node {@code node}
     *
     * @param node reference node
     * @param offset 0 to start from node {@code node}, 1 to start from its next
     * sibling, -1 to start from its previous sibling.
     * @param dir Direction: 1 for next, -1 for previous.
     */
    static Node findUp(Node node, int dir, int offset) {
        if (node == null) {
            return null;
        }
        Node parent = node.getParentNode();
        Node[] siblings;
        if (parent == null) {
            siblings = new Node[]{node};
        } else {
            siblings = parent.getChildren().getNodes(true);
        }
        int nodeIndex = findChildIndex(node, siblings);
        if (nodeIndex + offset < 0 || nodeIndex + offset >= siblings.length) {
            return findUp(parent, dir, dir);
        }
        for (int i = nodeIndex + offset;
                i >= 0 && i < siblings.length; i += dir) {
            Node found = findDown(siblings[i], siblings, i, dir);
            return found;
        }
        return findUp(parent, dir, offset);
    }

    /**
     * Find Depth-first search to find TextDetail node in the subtree.
     */
    private static Node findDown(Node node, Node[] siblings, int nodeIndex,
            int dir) {

        Node[] children = node.getChildren().getNodes(true);
        for (int i = dir > 0 ? 0 : children.length - 1;
                i >= 0 && i < children.length; i += dir) {
            Node found = findDown(children[i], children, i, dir);
            if (found != null) {
                return found;
            }
        }
        for (int i = nodeIndex; i >= 0 && i < siblings.length; i += dir) {
            if (isTextDetailNode(siblings[i])) {
                return siblings[i];
            }
        }
        return null;
    }

    private static boolean isTextDetailNode(Node n) {
        return n.getLookup().lookup(TextDetail.class) != null;
    }

    private static int findChildIndex(Node selectedNode, Node[] siblings) {
        int pos = -1;
        for (int i = 0; i < siblings.length; i++) {
            if (siblings[i] == selectedNode) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private void toggleExpand(boolean expand) {
        getOutlineView().expandNode(resultsNode);
        for (Node n : resultsNode.getChildren().getNodes()) {
            if (expand) {
                getOutlineView().expandNode(n);
            } else {
                getOutlineView().collapseNode(n);
            }
        }
    }
}