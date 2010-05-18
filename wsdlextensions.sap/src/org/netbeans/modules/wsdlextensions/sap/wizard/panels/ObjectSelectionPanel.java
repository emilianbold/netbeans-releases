/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ObjectSelectionPanel.java
 *
 * Created on Oct 7, 2009, 6:30:30 PM
 */

package org.netbeans.modules.wsdlextensions.sap.wizard.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIMethod;
import org.netbeans.modules.wsdlextensions.sap.model.BAPIObject;
import org.netbeans.modules.wsdlextensions.sap.model.IDocType;
import org.netbeans.modules.wsdlextensions.sap.model.RFC;
import org.netbeans.modules.wsdlextensions.sap.model.SapConnection;
import org.netbeans.modules.wsdlextensions.sap.util.BORClient;
import org.netbeans.modules.wsdlextensions.sap.util.IDocUtil;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;

/**
 *
 * @author jqian
 */
public class ObjectSelectionPanel extends javax.swing.JPanel
    implements TreeSelectionListener, TreeExpansionListener {

    private static String BAPI_NODE_TEXT = "Business Objects (BAPIs)";
    private static String RFC_NODE_TEXT = "Function Modules (RFCs)";
    private static String IDOC_NODE_TEXT = "ALE/EDI Messages (IDOCs)";

    // caching trees for performance purpose
    // (building the root BAPI object takes about 6-7 seconds)
    private static Map<String, JTree> connection2HierarchicalTree =
            new HashMap<String, JTree>();
    private static Map<String, JTree> connection2AlphabeticalTree =
            new HashMap<String, JTree>();

    private SapConnection connection;

    /** Creates new form ObjectSelectionPanel */
    public ObjectSelectionPanel() {
        initComponents();

         _tabbedPane.getModel().addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        updatePageAdvanceState();
                    }
                });
    }

    public void setConnection(SapConnection connection) {
        this.connection = connection;
        initTrees();
    }

    private void initTrees() {
        _trHierarchicalTree = getTree(connection, true);
        _spHierarchicalTree.setViewportView(_trHierarchicalTree);

        _trAlphabeticalTree = getTree(connection, false);
        _spAlphabeticalTree.setViewportView(_trAlphabeticalTree);
    }

    public void treeExpanded(final TreeExpansionEvent e) {

        TreePath path = e.getPath();
        if (path.getPathCount() != 2) {
            return;
        }

        DefaultMutableTreeNode lastNode =
                (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object lastObject = lastNode.getUserObject();
        if (lastNode.getChildCount() != 1) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JTree tree = (JTree) e.getSource();
                DefaultTreeModel treeModel = ((DefaultTreeModel) tree.getModel());
                boolean hierarchical = tree == _trHierarchicalTree;

                DefaultMutableTreeNode rootNode =
                        (DefaultMutableTreeNode) treeModel.getRoot();
                DefaultMutableTreeNode categoryNode = null;
                if (lastObject.equals(BAPI_NODE_TEXT)) {
                    categoryNode = (DefaultMutableTreeNode) rootNode.getChildAt(0);
                } else if (lastObject.equals(RFC_NODE_TEXT)) {
                    categoryNode = (DefaultMutableTreeNode) rootNode.getChildAt(1);
                } else {
                    assert lastObject.equals(IDOC_NODE_TEXT);
                    categoryNode = (DefaultMutableTreeNode) rootNode.getChildAt(2);
                }

                try {
                    BORClient borClient = new BORClient(connection);

                    if (lastObject.equals(BAPI_NODE_TEXT)) {
                        BAPIObject rootObject = borClient.buildBOModel();
                        if (hierarchical) {
                            categoryNode.setUserObject(rootObject);
                            populateHierarchicalBOTree(categoryNode);
                            categoryNode.setUserObject(BAPI_NODE_TEXT);
                        } else {
                            populateAlphabeticalBOTree(categoryNode, rootObject);
                        }
                    } else if (lastObject.equals(RFC_NODE_TEXT)) {
                        List<RFC> rfcs = borClient.getRFCList();
                        if (hierarchical) {
                            populateHierarchicalRFCTree(categoryNode, rfcs);
                        } else {
                            populateAlphabeticalRFCTree(categoryNode, rfcs);
                        }
                    } else {
                        assert lastObject.equals(IDOC_NODE_TEXT);
                        List<IDocType> iDocTypes =
                                new IDocUtil().getIDocTypes(borClient.getRfcClient(), "620"); // TMP system release

                        if (hierarchical) {
                            populateHierarchicalIDocTree(categoryNode, iDocTypes);
                        } else {
                            populateAlphabeticalIDocTree(categoryNode, iDocTypes);
                        }
                    }
                    categoryNode.remove(0); // remove the dummy child
                    treeModel.nodeStructureChanged(categoryNode);
                } catch (Exception ex) {
                    DefaultMutableTreeNode dummyChild =
                            ((DefaultMutableTreeNode) categoryNode.getChildAt(0));
                    dummyChild.setUserObject("Failed to connect to SAP R/3 system.");
                    treeModel.nodeChanged(dummyChild);
                }
            }
        });
    }

    public void treeCollapsed(TreeExpansionEvent e) {
    }

    /**
     * Gets the (possibly cached) hierarchical or alphabetical tree from
     * the given connection.
     *
     * @param connection    a SAP connection
     * @param hierarchical  <code>true</code> for hierarchical tree,
     *                      or <code>false</code> for alphabetical tree
     * @return
     */
    private JTree getTree(SapConnection connection, boolean hierarchical) {

        Map<String, JTree> connection2TreeMap =
                hierarchical ? connection2HierarchicalTree : connection2AlphabeticalTree;

        final String connectionString = connection.toString();

        JTree tree = connection2TreeMap.get(connectionString);

        if (tree == null) {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("SAP");

            DefaultMutableTreeNode bapiNode = new DefaultMutableTreeNode(BAPI_NODE_TEXT);
            DefaultMutableTreeNode rfcNode = new DefaultMutableTreeNode(RFC_NODE_TEXT);
            DefaultMutableTreeNode iDocNode = new DefaultMutableTreeNode(IDOC_NODE_TEXT);

            DefaultMutableTreeNode dummyChildNode1 = new DefaultMutableTreeNode("Please wait...");
            DefaultMutableTreeNode dummyChildNode2 = new DefaultMutableTreeNode("Please wait...");
            DefaultMutableTreeNode dummyChildNode3 = new DefaultMutableTreeNode("Please wait...");

            rootNode.add(bapiNode);
            rootNode.add(rfcNode);
            rootNode.add(iDocNode);

            bapiNode.add(dummyChildNode1);
            rfcNode.add(dummyChildNode2);
            iDocNode.add(dummyChildNode3);

            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

            tree = new JTree();
            tree.setModel(treeModel);
            tree.setRootVisible(true);
            tree.setCellRenderer(new MyTreeCellRenderer());

            connection2TreeMap.put(connectionString, tree);
        }

        tree.removeTreeExpansionListener(this);
        tree.addTreeExpansionListener(this);

        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        treeSelectionModel.removeTreeSelectionListener(this);
        treeSelectionModel.addTreeSelectionListener(this);

        // add refresh action to clear the cache
        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Refresh");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connection2HierarchicalTree.put(connectionString, null);
                connection2AlphabeticalTree.put(connectionString, null);
                initTrees();
            }
        });
        popup.add(menuItem);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });

        return tree;
    }
   
    /**
     * Gets the currently selected BO, BAPI or IDoc.
     *
     * @return
     */
    public Object getUserSelection() {
        Object ret = null;

        Object selection = null;

        JTree tree = _tabbedPane.getSelectedIndex() == 0 ? _trHierarchicalTree : _trAlphabeticalTree;
        TreePath selectionTreePath = tree.getSelectionPath();
        DefaultMutableTreeNode lastPathComponent = null;
        if (selectionTreePath != null) {
            lastPathComponent =
                    (DefaultMutableTreeNode) selectionTreePath.getLastPathComponent();
            selection = lastPathComponent.getUserObject();
        }

        if (selection != null) {
            if (selection instanceof BAPIObject) {
                if (((BAPIObject) selection).isLeaf()) {
                    ret = (BAPIObject) selection;
                }
            } else if (selection instanceof BAPIMethod) {
                ret = (BAPIMethod) selection;
            } else if (selection instanceof RFC) {
                ret = (RFC) selection;
            } else if (selection instanceof IDocType) {
                ret = (IDocType) selection;
            }
        }

        return ret;
    }

    public void valueChanged(TreeSelectionEvent e) {
        updatePageAdvanceState();
    }

    private void updatePageAdvanceState() {
        if (getUserSelection() == null) {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, null, "");
        } else {
            firePropertyChange(ExtensibilityElementConfigurationEditorComponent.PROPERTY_CLEAR_MESSAGES_EVT, null, "");
        }
    }

