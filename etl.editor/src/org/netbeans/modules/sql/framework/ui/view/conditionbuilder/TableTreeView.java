/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.openide.awt.StatusDisplayer;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableTreeView extends JPanel {

    //private static transient final Logger mLogger = Logger.getLogger(TableTreeView.class.getName());
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TableTreeView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private class ColumnTransferable implements Transferable {

        private Object transData;

        ColumnTransferable(Object obj) {
            this.transData = obj;
        }

        /**
         * Returns an object which represents the data to be transferred. The class of the
         * object returned is defined by the representation class of the flavor.
         * 
         * @param flavor the requested flavor for the data
         * @return data to be transferred
         * @throws IOException if the data is no longer available in the requested flavor.
         * @throws UnsupportedFlavorException if the requested data flavor is not
         *         supported.
         * @see DataFlavor#getRepresentationClass
         */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return (isDataFlavorSupported(flavor)) ? transData : null;
        }

        /**
         * Returns an array of DataFlavor objects indicating the flavors the data can be
         * provided in. The array should be ordered according to preference for providing
         * the data (from most richly descriptive to least descriptive).
         * 
         * @return an array of data flavors in which this data can be transferred
         */
        public DataFlavor[] getTransferDataFlavors() {
            return mDataFlavorArray;
        }

        /**
         * Returns whether or not the specified data flavor is supported for this object.
         * 
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            for (int i = 0; i < mDataFlavorArray.length; i++) {
                if (flavor.equals(mDataFlavorArray[i])) {
                    return true;
                }
            }

            return false;
        }
    }

    private class TableTreeCellRenderer extends DefaultTreeCellRenderer {

        /**
         * Configures the renderer based on the passed in components. The value is set
         * from messaging the tree with <code>convertValueToText</code>, which
         * ultimately invokes <code>toString</code> on <code>value</code>. The
         * foreground color is set based on the selection and the icon is set based on on
         * leaf and expanded.
         */
        public Component getTreeCellRendererComponent(JTree tree1, Object value, boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus1) {

            JLabel renderer = (JLabel) super.getTreeCellRendererComponent(tree1, value, sel, expanded, leaf, row, hasFocus1);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
                renderer.setToolTipText(null); // don't show tooltips here

                Object obj = treeNode.getUserObject();
                if (obj instanceof SQLDBTable) {
                    SQLDBTable table = (SQLDBTable) obj;

                    if (table.getObjectType() == SQLConstants.SOURCE_TABLE) {
                        renderer.setIcon(srcTableIcon);
                    } else if (table.getObjectType() == SQLConstants.TARGET_TABLE) {
                        renderer.setIcon(targetTableIcon);
                    } else if (table.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                        renderer.setIcon(runtimeInputIcon);
                    }

                    renderer.setToolTipText(UIUtil.getTableToolTip(table));
                } else if (obj instanceof SQLDBColumn) {
                    SQLDBColumn column = (SQLDBColumn) obj;
                    renderer.setText(column.getName());
                    renderer.setIcon(columnIcon);
                    renderer.setToolTipText(UIUtil.getColumnToolTip(column));
                }
            }

            return renderer;
        }
    }

    private class TreeDragGestureListener implements DragGestureListener {

        /**
         * A <code>DragGestureRecognizer</code> has detected a platform-dependent drag
         * initiating gesture and is notifying this listener in order for it to initiate
         * the action for the user.
         * <P>
         * 
         * @param dge the <code>DragGestureEvent</code> describing the gesture that has
         *        just occurred
         */
        public void dragGestureRecognized(DragGestureEvent dge) {
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object obj = selNode.getUserObject();

                if (obj instanceof SQLDBColumn) {
                    try {
                        dge.startDrag(DragSource.DefaultCopyDrop, new ColumnTransferable(obj));
                    } catch (InvalidDnDOperationException ex) {
                        String msg = mLoc.t("EDIT043: invalid drag and drop{0}", LOG_CATEGORY);
                        StatusDisplayer.getDefault().setStatusText(msg.substring(15) + ex.getMessage());
                        logger.log(Level.SEVERE, ex.getMessage());
                    }
                }
            }
        }
    }
    private static ImageIcon columnIcon;
    private static URL columnImgUrl = TableTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/column.png");
    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    private static ImageIcon runtimeInputIcon;
    private static URL runtimeInputImgUrl = TableTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/RuntimeInput.png");
    private static ImageIcon srcTableIcon;
    private static URL srcTableImgUrl = TableTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static ImageIcon targetTableIcon;
    private static URL targetTableImgUrl = TableTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/TargetTable.png");
    private String LOG_CATEGORY = TableTreeView.class.getName();
    private List tables;
    private JTree tree;
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);

            srcTableIcon = new ImageIcon(srcTableImgUrl);
            targetTableIcon = new ImageIcon(targetTableImgUrl);
            runtimeInputIcon = new ImageIcon(runtimeInputImgUrl);

            columnIcon = new ImageIcon(columnImgUrl);

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /** Creates a new instance of TableTreeView */
    public TableTreeView(List tables) {
        super();

        this.tables = tables;
        initGui();
    }

    private void createColumnNodes(SQLDBTable table, DefaultMutableTreeNode tableNode) {
        Iterator it = table.getColumnList().iterator();
        final boolean isNotRuntimeInput = (table.getObjectType() != SQLConstants.RUNTIME_INPUT);
        while (it.hasNext()) {
            SQLDBColumn column = (SQLDBColumn) it.next();
            // Add column node only if its parent is not a RuntimeInput and visible, or if
            // the runtime argument is editable and visible (i.e., not a system runtime
            // input like input file name, etc.)
            if ((isNotRuntimeInput && column.isVisible()) || (column.isEditable() && column.isVisible())) {
                DefaultMutableTreeNode columnNode = new DefaultMutableTreeNode(column);
                tableNode.add(columnNode);
            }
        }
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        Iterator it = tables.iterator();
        while (it.hasNext()) {
            SQLDBTable table = (SQLDBTable) it.next();
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(table);
            createColumnNodes(table, tableNode);

            // Add the table node only if it has at least one column node.
            if (tableNode.getChildCount() != 0) {
                rootNode.add(tableNode);
            }
        }

        return treeModel;
    }

    private void expandAllChildNodes(DefaultTreeModel model) {
        if (tree != null && model != null) {
            final int childCount = model.getChildCount(model.getRoot());
            for (int i = 0; i < childCount; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getChild(model.getRoot(), i);
                tree.expandPath(new TreePath(node.getPath()));
            }
        }
    }

    private void initGui() {
        DragSource dSource = DragSource.getDefaultDragSource();

        this.setLayout(new BorderLayout());
        DefaultTreeModel treeModel = createTreeModel();
        tree = new JTree();
        dSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());

        tree.setModel(treeModel);
        tree.setRootVisible(false);

        tree.setCellRenderer(new TableTreeCellRenderer());

        ToolTipManager.sharedInstance().registerComponent(tree);
        expandAllChildNodes(treeModel);

        JScrollPane sPane = new JScrollPane(tree);
        this.add(BorderLayout.CENTER, sPane);
    }
}

