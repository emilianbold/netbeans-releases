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
package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbContainerVendor;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * This class is used to extract the deployment descriptors from the given
 * jar file. If the given jar file contains jar files, it'll recursively
 * go through all the contained jar files and extract all the deployment
 * descriptors. 
 */

public final class DeploymentDescriptorExtractor 
{
    public static final String JAR_FILE_EXTENSION = ".jar";
    
    /**@todo where should the tmp dir be*/
    private String tmpDir = System.getProperty("java.io.tmpdir");
    //private String tmpDir = "d:/home/cao/xmls";
    
    // A map of ( standard deployment descriptor name, vendor specific deployment descriptor name )
    private Map deploymentDescriptors = new HashMap();
    
    // All the classes found from the jar file
    private Set allClazz = new HashSet();
    
    // The inner jar files - the jar files contained in the original jar file
    private ArrayList tmpJarFiles = new ArrayList();
   
    public DeploymentDescriptorExtractor( ArrayList jarFiles ) throws EjbLoadException {
        for( Iterator iter = jarFiles.iterator(); iter.hasNext(); )
        {
            String jarFile = (String)iter.next();
            extract( jarFile, this.deploymentDescriptors, this.allClazz );
        }
    }
    
    public Map getDeploymentDescriptors() {
        return this.deploymentDescriptors;
    }
    
    public Set getAllClazz()
    {
        return this.allClazz;
    }

   public ArrayList getTmpJarFiles()
   {
       return this.tmpJarFiles;
   }

