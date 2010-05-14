/*
 * ConfigParamsTreeView.java
 *
 * Created on November 20, 2006, 7:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.modules.etl.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.netbeans.modules.etl.ui.DataObjectHelper;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.impl.SQLDefinitionImpl;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyGroup;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.netbeans.modules.sql.framework.ui.editor.property.impl.PropertyViewManager;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import net.java.hulp.i18n.Logger;
import com.sun.etl.utils.XmlUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 *
 * @author karthik
 */
public class ConfigParamsTreeView extends JPanel implements PropertyChangeListener {

    private String LOG_CATEGORY = DBModelTreeView.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(ConfigParamsTreeView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private JTree tree;
    private List srcdbModels;
    private List tgtdbModels;
    private ConfigureParametersPanel editPanel;
    private Component comp;
    private IPropertySheet propSheet;
    private ETLDataObject dObj;
    private File confFile;
    private SQLDefinition sqlDefn;
    private final String CONFIG_FILE_PATH = "\\..\\..\\nbproject\\config\\";
    private final String CONFIG_FILE = ".conf";
    private final String ETL_CONFIG_TAG = "ETLConfig";
    private final String ETL_COLLAB_TAG = "ETLCollaboration";
    private final String ETL_COLLAB_ATTR_1 = "name";
    private final String ETL_SOURCE_TAG = "source";
    private final String ETL_TARGET_TAG = "target";
    private final String ETL_CONNECTION_TAG = "jdbcConnection";
    private final String ETL_CONNECTION_ATTR_1 = "name";
    private final String ETL_CONNECTION_ATTR_2 = "url";
    private final String ETL_CONNECTION_ATTR_3 = "username";
    private final String ETL_CONNECTION_ATTR_4 = "password";
    private final String ETL_CONNECTION_ATTR_5 = "schema";
    private final String ETL_CONNECTION_ATTR_6 = "catalog";
    private final String ETL_CONNECTION_ATTR_7 = "dbTable";
    private final String ETL_CONNECTION_ATTR_8 = "dataDir";
    private static URL rootImgUrl = ConfigParamsTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/root.png");
    private static URL columnImgUrl = ConfigParamsTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/column.png");
    private static URL tableImgUrl = ConfigParamsTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private static URL targetTableImgUrl = ConfigParamsTreeView.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/TargetTable.png");
    private static ImageIcon rootIcon;
    private static ImageIcon tableIcon;
    private static ImageIcon targetTableIcon;
    private static ImageIcon columnIcon;
    private IPropertyGroup pGroup;
    

    static {
        rootIcon = new ImageIcon(rootImgUrl);
        tableIcon = new ImageIcon(tableImgUrl);
        columnIcon = new ImageIcon(columnImgUrl);
        targetTableIcon = new ImageIcon(targetTableImgUrl);
    }

    /**
     * Creates a new instance of ConfigParamsTreeView.
     *
     * @param mObj etlDataObject containing designtime db parameters
     * @param editPanel ConfigureParametersPanel associated with this view
     */
    public ConfigParamsTreeView(ETLDataObject mObj, ConfigureParametersPanel editPanel) {
        super();
        this.dObj = mObj;
        this.editPanel = editPanel;
        this.confFile = new File(mObj.getPrimaryFile().getPath() + CONFIG_FILE_PATH + mObj.getName() + CONFIG_FILE);
        this.srcdbModels = getDBModels(true);
        this.tgtdbModels = getDBModels(false);
        initGui();
    }

    private void initGui() {

        this.setLayout(new BorderLayout());
        DefaultTreeModel treeModel = createTreeModel();
        tree = new JTree();

        tree.setCellRenderer(new TableTreeCellRenderer());
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                if (propSheet != null) {
                    propSheet.commitChanges();
                    if (pGroup != null) {
                        pGroup.removePropertyChangeListener(ConfigParamsTreeView.this);
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
                        pGroup.addPropertyChangeListener(ConfigParamsTreeView.this);
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

    /**
     * Gets sql definition data corresponding to the collab.
     *
     * @return SQLDefinition representing the collab DB models.
     */
    public SQLDefinition getData() {
        return sqlDefn;
    }

    private DefaultTreeModel createTreeModel() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Configuration");
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        DefaultMutableTreeNode srcNode = new DefaultMutableTreeNode("Source");
        DefaultMutableTreeNode tgtNode = new DefaultMutableTreeNode("Target");
        rootNode.add(srcNode);
        Iterator it = srcdbModels.iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            DefaultMutableTreeNode dbModelNode = new DefaultMutableTreeNode(dbModel);
            srcNode.add(dbModelNode);
            createTableNodes(dbModel, dbModelNode);
        }
        rootNode.add(tgtNode);
        it = tgtdbModels.iterator();
        while (it.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) it.next();
            DefaultMutableTreeNode dbModelNode = new DefaultMutableTreeNode(dbModel);
            tgtNode.add(dbModelNode);
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

    /**
     * get the DBModels list from the config file if present or from the etl definition file.
     *
     * @param isSource type of DBModel
     *
     */
    private List getDBModels(boolean isSource) {
        Node rootNode = null;
        try {
            Element element = XmlUtil.loadXMLFile(new BufferedReader(new FileReader(this.confFile)));
            rootNode = (Node) element;
        } catch (Exception ex) {
            mLogger.infoNoloc(mLoc.t("EDIT041: ConfigParamsTreeView.class.getName(){0}", ex.getMessage()));
        }
        Node node;
        if (rootNode != null) {
            Node sqlNode = rootNode.getFirstChild();
            try {
                sqlDefn = new SQLDefinitionImpl((Element) sqlNode);
            } catch (Exception ex) {
                mLogger.infoNoloc(mLoc.t("EDIT041: ConfigParamsTreeView.class.getName(){0}", ex.getMessage()));
            }
        }
        if (isSource) {
            return sqlDefn.getSourceDatabaseModels();
        } else {
            return sqlDefn.getTargetDatabaseModels();
        }
    }

    /**
     * Wrapper object around an instance of SQLDBModel. Required to restrict
     * getter and setter access to SQLDBModel instances by PropertySheet
     * implementations.
     */
    public class DBModelObj {

        private SQLDBModel dbModel;
        private SQLDBModel orgDBModel;

        /**
         * Creates an instance of DBModelObj associated with the given
         * SQLDBModel.
         *
         * @param dbModel SQLDBModel to be wrapped
         */
        public DBModelObj(SQLDBModel dbModel) {
            this.dbModel = dbModel;
            this.orgDBModel = dbModel;
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
            try {
                sqlDefn.removeObject(orgDBModel);
                sqlDefn.addObject(dbModel);
                orgDBModel = dbModel;
            } catch (Exception ex) {
                // ignore
            }
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
            try {
                sqlDefn.removeObject(orgDBModel);
                sqlDefn.addObject(dbModel);
                orgDBModel = dbModel;
            } catch (Exception ex) {
                // ignore
            }
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
            try {
                sqlDefn.removeObject(orgDBModel);
                sqlDefn.addObject(dbModel);
                orgDBModel = dbModel;
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    /**
     * Wrapper object around an instance of AbstractDBTable. Required to restrict getter
     * and setter access to AbstractDBTable instances by PropertySheet implementations.
     */
    public class DBTableObj {

        private SQLDBTable dbTable;
        private SQLDBTable orgDBTable;

        /**
         * Creates an instance of DBTableObj associated with the given AbstractDBTable.
         *
         * @param dbTable AbstractDBTable to be wrapped
         */
        public DBTableObj(SQLDBTable dbTable) {
            this.orgDBTable = dbTable;
            this.dbTable = dbTable;
        }

        /**
         * Sets current schema name.
         *
         * @param newSchema new catalog name
         */
        public void setSchema(String newSchema) {
            dbTable.setSchema(newSchema);
            try {
                SQLDBModel dbModel = (SQLDBModel) dbTable.getParent();
                sqlDefn.removeObject(dbModel);
                dbModel.deleteTable(dbTable.getName());
                sqlDefn.addObject(dbModel);
                orgDBTable = dbTable;
            } catch (Exception ex) {
                // ignore
            }
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
            try {
                SQLDBModel dbModel = (SQLDBModel) dbTable.getParent();
                sqlDefn.removeObject(dbModel);
                dbModel.deleteTable(dbTable.getName());
                sqlDefn.addObject(dbModel);
                orgDBTable = dbTable;
            } catch (Exception ex) {
                // ignore
            }
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
                            dbName = nbBundle1.substring(15);
                            break;

                        case SQLConstants.TARGET_DBMODEL:
                            dbName = nbBundle2.substring(15);
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
