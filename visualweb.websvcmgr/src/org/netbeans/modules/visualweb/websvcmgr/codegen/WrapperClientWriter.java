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

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import com.sun.tools.ws.processor.model.Operation;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor;
import org.netbeans.modules.visualweb.websvcmgr.codegen.DataProviderInfo;

import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.ErrorManager;

/**
 * A simple writer to write the Java Source.
 * @author  Winston Prakash, cao
 */
public class WrapperClientWriter extends java.io.PrintWriter {
    
    private String serviceName;
    private String serviceVariable;
    
    private String superClassName;
    private Set interfaces = new HashSet();
    private String packageName;
    private Set imports = new HashSet();
    private String className;
    private WebServiceDescriptor wsData;
    private WsdlPort port;
    private List<WsdlOperation> operations;
    
    int indent = 0;
    
    private Set dataProviders = new HashSet();
    private List<java.lang.reflect.Method> sortedMethods;
    boolean isJaxRpc = false;
    
    /** Creates a new instance of JavaWriter */
    public WrapperClientWriter(Writer writer, WebServiceDescriptor wsData, boolean isJaxRpc, List<java.lang.reflect.Method> sortedMethods, List<WsdlOperation> operations){
        super(writer);
        
        this.operations = operations;
        this.sortedMethods = sortedMethods;
        this.isJaxRpc = isJaxRpc;
        this.wsData = wsData;
        // Always implements java.io.Seriazable
        interfaces.add( "java.io.Serializable" );
    }
    
    public void setContainedClassInfo(String serviceName){
        this.serviceName = serviceName;
        serviceVariable = serviceName.substring(serviceName.lastIndexOf('.') + 1, serviceName.length());
        serviceVariable = serviceVariable.toLowerCase() + "1";
    }
    
    public Set getDataProviders() {
        return this.dataProviders;
    }
    
