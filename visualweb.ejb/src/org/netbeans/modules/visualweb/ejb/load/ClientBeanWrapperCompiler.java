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
