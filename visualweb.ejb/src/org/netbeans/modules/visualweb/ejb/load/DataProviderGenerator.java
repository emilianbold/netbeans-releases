/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
/*
 * DataProviderGenerator.java
 *
 * Created on February 14, 2005, 3:19 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * This class is to help generate a data provider class for a given method.
 * The generated data provide implements IndexedDataProvider if the given method
 * returns a Collection or Array. Otherwise, a base DataProvider interface is implemented
 *
 * @author  dongmei cao
 */
public class DataProviderGenerator {
    private static String[] PRIMITIVE_TYPES = {
        "char", "byte", "short", "int", "long", "float", "double", "boolean"
    };
    
    private static String[] PRIMITIVE_WRAPPERS = {
        "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean"
    };
    
    private String packageName;
    private String clientWrapperClassName;
    private MethodInfo methodInfo;
    private URLClassLoader classLoader;
    
    public DataProviderGenerator() {
    }
    
    public DataProviderGenerator( String clientWrapperFullClassName, MethodInfo methodInfo, URLClassLoader classLoader ) {
        
        // Get the package name
        int lastDot = clientWrapperFullClassName.lastIndexOf( '.' );
        packageName = clientWrapperFullClassName.substring( 0, lastDot );
        
        // The class name
        this.clientWrapperClassName = Util.getClassName(clientWrapperFullClassName);
        
        this.methodInfo = methodInfo;
        this.classLoader = classLoader;
    }
    
    public Collection generateClasses( String srcDir ) throws EjbLoadException {
        
        // First, generate the bean class source code
        ClassDescriptor[] beanClassDescriptor = generateBeanClass( srcDir, packageName );
        
        // Take care the case where there is no package
        String beanClassName = beanClassDescriptor[0].getPackageName() + "." + beanClassDescriptor[0].getClassName();
        if( beanClassDescriptor[0].getPackageName() == null || beanClassDescriptor[0].getPackageName().length() == 0 )
            beanClassName = beanClassDescriptor[0].getClassName();
        
        // Now, Generate the BeanInfo class source code
        DataProviderBeanInfoGenerator beanInfoGenerator = new DataProviderBeanInfoGenerator( beanClassName, methodInfo, clientWrapperClassName );
        ClassDescriptor beanInfoClassDescriptor = beanInfoGenerator.generateClass( srcDir );
        
        // Add generate the DesignInfo class souce code
        DataProviderDesignInfoGenerator designInfoGenerator = new DataProviderDesignInfoGenerator( clientWrapperClassName, beanClassName, methodInfo );
        ClassDescriptor[] designInfoClassDescriptors = designInfoGenerator.generateClass( srcDir );
        
        // Done! Return all the class descriptors
        
        ArrayList classDescriptors = new ArrayList();
        for (int i = 0; i < beanClassDescriptor.length; i++) {
            classDescriptors.add( beanClassDescriptor[i] );
        }
        
        classDescriptors.add( beanInfoClassDescriptor );
        
        for( int i = 0; i < designInfoClassDescriptors.length; i ++ )
            classDescriptors.add( designInfoClassDescriptors[i] );
        
        return classDescriptors;
    }
    
