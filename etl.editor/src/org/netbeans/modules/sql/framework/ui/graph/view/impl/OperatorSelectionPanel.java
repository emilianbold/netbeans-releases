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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoCategory;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class OperatorSelectionPanel extends JPanel {
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 230;

    private IOperatorXmlInfoModel model;
    private JTabbedPane tabPane;
    private boolean showNames = true;
    private IGraphView graphView;
    private int toolbarType = IOperatorXmlInfoModel.CATEGORY_ALL;

    /**
     * Creates a new instance of OperatorSelectionPanel using the given model.
     * 
     * @param m OperatorXmlInfoModel containing operator information
     */
    public OperatorSelectionPanel(IGraphView gView, IOperatorXmlInfoModel m, int type) {
        this.model = m;
        this.graphView = gView;
        this.toolbarType = type;
        this.setLayout(new BorderLayout());
        this.tabPane = new JTabbedPane();
        initializeSelectionPanel();
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.add(BorderLayout.CENTER, tabPane);
    }

    /**
     * Selects the panel corresponding to the given category name.
     * 
     * @param catName name of category whose panel is to be selected
     */
    public void selectCategory(String catName) {
        int i = tabPane.indexOfTab(catName);
        tabPane.setSelectedIndex(i);
    }

    /**
     * Updates display to show or hide names of operator items, depending on value of
     * given flag.
     * 
     * @param newFlag true to display operator names, false to hide
     */
    public synchronized void updateShowNames(boolean newFlag) {
        if (showNames != newFlag) {
            showNames = newFlag;
            Component[] panels = tabPane.getComponents();
            for (int i = 0; i < panels.length; i++) {
                if (panels[i] instanceof OperatorCategoryPanel) {
                    ((OperatorCategoryPanel) panels[i]).updateShowNames(newFlag);
                }
            }
        }
    }

    private void initializeSelectionPanel() {
        Node node = model.getRootNode();
        Children children = node.getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node catNode = nodes[i];
            IOperatorXmlInfoCategory catXmlInfo = (IOperatorXmlInfoCategory) catNode;
            if (shouldDisplay(catXmlInfo.getToolbarType())) {
                OperatorCategoryPanel catPanel = new OperatorCategoryPanel(catNode);
                tabPane.add(catNode.getDisplayName(), catPanel);
            }
        }
    }

    public void setToolbarType(int type) {
        this.toolbarType = type;
    }

    public int getToolbarType() {
        return toolbarType;
    }

    private boolean shouldDisplay(final int nodeType) {
        return (IOperatorXmlInfoModel.CATEGORY_ALL == toolbarType || IOperatorXmlInfoModel.CATEGORY_ALL == nodeType || (nodeType & toolbarType) != 0);
    }

    class OperatorCategoryPanel extends JPanel {
        private Node node;
        private JPanel innerPnl;
        private GridBagLayout layout;

        public OperatorCategoryPanel(Node myNode) {
            node = myNode;
            setName(node.getDisplayName());
            setLayout(new BorderLayout());

            layout = new GridBagLayout();
            innerPnl = new JPanel(layout);
            this.add(innerPnl, BorderLayout.NORTH);

            // this.setLayout(new FlowLayout(FlowLayout.LEFT));
            createOperators();
        }

        public synchronized void updateShowNames(boolean showNames1) {
            doUpdateShowNames(getComponents(), showNames1);
        }

        private void doUpdateShowNames(Component[] children, boolean showNames1) {
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof OperatorCheckBox) {
                    ((OperatorCheckBox) children[i]).setShowName(showNames1);
                } else if (children[i] instanceof JPanel) {
                    doUpdateShowNames(((JPanel) children[i]).getComponents(), showNames1);
                }
            }
        }

        private void createOperators() {
            Children children = node.getChildren();
            Node[] nodes = children.getNodes();
            int opsAdded = 0;

            for (int i = 0; i < nodes.length; i++) {
                IOperatorXmlInfo opNode = (IOperatorXmlInfo) nodes[i];
                if (OperatorSelectionPanel.this.shouldDisplay(opNode.getToolbarType())) {
                    int rd = (opsAdded + 1) % 3;
                    boolean nextLine = (rd == 0);
                    addCheckBox(opNode, nextLine);
                    opsAdded++;
                }
            }
        }

        private void addCheckBox(IOperatorXmlInfo node1, boolean nextLine) {
            GridBagConstraints c = new GridBagConstraints();

            JComponent checkBox = new OperatorCheckBox(node1, showNames);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            if (nextLine) {
                c.gridwidth = GridBagConstraints.REMAINDER;
            }

            layout.setConstraints(checkBox, c);
            innerPnl.add(checkBox);
        }
    }

    class OperatorCheckBox extends JPanel implements ItemListener, FocusListener, KeyListener {
        private JCheckBox cbox;
        private JPanel labelWrapper;
        private JLabel label;
        private IOperatorXmlInfo opNode;

        public OperatorCheckBox(IOperatorXmlInfo node) {
            this(node, true);
        }

        public OperatorCheckBox(final IOperatorXmlInfo node, boolean showName) {
            setLayout(new FlowLayout(FlowLayout.LEADING));
            setFocusable(true);

            opNode = node;
            cbox = new JCheckBox();
            cbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            cbox.setFocusable(false);
            add(cbox);

            MouseAdapter cboxClickListener = new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    OperatorCheckBox.this.requestFocusInWindow();
                }
            };

            labelWrapper = new JPanel();
            labelWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            Icon myIcon = node.getIcon();
            if (myIcon != null) {
                JLabel iconLabel = new JLabel(node.getIcon(), SwingConstants.LEADING);
                iconLabel.setFocusable(false);
                labelWrapper.add(iconLabel);
            }

            label = new JLabel(node.getDisplayName(), SwingConstants.LEADING);
            label.setFocusable(false);

            labelWrapper.add(label);
            labelWrapper.setToolTipText(node.getToolTip());
            this.add(labelWrapper);

            cbox.setSelected(node.isChecked());
            cbox.addItemListener(this);
            cbox.addMouseListener(cboxClickListener);

            label.setVisible(showName);

            addFocusListener(this);
            addKeyListener(this);

            // Set right, left arrow keys to be forward, backward traversal keys,
            // respectively.
            Set newForwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
            newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

            Set newBackwardKeys = new HashSet(getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
            newBackwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
            setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newBackwardKeys);
        }

        /**
         * Invoked when an item has been selected or deselected by the user. The code
         * written for this method performs the operations that need to occur when an item
         * is selected (or deselected).
         */
        public void itemStateChanged(ItemEvent e) {
            JCheckBox ckBox = (JCheckBox) e.getItem();
            opNode.setChecked(ckBox.isSelected());
        }

        public void setShowName(boolean showName) {
            label.setVisible(showName);
            revalidate();
            repaint();
        }

        public void focusGained(FocusEvent e) {
            // Highlight the focused label.
            labelWrapper.setBackground(Color.YELLOW);
            this.revalidate();
            this.repaint();
        }

        public void focusLost(FocusEvent e) {
            // Remove highlight from the previously focused label.
            labelWrapper.setBackground(getParent().getBackground());
            this.revalidate();
            this.repaint();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
         */
        public void keyTyped(KeyEvent e) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
         */
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    dropOperatorInstance();
                    e.consume();
                    break;

                case KeyEvent.VK_SPACE:
                    cbox.doClick();
                    e.consume();
                    break;

                default:
                    break;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
         */
        public void keyReleased(KeyEvent e) {
        }

        private void dropOperatorInstance() {
            if (graphView != null) {
                graphView.getGraphController().handleNodeAdded(opNode, new java.awt.Point(50, 50));
            }
        }
    }
}

