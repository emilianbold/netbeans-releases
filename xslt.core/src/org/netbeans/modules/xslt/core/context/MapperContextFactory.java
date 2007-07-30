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
package org.netbeans.modules.xslt.core.context;


import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.Invokes;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.model.api.TransformerDescriptor;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.model.Variable;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.model.api.OperationReference;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperContextFactory {
    private static MapperContextFactory INSTANCE = new MapperContextFactory();
    
    private MapperContextFactory() {
    }
    
    public static MapperContextFactory getInstance() {
        return INSTANCE;
    }
    
    public MapperContext createMapperContext(FileObject xsltFo, Project project) {
        assert xsltFo != null && project != null;
        
        MapperContext context = null;
        FileObject tMapFo = Util.getTMapFo(project);
        XslModel xslModel = org.netbeans.modules.xslt.core.util.Util.getXslModel(xsltFo);
        
        if (tMapFo == null ) {
            tMapFo = Util.createDefaultTransformmap(project);
            return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
        }
        
        TMapModel tMapModel = Util.getTMapModel(tMapFo);
        if (tMapModel == null) {
            return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
        }
        
        Transform transformContextComponent = getTransform(tMapModel, xsltFo);
        
        if (transformContextComponent == null) {
            // TODO m
            return new MapperContextImpl(xslModel, Util.getTMapModel(tMapFo));
        }
        
        
        // TODO m
        AXIComponent sourceComponent = getSourceComponent(transformContextComponent);
        AXIComponent targetComponent = getTargetComponent(transformContextComponent);
        // TODO m
        context = new MapperContextImpl( transformContextComponent, xslModel, sourceComponent, targetComponent);
        
        return context;
    }

    public void reinitMapperContext(MapperContextImpl context,
            FileObject xsltFo, Project project) 
    {
        assert project != null;
        
        FileObject tMapFo = Util.getTMapFo(project);
        TMapModel tMapModel = tMapFo == null ? null : Util.getTMapModel(tMapFo);

        if (xsltFo == null || tMapModel == null ) {
            context.setSourceType(null);
            context.setTargetType(null);
            return;
        }
        
        Transform transformContextComponent = getTransform(tMapModel, xsltFo);
        
        if (transformContextComponent == null) {
            context.setSourceType(null);
            context.setTargetType(null);
            return;
        }
        
        // TODO m
        AXIComponent sourceComponent = getSourceComponent(transformContextComponent);
        AXIComponent targetComponent = getTargetComponent(transformContextComponent);
        // TODO m
        
        context.setSourceType(sourceComponent);
        context.setTargetType(targetComponent);
    }

    // TODO m
    private Transform getTransform(TMapModel tMapModel, FileObject xsltFo) {
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
    
    // TODO m
    private boolean isEqual(FileObject xsltFo, String filePath) {
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
    private AXIComponent getSourceComponent(Transform transform) {
        AXIComponent source = null;
//        source = getAXIComponent(getSourceType(transform));
        source = getAXIComponent(getSchemaComponent(transform, true));
        return source;
    }
    
    // TODO m
    private AXIComponent getTargetComponent(Transform transform) {
        AXIComponent target = null;
//        target = getAXIComponent(getTargetType(transform));
        target = getAXIComponent(getSchemaComponent(transform, false));
        return target;
    }
    
    private AXIComponent getAXIComponent(ReferenceableSchemaComponent schemaComponent) {
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

    public ReferenceableSchemaComponent getSchemaComponent(Transform transform, boolean isInput) {
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
    
    // TODO m
    public ReferenceableSchemaComponent getSourceType(Transform transform) {
        assert transform != null;
        
        ReferenceableSchemaComponent sourceSchemaComponent = null;
        
        TMapComponent operation = transform.getParent();
        if (operation == null) {
            return null;
        }
        assert operation instanceof Operation;
        
        Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef = 
                                                    ((Operation)operation).getOperation();
        org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
        if (operationRef != null) {
                wsdlOp = operationRef.get();
        }

        if (wsdlOp != null) {
            org.netbeans.modules.xml.wsdl.model.Input wsdlInput = wsdlOp.getInput();
            
            NamedComponentReference<Message> message = 
                    wsdlInput == null ? null : wsdlInput.getMessage();
            if (message != null) {
                sourceSchemaComponent = getMessageSchemaType(wsdlOp.getModel(), message);
            }
        }
        
        return sourceSchemaComponent;
    }
    
    // TODO m
    public ReferenceableSchemaComponent getTargetType(Transform transform) {
        assert transform != null;
        
        ReferenceableSchemaComponent targetSchemaComponent = null;
        
        TMapComponent operation = transform.getParent();
        if (operation == null) {
            return null;
        }
        assert operation instanceof Operation;
        
        Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef = 
                                                    ((Operation)operation).getOperation();
        org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
        if (operationRef != null) {
                wsdlOp = operationRef.get();
        }

        if (wsdlOp != null) {
            org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = wsdlOp.getOutput();
            
            NamedComponentReference<Message> message = 
                    wsdlOutput == null ? null : wsdlOutput.getMessage();
            if (message != null) {
                targetSchemaComponent = getMessageSchemaType(wsdlOp.getModel(), message);
            }
        }
        
        return targetSchemaComponent;
    }
    
    /**
     * returns first message part type 
     */
    private ReferenceableSchemaComponent getMessageSchemaType(WSDLModel wsdlModel, NamedComponentReference<Message> message) {
        if (wsdlModel == null || message == null) {
            return null;
        }
        ReferenceableSchemaComponent schemaComponent = null;
        
        // look at parts
        String elNamespace = null;
        Class<? extends ReferenceableSchemaComponent> elType = null; 
        Collection<Part> parts = message.get().getParts();
        Part partElem = null;
        if (parts != null && parts.size() > 0) {
            partElem = parts.iterator().next();
        }
        
        NamedComponentReference<? extends ReferenceableSchemaComponent> element = partElem.getElement();
        if (element == null) {
            element = partElem.getType();
        }
        
        // TODO r
//        if (equalMessageType(element)) {
//            schemaComponent = element.get();
//        }
        schemaComponent = element.get();
        
        return schemaComponent;
    }

    /**
     * returns first message part type with partName
     */
    private ReferenceableSchemaComponent getMessageSchemaType(VariableReference usedVariable) {
        if (usedVariable == null) {
            return null;
        }
        
        ReferenceableSchemaComponent schemaComponent = null;

        //        String partName = getVarPartName(usedVariable);
//        if (partName == null) {
//            return null;
//        }
//        
//        
//        // look at parts
//        String elNamespace = null;
//        Class<? extends ReferenceableSchemaComponent> elType = null; 
//        Collection<Part> parts = message.getParts();
//        Part part = null;
//        if (parts != null && parts.size() > 0) {
//            for (Part partElem : parts) {
//                if (partElem != null && partName.equals(partElem.getName())) {
//                    part = partElem;
//                    break;
//                }
//            }
//        }
//        
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
    
    private Message getVariableMessage(String usedVariable, Transform transform) {
        if (usedVariable == null || transform == null) {
            return null;
        }
        String varName = getVarLocalName(usedVariable);
        if (varName == null) {
            return null;
        }
        
        TMapComponent operation = transform.getParent();
        if (operation == null) {
            return null;
        }
        assert operation instanceof Operation;        
        
        Message message = null;
        
        if (varName.equals(((Operation)operation).getInputVariable())) {
            message = getMessage((Operation)operation, true);
        } else if (varName.equals(((Operation)operation).getOutputVariable())) {
            message = getMessage((Operation)operation, false);
        } else {
            List<Invokes> invokess = ((Operation)operation).getInvokess();
            if (invokess != null && invokess.size() > 0) {
                for (Invokes elem : invokess) {
                    org.netbeans.modules.xml.wsdl.model.Operation tmpOp = null;
                    if (elem != null) {
                        if (varName.equals(elem.getInputVariable())) {
                            message = getMessage(elem, true);
                            break;
                        } else if (varName.equals(elem.getOutputVariable())) {
                            message = getMessage(elem, false);
                            break;
                        }                        
                    }
                }
            }
        }
        
        return message;
        
    }
    
    private Message getMessage(OperationReference tMapOpRef, boolean isInput) {
        if (tMapOpRef == null) {
            return null;
        }
        
        Message message = null;
        Reference<org.netbeans.modules.xml.wsdl.model.Operation> opRef = 
                tMapOpRef.getOperation();
        org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = 
                opRef == null ? null : opRef.get();
        if (wsdlOp != null) {
            OperationParameter opParam = isInput 
                    ? wsdlOp.getInput() : wsdlOp.getOutput();
            Reference<Message> messageRef = opParam == null 
                    ? null : opParam.getMessage();
            message = messageRef == null ? null : messageRef.get();
        }
        
        return message;
    }
    
    private String getVarLocalName(String varWithPart) {
        if (varWithPart == null) {
            return null;
        }
        int dotIndex = varWithPart.lastIndexOf("."); // NOI18N
        String varName = varWithPart;
        if (dotIndex > 0) {
            varName = varWithPart.substring(0, dotIndex);
        }
        return varName;
    }
    
    private String getVarPartName(String varWithPart) {
         if (varWithPart == null) {
            return null;
        }
        int dotIndex = varWithPart.lastIndexOf("."); // NOI18N
        String varName = "";
        if (dotIndex > 0) {
            varName = varWithPart.substring(dotIndex+1);
        }
        return varName;
    }
    
}