   /**
    * Extracts the deployment descriptor xml files
    */
   private void extract( String jarFileName, Map deploymentDescriptors, Set allClazz ) throws EjbLoadException
   {
      try 
      {
          // First, just extract the entry size only
          ZipFile zf=new ZipFile( jarFileName );
          Map entrySizes = new HashMap();
          
          Enumeration e=zf.entries();
          while( e.hasMoreElements() ) 
          {
              ZipEntry ze=(ZipEntry)e.nextElement();
              
              entrySizes.put( ze.getName(), new Integer( (int)ze.getSize() ) );
          }
          
          zf.close();

          // Now, extract resources and look for the deployment descriptors.
          
          FileInputStream fileInputStream = new FileInputStream( jarFileName );
          BufferedInputStream bufferedInputStream = new BufferedInputStream( fileInputStream );
          ZipInputStream zipInputStream = new ZipInputStream( bufferedInputStream );
          
          String stdXml = null;
          String vendorXml = null;
          ZipEntry zipEntry = null;
          while( ( zipEntry = zipInputStream.getNextEntry()) != null ) 
          {
             if( zipEntry.isDirectory() ) 
             {
                continue;
             }

             int size = (int)zipEntry.getSize();
             
             // -1 means unknown size. 
             if( size==-1 ) 
             {
                size = ((Integer)entrySizes.get( zipEntry.getName()) ).intValue();
             }
             
             // Read the content of this zip entry
             byte[] b = new byte[(int)size];
             int rb = 0;
             int chunk = 0;
             while( ((int)size - rb) > 0 ) 
             {
                 chunk = zipInputStream.read( b, rb, (int)size - rb );
                 if( chunk == -1 ) 
                 {
                    break;
                 }
                 
                 rb += chunk;
             }
             
             String theEntry = zipEntry.getName();
             
             // If it is a class            
             if( theEntry.endsWith( ".class" ) )
             {
                 int index = theEntry.indexOf( '.' );
                 allClazz.add( theEntry.substring(0,index).replace( '/', '.' ) );
             }
             else
             {
                 // If the file is a deployment descriptor, the save it to a tmp diretory.
                 // If the file is a jar file, save it and recursively extract it

                 String fileName = getFileName( theEntry );

                 if( fileName.equalsIgnoreCase( EjbContainerVendor.STANDARD_DEPLOYMENT_DESCRIPTOR ) ||
                     fileName.equalsIgnoreCase( EjbContainerVendor.SUN_DEPLOYMENT_DESCRIPTOR ) ||
                     fileName.equalsIgnoreCase( EjbContainerVendor.WEBLOGIC_DEPLOYMENT_DESCRIPTOR ) ||
                     fileName.equalsIgnoreCase( EjbContainerVendor.WEBSPHERE_DEPLOYMENT_DESCRIPTOR ) )
                 {
                     // Lets remember which xml file we're working on right now
                     boolean workingOnStdXml = true;
                     if( fileName.equalsIgnoreCase( EjbContainerVendor.SUN_DEPLOYMENT_DESCRIPTOR ) ||
                         fileName.equalsIgnoreCase( EjbContainerVendor.WEBLOGIC_DEPLOYMENT_DESCRIPTOR ) ||
                         fileName.equalsIgnoreCase( EjbContainerVendor.WEBSPHERE_DEPLOYMENT_DESCRIPTOR ) )
                         workingOnStdXml = false;

                     // Going to append a number to file name in case the jar file
                     // contains several deployment descriptor
                     // Note: need to deal with websphere deployment descriptors
                     // are little different because it is a .xmi not .xml
                     if( fileName.equalsIgnoreCase( EjbContainerVendor.WEBSPHERE_DEPLOYMENT_DESCRIPTOR ) )
                     {
                         String newNamePart = deploymentDescriptors.size() + ".xmi";
                         fileName = fileName.replaceAll( ".xmi", newNamePart );
                     }
                     else
                     {
                         String newNamePart = deploymentDescriptors.size() + ".xml";
                         fileName = fileName.replaceAll( ".xml", newNamePart );
                     }

                     String fileWithPath = writeXmlFile( fileName, b );

                     if( workingOnStdXml )
                         stdXml = fileWithPath;
                     else
                         vendorXml = fileWithPath;

                     // Add the file names to the map if both of them are ready
                     if( stdXml != null && vendorXml != null )
                     {
                         deploymentDescriptors.put( stdXml, vendorXml );
                         stdXml = null;
                         vendorXml = null;
                     }
                 }

                 // It is jar file, recursively look for deployment descriptors
                 if( fileName.indexOf( JAR_FILE_EXTENSION ) != -1 )
                 {
                     String tmpJarFile = writeXmlFile( fileName, b );
                     extract( tmpJarFile, deploymentDescriptors, allClazz );
                     tmpJarFiles.add( tmpJarFile );
                 }
             }
          }
       } catch( java.io.FileNotFoundException e ) 
       {
           // Log error
           String logMsg = "Error occurred when trying to extract the EJB deployment descriptors. Cannot find from jar file " + jarFileName;
           ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
           e.printStackTrace();
           
           // Throw up as USER_ERROR
           // Client Jaf file {0} not found
           String errMsg = NbBundle.getMessage( DeploymentDescriptorExtractor.class, "FILE_NOT_FOUND", jarFileName );
           throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
       }
       catch( java.io.IOException e )
       {
           // Log error
           String logMsg = "Error occurred when trying to extract the EJB deployment descriptors. Cannot read from jar file " + jarFileName;
           ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, logMsg );
           e.printStackTrace();
           
           // Throw up as USER_ERROR
           // Client Jar file {0} cannot be read
           String errMsg = NbBundle.getMessage( DeploymentDescriptorExtractor.class, "CANNOT_READ_FILE", jarFileName );
           throw new EjbLoadException( EjbLoadException.USER_ERROR, errMsg );
       } 
   }
  
   
   private String getFileName( String zipEntryName )
   {
       int index = zipEntryName.lastIndexOf( '/' );
       
       if( index == -1 )
           return zipEntryName;
       else
       {
           return zipEntryName.substring( index + 1);
       }
   }
   
   private String writeXmlFile( String fileName, byte[] bytes ) throws EjbLoadException
   {
       try
       {  
           File file = new File( getTempDir(), fileName );
           file.deleteOnExit();
           
           FileOutputStream fos = new FileOutputStream( file );
           BufferedOutputStream bos = new BufferedOutputStream( fos );
           bos.write( bytes );
           bos.flush();
           bos.close();
           fos.close();
           return file.getAbsolutePath();
       }
       catch( Exception e )
       {
           ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.DeploymentDescriptorExtractor").log( ErrorManager.ERROR, e.getMessage() );
           e.printStackTrace();
           
           throw new EjbLoadException( e.getMessage() );
       }
   }
   
   private File getTempDir()
   {
       File file = new File( tmpDir );
       
       if( !file.exists() )
           file.mkdirs();
       
       return file;
   }
}	
