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
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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

}
