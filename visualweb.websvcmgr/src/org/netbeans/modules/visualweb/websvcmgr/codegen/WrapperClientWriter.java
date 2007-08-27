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
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaType;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.visualweb.websvcmgr.WebServiceDescriptor;

import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;

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
    private final List<WsdlOperation> operations;
    
    int indent = 0;
    
    private Set<DataProviderInfo> dataProviders = new HashSet<DataProviderInfo>();
    private final List<java.lang.reflect.Method> sortedMethods;
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
        println("  private boolean initialized = false;");

        // Write the constructor
        println("  public " + className + "() {");
        println("  }");
        
        // Now the methods
        printOperations(port);
        
        // The method for turning on/off the test mode
        println( "  public void testMode( Boolean testing) {");
        println( "    this.testMode = testing.booleanValue();");
        println( "  }");
        println();
        
        // write the initialize method (moved from the constructor to avoid unnecessary
        // W/S instantiation during designtime)
        if (isJaxRpc) {
            println("  public void initialize() {");
            println("    if (initialized) return;");
            println("    System.setProperty(\"javax.xml.soap.MessageFactory\",\"com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl\");");
            println("    try {");
            println("      " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "_Impl();");
            println("      " + portInterfaceVariable + " = " + serviceVariable + ".get" +portImplName +"();");
            println("      initialized = true;");
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
            
            println("  public void initialize() {");
            println("    if (initialized) return;");
            println("    " + "java.net.URL wsdl = this.getClass().getResource(\"" + wsdlFileName + "\");");
            println("    " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "(wsdl, new javax.xml.namespace.QName(\"" + namespace + "\", \"" + qname + "\"));");
            println("    " + portInterfaceVariable + " = " + serviceVariable + ".get" +portImplName +"();");
            println("    initialized = true;");
            println("  }");
            println();
        }

        
        // End of class
        println("}");
    }
    
    private void printOperations(WsdlPort port) {
        
        // This is to keep track of the overloadded method names
        Map<String, Integer> methodNames = new HashMap<String, Integer>();
        
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
        
        // Use the internal model if doing jax-ws, otherwise use java reflection
        Iterator<DataProviderMethod> methodIterator = null;
        if (isJaxRpc) {
            methodIterator = new Iterator<DataProviderMethod>() {
                private int i = 0;
                public boolean hasNext() {
                    return i < sortedMethods.size();
                }

                public DataProviderMethod next() {
                    java.lang.reflect.Method method = sortedMethods.get(i++);
                    return new DataProviderJavaMethod(method);
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }else {
            methodIterator = new Iterator<DataProviderMethod>() {
                private int i = 0;
                public boolean hasNext() {
                    return i < operations.size();
                }

                public DataProviderMethod next() {
                    Operation nextOperation = (Operation)operations.get(i++).getInternalJAXWSOperation();
                    return new DataProviderModelMethod(nextOperation.getJavaMethod());
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        
        
        while (methodIterator.hasNext()) {
            DataProviderMethod method = methodIterator.next();
            
            println();
            
            String methodReturnTypeName = method.getMethodReturnType();
            String methodName = method.getMethodName();
            
            print("  public " + methodReturnTypeName + " " + methodName + "(");

            boolean firstParameter = true;
            for (DataProviderParameter parameter : method.getParameters()) {
                if (!firstParameter) {
                    print(",");
                }else {
                    firstParameter = false;
                }
                
                print(parameter.getType() + " " + parameter.getName());
            }
            print(") ");
            
            boolean firstException = true;
            for (String exceptionClass : method.getExceptions()) {
                if (firstException) {
                    print(" throws ");
                    firstException = false;
                }else {
                    print(", ");
                }
                
                print(exceptionClass);
            }
            println(" {");
            
            if(!"void".equals(methodReturnTypeName)){
                println( "      if( Beans.isDesignTime() && !testMode )" );
                println( "        return " + designTimeReturnValue(methodReturnTypeName) + ";" );
                println( "      else {");
                println( "         initialize();");
                print(   "         return " + portInterfaceVariable + "." + methodName + "(");
            }else{
                println( "      if( Beans.isDesignTime() && !testMode ) " );
                println( "        return;" );
                println( "      else {" );
                println( "        initialize();");
                print(   "         " + portInterfaceVariable + "." + methodName + "(");
            }
            
            firstParameter = true;
            for (DataProviderParameter parameter : method.getParameters()) {
                if (!firstParameter) {
                    print(", ");
                }else {
                    firstParameter = false;
                }
                print(parameter.getName());
            }
            println(");");
            println("      }");
            println("  }");

            // If this method return non-void, we'll need to generate DataProvider (readonly for now) for it
            // For overloaded method, we'll index method names
            if ( (!isJaxRpc && Util.hasOutput(((DataProviderModelMethod)method).getJavaMethod())) ||
                 (isJaxRpc && !"void".equals(method.getMethodReturnType()))) { // NOI18N
                String dpClassName = method.getMethodName();

                Integer occurrence = methodNames.get( method.getMethodName() );
                if( occurrence == null )
                    occurrence = new Integer(1);
                else
                {
                    occurrence = new Integer( occurrence.intValue() + 1 );

                    // The data provider class name = method name + num of occurrence 
                    dpClassName = method.getMethodName() + occurrence;
                }
                methodNames.put( method.getMethodName(), occurrence ); 
                
                DataProviderInfo info = new DataProviderInfo( packageName, className, method , dpClassName );
                dataProviders.add(info);
                
                if (!isJaxRpc) {
                    int index = Util.getOutputHolderIndex(((DataProviderModelMethod)method).getJavaMethod());
                    info.setOutputHolderIndex(index);
                }
            }
        }

        /**
         * Now print out the methods for setting the Stub properties.
         */
        // Cast the inteface to Stub (jaxrpc)
        
        if (isJaxRpc) {
            String stubVar = "((Stub)" + portInterfaceVariable + ")";
            
            println("  public void setUsername(String inUserName) {");
            println("        initialize();");
            println("        " + stubVar + "._setProperty(Stub.USERNAME_PROPERTY, inUserName);");
            println("  }");
            println();
            
            println("  public void setPassword(String inPassword) {");
            println("        initialize();");
            println("        " + stubVar + "._setProperty(Stub.PASSWORD_PROPERTY, inPassword);");
            println("  }");
            println();
            
            println("  public void setAddress(String inAddress) {");
            println("        initialize();");
            println("        " + stubVar + "._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, inAddress);");
            println("  }");
            println();
        }else {
            String bindingVar = "((BindingProvider)" + portInterfaceVariable + ")";
            
            println("  public void setUsername(String inUserName) {");
            println("        initialize();");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.USERNAME_PROPERTY, inUserName);");
            println("  }");
            println();
            
            println("  public void setPassword(String inPassword) {");
            println("        initialize();");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, inPassword);");
            println("  }");
            println();
            
            println("  public void setAddress(String inAddress) {");
            println("        initialize();");
            println("        " + bindingVar + ".getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, inAddress);");
            println("  }");
            println();
        }
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
