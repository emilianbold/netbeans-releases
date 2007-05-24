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
package org.netbeans.modules.compapp.test.wsdl;

import java.io.StringWriter;
import java.util.ArrayList;
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
    
    public static List<Binding> getSortedBindings(Definitions definitions) {
        List<Binding> bindings = 
                new ArrayList<Binding>(definitions.getBindings());
        
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
