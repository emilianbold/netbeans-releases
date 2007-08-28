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

package org.netbeans.modules.xml.wsdlextui.property;


import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.StringAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class SoapAddressConfigurator extends ExtensibilityElementConfigurator {
    
    
    private static QName addressQName = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    
    private static QName[] supportedQNames = {addressQName};
    /** Creates a new instance of SoapHeaderConfigurator */
    public SoapAddressConfigurator() {
    }
    
    @Override
    public Collection<QName> getSupportedQNames() {
        return Arrays.asList(supportedQNames);
    }
    
    @Override
    public Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        Node.Property property = null;
        if (addressQName.equals(qname)) {
            if ("location".equals(attributeName)) {
                ExtensibilityElementPropertyAdapter adapter = new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeName, generateAddressLocation(extensibilityElement));
                try {
                    property = new StringAttributeProperty(adapter, String.class, "getValue", "setValue");
                    property.setName(SOAPAddress.LOCATION_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(SoapAddressConfigurator.class, "PROP_NAME_ADDRESS_LOCATION"));
                } catch (NoSuchMethodException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        return property;
    }
    
    
    @Override
    public String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname) {
        // TODO Auto-generated method stub
        return null;
    }
   
    
    public String generateAddressLocation(ExtensibilityElement element) {
        //TODO:Complete this once decision is made.
        WSDLModel model = element.getModel();
        ModelSource ms = model.getModelSource();
        FileObject fo = (FileObject) ms.getLookup().lookup(FileObject.class);
        if (fo != null) {
            Project prj = FileOwnerQuery.getOwner(fo);
            //String prjPath = prj.getProjectDirectory().getPath();
            //String filePath = fo.getPath();
            
            //String path = filePath.substring(prjPath.length());
            //String path = FileUtil.getRelativePath(prj.getProjectDirectory().getParent(), fo);
            
            //URL url = new URL("http", "localhost", 18181, path);
            Port port = (Port) element.getParent();
            return "http://localhost:18181/" +  prj.getProjectDirectory().getName() + "/" + fo.getName() + "/" + port.getName();
        }
        return "http://localhost:18181/service";
    }

    @Override
    public String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        if (attributeName.equals("location")) {
            return generateAddressLocation(extensibilityElement);
        }
        return null;
    }

    @Override
    public String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname) {
        return NbBundle.getMessage(SoapAddressConfigurator.class, "LBL_SoapAddress_TypeDisplayName");
    }
    
    
}

