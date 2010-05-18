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

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import com.sun.tools.ws.processor.model.java.JavaParameter;
import java.util.List;
import org.netbeans.modules.visualweb.websvcmgr.util.Util;


import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;

/**
 * Creates the DataProvider Java source code for the given method
 * @author  cao
 */
public class DataProviderWriter extends java.io.PrintWriter {

    private static String[] PRIMITIVE_TYPES = {
        "char", "byte", "short", "int", "long", "float", "double", "boolean"
    };
    
    private static String[] PRIMITIVE_WRAPPERS = {
        "Character", "Byte", "Short", "Integer", "Long", "Float", "Double", "Boolean"
    };
    
    private DataProviderInfo dataProviderInfo;
    private Set imports = new HashSet();
    private boolean isJ2EE_15;
    private ClassLoader classLoader;
    
    public DataProviderWriter(Writer writer, DataProviderInfo dataProviderInfo, boolean isJ2EE_15 ){
        super(writer);
        this.dataProviderInfo = dataProviderInfo;
        this.isJ2EE_15 = isJ2EE_15;
    }
    
    public void addImport(String importLine){
        imports.add(importLine);
    }
    
    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }
    
    public void writeClass(){
        boolean isPrimitiveReturnType = Util.isPrimitiveType(dataProviderInfo.getMethod().getMethodReturnType());
        
        // package
        println( "package " + dataProviderInfo.getPackageName() + ";" );
        
        // comments
        println( "/**" );
        println( " * Source code created on " + new Date() );
        println( " */" );
        println();
        
        // Import
        if (!imports.isEmpty()) {
            Iterator iter = imports.iterator();
            while(iter.hasNext()) {
                println("import " + iter.next() + ";");
            }
            println();
        }
        println( "import com.sun.data.provider.*;" );
        println( "import com.sun.data.provider.impl.*;" );
        println( "import java.lang.reflect.Method;" );
        if (isJ2EE_15) {
            println( "import java.lang.reflect.ParameterizedType;" );
            println( "import java.lang.reflect.Type;" );
        }
        println( "import java.beans.*;" );
        println( "import java.util.ArrayList;" );
        println();
        
        // start of class
        // Always extends from "MethodResultTableDataProvider
        String dpSuperClassName = "MethodResultTableDataProvider";
        
        String className = dataProviderInfo.getClassName();
        println( "public class " + className + " extends " + dpSuperClassName + " {" );
        println();
        
        // Memeber variables
        String clientWrapperClassName = dataProviderInfo.getClientWrapperClassName();
        String clientWrapperClassVar = 
                ManagerUtil.makeValidJavaBeanName(ManagerUtil.decapitalize( clientWrapperClassName ));
        
        
        println( "    protected " + clientWrapperClassName + " " + clientWrapperClassVar + ";" );
        println( "    // Properties. One per method parameter." );

        for (DataProviderParameter parameter : dataProviderInfo.getMethod().getParameters()) {
            println( "    protected " + parameter.getType() + " " + parameter.getName() + ";" );
        }
        println();
        
        // Default Constructor
        println( "    public " + className + "() {" );
        println( "    }" );
        println();
        
        // Getter and setter for the client wrapper class
        println( "    public " + clientWrapperClassName + " get" + clientWrapperClassName + "() {" );
        println( "        return  this." + clientWrapperClassVar + ";" );
        println( "    }" );
        println();
        
        println( "    public void set" + clientWrapperClassName + "( " + clientWrapperClassName + " " + clientWrapperClassVar + " ) { ");
        println( "        this." + clientWrapperClassVar + " = " + clientWrapperClassVar + ";" );
        
        // Call super.setDataMethod() - need to the method name and parameter class types
        println( "        try { " );
        println( "            setDataProviderProperties(); ");
        
        
        // Call super.setCollectionElementType(Class) - needed to generate correct FieldKeys for List<T> return types (only for JAX-WS)
        if (isJ2EE_15) {
            println( "            Method dataMethod = super.getDataMethod(); " );
            println( "            Class returnClass = dataMethod.getReturnType();");
            println( "            if (java.util.Collection.class.isAssignableFrom(returnClass)) {" );
            println( "                Type returnType = dataMethod.getGenericReturnType();" );
            println( "                if (returnType instanceof ParameterizedType) { " );
            println( "                    ParameterizedType paramType = (ParameterizedType)returnType;" );
            println( "                    Type[] actualTypes = paramType.getActualTypeArguments();" );
            println( "                    if (actualTypes.length == 1 && actualTypes[0] instanceof Class) { " );
            println( "                        super.setCollectionElementType((Class)actualTypes[0]);" );
            println( "                    }" );
            println( "                }" );
            println( "            }");
        }
        
        
        println( "        } catch( java.lang.NoSuchMethodException ne ) { " );
        println( "            ne.printStackTrace();" );
        println( "        }");
        println( "    }" );
        println();
        
        // Methods for get/set of the properties/method parameters
        
        for (DataProviderParameter param : dataProviderInfo.getMethod().getParameters()) {
            // Getter
            println( "    public " + param.getType() + " get" + ManagerUtil.upperCaseFirstChar( param.getName() ) + "() {" );
            println( "        return " + param.getName() + ";");
            println( "    }" );
            println();
            
            // Setter
            println( "    public void set" + ManagerUtil.upperCaseFirstChar( param.getName() ) + "( " + param.getType() + " " + param.getName() + " ) { " );
            println( "        this." + param.getName() + " = " + param.getName() + ";" );
            println( "    }" );
            println();
        }
        println();
        
        // Implement abstract method from super class - getDataMethodArguments()
        if (!isPrimitiveReturnType && dataProviderInfo.getOutputHolderIndex() < 0 && dataProviderInfo.getWrappedProperty() == null) {
            println( "    public Object[] getDataMethodArguments() {" );
        }else {
            println( "    private Object[] getOriginalDataMethodArguments() {" );
        }

        int methodArgSize = dataProviderInfo.getMethod().getParameters().size();
        
        println("        try { ");
        println("            Object[] values = new Object[" + methodArgSize + "];");
        println();

        for (int i = 0; i < methodArgSize; i++) {
            DataProviderParameter field = dataProviderInfo.getMethod().getParameters().get(i);
            println("            values[" + i + "] = " + convertPrimitiveType(field.getName(), field.getType()) + ";");
        }

        println("            return values;");
        println("        } catch( Exception e ) { ");
        println("            e.printStackTrace();");
        println("            return null; ");
        println("        }");
        
        println();
        println( "    }" );
        println();
        
        // Override getFieldKeys() method to filter out the class field
        println( "    public FieldKey[] getFieldKeys() throws DataProviderException {" );
        println( "        FieldKey[] fieldKeys = super.getFieldKeys(); " );
        println( "        ArrayList finalKeys = new ArrayList(); " );
        println( "        for( int i = 0; i < fieldKeys.length; i ++ ) { " );
        println( "            if( !fieldKeys[i].getFieldId().equals( \"class\" ) )" );
        println( "                finalKeys.add( fieldKeys[i] ); " );
        println( "        } " );
        println( "        return (FieldKey[])finalKeys.toArray( new FieldKey[0] ); " );
        println( "    } " );
        
        int outputHolderIndex = dataProviderInfo.getOutputHolderIndex();
        
        if (dataProviderInfo.getWrappedProperty() != null) {
            println( "    private void setDataProviderProperties() throws NoSuchMethodException { " );
            println( "        super.setDataClassInstance( this );" );
            println( "        originalDataMethod = " + clientWrapperClassName + ".class.getMethod(" );
            println( "            \"" + dataProviderInfo.getMethod().getMethodName() + "\", new Class[] {" + getMethodParamTypes() + "} );" );
            println( "        super.setDataMethod( getWrapperMethod() ); ");
            println( "    }" );
            writeUnwrapMethod(clientWrapperClassVar, dataProviderInfo.getMethod().getMethodReturnType(),
                    dataProviderInfo.getWrappedProperty().getName(), dataProviderInfo.getWrappedProperty().getType());
        }else if (outputHolderIndex >= 0) {            
            List<JavaParameter> args = ((DataProviderModelMethod) dataProviderInfo.getMethod()).getJavaMethod().getParametersList();
            String holderValueType = args.get(outputHolderIndex).getType().getRealName();
            
            println( "    private void setDataProviderProperties() throws NoSuchMethodException { " );
            for (DataProviderParameter param : dataProviderInfo.getMethod().getParameters()) {
                if (param.getType().startsWith("javax.xml.ws.Holder")) {
                    String paramName = param.getName();
                    String paramType = param.getType();
                    println( "        " + paramName + " = new " + paramType + "();" );
                }
            }
            println( "        super.setDataClassInstance( this );" );
            println( "        originalDataMethod = " + clientWrapperClassName + ".class.getMethod(" );
            println( "            \"" + dataProviderInfo.getMethod().getMethodName() + "\", new Class[] {" + getMethodParamTypes() + "} );" );
            println( "        super.setDataMethod( getWrapperMethod() ); ");
            println( "    }" );
            
            writeOutputHolderMethodWrapper(clientWrapperClassVar, holderValueType);
        }else if (!isPrimitiveReturnType) {
            println( "    private void setDataProviderProperties() throws NoSuchMethodException { " );
            println( "        super.setDataClassInstance( " + clientWrapperClassVar + ");" );
            println( "        java.lang.reflect.Method dataMethod = " + clientWrapperClassName + ".class.getMethod(" );
            println( "            \"" + dataProviderInfo.getMethod().getMethodName() + "\", new Class[] {" + getMethodParamTypes() + "} );" );
            println( "        super.setDataMethod( dataMethod );" );
            println( "    }" );
        }else {
            println( "    private void setDataProviderProperties() throws NoSuchMethodException { " );
            println( "        super.setDataClassInstance( this );" );
            println( "        originalDataMethod = " + clientWrapperClassName + ".class.getMethod(" );
            println( "            \"" + dataProviderInfo.getMethod().getMethodName() + "\", new Class[] {" + getMethodParamTypes() + "} );" );
            println( "        super.setDataMethod( getWrapperMethod() ); ");
            println( "    }" );
            writePrimitiveMethodWrapper(clientWrapperClassVar);
        }
        
        // End of client bean class
        println( "}" );
    }
    
    private void writeOutputHolderMethodWrapper(String clientVar, String holderValueType) {
        int outputHolderIndex = dataProviderInfo.getOutputHolderIndex();
        String getter = "get" + ManagerUtil.upperCaseFirstChar(dataProviderInfo.getMethod().getParameters().get(outputHolderIndex).getName()) + "()";

        String exceptionReturnValue = "null";
        
        if (ManagerUtil.isJavaPrimitive(holderValueType)) {
            exceptionReturnValue = getDefaultPrimitiveRepresentation(holderValueType);
        }
        
        println( "" );
        println( "    private Method originalDataMethod; " );
        println( "" );
        println( "    public " + holderValueType + " invokeMethod() {" );
        println( "        try { ");
        println( "            originalDataMethod.invoke(" + clientVar + ", getOriginalDataMethodArguments()); ");
        println( "            " + holderValueType + " methodResult = this." + getter + ".value;");
        println( "            return methodResult; ");
        println( "        }catch (Exception ex) { ");
        println( "            ex.printStackTrace(); ");
        println( "            return " + exceptionReturnValue + "; ");
        println( "        }");
        println( "    } ");
        println( "" );
        println( "    private Method getWrapperMethod() throws NoSuchMethodException {");
        println( "        return this.getClass().getMethod(\"invokeMethod\", new Class[0]); ");
        println( "    } ");
        println( "" );
    }
    
    private void writeUnwrapMethod(String clientVar, String wrappedType, String propertyName, String propertyType) {
        java.lang.reflect.Method getMethod = Util.getPropertyGetter(wrappedType, propertyName, classLoader);
        String getter = getMethod.getName() + "()";
        String exceptionReturnValue = "null";
        
        if (Util.isJavaPrimitive(propertyType)) {
            exceptionReturnValue = getDefaultPrimitiveRepresentation(propertyType);
        }
        
        println( "" );
        println( "    private Method originalDataMethod; " );
        println( "" );
        println( "    public " + propertyType + " invokeMethod() {" );
        println( "        try { ");
        println( "            " + wrappedType + " result = (" + wrappedType + ") originalDataMethod.invoke(" + clientVar + ", getOriginalDataMethodArguments()); ");
        println( "            " + propertyType + " methodResult = result." + getter + ";");
        println( "            return methodResult; ");
        println( "        }catch (Exception ex) { ");
        println( "            ex.printStackTrace(); ");
        println( "            return " + exceptionReturnValue + "; ");
        println( "        }");
        println( "    } ");
        println( "" );
        println( "    private Method getWrapperMethod() throws NoSuchMethodException {");
        println( "        return this.getClass().getMethod(\"invokeMethod\", new Class[0]); ");
        println( "    } ");
        println( "" );        
    }
    
    private void writePrimitiveMethodWrapper(String clientVar) {
        String mrt = dataProviderInfo.getMethod().getMethodReturnType();
        if (Util.isJavaPrimitive(mrt)) {
            mrt = Util.getWrapperForPrimitive(mrt);
        }
        
        println( "" );
        println( "    private Method originalDataMethod; " );
        println( "" );
        println( "    public ResultBean invokeMethod() {" );
        println( "        try { ");
        println( "            " + mrt + " result = (" + mrt + ")originalDataMethod.invoke(" + clientVar + ", getOriginalDataMethodArguments()); ");
        println( "            ResultBean methodResult = new ResultBean(); " );
        println( "            methodResult.setMethodResult(result); " );
        println( "            return methodResult; ");
        println( "        }catch (Exception ex) { ");
        println( "            ex.printStackTrace(); ");
        println( "            return null; ");
        println( "        }");
        println( "    } ");
        println( "" );
        println( "    private Method getWrapperMethod() throws NoSuchMethodException {");
        println( "        return this.getClass().getMethod(\"invokeMethod\", new Class[0]); ");
        println( "    } ");
        println( "" );
        println( "    public static final class ResultBean { ");
        println( "        private " + mrt + " methodResult; ");
        println( "" );
        println( "        public ResultBean() { ");
        println( "        } " );
        println( "" );
        println( "        public " + mrt + " getMethodResult() { ");
        println( "            return this.methodResult; " );
        println( "        }" );
        println( "" );
        println( "        public void setMethodResult(" + mrt + " result) { " );
        println( "            this.methodResult = result; " );
        println( "        } " );
        println( "    } " );
    }
    
    private String convertPrimitiveType(String name, String type) {
        for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
            String typeName = PRIMITIVE_TYPES[i];
            if (type != null && typeName.equals(type.trim())) {
                return PRIMITIVE_WRAPPERS[i] + ".valueOf(this." + name + ")";
            }
        }

        return "this." + name;
    }
    
    private String getMethodParamTypes()
    {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        
        for (DataProviderParameter param : dataProviderInfo.getMethod().getParameters()) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            
            int len = separateGenericType(param.getType());
            String typeClass = param.getType().substring(0, len);
            
            buf.append(typeClass);
            buf.append(".class"); // NOI18N
        }
        return buf.toString();
    }
    
    // TODO - merge this with ReflectionHelper.separateGenericType
    private int separateGenericType(String typeName) {
        int length = typeName.length();
        
        if (length < 2 || typeName.charAt(length-1) != '>') { // NOI18N
            return length;
        }else {
            int depth = 1;
            for (int i = length - 2; i >= 0; i--) {
                if (typeName.charAt(i) == '>') {
                    depth += 1;
                }else if (typeName.charAt(i) == '<') {
                    depth -= 1;
                }
                
                if (depth == 0) {
                    return i;
                }
            }
            
            return length;
        }
    }

    private String getDefaultPrimitiveRepresentation(String typeName) {
        if (typeName.equals("boolean")) {
            return "false";
        } else {
            return "0";
        }
    }
    
}
