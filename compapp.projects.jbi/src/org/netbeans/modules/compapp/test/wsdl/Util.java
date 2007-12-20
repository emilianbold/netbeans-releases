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
package org.netbeans.modules.compapp.test.wsdl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.Exceptions;

/**
 * Util.java
 *
 * Created on February 2, 2006, 3:27 PM
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class Util {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.Util"); // NOI18N
    
    public static ExtensibilityElement getAssignableExtensiblityElement(
            List<ExtensibilityElement> list, Class type) {
        List<ExtensibilityElement> eelist = 
                getAssignableExtensiblityElementList(list, type);
        return eelist.size() > 0? eelist.get(0) : null;
    }
    
    public static List<ExtensibilityElement> getAssignableExtensiblityElementList(
            List<ExtensibilityElement> list, Class type) {
        List<ExtensibilityElement> result = new ArrayList<ExtensibilityElement>();
        
        for (ExtensibilityElement ee : list) {
            if(type.isAssignableFrom(ee.getClass())) {
                result.add(ee);
            }
        }
        
        return result;
    }
    
    public static String getPrettyText(XmlObject xmlObject) throws Exception {
        StringWriter writer = new StringWriter();
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);
        options.setSaveNoXmlDecl();
        options.setSaveAggressiveNamespaces();
        xmlObject.save(writer, options);
        return writer.toString();
    }
    
    private static void getBindings(WSDLModel wsdlModel, boolean recursive, 
            Collection<Binding> bindings) {
        
        Definitions definitions = wsdlModel.getDefinitions();
        bindings.addAll(definitions.getBindings());
        
        if (recursive) {
            for (Import imp : definitions.getImports()) {
                try {
                    WSDLModel importedWsdlModel = imp.getImportedWSDLModel();
                    getBindings(importedWsdlModel, recursive, bindings);
                } catch (CatalogModelException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public static List<Binding> getSortedBindings(WSDLModel wsdlModel) {
        List<Binding> bindings = new ArrayList<Binding>();
        
        getBindings(wsdlModel, true, bindings); 
        
        Collections.sort(bindings, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Binding)o1).getName().compareTo(((Binding)o2).getName());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
            
        return bindings;
     }
    
      
    private static void getPorts(WSDLModel wsdlModel, boolean recursive, 
            Collection<Port> ports) {
        
        Definitions definitions = wsdlModel.getDefinitions();
        for (Service service : definitions.getServices()) {            
            ports.addAll(service.getPorts());
        }
        
        if (recursive) {
            for (Import imp : definitions.getImports()) {
                try {
                    WSDLModel importedWsdlModel = imp.getImportedWSDLModel();
                    getPorts(importedWsdlModel, recursive, ports);
                } catch (CatalogModelException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public static List<Port> getSortedPorts(WSDLModel wsdlModel) {
        List<Port> ports = new ArrayList<Port>();
        
        getPorts(wsdlModel, true, ports); 
        
        Collections.sort(ports, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Port)o1).getName().compareTo(((Port)o2).getName());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
            
        return ports;
     }
    
    public static List<BindingOperation> getSortedBindingOperations(Binding binding) {
        List<BindingOperation> bindingOps = 
                new ArrayList<BindingOperation>(binding.getBindingOperations());
        
        Collections.sort(bindingOps, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((BindingOperation)o1).getName().compareTo(((BindingOperation)o2).getName());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
            
        return bindingOps;
     }
}
