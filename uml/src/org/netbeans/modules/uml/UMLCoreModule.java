/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * UMLCoreModule.java
 *
 * Created on March 14, 2005, 7:21 AM
 */

package org.netbeans.modules.uml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class UMLCoreModule 
{   
   public static void checkInitUml1() {
      // Retrieve the desired configuration home.  If the property
      // embarcadero.home-dir has already been set then do not change it
      // (because that is where the user wants the configuration directory).
      // If the configuration directory has not been set then set it to
      // netbeans user home.  The configuration manager will then use the
      // embarcadero.home-dir property to determine the configuration location.

       String nbuser = System.getProperty("netbeans.user"); // NOI18N
       if (nbuser!=null)
       {
           copyDotUmlIntoUserDir(nbuser, "org/netbeans/modules/uml/dotuml1.zip", null);
       }
   }
   
   
   public static void checkInit() {
       String nbuser = System.getProperty("netbeans.user"); // NOI18N
       if (nbuser!=null)
       {
           copyDotUmlIntoUserDir(nbuser, 
                                 "org/netbeans/modules/uml/dotuml2.zip", 
                                 "config"+File.separator+"DesignCenter");
       }
   }


   public static void copyDotUmlIntoUserDir(String userdir, String zipResource, String subdirToExist) 
   {
       try {
			//check to see if .uml already exists in the userdir
                        File file1;
                        if (subdirToExist == null || subdirToExist.length() == 0) {
                            file1 = new File(new File(new File(userdir), ".uml"), ".created-" + Integer.toHexString(zipResource.hashCode()));
                        } else {
                            file1 = new File(new File(new File(userdir), ".uml"), subdirToExist);
                        }
                        
                        if (file1.exists()) {
                            return;
			}
                        
			ClassLoader loader = UMLCoreModule.class.getClassLoader();
			InputStream in = null;
			if (loader!=null)
			   in = loader.getResourceAsStream(zipResource);
			else
			   return;

			if (in==null)
			   return;
			
			byte[] buf=new byte[1024];
			int n;
			ZipEntry zipEntry;
			FileOutputStream fileOutputStream = null;
			ZipInputStream zipInputStream = null;
			
			zipInputStream = new ZipInputStream(in);
			zipEntry = zipInputStream.getNextEntry();
			
			while (zipEntry != null) 
            { 
                //for each entry to be extracted
                String entryName = zipEntry.getName();
				if (zipEntry.isDirectory()) 
				{
				   File f1 = new File(userdir + File.separator + entryName);
				   f1.mkdirs();
				}
				else
				{
					fileOutputStream = new FileOutputStream(
					   userdir + File.separator + entryName);             

					while ((n = zipInputStream.read(buf, 0, 1024)) > -1)
						fileOutputStream.write(buf, 0, n);
				}
				if (fileOutputStream!=null)
					fileOutputStream.close(); 
                zipInputStream.closeEntry();
                zipEntry = zipInputStream.getNextEntry();

            }//while

            zipInputStream.close();
            
            if (!file1.exists()) {
                file1.createNewFile();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
   }
}
