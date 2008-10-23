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


package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Trey Spiva
 */
public class FileSysManip
{
   public static String getExtension(final String fullFilename)
   {
      String retVal = "";

      int pos = fullFilename.lastIndexOf('.');
      if(pos >= 0)
      {
         retVal = fullFilename.substring(pos + 1);
      }

      return retVal;
   }

   /**
	*
	* Determines whether or not the file pointed to contains the passed-in
	* string anywhere in the file.
	*
	* @param fileName[in]  The file to crack open and search
	* @param strToFind[in] The string to search for.
	*
	* @return true if strToFind was found in the passed in file, else false
	*
	*/
   public static boolean isInFile(String strToFind, String fileName )
   {
   		boolean isFound = false;
   		isFound = FileManip.isInFile(strToFind, fileName);
   		return isFound;
   }
   
   /**
	*
	* Resolves any sub strings in the passed in string that begin with '%' and
	* end with '%'. The string between the asterixes must be found in the preference
	* file, else filePath is returned
	*
	* @param filePath[in]  The string to check for variables
	*
	* @return The resolved string
	*
	*/
   public static String resolveVariableExpansion(String filePath)
   {
   		return FileManip.resolveVariableExpansion(filePath);
   }
   
   /**
	* Returns the argument filename with the argument extension
	*
	* @param fullFilename The full path to the file
	* @param extension The extension for the file
	* 
	* @return The full filename for the file with the indicated extension
	*/
   public static String ensureExtension(String fullFilename, String extension)
   {
   		return StringUtilities.ensureExtension(fullFilename, extension);
   }

   public static String getFinalDirectory(String basePath)
   {
      String retVal = basePath;
      
      int index = basePath.lastIndexOf(File.separatorChar);
      if(index >= 0)
      {
         retVal = basePath.substring(index);
      }
      
      return retVal;
   }
   
   /**
    * Returns just the path for the incoming filename
    */
   public static String getPath(String filename)
   {
      // We just want to return the path that contains the file.  Therefore,
      // we want the path to the files parent.
      File f = new File(filename);
      return f.getParent();
   }
   
	/**
	 * @param fileName
	 * @param m_CacheDir
	 * @return
	 */
	public static String retrieveRelativePath(String fileName, String directory)
	{
		return PathManip.retrieveRelativePath(fileName, directory);
//		String retVal = "";
//		if (fileName != null && directory != null)
//		{
//			try
//			{
//				File direc = new File(directory);
//				File file = new File(fileName);
//				while (true)
//				{
//					File parent = file.getParentFile();
//					String str = file.getParent();
//					if (parent != null && parent.compareTo(direc) == 0)
//					{
//						retVal = File.separatorChar + file.getName();
//						break;
//					}
//					else if (str == null)
//					{
//						retVal = fileName;
//						break;
//					}
//					else
//					{
//						file = file.getParentFile();
//					}
//				}
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//		}
//		return retVal;
	}

	/**
	 *
	 * Checks to see if path is relative to rootPath. If it is, an absolute path
	 * is built.
	 *
	 * @param path[in] The path to convert to an absolute path if necessary.
	 * @param rootPath[in] The location that path is checked against to see if it is
	 *                     relative
	 * @param rootPathAttr [in] Specifies whether rootPath is a directory or a file.
	 *
	 * @return HRESULT
	 *
	 */
	public static String retrieveAbsolutePath(String path, String rootPath)
	{
		String newPath = path;
		if (path != null)
		{
			File file = new File(path);
			if (!file.isAbsolute())
			{
				if (file.isFile())
				{
					// If we have a file remove the file spec, otherwise leave the path
					// alone and make sure, below, it has a slash
					file = file.getParentFile();
				}
				newPath = file.getAbsolutePath() + File.separator + rootPath;
			}
		}
		return newPath;
	}

	/**
	 * Creates a full path out of a location, filename and extension.  This
	 * version DOES NOT account for the location being a possible filename.
	 *
	 * @param location The location of the file
	 * @param filename The filename for the file
	 * @param extension The extension for the file
	 * 
	 * @return The full filename for the file
	 */
	public static String createFullPath(String location, String filename, String extension)
	{
		String retStr = "";
		File file = new File(location);
		if (!file.exists())
		{
			file.mkdirs();
		}
      retStr = file.getPath() + File.separatorChar + filename;
      retStr = ensureExtension(retStr, extension);
      
		return retStr;
	}
        
   public static boolean copyFile1(String fromFile, String toFile)
   {
   		boolean copySuccess = false;
   		File from = new File(fromFile);
   		File to = new File(toFile);
   		copySuccess = from.renameTo(to);
   		return copySuccess;
   }
   
   public static boolean copyFile(String fromFile, String toFile)
   {
       boolean copySuccess = false;
       if ( fromFile != null && toFile != null)
       {
            try {
                FileObject fromFO = FileUtil.toFileObject(new File(fromFile));
                FileObject toFO = FileUtil.createData(new File(toFile));
                if ( fromFO != null) {
                    FileObject copiedFO = FileUtil.copyFile(fromFO, 
                            toFO.getParent(), toFO.getName(), toFO.getExt());
                    if (copiedFO != null)
                    {
                        fromFO.delete();
                        copySuccess = true;
                    }
                }
            } catch (IOException ex) {
                UMLLogger.logException(ex, Level.WARNING);
            }
       }
       return copySuccess;
   }
   /**
	* Adds a backslash to the path
	*/
   public static String addBackslash(String sPath)
   {
   		String sNew = "";
   		if (sPath != null && sPath.length() > 0)
   		{
   			File f = new File(sPath);
   			String slash = File.separator;
   			int pos = sPath.lastIndexOf(slash);
   			if (pos == (sPath.length() -1) )
   			{
   				sNew = sPath; 
   			}
   			else
   			{
   				sNew = sPath + slash;
   			}
   		}
   		return sNew;
   }

   /**
    * Returns just the filename (with extension) for the incoming, full path, filename
    */
   public static String getFileNameAndExtension( String fullFilename )
   {
      File file = new File( fullFilename );
      return file.getName();
   }

   public static void backupCopy(String fileName)
   {
       try {
           if ((fileName != null) && (fileName.length() > 0))
           {
               File srcFile = new File(fileName);
               FileObject srcFO = FileUtil.toFileObject(srcFile);
               backupCopy(srcFO);
           }
       } catch (Exception ex) {
           UMLLogger.logException(ex, Level.WARNING);
       }
   }

   public static void backupCopy(FileObject srcFO)
   {
       try {
           if (srcFO != null) 
           {
               FileObject parentFolderFO = srcFO.getParent(); 
               String fileNameWithoutExt = srcFO.getName(); 
               
               FileObject destFolderFO = FileUtil.createFolder(parentFolderFO, "DiagramBackup"); //NOI18N
               FileObject destFO = destFolderFO.getFileObject(fileNameWithoutExt, srcFO.getExt());
               if (destFO != null) 
               {
                   destFO.delete();
               }                                
               FileUtil.copyFile(srcFO, destFolderFO, fileNameWithoutExt);
           }
       } catch (Exception ex) {
           UMLLogger.logException(ex, Level.WARNING);
       }
   }


}
