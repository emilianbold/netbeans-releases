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


/**
 * This class is to generate the BeanInfo class for the generated DataProvider
 *
 * @author  cao
 */
public class DataProviderBeanInfoGenerator {
    
    // Hardcode the iconFileName here
    public static final String DATA_PROVIDER_ICON_FILE_NAME = "/org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif";
    public static final String DATA_PROVIDER_ICON_FILE_NAME2 = "/org/netbeans/modules/visualweb/ejb/resources/table_dp_badge.png";
    
    private String fullBeanClassName;
    private MethodInfo method;
    
    public DataProviderBeanInfoGenerator( String fullBeanClassName, MethodInfo method ) {
        this.fullBeanClassName = fullBeanClassName;
        this.method = method;
    }
    
    public ClassDescriptor generateClass( String srcDir ) throws EjbLoadException
    {
         
        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;
        
        try
        {
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
            
            PrintWriter out = new PrintWriter( new FileOutputStream(javaFile) );

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
}
