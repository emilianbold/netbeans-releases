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
package org.netbeans.modules.etl.ui.view;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyViewManager;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class DBModelTreeView extends JPanel implements PropertyChangeListener {

    private String LOG_CATEGORY = DBModelTreeView.class.getName();
    private JTree tree;
    private List dbModels;
    private EditDBModelPanel editPanel;
    private Component comp;
    private IPropertySheet propSheet;
    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    private static transient final Logger mLogger = Logger.getLogger(DBModelTreeView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static URL rootImgUrl = DBModelTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/root.png");
    private static URL columnImgUrl = DBModelTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/column.png");
    private static URL tableImgUrl = DBModelTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static URL targetTableImgUrl = DBModelTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/TargetTable.png");
    private static ImageIcon rootIcon;
    private static ImageIcon tableIcon;
    private static ImageIcon targetTableIcon;
    private static ImageIcon columnIcon;
    private IPropertyGroup pGroup;
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
            rootIcon = new ImageIcon(rootImgUrl);
            tableIcon = new ImageIcon(tableImgUrl);
            columnIcon = new ImageIcon(columnImgUrl);
            targetTableIcon = new ImageIcon(targetTableImgUrl);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a new instance of DBModelTreeView.
     * 
     * @param dbModels List of DatabaseModels whose contents are to be displayed
     * @param editPanel EditDBModelPanel associated with this view
     */
    public DBModelTreeView(List dbModels, EditDBModelPanel editPanel) {
        super();
        this.editPanel = editPanel;
        this.dbModels = dbModels;
        if (dbModels == null || dbModels.size() == 0) {
            return;
        }

        initGui();
    }

    private void initGui() {
        DragSource dSource = DragSource.getDefaultDragSource();

        this.setLayout(new BorderLayout());
        DefaultTreeModel treeModel = createTreeModel();
        tree = new JTree();

        tree.setCellRenderer(new TableTreeCellRenderer());
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                if (propSheet != null) {
                    propSheet.commitChanges();
                    if (pGroup != null) {
                        pGroup.removePropertyChangeListener(DBModelTreeView.this);
                    }
                    tree.repaint();
                }

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }

                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof SQLDBModel) {
                    if (comp != null) {
                        editPanel.remove(comp);
                    }
                    PropertyViewManager pvMgr = DataObjectHelper.getPropertyViewManager();
                    DBModelObj dbObj = new DBModelObj((SQLDBModel) nodeInfo);
                    propSheet = pvMgr.getPropertySheet(dbObj, "DBModel");
                    comp = propSheet.getPropertySheet();
                    editPanel.setRightComponent(comp);
                    editPanel.updateUI();
                } else if (nodeInfo instanceof DBTable) {
                    if (comp != null) {
                        editPanel.remove(comp);
                    }
                    PropertyViewManager pvMgr = DataObjectHelper.getPropertyViewManager();
                    DBTableObj dbTab = new DBTableObj((SQLDBTable) nodeInfo);
                    propSheet = pvMgr.getPropertySheet(dbTab, "DBTable");
                    pGroup = propSheet.getPropertyGroup("default");
                    if (pGroup != null) {
                        pGroup.addPropertyChangeListener(DBModelTreeView.this);
                    }
                    comp = propSheet.getPropertySheet();
                    editPanel.setRightComponent(comp);
                    editPanel.updateUI();
                } else {
                    editPanel.setRightComponent(new JPanel());
                    editPanel.updateUI();
                }
            }
        });

        dSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY_OR_MOVE, new TreeDragGestureListener());

        tree.setModel(treeModel);

        tree.setRootVisible(false);
        tree.setDragEnabled(true);
        tree.setShowsRootHandles(true);

        Object root = treeModel.getRoot();
        Object rootFirstChild = treeModel.getChild(root, 0);

        Object pathArray[] = new Object[2];
        pathArray[0] = root;
        pathArray[1] = rootFirstChild;

        TreePath tpath = new TreePath(pathArray);
        tree.setSelectionPath(tpath);
        JScrollPane treePane = new JScrollPane(tree);
        this.add(BorderLayout.CENTER, treePane);
    }

    /**
     * Gets current IPropertySheet instance, if any.
     * 
     * @return current IPropertySheet, possibly null.
     */
    public IPropertySheet getPropSheet() {
        return propSheet;
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        Iterator it = dbModels.iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            DefaultMutableTreeNode dbModelNode = new DefaultMutableTreeNode(dbModel);
            rootNode.add(dbModelNode);
            createTableNodes(dbModel, dbModelNode);
        }

        return treeModel;
    }

    private void createTableNodes(SQLDBModel dbModel, DefaultMutableTreeNode dbModelNode) {
        Iterator it = dbModel.getTables().iterator();
        while (it.hasNext()) {
            DBTable table = (DBTable) it.next();
            DefaultMutableTreeNode tableNode = new TableNode(table);
            dbModelNode.add(tableNode);
        }
    }

    private class TableNode extends DefaultMutableTreeNode {

        private SQLDBTable table;

        public TableNode(Object userObj) {
            super(userObj);
            this.table = (SQLDBTable) userObj;
        }

        public String toString() {
            String displayPrefix = table.getSchema();
            displayPrefix += (((displayPrefix != null) && displayPrefix.trim().length() != 0) ? "." : "");

            return displayPrefix + table.getQualifiedName();
        }
    }

    /**
     * This method gets called when a bound property is changed.
     * 
     * @param evt A PropertyChangeEvent object describing the event source and the
     *        property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (propSheet != null) {
            propSheet.commitChanges();
        }

        tree.repaint();
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

                if (obj instanceof SourceColumn) {
                    try {
                        dge.startDrag(DragSource.DefaultCopyDrop, new ColumnTransferable(obj));
                    } catch (InvalidDnDOperationException ex) {
                        mLogger.errorNoloc(mLoc.t("EDIT043: invalid drag and drop{0}", LOG_CATEGORY), ex);
                    }
                }
            }
        }
    }

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

    /**
     * Wrapper object around an instance of SQLDBModel. Required to restrict
     * getter and setter access to SQLDBModel instances by PropertySheet
     * implementations.
     */
    public class DBModelObj {

        private SQLDBModel dbModel;

        /**
         * Creates an instance of DBModelObj associated with the given
         * SQLDBModel.
         * 
         * @param dbModel SQLDBModel to be wrapped
         */
        public DBModelObj(SQLDBModel dbModel) {
            this.dbModel = dbModel;
        }

        /**
         * Gets user name.
         * 
         * @return current user name
         */
        public String getUserName() {
            return dbModel.getConnectionDefinition().getUserName();
        }

        /**
         * Sets username with given String.
         * 
         * @param userName new user name
         */
        public void setUserName(String userName) {
            ((SQLDBConnectionDefinition) this.dbModel.getConnectionDefinition()).setUserName(userName);
        }

        /**
         * Gets current password.
         * 
         * @return current password
         */
        public String getPassword() {
            return dbModel.getConnectionDefinition().getPassword();
        }

        /**
         * Sets password with given String.
         * 
         * @param password new user name
         */
        public void setPassword(String password) {
            ((SQLDBConnectionDefinition) this.dbModel.getConnectionDefinition()).setPassword(password);
        }

        /**
         * Gets current connection URL.
         * 
         * @return current connection URL
         */
        public String getConnectionURL() {
            return dbModel.getConnectionDefinition().getConnectionURL();
        }

        /**
         * Sets connection URL with given String.
         * 
         * @param newURL new connection URL
         */
        public void setConnectionURL(String newURL) {
            ((SQLDBConnectionDefinition) this.dbModel.getConnectionDefinition()).setConnectionURL(newURL);
        }
    }

    /**
     * Wrapper object around an instance of AbstractDBTable. Required to restrict getter
     * and setter access to AbstractDBTable instances by PropertySheet implementations.
     */
    public class DBTableObj {

        private SQLDBTable dbTable;

        /**
         * Creates an instance of DBTableObj associated with the given AbstractDBTable.
         * 
         * @param dbTable AbstractDBTable to be wrapped
         */
        public DBTableObj(SQLDBTable dbTable) {
            this.dbTable = dbTable;
        }

        /**
         * Sets current schema name.
         * 
         * @param newSchema new catalog name
         */
        public void setSchema(String newSchema) {
            dbTable.setSchema(newSchema);
        }

        /**
         * Gets current schema name.
         * 
         * @return current catalog name
         */
        public String getSchema() {
            return dbTable.getSchema();
        }

        /**
         * Gets current catalog name.
         * 
         * @return current catalog name
         */
        public String getCatalog() {
            return dbTable.getCatalog();
        }

        /**
         * Sets current catalog name.
         * 
         * @param newCatalog new catalog name
         */
        public void setCatalog(String newCatalog) {
            dbTable.setCatalog(newCatalog);
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
                Object obj = treeNode.getUserObject();
                if (obj instanceof SQLDBModel) {
                    SQLDBModel dbModel = (SQLDBModel) obj;
                    String dbName = dbModel.getModelName();
                    
                    String nbBundle1 = mLoc.t("BUND156: {0} [Source]",dbName);
                    String nbBundle2 = mLoc.t("BUND157: {0} [Target]",dbName);
                    switch (dbModel.getObjectType()) {
                        case SQLConstants.SOURCE_DBMODEL:
                            dbName =  nbBundle1.substring(15);
                            break;

                        case SQLConstants.TARGET_DBMODEL:
                            dbName =  nbBundle2.substring(15);
                            break;

                        default:
                            break;
                    }

                    renderer.setText(dbName);
                    renderer.setIcon(rootIcon);
                    renderer.setToolTipText(dbName);
                } else if (obj instanceof SourceTable) {
                    renderer.setIcon(tableIcon);
                    renderer.setToolTipText(UIUtil.getTableToolTip((SQLDBTable) obj));
                } else if (obj instanceof TargetTable) {
                    renderer.setIcon(targetTableIcon);
                    renderer.setToolTipText(UIUtil.getTableToolTip((SQLDBTable) obj));
                } else if (obj instanceof SQLDBColumn) {
                    renderer.setIcon(columnIcon);
                    renderer.setToolTipText(UIUtil.getColumnToolTip((SQLDBColumn) obj));
                }
            }

            return renderer;
        }
    }
}
