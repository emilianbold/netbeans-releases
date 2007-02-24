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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.Vector;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;

public class ProjectClassLocator implements IProjectClassLocator
{
   private IElementLocator m_Locator = null;
   private Vector<IProject> m_Projects = null;

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IProjectClassLocator#addProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
    */
   public void addProject(IProject proj) 
   {
      if (m_Projects == null)
      {
         m_Projects = new Vector<IProject>();
      }
      m_Projects.add(proj);
   }

   /**
    * Retreive the loacation the class that contains the speicfied class.  The 
    * Describe projects are searched to locate a class with the specified package
    * can class name.
    * 
    * @param package [in] The package that contains the calling class.
    * @param className [in] The name of the class to locate.
    * @param fullpath [out] The name of the file that contains the class defintion.
    *                       If the class is not located fullpath wil be an
    *                       empty string.
    * @return S_OK if no errors ocured. DSL_E_INIT_SYSTEM if the system has not
    *         been initialized, and E_INVALID_ARG if className is an empty string.
    */
   public ETPairT<String,String> locateFileForClass(
           String pack, 
           String className,
           ETList<IDependencyEvent> deps)
   {
      ETPairT<String, String> retVal = null;
      IElementLocator pLocator = getElementLocator();
      if (pLocator != null && m_Projects != null)
      {
         int count = m_Projects.size();
         for (int i=0; i<count; i++)
         {
            IProject proj = m_Projects.get(i);
            ETList<IElement> foundElems = pLocator.findScopedElements(proj, className);
            if (foundElems != null)
            {
               int size = foundElems.size();
               for (int j=0; j<size; j++)
               {
                  IElement curEle = foundElems.get(j);
                  if (curEle instanceof INamedElement)
                  {
                     INamedElement neEle = (INamedElement)curEle;
                     retVal = findClass(neEle, className, pack, deps);
                     if (retVal != null)
                     {
                        break;
                     }
                  }
               }
            }
         }
      }
      return retVal;
   }

   /**
    * Checks if an INamedElement is the model elemetn that we are looking for,
    * 
    * Search Algorithm:
    *  1) Check if the elements qualified name is the same as the passed in
    *     class name.
    *  2) Check if the passed in name is in the same package as the passed
    *     in package name.
    *  3) Check if the passed in element is specified in the dependencies.
    *
    * @param pElement [in] The element to test.
    * @param className [in] The name of the class to find.
    * @param package [in] The package that contains the eferencing ("this") class.
    * @param depends [in] The dependencies of the referencing ("this") class.
    * @param fullScopeName [out] The fully qualified name of the located class.
    *                            <B>NOTE:</B> fullScopeName will only be set if 
    *                             the return value is S_OK.
    * @param fullPath [out] The path to the file that contains the class.
    *                       <B>NOTE:</B> fullPath will only be set if 
    *                       the return value is S_OK.
    *
    * @return S_OK if the class was located; S_FALSE if the class was not located.
    */
   public ETPairT<String,String> findClass(INamedElement   pElement,
                                           String          className,
                                           String          pack, 
                                           ETList<IDependencyEvent> deps)
   {
      ETPairT<String,String> retVal = null;
      
      String qualName = pElement.getFullyQualifiedName(false);
      String classStr = className;
      String fullScopeName = "";
      String fullPath = getSourceIfSame(pElement, classStr, qualName);
      if (pack != null && pack.length() > 0 && fullPath == null)
      {
         String cPack = pack;
         cPack += "::";
         cPack += className;
         fullPath = getSourceIfSame(pElement, cPack, qualName);
         if (fullPath != null)
         {
            fullScopeName = cPack;
         }
      }
      else
      {
         fullScopeName = qualName;
      }
      
      if (fullPath == null)
      {
         retVal = findUsingDependencies(pElement, qualName, className, deps, fullScopeName);
      }
      else
      {
         retVal = new ETPairT<String, String>(fullScopeName, fullPath);
      }
      
      return retVal;
   }
   
   /**
    * Searchs the dependencies to determine if the specified qualified name is
    * the is the desiried class.
    *
    * @param pElement [in] The element to test.
    * @param qualName [in] The qualified name of the class to find.
    * @param className [in] The name of the class to find.  The class name is used if
    *                       a dependency is a package dependency as opposed to a class
    *                       dependency.
    * @param package [in] The package that contains the eferencing ("this") class.
    * @param depends [in] The dependencies of the referencing ("this") class.
    * @param fullScopeName [out] The fully qualified name of the located class.
    *                            <B>NOTE:</B> fullScopeName will only be set if 
    *                             the return value is S_OK.
    * @param fullPath [out] The path to the file that contains the class.
    *                       <B>NOTE:</B> fullPath will only be set if 
    *                       the return value is S_OK.
    *
    * @return S_OK if the class was located; S_FALSE if the class was not located.
    */
   public ETPairT<String, String> findUsingDependencies(INamedElement   pElement, String qualName,
                                       String className, ETList<IDependencyEvent> deps,
                                       String fullScopeName)
   {
      ETPairT<String, String> retVal = null;
      String fullPath = "";
      if (deps != null)
      {
         int count = deps.size();
         for (int i=0; i<count; i++)
         {
            IDependencyEvent depEvent = deps.get(i);
            String depPack = depEvent.getSupplier();
            if (depPack != null && depPack.length() > 0)
            {
               // If the dependency is a class dependecy and it is the 
               // class that we are looking for then use that dependency;
               boolean isClassDep = depEvent.getIsClassDependency();
               if (isClassDep)
               {
                  boolean isSame = depEvent.isSameClass(className);
                  if (isSame)
                  {
                     fullPath = getSourceIfSame(pElement, depPack, qualName);
                     if (fullPath != null)
                     {
                        fullScopeName = depPack;
                     }
                     break;
                  }
               }
               else
               {
                  depPack += "::";
                  depPack += className;
                  
                  fullPath = getSourceIfSame(pElement, depPack, qualName);
                  
                  if (fullPath != null)
                  {
                     fullScopeName = depPack;
                  }
                  break;
               }
            }
         }
      }
      return retVal;
   }   

   public String getSourceIfSame(INamedElement pElement, String dependencyPackage, String qualName)
   {
      String fullPath = null;
      if (dependencyPackage != null && dependencyPackage.equals(qualName))
      {
         fullPath = getSourceLocation(pElement);
      }
      return fullPath;
   }
   
   public String getSourceLocation(IElement pElement)
   {
      String retVal = null;
      if (pElement != null)
      {
         ETList<IElement> pArtifacts = pElement.getSourceFiles();
         if (pArtifacts != null)
         {
            int count = pArtifacts.size();
            if (count > 0)
            {
               IElement pEle = pArtifacts.get(0);
               if (pEle instanceof ISourceFileArtifact)
               {
                  retVal = ((ISourceFileArtifact)pEle).getSourceFile();
               }
            }
         }
      }
      return retVal;
   }

   /**
    * Retrieve the full path to the specified class name.
    * 
    * @param filename [in] The short form of the file name.
    * @param fullpath [out] The full path to the specified file.
    *                       If the class is not located fullpath wil be an
    *                       empty string.
    */
   public String locateFile(String filename) 
   {
      //nothing in C++ code.
      return null;
   }

   /**
    * Only valid way to access m_cpElementLocator.
    * Ensures m_cpElementLocator is valid and returns it as a reference
    */
   private IElementLocator getElementLocator()
   {
      if (m_Locator == null)
      {
         m_Locator = new ElementLocator();
      }
      return m_Locator;
   }
}
