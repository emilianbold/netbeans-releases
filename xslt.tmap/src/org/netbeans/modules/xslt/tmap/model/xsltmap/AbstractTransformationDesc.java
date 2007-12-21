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
package org.netbeans.modules.xslt.tmap.model.xsltmap;

import java.util.Collection;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class AbstractTransformationDesc implements TransformationDesc {
    private String partnerLink;
    private String roleName;
    private String portType;
    private String operation;
    private String messageType;
    private String file;
    private boolean transformJBI;
    private TransformationUC parentTUC;
    private XsltMapModel model;
    
    public AbstractTransformationDesc(XsltMapModel model, TransformationUC parent) {
        this.parentTUC = parent;
        this.model = model;
        parentTUC.addTransformationDesc(this);
    }

    public XsltMapModel getXsltMapModel() {
        return model;
    }
    
    public TransformationUC getParent() {
        return parentTUC;
    } 
    
    public XsltMapModel getModel() {
        return model;
    }
    
    public String getPartnerLink() {
        return partnerLink;
    }

    public void setPartnerLink(String partnerLink) {
        this.partnerLink = partnerLink;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getPortType() {
        return portType;
    }

    public void setPortType(String portType) {
        this.portType = portType;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getTransformJBI() {
        return Boolean.toString(transformJBI);
    }

    public void setTransformJBI(boolean transformJBI) {
        this.transformJBI = transformJBI;
    }
    
    public void setTransformJBI(String transformJBI) {
        this.transformJBI = "true".equals(transformJBI);
    }

    public boolean isTransformJBI() {
        return transformJBI;
    }
    
    public boolean isEqualInputFile(FileObject xsltFile) {
        String inputFile = getFile();
        return inputFile != null 
                && xsltFile != null 
                && inputFile.equals(xsltFile.getNameExt());
    }

    public AXIModel getSourceAxiModel(FileObject projectRoot) {
        assert projectRoot != null;
        AXIModel axiSourceModel = null;
        SchemaModel sourceSchema = getSourceSchema(projectRoot);
        if ( sourceSchema != null) {
            axiSourceModel = AXIModelFactory.getDefault().getModel(sourceSchema);
        }
        
        return axiSourceModel;
    }
    
    public SchemaModel getSourceSchema(FileObject projectRoot) {
        if (isTransformJBI()) {
            return getJBISourceSchema();
        }
        
        assert projectRoot != null;
        SchemaModel sourceSchema = null;
        Operation operation = Util.findWsdlOperation(projectRoot,this);
        if (operation != null) {
            org.netbeans.modules.xml.wsdl.model.Input wsdlInput = operation.getInput();
            NamedComponentReference<Message> message = wsdlInput.getMessage();
            if (message == null) {
                return null;
            }
            
            sourceSchema = getMessageSchemaModel(operation.getModel(), message);
        }
        return sourceSchema;
    }
    
    public ReferenceableSchemaComponent getSourceType(FileObject projectRoot) {
        if (isTransformJBI()) {
            return null;
        }
        
        assert projectRoot != null;
        ReferenceableSchemaComponent sourceSchemaComponent = null;
        Operation operation = Util.findWsdlOperation(projectRoot,this);
        if (operation != null) {
            org.netbeans.modules.xml.wsdl.model.Input wsdlInput = operation.getInput();
            NamedComponentReference<Message> message = wsdlInput.getMessage();
            if (message == null) {
                return null;
            }
            
            sourceSchemaComponent = getMessageSchemaType(operation.getModel(), message);
        }
        
        return sourceSchemaComponent;
    }

    public AXIComponent getPltSourceAXIType(FileObject projectRoot) {
        assert projectRoot != null;
        return getAXIComponent(getSourceType(projectRoot));
    }

    private SchemaModel getJBISourceSchema() {
        // TODO impl it
        return null;
    }
    
    private SchemaModel getJBITargetSchema() {
        // TODO impl it
        return null;
    }

    /**
     * returns first message part element/type schema model
     */
    private SchemaModel getMessageSchemaModel(WSDLModel wsdlModel, NamedComponentReference<Message> message) {
        if (wsdlModel == null || message == null) {
            return null;
        }
        SchemaModel schemaModel = null;
        
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
        
        if (element != null && element.get() != null) {
            schemaModel = element.get().getModel();
        }
        
        return schemaModel;
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

    private boolean equalMessageType(NamedComponentReference<? extends ReferenceableSchemaComponent> element) {
        String curMessageType = getMessageType();
        if (curMessageType  == null || element == null ) {
            return false;
        }
        
        return curMessageType.equals(element.getRefString());
    }
    
    public ReferenceableSchemaComponent getTargetType(FileObject projectRoot) {
        if (isTransformJBI()) {
            return null;
        }
        
        assert projectRoot != null;
        ReferenceableSchemaComponent targetSchemaComponent = null;
        Operation operation = Util.findWsdlOperation(projectRoot,this);
        if (operation != null) {
            org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = operation.getOutput();
            NamedComponentReference<Message> message = wsdlOutput.getMessage();
            if (message == null) {
                return null;
            }
            
            targetSchemaComponent = getMessageSchemaType(operation.getModel(), message);
        }
        return targetSchemaComponent;
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
    
    public AXIComponent getPltTargetAXIType(FileObject projectRoot) {
        assert projectRoot != null;
        return getAXIComponent(getTargetType(projectRoot));
    }

    public SchemaModel getTargetSchema(FileObject projectRoot) {
        if (isTransformJBI()) {
            return getJBITargetSchema();
        }
        
        assert projectRoot != null;
        SchemaModel targetSchema = null;
        Operation operation = Util.findWsdlOperation(projectRoot,this);
        if (operation != null) {
            org.netbeans.modules.xml.wsdl.model.Output wsdlOutput = operation.getOutput();
            if (wsdlOutput != null) {
                NamedComponentReference<Message> message = wsdlOutput.getMessage();
                if (message == null) {
                    return null;
                }

                targetSchema = getMessageSchemaModel(operation.getModel(), message);
            }
        }
        return targetSchema;
    }

    public AXIModel getTargetAxiModel(FileObject projectRoot) {
        assert projectRoot != null;
        AXIModel axiTargetModel = null;
        SchemaModel targetSchema = getTargetSchema(projectRoot);
        if ( targetSchema != null) {
            axiTargetModel = AXIModelFactory.getDefault().getModel(targetSchema);
        }
        
        return axiTargetModel;
    }
}
