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

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * @author sumitabhk
 *
 */
public class PathManip
{
   /**
    *
    * Checks to see if path is relative to rootPath. If it is, an absolute path
    * is built.
    *
    * @param project
    * @param path[in] The path to convert to an absolute path if necessary.
    * @param rootPath[in] The location that path is checked against to see if it is
    *                     relative
    *
    * @return HRESULT
    *
    */
   public static String retrieveAbsolutePath(String path, String rootPath)
   {
      String absPath = "";
      try
      {
         String fileName = "";
         if (rootPath != null && rootPath.length() > 0)
         {
            File file = new File(rootPath);
            if (file.exists())
            {
               if (file.isFile())
               {
                  fileName = file.getParent() + File.separator + path;
               }
               else
               {
                  fileName = rootPath + File.separator + path;
               }
            }
         }
         else
         {
            fileName = rootPath + File.separator + path;
         }
         File file2 = new File(fileName);
         absPath = file2.getCanonicalPath();
      } catch (IOException e)
      {
      }
      
      //
      if( (absPath.length() <= 0) &&
      (rootPath != null) )
      {
         absPath = path;
      }
      
      return absPath;
   }
   
   public static String retrieveSourceAbsolutePath(IProject project, String path, String rootPath)
   {
      String absPath = getAbsolutePathBasedOnSourceRoots(project, path);
      if((absPath == null) || (absPath.length() <= 0))
      {
         absPath = retrieveAbsolutePath(path, rootPath);
      }
      return absPath;
   }
   
   protected static String getAbsolutePathBasedOnSourceRoots(IProject project, String file)
   {
      String retVal = "";
      
      // In NetBeans 4.1 we now associate a UML project to a NetBeans Project.
      // Since a NetBeans project can have multiple source roots we first try
      // convert the filename by using the soruce roots. 
      if(project != null)
      {
         IAssociatedProjectSourceRoots roots = project.getAssociatedProjectSourceRoots();
         if(roots != null)
         {
            retVal = roots.createAbsolutePath(file);
         }
      }
      
      return retVal;
   }
   
   protected static String getRelativeBasedOnSourceRoots(IProject project, String file)
   {
      String retVal = "";
      
      // In NetBeans 4.1 we now associate a UML project to a NetBeans Project.
      // Since a NetBeans project can have multiple source roots we first try
      // convert the filename by using the soruce roots. 
      if(project != null)
      {
         IAssociatedProjectSourceRoots roots = project.getAssociatedProjectSourceRoots();
         if(roots != null)
         {
            retVal = roots.createRelativePath(file);
         }
      }
      
      return retVal;
   }
   /**
    *
    * Retrieves the relative path between newFile and curFile
    *
    * @param newFile[in] The new file we are trying to get a relative path to.
    * @param curFile[in] The root path where we are relative to.
    * @param attrFrom[in] A file attribute indicating the type of the file
    *                     newFile is.
    * @param attrTo[in] A file attribute indicating the type of the file
    *                   curFile is.
    *
    * @return The relative path, else "" on error.
    *
    */
   public static String retrieveRelativePath(String file, String parent)
   {
      String retVal = null;
      
      File parentFile = new File(parent);
      //if parentFile is not a directory, then get its parent.
      if (parentFile != null && (!parentFile.exists() || !parentFile.isDirectory()))
         parentFile = parentFile.getParentFile();
      
      if (parentFile == null)
         return file;
      
      //File childFile = new File(file).getAbsoluteFile();
      File childFile = new File(file);
      if(childFile.isAbsolute() == false)
      {
         childFile = new File(parentFile, file).getAbsoluteFile();
      }
      
      try
      {
         parentFile = parentFile.getAbsoluteFile().getCanonicalFile();
         if (childFile.exists())
            childFile = childFile.getCanonicalFile();
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return file;
      }
      
      
      // If both these files exist, we can use java utility.
      if (parentFile.exists())
      {
         //         if (childFile.exists())
         {
            URI parentURI = parentFile.toURI();
            URI childURI = childFile.toURI();
            String beforeRelative = childURI.getPath();
            
            URI relative = parentURI.relativize(childURI);
            retVal = relative.getPath();
            
            File curParent = parentFile;
            String prefix = "";
            while(retVal.equals(beforeRelative) == true)
            {
               curParent = curParent.getParentFile();
               if(curParent != null)
               {
                  parentURI = curParent.toURI();
                  
                  if(prefix.length() <= 0)
                  {
                     prefix = "..";
                  }
                  else
                  {
                     prefix += File.separatorChar + "..";
                  }
                  
                  relative = parentURI.relativize(childURI);
                  retVal = relative.getPath();
               }
               else
               {
                  // Since there is no parent that means that the child
                  // path is not relative to the parent path.  Therefore
                  // use the full path.
                  retVal = beforeRelative;
                  break;
               }
            }
            
            
            File relFile = new File(retVal);
            retVal = relFile.toString();
            
            if (!relFile.isAbsolute())
            {
               // HAVE TODO: Figure out the relative path.
               // if we have got a relative path and its not preceded
               String tempStr = "." + File.separator;
               if (retVal != null && !retVal.startsWith(tempStr))
               {
                  if(prefix.length() > 0)
                  {
                     retVal = prefix + File.separatorChar + retVal;
                  }
                  else
                  {
                     retVal = tempStr + retVal;
                  }
               }
            }
         }
      }
      else
      {
         retVal = file;
      }
      
      return retVal;
   }
   
   public static String retrieveSourceRelativePath(IProject project, String file, String parent)
   {
      String retVal = getRelativeBasedOnSourceRoots(project, file);
      
      if((retVal == null) || (retVal.length() <= 0))
      {
         return retrieveRelativePath(file, parent);
      }
      
      return retVal;
   }
}


