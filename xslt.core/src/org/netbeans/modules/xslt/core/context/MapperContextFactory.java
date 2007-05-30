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
import org.netbeans.modules.xslt.model.XslModel;
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
        
        TransformerDescriptor transformContextComponent = getOperation(tMapModel, xsltFo);
        if (transformContextComponent == null) {
            transformContextComponent = getInvokes(tMapModel, xsltFo);
        }
        
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

    // TODO m
    private Operation getOperation(TMapModel tMapModel, FileObject xsltFo) {
        assert tMapModel != null && xsltFo != null;
        Operation transformOp = null;
        
        TransformMap root = tMapModel.getTransformMap();
        List<Service> services = root == null ? null : root.getServices();
        if (services != null) {
            for (Service service : services) {
                List<Operation> operations = service.getOperations();
                for (Operation elem : operations) {
                    if (isEqual(xsltFo, elem.getFile())) {
                        transformOp = elem;
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
    private Invokes getInvokes(TMapModel tMapModel, FileObject xsltFo) {
        assert tMapModel != null && xsltFo != null;
        Invokes transformInv = null;
        
        TransformMap root = tMapModel.getTransformMap();
        List<Service> services = root == null ? null : root.getServices();
        if (services != null) {
            for (Service service : services) {
                List<Operation> operations = service.getOperations();
                for (Operation elem : operations) {
                    Invokes invokes = elem.getInvokes();
                    if (invokes != null && isEqual(xsltFo, invokes.getFile())) {
                        transformInv = invokes;
                        break;
                    }
                }
                if (transformInv != null) {
                    break;
                }
            }
        }
        
        return transformInv;
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
    private AXIComponent getSourceComponent(TransformerDescriptor tDescriptor) {
        AXIComponent source = null;
        source = getAXIComponent(getSourceType(tDescriptor));
        return source;
    }
    
    // TODO m
    private AXIComponent getTargetComponent(TransformerDescriptor tDescriptor) {
        AXIComponent target = null;
        target = getAXIComponent(getTargetType(tDescriptor));
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

    // TODO m
    public ReferenceableSchemaComponent getSourceType(TransformerDescriptor tDescriptor) {
        if (tDescriptor instanceof Operation) {
            return getSourceType((Operation)tDescriptor);
        } else if (tDescriptor instanceof Invokes) {
            return getSourceType((Invokes)tDescriptor);
        }
        return null;
    }

    // TODO m
    public ReferenceableSchemaComponent getSourceType(Invokes invokes) {
        assert invokes != null;
        
        ReferenceableSchemaComponent sourceSchemaComponent = null;
        
        Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef = 
                                                    invokes.getOperation();        
        
        org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
        if (operationRef != null) {
                wsdlOp = operationRef.get();
        }
        
        if (wsdlOp != null) {
            org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = wsdlOp.getOutput();
            
            NamedComponentReference<Message> message = 
                    wsdlOutput == null ? null : wsdlOutput.getMessage();
            if (message != null) {
                sourceSchemaComponent = getMessageSchemaType(wsdlOp.getModel(), message);
            }
        }
        
        return sourceSchemaComponent;
    }
    
    public ReferenceableSchemaComponent getSourceType(Operation operation) {
        assert operation != null;
        
        ReferenceableSchemaComponent sourceSchemaComponent = null;
        Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef = 
                                                    operation.getOperation();
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
    public ReferenceableSchemaComponent getTargetType(TransformerDescriptor tDescriptor) {
        if (tDescriptor instanceof Operation) {
            return getTargetType((Operation)tDescriptor);
        } else if (tDescriptor instanceof Invokes) {
            return getTargetType((Invokes)tDescriptor);
        }
        return null;
    }

    // TODO m
    public ReferenceableSchemaComponent getTargetType(Invokes invokes) {
        assert invokes != null;
        
        ReferenceableSchemaComponent targetSchemaComponent = null;

        Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef = null;
        TMapComponent parent = invokes.getParent();
        if (parent instanceof Operation) {
            operationRef = ((Operation)parent).getOperation();
        }
        
        org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
        
        if (operationRef != null) {
                wsdlOp = operationRef.get();
        }
        
        org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = null;
        
        if (wsdlOp != null) {
             wsdlOutput = wsdlOp.getOutput();
        }
        
        NamedComponentReference<Message> message = null;
        
        if (wsdlOutput != null) {
            message = wsdlOutput.getMessage();
        }
        
        if (message != null) {
            targetSchemaComponent =
                    getMessageSchemaType(wsdlOp.getModel(), message);
        }
        
        return targetSchemaComponent;
    }

    // TODO m
    public ReferenceableSchemaComponent getTargetType(Operation operation) {
        assert operation != null;
        ReferenceableSchemaComponent sourceSchemaComponent = null;
        
        boolean hasInvokes = operation.getInvokes() != null;
        if (! hasInvokes) {
            Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef =
                    operation.getOperation();
            org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
            if (operationRef != null) {
                wsdlOp = operationRef.get();
            }
            
            org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = null;
            if (wsdlOp != null) {
                 wsdlOutput = wsdlOp.getOutput();

                NamedComponentReference<Message> message = null;
                if (wsdlOutput != null) {
                    message = wsdlOutput.getMessage();
                }

                if (message != null) {
                    sourceSchemaComponent = getMessageSchemaType(wsdlOp.getModel(), message);
                }
            }
        } else {
            Reference<org.netbeans.modules.xml.wsdl.model.Operation> operationRef =
                    operation.getInvokes().getOperation();
            org.netbeans.modules.xml.wsdl.model.Operation wsdlOp = null;
            if (operationRef != null) {
                wsdlOp = operationRef.get();
            }
            
            if (wsdlOp != null) {
                org.netbeans.modules.xml.wsdl.model.Input wsdlInput = wsdlOp.getInput();
                
                NamedComponentReference<Message> message = null;
                if (wsdlInput != null) {
                    message = wsdlInput.getMessage();
                }
                
                if (message != null) {
                    sourceSchemaComponent = getMessageSchemaType(wsdlOp.getModel(), message);
                }
            }
        }

        return sourceSchemaComponent;
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

    
}
