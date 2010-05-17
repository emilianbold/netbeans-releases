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
 * DataProviderBeanInfoGenerator.java
 *
 * Created on February 16, 2005, 4:50 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;


/**
 * This class is to generate the BeanInfo class for the generated DataProvider
 *
 * @author  cao
 */
public class DataProviderBeanInfoGenerator {
    
    // Hardcode the iconFileName here
    public static final String DATA_PROVIDER_ICON_FILE_NAME = "/org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif";
    public static final String DATA_PROVIDER_ICON_FILE_NAME2 = "/org/netbeans/modules/visualweb/ejb/resources/table_dp_badge.png";

    private static final String[][] DEFAULT_PROPERTIES = {
        {"dataClassInstance", "prop_dataClassInstance", "\"getDataClassInstance\""},
        {"dataMethod", "prop_dataMethod", "\"getDataMethod\""},
        {"dataMethodArguments", "propDataMethodArguments", "\"getDataMethodArguments\""},
        {"resultObject", "prop_resultObject", "\"getResultObject\""},
        {"resultObjects", "prop_resultObjects", "\"getResultObjects\""}
    };
    
    private String fullBeanClassName;
    private MethodInfo method;
    private PrintWriter out;
    private String clientWrapperClassName;
    
    public DataProviderBeanInfoGenerator( String fullBeanClassName, MethodInfo method, String clientWrapperClassName ) {
        this.fullBeanClassName = fullBeanClassName;
        this.method = method;
        this.clientWrapperClassName = clientWrapperClassName;
    }
    
