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
        ClassDescriptor beanClassDescriptor = generateBeanClass( srcDir, packageName );
        
        // Take care the case where there is no package
        String beanClassName = beanClassDescriptor.getPackageName() + "." + beanClassDescriptor.getClassName();
        if( beanClassDescriptor.getPackageName() == null || beanClassDescriptor.getPackageName().length() == 0 )
            beanClassName = beanClassDescriptor.getClassName();
        
        // Now, Generate the BeanInfo class source code
        DataProviderBeanInfoGenerator beanInfoGenerator = new DataProviderBeanInfoGenerator( beanClassName, methodInfo );
        ClassDescriptor beanInfoClassDescriptor = beanInfoGenerator.generateClass( srcDir );
        
        // Add generate the DesignInfo class souce code
        DataProviderDesignInfoGenerator designInfoGenerator = new DataProviderDesignInfoGenerator( clientWrapperClassName, beanClassName, methodInfo );
        ClassDescriptor[] designInfoClassDescriptors = designInfoGenerator.generateClass( srcDir );
        
        // Done! Return all the class descriptors
        
        ArrayList classDescriptors = new ArrayList();
        classDescriptors.add( beanClassDescriptor );
        classDescriptors.add( beanInfoClassDescriptor );
        
        for( int i = 0; i < designInfoClassDescriptors.length; i ++ )
            classDescriptors.add( designInfoClassDescriptors[i] );
        
        return classDescriptors;
    }
    
    public ClassDescriptor generateBeanClass( String srcDir, String packageName ) throws EjbLoadException {
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
            
            // Import
            out.println( "import com.sun.data.provider.*;" );
            out.println( "import com.sun.data.provider.impl.*;" );
            out.println( "import java.lang.reflect.Method;" );
            out.println( "import java.beans.*;" );
            out.println( "import java.util.ArrayList;" );
            out.println();
            
            // start of class
            String dpSuperClassName = "MethodResultTableDataProvider";
            //String dpSuperClassName = "MethodResultDataProvider";
            
            out.println( "public class " + className + " extends " + dpSuperClassName + " {" );
            out.println();
            
            // Memeber variables
            String clientWrapperClassVar = Util.decapitalize( clientWrapperClassName );
            out.println( "    protected " + clientWrapperClassName + " " + clientWrapperClassVar + ";" );
            out.println( "    protected ArrayList methodArgumentNames = new ArrayList();" );
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
            for( int i = 0; i < methodParams.size(); i ++ ) {
                MethodParam p = (MethodParam)methodParams.get( i );
                out.println( "        methodArgumentNames.add( \"" + p.getName() + "\" );" );
            }
            out.println( "    }" );
            out.println();
            
            // Getter and setter for the client wrapper class
            out.println( "    public " + clientWrapperClassName + " get" + clientWrapperClassName + "() {" );
            out.println( "        return  this." + clientWrapperClassVar + ";" );
            out.println( "    }" );
            out.println();
            
            out.println( "    public void set" + clientWrapperClassName + "( " + clientWrapperClassName + " " + clientWrapperClassVar + " ) { ");
            out.println( "        this." + clientWrapperClassVar + " = " + clientWrapperClassVar + ";" );
            
            // Set the object instance the method is invoked from
            out.println( "        super.setDataClassInstance( " + clientWrapperClassVar + ");" );
            
            // Set the Collection element type is the return type is Collection
            if( methodInfo.getReturnType().isCollection() ) {
                String elemClassName = methodInfo.getReturnType().getElemClassName();
                
                out.println( "        try {" );
                out.println( "            Class elemType = Class.forName( \"" + elemClassName + "\");" );
                out.println( "            super.setCollectionElementType( elemType );" );
                out.println( "        } catch( java.lang.ClassNotFoundException ne ) {" );
                out.println( "            ne.printStackTrace();" );
                out.println( "        }" );
                out.println();
            }
            
            // Set the method where the data is retrieved
            out.println( "        try { " );
            out.println( "            super.setDataMethod( " + clientWrapperClassName + ".class.getMethod(" );
            out.println( "                \"" + methodInfo.getName() + "\", new Class[] {" + getMethodParamTypes() + "} ) );" );
            out.println( "        } catch( java.lang.NoSuchMethodException ne ) { " );
            out.println( "            ne.printStackTrace();" );
            out.println( "        }");
            out.println( "    }" );
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
            out.println( "    public Object[] getDataMethodArguments() {" );
            if( methodInfo.getParameters() == null || methodInfo.getParameters().isEmpty() )
                out.println( "        return new Object[0];" );
            else {
                out.println( "        try { " );
                out.println( "            Object[] values = new Object[methodArgumentNames.size()];" );
                out.println();
                out.println( "            // Using the BeanInfo to get the property values" );
                out.println( "            BeanInfo beanInfo = Introspector.getBeanInfo( this.getClass() );" );
                out.println( "            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();" );
                out.println();
                out.println( "            for( int i = 0; i < propertyDescriptors.length; i ++ ) {" );
                out.println();
                out.println( "                String propName = propertyDescriptors[i].getName();" );
                out.println();
                out.println( "                int argPos = findArgumentPosition( new String(propName) );" );
                out.println( "                if( argPos != -1 ) {" );
                out.println( "                    Method reader = propertyDescriptors[i].getReadMethod();" );
                out.println( "                    if (reader != null) " );
                out.println( "                        values[argPos] = reader.invoke(this, new Object[0]);" );
                out.println( "                }" );
                out.println( "            }" );
                out.println();
                out.println( "            return values;" );
                out.println( "        } catch( Exception e ) { " );
                out.println( "            e.printStackTrace();" );
                out.println( "            return null; " );
                out.println( "        }" );
            }
            
            out.println();
            out.println( "    }" );
            out.println();
            
            out.println( "    private int findArgumentPosition( String propName ) {" );
            out.println( "        // First try the propName itself" );
            out.println( "        int index = methodArgumentNames.indexOf( propName );" );
            out.println();
            out.println( "        char chars[] = propName.toCharArray();" );
            out.println();
            out.println( "        if( index == -1 ) {" );
            out.println( "            // fFlip the capitalization of the first char and try it again" );
            out.println( "            if( Character.isUpperCase( chars[0] ) )" );
            out.println( "                chars[0] = Character.toLowerCase(chars[0]);" );
            out.println( "            else" );
            out.println( "                chars[0] = Character.toUpperCase(chars[0]);" );
            out.println();
            out.println( "            index = methodArgumentNames.indexOf( new String(chars) ); " );
            out.println( "        }" );
            out.println();
            out.println( "        return index; " );
            out.println( "    }" );
            out.println();
            
            // Override getFieldKeys() method to filter out the class field
            out.println( "    public FieldKey[] getFieldKeys() throws DataProviderException {" );
            out.println( "        FieldKey[] fieldKeys = super.getFieldKeys(); " );
            out.println( "        ArrayList finalKeys = new ArrayList(); " );
            out.println( "        for( int i = 0; i < fieldKeys.length; i ++ ) { " );
            out.println( "            if( !fieldKeys[i].getFieldId().equals( \"class\" ) )" );
            out.println( "                finalKeys.add( fieldKeys[i] ); " );
            out.println( "        } " );
            out.println( "        return (FieldKey[])finalKeys.toArray( new FieldKey[0] ); " );
            out.println( "    } " );
            
            // End of client bean clas
            out.println( "}" );
            
            out.flush();
            out.close();
            
            return classDescriptor;
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
                
                // TODO need to handle primitive type
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
