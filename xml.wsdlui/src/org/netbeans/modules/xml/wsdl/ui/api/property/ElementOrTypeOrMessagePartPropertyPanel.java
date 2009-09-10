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

/*
 * Created on May 16, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeOrMessagePartProvider.ParameterType;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.ElementOrTypeChooserPanel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.customizer.FolderNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypeOrMessagePartPropertyPanel extends JPanel {

    private PropertyEnv mEnv;

    private WsdlPartnerLinkTypeTreeView mTreeView;

    private Node mSelectedNode;

    private WSDLModel mModel;

    private ElementOrTypeOrMessagePartProvider mProv;


    public ElementOrTypeOrMessagePartPropertyPanel(ElementOrTypeOrMessagePartProvider prov, PropertyEnv env) {
        this.mProv = prov;
        this.mEnv = env;
        this.mEnv.setState(PropertyEnv.STATE_INVALID);

        this.mModel = mProv.getModel();

        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        ElementOrTypeOrMessagePart elementOrTypeOrMessagePart = mProv.getValue();
        
        this.mTreeView = new WsdlPartnerLinkTypeTreeView(elementOrTypeOrMessagePart);
        this.add(BorderLayout.CENTER, this.mTreeView);
    }

    public Component getSelectedElementOrTypeOrMessagePart() {
        if (mSelectedNode != null) {
            WSDLComponent comp = mSelectedNode.getLookup().lookup(WSDLComponent.class);
            if (comp != null) {
                if (comp instanceof Part) {
                    return comp;
                }
            } else {
                SchemaComponent sc = null;
                SchemaComponentReference reference = mSelectedNode.getLookup().lookup(SchemaComponentReference.class);
                if (reference != null) {
                    sc = reference.get();
                }
                if (sc == null) {
                    sc = mSelectedNode.getLookup().lookup(SchemaComponent.class);
                }

                if (sc != null) {
                    return sc;
                }
            }
        }
        return null;
    }

    private class WsdlPartnerLinkTypeTreeView
            extends JPanel implements ExplorerManager.Provider {

        private BeanTreeView btv;

        private ExplorerManager manager;


        public static final String PROP_VALID_NODE_SELECTED = "PROP_VALID_NODE_SELECTED";//NOI18N

        public static final String PROP_DUPLICATE_NODE_SELECTED = "PROP_DUPLICATE_NODE_SELECTED"; //NOI18N

        public PropertyChangeSupport pChangeSupport = new PropertyChangeSupport(this);

        private Node mRootNode;

        private ElementOrTypeOrMessagePart previousSelection;




        public WsdlPartnerLinkTypeTreeView(ElementOrTypeOrMessagePart elementOrType) {
            previousSelection = elementOrType;
            initGUI();
        }


        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pChangeSupport.removePropertyChangeListener(listener);
        }

        private void initGUI() {
            this.setLayout(new BorderLayout());

            manager = new ExplorerManager();
            manager.addPropertyChangeListener(new ExplorerPropertyChangeListener());

            mRootNode = new AbstractNode(new Children.Array());
            populateRootNode();
            manager.setRootContext( mRootNode );

            
            // Create the templates view
            btv = new BeanTreeView();
            btv.setRootVisible( false );
            btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
            btv.setPopupAllowed( false );
            btv.expandNode(mRootNode);
            btv.setDefaultActionAllowed(false);
            btv.setFocusable(false);
            btv.setAutoscrolls(true);
            btv.setDragSource(false);
            btv.setDropTarget(false);
            btv.setName("beanTreeView1"); // NOI18N
        
        
            Utility.expandNodes(btv, 2, mRootNode);
            manager.setExploredContext(mRootNode);
            this.add(btv, BorderLayout.CENTER);
            btv.setName(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.name")); // NOI18N
            btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.AccessibleContext.accessibleName")); // NOI18N
            btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.AccessibleContext.accessibleDescription")); // NOI18N
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        BeanTreeView getTreeView() {
            return this.btv;
        }

        private void populateRootNode() {
            MessagePartChooserHelper wsdlHelper = new MessagePartChooserHelper(mModel);
            wsdlHelper.populateNodes(mRootNode);
            
            Node elementOrTypeFolderNode = new FolderNode(new Children.Array());
            elementOrTypeFolderNode.setDisplayName(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "LBL_ElementOrType_DisplayName"));
            ElementOrTypeChooserHelper schemaHelper = new ElementOrTypeChooserHelper(mModel);
            schemaHelper.populateNodes(elementOrTypeFolderNode);
            mRootNode.getChildren().add(new Node[] {elementOrTypeFolderNode});
            
            if (previousSelection != null) {
                ParameterType type = previousSelection.getParameterType();
                Node selected = null;
                switch (type) {
                case ELEMENT:
                    selected = schemaHelper.selectNode(previousSelection.getElement());
                    break;
                case TYPE:
                    selected = schemaHelper.selectNode(previousSelection.getType());
                    break;
                case MESSAGEPART:
                    selected = wsdlHelper.selectNode(previousSelection.getMessagePart());
                    break;
                case NONE :

                }
                if (selected != null) {
                    selectNode(selected);
                    firePropertyChange(ElementOrTypeChooserPanel.PROP_ACTION_APPLY, false, true);
                }
            } else {
                selectNode(mRootNode);
            }

        }

        private void selectNode(Node node) {
            final Node finalNode = node;
            Runnable run = new Runnable() {
                public void run() {
                    if(manager != null) {
                        try {
                            manager.setExploredContextAndSelection(finalNode, new Node[] {finalNode});
                            btv.expandNode(finalNode);
                        } catch(PropertyVetoException ex) {
                            //ignore this
                        }

                    }
                }
            };
            SwingUtilities.invokeLater(run);
        }

        class ExplorerPropertyChangeListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] nodes = (Node[]) evt.getNewValue();
                    if(nodes.length > 0) {
                        Node node = nodes[0];
                        //set the selected node to null and state as invalid by default
                        mSelectedNode = null;
                        mEnv.setState(PropertyEnv.STATE_INVALID);
                        
                        WSDLComponent comp = node.getLookup().lookup(WSDLComponent.class);
                        if (comp != null && comp instanceof Part) {
                            mSelectedNode = node;
                            mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                            return;
                        }
                        SchemaComponent sc = null;
                        SchemaComponentReference reference = node.getLookup().lookup(SchemaComponentReference.class);
                        if (reference != null) {
                            sc = reference.get();
                        }
                        if (sc == null) {
                            sc = node.getLookup().lookup(SchemaComponent.class);
                        }
                        
                        if (sc != null && (sc instanceof GlobalType || sc instanceof GlobalElement)) {
                            mSelectedNode = node;
                            mEnv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                        }
                    }
                }
            }
        }
    }
    
    
}

