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
