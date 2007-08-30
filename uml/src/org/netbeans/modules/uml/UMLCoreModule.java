/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.openide.modules.ModuleInstall;
import java.util.zip.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class UMLCoreModule extends ModuleInstall 
{   
   public void restored() 
   {
      // Retrieve the desired configuration home.  If the property
      // embarcadero.home-dir has already been set then do not change it
      // (because that is where the user wants the configuration directory).
      // If the configuration directory has not been set then set it to
      // netbeans user home.  The configuration manager will then use the
      // embarcadero.home-dir property to determine the configuration location.

       instantiateDrawingLibrary();
       String nbuser = System.getProperty("netbeans.user"); // NOI18N
       if (nbuser!=null)
       {
		   copyDotUmlIntoUserDir(nbuser);
       }
   }
   
   
   private void instantiateDrawingLibrary() {
       try
        {            
            org.netbeans.modules.uml.DrawingLibraryDecrypter decrypter = 
					new org.netbeans.modules.uml.DrawingLibraryDecrypter("org.netbeans.modules.uml");
            decrypter.myDecrypt(decrypter);            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
   }
   
   
   public void copyDotUmlIntoUserDir(String userdir) 
   {
       try 
	   {
			//check to see if .uml already exists in the userdir
			File file1 = new File(userdir+File.separator+".uml");
			if (file1.exists()) {
				return;
			}

                        if (Utilities.isMac()) {
                               showMacWarning() ;
                        }
                        
			ClassLoader loader = UMLCoreModule.class.getClassLoader();
			InputStream in = null;
			if (loader!=null)
			   in = loader.getResourceAsStream("org/netbeans/modules/uml/dotuml.zip");
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
   }
 
   private void showMacWarning() {
        DialogDescriptor dd = new DialogDescriptor(
                NbBundle.getMessage (UMLCoreModule.class, "MAC_WARNING"), 
                NbBundle.getMessage (UMLCoreModule.class, "MAC_WARNING_TITLE"), 
                 false, 
                 new Object[] {DialogDescriptor.OK_OPTION}, 
                 null, 
                 DialogDescriptor.DEFAULT_ALIGN,
                 null,
                 null)  ;
        
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }
   
}
