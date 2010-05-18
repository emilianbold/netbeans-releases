/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.jaxrpc;


import com.sun.xml.rpc.processor.model.java.JavaException;
import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;

import org.netbeans.modules.websvc.registry.util.Util;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple writer to write the Java Source.
 * @author  Winston Prakash
 */
public class WrapperClientWriter extends java.io.PrintWriter {
    
    private String serviceName;
    private String serviceVariable;
    
    private String className;
    private String superClassName;
    private Set interfaces = new HashSet();
    private String packageName;
    private Set imports = new HashSet();
    private Set ports = new HashSet();
    
    private Set constructorStatements = new HashSet();
    
    int indent = 0;
    
    /** Creates a new instance of JavaWriter */
    public WrapperClientWriter(Writer writer){
        super(writer);
    }
    
    public void setContainedClassInfo(String serviceName){
        this.serviceName = serviceName;
        serviceVariable = serviceName.substring(serviceName.lastIndexOf('.') + 1, serviceName.length());
        serviceVariable = serviceVariable.toLowerCase() + "1";
    }
    
    /** Set package name */
    public void setPackage(String pkgName){
        packageName = pkgName;
    }
    
    public void addImport(String importLine){
        imports.add(importLine);
    }
    
    /** Set the name of the class */
    public void setName(String name){
        className = name;
    }
    
    /** Set the name of the super class this class would extends */
    public void setSuperClass(String superClass){
        superClassName = superClass;
    }
    
    /** Set the name of the interfaces this class would extends */
    public void addInterface(String interfaceName){
        interfaces.add(interfaceName);
    }
    
    public void addConstructorStatements(String statement){
        constructorStatements.add(statement);
    }
    
    public void addPort(Port inPort){
        ports.add(inPort);
    }
    
    public void writeClass(){
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
		println("import javax.xml.rpc.ServiceException;");
        println("import javax.xml.rpc.Stub;");
        println();
        
        /**
         * Write the class declaration
         */
        print("public class " + className);
        if(superClassName != null) print("extends " + superClassName + " ");
        if (!interfaces.isEmpty()) {
            println("implements ");
            Iterator iter = interfaces.iterator();
            while(iter.hasNext()) {
                print((String)iter.next());
                if(iter.hasNext()) print(",");
            }
        }
        println(" {");
        println();
        
        /**
         * write the class instance variables.
         */
        
        /**
         * write a variable for the service implementation.
         */
        println("  private " + serviceName + " " + serviceVariable + " = " + "new " + serviceName + "_Impl();");
        
        /**
         * write a variable for each port in the serviee.
         */
        if(!ports.isEmpty()) {
            Iterator portIterator = ports.iterator();
            Port currentPort = null;
            while(portIterator.hasNext()) {
                currentPort = (Port)portIterator.next();
                /**
                 * get the Java class name for the port.
                 */
               String portImplName = Util.getProperPortName(currentPort.getName().getLocalPart());
               String portInterfaceName = currentPort.getJavaInterface().getName();
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
                
                
                println("  private " + portInterfaceName + " " + portInterfaceVariable + ";"); // " + portInterfaceVariable +  " = " + serviceVariable + ".get" +portImplName +"();");
				addConstructorStatements(portInterfaceVariable + " = " + serviceVariable + ".get" + portImplName +"()");
                println("  private Stub " + portInterfacePrefix + "Stub;"); //  = (Stub)" + portInterfaceVariable + ";");
				addConstructorStatements(portInterfacePrefix + "Stub = (Stub)" + portInterfaceVariable);
            }
            
            println();
            // Write the constructor
            println("  public " + className + "() throws ServiceException {");
            if (!constructorStatements.isEmpty()) {
                Iterator iter = constructorStatements.iterator();
                while(iter.hasNext()) {
                    println("    " + (String)iter.next() + ";");
                }
            }
            println("  }");
            println();
            
            printOperations(ports);
            println("}");
            
        }
    }
    
    private void printOperations(Set inPorts) {
        Iterator portIterator = inPorts.iterator();
        Port currentPort = null;
        while(portIterator.hasNext()) {
            currentPort = (Port)portIterator.next();
            /**
             * Get the name for the current port.
             */
            String lowercasePortName = Util.getProperPortName(currentPort.getName().getLocalPart()).toLowerCase();
            /**
             * get the Java class name for the port.
             */
            String portInterfaceName = currentPort.getJavaInterface().getName();
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
            Iterator operationsIterator = currentPort.getOperations();
            Operation currentOperation = null;
            while(operationsIterator.hasNext()) {
                currentOperation = (Operation)operationsIterator.next();
                if(null == currentOperation) {
                    continue;
                }
                JavaMethod method = currentOperation.getJavaMethod();
                String modifiedMethodName = Util.upperCaseFirstChar(method.getName());
                println();
                print("  public " + method.getReturnType().getRealName() + " ");
                print(lowercasePortName + modifiedMethodName + "(");
                Iterator params = method.getParameters();
				String parameterType = "";
				
                while (params.hasNext()) {
                    JavaParameter param = (JavaParameter)params.next();
                    /**
                     * Bug fix: 5059732
                     * If the parameter is a "Holder" we need the holder type and not the JavaType.  This is
                     * typically the case when there is no return type and the parameter's meant to be mutable, pass-by-reference
                     * type parameters.  I took the code below directly from the JAX-RPC class:
                     * "com.sun.xml.rpc.processor.generator.StubGenerator"
                     * - David Botterill 6/8/2004
                     */
                    
                    parameterType = Util.getParameterType(currentPort,param);
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
                    print("       return " + portInterfaceVariable + "." + method.getName()+ "(");
                }else{
                    print("       " + portInterfaceVariable + "." + method.getName() + "(");
                }
                params = method.getParameters();
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
            }
            
            /**
             * Now print out the methods for setting the Stub properties.
             */
            println("public void " + lowercasePortName + "SetUsername(String inUserName) {");
            println("        " + portInterfacePrefix + "Stub._setProperty(Stub.USERNAME_PROPERTY, inUserName);");
            println("}");
            println();
            println("public void " + lowercasePortName + "SetPassword(String inPassword) {");
            println("        " + portInterfacePrefix + "Stub._setProperty(Stub.PASSWORD_PROPERTY, inPassword);");
            println("}");
            println();
            println("public void " + lowercasePortName + "SetAddress(String inAddress) {");
            println("        " + portInterfacePrefix + "Stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, inAddress);");
            println("}");
            println();
        }
        
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
