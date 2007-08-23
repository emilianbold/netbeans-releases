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

import org.netbeans.modules.visualweb.websvcmgr.util.Util;


import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Creates the DataProvider Java source code for the given method
 * @author  cao
 */
public class DataProviderWriter extends java.io.PrintWriter {
    
    private DataProviderInfo dataProviderInfo;
    private Set imports = new HashSet();
    private boolean isJ2EE_15;
    
    public DataProviderWriter(Writer writer, DataProviderInfo dataProviderInfo, boolean isJ2EE_15 ){
        super(writer);
        this.dataProviderInfo = dataProviderInfo;
        this.isJ2EE_15 = isJ2EE_15;
    }
    
    public void addImport(String importLine){
        imports.add(importLine);
    }
    
    public void writeClass(){
        boolean isPrimitiveReturnType = isPrimitiveType(dataProviderInfo.getMethod().getMethodReturnType());
        
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
        String clientWrapperClassVar = Util.decapitalize( clientWrapperClassName );
        println( "    protected " + clientWrapperClassName + " " + clientWrapperClassVar + ";" );
        println( "    protected ArrayList methodArgumentNames = new ArrayList();" );
        println( "    // Properties. One per method parameter." );

        for (DataProviderParameter parameter : dataProviderInfo.getMethod().getParameters()) {
            println( "    protected " + parameter.getType() + " " + parameter.getName() + ";" );
        }
        println();
        
        // Default Constructor
        println( "    public " + className + "() {" );
        // Collect the method parameter names
        for (DataProviderParameter parameter : dataProviderInfo.getMethod().getParameters()) {
            println( "        methodArgumentNames.add( \"" + parameter.getName() + "\" );" );
        }
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
            println( "    public " + param.getType() + " get" + Util.upperCaseFirstChar( param.getName() ) + "() {" );
            println( "        return " + param.getName() + ";");
            println( "    }" );
            println();
            
            // Setter
            println( "    public void set" + Util.upperCaseFirstChar( param.getName() ) + "( " + param.getType() + " " + param.getName() + " ) { " );
            println( "        this." + param.getName() + " = " + param.getName() + ";" );
            println( "    }" );
            println();
        }
        println();
        
        // Implement abstract method from super class - getDataMethodArguments()
        if (!isPrimitiveReturnType) {
            println( "    public Object[] getDataMethodArguments() {" );
        }else {
            println( "    private Object[] getOriginalDataMethodArguments() {" );
        }
        
        if( dataProviderInfo.getMethod().getParameters().isEmpty() )
            println( "        return new Object[0];" );
        else {
            println( "        try { " );
            println( "            Object[] values = new Object[methodArgumentNames.size()];" );
            println();
            println( "            // Using the BeanInfo to get the property values" );
            println( "            BeanInfo beanInfo = Introspector.getBeanInfo( this.getClass() );" );
            println( "            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();" );
            println();
            println( "            for( int i = 0; i < propertyDescriptors.length; i ++ ) {" );
            println();
            println( "                String propName = propertyDescriptors[i].getName();" );
            println();
            println( "                int argPos = findArgumentPosition( new String(propName) );" );
            println( "                if( argPos != -1 ) {" );
            println( "                    Method reader = propertyDescriptors[i].getReadMethod();" );
            println( "                    if (reader != null) " );
            println( "                        values[argPos] = reader.invoke(this, new Object[0]);" );
            println( "                }" );
            println( "            }" );
            println();
            println( "            return values;" );
            println( "        } catch( Exception e ) { " );
            println( "            e.printStackTrace();" );
            println( "            return null; " );
            println( "        }" );
        }
        
        println();
        println( "    }" );
        println();
        
        println( "    private int findArgumentPosition( String propName ) {" );
        println( "        // First try the propName itself" );
        println( "        int index = methodArgumentNames.indexOf( propName );" );
        println();    
        println( "        char chars[] = propName.toCharArray();" );
        println();  
        println( "        if( index == -1 ) {" );
        println( "            // fFlip the capitalization of the first char and try it again" );
        println( "            if( Character.isUpperCase( chars[0] ) )" );
        println( "                chars[0] = Character.toLowerCase(chars[0]);" );  
        println( "            else" );  
        println( "                chars[0] = Character.toUpperCase(chars[0]);" );  
        println();
        println( "            index = methodArgumentNames.indexOf( new String(chars) ); " );
        println( "        }" );
        println();    
        println( "        return index; " );  
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
        
        if (!isPrimitiveReturnType) {
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
    
    private void writePrimitiveMethodWrapper(String clientVar) {
        String mrt = dataProviderInfo.getMethod().getMethodReturnType();
        if (isJavaPrimitive(mrt)) {
            mrt = getWrapperForPrimitive(mrt);
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
    
    private String getMethodParamTypes()
    {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        
        for (DataProviderParameter param : dataProviderInfo.getMethod().getParameters()) {
            if( first )
                    first = false;
                else
                    buf.append( ", " );

                // TODO need to handle primitive type
                buf.append( param.getType() );
                buf.append( ".class" ); // NOI18N
            
        }
        return buf.toString();
    }
    
    private static final String[] PRIMITIVE_WRAPPER_CLASSES = 
    { "java.lang.Boolean", 
      "java.lang.Byte", 
      "java.lang.Double", 
      "java.lang.Float", 
      "java.lang.Integer", 
      "java.lang.Long", 
      "java.lang.Short", 
      "java.lang.Character", 
      "java.lang.String" };
    
    private static final String[] PRIMITIVE_TYPES = 
    { "boolean",
      "byte",
      "double",
      "float",
      "int",
      "long",
      "short",
      "char" };
    
    private boolean isPrimitiveType(String typeName) {
        for (int i = 0; i < PRIMITIVE_WRAPPER_CLASSES.length; i++) {
            if (PRIMITIVE_WRAPPER_CLASSES[i].equals(typeName)) {
                return true;
            }
        }
        
        return isJavaPrimitive(typeName);
    }
    
    private boolean isJavaPrimitive(String typeName) {
        for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
            if (PRIMITIVE_TYPES[i].equals(typeName)) {
                return true;
            }
        }
        
        return false;        
    }
    
    private String getWrapperForPrimitive(String javaPrimitive) {
        for (int i = 0; i < PRIMITIVE_TYPES.length; i++) {
            if (PRIMITIVE_TYPES[i].equals(javaPrimitive)) {
                return PRIMITIVE_WRAPPER_CLASSES[i];
            }     
        }
        
        return null;
    }
    
}
