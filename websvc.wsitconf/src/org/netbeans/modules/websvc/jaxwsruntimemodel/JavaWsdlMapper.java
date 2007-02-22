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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.jaxwsruntimemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.wsitconf.util.JMIUtils;

/**
 * This class is compiled from:
 * com.sun.xml.ws.model.RuntimeModeler
 * com.sun.xml.ws.model.SEIModel
 * com.sun.xml.ws.model.AbstractSEIModelImpl
 * com.sun.xml.ws.wsdl.writer.WSDLGenerator
 * in JAX-WS component.
 * @author Martin Grebac
 */
public class JavaWsdlMapper {
    
    public static final String RESPONSE             = "Response";   //NOI18N
    public static final String RETURN               = "return";     //NOI18N
    public static final String BEAN                 = "Bean";       //NOI18N
    public static final String SERVICE              = "Service";    //NOI18N
    public static final String PORT                 = "Port";       //NOI18N
    public static final String PORT_TYPE            = "PortType";   //NOI18N
    public static final String BINDING              = "Binding";    //NOI18N

    /** Creates a new instance of JavaWsdlMapper */
    public JavaWsdlMapper() { }
    
    /**
     * gets the namespace <code>String</code> for a given <code>packageName</code>
     * @param packageName the name of the package used to find a namespace
     * @return the namespace for the specified <code>packageName</code>
     */
    public static String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0)
            return null;

        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i=tokenizer.countTokens()-1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuilder namespace = new StringBuilder("http://");     //NOI18N
        for (int i=0; i<tokens.length; i++) {
            if (i!=0) {
                namespace.append('.');
            }
            namespace.append(tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

    /**
     * gets the <code>wsdl:serviceName</code> for a given implementation class
     * @param implClass the implementation class
     * @return the <code>wsdl:serviceName</code> for the <code>implClass</code>
     */
//    public static QName getServiceName(JavaClass implClass) {
//
//        if (implClass == null) {
//            return null;
//        }
//        
//        String name = implClass.getSimpleName()+SERVICE;
//        String packageName = getPackageFromClass(implClass);
//        String targetNamespace = getNamespace(packageName);
//
//        if (implClass != null) {
//            List<Annotation> annotations = implClass.getAnnotations();
//            if (annotations != null) {
//                for (Annotation a : annotations) {
//                    if ("javax.jws.WebService".equals(a.getType().getName())) { //NOI18N
//
//                        String serviceNameAnnot = "";
//                        String targetNamespaceAnnot = "";
//                        
//                        List<AttributeValue> attrs = a.getAttributeValues();
//                        for (AttributeValue av : attrs) {
//                            if ("serviceName".equals(av.getName())) { //NOI18N
//                                serviceNameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                            if ("targetNamespace".equals(av.getName())) { //NOI18N
//                                targetNamespaceAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                        }
//
//                        if (serviceNameAnnot.length() > 0) {
//                            name = serviceNameAnnot;
//                        }
//                        
//                        if (targetNamespaceAnnot.length() > 0) {
//                            targetNamespace = targetNamespaceAnnot;
//                        }
//
//                        return new QName(targetNamespace, name);
//                    }
//                } 
//            }
//        }
//        return null;
//    }

    /**
     * gets the <code>wsdl:portName</code> for a given implementation class
     * @param implClass the implementation class
     * @param targetNamespace Namespace URI for service name
     * @return the <code>wsdl:portName</code> for the <code>implClass</code>
     */
//    public static String getBindingName(JavaClass implClass, String targetNamespace) {
//        QName portName = getPortName(implClass, targetNamespace);
//        if (portName != null) {
//            return (portName.getLocalPart() + BINDING);
//        }
//        return null;
//    }

    public static final int UNKNOWN = -1;
    public static final int OUTPUTINPUT = 0;
    public static final int OUTPUT = 1;
    public static final int INPUT = 2;
    
//    public static int getParamDirections(JavaClass implClass, String operationName) {
//        if ((implClass == null) || (operationName == null)) {
//             return UNKNOWN;
//        }
//        
//        Method[] methods = JMIUtils.getMethods(implClass);
//        for (Method m : methods) {
//            List<Annotation> annotations = m.getAnnotations();
//            if (annotations != null) {
//                for (Annotation a : annotations) {
//                    if ("javax.jws.WebMethod".equals(a.getType().getName())) { //NOI18N
//
//                        String operationNameAnnot = m.getName();
//
//                        List<AttributeValue> attrs = a.getAttributeValues();
//                        for (AttributeValue av : attrs) {
//                            if ("operationName".equals(av.getName())) {         //NOI18N
//                                operationNameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                        }
//                        
//                        if (operationName.equals(operationNameAnnot)) {
//                            boolean output = ((m.getType() != null) && (!"void".equals(m.getType().getName())));   //NOI18N
//                            boolean input = ((m.getParameters() != null) && (!m.getParameters().isEmpty()));
//                            if (output && input) return OUTPUTINPUT;
//                            if (output) return OUTPUT;
//                            if (input) return INPUT;
//                        }
//                    }
//                }
//            }
//        }
//        return UNKNOWN;
//    }
    
//    public static List<String> getOperationFaults(JavaClass implClass, String operationName) {
//        if ((implClass == null) || (operationName == null)) {
//             return Collections.EMPTY_LIST;
//        }
//        
//        Method[] methods = JMIUtils.getMethods(implClass);
//        for (Method m : methods) {
//            List<Annotation> annotations = m.getAnnotations();
//            if (annotations != null) {
//                for (Annotation a : annotations) {
//                    if ("javax.jws.WebMethod".equals(a.getType().getName())) { //NOI18N
//
//                        String operationNameAnnot = m.getName();
//
//                        List<AttributeValue> attrs = a.getAttributeValues();
//                        for (AttributeValue av : attrs) {
//                            if ("operationName".equals(av.getName())) {         //NOI18N
//                                operationNameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                        }
//                        
//                        if (operationName.equals(operationNameAnnot)) {
//                            List<JavaClass> exceptions = m.getExceptions();
//                            List<String> exceptionNames = new ArrayList(1);
//                            for (JavaClass exc : exceptions) {
//                                exceptionNames.add(exc.getSimpleName());
//                            }
//                            return exceptionNames;
//                        }
//                    }
//                }
//            }
//        }
//        return Collections.EMPTY_LIST;
//    }

//    public static List<String> getOperationNames(JavaClass implClass) {
//        if (implClass == null) {
//             return null;
//        }
//        
//        List<String> opNames = new ArrayList();
//        Method[] methods = JMIUtils.getMethods(implClass);
//        for (Method m : methods) {
//            List<Annotation> annotations = m.getAnnotations();
//            if (annotations != null) {
//                for (Annotation a : annotations) {
//                    if ("javax.jws.WebMethod".equals(a.getType().getName())) {  //NOI18N
//
//                        String operationNameAnnot = m.getName();
//
//                        List<AttributeValue> attrs = a.getAttributeValues();
//                        for (AttributeValue av : attrs) {
//                            if ("operationName".equals(av.getName())) {         //NOI18N
//                                operationNameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                        }
//                        
//                        opNames.add(operationNameAnnot);
//                    }
//                }
//            }
//        }
//        return opNames;
//    }
    
//    public static QName getPortTypeName(JavaClass implClass) {
//        
//        if (implClass == null) {
//             return null;
//        }
//        
//        String portTypeLocalName = implClass.getSimpleName();
//        List<Annotation> annotations = implClass.getAnnotations();
//        if (annotations != null) {
//            for (Annotation a : annotations) {
//                if ("javax.jws.WebService".equals(a.getType().getName())) { //NOI18N
//
//                    String nameAnnot = "";
//                    String targetNamespaceAnnot = "";
//
//                    List<AttributeValue> attrs = a.getAttributeValues();
//                    for (AttributeValue av : attrs) {
//                        if ("name".equals(av.getName())) {                  //NOI18N
//                            nameAnnot = ((StringLiteral)av.getValue()).getValue();
//                        }
//                        if ("targetNamespace".equals(av.getName())) {       //NOI18N
//                            targetNamespaceAnnot = ((StringLiteral)av.getValue()).getValue();
//                        }
//                    }
//                    if (nameAnnot.length() >0) {
//                        portTypeLocalName = nameAnnot;
//                    }
//        
//                    String pkg = getPackageFromClass(implClass);
//
//                    String targetNamespace = targetNamespaceAnnot;
//                    if ((targetNamespace == null) || (targetNamespace.length() == 0)) {
//                        targetNamespace = getNamespace(pkg);
//                    }
//                    QName portTypeName = new QName(targetNamespace, portTypeLocalName);
//                    return portTypeName;
//                }
//            }
//        }
//        return null;
//    }
//    
//    public static String getWsdlLocation(JavaClass implClass) {
//        
//        if (implClass == null) {
//             return null;
//        }
//        
//        List<Annotation> annotations = implClass.getAnnotations();
//        if (annotations != null) {
//            for (Annotation a : annotations) {
//                if ("javax.jws.WebService".equals(a.getType().getName())) { //NOI18N
//                    String location = null;
//                    List<AttributeValue> attrs = a.getAttributeValues();
//                    for (AttributeValue av : attrs) {
//                        if ("wsdlLocation".equals(av.getName())) {                  //NOI18N
//                            location = ((StringLiteral)av.getValue()).getValue();
//                        }
//                    }
//                    return location;
//                }
//            }
//        }
//        return null;
//    }
    
    /**
     * gets the <code>wsdl:portName</code> for a given implementation class
     * @param implClass the implementation class
     * @param targetNamespace Namespace URI for service name
     * @return the <code>wsdl:portName</code> for the <code>implClass</code>
     */
//    public static QName getPortName(JavaClass implClass, String targetNamespace) {
//        
//        if (implClass == null) {
//            return null;
//        }
//        
//        if (implClass != null) {
//            List<Annotation> annotations = implClass.getAnnotations();
//            if (annotations != null) {
//                for (Annotation a : annotations) {
//                    if ("javax.jws.WebService".equals(a.getType().getName())) { //NOI18N
//                        
//                        String portNameAnnot = "";                          //NOI18N
//                        String nameAnnot = "";                              //NOI18N
//                        String targetNamespaceAnnot = "";                   //NOI18N
//
//                        List<AttributeValue> attrs = a.getAttributeValues();
//                        for (AttributeValue av : attrs) {
//                            if ("portName".equals(av.getName())) {          //NOI18N
//                                portNameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            } 
//                            if ("name".equals(av.getName())) {              //NOI18N
//                                nameAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                            if ("targetNamespace".equals(av.getName())) {   //NOI18N
//                                targetNamespaceAnnot = ((StringLiteral)av.getValue()).getValue();
//                            }
//                        }
//
//                        String name;
//                        if (portNameAnnot.length() > 0) {
//                            name = portNameAnnot;
//                        } else if (nameAnnot.length() > 0) {
//                            name = nameAnnot + PORT;
//                        } else {
//                            name = implClass.getSimpleName() + PORT;
//                        }
//
//                        if (targetNamespace == null) {
//                            if (targetNamespaceAnnot.length() > 0) {
//                                targetNamespace = targetNamespaceAnnot;
//                            } else {
//                                String packageName = getPackageFromClass(implClass);
//                                targetNamespace = getNamespace(packageName);
//                            }
//                        }
//                        return new QName(targetNamespace, name);
//                    }
//                }
//            }
//        }
//        return null;
//    }
    
//    private static String getPackageFromClass(JavaClass cl) {
//        String fulName = cl.getName();
//        String pkg = fulName.substring(0, fulName.lastIndexOf('.'));
//        return pkg;
//    }    

}