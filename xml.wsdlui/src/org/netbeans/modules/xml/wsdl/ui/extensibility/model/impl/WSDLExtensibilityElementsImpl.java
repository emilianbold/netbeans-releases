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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLExtensibilityElementsImpl implements WSDLExtensibilityElements {
	
	private static final Logger mLogger = Logger.getLogger(WSDLExtensibilityElementsImpl.class.getName());
	
	private DataFolder mRootFolder = null;
	
	private Map elementsMap = new HashMap();
	
	private Map<String, XMLSchemaFileInfo> schemasMap = new HashMap<String, XMLSchemaFileInfo>();
	
	public WSDLExtensibilityElementsImpl(DataFolder rootFolder) {
		this.mRootFolder = rootFolder;
		readAllSchemas();
	}
	
	public WSDLExtensibilityElement getWSDLExtensibilityElement(String name) {
		WSDLExtensibilityElement element = (WSDLExtensibilityElement) elementsMap.get(name);
		if(element != null) {
			return element;
		}
		
		DataObject[] dataObjects = this.mRootFolder.getChildren();
		for(int i = 0; i < dataObjects.length; i++ ) {
			DataObject dObj = dataObjects[i];
			if(dObj instanceof DataFolder && dObj.getName().equals(name)) {
				element = new WSDLExtensibilityElementImpl((DataFolder) dObj, this);
				elementsMap.put(name, element);
				break;
			}
		}
		
		return element;
	}

	public InputStream[] getAllExtensionSchemas() {
		ArrayList extensionSchemas = new ArrayList();
		DataObject[] dataObjects = this.mRootFolder.getChildren();
		for(int i = 0; i < dataObjects.length; i++ ) {
			DataObject dObj = dataObjects[i];
			if(!(dObj instanceof DataFolder) && dObj.getPrimaryFile().hasExt(Constants.XSD_EXT)) {
				InputStream in = null;
				try {
					in = dObj.getPrimaryFile().getInputStream();
				} catch(Throwable t) {
					mLogger.log(Level.SEVERE, NbBundle.getMessage(WSDLExtensibilityElementsImpl.class, "ERR_MSG_FAILED_TO_GET_SCHEMA", dObj.getPrimaryFile().getPath()));
				}
				
				if(in != null) {
					extensionSchemas.add(in);
				}
			}
		}
		
		return (InputStream[]) extensionSchemas.toArray( new InputStream[] {});
	}
	
	public XMLSchemaFileInfo getXMLSchemaFileInfoMatchingFileName(String fileName) {
		XMLSchemaFileInfo schemaInfo =  this.schemasMap.get(fileName);
		return schemaInfo;
	}
	
	public XMLSchemaFileInfo getXMLSchemaFileInfo(String namespace) {
	    XMLSchemaFileInfo schemaInfo = null;

	    Iterator<XMLSchemaFileInfo> it = this.schemasMap.values().iterator();
	    while(it.hasNext()) {
	        XMLSchemaFileInfo info = it.next();
            String ns = info.getSchema().getTargetNamespace();
	        //String ns = info.getNamespace();
	        if(ns != null && ns.equals(namespace)) {
	            schemaInfo = info;
	            break;
	        }
	    }

	    return schemaInfo;
	}
        
	private void readAllSchemas() {
		DataObject[] dataObjects = this.mRootFolder.getChildren();
		for(int i = 0; i < dataObjects.length; i++ ) {
			DataObject dObj = dataObjects[i];
			if(!(dObj instanceof DataFolder) && dObj.getPrimaryFile().hasExt(Constants.XSD_EXT)) {
				XMLSchemaFileInfo schemaFileInfo = new XMLSchemaFileInfoImpl(dObj);
				this.schemasMap.put(dObj.getName(), schemaFileInfo);
			}
		}
	}

    public XMLSchemaFileInfo[] getAllXMLSchemaFileInfos() {
        Collection<XMLSchemaFileInfo> infos = schemasMap.values();
        return infos.toArray(new XMLSchemaFileInfo[infos.size()]);
    }
	
}
