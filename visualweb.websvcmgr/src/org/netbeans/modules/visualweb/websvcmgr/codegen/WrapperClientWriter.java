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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.visualweb.websvcmgr.model.WebServiceData;

import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaException;
import com.sun.tools.ws.processor.model.java.JavaParameter;

/*
import com.sun.xml.rpc.processor.model.java.JavaException;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;
*/

/*
 * SD
import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaException;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaMethod;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.java.JavaParameter;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.Port;
import org.netbeans.modules.visualweb.xml.rpc.processor.model.Operation;
*/

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
    private WsdlPort port;
    private String className;
    private WebServiceData wsData;
    
    private Set constructorStatements = new HashSet();
    
    int indent = 0;
    
    private Set dataProviders = new HashSet();
    private List<java.lang.reflect.Method> sortedMethods;
    boolean isJaxRpc = false;
    
    /** Creates a new instance of JavaWriter */
    public WrapperClientWriter(Writer writer, WebServiceData wsData, boolean isJaxRpc, List<java.lang.reflect.Method> sortedMethods){
        super(writer);
        
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
    
    /** Set the name of the super class this class would extends */
    public void setSuperClass(String superClass){
        superClassName = superClass;
    }
    
    public void setPort(WsdlPort inPort ) {
        this.port = inPort;
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
            URL url = new File(wsData.getURL()).toURI().toURL();
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
            String namespace = wsData.getWsdlService().getNamespaceURI();
            String qname = wsData.getWsdlService().getName();
            
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
        Iterator operationsIterator = port.getOperations().iterator();

        Operation currentOperation = null;
        while(operationsIterator.hasNext()) {
            WsdlOperation oper = (WsdlOperation) operationsIterator.next();
            currentOperation = (Operation)oper.getInternalJAXWSOperation();
            if(null == currentOperation) {
                continue;
            }
            
            JavaMethod method = currentOperation.getJavaMethod();
            println();
            
            /**
             *  XXX The return types may differ between JAX-RPC and JAX-WS; since
             *  the model was created for JAX-WS, an alternate method of discovering
             *  the return values is used (reflection on the port interface methods)
             */
            String methodReturnTypeName = (isJaxRpc) ? getJaxRpcReturnType(method) : method.getReturnType().getRealName();
            
            print("  public " + methodReturnTypeName + " ");
            print(method.getName() + "(");
            Iterator params = method. getParametersList().iterator();
            String parameterType = "";

            while (params.hasNext()) {
                JavaParameter param = (JavaParameter)params.next();
                /**
                 * Bug fix: 5059732
                 * If the parameter is a "Holder" we need the holder type and not the JavaType.  This is
                 * typically the case when there is no return type and the parameter's meant to be mutable, pass-by-reference
                 * type parameters.
                 * - David Botterill 6/8/2004
                 */
 
                parameterType = Util.getParameterType(port,param);
                /**
                 * end of bug fix: 5059732
                 */

                print(parameterType + " " + param.getName());
                if(params.hasNext()) write(", ");
            }
            print(") ");
            Iterator exceptions = method.getExceptions();
            /**
             * Bugid: 4970323 This area of code was not exercised before WSDLInfo was fixed.  Hence this bug showed up as a result of
             * fixing a bug.
             *
             */

            boolean firstException = true;
            while (exceptions.hasNext()) {
                if (firstException == true) {
                    firstException = false;
                    print(" throws ");
                }else {
                    print(", ");
                }
                                
                /**
                 * Bugid: 4970323 - The return type of an exceptions is a String not a JavaException.  This
                 * can only be know for sure by reading the current JavaException code since the "JavaMethod.getExceptions()"
                 * method returns an Iterator and the javadoc says nothing of it being a String.
                 *
                 * This area of code was not exercised before WSDLInfo was fixed.  Hence this bug showed up as a result of
                 * fixing a bug.
                 */
                /**
                 * Make sure we don't get back a null or an empty String.  Check to make sure
                 * the Object is a string in case the API matures and JavaException turns back into a real object with
                 * a "getName" method.
                 */
                Object currentException = exceptions.next();
                if(null != currentException &&
                currentException instanceof String &&
                ((String)currentException).length() > 0) {
                    print((String)currentException);
                }
            }
            
            // only throw RemoteException in jaxrpc
            if (isJaxRpc && firstException) {
                println(" throws RemoteException { ");
            }else if (isJaxRpc) {
                println(", RemoteException { ");
            }else {
                println(" { ");
            }

            if(!"void".equals(methodReturnTypeName)){
                println( "      if( Beans.isDesignTime() && !testMode )" );
                println( "        return " + designTimeReturnValue(methodReturnTypeName) + ";" );
                println( "      else");
                print("         return " + portInterfaceVariable + "." + method.getName()+ "(");
            }else{
                println( "      if( Beans.isDesignTime() ) " );
                println( "        return;" );
                println( "      else " );
                print("         " + portInterfaceVariable + "." + method.getName() + "(");
            }
            params = method.getParametersList().iterator();
            while (params.hasNext()) {
                JavaParameter param = (JavaParameter)params.next();
                //if(param.isHolder()){
                //print(param.getHolderName());
                //}else {
                print(param.getName());
                //}
                if(params.hasNext()) write(", ");
            }
            println(");");


            println("  }");

            // If this method return non-void, we'll need to generate DataProvider (readonly for now) for it
            // For overloaded method, we'll index method names
            if(!"void".equals(method.getReturnType().getRealName()))
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

                dataProviders.add( new DataProviderInfo( packageName, className, method, dpClassName ) ); // NOI18N
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
    
    private String getJaxRpcReturnType(JavaMethod method) {
        String modelMethodName = method.getName();
        int index = doBinarySearch(sortedMethods, modelMethodName);
                
        // search forwards and backwards
        for (int i = index; i < sortedMethods.size() && i >= 0; i++) {
            java.lang.reflect.Method nextMethod = sortedMethods.get(i);
            if (methodsEqual(nextMethod, method)) {
                return nextMethod.getReturnType().getCanonicalName();
            }else if (!nextMethod.getName().equals(method.getName())) {
                break;
            }
        }
        
        for (int i = index; i >= 0; i--) {
            java.lang.reflect.Method nextMethod = sortedMethods.get(i);
            if (methodsEqual(nextMethod, method)) {
                return nextMethod.getReturnType().getCanonicalName();
            }else if (!nextMethod.getName().equals(method.getName())) {
                break;
            }            
        }
        
        return method.getReturnType().getRealName();
    }
    
    private boolean methodsEqual(java.lang.reflect.Method realMethod, JavaMethod modelMethod) {
        if (!realMethod.getName().equals(modelMethod.getName())) {
            return false;
        }else {
            List<JavaParameter> modelParams = modelMethod.getParametersList();
            Class<? extends Object>[] realParams = realMethod.getParameterTypes();
            
            if (realParams.length != realParams.length) {
                return false;
            }
            
            for (int i = 0; i < realParams.length; i++) {
                String modelNext = modelParams.get(i).getType().getRealName();
                String realNext = realParams[i].getCanonicalName();
                
                if (!modelNext.equals(realNext)) {
                    return false;
                }
            }
            
            return true;
        }
    }
    
    private static int doBinarySearch(List<java.lang.reflect.Method> methods, String name) {
        int low = 0;
        int high = methods.size();
        
        while (low < high) {
            int mid = (low + high) / 2;
            String nextMethod = methods.get(mid).getName();
            int compare = nextMethod.compareTo(name);
            if (compare == 0) {
                return mid;
            }else if (compare < 0) {
                low = mid + 1;
            }else if (compare > 0) {
                high = mid;
            }
        }

        return -1;
    }
    
    public class Method {
        Set methodStatement = new HashSet();
        public Method(JavaMethod method){
        }
        
        public void addStatement(String statement){
            methodStatement.add(statement);
        }
        
        public Set getStatements(){
            return methodStatement;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    }
    
}
