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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.support.IAssociatedProjectSourceRoots;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import org.netbeans.api.project.FileOwnerQuery;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISourceFileArtifact;
import org.netbeans.modules.uml.core.reverseengineering.reframework.CompositeClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.FileSystemClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ICompositeClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IFileSystemClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ILanguageLibrary;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IProjectClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.LanguageLibrary;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ProjectClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 */
public class OpParserOptions implements IOpParserOptions
{
    /**
     * Initialize the parser options with the given input, creating a class loader
     *
     * @param pElements[in]
     * @param bProcessTest[in]
     * @param bProcessInit[in]
     * @param bProcessPost[in]
     *
     * @return HRESULT
     */
    public boolean initialize(ETList<IElement> els, boolean processTest,
                            boolean processInit, boolean processPost)
    {
       boolean retVal = false;
       retVal = initializeClassLoader(els);
        m_IsProcessTest = processTest;
        m_IsProcessInit = processInit;
        m_IsProcessPost = processPost;
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#setProcessTest(boolean)
     */
    public void setProcessTest(boolean pVal)
    {
        m_IsProcessTest = pVal;
    }

    /**
     * Specifies whether or not to process the details of a combined 
     * fragment test section.  The OnBeginTest and OnEndTest events 
     * will still be sent.
     *
     * @param pVal [out] The new value.  True if the parser is to process.
     */
    public boolean isProcessTest()
    {
        return m_IsProcessTest;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#setProcessInit(boolean)
     */
    public void setProcessInit(boolean pVal)
    {
        m_IsProcessInit = pVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#isProcessInit()
     */
    public boolean isProcessInit()
    {
        return m_IsProcessInit;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#setProcessPost(boolean)
     */
    public void setProcessPost(boolean pVal)
    {
        m_IsProcessPost = pVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#isProcessPost()
     */
    public boolean isProcessPost()
    {
        return m_IsProcessPost;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#getClassLoader()
     */
    public IREClassLoader getClassLoader()
    {
        return m_ClassLoader;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#setClassLoader(org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IREClassLoader)
     */
    public void setClassLoader(IREClassLoader pVal)
    {
        m_ClassLoader = pVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#getOperation()
     */
    public IREOperation getOperation()
    {
        return m_Operation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IOpParserOptions#setOperation(org.netbeans.modules.uml.core.reverseengineering.reframework.IREOperation)
     */
    public void setOperation(IREOperation pVal)
    {
        m_Operation = pVal;
    }

    /**
     * Load the class loader with all the source files indicated by the input elements
     */
    protected boolean initializeClassLoader(ETList<IElement> inElements) {
        m_ClassLoader = new REClassLoader();
        
        ICompositeClassLocator cpCompositeLocator = new CompositeClassLocator();
        
        // Initialize the Project Class Locator
        addProjectLocators(inElements, cpCompositeLocator);
        
        //kris richards - OPRE_SearchDirectoriesFile pref expunged. Set to "".
        //kris richards - OPRE_ShowBaseDirDialog pref deleted.
        // therefore no condition necessary
        
        // Prepare the loader
        ETList<IElement> els = getAllSourceFileArtifacts(inElements);
        if (els != null) {
            for (int i = 0, count = els.size(); i < count; ++i) {
                IElement el = els.get(i);
                if (!(el instanceof ISourceFileArtifact)) continue;
                
                ISourceFileArtifact sf = (ISourceFileArtifact) el;
                
                // Get the associated language
                ILanguage lang = sf.getLanguage();
                if (lang != null) {
                    IStrings libraryNames = lang.getLibraryNames();
                    if (libraryNames != null) {
                        for (int j = 0, lnc = libraryNames.size(); j < lnc; ++j) {
                            String libraryName = libraryNames.get(j);
                            if (libraryName == null || libraryName.length() == 0)
                                continue;
                            
                            String def = lang.getLibraryDefinition(libraryName);
                            if (def != null && def.length() > 0)
                                addLookupLibrary(m_ClassLoader, def);
                        }
                    }
                }
            }
            
            m_ClassLoader.setClassLocator(cpCompositeLocator);
            setClassLoader(m_ClassLoader);
        }
        
        return false;
    }

   public void addProjectLocators(ETList<IElement> inElements, ICompositeClassLocator compLocator)
   {
      Hashtable<String, IProject> projMap = getProjects(inElements);
      
      IProjectClassLocator projLocator = new ProjectClassLocator();
      if (projMap != null)
      {
         Enumeration elems = projMap.elements();
         while (elems.hasMoreElements())
         {
            IProject proj = (IProject)elems.nextElement();
            projLocator.addProject(proj); 
         }
         compLocator.addLocator(projLocator);
      }
   }

   public Hashtable<String, IProject> getProjects(ETList<IElement> inElements)
   {
      Hashtable<String, IProject> retVal = new Hashtable<String, IProject>();
      if (inElements != null)
      {
         int count = inElements.size();
         
         // For each element in the collection add the project associated with the
         // element.  Now, I only want to add the project once.  So, first create
         // a map of all of the projects.  Then add these projects to the 
         // project class locator.
         for (int i=0; i<count; i++)
         {
            IElement elem = inElements.get(i);
            IProject proj = elem.getProject();
            if (proj != null)
            {
               String name = proj.getName();
               if (name != null && name.length() > 0)
               {
                  if (!retVal.containsKey(name))
                  {
                     retVal.put(name, proj);
                  }
               }
            }
         }
      }
      return retVal;
   }

    /**
     * Retreive all the source file artifact elements from all the operations
     */
    protected ETList<IElement> getAllSourceFileArtifacts(
            ETList<IElement> inElements)
    {
        ETList<IElement> sfs = new ETArrayList<IElement>();
        
        for (int i = 0, count = inElements.size(); i < count; ++i)
        {
            IElement el = inElements.get(i);
            if (el == null) continue;
            
            ETList<IElement> sels = el.getSourceFiles();
            if (sels != null)
                sfs.addAll(sels);
        }
        
        return sfs;
    }

    /**
     * Adds the a lookup library to the input class loader
     */
    protected void addLookupLibrary(IREClassLoader classLoader, 
                                    String definitionFile)
    {
        ILanguageLibrary lib = new LanguageLibrary();
        lib.setLookupFile(definitionFile + ".etd");
        lib.setIndex(definitionFile + ".index");
        
        classLoader.addLibrary(lib);
    }

    private boolean m_IsProcessTest = true;
    private boolean m_IsProcessInit = true;
    private boolean m_IsProcessPost = true;
    private IREClassLoader m_ClassLoader;
    private IREOperation   m_Operation;
}