//    @Override
//    public void buildWSDL() throws javax.wsdl.WSDLException {
//
//        try {
//            File wsdlFile = generateWSDL();
//            assert (wsdlFile != null);
//
//            // parse the WSDL file and get the sole operation
//            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
//            String wsdlURI = wsdlFile.toURI().toString();
//            Definition def = wsdlReader.readWSDL(wsdlURI);
//            Map portTypes = def.getPortTypes();
//            PortType portType = (PortType) portTypes.values().iterator().next();
//            List operations = portType.getOperations();
//            Operation operation = (Operation) operations.get(0);
//            String operationName = operation.getName();
//            m_wcontext._write_request_porttype = portType;
//            m_wcontext.setOperationName(operationName);
//
//            // add import
//            Definition def1 = m_wcontext.getWriteDefinition();
//            Import imp = def1.createImport();
//            imp.setLocationURI(wsdlFile.getName());
//            imp.setNamespaceURI(portType.getQName().getNamespaceURI());
//            def1.addImport(imp);
//
//            // add PLT
//            PartnerLinkType plt = (PartnerLinkType) m_wcontext._extReg.createExtension(
//                    PartnerLinkType.PARENT_CLASS,
//                    PartnerLinkType.FIELD_ELEMENT_TYPE);
//            def1.addExtensibilityElement(plt);
//            plt.setContext(m_wcontext);
//
//            String portTypeName = portType.getQName().getLocalPart();
//            String pltName = portTypeName;
//            if (pltName.endsWith(m_wcontext.PortType_suffix)) {
//                pltName = pltName.substring(0, pltName.length() - m_wcontext.PortType_suffix.length());
//            }
//            m_wcontext._partnerLinkType_name = pltName + m_wcontext.PartnerLinkType_suffix; //operationName
//            plt.setPartnerLinkTypeName(m_wcontext._partnerLinkType_name);
//
//            m_wcontext._role_name = operationName + m_wcontext.Role_suffix;
//            plt.setRoleName(m_wcontext._role_name);
//
//            def1.addNamespace("impl", portType.getQName().getNamespaceURI());
//            plt.setPortNSPrefix("impl");
//            plt.setPortName(portTypeName);
//
//        } catch (JCoException ex) {
//            Logger.getLogger(SapObjectSelectionPage.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (WSDLException ex) {
//            Logger.getLogger(SapObjectSelectionPage.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    /**
//     * Gets the TreeModel for the hierarchical view.
//     *
//     * @param rootObject    root BAPI object
//     * @return              the TreeModel for the hierarchical view
//     */
//    private static void populateBapiHierarchicalTreeNode(
//            DefaultMutableTreeNode bapiRootTreeNode,
//            BAPIObject rootObject) {
//
//        bapiRootTreeNode.setUserObject(rootObject);
//        populate(bapiRootTreeNode);
//    }
    private static void populateHierarchicalBOTree(DefaultMutableTreeNode parentTreeNode) {
        BAPIObject parentObject = (BAPIObject) parentTreeNode.getUserObject();
        List<BAPIObject> childObjects = parentObject.getChildren();
        if (childObjects != null) {
            
            Collections.sort(childObjects, new Comparator<BAPIObject>() {
                public int compare(BAPIObject o1, BAPIObject o2) {
                    boolean isLeaf1 = o1.isLeaf();
                    boolean isLeaf2 = o2.isLeaf();

                    if (isLeaf1 == isLeaf2) {
                        if (isLeaf1) {
                            return o1.getExtName().compareToIgnoreCase(o2.getExtName());
                        } else {
                            return o1.getShortText().compareToIgnoreCase(o2.getShortText());
                        }
                    } else if (isLeaf1) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

            for (BAPIObject childObject : childObjects) {
                DefaultMutableTreeNode childTreeNode =
                        new DefaultMutableTreeNode(childObject);
                parentTreeNode.add(childTreeNode);
                populateHierarchicalBOTree(childTreeNode);
            }
            assert parentObject.getMethods() == null;
        } else {
            List<BAPIMethod> methods = parentObject.getMethods();
            assert methods != null; // BO w/o methods have been filtered out.
            for (BAPIMethod method : methods) {
                DefaultMutableTreeNode methodTreeNode =
                        new DefaultMutableTreeNode(method);
                parentTreeNode.add(methodTreeNode);
            }
        }
    }

    private static void populateHierarchicalRFCTree(
            DefaultMutableTreeNode rfcRootNode,
            List<RFC> rfcs) {

        // group nodes are ordered
        Map<String, DefaultMutableTreeNode> group2Node =
                new TreeMap<String, DefaultMutableTreeNode>();

        for (RFC rfc : rfcs) {
            String groupName = rfc.getGroupName();
            DefaultMutableTreeNode groupNode = group2Node.get(groupName);
            if (groupNode == null) {
                groupNode = new DefaultMutableTreeNode(groupName);
                group2Node.put(groupName, groupNode);
                rfcRootNode.add(groupNode);
            }
            DefaultMutableTreeNode rfcTreeNode = new DefaultMutableTreeNode(rfc);
            groupNode.add(rfcTreeNode);
        }
    }

//    private static void populateHierarchicalRFCTree(
//            DefaultMutableTreeNode iDocRootNode,
//            List<RFC> rfcs) {
//
//        for (RFC rfc : rfcs) {
//            DefaultMutableTreeNode rfcTreeNode = new DefaultMutableTreeNode(rfc);
//            iDocRootNode.add(rfcTreeNode);
//        }
//    }

    private static void populateAlphabeticalRFCTree(
            DefaultMutableTreeNode rfcRootNode,
            List<RFC> rfcs) {

        Map<Character, DefaultMutableTreeNode> char2Node =
                new TreeMap<Character, DefaultMutableTreeNode>();

        Collections.sort(rfcs, new Comparator<RFC>() {
            public int compare(RFC o1, RFC o2) {
                // function name is used in SAP Browser for RFCs
                return o1.getFunctionName().compareToIgnoreCase(o2.getFunctionName());
            }
        });

        for (RFC rfc : rfcs) {
            char ch = rfc.getFunctionName().charAt(0);
            DefaultMutableTreeNode charNode = char2Node.get(ch);
            if (charNode == null) {
                charNode = new DefaultMutableTreeNode(ch);
                char2Node.put(ch, charNode);
            }
            DefaultMutableTreeNode rfcTreeNode = new DefaultMutableTreeNode(rfc);
            charNode.add(rfcTreeNode);
        }

        for (Character ch : char2Node.keySet()) {
            DefaultMutableTreeNode charNode = char2Node.get(ch);
            rfcRootNode.add(charNode);
        }
    }

    private static void populateHierarchicalIDocTree(
            DefaultMutableTreeNode iDocRootNode,
            List<IDocType> iDocTypes) {

        for (IDocType iDocType : iDocTypes) {
            DefaultMutableTreeNode iDocTypeTreeNode =
                    new DefaultMutableTreeNode(iDocType);
            iDocRootNode.add(iDocTypeTreeNode);
        }
    }

    private static void populateAlphabeticalIDocTree(
            DefaultMutableTreeNode iDocRootNode,
            List<IDocType> iDocTypes) {

        Map<Character, DefaultMutableTreeNode> char2TreeNode =
                new HashMap<Character, DefaultMutableTreeNode>();

        Collections.sort(iDocTypes, new Comparator<IDocType>() {
            public int compare(IDocType o1, IDocType o2) {
                // name is used in SAP Browser for IDocs
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        for (IDocType iDocType : iDocTypes) {
            char ch = iDocType.getName().charAt(0);
            DefaultMutableTreeNode charTreeNode = char2TreeNode.get(ch);
            if (charTreeNode == null) {
                charTreeNode = new DefaultMutableTreeNode(ch);
                char2TreeNode.put(ch, charTreeNode);
                iDocRootNode.add(charTreeNode);
            }
            DefaultMutableTreeNode iDocTypeTreeNode =
                    new DefaultMutableTreeNode(iDocType);
            charTreeNode.add(iDocTypeTreeNode);
        }
    }

    /**
     * Gets the TreeModel for the alphabetical view.
     *
     * @param rootObject    root BAPI object
     */
    private static void populateAlphabeticalBOTree(
            DefaultMutableTreeNode bapiTreeNode,
            BAPIObject rootObject) {

        Map<Character, List<BAPIObject>> char2LeafBOs =
                new HashMap<Character, List<BAPIObject>>();

        getBOLeafs(rootObject, char2LeafBOs);

        for (char c = 'A'; c <= 'Z'; c++) {
            List<BAPIObject> objectList = char2LeafBOs.get(c);
            if (objectList != null) {
                DefaultMutableTreeNode charTreeNode = new DefaultMutableTreeNode(c);
                bapiTreeNode.add(charTreeNode);

                Collections.sort(objectList, new Comparator<BAPIObject>() {
                    public int compare(BAPIObject o1, BAPIObject o2) {
                        // ext name is used in SAP Browser for leaf BOs
                        return o1.getExtName().compareToIgnoreCase(o2.getExtName());
                    }                    
                });

                for (BAPIObject object : objectList) {
                    DefaultMutableTreeNode objectTreeNode =
                            new DefaultMutableTreeNode(object);
                    charTreeNode.add(objectTreeNode);

                    List<BAPIMethod> methods = object.getMethods();
                    //assert methods != null;
                    if (methods == null) {
                        System.out.println("!!!??? FIXME: " + object.getName() + " whose method list is null should have already been removed.");
                    } else {
                        for (BAPIMethod method : methods) {
                            DefaultMutableTreeNode methodTreeNode =
                                    new DefaultMutableTreeNode(method);
                            objectTreeNode.add(methodTreeNode);
                        }
                    }
                }
            }
        }
    }

    private static void getBOLeafs(BAPIObject object,
            Map<Character, List<BAPIObject>> char2LeafBOs) {

        if (object.isLeaf()) {
            String shortText = object.getShortText();
            if (shortText != null && shortText.length() > 0) {
                char firstChar = object.getExtName().charAt(0);
                List<BAPIObject> objectList = char2LeafBOs.get(firstChar);
                if (objectList == null) {
                    objectList = new ArrayList<BAPIObject>();
                    char2LeafBOs.put(firstChar, objectList);
                }
                objectList.add(object);
            } else {
//                System.out.println("WARNING: object has an empty short text and can't be alphabetized: " + object.getName());
            }
            //System.out.println("Building char2Objects: " + firstChar + " => " + object.getShortText());
        } else {
            for (BAPIObject child : object.getChildren()) {
                getBOLeafs(child, char2LeafBOs);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        _lblDescription = new javax.swing.JLabel();
        _tabbedPane = new javax.swing.JTabbedPane();
        _panelHierarchicalTree = new javax.swing.JPanel();
        _spHierarchicalTree = new javax.swing.JScrollPane();
        _trHierarchicalTree = new javax.swing.JTree();
        _panelAlphabeticalTree = new javax.swing.JPanel();
        _spAlphabeticalTree = new javax.swing.JScrollPane();
        _trAlphabeticalTree = new javax.swing.JTree();

        _lblDescription.setText("Select a BAPI, RFC, or IDoc to retrieve and describe.");

        _trHierarchicalTree.setModel(null);
        _spHierarchicalTree.setViewportView(_trHierarchicalTree);

        org.jdesktop.layout.GroupLayout _panelHierarchicalTreeLayout = new org.jdesktop.layout.GroupLayout(_panelHierarchicalTree);
        _panelHierarchicalTree.setLayout(_panelHierarchicalTreeLayout);
        _panelHierarchicalTreeLayout.setHorizontalGroup(
            _panelHierarchicalTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(_spHierarchicalTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );
        _panelHierarchicalTreeLayout.setVerticalGroup(
            _panelHierarchicalTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(_spHierarchicalTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
        );

        _tabbedPane.addTab("Hierarchical", _panelHierarchicalTree);

        _trAlphabeticalTree.setModel(null);
        _spAlphabeticalTree.setViewportView(_trAlphabeticalTree);

        org.jdesktop.layout.GroupLayout _panelAlphabeticalTreeLayout = new org.jdesktop.layout.GroupLayout(_panelAlphabeticalTree);
        _panelAlphabeticalTree.setLayout(_panelAlphabeticalTreeLayout);
        _panelAlphabeticalTreeLayout.setHorizontalGroup(
            _panelAlphabeticalTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(_spAlphabeticalTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
        );
        _panelAlphabeticalTreeLayout.setVerticalGroup(
            _panelAlphabeticalTreeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(_spAlphabeticalTree, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
        );

        _tabbedPane.addTab("Alphabetical", _panelAlphabeticalTree);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(_tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                    .add(_lblDescription))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(_lblDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(_tabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel _lblDescription;
    private javax.swing.JPanel _panelAlphabeticalTree;
    private javax.swing.JPanel _panelHierarchicalTree;
    private javax.swing.JScrollPane _spAlphabeticalTree;
    private javax.swing.JScrollPane _spHierarchicalTree;
    private javax.swing.JTabbedPane _tabbedPane;
    private javax.swing.JTree _trAlphabeticalTree;
    private javax.swing.JTree _trHierarchicalTree;
    // End of variables declaration//GEN-END:variables

}


class MyTreeCellRenderer extends DefaultTreeCellRenderer {

    private Icon boIcon = new ImageIcon(getClass().getResource("resources/bo.png"));
    private Icon bomIcon = new ImageIcon(getClass().getResource("resources/bom.png"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value,
                selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();
        if (userObject instanceof BAPIObject) {
            if (((BAPIObject) userObject).isLeaf()) {
                setIcon(boIcon);
                setText(((BAPIObject) userObject).getExtName());
            } else {
                setText(((BAPIObject) userObject).getShortText());
            }
        } else if (userObject instanceof BAPIMethod) {
            setIcon(bomIcon);
            setText(((BAPIMethod) userObject).getMethodName());
        } else if (userObject instanceof RFC) {
            RFC rfc = (RFC) userObject;
            setText(rfc.getFunctionName() + " -- " + rfc.getShortText());
        } else if (userObject instanceof IDocType) {
            IDocType iDocType = (IDocType) userObject;
            setText(iDocType.getName() + " -- " + iDocType.getDescription());
        } else {
            setText(value.toString());
        }

        return this;
    }
}
