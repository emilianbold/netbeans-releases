/*
 * ClientBeanWrapperCompiler.java
 *
 * Created on May 13, 2004, 1:52 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.util.RunCommand;
import java.io.File;
import java.util.*;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;

/**
 * To compile the wrapper client bean and bean info classes for the EJB session beans.
 * Using com.sun.tools.java.Main for now. Probably should use org.openide.compiler. Look
 * into later ...
 *
 * @author  cao
 */
public class ClientBeanWrapperCompiler {
    private ArrayList cmdArray;
    
    public ClientBeanWrapperCompiler() {
    }
    
    /**
     * Compile the given java classes
     *
     * @param srcDir The director where the src code exists
     * @param classDescriptors A list of ClassDescriptors, one per java class
     * @param jarFiles A list jars which should be in the classpath
     */
    public void compile( String srcDir, ArrayList classDescriptors, ArrayList jarFiles ) throws EjbLoadException {
        // The command array contains
        // javac
        // -source
        // value of the source version, which is hardcoded to 1.4 for now
        // -target
        // value of the target JVM version, which is 1.4
        // -classpath
        // value of the classpath
        // src1
        // src2
        // ....
        
        cmdArray = new ArrayList();
        
        cmdArray.add( System.getProperty( "jdk.home" ) + "/bin/javac" );
        cmdArray.add( "-source" );
        cmdArray.add( "1.4" );
        cmdArray.add( "-target" );
        cmdArray.add( "1.4" );
        cmdArray.add( "-classpath" );
        
        // Now what should be in the classpath?
        // The source dirs and all the passed in jar files
        
        String classpath = srcDir;
        for( Iterator iter = jarFiles.iterator(); iter.hasNext(); ) {
            String jarFile = (String)iter.next();
            classpath += File.pathSeparator + jarFile;
        }
        
        cmdArray.add( classpath );
        
        // Now the java source files
        int i = 3;
        for( Iterator iter = classDescriptors.iterator(); iter.hasNext(); ) {
            ClassDescriptor srcClass = (ClassDescriptor)iter.next();
            
            if( !srcClass.isInnerClass() )
                cmdArray.add( srcClass.getFullPathFileName() );
        }
        
        try {
            RunCommand runCommand = new RunCommand();
            runCommand.execute( (String[])cmdArray.toArray( new String[0] ) );
            if(runCommand.getReturnStatus() != 0){
                // Something went wrong. Log the attempted command here
                ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanWrapperCompiler" ).log( ErrorManager.ERROR, "Failed to compile warpper bean classes" );
                
                // throw up as SYSTEM_ERROR
                throw new EjbLoadException( "Failed to compile wrapper bean classes" ); // NOI18N
            }
            
            // Need to create ClassDescriptors for the compiled .class's. One per src file
            ArrayList compiledClasses = new ArrayList();
            for( Iterator iter = classDescriptors.iterator(); iter.hasNext(); ) {
                ClassDescriptor srcClass = (ClassDescriptor)iter.next();
                
                // We need to check the full path file name. If it is null, that means
                // the class is a inner class. In that case, we don't need to add a class
                // descriptor for the .class. The original one is enough
                
                if( !srcClass.isInnerClass() ) {
                    // Replace ".java" with ".class" for the compiled class files
                    int lastIndexJavaFullPath = srcClass.getFullPathFileName().lastIndexOf( '.' );
                    int lastIndexJavaPkg = srcClass.getPackageFileName().lastIndexOf( '.' );
                    String classFullPath = srcClass.getFullPathFileName().substring( 0,  lastIndexJavaFullPath ) + ".class";// NOI18N
                    String classPkgFileName = srcClass.getPackageFileName().substring( 0, lastIndexJavaPkg ) + ".class";// NOI18N
                    
                    compiledClasses.add( new ClassDescriptor(
                            srcClass.getClassName(),
                            srcClass.getPackageName(),
                            classFullPath,
                            classPkgFileName ) );
                }
                
            }
            
            classDescriptors.addAll( compiledClasses );
        } catch( Exception ex ) {
            // Log error
            String logMsg = "Error occurred when trying to execute command: " + getCommand();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanWrapperCompiler" ).log( ErrorManager.ERROR,  logMsg );
            ex.printStackTrace();
            
            // throw up as SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }
    
    private String getCommand() {
        StringBuffer cmd = new StringBuffer();
        for( int i = 0; i < cmdArray.size(); i ++ ) {
            cmd.append( (String)cmdArray.get(i) );
            cmd.append( " " );
        }
        
        return cmd.toString();
    }
}
