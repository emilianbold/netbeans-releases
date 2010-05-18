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
 * ClientBeanInfoGenerator.java
 *
 * Created on May 12, 2004, 8:37 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import org.openide.ErrorManager;

/**
 * Generates the BeanInfo class for the wrapper Bean
 *
 * @author  cao
 */
public class ClientBeanInfoGenerator {
    // Hardcode the iconFileName here
    public static final String EJB_ICON_FILE_NAME = "/org/netbeans/modules/visualweb/ejb/resources/session_bean.png";
    
    /**
     * The bean class which this BeanInfo is for
     */
    private String fullBeanClassName;
    
    private ArrayList properties;
    
    private String ejbName;
    
    public ClientBeanInfoGenerator( String ejbName, String fullBeanClassName, ArrayList properties ) {
        this.fullBeanClassName = fullBeanClassName;
        this.properties = properties;
        this.ejbName = ejbName;
    }
    
    /**
     * Generates the BeanInfo class
     *
     * @param srcDir Where the java source code will be saved
     * @return the ClassDescriptor for the BeanInfo class just generated
     */
    public ClassDescriptor generateClass( String srcDir ) throws EjbLoadException {
        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;
        
        try {
            // Figure out the package name, class name and directory/file name
            
            int i = fullBeanClassName.lastIndexOf( "." );
            String beanClassName = fullBeanClassName.substring( i+1 );
            
            // Package name
            if( i == -1 ) // No package
                i = 0;
            String packageName = fullBeanClassName.substring( 0, i );
            
            String beanInfoClassName = beanClassName + "BeanInfo";
            
            String classDir = packageName.replace( '.', File.separatorChar );
            File dirF = new File( srcDir + File.separator + classDir );
            if( !dirF.exists() ) {
                if( !dirF.mkdirs() )
                    System.out.println( ".....failed to make dir" + srcDir + File.separator + classDir );
            }
            
            String beanInfoClassFile =  beanInfoClassName + ".java";
            javaFile = new File( dirF, beanInfoClassFile );
            javaFile.createNewFile();
            
            ClassDescriptor classDescriptor = new ClassDescriptor(
                    beanInfoClassName,
                    packageName,
                    javaFile.getAbsolutePath(),
                    classDir + File.separator + beanInfoClassFile );
            
            // Generate java code
            
            PrintWriter out = new PrintWriter( new FileOutputStream(javaFile) );
            
            // pacage
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
            out.println( "import java.awt.Image;" );
            out.println( "import java.beans.BeanDescriptor;" );
            out.println( "import java.beans.PropertyDescriptor;" );
            out.println( "import java.beans.SimpleBeanInfo;" );
            out.println();
            
            // Start class
            out.println( "public class " + beanInfoClassName + " extends SimpleBeanInfo {" );
            out.println();
            
            // Private variables
            String beanClassVariable = "beanClass";
            String iconFileNameVariable = "iconFileName";
            String beanDescriptorVariable = "beanDescriptor";
            String propDescriptorsVariable = "propDescriptors";
            out.println( "    private Class " + beanClassVariable + " = " + fullBeanClassName + ".class;" );
            out.println( "    private String " + iconFileNameVariable + " = \"" + EJB_ICON_FILE_NAME + "\";" );
            out.println( "    private BeanDescriptor " + beanDescriptorVariable + " = null;" );
            out.println( "    private PropertyDescriptor[] " + propDescriptorsVariable + " = null; " );
            out.println();
            
            // Method - getBeanDescriptor()
            out.println( "    public BeanDescriptor getBeanDescriptor() {" );
            out.println( "        if( " + beanDescriptorVariable + " == null ) {" );
            out.println( "           " + beanDescriptorVariable + " = new BeanDescriptor( " + beanClassVariable + " );" );
            out.println( "           " + beanDescriptorVariable + ".setValue( \"trayComponent\", Boolean.TRUE );" );
            out.println( "        }" );
            out.println( "        return " + beanDescriptorVariable + ";" );
            out.println( "    }" );
            out.println();
            
            // Method - getPropertyDescriptors()
            out.println( "    public PropertyDescriptor[] getPropertyDescriptors() {" );
            if( properties == null || properties.isEmpty() ) {
                // No try-catch
                out.println( "            if( " +  propDescriptorsVariable + " == null ) { " );
                out.println( "                    " + propDescriptorsVariable + " = new PropertyDescriptor[] {" );
                out.println( "                }; " );
                out.println( "            }" );
                out.println( "            return " + propDescriptorsVariable + ";" );
            } else {
                // Need try-catch block
                out.println( "        try { " );
                out.println( "            if( " +  propDescriptorsVariable + " == null ) { " );
                out.println( "                    " + propDescriptorsVariable + " = new PropertyDescriptor[] {" );
                
                // Virtual JavaBean properties
                for( Iterator iter = properties.iterator(); iter.hasNext(); ) {
                    String prop = (String)iter.next();
                    out.println( "                     new PropertyDescriptor( \"" + prop + "\", " + beanClassVariable + "), " );
                }
                
                out.println( "                }; " );
                out.println( "            }" );
                out.println( "            return " + propDescriptorsVariable + ";" );
                out.println( "        } catch( java.beans.IntrospectionException e ) {" );
                out.println( "            return " + propDescriptorsVariable + ";" );
                out.println( "        }" );
            }
            out.println( "    }" );
            out.println();
            
            
            // Method - getIcon()
            out.println( "    public Image getIcon(int iconKind) {" );
            out.println( "        return loadImage( " + iconFileNameVariable + " );" );
            out.println( "    }" );
            out.println();
            
            // End of class
            out.println( "}" );
            
            out.flush();
            out.close();
            
            return classDescriptor;
        } catch( java.io.FileNotFoundException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                    + ". Could not find file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        } catch( java.io.IOException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                    + ". Could not create file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }
}
