/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl;

import java.util.ArrayList;
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
