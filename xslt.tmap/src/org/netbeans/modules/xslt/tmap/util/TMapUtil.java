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
package org.netbeans.modules.xslt.tmap.util;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.TMapReferenceBuilder;
import org.netbeans.modules.xslt.tmap.multiview.source.TMapSourceMultiViewElementDesc;
import org.netbeans.modules.xslt.tmap.multiview.tree.TreeMultiViewElementDesc;
import org.netbeans.modules.xslt.tmap.nodes.NavigatorNodeFactory;
import org.netbeans.modules.xslt.tmap.nodes.NodeType;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapUtil {

    private TMapUtil() {
    }

    public static Transform getTransform(TMapModel tMapModel, FileObject xsltFo) {
        assert tMapModel != null && xsltFo != null;
        Transform transformOp = null;

        TransformMap root = tMapModel.getTransformMap();
        List<Service> services = root == null ? null : root.getServices();
        if (services != null) {
            for (Service service : services) {
                List<Operation> operations = service.getOperations();
                if (operations == null) {
                    break;
                }
                for (Operation oElem : operations) {
                    List<Transform> transforms = oElem.getTransforms();
                    for (Transform tElem : transforms) {
                        if (isEqual(xsltFo, tElem.getFile())) {
                            transformOp = tElem;
                            break;
                        }
                    }
                    if (transformOp != null) {
                        break;
                    }
                }
                if (transformOp != null) {
                    break;
                }
            }
        }

        return transformOp;
    }

    public static boolean isEqual(FileObject xsltFo, String filePath) {
        assert xsltFo != null;
        if (filePath == null) {
            return false;
        }
        
        String xsltFoPath = xsltFo.getPath();
        if (xsltFoPath.equals(filePath)) {
            return true;
        }
        
        // may be relative ?
        File rootDir = FileUtil.toFile(xsltFo);
        File tmpDir = FileUtil.toFile(xsltFo);
        while ( (tmpDir = tmpDir.getParentFile()) != null){
            rootDir = tmpDir;
        }
        
        if (filePath != null && filePath.startsWith(rootDir.getPath())) {
            return false;
        }
        
        String pathSeparator = System.getProperty("path.separator");
        StringTokenizer tokenizer = new StringTokenizer(filePath, pathSeparator);
        
        boolean isEqual = true;
        isEqual = filePath != null && filePath.equals(xsltFo.getNameExt());
// TODO m
////        FileObject nextFileParent = xsltFo;
////        while (tokenizer.hasMoreElements()) {
////            if (nextFileParent == null || 
////                    !tokenizer.nextToken().equals(nextFileParent.getNameExt())) 
////            {
////                isEqual = false;
////                break;
////            }
////        }
        
        return isEqual;
    }

    // TODO m
    public static AXIComponent getSourceComponent(Transform transform) {
        AXIComponent source = null;
//        source = getAXIComponent(getSourceType(transform));
        source = getAXIComponent(getSchemaComponent(transform, true));
        return source;
    }
    
    // TODO m
    public static AXIComponent getTargetComponent(Transform transform) {
        AXIComponent target = null;
//        target = getAXIComponent(getTargetType(transform));
        target = getAXIComponent(getSchemaComponent(transform, false));
        return target;
    }
    
    private static AXIComponent getAXIComponent(ReferenceableSchemaComponent schemaComponent) {
        if (schemaComponent == null) {
            return null;
        }
        AXIComponent axiComponent = null;

        AXIModel axiModel = AXIModelFactory.getDefault().getModel(schemaComponent.getModel());
        if (axiModel != null ) {
            axiComponent = AxiomUtils.findGlobalComponent(axiModel.getRoot(),
                    null,
                    schemaComponent);
        }
        
        return axiComponent;
    }
    
    public static ReferenceableSchemaComponent getSchemaComponent(Transform transform, boolean isInput) {
        assert transform != null;
        
        ReferenceableSchemaComponent schemaComponent = null;

        VariableReference usedVariable = isInput ? transform.getSource() : transform.getResult();
        
//        Message message = 
//                getVariableMessage(usedVariable, transform);

        if (usedVariable != null) {
            schemaComponent = getMessageSchemaType(usedVariable);
        }
        
        return schemaComponent;
    }

    /**
     * returns first message part type with partName
     */
    private static ReferenceableSchemaComponent getMessageSchemaType(VariableReference usedVariable) {
        if (usedVariable == null) {
            return null;
        }
        
        ReferenceableSchemaComponent schemaComponent = null;

        WSDLReference<Part> partRef = usedVariable.getPart();
        Part part = partRef == null ? null : partRef.get();
        
        NamedComponentReference<? extends ReferenceableSchemaComponent> element = null;
        if (part != null) {
            element = part.getElement();
            if (element == null) {
                element = part.getType();
            }

            schemaComponent = element.get();
        }
        return schemaComponent;
    }

    public static void goToTreeView(Component component) {
        if ( !(component instanceof TMapComponent)) {
            return;
        }
        final TMapComponent tmapComponent = (TMapComponent) component;

        if (tmapComponent.getModel() == null) { // deleted
            return;
        }
        FileObject fo = SoaUtil.getFileObjectByModel(tmapComponent.getModel());

        if (fo == null) {
            return;                                                        
        }
        try {
            DataObject d = DataObject.find(fo);
            final Lookup lookup = d != null ? d.getLookup() : null;

            final EditCookie ec = d.getCookie(EditCookie.class);
            if (ec == null) {
                return;
            }

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ec.edit();
                    openActiveTreeEditor();
                    if (lookup != null || tmapComponent != null) {
                        NodeType nodeType = NodeType.getNodeType(tmapComponent);
                        if (nodeType == null) {
                            return;
                        } 
                        Node tmapNode = NavigatorNodeFactory.getInstance().
                                createNode(nodeType, tmapComponent, Children.LEAF, lookup);
                        TopComponent treeTc = WindowManager.getDefault().getRegistry().getActivated();

                        if (treeTc != null) {
                            treeTc.setActivatedNodes(new Node[0]);
                            treeTc.setActivatedNodes(new Node[] {tmapNode});
                        }
                    }
                }
            });
        } catch (DataObjectNotFoundException ex) {
          return;
        }
    }

    public static void goToSourceView(Component component) {
        if (component.getModel() == null) { // deleted
            return;
        }
        if ( !(component instanceof DocumentComponent)) {
            return;
        }
        DocumentComponent document = (DocumentComponent) component;
        FileObject fo = SoaUtil.getFileObjectByModel(component.getModel());

        if (fo == null) {
            return;
        }
        try {
            DataObject d = DataObject.find(fo);
            LineCookie lc = d.getCookie(LineCookie.class);
            if (lc == null) {
                return;
            }
            int lineNum = SoaUtil.getLineNum(document);
            if (lineNum < 0) {
                return;
            }

            final Line l = lc.getLineSet().getCurrent(lineNum);
            final int column = SoaUtil.getColumnNum(document);
            if (column < 0) {
                return;
            }

            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    l.show(Line.SHOW_GOTO, column);
                    openActiveSourceEditor();
                }
            });
        } catch (DataObjectNotFoundException ex) {
          return;
        }
    }

    private static void openActiveTreeEditor() {
        SoaUtil.openActiveMVEditor(TreeMultiViewElementDesc.PREFERRED_ID);
    }
    
    private static void openActiveSourceEditor() {
        SoaUtil.openActiveMVEditor(TMapSourceMultiViewElementDesc.PREFERED_ID);
    }
    
    public static QName getQName(String value, TMapComponent component) {
        if (value == null) {
            return null;
        }
        String[] splited = new String[2];
        splitQName(value, splited);
        String uri = component.getNamespaceContext().getNamespaceURI(splited[0]);

        if (uri == null) {
            return null;
        }
        return new QName(uri, splited[1]);
    }

    public static void splitQName(String qName, String[] result) {
        assert qName != null;
        assert result != null;
        String[] parts = qName.split(":"); //NOI18N

        String prefix;
        String localName;
        if (parts.length == 2) {
            prefix = parts[0];
            localName = parts[1];
        } else {
            prefix = null;
            localName = parts[0];
        }
        if (result.length > 0) {
            result[0] = prefix;
        }
        if (result.length > 1) {
            result[1] = localName;
        }
    }
}
