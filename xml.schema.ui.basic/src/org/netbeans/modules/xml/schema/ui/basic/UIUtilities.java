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

/*
 * UIUtilities.java
 *
 * Created on June 23, 2006, 3:24 PM
 *
 */

package org.netbeans.modules.xml.schema.ui.basic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategoryNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.PrimitiveSimpleTypesNode;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class UIUtilities {
    
    /** Creates a new instance of UIUtilities */
    private UIUtilities() {
    }
    
    /**
     * finds path of given sceham component from root node representing its schema
     * example
     * 1. Schema->GlobalElement->ComplexType->Sequence
     *		results in SchemaNode->Elements Category Node->GlobalElement Node ->
     *                 Complex Type Node -> Sequence Node
     * 2. Schema->GlobalElement->ComplexType->Complex Content->Extension
     *		results in SchemaNode->Elements Category Node->GlobalElement Node ->
     *                 Complex Type Node
     *		(as complex content and extension nodes are not shown)
     */
    public static List<Node> findPathFromRoot(Node root, SchemaComponent sc) {
        if(sc.getModel()==null) return Collections.emptyList();
        ArrayList<SchemaComponent> path = new ArrayList<SchemaComponent>();
        SchemaComponent parent = sc;
        while(parent!=null) {
            path.add(0,parent);
            parent = parent.getParent();
        }
        if(path.get(0) !=sc.getModel().getSchema())
            return Collections.emptyList();
        SchemaComponentNode rootSCN = (SchemaComponentNode)root.
                getCookie(SchemaComponentNode.class);
        if(rootSCN==null||rootSCN.getReference().get()!=path.get(0))
            return Collections.emptyList();
        ArrayList<Node> selectionPath = new ArrayList<Node>();
        selectionPath.add(root);
        Class<? extends SchemaComponent> categoryType = null;
        if(path.size()>1) categoryType = path.get(1).getComponentType();
        Node parentNode = root;
        for(int i=0;i<path.size();i++) {
            SchemaComponent comp = path.get(i);
            List<Node> subPath = null;
            Node[] children = parentNode.getChildren().getNodes();
            if (children == null || children.length<0) {
                return selectionPath;
            }
            if (subPath==null) {
                for (Node n : children) {
                    CategoryNode cNode = (CategoryNode) n.getCookie(CategoryNode.class);
                    if (cNode != null && categoryType != null && cNode.getChildType().isAssignableFrom(categoryType)) {
                        Node[] nChildren = n.getChildren().getNodes();
                        if (nChildren != null && nChildren.length > 0) {
                            for (Node nChild : nChildren) {
                                SchemaComponentNode node = (SchemaComponentNode) nChild.
                                        getCookie(SchemaComponentNode.class);
                                if (node != null) {
                                    SchemaComponent scomp = node.getReference().get();
                                    int idx = path.indexOf(scomp);
                                    if (idx>=i) {
                                        subPath = new ArrayList<Node>();
                                        subPath.add(n);
                                        subPath.add(nChild);
                                        parentNode = nChild;
                                        i=idx;
                                        if(i!=0&&i<path.size()) categoryType =
                                                path.get(i).getComponentType();
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    } 
                }
            }
            if (subPath==null) {
                for (Node n : children) {
                    SchemaComponentNode node = (SchemaComponentNode) n.getCookie(
                            SchemaComponentNode.class);
                    if (node != null) {
                        SchemaComponent scomp = node.getReference().get();
                        int idx = path.indexOf(scomp);
                        if (idx>=i) {
                            subPath = Collections.singletonList(n);
                            parentNode = n;
                            i=idx;
                            if(i!=0&&i<path.size()) categoryType =
                                    path.get(i).getComponentType();
                            break;
                        }
                    }
                }
            }
            if (subPath==null) break;
            selectionPath.addAll(subPath);
        }
        return selectionPath;
    }
    
    /**
     * Searches for node representing given schema component, under given node.
     * Search is recusrive.
     */
    public static Node findNode(Node parent, ReferenceableSchemaComponent sc,  SchemaModel currentModel) {
        if (parent == null) return null;
        Node[] children = parent.getChildren().getNodes();
        if(children == null || children.length<0) return null;
        boolean primitive = sc.getModel()==
                SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        boolean externalRef = !primitive&&sc.getModel()!=currentModel;
        for (Node n:children) {
            SchemaComponentNode node =(SchemaComponentNode)n.
                    getCookie(SchemaComponentNode.class);
            SchemaComponent scomp = node==null?null:node.getReference().get();
            if (scomp == sc) {
                return n;
            }
            if(primitive) {
                if (n instanceof PrimitiveSimpleTypesNode) {
                    return findNode(n,sc,currentModel);
                }
            }
            if(externalRef) {
                if(scomp instanceof Schema && scomp.getModel() == sc.getModel()) {
                    Node result = findNode(n,sc,sc.getModel());
                    if (result != null) return result;
                }
                try {
                    if(scomp instanceof SchemaModelReference &&
                            ((SchemaModelReference)scomp).
                            resolveReferencedModel() == sc.getModel()) {
                        Node result = findNode(n,sc,sc.getModel());
                        if (result != null) return result;
                    }
                } catch(CatalogModelException cme) {
                    // nothing
                }
            }
            if(scomp!=null) continue;
            CategoryNode cNode = (CategoryNode)n.getCookie(
                    CategoryNode.class);
            if(cNode==null) continue;
            if(!externalRef && cNode.getChildType().
                    isAssignableFrom(sc.getComponentType())) {
                return findNode(n,sc,currentModel);
            } else if(externalRef&& SchemaModelReference.class.
                    isAssignableFrom(cNode.getChildType())) {
                return findNode(n,sc,currentModel);
            }
        }
        return null;
    }
    
    public static DialogDescriptor getCustomizerDialog(
            final Customizer customizer, final String title, final boolean editable) {
        java.awt.Component component = customizer.getComponent();
        final DialogDescriptor descriptor = new DialogDescriptor(component,
                NbBundle.getMessage(UIUtilities.class,
                "TITLE_SchemaComponentNode_Customizer", title),
                true, null);
        descriptor.setHelpCtx(customizer.getHelpCtx());
        if(editable) {
            // customizer's property change listener to enable/disable OK
            final PropertyChangeListener pcl = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getSource()==customizer && evt.getPropertyName().
                            equals(Customizer.PROP_ACTION_APPLY)) {
                        descriptor.setValid(((Boolean) evt.getNewValue()).booleanValue());
                    }
                }
            };
            customizer.addPropertyChangeListener(pcl);
            // dialog's action listener
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION) ||
                            evt.getSource().equals(DialogDescriptor.CANCEL_OPTION) ||
                            evt.getSource().equals(DialogDescriptor.CLOSED_OPTION)) {
                        customizer.removePropertyChangeListener(pcl);
                    }
                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                        try {
                            customizer.apply();
                        } catch (IOException ioe) {
                        }
                    }
                }
            };
            descriptor.setButtonListener(al);
        }
        descriptor.setValid(customizer.canApply());
        return descriptor;
    }
}
