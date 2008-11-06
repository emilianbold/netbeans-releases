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

package org.netbeans.modules.xml.wsdlextui.property.http;


import org.netbeans.modules.xml.wsdlextui.property.*;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPAddress;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementConfigurator.class)
public class HttpAddressConfigurator extends ExtensibilityElementConfigurator {
    
    
    private static QName addressQName = new QName("http://schemas.xmlsoap.org/wsdl/http/", "address");
    
    private static QName[] supportedQNames = {addressQName};
    
    public HttpAddressConfigurator() {
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
                    property.setName(HTTPAddress.LOCATION_PROPERTY);
                    property.setDisplayName(NbBundle.getMessage(HttpAddressConfigurator.class, "PROP_NAME_ADDRESS_LOCATION"));
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
            return "http://localhost:${HttpDefaultPort}/" +  prj.getProjectDirectory().getName() + "/" + fo.getName() + "/" + port.getName();
        }
        return "http://localhost:${HttpDefaultPort}/service";
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
        return NbBundle.getMessage(HttpAddressConfigurator.class, "LBL_HttpAddress_TypeDisplayName");
    }
}
