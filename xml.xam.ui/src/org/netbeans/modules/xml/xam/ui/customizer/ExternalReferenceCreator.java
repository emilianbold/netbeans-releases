/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.ModelCookie;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.ui.XAMUtils;

/**
 * Base class for external reference creators. Unlike a customizer, a
 * creator may create mulitple new components at a time.
 *
 * @author  Ajit Bhate
 * @author  Nathan Fiedler
 */
public abstract class ExternalReferenceCreator<T extends Component>
        extends AbstractReferenceCustomizer<T>
        implements ExplorerManager.Provider, PropertyChangeListener {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Map of registered nodes, keyed by their representative DataObject. */
    private Map<DataObject, NodeSet> registeredNodes;
    /** The file being modified (where the import will be added). */
    private transient FileObject sourceFO;
    /** Used to deal with project catalogs. */
    private transient DefaultProjectCatalogSupport myCatalogSupport;

    /**
     * Creates new form ExternalReferenceCreator
     *
     * @param  component  component in which to create new components.
     * @param  model      model in which to create components.
     */
    public ExternalReferenceCreator(T component, Model model) {
        super(component);
        registeredNodes = new HashMap<DataObject, NodeSet>();
        initComponents();
        sourceFO = (FileObject) component.getModel().getModelSource().getLookup().
             lookup(FileObject.class);
        myCatalogSupport = DefaultProjectCatalogSupport.getInstance(sourceFO);
        init(component, model);
        // View for selecting an external reference.
        TreeTableView locationView = new LocationView();
        locationView.setDefaultActionAllowed(false);
        locationView.setPopupAllowed(false);
        locationView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        locationView.setRootVisible(false);
        locationView.getAccessibleContext().setAccessibleName(locationLabel.getToolTipText());
        locationView.getAccessibleContext().setAccessibleDescription(locationLabel.getToolTipText());
        Node.Property[] columns = new Node.Property[] {
            new Column(ExternalReferenceDataNode.PROP_NAME, String.class, true),
            new ImportColumn(referenceTypeName()),
            new Column(ExternalReferenceDataNode.PROP_PREFIX, String.class, false),
        };
        locationView.setProperties(columns);
        locationView.setTreePreferredWidth(200);
        locationView.setTableColumnPreferredWidth(0, 25);
        locationView.setTableColumnPreferredWidth(1, 25);
        locationPanel.add(locationView, BorderLayout.CENTER);
        explorerManager = new ExplorerManager();
        explorerManager.addPropertyChangeListener(this);
        explorerManager.setRootContext(createRootNode());
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ExternalReferenceCreator.class,"DSC_ExternalReferenceCreator"));
    }

    public void applyChanges() throws IOException {
// # 174959
//        List<Node> nodes = getSelectedNodes();
//    
//        for (Node node : nodes) {
//            if ( !(node instanceof ExternalReferenceNode)) {
//                continue;
//            }
//            Model model = ((ExternalReferenceNode) node).getModel();
//
//            if (model == null || model == getModelComponent().getModel()) {
//                continue;
//            }
//            FileObject file = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
//
//            if (file == null) {
//                continue;
//            }
//            try {
//                if (myCatalogSupport != null && myCatalogSupport.needsCatalogEntry(sourceFO, file)) {
//                    URI uri = myCatalogSupport.getReferenceURI(sourceFO, file);
//                    myCatalogSupport.removeCatalogEntry(uri);
//                    myCatalogSupport.createCatalogEntry(sourceFO, file);
//                }
//            }
//            catch (URISyntaxException e) {
//                ErrorManager.getDefault().notify(e);
//            }
//            catch (IOException e) {
//                ErrorManager.getDefault().notify(e);
//            }
//            catch (CatalogModelException e) {
//                ErrorManager.getDefault().notify(e);
//            }
//      }
    }

    /**
     * Return the target namespace of the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  target namespace, or null if none.
     */
    protected abstract String getTargetNamespace(Model model);

    /**
     * Retrieve the list of nodes that the user selected.
     *
     * @return  list of selected nodes (empty if none).
     */
    protected List<Node> getSelectedNodes() {
        List<Node> results = new LinkedList<Node>();
        Collection<NodeSet> sets = registeredNodes.values();
        for (NodeSet set : sets) {
            if (set.isSelected()) {
                List<ExternalReferenceDataNode> nodes = set.getNodes();
                if (nodes.size() > 0) {
                    // Use just one of the corresponding nodes, as the
                    // others are basically duplicates.
                    results.add(nodes.get(0));
                }
            }
        }
        return results;
    }

    /**
     * Check if prefix is unique on UI.
     *
     * @return  true if Prefix is not unique on UI, false otherwise.
     */
    private boolean isValidPrefix(ExternalReferenceDataNode node) {
        DataObject dobj = (DataObject) node.getLookup().lookup(DataObject.class);
        NodeSet nodeSet = registeredNodes.get(dobj);
        Collection<NodeSet> sets = registeredNodes.values();
        for (NodeSet set : sets) {
            // Ignore the set which contains the given node, and those
            // sets which are not selected.
            if (!set.equals(nodeSet) && set.isSelected()) {
                // Only need to check the first node, as all of them have
                // the same prefix (or at least that is the idea).
                ExternalReferenceDataNode other = set.getNodes().get(0);
                if (node.getPrefix().equals(other.getPrefix())) {
                    // Additionally check that the namespaces are different.
                    // It is allowed to use the same prefix if the namespace is the same.
                    String ns1 = node.getNamespace();
                    String ns2 = other.getNamespace();
                    if (!XAMUtils.equal(ns1, ns2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determine the number of nodes that the user selected, useful for
     * knowing if any nodes are selected or not.
     *
     * @return  number of selected nodes.
     */
    private int countSelectedNodes() {
        int results = 0;
        Collection<NodeSet> sets = registeredNodes.values();
        for (NodeSet set : sets) {
            if (set.isSelected()) {
                List<ExternalReferenceDataNode> nodes = set.getNodes();
                if (nodes.size() > 0) {
                    results++;
                }
            }
        }
        return results;
    }

    /**
     * Return the existing external reference prefixes for the given model.
     *
     * @param  model  the model for which to get the namespace.
     * @return  set of prefixes; empty if none.
     */
    protected abstract Map<String, String> getPrefixes(Model model);

    /**
     * Returns the NodeDecorator for this customizer, if any.
     *
     * @return  node decorator for files nodes, or null if none.
     */
    protected abstract ExternalReferenceDecorator getNodeDecorator();

    /**
     * Indicates if the namespace value must be different than that of
     * the model containing the component being customized. If false,
     * then the opposite must hold - the namespace must be the same.
     * The one exception is if the namespace is not defined at all.
     *
     * @return  true if namespace must differ, false if same.
     */
    public abstract boolean mustNamespaceDiffer();

    /**
     * Return the proper name of the type of reference this creator is to
     * be used for. This will become the title of the first column in the
     * tree-table. Generally this should be something of the form of
     * "Import", "Include", "Redefine", etc.
     *
     * @return  human-readable name for the first column title.
     */
    protected abstract String referenceTypeName();

    /**
     * Called from constructor, after the interface components have been
     * constructed, but before they have been initialized. Gives subclasses
     * a chance to perform initialization based on the given component.
     *
     * @param  component  the reference to be customized.
     * @param  model      the model passed to the constructor (may be null).
     */
    protected void init(T component, Model model) {
        // Note, do not place any code here, as there is no guarantee
        // that the subclasses will delegate to this method at all.
    }

    protected void initializeUI() {
        if (!mustNamespaceDiffer()) {
            namespaceLabel.setVisible(false);
            namespaceTextField.setVisible(false);
        }
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
        DataObject dobj = (DataObject) original.getLookup().lookup(DataObject.class);
        NodeSet set = registeredNodes.get(dobj);
        if (set == null) {
            set = new NodeSet(this);
            registeredNodes.put(dobj, set);
        }
        ExternalReferenceDataNode erdn = new ExternalReferenceDataNode(
                original, getNodeDecorator());
        set.add(erdn);
        if (set.isSelected() && erdn.canSelect()) {
            erdn.setSelected(true);
        }
        erdn.addPropertyChangeListener(this);
        return erdn;
    }

    /**
     * Indicate if this creator allows the user to select no files at all.
     * This is useful from the new file wizard which needs to allow the
     * user to deselect what they had previously selected.
     *
     * @return  true if user may select no files and still be considered
     *          as valid input, false to require one or more selections.
     */
    protected boolean allowEmptySelection() {
        // By default we require the user to select at least one file.
        return false;
    }

    /**
     * Determine if the user's input is valid or not. This will enable
     * or disable the save/reset controls based on the results, as well
     * as issue error messages.
     *
     * @param  node  selected node.
     */
    protected void validateInput(ExternalReferenceNode node) {
        String msg = null;
        if (mustNamespaceDiffer() && node instanceof ExternalReferenceDataNode) {
            ExternalReferenceDataNode erdn = (ExternalReferenceDataNode) node;
            Map<String, String> prefixMap = getPrefixes(getModelComponent().getModel());
            String ep = erdn.getPrefix();
            String nodeNs = erdn.getNamespace();
            // Skip this check if namespace is not specified.
            if (nodeNs != null && nodeNs.length() != 0) {
                //
                // Must be a non-empty prefix, that is not already in use, and
                // is unique among the selected nodes (and be selected itself).
                //
                String registeredNs = prefixMap.get(ep);
                boolean prefixRegistered = registeredNs != null;
                boolean registeredPrefixIncorrect = !nodeNs.equals(registeredNs);
                //
                if (ep.length() == 0 || 
                        (prefixRegistered && registeredPrefixIncorrect) ||
                        (!isValidPrefix(erdn) && erdn.isSelected())) {
                    msg = NbBundle.getMessage(ExternalReferenceCreator.class,
                            "LBL_ExternalReferenceCreator_InvalidPrefix");
                }
            }
        }
        if (node instanceof RetrievedFilesChildren.RetrievedFileNode) {
            RetrievedFilesChildren.RetrievedFileNode rNode =
                    (RetrievedFilesChildren.RetrievedFileNode) node;
            if (!rNode.isValid()) {
                msg = NbBundle.getMessage(ExternalReferenceCreator.class,
                        "LBL_ExternalReferenceCreator_InvalidCatalogEntry");
            }
        }
        if (msg != null) {
            showMessage(msg);
        }
        int selected = countSelectedNodes();
        // Must have selected nodes, and no error messages.
        setSaveEnabled((allowEmptySelection() || selected > 0) && msg == null);
    }

    protected void showMessage(String msg) {
        if (msg == null) {
            messageLabel.setText(" ");
            messageLabel.setIcon(null);
        } else {
            messageLabel.setText(msg);
            messageLabel.setIcon(ImageUtilities.loadImageIcon(
                "org/netbeans/modules/xml/xam/ui/resources/error.gif", false)); // NOI18N
        }
    }

    /**
     * A TreeTableView that toggles the selection of the external reference
     * data nodes using a single mouse click.
     */
    private class LocationView extends TreeTableView {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of LocationView.
         */
        public LocationView() {
            super();
            tree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Invert the selection of the data node, if such a
                    // node was clicked on.
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        Object comp = path.getLastPathComponent();
                        Node node = Visualizer.findNode(comp);
                        if (node instanceof ExternalReferenceDataNode) {
                            ExternalReferenceDataNode erdn =
                                    (ExternalReferenceDataNode) node;
                            if (erdn.canSelect()) {
                                boolean selected = !erdn.isSelected();
                                String ns = null;
                                if (selected) {
                                    // Have to collect the namespace value
                                    // when the node is selected.
                                    Model model = erdn.getModel();
                                    if (model != null) {
                                        ns = getTargetNamespace(model);
                                    }
                                }
                                // This will clear the field if the user has
                                // deselected the file, which is to prevent
                                // the user from being confused as to what
                                // the namespace field represents.
                                namespaceTextField.setText(ns);
                                erdn.setSelected(selected);
                            }
                        }
                    }
                }
            });
        }
    }

    protected Node createRootNode() {
        Set/*<Project>*/ refProjects = null;
        if (myCatalogSupport != null && myCatalogSupport.supportsCrossProject()) {
            refProjects = myCatalogSupport.getProjectReferences();
        }
        ExternalReferenceDecorator decorator = getNodeDecorator();
        Node[] rootNodes = new Node[0];
        Project prj = FileOwnerQuery.getOwner(sourceFO);
        if (prj == null) {
            showMessage(NbBundle.getMessage(ExternalReferenceCreator.class,
                "LBL_ProjectInaccessible"));
            // set empty root.
            Node rootNode = new AbstractNode(Children.LEAF);
            return rootNode;
        } else {
            LogicalViewProvider viewProvider = (LogicalViewProvider) prj.getLookup().
                    lookup(LogicalViewProvider.class);
            rootNodes = new Node[1 + (refProjects == null ? 0 : refProjects.size())];
            rootNodes[0] = decorator.createExternalReferenceNode(
                    viewProvider.createLogicalView());
        }
        int rootIndex = 1;
        List<FileObject> projectRoots = new ArrayList<FileObject>();

        if (prj != null) {
            projectRoots.add(prj.getProjectDirectory());
        }
        if (refProjects != null) {
            for (Object o : refProjects) {
                Project refPrj = (Project) o;
                LogicalViewProvider viewProvider = (LogicalViewProvider) refPrj.getLookup().
                        lookup(LogicalViewProvider.class);
                rootNodes[rootIndex++] = decorator.createExternalReferenceNode(
                        viewProvider.createLogicalView());
                projectRoots.add(refPrj.getProjectDirectory());
            }
        }
        FileObject[] roots = projectRoots.toArray(
                new FileObject[projectRoots.size()]);
        Children fileChildren = new Children.Array();

        if (rootNodes != null) {
            fileChildren.add(rootNodes);
        }
        Node byFilesNode = new FolderNode(fileChildren);
        byFilesNode.setDisplayName(NbBundle.getMessage(
                ExternalReferenceCreator.class,
                "LBL_ExternalReferenceCreator_Category_By_File"));

        // Construct the By Namespace node.
        Children nsChildren = new NamespaceChildren(roots, decorator);
        Node byNsNode = new FolderNode(nsChildren);
        byNsNode.setDisplayName(NbBundle.getMessage(
                ExternalReferenceCreator.class,
                "LBL_ExternalReferenceCreator_Category_By_Namespace"));
// Hide the Retrieved node tree until we are sure the runtime can handle
// URLs with respect to the catalog.
//        Node retrievedNode;
//        CatalogWriteModel cwm = getCatalogWriteModel();
//        if (cwm != null) {
//            Children rChildren = new RetrievedFilesChildren(cwm , decorator);
//            retrievedNode = new ExternalReferenceNode(projectNode, rChildren, decorator);
//        } else {
//            retrievedNode = new ExternalReferenceNode(projectNode, Children.LEAF, decorator);
//        }
//        retrievedNode.setDisplayName(NbBundle.getMessage(
//                ExternalReferenceCreator.class,
//                "LBL_ExternalReferenceCreator_Category_By_Retrieved"));
        Children categories = new Children.Array();
//        categories.add(new Node[]{ byFilesNode, byNsNode, retrievedNode });
        categories.add(new Node[] { byFilesNode, byNsNode });
        Node rootNode = new AbstractNode(categories);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(ExternalReferenceCreator.class,
                "CTL_ExternalReferenceCreator_Column_Name_name"));
        rootNode.setShortDescription(NbBundle.getMessage(ExternalReferenceCreator.class,
                "CTL_ExternalReferenceCreator_Column_Desc_name"));
        return rootNode;
    }

//    private CatalogWriteModel getCatalogWriteModel() {
//        try {
//            FileObject myFobj = (FileObject) getModelComponent().getModel().
//                    getModelSource().getLookup().lookup(FileObject.class);
//            CatalogWriteModel cwm = CatalogWriteModelFactory.getInstance().
//                    getCatalogWriteModelForProject(myFobj);
//            return cwm;
//        } catch (CatalogModelException cme) {
//        }
//        return null;
//    }

    public void propertyChange(PropertyChangeEvent event) {
        String pname = event.getPropertyName();
        if (ExplorerManager.PROP_SELECTED_NODES.equals(pname)) {
            showMessage(null);
            Node[] nodes = (Node[]) event.getNewValue();
            // Validate the node selection.
            if (nodes != null && nodes.length > 0 &&
                    nodes[0] instanceof ExternalReferenceNode) {
                ExternalReferenceNode node = (ExternalReferenceNode) nodes[0];
                Model model = node.getModel();
                // Without a model, the selection is completely invalid.
                if (model != null) {
                    // Ask decorator if selection is valid or not.
                    String msg = getNodeDecorator().validate(node);
                    if (msg != null) {
                        showMessage(msg);
                    } else {
                        // If node is okay, validate the rest of the input.
                        validateInput(node);
                    }
                }
            }
        } else if (pname.equals(ExternalReferenceDataNode.PROP_PREFIX)) {
            ExternalReferenceDataNode erdn =
                    (ExternalReferenceDataNode) event.getSource();
            // Look up the node in the map of sets, and ensure they all
            // have the same prefix.
            String prefix = (String) event.getNewValue();
            DataObject dobj = (DataObject) erdn.getLookup().lookup(DataObject.class);
            NodeSet set = registeredNodes.get(dobj);
            // Ideally the set should already exist, but cope gracefully.
            assert set != null : "node not created by customizer";
            if (set == null) {
                set = new NodeSet(this);
                set.add(erdn);
            }
            set.setPrefix(prefix);
            validateInput(erdn);
        } else if (pname.equals(ExternalReferenceDataNode.PROP_SELECTED)) {
            ExternalReferenceDataNode erdn =
                    (ExternalReferenceDataNode) event.getSource();
            // Look up the node in the map of sets, and ensure they are all
            // selected as a unit.
            boolean selected = ((Boolean) event.getNewValue()).booleanValue();
            DataObject dobj = (DataObject) erdn.getLookup().lookup(DataObject.class);
            NodeSet set = registeredNodes.get(dobj);
            // Ideally the set should already exist, but cope gracefully.
            assert set != null : "node not created by customizer";
            if (set == null) {
                set = new NodeSet(this);
                set.add(erdn);
            }
            String ns = null;
            if (selected) {
                // Have to collect the namespace value
                // when the node is selected.
                Model model = erdn.getModel();
                if (model != null) {
                    ns = getTargetNamespace(model);
                }
            }
            // This will clear the field if the user has
            // deselected the file, which is to prevent
            // the user from being confused as to what
            // the namespace field represents.
            namespaceTextField.setText(ns);
            set.setSelected(selected);
            // Check if the current selection is valid.
            validateInput(erdn);
        }
    }

    /**
     * Get the URI location for the given node.
     *
     * @param  node  Node from which to retrieve location value.
     * @return  location for given Node, or null.
     */
    protected String getLocation(Node node) {
        String location = null;
        if (node instanceof RetrievedFilesChildren.RetrievedFileNode) {
            RetrievedFilesChildren.RetrievedFileNode rNode =
                    (RetrievedFilesChildren.RetrievedFileNode) node;
            if (rNode.isValid()) {
                String ns = rNode.getNamespace();
                if (ns == null || mustNamespaceDiffer() !=
                        ns.equals(getTargetNamespace())) {
                    location = rNode.getLocation();
                }
            }
        } else {
            DataObject dobj = (DataObject) node.getLookup().
                    lookup(DataObject.class);
            if (dobj != null && dobj.isValid()) {
                FileObject file = dobj.getPrimaryFile();
                ModelCookie cookie = (ModelCookie) dobj.getCookie(ModelCookie.class);
                Model model;

                try {
                    if (cookie != null && (model = cookie.getModel()) !=
                            getModelComponent().getModel()) {
                        String ns = getTargetNamespace(model);
                
                        if (ns == null || mustNamespaceDiffer() != 
                                ns.equals(getTargetNamespace())) {
                            // # 174959
                            location = ReferenceUtil.getLocation(ReferenceUtil.getProject(sourceFO).getProjectDirectory(), file);
                            // # 177775
                            if (location == null && myCatalogSupport != null) {
                                location = myCatalogSupport.getReferenceURI(sourceFO, file).toString();
                            }
                            return location;
                        }
                    }
                }
                catch (URISyntaxException urise) {
                    ErrorManager.getDefault().notify(urise);
                }
                catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
        }
        if (location != null) {
            try {
                URI uri = new URI("file", location, null); // NOI18N
                uri = uri.normalize();
                location = uri.getRawSchemeSpecificPart();
            } catch (URISyntaxException use) {
                showMessage(use.toString());
                // Despite this, we can still use the location we have.
            }
        }
        return location;
    }

    /**
     * Get the namespace for the given node.
     *
     * @param  node  Node from which to retrieve namespace value.
     * @return  namespace for given Node, or null.
     */
    protected String getNamespace(Node node) {
        String ns = null;
        if (node instanceof RetrievedFilesChildren.RetrievedFileNode) {
            RetrievedFilesChildren.RetrievedFileNode rNode =
                    (RetrievedFilesChildren.RetrievedFileNode) node;
            if (!rNode.isValid()) {
                return null;
            }
            ns = rNode.getNamespace();
        } else {
            DataObject dobj = (DataObject) node.getLookup().
                    lookup(DataObject.class);
            if (dobj != null && dobj.isValid()) {
                ModelCookie cookie = (ModelCookie) dobj.getCookie(
                        ModelCookie.class);
                Model model;
                try {
                    if (cookie != null && (model = cookie.getModel()) !=
                            getModelComponent().getModel()) {
                        ns = getTargetNamespace(model);
                    }
                } catch (IOException ioe) {
                    // Fall through and return null.
                }
            }
        }
        return ns;
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * A column for the reference customizer table.
     *
     * @author  Nathan Fiedler
     */
    protected class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key   keyword for this column.
         * @param  type  type of the property (e.g. String.class).
         * @param  tree  true if this is the 'tree' column.
         */
        public Column(String key, Class type, boolean tree) {
            super(key, type,
                  NbBundle.getMessage(Column.class,
                    "CTL_ExternalReferenceCreator_Column_Name_" + key),
                  NbBundle.getMessage(Column.class,
                    "CTL_ExternalReferenceCreator_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Special column for the reference customizer table's import column.
     *
     * @author  Nathan Fiedler
     */
    protected class ImportColumn extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Creates a new instance of ImportColumn.
         *
         * @param  name  the column's name.
         */
        public ImportColumn(String name) {
            super("selected", Boolean.TYPE, name,
                  NbBundle.getMessage(Column.class,
                    "CTL_ExternalReferenceCreator_Column_Desc_selected"));
            this.key = "selected";
            setValue("TreeColumnTTV", Boolean.FALSE);
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Manages the state of a set of nodes.
     */
    private static class NodeSet {
        /** The property change listener for each node. */
        private PropertyChangeListener listener;
        /** Nodes in this set. */
        private List<ExternalReferenceDataNode> nodes;
        /** True if this set is selected, false otherwise. */
        private boolean selected;

        /**
         * Creates a new instance of NodeSet.
         *
         * @param  listener  listens to the Node.
         */
        public NodeSet(PropertyChangeListener listener) {
            this.listener = listener;
        }

        /**
         * Add the given node to this set.
         *
         * @param  node  node to be added to set.
         */
        public void add(ExternalReferenceDataNode node) {
            if (nodes == null) {
                nodes = new LinkedList<ExternalReferenceDataNode>();
            }
            nodes.add(node);
        }

        /**
         * Returns the list of nodes in this set.
         *
         * @return  list of nodes.
         */
        public List<ExternalReferenceDataNode> getNodes() {
            return nodes;
        }

        /**
         * Indicates if this set is selected or not.
         *
         * @return  true if selected, false otherwise.
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Set the prefix for Nodes in this group.
         *
         * @param  prefix  new namespace prefix.
         */
        public void setPrefix(String prefix) {
            for (ExternalReferenceDataNode node : nodes) {
                if (!node.getPrefix().equals(prefix)) {
                    node.removePropertyChangeListener(listener);
                    node.setPrefix(prefix);
                    node.addPropertyChangeListener(listener);
                }
            }
        }

        /**
         * Set this group of Nodes as being selected.
         *
         * @param  select  true to select, false to de-select.
         */
        public void setSelected(boolean select) {
            selected = select;
            for (ExternalReferenceDataNode node : nodes) {
                if (node.canSelect()) {
                    node.removePropertyChangeListener(listener);
                    node.setSelected(select);
                    node.addPropertyChangeListener(listener);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to
     * initializeTypeView the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        locationLabel = new javax.swing.JLabel();
        locationPanel = new javax.swing.JPanel();
        namespaceLabel = new javax.swing.JLabel();
        namespaceTextField = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        locationLabel.setLabelFor(locationPanel);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("LBL_ExternalReferenceCreator_Location"));
        locationLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCreator_Location"));

        locationPanel.setLayout(new java.awt.BorderLayout());

        locationPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        namespaceLabel.setLabelFor(namespaceTextField);
        org.openide.awt.Mnemonics.setLocalizedText(namespaceLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("LBL_ExternalReferenceCreator_Namespace"));
        namespaceLabel.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/xam/ui/customizer/Bundle").getString("TIP_ExternalReferenceCreator_Namespace"));

        namespaceTextField.setEditable(false);

        messageLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(namespaceLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(namespaceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE))
                    .add(locationLabel)
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(locationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(locationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(namespaceLabel)
                    .add(namespaceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel locationLabel;
    public javax.swing.JPanel locationPanel;
    public javax.swing.JLabel messageLabel;
    public javax.swing.JLabel namespaceLabel;
    public javax.swing.JTextField namespaceTextField;
    // End of variables declaration//GEN-END:variables
}
