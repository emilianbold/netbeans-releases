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


package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;

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
   
   public static boolean copyFile(String fromFile, String toFile)
   {
   		boolean copySuccess = false;
   		File from = new File(fromFile);
   		File to = new File(toFile);
   		copySuccess = from.renameTo(to);
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
}