    public ClassDescriptor generateClass( String srcDir ) throws EjbLoadException
    {
         
        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;
        
        try
        {
            // Figure out the package name, class name and directory/file name

            int j = fullBeanClassName.lastIndexOf( "." );
            String beanClassName = fullBeanClassName.substring( j+1 );
            
            // Package name
            if( j == -1 ) // No package
                j = 0;
            
            String packageName = fullBeanClassName.substring( 0, j  );
            
            String beanInfoClassName = beanClassName + "BeanInfo";

            String classDir = packageName.replace( '.', File.separatorChar );
            File dirF = new File( srcDir + File.separator + classDir );
            if( !dirF.exists() )
            {
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
            
            out = new PrintWriter( new FileOutputStream(javaFile) );

            // pacage
            if( packageName != null && packageName.length() != 0 )
            {
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
            out.println( "import javax.swing.ImageIcon;" );
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
            String iconFileNameVariable2 = "iconFileName2";
            String beanDescriptorVariable = "beanDescriptor";
            String propDescriptorsVariable = "propDescriptors";
            out.println( "    private Class " + beanClassVariable + " = " + fullBeanClassName + ".class;" );
            out.println( "    private PropertyDescriptor[] " + propDescriptorsVariable + " = null; " );
            out.println( "    private String " + iconFileNameVariable + " = \"" + DATA_PROVIDER_ICON_FILE_NAME + "\";" );
            out.println( "    private String " + iconFileNameVariable2 + " = \"" + DATA_PROVIDER_ICON_FILE_NAME2 + "\";" );
            out.println( "    private BeanDescriptor " + beanDescriptorVariable + " = null;" );
            out.println();
            
            // Method - getIcon()
            out.println( "    public Image getIcon(int iconKind) {" );
            out.println( "        ImageIcon imgIcon1 = new ImageIcon(getClass().getResource( " + iconFileNameVariable + " )); " );
            out.println( "        ImageIcon imgIcon2 = new ImageIcon(getClass().getResource( " + iconFileNameVariable2 + " )); " );
            out.println( "        return mergeImages( imgIcon1.getImage(), imgIcon2.getImage() );" );
            out.println( "    }" );
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

            println("    public PropertyDescriptor[] getPropertyDescriptors() {");
            println("        if (" + propDescriptorsVariable + " != null) {");
            println("            return " + propDescriptorsVariable + ";");
            println("        }");
            println("        try {");

            String indent = "            ";
            for (int i = 0; i < DEFAULT_PROPERTIES.length; i++) {
                String[] p = DEFAULT_PROPERTIES[i];
                genPropertyDescriptor(indent, p[1], p[0], p[2], "null", true, true);
            }
            
            String clientClass = clientWrapperClassName;
            String clientProp = Character.toLowerCase(clientClass.charAt(0)) + clientClass.substring(1);

            String clientVar = "prop_" + clientProp;
            String clientGetter = "\"get" + clientClass + "\"";
            String clientSetter = "\"set" + clientClass + "\"";
            genPropertyDescriptor(indent, clientVar, clientProp, clientGetter, clientSetter, false, true);

            for (Object p : method.getParameters()) {
                MethodParam param = (MethodParam)p;
                String name = param.getName();
                String propVar = "prop_" + name;
                String nameCaps = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                String getter = "\"get" + nameCaps + "\"";
                String setter = "\"set" + nameCaps + "\"";
                
                genPropertyDescriptor(indent, propVar, name, getter, setter, false, false);
            }

            println("            " + propDescriptorsVariable + " = new PropertyDescriptor[] {");
            for (int i = 0; i < DEFAULT_PROPERTIES.length; i++) {
                println("                " + DEFAULT_PROPERTIES[i][1] + ",");
            }
            
            for (Object p : method.getParameters()) {
                MethodParam param = (MethodParam)p;
                println("                prop_" + param.getName() + ",");
            }

            println("                " + clientVar);
            println("            };");

            println("            return " + propDescriptorsVariable + ";");
            println("        }catch (java.beans.IntrospectionException e) {");
            println("            e.printStackTrace();");
            println("            return null;");
            println("        }");
            println("    }");
            println();
            
            // private method for merging two images into one
            out.println( "    private Image mergeImages (Image image1, Image image2) {" );
            out.println( "        int w = image1.getWidth(null);" );
            out.println( "        int h = image1.getHeight(null);" );
            out.println( "        int x = image1.getWidth(null) - image2.getWidth(null);" );
            out.println( "        int y = image1.getHeight(null) - image2.getHeight(null);" );
            out.println();
            out.println( "        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment ()." );
            out.println( "                                          getDefaultScreenDevice ().getDefaultConfiguration ()." );
            out.println( "                                          getColorModel (java.awt.Transparency.BITMASK);" );
            out.println( "        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage (model," );
            out.println( "             model.createCompatibleWritableRaster (w, h), model.isAlphaPremultiplied (), null);" );
            out.println();
            out.println( "        java.awt.Graphics g = buffImage.createGraphics ();" );
            out.println( "        g.drawImage (image1, 0, 0, null);" );
            out.println( "        g.drawImage (image2, x, y, null);" );
            out.println( "        g.dispose();" );
            out.println();
            out.println( "        return buffImage;" );
            out.println( "    }" );
            out.println();

            // End of class
            out.println( "}" );
            
            out.flush();
            out.close();
            
            return classDescriptor;
        }
        catch( java.io.FileNotFoundException ex )
        {
            // Log error
            /*String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                            + ". Could not find file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );*/
            ex.printStackTrace();

            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
        catch( java.io.IOException ex ) 
        {
            // Log error
            /*String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                            + ". Could not create file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );*/
            ex.printStackTrace();

            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }
    
    private void genPropertyDescriptor(String indent, String propVar, String prop, String getter, String setter, boolean hidden, boolean useCustomEditor) {
        println(indent + "PropertyDescriptor " + propVar + " = new PropertyDescriptor(\"" + prop + "\",beanClass," + getter + "," + setter + ");");
        
        println(indent + propVar + ".setExpert(false);");
        
        if (hidden) {
            println(indent + propVar + ".setHidden(true);");
        }else {
            println(indent + propVar + ".setHidden(false);");
        }

        println(indent + propVar + ".setPreferred(false);");
        
        if (useCustomEditor) {
            println(indent + propVar + ".setPropertyEditorClass(com.sun.rave.propertyeditors.SelectOneDomainEditor.class);");
            println(indent + propVar + ".setValue(com.sun.rave.designtime.Constants.PropertyDescriptor.CATEGORY,com.sun.rave.designtime.base.CategoryDescriptors.DATA);");
            println(indent + propVar + ".setValue(\"com.sun.rave.propertyeditors.DOMAIN_CLASS\", com.sun.rave.propertyeditors.domains.InstanceVariableDomain.class);");
        }
    }

    private void println(String string) {
        out.println(string);
    }
    
    private void println() {
        out.println();
    }
}
