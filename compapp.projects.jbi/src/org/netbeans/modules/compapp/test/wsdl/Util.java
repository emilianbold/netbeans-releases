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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.extensions.ExtensibilityElement;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import java.util.logging.Logger;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

/**
 * Util.java
 *
 * Created on February 2, 2006, 3:27 PM
 *
 * @author Bing Lu
 */
public class Util {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.Util"); // NOI18N
    
    public static ExtensibilityElement getAssignableExtensiblityElement(List list, Class type) {
        List eelist = getAssignableExtensiblityElementList(list, type);
        return eelist.size() > 0? (ExtensibilityElement)eelist.get(0) : null;
    }
    
    public static List getAssignableExtensiblityElementList(List list, Class type) {
        List result = new ArrayList();
        
        for (int i = 0, I = list.size(); i < I; i++) {
            ExtensibilityElement ee = (ExtensibilityElement) list.get(i);
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
    
    public static List getSortedBindings(Definition def) {
        List bdNameList = new ArrayList();
        Map bdMap = def.getBindings();
        for (Iterator itr = bdMap.keySet().iterator(); itr.hasNext();) {
            QName qn = (QName)itr.next();
            bdNameList.add(qn);
        }
        Collections.sort(bdNameList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((QName)o1).getLocalPart().compareTo(((QName)o2).getLocalPart());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
        List bdList = new ArrayList();
        for (int i = 0; i < bdNameList.size(); i++) {
            bdList.add(bdMap.get(bdNameList.get(i)));
        }
        return bdList;
    }
    
    public static List getSortedBindingOperations(Binding binding) {
        List opList = binding.getBindingOperations();
        Collections.sort(opList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((BindingOperation)o1).getName().compareTo(((BindingOperation)o2).getName());
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
        return opList;
    }

    public static List getSortedOperations(Binding binding) {
        PortType portType = binding.getPortType();
        List opList = portType.getOperations();
        Collections.sort(opList, new Comparator() {
            public int compare(Object o1, Object o2) {
                Operation op1 = (Operation)o1;
                Operation op2 = (Operation)o2;
                
                int i0 = 100*op1.getName().compareTo(op2.getName());
                
                String in1 = op1.getInput() == null? " " : op1.getInput().getMessage().getQName().getLocalPart(); // NOI18N
                String in2 = op2.getInput() == null? " " : op2.getInput().getMessage().getQName().getLocalPart(); // NOI18N
                int i1 = 10*in1.compareTo(in2);

                String out1 = op1.getOutput() == null? " " : op1.getOutput().getMessage().getQName().getLocalPart(); // NOI18N
                String out2 = op2.getOutput() == null? " " : op2.getOutput().getMessage().getQName().getLocalPart(); // NOI18N
                int i2 = out1.compareTo(out2);

                return i0 + i1 + i2;
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
        });
        return opList;
    }

    public static BindingOperation getBindingOperation(Binding binding, Operation operation) {
        List bopList = binding.getBindingOperations();
        List nameMatchList = new ArrayList();
        for (int i = 0; i < bopList.size(); i++) {
            BindingOperation bop = (BindingOperation)bopList.get(i);
            if (bop.getName().equals(operation.getName())) {
                nameMatchList.add(bop);
            }
        }
        if (nameMatchList.size() == 0) {
            return null;
        }
        if (nameMatchList.size() == 1) {
            return (BindingOperation)nameMatchList.get(0);
        }
        // Overloaded operations: BindingOperation must specifiy their input names
        // and output names, hence can be searched using operation's input name and
        // output name
        Input input = operation.getInput();
        String inputName = input == null? null : input.getName();
        Output output = operation.getOutput();
        String outputName = output == null? null : output.getName();
        return binding.getBindingOperation(operation.getName(), inputName, outputName);
    }
}
