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

package org.netbeans.modules.xml.schema.refactoring.query.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.refactoring.ui.DisplayInfoVisitor;
import org.netbeans.modules.xml.schema.refactoring.ui.DisplayInfoVisitor.DisplayInfo;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryUtilities;
import org.netbeans.modules.xml.xam.Named;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class WhereUsedExplorer extends JPanel implements ExplorerManager.Provider {
    
    static final long serialVersionUID = 1L;
    protected SchemaModel model;
    protected  ExplorerManager explorerManager;
    protected CustomizerTreeView treeView;
    protected int count;	// node Count, excluding primitives
    protected  Action nodePreferredAction;
    
    public static final String ICON_BASE =
            "org/netbeans/modules/xml/schema/ui/nodes/resources/";
    public static final String COMPLEX_TYPE_IMAGE   = "complextype.png";
    public static final String ELEMENT_IMAGE        = "element.png";
    public static final String SIMPLE_TYPE_IMAGE    = "simpletype.png";
    public static final String PRIMITIVE_TYPE_IMAGE = SIMPLE_TYPE_IMAGE;
    public static final String GROUP_IMAGE          = "complextype.png";
    public static final String ATTRIBUTE_IMAGE      = "attribute.png";
    public static final String ATTRIBUTE_GROUP_IMAGE = "attribute.png";
    
    public static final String SELECTION_CHANGED = "selection-changed"; // for property change event
    
    
    /**
     * @param model                  the Schema Model
     *
     */
    public WhereUsedExplorer(SchemaModel model) {
        this.model = model;
        initialize();
    }
    
    /**
     *
     *
     */
    private void initialize() {
        removeAll();
//        setPreferredSize(new Dimension(150,200));
        setLayout(new BorderLayout());
        explorerManager = new ExplorerManager();
        Node root = createTree();
        
//        nodePreferredAction = new WhereUsedAction(customizer.getColView());
        treeView=new CustomizerTreeView();
        treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeView.setRootVisible(root.getChildren().getNodesCount()>0?false:true);
        treeView.setDefaultActionAllowed(true);
        Object key = "org.openide.actions.PopupAction";
        KeyStroke ks = KeyStroke.getKeyStroke("shift F10");
        treeView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, key);
        
        add(treeView,BorderLayout.CENTER);
        explorerManager.setRootContext(root);
        
        addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e) {
                // Don't expand nodes if scrolling will be needed
                // Nodes for Primitive types are not expanded
                removeComponentListener(this);	// only check once
//				System.out.println("WhereUsedExplorer component listener removed.");
                Dimension d = getSize();
                int rh = treeView.getRowHeight();
                int totalH = rh * count;
                if (d.height > totalH){
                    // expand nodes
                    Node[] catNodes = explorerManager.getRootContext().getChildren().getNodes();
                    for (int i = 0; i < catNodes.length;i++) {
                        if (!catNodes[i].getName().equals(
                                NbBundle.getMessage(WhereUsedExplorer.class,"LBL_Primitives"))){
                            treeView.expandNode(catNodes[i]);
                        }
                    }
                }
            }
        });
    }
    /**
     *
     * change the default action for the QueryCustomizerNode
     * Default is WhereUsedAction
     */
    public void setNodePreferredAction(Action action){
        this.nodePreferredAction = action;
    }
    
    public SchemaComponent getSelectedSchemaComponent() {
        QueryCustomizerNode node = null;
        Node[] selNodes = explorerManager.getSelectedNodes();
        if (selNodes != null && selNodes.length > 0){
            if (!(selNodes[0] instanceof QueryCustomizerNode)){
                return null;
            }
            node = (QueryCustomizerNode)selNodes[0];
            return node.getSchemaComponent();
            
        } else {
            return null;
        }
    }
    
    protected Node findNodeForRef(SchemaComponentReference ref){
        Node node = null;
        Node root = explorerManager.getRootContext();
        SchemaComponent sc = ref.get();
        Named named = null;
        if (sc instanceof Named){
            named = (Named)sc;
        } else{
            return null;
        }
        if (root == null){
            return null;
        }
        Node[] cats = root.getChildren().getNodes();
        if (cats != null){
            for (Node n:cats){
                Children ch = n.getChildren();
                node = ch.findChild(named.getName());
                if (node != null){
                    return node;
                }
            }
        }
        return null;
        
    }
    
    protected Node createTree() {
        count = 0;
        Children.Array categories = new Children.Array();
        AbstractNode root = new AbstractNode(categories);
        // this name is visible in the table header of the navigator
        root.setName(NbBundle.getMessage(WhereUsedExplorer.class,
                "LBL_Global_Comps"));
        
        ArrayList<AbstractNode> catItems = new ArrayList<AbstractNode>();
        
        Schema schema = model.getSchema();
        
        /**  order of categories suggested by Chris Webster
         * GlobalComplexTypes
         * GlobalElements (the ordering of these two really depends on the style of the schema)
         * GlobalSimpleTypes
         * primitives
         * GlobalGroups
         * Attributes
         * Attribute Groups
         */
        
        
        AbstractNode catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.COMPLEX_TYPE);
        count++;
        String icon_str = ICON_BASE + COMPLEX_TYPE_IMAGE;
        catItems.add(catNode);
        for(GlobalComplexType g : schema.getComplexTypes()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ELEMENT);
        count++;
        icon_str = ICON_BASE + ELEMENT_IMAGE;
        catItems.add(catNode);
        for(GlobalElement g : schema.getElements()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.SIMPLE_TYPE);
        count++;
        icon_str = ICON_BASE + SIMPLE_TYPE_IMAGE;
        catItems.add(catNode);
        for(GlobalSimpleType g : schema.getSimpleTypes()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
            
        }
        
        
        //  primitives
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.PRIMITIVE);
        count++;
        icon_str = ICON_BASE + PRIMITIVE_TYPE_IMAGE;
        catItems.add(catNode);
        for(GlobalSimpleType g : SchemaModelFactory.getDefault().
                getPrimitiveTypesModel().getSchema().getSimpleTypes()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str, true);   // primitive
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.GROUP);
        count++;
        icon_str = ICON_BASE + GROUP_IMAGE;
        catItems.add(catNode);
        for(GlobalGroup g : schema.getGroups()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ATTRIBUTE);
        count++;
        icon_str = ICON_BASE + ATTRIBUTE_IMAGE;
        catItems.add(catNode);
        for(GlobalAttribute g : schema.getAttributes()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        
        
        catNode = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.ATTRIBUTE_GROUP);
        count++;
        icon_str = ICON_BASE + ATTRIBUTE_GROUP_IMAGE;
        catItems.add(catNode);
        for(GlobalAttributeGroup g : schema.getAttributeGroups()) {
            ArrayList<QueryCustomizerNode> items = new ArrayList<QueryCustomizerNode>();
            createCustomizerNode(items, g, icon_str);
            catNode.getChildren().add((QueryCustomizerNode[]) items.toArray(new QueryCustomizerNode[items.size()]));
        }
        
        
        
        
        
        categories.add((AbstractNode[])
        catItems.toArray(new AbstractNode[catItems.size()]));
        
        
        
        
        return root;
    }
    
    
    /**
     *
     *
     */
    protected void createCustomizerNode(List<QueryCustomizerNode> items, Named c, String icon_base_with_ext, boolean primitive){
        QueryCustomizerNode n  = null;
        if (primitive){
            n  = new QueryCustomizerNode(Children.LEAF, c, primitive, model);
        } else {
            n  = new QueryCustomizerNode(Children.LEAF, c, primitive);
        }
        
        n.setIconBaseWithExtension(icon_base_with_ext);
        n.setName(c.getName());
        items.add(n);
        if (!primitive){
            count++;
        }
    }
    
    
    /**
     *
     *
     */
    protected void createCustomizerNode(List<QueryCustomizerNode> items, Named c, String icon_base_with_ext){
        createCustomizerNode(items, c, icon_base_with_ext, false);
    }
    
    
    
    
    ///////////////////////////////////////////////////////////
    // ExplorerManager.Provider interface implementation
    ///////////////////////////////////////////////////////////
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    
    
    ///////////////////////////////////////////////////////////
    // Column interface implementation
    ///////////////////////////////////////////////////////////
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(treeView != null)
            treeView.removePropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(treeView != null)
            treeView.addPropertyChangeListener(listener);
    }
    
    public String getTitle() {
        return NbBundle.getMessage(WhereUsedExplorer.class, "LBL_Schema_Queries");
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    public boolean requestFocusInWindow() {        
        return treeView.requestFocusInWindow();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     *
     *
     */
    protected  class CustomizerTreeView extends BeanTreeView {
        static final long serialVersionUID = 1L;
        
//        Timer timer;	// Swing timer
        /**
         *
         *
         */
        public CustomizerTreeView() {
            super();
            
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
        }
        
        public int getRowHeight() {
            return tree.getRowHeight();
        }
        
        public void setSelectedNode(Node node){
            try {
                selectionChanged(new Node[] {node}, explorerManager);
            } catch (PropertyVetoException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        
        @Override
                protected void selectionChanged(Node[] nodes, ExplorerManager eMgr ) throws PropertyVetoException {
            super.selectionChanged(nodes, eMgr);
            if (nodes != null && nodes.length > 0){
                if (nodes[0] instanceof QueryCustomizerNode) {
                    // assume it's valid as long as it's a QueryCustomizerNode and not an AbstractNode (category node)
                    firePropertyChange(SELECTION_CHANGED, null, Boolean.TRUE);
                    return;
                }
            }
            firePropertyChange(SELECTION_CHANGED, null, Boolean.FALSE);
           
        }
        
    }
    
    
    /**
     *
     *
     */
    public  class QueryCustomizerNode extends AbstractNode {
        
        protected String type;
        protected SchemaComponent sc;
        protected boolean isPrimitive;
        protected SchemaModel userModel;        // model for user schema, if primitive
        
        
        public QueryCustomizerNode(Children children, Named sc, boolean primitive){
            this(children, sc, primitive, null);
            
            
        }
        
        
        public QueryCustomizerNode(Children children, Named sc, boolean primitive, SchemaModel userModel){
            super(children);
            this.sc = SchemaComponent.class.cast(sc);
            this.isPrimitive = primitive;
            this.userModel = userModel;
            
            
        }
        
        public SchemaModel getUserSchemaModel(){
            return userModel;
        }
        
        /**
         *
         *
         */
        @Override
                protected Sheet createSheet() {
            Sheet sheet=Sheet.createDefault();
            Sheet.Set set=sheet.get(Sheet.PROPERTIES);
            
            set.put(
                    new PropertySupport("type",String.class,
                    NbBundle.getMessage(WhereUsedExplorer.class,
                    "PROP_SchemaComponentNode_Type"),
                    "",true,false) {
                public Object getValue() {
                    if (isPrimitive){
                        return NbBundle.getMessage(
                                WhereUsedExplorer.class,
                                "LBL_Primitives");
                    } else {
                        DisplayInfoVisitor div = new DisplayInfoVisitor();
                        DisplayInfo info = div.getDisplayInfo(sc);
                        return info.getCompType();
                    }
                }
                
                public void setValue(Object value) {
                    // Not modifiable
                }
            });
            
            return sheet;
        }
        
        /**
         *
         *
         *
         */
        public SchemaComponent getSchemaComponent() {
            return sc;
        }
        
        /**
         *
         *
         */
        public Action getPreferredAction() {
            
            if (nodePreferredAction != null){
                return nodePreferredAction;
            }
            return super.getPreferredAction();
            
        }
        
        public boolean isPrimitive() {
            return isPrimitive;
        }
        
        
    }// end QueryCustomizerNode
    
    
}