    public ClassDescriptor[] generateBeanClass( String srcDir, String packageName ) throws EjbLoadException {
        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;
        
        try {
            // Update the data provider with full package data provider class name
            String className = methodInfo.getDataProvider();
            methodInfo.setDataProvider( packageName + "." + className );
            
            String classDir = packageName.replace( '.', File.separatorChar );
            File dirF = new File( srcDir + File.separator + classDir );
            if( !dirF.exists() ) {
                if( !dirF.mkdirs() )
                    System.out.println( ".....failed to make dir" + srcDir + File.separator + classDir );
            }
            
            String classFile =  className + ".java";  // NOI18N
            javaFile = new File( dirF, classFile );
            javaFile.createNewFile();
            
            ClassDescriptor classDescriptor = new ClassDescriptor(
                    className,  // class name
                    packageName,  // package name
                    javaFile.getAbsolutePath(),  // full path java file name
                    classDir + File.separator + classFile ); // file name with package in path
            
            ClassDescriptor helperDescriptor = new ClassDescriptor(
                    className + "$ResultBean",
                    packageName,
                    new File( dirF, className + "$ResultBean.class" ).getAbsolutePath(),
                    classDir + File.separator + className + "$ResultBean.class",
                    true);
            
            String qualifiedClassName = packageName + "." + className;
            String qualifiedClientWrapperName = packageName + "." + clientWrapperClassName;
            
            // Generate java code
            
            PrintWriter out = new PrintWriter( new FileOutputStream(javaFile) );
            
            // package
            if( packageName != null && packageName.length() != 0 ) {
                out.println( "package " + packageName + ";" );
                out.println();
            }
            
            // comments
            out.println( "/**" );
            out.println( " * Source code created on " + new Date() );
            out.println( " */" );
            out.println();
            
            // start of class
            String dpSuperClassName = "com.sun.data.provider.impl.MethodResultTableDataProvider";
            //String dpSuperClassName = "MethodResultDataProvider";
            
            out.println( "public class " + className + " extends " + dpSuperClassName + " {" );
            out.println();
            
            // Memeber variables
            String clientWrapperClassVar = Util.decapitalize( clientWrapperClassName );
            out.println( "    protected " + qualifiedClientWrapperName + " " + clientWrapperClassVar + ";" );
            out.println( "    // Properties. One per method parameter." );
            ArrayList methodParams = methodInfo.getParameters();
            for( int i = 0; i < methodParams.size(); i ++ ) {
                MethodParam p = (MethodParam)methodParams.get( i );
                out.println( "    protected " + p.getType() + " " + p.getName() + ";" );
            }
            out.println();
            
            out.println( "    // The EJB wrapper object" );
            
            // Default Constructor
            out.println( "    public " + className + "() {" );
            out.println( "    }" );
            out.println();
            
            // Getter and setter for the client wrapper class
            out.println( "    public " + qualifiedClientWrapperName + " get" + clientWrapperClassName + "() {" );
            out.println( "        return  this." + clientWrapperClassVar + ";" );
            out.println( "    }" );
            out.println();
            
            out.println( "    public void set" + clientWrapperClassName + "( " + clientWrapperClassName + " " + clientWrapperClassVar + " ) { ");
            out.println( "        this." + clientWrapperClassVar + " = " + clientWrapperClassVar + ";" );
            
            boolean usePrimitiveWrapper = false;
            
            if (methodInfo.getReturnType().isPrimitive() && !methodInfo.getReturnType().isCollection()) {
                usePrimitiveWrapper = true;
                out.println("        super.setDataClassInstance( this );");
                out.println("        try { ");
                out.println("            originalDataMethod = " + qualifiedClientWrapperName + ".class.getMethod(" );
                out.println("                    \"" + methodInfo.getName() + "\", new Class[] {" + getMethodParamTypes() + "} );");
                out.println("            super.setDataMethod( getWrapperMethod() ); ");
                out.println("        }catch (java.lang.NoSuchMethodException ne) {");
                out.println("            ne.printStackTrace();");
                out.println("        }");
            }else {
                // Set the object instance the method is invoked from
                out.println("        super.setDataClassInstance( " + clientWrapperClassVar + ");");

                // Set the Collection element type is the return type is Collection
                if (methodInfo.getReturnType().isCollection()) {
                    String elemClassName = methodInfo.getReturnType().getElemClassName();

                    out.println("        try {");
                    out.println("            Class elemType = Class.forName( \"" + elemClassName + "\");");
                    out.println("            super.setCollectionElementType( elemType );");
                    out.println("        } catch( java.lang.ClassNotFoundException ne ) {");
                    out.println("            ne.printStackTrace();");
                    out.println("        }");
                    out.println();
                }

                // Set the method where the data is retrieved
                out.println("        try { ");
                out.println("            super.setDataMethod( " + qualifiedClientWrapperName + ".class.getMethod(");
                out.println("                \"" + methodInfo.getName() + "\", new Class[] {" + getMethodParamTypes() + "} ) );");
                out.println("        } catch( java.lang.NoSuchMethodException ne ) { ");
                out.println("            ne.printStackTrace();");
                out.println("        }");
            }
            out.println(    "    }");
            out.println();

            // Methods for get/set of the properties/method parameters
            for( int i = 0; i < methodParams.size(); i ++ ) {
                MethodParam p = (MethodParam)methodParams.get( i );
                
                // Getter
                out.println( "    public " + p.getType() + " get" + Util.capitalize( p.getName() ) + "() {" );
                out.println( "        return " + p.getName() + ";");
                out.println( "    }" );
                out.println();
                
                // Setter
                out.println( "    public void set" + Util.capitalize( p.getName() ) + "( " + p.getType() + " " + p.getName() + " ) { " );
                out.println( "        this." + p.getName() + " = " + p.getName() + ";" );
                out.println( "    }" );
                out.println();
            }
            out.println();
            
            // Implement abstract method from super class - getDataMethodArguments()
            if (usePrimitiveWrapper) {
                out.println( "    private java.lang.Object[] getOriginalDataMethodArguments() {");
            }else {
                out.println( "    public java.lang.Object[] getDataMethodArguments() {" );
            }
            
            int methodArgSize = (methodInfo.getParameters() == null) ? 0 : methodInfo.getParameters().size();

            out.println("        try { ");
            out.println("            java.lang.Object[] values = new java.lang.Object[" + methodArgSize + "];");
            out.println();

            for (int i = 0; i < methodArgSize; i++) {
                
                
                MethodParam parameter = (MethodParam) methodInfo.getParameters().get(i);
                out.println("            values[" + i + "] = " + convertPrimitiveType(parameter.getName(), parameter.getType()) + ";");
            }

            out.println("            return values;");
            out.println("        } catch( java.lang.Exception e ) { ");
            out.println("            e.printStackTrace();");
            out.println("            return null; ");
            out.println("        }");
            
            out.println();
            out.println( "    }" );
            out.println();
            
            // Override getFieldKeys() method to filter out the class field
            out.println( "    public com.sun.data.provider.FieldKey[] getFieldKeys() throws com.sun.data.provider.DataProviderException {" );
            out.println( "        com.sun.data.provider.FieldKey[] fieldKeys = super.getFieldKeys(); " );
            out.println( "        java.util.ArrayList finalKeys = new java.util.ArrayList(); " );
            out.println( "        for( int i = 0; i < fieldKeys.length; i ++ ) { " );
            out.println( "            if( !fieldKeys[i].getFieldId().equals( \"class\" ) )" );
            out.println( "                finalKeys.add( fieldKeys[i] ); " );
            out.println( "        } " );
            out.println( "        return (com.sun.data.provider.FieldKey[])finalKeys.toArray( new com.sun.data.provider.FieldKey[0] ); " );
            out.println( "    } " );

            
            // helper methods for primitive return types
            if (usePrimitiveWrapper) {
                String mrt = methodInfo.getReturnType().getClassName();
                String resultBeanName = qualifiedClassName + ".ResultBean";
                
                out.println("");
                out.println("    private java.lang.reflect.Method originalDataMethod; ");
                out.println("");
                out.println("    public " + resultBeanName + " invokeMethod() {");
                out.println("        try { ");
                out.println("            " + mrt + " result = (" + mrt + ")originalDataMethod.invoke(" + clientWrapperClassVar + ", getOriginalDataMethodArguments()); ");
                out.println("            " + resultBeanName + " methodResult = new " + resultBeanName + "(); ");
                out.println("            methodResult.setMethodResult(result); ");
                out.println("            return methodResult; ");
                out.println("        }catch (java.lang.Exception ex) { ");
                out.println("            ex.printStackTrace(); ");
                out.println("            return null; ");
                out.println("        }");
                out.println("    } ");
                out.println("");
                out.println("    private java.lang.reflect.Method getWrapperMethod() throws java.lang.NoSuchMethodException {");
                out.println("        return this.getClass().getMethod(\"invokeMethod\", new java.lang.Class[0]); ");
                out.println("    } ");
                out.println("");
                out.println("    public static final class ResultBean { ");
                out.println("        private " + mrt + " methodResult; ");
                out.println("");
                out.println("        public ResultBean() { ");
                out.println("        } ");
                out.println("");
                out.println("        public " + mrt + " getMethodResult() { ");
                out.println("            return this.methodResult; ");
                out.println("        }");
                out.println("");
                out.println("        public void setMethodResult(" + mrt + " result) { ");
                out.println("            this.methodResult = result; ");
                out.println("        } ");
                out.println("    } ");
            }
            
            
            // End of client bean clas
            out.println( "}" );
            
            out.flush();
            out.close();
            
            if (usePrimitiveWrapper) {
                return new ClassDescriptor[] { classDescriptor, helperDescriptor };
            }else {
                return new ClassDescriptor[] { classDescriptor };
            }
        } catch( java.io.FileNotFoundException ex ) {
            // Log error
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        } catch( java.io.IOException ex ) {
            // Log error
            
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
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
    
    private String getMethodParamTypes() {
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        if( methodInfo.getParameters() != null ) {
            for( Iterator iter = methodInfo.getParameters().iterator(); iter.hasNext(); ) {
                MethodParam p = (MethodParam)iter.next();
                
                if( first )
                    first = false;
                else
                    buf.append( ", " );
                
                buf.append( p.getType() );
                buf.append( ".class" ); // NOI18N
            }
        }
        
        return buf.toString();
    }
    
    
    private void addMethodComment( PrintWriter out, String comment ) {
        out.println( "    /**" );
        out.println( "     *" + comment );
        out.println( "     */" );
    }
    
    private boolean isIndexedDataProvider() {
        if( methodInfo.getReturnType().isCollection())
            return true;
        else
            if( methodInfo.getReturnType().getClassName().indexOf( "[]" ) != -1 )
                return true;
            else
                return false;
    }
}
