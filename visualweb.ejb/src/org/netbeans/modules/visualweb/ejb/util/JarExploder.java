/*
 * JarExploder.java
 *
 * Created on October 20, 2004, 1:43 PM
 */

package org.netbeans.modules.visualweb.ejb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.openide.ErrorManager;

/**
 * A utility class to explode the given jar file
 *
 * @author  cao
 */
public class JarExploder {
    
    private boolean classOnly = false;
    
    public JarExploder()
    {
        this( false );
    }
    
    public JarExploder( boolean classOnly )
    {
        this.classOnly = classOnly;
    }
    
    public static ArrayList getAllClasses( String jarPath ) throws FileNotFoundException, IOException {
        try
        {
            ArrayList allClazz = new ArrayList();
            
            // First, just extract the entry size only
            ZipFile zf=new ZipFile( jarPath );
            Map entrySizes = new HashMap();

            Enumeration e=zf.entries();
            while( e.hasMoreElements() ) {
                ZipEntry ze=(ZipEntry)e.nextElement();

                entrySizes.put( ze.getName(), new Integer( (int)ze.getSize() ) );
            }

            zf.close();

            // Now, extract resources and look for the deployment descriptors.

            FileInputStream fileInputStream = new FileInputStream( jarPath );
            BufferedInputStream bufferedInputStream = new BufferedInputStream( fileInputStream );
            ZipInputStream zipInputStream = new ZipInputStream( bufferedInputStream );

            String stdXml = null;
            String vendorXml = null;
            ZipEntry zipEntry = null;
            while( ( zipEntry = zipInputStream.getNextEntry()) != null ) {
                if( zipEntry.isDirectory() ) {
                    continue;
                }

                int size = (int)zipEntry.getSize();

                // -1 means unknown size.
                if( size==-1 ) {
                    size = ((Integer)entrySizes.get( zipEntry.getName()) ).intValue();
                }

                // Read the content of this zip entry
                byte[] b = new byte[(int)size];
                int rb = 0;
                int chunk = 0;
                while( ((int)size - rb) > 0 ) {
                    chunk = zipInputStream.read( b, rb, (int)size - rb );
                    if( chunk == -1 ) {
                        break;
                    }

                    rb += chunk;
                }
                
                if( zipEntry.getName().endsWith( ".class" ) )
                {
                    int index = zipEntry.getName().indexOf( '.' );
                    allClazz.add( zipEntry.getName().substring(0,index).replace( '/', '.' ) );
                }
            }
            
            return allClazz;
            
        } catch( java.io.FileNotFoundException e ) {
            // Log eror
            String logMsg = "Error occurred when trying to explode jar file. Cannot find file " + jarPath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.util.JarExploder").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
        catch( java.io.IOException e )
        {
            // Log eror
            String logMsg = "Error occurred when trying to explode jar file. Cannot read file " + jarPath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.util.JarExploder").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
    }
    
    public void explodeJar( String destDir, String jarPath ) throws FileNotFoundException, IOException
    {
        try
        {
            // First, just extract the entry size only
            ZipFile zf=new ZipFile( jarPath );
            Map entrySizes = new HashMap();

            Enumeration e=zf.entries();
            while( e.hasMoreElements() ) {
                ZipEntry ze=(ZipEntry)e.nextElement();

                entrySizes.put( ze.getName(), new Integer( (int)ze.getSize() ) );
            }

            zf.close();

            // Now, extract resources and look for the deployment descriptors.

            FileInputStream fileInputStream = new FileInputStream( jarPath );
            BufferedInputStream bufferedInputStream = new BufferedInputStream( fileInputStream );
            ZipInputStream zipInputStream = new ZipInputStream( bufferedInputStream );

            String stdXml = null;
            String vendorXml = null;
            ZipEntry zipEntry = null;
            while( ( zipEntry = zipInputStream.getNextEntry()) != null ) {
                if( zipEntry.isDirectory() ) {
                    continue;
                }

                int size = (int)zipEntry.getSize();

                // -1 means unknown size.
                if( size==-1 ) {
                    size = ((Integer)entrySizes.get( zipEntry.getName()) ).intValue();
                }

                // Read the content of this zip entry
                byte[] b = new byte[(int)size];
                int rb = 0;
                int chunk = 0;
                while( ((int)size - rb) > 0 ) {
                    chunk = zipInputStream.read( b, rb, (int)size - rb );
                    if( chunk == -1 ) {
                        break;
                    }

                    rb += chunk;
                }
                
                if( !this.classOnly ||
                    (this.classOnly && zipEntry.getName().endsWith( ".class" ) ) )
                {
                    saveFile( destDir, zipEntry.getName(), b );
                }
            }
        } catch( java.io.FileNotFoundException e ) {
            // Log eror
            String logMsg = "Error occurred when trying to explode jar file. Cannot find file " + jarPath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.util.JarExploder").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
        catch( java.io.IOException e )
        {
            // Log eror
            String logMsg = "Error occurred when trying to explode jar file. Cannot read file " + jarPath;
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.util.JarExploder").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
    }
    
    private String saveFile( String destDir, String fileName, byte[] bytes ) throws FileNotFoundException, IOException {
        
        File file = new File( destDir, fileName );
        
        try {
            if( !file.exists() ) 
                file.getParentFile().mkdirs();
            
            FileOutputStream fos = new FileOutputStream( file );
            BufferedOutputStream bos = new BufferedOutputStream( fos );
            bos.write( bytes );
            bos.flush();
            bos.close();
            fos.close();
            return file.getAbsolutePath();
        }
        catch( java.io.FileNotFoundException e ) {
            // Log eror
            String logMsg = "Error occurred when trying to save file. Cannot find file " + file.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
        catch( java.io.IOException e )
        {
            // Log eror
            String logMsg = "Error occurred when trying to save file. Cannot write to file " + file.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
            e.printStackTrace();
            
            throw e;
        }
    }
    
}
