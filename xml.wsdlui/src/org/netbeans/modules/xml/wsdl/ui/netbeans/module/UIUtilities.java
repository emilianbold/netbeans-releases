/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.text.StyledDocument;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategoryNode;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.validation.ValidationAnnotation;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.FolderNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.WSDLElementNode;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
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
     * finds path of given component from root node
     */
    public static List<Node> findPathFromRoot(Node root, WSDLComponent sc) {
        if (sc.getModel() == null) {
            return Collections.emptyList();
        }
        ArrayList<WSDLComponent> path = new ArrayList<WSDLComponent>();
        WSDLComponent parent = sc;
        while (parent != null) {
            path.add(0,parent);
            parent = parent.getParent();
        }
        if (path.get(0) != sc.getModel().getDefinitions()) {
            return Collections.emptyList();
        }
        WSDLElementNode rootSCN = (WSDLElementNode) root.getCookie(
                WSDLElementNode.class);
        if (rootSCN == null || !rootSCN.isSameAsMyWSDLElement(path.get(0))) {
            return Collections.emptyList();
        }
        ArrayList<Node> selectionPath = new ArrayList<Node>();
        selectionPath.add(root);
        path.remove(0);
        Node parentNode = root;
        for (int ii = 0; ii < path.size(); ii++) {
            WSDLComponent comp = path.get(ii);
            List<Node> subPath = null;
            Node[] children = parentNode.getChildren().getNodes();
            if (children == null || children.length < 0) {
                return selectionPath;
            }
            for (Node n : children) {
                WSDLElementNode node = (WSDLElementNode) n.getCookie(
                        WSDLElementNode.class);
                if (node != null) {
                    WSDLComponent scomp = node.getWSDLComponent();
                    int idx = path.indexOf(scomp);
                    if (idx >= ii) {
                        subPath = Collections.singletonList(n);
                        parentNode = n;
                        ii=idx;
                        break;
                    }
                }
            }
            if (subPath == null) {
                for (Node n : children) {
                    FolderNode cNode = (FolderNode) n.getCookie(FolderNode.class);
                    if (cNode != null && cNode.getChildType().isInstance(comp)) {
                        Node[] nChildren = n.getChildren().getNodes();
                        if (nChildren != null && nChildren.length > 0) {
                            for (Node nChild : nChildren) {
                                WSDLElementNode node = (WSDLElementNode) nChild.
                                        getCookie(WSDLElementNode.class);
                                if (node != null) {
                                    WSDLComponent scomp = node.getWSDLComponent();
                                    int idx = path.indexOf(scomp);
                                    if (idx >= ii) {
                                        subPath = new ArrayList<Node>();
                                        subPath.add(n);
                                        subPath.add(nChild);
                                        parentNode = nChild;
                                        ii = idx;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
            if (subPath == null) {
                break;
            }
            selectionPath.addAll(subPath);
        }
        return selectionPath;
    }

    
    /**
     * finds path of given sceham component from root node representing its schema
     * example
     * 1. Schema->GlobalElement->ComplexType->Sequence
     *      results in SchemaNode->Elements Category Node->GlobalElement Node ->
     *                 Complex Type Node -> Sequence Node
     * 2. Schema->GlobalElement->ComplexType->Complex Content->Extension
     *      results in SchemaNode->Elements Category Node->GlobalElement Node ->
     *                 Complex Type Node
     *      (as complex content and extension nodes are not shown)
     */
    public static List<Node> findPathFromRoot(Node root, SchemaComponent sc, WSDLModel model)
    {
        if (sc.getModel() == null) {
            return Collections.emptyList();
        }
        
        ArrayList<SchemaComponent> path = new ArrayList<SchemaComponent>();
        SchemaComponent parent = sc;
        while(parent!=null)
        {
            path.add(0, parent);
            parent = parent.getParent();
        }
        
        //Expecting schema as the root for this schemacomponent.
        if (!(path.get(0) instanceof Schema)) return Collections.emptyList();
        
        Types types = model.getDefinitions().getTypes();
        Collection<WSDLSchema> schemas = types.getExtensibilityElements(WSDLSchema.class);
        
        WSDLSchema wsdlSchema = null;
        for (WSDLSchema schema : schemas) {
            if (path.get(0) == schema.getSchemaModel().getSchema()) {
                wsdlSchema = schema;
                break;
            }
        }
        
        //Should be able to find a schema in the wsdl which contains this SchemaComponent.
        if (wsdlSchema == null) return Collections.emptyList();
        
        //get the wsdlschema
        List<Node> wsdlPathTillTypes = findPathFromRoot(root, types);
        
        if (wsdlPathTillTypes == null || wsdlPathTillTypes.isEmpty()) {
            return Collections.emptyList();
        }
        
        WSDLElementNode rootNode = (WSDLElementNode) root.getCookie(WSDLElementNode.class);
        if (rootNode == null || rootNode != wsdlPathTillTypes.get(0)) {
            return Collections.emptyList();
        }
        
        
        Node typesNode = wsdlPathTillTypes.get(wsdlPathTillTypes.size() - 1);
        Node[] schemaNodes = typesNode.getChildren().getNodes();
        Node schemaRootNode = null;
        if (schemaNodes != null) {
            for (Node schemaNode : schemaNodes) {
                SchemaComponentNode schemaCNode = (SchemaComponentNode) schemaNode.getCookie(SchemaComponentNode.class);
                if (schemaCNode.getReference().get().equals(wsdlSchema.getSchemaModel().getSchema())) {
                    schemaRootNode = schemaNode;//pass the filter node, not the schemacomponentnode for the schema. fix for IZ 84466
                    break;
                }
            }
        }
        
        ArrayList<Node> selectionPath = new ArrayList<Node>();
        selectionPath.add(schemaRootNode);
        Class<? extends SchemaComponent> categoryType = null;
        if(path.size() > 1) categoryType = path.get(1).getComponentType();
        Node parentNode = schemaRootNode;
        for(int i = 0; i < path.size(); i++)
        {
            SchemaComponent comp = path.get(i);
            List<Node> subPath = null;
            Node[] children = parentNode.getChildren().getNodes();
            if (children == null || children.length < 0)
            {
                return selectionPath;
            }
            if (subPath==null)
            {
                for (Node n : children)
                {
                    CategoryNode cNode = (CategoryNode) n.getCookie(CategoryNode.class);
                    if (cNode != null && cNode.getChildType()==categoryType)
                    {
                        Node[] nChildren = n.getChildren().getNodes();
                        if (nChildren != null && nChildren.length > 0)
                        {
                            for (Node nChild : nChildren)
                            {
                                SchemaComponentNode node = (SchemaComponentNode) nChild.
                                        getCookie(SchemaComponentNode.class);
                                if (node != null)
                                {
                                    SchemaComponent scomp = node.getReference().get();
                                    int idx = path.indexOf(scomp);
                                    if (idx>=i)
                                    {
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
            if (subPath==null)
            {
                for (Node n : children)
                {
                    SchemaComponentNode node = (SchemaComponentNode) n.getCookie(
                            SchemaComponentNode.class);
                    if (node != null)
                    {
                        SchemaComponent scomp = node.getReference().get();
                        int idx = path.indexOf(scomp);
                        if (idx>=i)
                        {
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
        wsdlPathTillTypes.addAll(selectionPath);
        return wsdlPathTillTypes;
    }
    
    /**
     * Searches for node representing given component, under given node.
     * Search is recusrive.
     */
    public static Node findNode(Node parent, ReferenceableWSDLComponent sc,  WSDLModel currentModel) {
        if (parent == null) return null;
        Node[] children = parent.getChildren().getNodes();
        if(children == null || children.length<0) return null;
//		boolean primitive = sc.getModel()==
//				WSDLModelFactory.getDefault().getPrimitiveTypesModel();
        boolean externalRef = /*!primitive&&*/sc.getModel()!=currentModel;
        for (Node n:children) {
            WSDLElementNode node =(WSDLElementNode)n.
                    getCookie(WSDLElementNode.class);
            WSDLComponent scomp = node==null?null:node.getWSDLComponent();
            if (scomp == sc) {
                return n;
            }
//			if(primitive)
//			{
//				if (n instanceof PrimitiveSimpleTypesNode)
//				{
//					return findNode(n,sc,currentModel);
//				}
//			}
            if(externalRef) {
                if(scomp instanceof Definitions && scomp.getModel() == sc.getModel()) {
                    return findNode(n,sc,scomp.getModel());
                }
//				if(scomp instanceof WSDLModelReference)
//				{
//					Node result = findNode(n,sc,currentModel);
//					if (result != null) return result;
//				}
            }
//			if(scomp!=null) continue;
//			CategoryNode cNode = (CategoryNode)n.getCookie(
//					CategoryNode.class);
//			if(cNode==null) continue;
//			if(!externalRef && cNode.getChildType().
//					isAssignableFrom(sc.getComponentType()))
//			{
//				return findNode(n,sc,currentModel);
//			}
//			else if(externalRef&& SchemaModelReference.class.
//					isAssignableFrom(cNode.getChildType()))
//			{
//				return findNode(n,sc,currentModel);
//			}
        }
        return null;
    }

    private static WSDLElementNode findWSDLComponentNode(Node node) {
        WSDLElementNode scn =(WSDLElementNode)node
                .getCookie(WSDLElementNode.class);
        if(scn!=null) return scn;
        Node parent = node;
        while((parent=parent.getParentNode())!=null) {
            scn = (WSDLElementNode)parent
                    .getCookie(WSDLElementNode.class);
            if(scn!=null) return scn;
        }
        return null;
    }

    public static <T extends ReferenceableWSDLComponent> T findReferenceable(Node node,
            Class<T> type) {
        WSDLElementNode scn =findWSDLComponentNode(node);
        if(scn==null) return null;
        WSDLComponent component =
                scn.getWSDLComponent();
        if(type.isInstance(component))
            return type.cast(component);
        while((component=component.getParent())!=null)
            if(type.isInstance(component))
                return type.cast(component);
        return null;
    }

    /**
     * Create a dialog to contain a "creator" customizer (a customizer that
     * is used to create something that does not yet exist), which generally
     * means the title will be "Add Xyz".
     *
     * @return  dialog description with appropriate title.
     */
    public static DialogDescriptor getCreatorDialog(
            Customizer customizer, String title, boolean editable) {
        title = NbBundle.getMessage(UIUtilities.class,
                "TITLE_WSDLElementNode_Creator", title);
        return getCustomizerDialog0(customizer, title, editable);
    }

    /**
     * Create a dialog to contain a customizer (a customizer that is used
     * to edit something that already exists), which generally means the
     * title will be "Xyz Customizer".
     *
     * @return  dialog description with appropriate title.
     */
    public static DialogDescriptor getCustomizerDialog(
            Customizer customizer, String title, boolean editable) {
        title = NbBundle.getMessage(UIUtilities.class,
                "TITLE_WSDLElementNode_Customizer", title);
        return getCustomizerDialog0(customizer, title, editable);
    }

    private static DialogDescriptor getCustomizerDialog0(
            final Customizer customizer, final String title, final boolean editable) {
        java.awt.Component component = customizer.getComponent();
        final DialogDescriptor descriptor = new DialogDescriptor(component,
                title, true, null);
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
        return descriptor;
    }
    
    /**
     * annotates the source view and if shoudShowSource is true, then jumps to the line in the source editor
     * 
     * @param dobj
     * @param wsdlComp the component with error
     * @param errorMessage the errormessage
     * @param shouldShowSource whether should jump to source editor
     */
    public static void annotateSourceView(WSDLDataObject dobj, DocumentComponent wsdlComp, String errorMessage, boolean shouldShowSource) {
        LineCookie lc = (LineCookie) dobj.getCookie(LineCookie.class);
        EditCookie ec = (EditCookie) dobj.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        ec.edit();
        ValidationAnnotation.clearAll();
        int lineNum = getLineNumber(wsdlComp);
        if (lineNum < 1) {
            return;
        }
        
        Line l = lc.getLineSet().getCurrent(lineNum);
        if (errorMessage != null) {
            ValidationAnnotation annotation = ValidationAnnotation.getNewInstance();
            annotation.setErrorMessage(errorMessage);
            annotation.attach( l );
            l.addPropertyChangeListener( annotation );
        }
        
        if (shouldShowSource) {
            l.show(Line.SHOW_GOTO);
        }
    }
    
    public static int getLineNumber(DocumentComponent comp) {
        int position = comp.findPosition();
        ModelSource modelSource = comp.getModel().getModelSource();
        assert modelSource != null;
        Lookup lookup = modelSource.getLookup();
        
        StyledDocument document = (StyledDocument)lookup.lookup(StyledDocument.class);
        if (document == null) {
            return -1;
        }
        return NbDocument.findLineNumber(document,position);
    }
}
