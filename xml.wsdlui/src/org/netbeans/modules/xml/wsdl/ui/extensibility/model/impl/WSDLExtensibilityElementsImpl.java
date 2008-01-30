/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
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
    
    private String rootFolderNamePrefix;
    
    private Map elementsMap = new HashMap();
    
    private Map<String, XMLSchemaFileInfo> schemasMap = new HashMap<String, XMLSchemaFileInfo>();
    
    public WSDLExtensibilityElementsImpl(DataFolder rootFolder) {
        this.mRootFolder = rootFolder;
        this.rootFolderNamePrefix = mRootFolder.getName() + "/";
        readAllSchemas();
    }
    
    public WSDLExtensibilityElement getWSDLExtensibilityElement(String name) {
        WSDLExtensibilityElement element = (WSDLExtensibilityElement) elementsMap.get(name);
        if(element != null) {
            return element;
        }
        
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(rootFolderNamePrefix + name);
        if (fo != null) {
            DataFolder folder = DataFolder.findFolder(fo);
            if (folder != null && folder.getName().equals(name)) {
                element = new WSDLExtensibilityElementImpl(folder, this);
                elementsMap.put(name, element);
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
