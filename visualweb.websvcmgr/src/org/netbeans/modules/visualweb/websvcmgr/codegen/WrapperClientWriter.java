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

import java.io.PrintWriter;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    private Port port;
    private String className;
    
    private Set constructorStatements = new HashSet();
    
    int indent = 0;
    
    private Set dataProviders = new HashSet();
    
    /** Creates a new instance of JavaWriter */
    public WrapperClientWriter(Writer writer){
        super(writer);
        
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
    
    public void setPort( Port inPort ) {
        this.port = inPort;
    }
   
    public void writeClass() {
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
        println("import java.rmi.RemoteException;");
        println("import javax.xml.rpc.Stub;");
        println("import javax.xml.rpc.ServiceException;");
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
        println("  private " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "_Impl();");
        
        // write a variable for the port

        // get the Java class name for the port
        String portImplName = Util.getProperPortName(port.getName().getLocalPart());
        String portInterfaceName = port.getJavaInterface().getName();
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
        println("  public " + className + "() {");
        println("    try {");
        println("      " + portInterfaceVariable + " = " + serviceVariable + ".get" +portImplName +"();");
        println("    } catch (ServiceException se) {");
        println("      se.printStackTrace();" );
        println("    }");

        println("  }");
        println();

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
    
    private void printOperations(Port port) {
        
        // This is to keep track of the overloadded method names
        Map methodNames = new HashMap();
        
        /**
         * get the Java class name for the port.
         */
        String portInterfaceName = port.getJavaInterface().getName();
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
            currentOperation = (Operation)operationsIterator.next();
            if(null == currentOperation) {
                continue;
            }
            
            JavaMethod method = currentOperation.getJavaMethod();
            println();
            print("  public " + method.getReturnType().getRealName() + " ");
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

            /**
             * We need a "throws" at least for the RemoteException
             *
             */
            print(" throws ");

            while (exceptions.hasNext()) {
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
                    /**
                     * always print a "," because we have the RemoteException following.
                     */
                    print(", ");
                }
            }
            println(" RemoteException { ");

            if(!"void".equals(method.getReturnType().getRealName())){
                println( "      if( Beans.isDesignTime() && !testMode )" );
                println( "        return " + designTimeReturnValue(method) + ";" );
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
        // Cast the inteface to Stub
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
    }
    
    private String designTimeReturnValue( JavaMethod method ) {
        
        String fakeReturn = "null";

        String returnType = method.getReturnType().getRealName();

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
