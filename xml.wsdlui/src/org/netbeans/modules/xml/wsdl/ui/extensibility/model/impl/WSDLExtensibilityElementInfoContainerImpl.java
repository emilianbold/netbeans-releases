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
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;

import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfoContainer;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLExtensibilityElementInfoContainerImpl implements WSDLExtensibilityElementInfoContainer {
    
    private static final Logger mLogger = Logger.getLogger(WSDLExtensibilityElementInfoContainerImpl.class.getName());
    
    
    private DataObject mDataObject;
    
    private List<WSDLExtensibilityElementInfo> mElementInfos = new ArrayList<WSDLExtensibilityElementInfo>();
    
    private WSDLExtensibilityElementsImpl mRootElement;
    
    
    public WSDLExtensibilityElementInfoContainerImpl(DataObject dataObject, WSDLExtensibilityElementsImpl element) {
        this.mDataObject = dataObject;
        this.mRootElement = element;
    }
    
    public List<WSDLExtensibilityElementInfo> getAllWSDLExtensibilityElementInfo() {
        if(mElementInfos.size() != 0) {
            return mElementInfos;
        }
        
        //if folder then look for files in the folder
        if(this.mDataObject instanceof DataFolder) {
            DataObject[] children = ((DataFolder)this.mDataObject).getChildren();
            for(int i = 0; i < children.length; i++ ) {
                DataObject dObj = children[i];
                Object val = dObj.getPrimaryFile().getAttribute(Constants.XSD_FILE_NAME);
                XMLSchemaFileInfo schemaFileInfo = null;
                if(val instanceof String) {
                    schemaFileInfo = this.mRootElement.getXMLSchemaFileInfoMatchingFileName((String) val);
                }
                
                WSDLExtensibilityElementInfo elementInfo = new WSDLExtensibilityElementInfoImpl(dObj, schemaFileInfo);
                mElementInfos.add(elementInfo);
                
            }
        } else {
//			//if file then use it
//			WSDLExtensibilityElementInfo elementInfo = new WSDLExtensibilityElementInfoImpl(mDataObject);
//			mElementInfos.add(elementInfo);
        }
        
        return mElementInfos;
    }
    
    public List<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfo(String namespace) {
        List<WSDLExtensibilityElementInfo> allInfos = getAllWSDLExtensibilityElementInfo();
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
    
    public String getDisplayName() {
        return mDataObject.getNodeDelegate().getDisplayName();
    }
    
    public DataObject getDataObject() {
        return mDataObject;
    }
    
    public String getName() {
        return mDataObject.getName();
    }
    
}
