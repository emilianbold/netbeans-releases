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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.ui.common.Constants;
import org.netbeans.modules.xml.wsdl.ui.cookies.RefreshExtensibilityElementNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfo;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementInfoContainer;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;



/**
 * @author radval
 *
 * elements need to be unique withing this  WSDLExtensibilityElement.
 * (i.e two element name can be same as long as they are in different targetNamespace)
 * 
 */
public class WSDLExtensibilityElementImpl implements WSDLExtensibilityElement {
    
    private DataFolder mDataFolder;
    
    private Map<String, WSDLExtensibilityElementInfo> elementInfoMap = new HashMap<String, WSDLExtensibilityElementInfo>();
    
    private Map<String, WSDLExtensibilityElementInfoContainer> providersMap = new HashMap<String, WSDLExtensibilityElementInfoContainer>();
    
    private WSDLExtensibilityElementsImpl mRootElement;
    
    public WSDLExtensibilityElementImpl(DataFolder dataObject, WSDLExtensibilityElementsImpl element) {
        this.mDataFolder = dataObject;
        this.mRootElement = element;
        mDataFolder.getPrimaryFile().addFileChangeListener(new WSDLExtensibilityElementFileChangeListener());
    }
    
    public WSDLExtensibilityElementInfo getWSDLExtensibilityElementInfos(QName elementQName) {
        List<WSDLExtensibilityElementInfo> allInfos = getAllWSDLExtensibilityElementInfos();
        String ns = elementQName.getNamespaceURI();
        String localPart = elementQName.getLocalPart();
        for (WSDLExtensibilityElementInfo eInfo : allInfos) {
            Schema schema = eInfo.getSchema();
            if(schema != null && ns != null) {
                if (ns.equals(schema.getTargetNamespace()) && localPart.equals(eInfo.getElementName())) {
                    if (findGlobalElement(elementQName, schema) != null) {
                        return eInfo;
                    }
                }
            }
        }
        
        return null;
    }
    
    public List<WSDLExtensibilityElementInfo> getAllWSDLExtensibilityElementInfos() {
        ArrayList<WSDLExtensibilityElementInfo> elementsInfo = new ArrayList<WSDLExtensibilityElementInfo>();
        elementsInfo.addAll(getWSDLExtensibilityElementInfos());

        List<WSDLExtensibilityElementInfoContainer> containers = getAllWSDLExtensibilityElementInfoContainers();
        for (WSDLExtensibilityElementInfoContainer container : containers) {
            elementsInfo.addAll(container.getAllWSDLExtensibilityElementInfo());
        }
        
        return elementsInfo;
    }
    
    public List<WSDLExtensibilityElementInfo> getWSDLExtensibilityElementInfos() {
        ArrayList<WSDLExtensibilityElementInfo> elementInfos = new ArrayList<WSDLExtensibilityElementInfo>();
        ArrayList<String> allDataObjectNames = new ArrayList<String>();
        
        DataObject[] children = this.mDataFolder.getChildren();
        for (DataObject dObj : children) {
            if(!(dObj instanceof DataFolder)) {
                WSDLExtensibilityElementInfo elementInfo = elementInfoMap.get(dObj.getName());
                
                if(elementInfo == null) {
                    elementInfo = createNewElementInfo(dObj);
                    elementInfoMap.put(dObj.getName(), elementInfo);
                } 
                
                elementInfos.add(elementInfo);
                allDataObjectNames.add(dObj.getName());
            }
        }
        
        return elementInfos;
    }
    
    public List<WSDLExtensibilityElementInfoContainer> getAllWSDLExtensibilityElementInfoContainers() {
        ArrayList<WSDLExtensibilityElementInfoContainer> providers = new ArrayList<WSDLExtensibilityElementInfoContainer>();
        DataObject[] children = this.mDataFolder.getChildren();
        for (DataObject dObj : children) {
            if(dObj instanceof DataFolder) {
                WSDLExtensibilityElementInfoContainer provider = 
                     providersMap.get(dObj.getName());
                
                if(provider == null) {
                    provider = createNewProvider((DataFolder) dObj);
                    providersMap.put(dObj.getName(), provider);
                }
                providers.add(provider);
            }
        }
        
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
        for (GlobalElement elem : elements) {
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
    
    class WSDLExtensibilityElementFileChangeListener extends FileChangeAdapter {
        
        @Override
        public void fileDataCreated(FileEvent fe) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    //first try to refresh the active topcomponent
                    TopComponent tc = TopComponent.getRegistry().getActivated();
                    if (tc != null) {
                        RefreshExtensibilityElementNodeCookie rc = tc.getLookup().lookup(RefreshExtensibilityElementNodeCookie.class);
                        if (rc != null) rc.refresh();
                    }
                    
                    Set<TopComponent> openedTCs = TopComponent.getRegistry().getOpened();
                    for (TopComponent openedTC : openedTCs) {
                        RefreshExtensibilityElementNodeCookie rc = openedTC.getLookup().lookup(RefreshExtensibilityElementNodeCookie.class);
                        if (rc != null) {
                            rc.refresh();
                        }
                    }
                }
            });

        }

    }
}
