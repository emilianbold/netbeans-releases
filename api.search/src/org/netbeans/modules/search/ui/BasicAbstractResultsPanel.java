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
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.netbeans.modules.search.BasicComposition;
import org.netbeans.modules.search.BasicSearchCriteria;
import org.netbeans.modules.search.Manager;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.PrintDetailsTask;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.ResultView;
import org.netbeans.modules.search.TextDetail;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public abstract class BasicAbstractResultsPanel
        extends AbstractSearchResultsPanel {

    private static final String NEXT_ICON =
            "org/netbeans/modules/search/res/next.png";                 //NOI18N
    private static final String PREV_ICON =
            "org/netbeans/modules/search/res/prev.png";                 //NOI18N
    private static final String EXPAND_ICON =
            "org/netbeans/modules/search/res/expandTree.png";           //NOI18N
    private static final String COLLAPSE_ICON =
            "org/netbeans/modules/search/res/colapseTree.png";          //NOI18N
    private static final String SHOW_DETAILS_ICON =
            "org/netbeans/modules/search/res/search.gif";               //NOI18N
    private static final String FLAT_VIEW_ICON =
            "org/netbeans/modules/search/res/logical_view.png";         //NOI18N
    private static final String FOLDER_VIEW_ICON =
            "org/netbeans/modules/search/res/file_view.png";            //NOI18N
    protected ResultModel resultModel;
    private JButton nextButton;
    private JButton prevButton;
    private JToggleButton expandButton;
    private JToggleButton toggleViewButton;
    private JButton showDetailsButton;
    protected boolean details;
    private BasicComposition composition;
    protected final ResultsOutlineSupport resultsOutlineSupport;

    public BasicAbstractResultsPanel(ResultModel resultModel,
            BasicComposition composition, boolean details,
            List<FileObject> rootFiles,
            ResultsOutlineSupport resultsOutlineSupport) {

        super(composition, composition.getSearchProviderPresenter());
        this.composition = composition;
        this.details = details;
        this.resultModel = resultModel;
        this.resultsOutlineSupport = resultsOutlineSupport;
        getExplorerManager().setRootContext(
                resultsOutlineSupport.getRootNode());
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

    public void update() {
        if (details && expandButton != null && !expandButton.isEnabled()) {
            expandButton.setEnabled(resultModel.size() > 0);
        }
        updateShiftButtons();
        resultsOutlineSupport.update();
    }

    private void updateShiftButtons() {
        if (details && prevButton != null && nextButton != null) {
            prevButton.setEnabled(findShifNode(-1) != null);
            nextButton.setEnabled(findShifNode(1) != null);
        }
    }

    @Override
    protected AbstractButton[] createButtons() {
        toggleViewButton = new JToggleButton();
        toggleViewButton.setEnabled(true);
        toggleViewButton.setIcon(ImageUtilities.loadImageIcon(FOLDER_VIEW_ICON,
                true));
        toggleViewButton.setSelectedIcon(ImageUtilities.loadImageIcon(
                FLAT_VIEW_ICON, true));
        toggleViewButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_TOGGLE_VIEW"));                            //NOI18N
        toggleViewButton.setSelected(false);
        toggleViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (toggleViewButton.isSelected()) {
                    resultsOutlineSupport.setFolderTreeMode();
                } else {
                    resultsOutlineSupport.setFlatMode();
                }
            }
        });
        if (!details) {
            return new AbstractButton[]{toggleViewButton};
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
        showDetailsButton = new JButton();
        showDetailsButton.setEnabled(false);
        showDetailsButton.setIcon(ImageUtilities.loadImageIcon(
                SHOW_DETAILS_ICON, true));
        showDetailsButton.setToolTipText(UiUtils.getText(
                "TEXT_BUTTON_FILL"));                                   //NOI18N
        showDetailsButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                fillOutput();
            }
        });

        return new AbstractButton[]{prevButton, nextButton, expandButton,
                    toggleViewButton, showDetailsButton};
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
        Node rootNode = getExplorerManager().getRootContext();
        getOutlineView().expandNode(rootNode);
        toggleExpand(rootNode, expand);
    }

    public void toggleExpand(Node root, boolean expand) {
        for (Node n : root.getChildren().getNodes()) {
            if (expand) {
                getOutlineView().expandNode(n);
            } else {
                getOutlineView().collapseNode(n);
            }
            toggleExpand(n, expand);
        }
    }

    @Override
    public void searchStarted() {
        super.searchStarted();
        setRootDisplayName(NbBundle.getMessage(ResultView.class,
                "TEXT_SEARCHING___"));                                  //NOI18N
    }

    @Override
    public void searchFinished() {
        super.searchFinished();
        if (details && resultModel.size() > 0 && showDetailsButton != null) {
            showDetailsButton.setEnabled(true);
        }
        setFinalRootNodeText();
    }

    /**
     * Send search details to output window.
     */
    public void fillOutput() {
        Manager.getInstance().schedulePrintTask(
                new PrintDetailsTask(resultModel.getMatchingObjects(),
                composition.getBasicSearchCriteria()));
    }

    public void addMatchingObject(MatchingObject mo) {
        resultsOutlineSupport.addMatchingObject(mo);
        updateRootNodeText();
    }

    public OutlineView getOutlineView() {
        return resultsOutlineSupport.getOutlineView();
    }

    private void setFinalRootNodeText() {

        int resultSize = resultModel.size();

        if (resultModel.wasLimitReached()) {
            setRootDisplayName(
                    NbBundle.getMessage(
                    ResultView.class,
                    "TEXT_MSG_FOUND_X_NODES_LIMIT", //NOI18N
                    Integer.valueOf(resultSize),
                    Integer.valueOf(resultModel.getTotalDetailsCount()))
                    + ' ' + resultModel.getLimitDisplayName());         //NOI18N
            return;
        }

        String baseMsg;
        if (resultSize == 0) {
            baseMsg = NbBundle.getMessage(ResultView.class,
                    "TEXT_MSG_NO_NODE_FOUND");                          //NOI18N
        } else {
            String bundleKey;
            Object[] args;
            if (resultModel.isSearchAndReplace()) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_REPLACE";           //NOI18N
                args = new Object[4];
            } else if (resultModel.canHaveDetails()) {
                bundleKey = "TEXT_MSG_FOUND_X_NODES_FULLTEXT";          //NOI18N
                args = new Object[2];
            } else {
                bundleKey = "TEXT_MSG_FOUND_X_NODES";                   //NOI18N
                args = new Object[1];
            }
            args[0] = new Integer(resultModel.size());
            if (args.length > 1) {
                args[1] = new Integer(resultModel.getTotalDetailsCount());
            }
            if (args.length > 2) {
                BasicSearchCriteria bsc = composition.getBasicSearchCriteria();
                args[2] = bsc.getTextPatternExpr();
                args[3] = bsc.getReplaceExpr();
            }
            baseMsg = NbBundle.getMessage(ResultView.class, bundleKey, args);
        }
        String exMsg = resultModel.getExceptionMsg();
        String msg = exMsg == null ? baseMsg
                : baseMsg + " (" + exMsg + ")";      //NOI18N
        setRootDisplayName(msg);
    }

    private void setRootDisplayName(String displayName) {
        Node root = getExplorerManager().getRootContext();
        root.setDisplayName(displayName);
    }

    protected void updateRootNodeText() {
        Integer objectsCount = resultModel.size();
        if (details) {
            Integer detailsCount = resultModel.getTotalDetailsCount();
            setRootDisplayName(NbBundle.getMessage(ResultView.class,
                    "TXT_RootSearchedNodes", //NOI18N
                    objectsCount, detailsCount));
        } else {
            setRootDisplayName(NbBundle.getMessage(ResultView.class,
                    "TXT_RootSearchedNodesFulltext", objectsCount));    //NOI18N
        }
    }
}