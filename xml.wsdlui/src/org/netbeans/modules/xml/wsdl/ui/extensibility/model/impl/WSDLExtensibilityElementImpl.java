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

/*
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfoContainer;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;



/**
 * @author radval
 *
 * elements need to be unique withing this  WSDLExtensibilityElement.
 * (i.e two element name can be same as long as they are in different targetNamespace)
 * 
 */
public class WSDLExtensibilityElementImpl implements WSDLExtensibilityElement {
    
    
    private DataFolder mDataFolder;
    
    private Map elementInfoMap = new HashMap();
    
    private Map providersMap = new HashMap();
    
    private WSDLExtensibilityElementsImpl mRootElement;
    
    public WSDLExtensibilityElementImpl(DataFolder dataObject, WSDLExtensibilityElementsImpl element) {
        this.mDataFolder = dataObject;
        this.mRootElement = element;
    }
    
    public WSDLExtensibilityElementInfo getWSDLExtensibilityElementInfos(QName elementQName) {
        List allInfos = getAllWSDLExtensibilityElementInfos();
        Iterator it = allInfos.iterator();
        String ns = elementQName.getNamespaceURI();
        String localPart = elementQName.getLocalPart();
        while(it.hasNext()) {
            WSDLExtensibilityElementInfo eInfo = (WSDLExtensibilityElementInfo) it.next();
            Schema schema = eInfo.getSchema();
            if(schema != null) {
                if(ns != null) {
                    if(ns.equals(schema.getTargetNamespace()) 
                            && localPart.equals(eInfo.getElementName())) {
                        if(findGlobalElement(elementQName, schema) != null) {
                            return eInfo;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public List getAllWSDLExtensibilityElementInfos() {
        ArrayList elementsInfo = new ArrayList();
        elementsInfo.addAll(getWSDLExtensibilityElementInfos());
        
        List containers = getAllWSDLExtensibilityElementInfoContainers();
        Iterator it = containers.iterator();
        while(it.hasNext()) {
            WSDLExtensibilityElementInfoContainer container = (WSDLExtensibilityElementInfoContainer) it.next();
            elementsInfo.addAll(container.getAllWSDLExtensibilityElementInfo());
        }
        
        return elementsInfo;
    }
    
    public List getWSDLExtensibilityElementInfos() {
        ArrayList elementInfos = new ArrayList();
        ArrayList allDataObjectNames = new ArrayList();
        
        DataObject[] children = this.mDataFolder.getChildren();
        for(int i = 0; i < children.length; i++ ) {
            DataObject dObj = children[i];
            if(!(dObj instanceof DataFolder)) {
                WSDLExtensibilityElementInfo elementInfo = 
                    (WSDLExtensibilityElementInfo) elementInfoMap.get(dObj.getName());
                
                if(elementInfo == null) {
                    elementInfo = createNewElementInfo(dObj);
                    elementInfoMap.put(dObj.getName(), elementInfo);
                } 
                
                elementInfos.add(elementInfo);
                allDataObjectNames.add(dObj.getName());
            }
        }
        
        
        //TODO:update elementInfoMap map. this is to ensure if some
        //modules are disabled then we remove the elementInfos provided 
        //by that module
        
        
        return elementInfos;
    }
    
    public List getAllWSDLExtensibilityElementInfoContainers() {
        ArrayList providers = new ArrayList();
        DataObject[] children = this.mDataFolder.getChildren();
        for(int i = 0; i < children.length; i++ ) {
            DataObject dObj = children[i];
            if(dObj instanceof DataFolder) {
                WSDLExtensibilityElementInfoContainer provider = 
                    (WSDLExtensibilityElementInfoContainer) providersMap.get(dObj.getName());
                
                if(provider == null) {
                    provider = createNewProvider((DataFolder) dObj);
                    providersMap.put(dObj.getName(), provider);
                } 
                providers.add(provider);
            }
        }
        
        //update provider map. this is to ensure if some
        //modules are disabled then we remove the provider provided 
        //by that module
        
        return providers;
    }
    
    public String getName() {
        return mDataFolder.getName();
    }
    
    public boolean isExtensibilityElementsAvailable() {
        boolean available = false;
        
        if(this.getAllWSDLExtensibilityElementInfos().size() != 0) {
            available = true;
        } 
        return available;
    }
    
    private WSDLExtensibilityElementInfoContainer createNewProvider(DataFolder dataObject) {
        WSDLExtensibilityElementInfoContainer provider = 
            new WSDLExtensibilityElementInfoContainerImpl(dataObject, this.mRootElement);
        
        return provider;
    }
    
    
    private WSDLExtensibilityElementInfo createNewElementInfo(DataObject dataObject) {
        
        Object val = dataObject.getPrimaryFile().getAttribute(Constants.XSD_FILE_NAME);
        XMLSchemaFileInfo xmlSchemaInfo = null;
        if(val instanceof String) {
            xmlSchemaInfo = this.mRootElement.getXMLSchemaFileInfoMatchingFileName((String) val);
        }
        
        
        WSDLExtensibilityElementInfo elementInfo = 
            new WSDLExtensibilityElementInfoImpl(dataObject, xmlSchemaInfo);
        
        return elementInfo;
    }
        
        
    private GlobalElement findGlobalElement(QName elementQName, Schema schema) {
        Collection<GlobalElement> elements = schema.getElements();
        Iterator<GlobalElement> iter = elements.iterator();
        while (iter.hasNext()) {
            GlobalElement elem =  iter.next();
            if (elem.getName().equals(elementQName.getLocalPart())) {
                return elem;
            }
        }
        
        return null;
    }

    public Collection<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfos(String namespace) {
        List<WSDLExtensibilityElementInfo> allInfos = getAllWSDLExtensibilityElementInfos();
        List<WSDLExtensibilityElementInfo> result  = new ArrayList<WSDLExtensibilityElementInfo>();
        for (WSDLExtensibilityElementInfo eInfo : allInfos) {
            Schema schema = eInfo.getSchema();
            if(schema != null) {
                if(namespace != null) {
                    if(namespace.equals(schema.getTargetNamespace())) {
                        result.add(eInfo);
                    }
                }
            }
        }
        
        return result;
    }
}
