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
    protected boolean initializeClassLoader(ETList<IElement> inElements)
    {
        m_ClassLoader = new REClassLoader();
        
        ICompositeClassLocator cpCompositeLocator = new CompositeClassLocator();
        
        // Initialize the Project Class Locator
        addProjectLocators(inElements, cpCompositeLocator);
      
        boolean bCanceled = addFileSystemLocator(inElements, cpCompositeLocator);
        
        if (!bCanceled)
        {
           // Prepare the loader
           ETList<IElement> els = getAllSourceFileArtifacts(inElements);
           if (els != null)
           {
               for (int i = 0, count = els.size(); i < count; ++i)
               {    
                   IElement el = els.get(i);
                   if (!(el instanceof ISourceFileArtifact)) continue;
                
                   ISourceFileArtifact sf = (ISourceFileArtifact) el;
                
                   // Get the associated language
                   ILanguage lang = sf.getLanguage();
                   if (lang != null)
                   {
                       IStrings libraryNames = lang.getLibraryNames();
                       if (libraryNames != null)
                       {
                           for (int j = 0, lnc = libraryNames.size(); j < lnc; ++j)
                           {
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
        }
        return bCanceled;
    }
    
    public boolean addFileSystemLocator(ETList<IElement> inElements, ICompositeClassLocator compLocator)
    {
       boolean bCanceled = false;
       
       Hashtable<String, IProject> projMap = getProjects(inElements);
       ETPairT<IStrings, Boolean> result = getBaseDirectories(projMap);
       IStrings baseDirs = result.getParamOne();
       bCanceled = result.getParamTwo().booleanValue();
       
       if (baseDirs != null)
       {
          IFileSystemClassLocator fileSystemLocator = new FileSystemClassLocator();
          int count = baseDirs.getCount();
          for (int i=0; i<count; i++)
          {
             String userDir = baseDirs.item(i);
             if (userDir != null && userDir.length() > 0)
             {
                fileSystemLocator.addBaseDirectory(userDir);
             }
          }
          compLocator.addLocator(fileSystemLocator);
       }
       
       return bCanceled;
    }

    public ETPairT<IStrings, Boolean> getBaseDirectories(Hashtable<String, IProject> projMap) {
        IStrings strs = null;
        boolean cancel = false;
        
        String settingFileName = getPreference("OPRE_SearchDirectoriesFile");
        IREOperationWizard wizard = new REOperationWizard();
        if (settingFileName != null && settingFileName.length() > 0 && (new File(settingFileName)).exists()) {
            wizard.addSettingsFile(settingFileName);
        } else {
            //Enumeration keys = projMap.keys();
            Enumeration elems = projMap.elements();
            while (elems.hasMoreElements()) {
                IProject proj = (IProject)elems.nextElement();
                if(proj != null) {
                    // If we ever want to support other languages like C++ we need
                    // to remove the dependency on Java Project constructs
//               Project umlProj = ProjectUtil.findNetBeansProjectForModel(proj);
                    Project umlProj=null;
                    
                    String filename = proj.getFileName();
                    if((filename != null) && (filename.length() > 0)) {
                        FileObject fo = FileUtil.toFileObject(new File(filename));
                        umlProj = FileOwnerQuery.getOwner(fo);
                    }
                    
                    if (umlProj!=null) {
                        IAssociatedProjectSourceRoots provider = (IAssociatedProjectSourceRoots)umlProj.getLookup().lookup(IAssociatedProjectSourceRoots.class);
                        if(provider != null) {
                            File[] roots = provider.getCompileDependencies();
                            //Kris Richards - hack. The roots comes back as null 
                            //when a fwd engineer is followed by a rev engineer and 
                            //the a seq diag rev eng is requested.
                            if (roots != null)
                                for(File root : roots) {
                                    String absName = root.getAbsolutePath();
                                    if(absName.endsWith("jar") == false) {
                                        wizard.addLookupDirectory(absName);
                                    }
                                }
                        }
                    }
                }
            }
        }
        
        cancel = wizard.display(null);
        strs = wizard.getBaseDirectories();
        
        return new ETPairT<IStrings, Boolean> (strs, Boolean.valueOf(cancel));
    }

   public String getPreference(String prefName)
   {
      String retVal = null;
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IPreferenceManager2 prefMan = prod.getPreferenceManager();
         if (prefMan != null)
         {
            retVal = prefMan.getPreferenceValue(
                "Default", 
                "ReverseEngineering|OperationElements|OPRE_SearchDirectoriesFile", 
                prefName);
         }
      }
      return retVal;
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
