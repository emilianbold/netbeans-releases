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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.axis2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
public class WSDLUtils {
    private static final String DEFAULT_PACKAGE_NAME="org.apache.ws.axis2"; //NOI18N
    
    
    public static WSDLModel getWSDLModel(FileObject wsdlFile, boolean editable){
        ModelSource ms = AxisUtils.createModelSource(wsdlFile, editable);
        return WSDLModelFactory.getDefault().getModel(ms);
    }
    
    public static Collection<Service> getServices(WSDLModel wsdlModel) {
        return wsdlModel.getDefinitions().getServices();
    }
    
    public static Collection<Port> getPortsForService(Collection<Service> services, String serviceName) {
        List<Port> ports = new ArrayList<Port>();
        for (Service service:services) {
            if (serviceName.equals(service.getName())) {
                return service.getPorts();
            }
        }
        return Collections.<Port>emptyList();
    }
    public static Collection<Port> getPortsForService(Service service) {
        return service.getPorts();
    }
    
    public static String getTargetNamespace(WSDLModel wsdlModel) {
        return wsdlModel.getDefinitions().getTargetNamespace();
    }    
    
    public static String getPackageNameFromNamespace(String ns) {
        String base = ns;
        int doubleSlashIndex = ns.indexOf("//"); //NOI18N
        if (doubleSlashIndex >=0) {
            base = ns.substring(doubleSlashIndex+2);
        } else {
            int colonIndex = ns.indexOf(":");
            if (colonIndex >=0) base = ns.substring(colonIndex+1);
        }
        StringTokenizer tokens = new StringTokenizer(base,"/"); //NOI18N
        if (tokens.countTokens() > 0) {
            List<String> packageParts = new ArrayList<String>();
            List<String> nsParts = new ArrayList<String>();
            while (tokens.hasMoreTokens()) {
                String part = tokens.nextToken();
                if (part.length() >= 0) {
                    nsParts.add(part);
                }
            }
            if (nsParts.size() > 0) {
                StringTokenizer tokens1 = new StringTokenizer(nsParts.get(0),"."); //NOI18N
                int countTokens = tokens1.countTokens();
                if (countTokens > 0) {
                    List<String> list = new ArrayList<String>();
                    while(tokens1.hasMoreTokens()) {
                        list.add(tokens1.nextToken());
                    }
                    for (int i=countTokens-1; i>=0; i--) {
                        packageParts.add(list.get(i).toLowerCase());
                    }
                } else {
                    return DEFAULT_PACKAGE_NAME;
                }
                for (int i=1; i<nsParts.size(); i++) {
                    packageParts.add(nsParts.get(i).toLowerCase());
                }
                StringBuffer buf = new StringBuffer(packageParts.get(0));
                for (int i=1;i<packageParts.size();i++) {
                    buf.append("."+packageParts.get(i));
                }
                return buf.toString();
            }
        }
        return DEFAULT_PACKAGE_NAME;
        
    }
}