    /** Set package name */
    public void setPackage(String pkgName){
        packageName = pkgName;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public void addImport(String importLine){
        imports.add(importLine);
    }
    
    public void setPort(WsdlPort port) {
        this.port = port;
    }
    
    /** Set the name of the super class this class would extends */
    public void setSuperClass(String superClass){
        superClassName = superClass;
    }
    
    public void writeClass() throws IOException {
        /**
         * Write the package statement
         */
        println("package " + packageName + ";");
        println();
        
        
        /**
         * Write the imports statement.
         */
        if (!imports.isEmpty()) {
            Iterator iter = imports.iterator();
            while(iter.hasNext()) {
                println("import " + iter.next() + ";");
            }
            println();
        }
        
        if (isJaxRpc) {
            println("import java.rmi.RemoteException;");
            println("import javax.xml.rpc.Stub;");
            println("import javax.xml.rpc.ServiceException;");
        }else if (!isJaxRpc) {
            println("import javax.xml.ws.BindingProvider;");
        }
        
        println("import java.beans.Beans;");
        println();
        
        /**
         * Write the class declaration
         */
        
        // The class name will be the port display + "Client" 
        print("public class " + className);
        if(superClassName != null) 
            print(" extends " + superClassName + " ");
        else 
            print( " " );
        if (!interfaces.isEmpty()) {
            print("implements ");
            Iterator iter = interfaces.iterator();
            while(iter.hasNext()) {
                print((String)iter.next());
                if(iter.hasNext()) print(",");
            }
        }
        println(" {");
        println();
        
        // write the class instance variables.
        
        // write a variable for the service implementation.
        if (isJaxRpc) {
            println("  private " + serviceName + " " + serviceVariable + ";");
        }else {
            println("  private " + serviceName + " " + serviceVariable + ";");
        }
        
        // write a variable for the port

        // get the Java class name for the port
        String portImplName = Util.getProperPortName(port.getName());
        String portInterfaceName = port.getJavaName();
        /**
         * Strip off the leading package qualification since we don't need it.
         */
        portInterfaceName = portInterfaceName.substring(portInterfaceName.lastIndexOf('.') + 1, portInterfaceName.length());
        /**
         *  create the variable name for the port
         * NOTE: - When we write out the methods, we'll need to use the same convention of naming the port that
         * we do here.
         */
        String portInterfaceVariable = portInterfaceName.substring(portInterfaceName.lastIndexOf('.') + 1, portInterfaceName.length());
        String portInterfacePrefix = portInterfaceVariable.toLowerCase() ;
        portInterfaceVariable = portInterfaceVariable.toLowerCase() + "1";
        /**
         * Apparently, the compiletool uppercases the first letter of the port name to make it a proper getter so we need to do the same.
         */
        String modifiedPortName = Util.upperCaseFirstChar(portInterfaceName);

        println("  private " + portInterfaceName + " " + portInterfaceVariable +  ";");
        println();
        
        // Variable to indicate whehther it is in test mode or ot
        println("  private boolean testMode = false;");

        // Write the constructor
        if (isJaxRpc) {
            println("  public " + className + "() {");
            println("    System.setProperty(\"javax.xml.soap.MessageFactory\",\"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl\");");
            println("    try {");
            println("      " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "_Impl();");
            println("      " + portInterfaceVariable + " = " + serviceVariable + ".get" +portImplName +"();");
            println("    } catch (ServiceException se) {");
            println("      se.printStackTrace();" );
            println("    }");
            
            println("  }");
            println();
        }else {
            // Instantiate the Service class, specifying the wsdl URL kept in the
            // web service jar
            URL url = wsData.getWsdlUrl();
            String urlPath = url.getPath();
            int start;
            if (url.getProtocol().toLowerCase().startsWith("file")) { // NOI18N
                start = urlPath.lastIndexOf(System.getProperty("path.separator"));
                start = (start < 0) ? urlPath.lastIndexOf("/"): start;
            }else {
                start = urlPath.lastIndexOf("/");
            }
            start = (start < 0 || start >= urlPath.length()-1) ? 0 : start + 1;
            
            String wsdlFileName = urlPath.substring(start);
            String namespace = wsData.getModel().getNamespaceURI();
            String qname = wsData.getName();
            
            println("  public " + className + "() {");
            println("    " + "java.net.URL wsdl = this.getClass().getResource(\"" + wsdlFileName + "\");");
            println("    " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "(wsdl, new javax.xml.namespace.QName(\"" + namespace + "\", \"" + qname + "\"));");
            println("    " + portInterfaceVariable + " = " + serviceVariable + ".get" +portImplName +"();");
            println("  }");
            println();
        }

        // Now the methods
        printOperations(port);
        
        // The method for turning on/off the test mode
        println( "  public void testMode( Boolean testing) {");
        println( "    this.testMode = testing.booleanValue();");
        println( "  }");
        println();
        
        // End of class
        println("}");
    }
    
    private void printOperations(WsdlPort port) {
        
        // This is to keep track of the overloadded method names
        Map methodNames = new HashMap();
        
        /**
         * get the Java class name for the port.
         */
        String portInterfaceName = port.getJavaName();
        /**
         * Strip off the leading package qualification since we don't need it.
         */
        portInterfaceName = portInterfaceName.substring(portInterfaceName.lastIndexOf('.') + 1, portInterfaceName.length());
        /**
         *  create the variable name for the port
         * NOTE: -This variable name needs to be the same one written out for the class instance varables.
         */
        String portInterfaceVariable = portInterfaceName.substring(portInterfaceName.lastIndexOf('.') + 1, portInterfaceName.length());
        String portInterfacePrefix = portInterfaceVariable.toLowerCase() ;
        portInterfaceVariable = portInterfaceVariable.toLowerCase() + "1";

        for(int j = 0; j < sortedMethods.size(); j++) {
            java.lang.reflect.Method method = sortedMethods.get(j);
            Operation operation = getCorrespondingOperation(method, operations);
            
            if (operation == null) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Web Service client method not found in model - " + method.getName());
                continue;
            }
            
            println();
            
            String methodReturnTypeName = method.getReturnType().getCanonicalName();
            String methodName = method.getName();
            
            print("  public " + methodReturnTypeName + " " + methodName + "(");
            
            Class[] parameters = method.getParameterTypes();
            for (int i = 0; i < parameters.length; i++) {
                if (i != 0) {
                    print(",");
                }
                            
                print(parameters[i].getCanonicalName() + " " + "arg" + i);
            }
            print(") ");
            
            Class[] exceptions = method.getExceptionTypes();
            for (int i = 0; i < exceptions.length; i++) {
                if (i == 0) {
                    print(" throws ");
                }else {
                    print(", ");
                }
                
                print(exceptions[i].getCanonicalName());
            }
            println(" {");
            
            if(!"void".equals(methodReturnTypeName)){
                println( "      if( Beans.isDesignTime() && !testMode )" );
                println( "        return " + designTimeReturnValue(methodReturnTypeName) + ";" );
                println( "      else");
                print("         return " + portInterfaceVariable + "." + methodName + "(");
            }else{
                println( "      if( Beans.isDesignTime() ) " );
                println( "        return;" );
                println( "      else " );
                print("         " + portInterfaceVariable + "." + methodName + "(");
            }
            
            for (int i = 0; i < parameters.length; i++) {
                print("arg" + i);
                if (i + 1 < parameters.length) print(", ");
            }
            println(");");
            println("  }");

            // If this method return non-void, we'll need to generate DataProvider (readonly for now) for it
            // For overloaded method, we'll index method names
            if(!"void".equals(methodReturnTypeName))
            {
                String dpClassName = method.getName();

                Integer occurrence = (Integer)methodNames.get( method.getName() );
                if( occurrence == null )
                    occurrence = new Integer(1);
                else
                {
                    occurrence = new Integer( occurrence.intValue() + 1 );

                    // The data provider class name = method name + num of occurrence 
                    dpClassName = method.getName() + occurrence;
                }
                methodNames.put( method.getName(), occurrence ); 

                dataProviders.add( new DataProviderInfo( packageName, className, operation.getJavaMethod() , dpClassName ) ); // NOI18N
            }
        }

        /**
         * Now print out the methods for setting the Stub properties.
         */
        // Cast the inteface to Stub (jaxrpc)
        
        if (isJaxRpc) {
            String stubVar = "((Stub)" + portInterfaceVariable + ")";
            
            println("  public void setUsername(String inUserName) {");
            println("        " + stubVar + "._setProperty(Stub.USERNAME_PROPERTY, inUserName);");
            println("  }");
            println();
            
            println("  public void setPassword(String inPassword) {");
            println("        " + stubVar + "._setProperty(Stub.PASSWORD_PROPERTY, inPassword);");
            println("  }");
            println();
            
            println("  public void setAddress(String inAddress) {");
            println("        " + stubVar + "._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, inAddress);");
            println("  }");
            println();
        }else {
            String bindingVar = "((BindingProvider)" + portInterfaceVariable + ")";
            
            println("  public void setUsername(String inUserName) {");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.USERNAME_PROPERTY, inUserName);");
            println("  }");
            println();
            
            println("  public void setPassword(String inPassword) {");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, inPassword);");
            println("  }");
            println();
            
