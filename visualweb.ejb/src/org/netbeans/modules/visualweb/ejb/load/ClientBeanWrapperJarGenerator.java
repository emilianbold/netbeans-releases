/*
 * ClientBeanWrapperJarGenerator.java
 *
 * Created on May 14, 2004, 3:42 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.openide.ErrorManager;

/**
 *
 * @author  cao
 */
public class ClientBeanWrapperJarGenerator {
    private static final int BUFFER = 2048;
    
    /**
     * Jar all the given class in the given jar file
     *
     * @param destJarFile Where all the classes will be added to
     * @param classDescriptors All the classes will be jarred. It is a Collection of ClassDescriptors
     */
    public static void jarThemUp( String ejbGroupName, EjbGroup ejbGroup, ArrayList classDescriptors ) throws EjbLoadException {
        // Split the classes into two pile
        // - DesignInfo classes
        // - not DesignInfo classes
        
        ArrayList designInfoClasses = new ArrayList();
        ArrayList clientWrapperBeanClasses = new ArrayList();
        
        for( int i = 0; i < classDescriptors.size(); i ++ ) {
            ClassDescriptor classDescriptor = (ClassDescriptor)classDescriptors.get( i );
            
            if( classDescriptor.getClassName().indexOf( "DesignInfo" ) != -1 ) // NOI18N
                designInfoClasses.add( classDescriptor );
            else
                clientWrapperBeanClasses.add( classDescriptor );
        }
        
        // Make a jar containing the client wrapper classes (including the BeanInfo classes )
        makeJar( ejbGroupName, ejbGroup.getClientWrapperBeanJar(), clientWrapperBeanClasses, true );
        
        // Make a jar containing the DesignInfo classes
        if( !designInfoClasses.isEmpty() )
            makeJar( ejbGroupName, ejbGroup.getDesignInfoJar(), designInfoClasses, false );
        else
            // For very corner case - for example, the client jar only has one ejb with one method returning void
            ejbGroup.setDesignInfoJar( null );
    }
    
    private static void makeJar( String ejbGroupName, String destJarFile, ArrayList classDescriptors, boolean includeImages ) throws EjbLoadException {
        try {
            // The manifest file contains version and time stamp
            Manifest manifest = new Manifest();
            Attributes attrs = manifest.getMainAttributes();
            attrs.putValue( Attributes.Name.MANIFEST_VERSION.toString(), "2.0" ); // Always 2.0. It matches the Creator
            attrs.putValue( "Generated-time-in-millies",  Long.toString( System.currentTimeMillis() ) ); // NOI18N
            FileOutputStream dest = new FileOutputStream( destJarFile );
            JarOutputStream jarOutputStream = new JarOutputStream( new  BufferedOutputStream(dest), manifest );
            jarOutputStream.setMethod( JarOutputStream.DEFLATED );
            
            byte data[] = new byte[BUFFER];
            BufferedInputStream origin = null;
            
            // Add the classes to the jar file - exclude out the DesignInfo classes
            for( int i = 0; i < classDescriptors.size(); i ++ ) {
                ClassDescriptor classDescriptor = (ClassDescriptor)classDescriptors.get( i );
                
                // Note: I have to replace "\" with "/". Otherwise, the class loader can open the jar file. Weird ...
                FileInputStream fileInputStream = new  FileInputStream( classDescriptor.getFullPathFileName().replace( '\\', '/' ) );
                origin = new  BufferedInputStream( fileInputStream, BUFFER );
                
                JarEntry entry = new JarEntry( classDescriptor.getPackageFileName().replace( '\\', '/' ) );
                jarOutputStream.putNextEntry(entry);
                
                int count;
                while( (count = origin.read(data, 0, BUFFER)) != -1 ) {
                    jarOutputStream.write( data, 0, count );
                }
                
                origin.close();
            }
            
            if( includeImages ) {
                // Add the session bean icon and data provider icon to the wrapper jar file
                
                URL[] imageUrls = new URL[] { ClientBeanWrapperJarGenerator.class.getResource("/org/netbeans/modules/visualweb/ejb/resources/session_bean.png"),
                        ClientBeanWrapperJarGenerator.class.getResource("/org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif"),
                        ClientBeanWrapperJarGenerator.class.getResource("/org/netbeans/modules/visualweb/ejb/resources/table_dp_badge.png") };
                        String[] entryNames = new String[] { Util.getFileName(ClientBeanInfoGenerator.EJB_ICON_FILE_NAME),
                                Util.getFileName(DataProviderBeanInfoGenerator.DATA_PROVIDER_ICON_FILE_NAME),
                                Util.getFileName(DataProviderBeanInfoGenerator.DATA_PROVIDER_ICON_FILE_NAME2) };
                                
                                for( int i = 0; i < imageUrls.length; i ++ ) {
                                    DataInputStream beanImageIn = new DataInputStream( imageUrls[i].openStream());
                                    origin = new BufferedInputStream( beanImageIn, BUFFER );
                                    
                                    // The image entry should be something like org/netbeans/modules/visualweb/ejb/resources/session_bean.png
                                    JarEntry beanImageEntry = new JarEntry( EjbLoader.CLIENT_WRAPPER_PACKAGE_NAME.replace( '.', '/' ) + "/images/" + entryNames[i] ); // NOI18N
                                    jarOutputStream.putNextEntry( beanImageEntry );
                                    
                                    int count;
                                    while( (count = origin.read(data, 0, BUFFER)) != -1 ) {
                                        jarOutputStream.write( data, 0, count );
                                    }
                                    
                                    origin.close();
                                }
            }
            
            // Done with the jar file. Flush...close..
            
            jarOutputStream.flush();
            jarOutputStream.close();
            
        } catch( java.io.FileNotFoundException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to jar the wrapper bean classes for EJB set " + ejbGroupName
                    + ". Could not find file " + destJarFile;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientWrapperJarGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        } catch( java.io.IOException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to jar the wrapper bean classes for EJB set " + ejbGroupName
                    + ". Could not create file " + destJarFile;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientWrapperJarGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }
}
