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

package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.ModelSourceProvider;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.netbeans.modules.xml.wsdl.ui.schema.visitor.AbstractXSDVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author radval
 *
 */

public class ExtensibilityUtils {
    
    private static ModelSourceProvider mProvider;
    
    public static Schema readSchema(DataObject dataObject) {
        
        Schema schema = null;
        try {
            FileObject fileObject = dataObject.getPrimaryFile();
            ModelSource ms = null;
            if(mProvider != null) {
                ms = mProvider.getModelSource(fileObject, false);
            } else {
                ms = Utilities.getModelSource(fileObject, false);
            }
            SchemaModel schemaModel = SchemaModelFactory.getDefault().getModel(ms);
            if (schemaModel.getState() != Model.State.NOT_WELL_FORMED) {
                schema = schemaModel.getSchema();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return schema;
    }
    
    public static XMLSchemaFileInfo createXMLSchemaFileInfo(DataObject dataObject) {
        return new XMLSchemaFileInfoImpl(dataObject);
    }
    
    public static String getExtensibilityElementType(WSDLComponent component) {
        
        
        if (component instanceof Binding) {
            return WSDLExtensibilityElements.ELEMENT_BINDING;
        }
        if (component instanceof BindingOperation) {
            return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION;
        }
        if (component instanceof BindingFault) {
            return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_FAULT;
        }
        if (component instanceof BindingInput) {
            return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT;
        }
        if (component instanceof BindingOutput) {
            return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_OUTPUT;
        }
        if (component instanceof Definitions) {
            return WSDLExtensibilityElements.ELEMENT_DEFINITIONS;
        }
        if (component instanceof Message) {
            return WSDLExtensibilityElements.ELEMENT_MESSAGE;
        }
        if (component instanceof Operation) {
            return WSDLExtensibilityElements.ELEMENT_PORTTYPE_OPERATION;
        }
        if (component instanceof Service) {
            return WSDLExtensibilityElements.ELEMENT_SERVICE;
        }
        if (component instanceof Port) {
            return WSDLExtensibilityElements.ELEMENT_SERVICE_PORT;
        }
        

        return null;
    }
    
    public static Element getElement(ExtensibilityElement component) {
        List<QName> qnames = new ArrayList<QName>();
        ExtensibilityElement tempComponent = component;
        if (tempComponent != null) {
            qnames.add(0, tempComponent.getQName());
            while (ExtensibilityElement.class.isAssignableFrom(tempComponent.getParent().getClass())) {
                tempComponent = (ExtensibilityElement) tempComponent.getParent();
                qnames.add(0, tempComponent.getQName());
            }
        }
        String extensibilityElementType = null;
        if (component.getParent() != null) {
            extensibilityElementType = getExtensibilityElementType(tempComponent.getParent());
        }
        
        if (extensibilityElementType != null) {
            try {
                WSDLExtensibilityElements elements = WSDLExtensibilityElementsFactory.getInstance().getWSDLExtensibilityElements();
                WSDLExtensibilityElement mExtensibilityElement = elements.getWSDLExtensibilityElement(extensibilityElementType);
                if (mExtensibilityElement != null && qnames.size() > 0) {
                    WSDLExtensibilityElementInfo infos = mExtensibilityElement.getWSDLExtensibilityElementInfos(qnames.remove(0));
                    if (infos != null) {
                        Element element = infos.getElement();
                        ElementFinderVisitor visitor = new ElementFinderVisitor(qnames);
                        element.accept(visitor);
                        return visitor.getElement();
                    }
                }
                
                
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
            
            
        }
        return null;
    }
    
    //This should be used only from JUnit test
    public static void setModelSourceProvider(ModelSourceProvider provider) {
        mProvider = provider;
    }
    
    
}
class ElementFinderVisitor  extends AbstractXSDVisitor {
    
    private List<QName> qnames;
    private Element element;
    
    public ElementFinderVisitor(List<QName> qnames) {
        this.qnames = qnames;
    }
    
    @Override
    public void visit(GlobalElement ge) {
        if (qnames.size() > 0) {
            if (new QName(ge.getModel().getSchema ().getTargetNamespace(), ge.getName()).equals(qnames.get(0))) {
                qnames.remove(0);
                if (qnames.size() == 0) {
                    element = ge;
                }
            }
            if (element == null) {
                super.visit(ge);
            }
        }
    }

    @Override
    public void visit(LocalElement le) {
        if (qnames.size() > 0) {
            if (new QName(le.getModel().getSchema ().getTargetNamespace(), le.getName()).equals(qnames.get(0))) {
                qnames.remove(0);
                if (qnames.size() == 0) {
                    element = le;
                }
            }
            if (element == null) {
                super.visit(le);
            }
        }
    }
    
    public Element getElement() {
        return element;
    }
    
    
    
    
}