            println("  public void setAddress(String inAddress) {");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, inAddress);");
            println("  }");
            println();
        }
    }
        
    private Operation getCorrespondingOperation(java.lang.reflect.Method method, List<WsdlOperation> operations) {
        Operation result = null;
        String methodName = method.getName();
        Class[] parameters = method.getParameterTypes();
        for (WsdlOperation op : operations) {
            String opName = op.getJavaName();
            List<WsdlParameter> opParameters = op.getParameters();
            
            // check the method names (case-sensitivity is ignored in some cases
            // due to the differences between JAX-WS and JAX-RPC operation naming
            // conventions)
            if (!opName.equalsIgnoreCase(methodName) ||
                (!opName.equals(methodName) && result != null)) {
                continue;
            }
            
            // check parameter signatures
            if (opParameters.size() != parameters.length) {
                continue;
            }
            
            boolean matches = true;
            for (int i = 0; i < parameters.length; i++) {
                WsdlParameter opParam = opParameters.get(i);
                if (opParam.isHolder() && isJaxRpc) {
                    Class[] interfaces = parameters[i].getInterfaces();
                    boolean hasHolderImpl = false;
                    for (int j = 0; j < interfaces.length; j++) {
                        if (interfaces[i].getName().equals("javax.xml.rpc.holders.Holder")) { // NOI18N
                            hasHolderImpl = true;
                            break;
                        }
                    }
                    matches = matches && hasHolderImpl;
                }else if (opParam.isHolder()) {
                    matches = matches && parameters[i].getName().equals("javax.xml.ws.Holder"); //NOI18N
                }else {
                    matches = matches && parameters[i].getName().equals(opParam.getTypeName());
                }
            }
            
            if (matches) {
                result = (Operation)op.getInternalJAXWSOperation();
            }
            
        }
        
        return result;
    }
    
    private String designTimeReturnValue( String returnType ) {
        
        String fakeReturn = "null";

        // Can be one of the following:
        // int, long, double, float, short, byte
        // char
        // boolean
        // void
        if( returnType.equals( "int" ) ||
            returnType.equals( "long" ) ||
            returnType.equals( "double" ) ||
            returnType.equals( "float" ) ||
            returnType.equals( "short" ) ||
            returnType.equals( "byte" ) )
        {
            return "0";
        }
        else if( returnType.equals( "boolean" ) )
        {
            return "false";
        }
        else if( returnType.equals( "char" ) )
        {
            return "\'A\'";
        }
        else if( returnType.equals( "java.lang.String" ) )
        {
            return "\"ABC\"";
        }
        else 
            // return null for all object return type
            return null;
    }
}
