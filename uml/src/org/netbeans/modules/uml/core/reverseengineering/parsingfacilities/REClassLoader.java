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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IFileInformation;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ILanguageLibrary;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REClassLoader implements IREClassLoader
{

    /**
     * Loads a class into memory and returns the class details.  The class that is to be 
     * retrieved is specified by the fully qualified name of the class.
     *
     * @param className [in] The name of the class to load.
     * @param data [out] The class information.
     */
    public IREClass loadClass(String className)
    {
        if (className == null || className.length() == 0) return null;
        
        // LookupClass will return S_FALSE when it fails to find the 
        // class in the lookup table.  So, I want to try to locate the
        // file if a NON-ERROR hr result occurs.
        IREClass c = lookupClass(className);
        if (c == null)
        {
            if ((c = checkLibraries(className, null)) == null)
                c = searchLocators(null, className, null);
        }
        return c;
    }

    /**
     * Loads a class into memory and returns the class details.  If the class is not found 
     * the depend include path is searched.
     * 
     * @param className [in] The name of the class to load.
     * @param thisPtr [in] The class information that contains the dependency data.
     * @param data [out] The class information.
     */
    public IREClass loadClass(String className, IREClass thisC)
    {
        if (className == null || className.length() == 0) return null;
        
        // ---------------------------------------------------------------------
        // Notes
        // 1) Check the lookup table
        //    A) Cycle through all the dependencies until the class if found.
        // 2) Check the libraries for the class.
        //    A) Cycle through all the dependencies until the class if found.
        // 3) Check the file locators.
        //    A) pass the dependencies to the class locators so they will be
        //       able to check all the dependencies.
        // ---------------------------------------------------------------------
        IREClass c = lookupClass(className, thisC);
        if (c == null && (c = checkLibraries(className, thisC)) == null)
            c = checkLocators(className, thisC);
        return c;
    }

    /**
     * Retrieve all the classes that have been loaded by the class loader.
     * 
     * @param classes [out] A collection of all classes.
     */
    public ETList<IREClass> getLoadedClasses()
    {
        ETList<IREClass> cs = new ETArrayList<IREClass>();
        Iterator< Map.Entry< String, IREClass > > iter = 
                        m_ClassMap.entrySet().iterator();
        while (iter.hasNext())
        {
            IREClass c = iter.next().getValue();
            if (c != null)
                cs.add(c);
        }
        return cs;
    }

    /**
     * Load all classes in a source file and retrieve the details about a specific class declaration.
     * 
     * @param filename [in] The file to be loaded.
     * @param className [in] The name of the class to retrieve.
     * @param details [out] The classes contained in the file.
     */
    public IREClass loadClassFromFile(String fileName, String className)
    {
        if (fileName == null || className == null || fileName.length() == 0
                || className.length() == 0)
            return null;
        loadFile(fileName);
        return lookupClass(className);
    }

    /**
     * Load all the classes in a file and return the details about all classes contained in the file.
     * 
     * @param filename [in] The file to be loaded.
     * @param details [out] The classes contained in the file.
     */
    public ETList<IREClass> loadClassesFromFile(String fileName)
    {
        if (fileName == null || fileName.length() == 0)
            return null;
        loadFile(fileName);
        
        IFileInformation fi = getClassInformation(fileName);
        return retrieveClassesFromFileInfo(fi);
    }

    /**
     * Load all the classes in the source file.
     * 
     * @param filename [in] The file to load.
     */
    public void loadFile(String fileName)
    {
        if (fileName == null || fileName.length() == 0)
            return;
        IUMLParser parser = getParser();
        if (parser != null)
        {
            // I want to first register a listener to the UMLParser dispatcher.
            // Then parse the file and revoke the listener.
            // 
            // After the parser has completed The IFileInformation can be retrieved
            // from the IClassLoaderListener and added to our database.
            IUMLParserEventDispatcher disp = parser.getUMLParserDispatcher();
            if (disp != null)
            {
                IClassLoaderListener list = new ClassLoaderListener();
                disp.registerForUMLParserEvents(list, fileName);
                parser.processStreamFromFile(fileName);
                disp.revokeUMLParserSink(list);
                
                loadFileInformation(fileName, list);
            }
        }
    }

    public List < IDependencyEvent > getDependencies(IREClass context)
    {
        IFileInformation fileInfo = getClassInformation(context);
        
        if (fileInfo != null)
        return fileInfo.getDependencies();
        else
            return null ;
    }
    /**
     * Gets the class file locator for the class loader.
     * 
     * @param pVal [out] The class loader.
     */
    public IClassLocator getClassLocator()
    {
        return m_Locator;
    }

    /**
     * Sets the class file locator for the class loader.
     * 
     * @param pVal [in] The class loader.
     */
    public void setClassLocator(IClassLocator pVal)
    {
        m_Locator = pVal;
    }
    
    /**
     * Retrieves the parser for the file that is to be loaded.
     * 
     * @param filename [in] The name of the file.
     * @param parser [out] The parser or NULL if one was not found.
     */
    protected IUMLParser getParser()
    {
        return new LanguageFacilityFactory().getUMLParser();
    }
    
    /**
     * Add the file information and all of its classes to the class loader for quick lookup.
     * 
     * @param filename [in] The file that is being loaded.
     * @param listener [in] The listener that recieved the parser events.
     * @see #AddClassInformation(IFileInformation* pInfo)
     */
    protected void loadFileInformation(String filename, 
                                       IClassLoaderListener listener)
    {
        IFileInformation fi = listener.getFileInformation();
        if (fi != null)
        {
            m_FileInfoMap.put(filename, fi);
            addClassInformation(fi);
        }
    }
    
    /**
     * Add the class information to the class loader for quick look up later.
     * Each class (including inner classes) will be added collecctions.
     * 
     * @param pInfo [in] The file inforamtion that contains the class information.
     * @see #AddClassInformation(IREClass* clsInfo)
     */
    protected void addClassInformation(IFileInformation fi)
    {
        if (fi == null) return ;
        
        for (int i = 0, count = fi.getTotalClasses(); i < count; ++i)
            addClassInformation(fi.getClass(i));
    }
    
    /**
     * Adds the class information to the map that manages the class information
     * references.  All inner classes will also be managed as well.
     * 
     * @param clsInfo [in] The Class to manager.
     */
    protected void addClassInformation(IREClass c)
    {
        if (c == null) return ;
        String pack = c.getPackage();
        String name = c.getName();
        if (name != null && name.length() > 0)
        {    
            String fullname = pack != null && pack.length() > 0?
                                    pack + "::" + name : name;
            m_ClassMap.put(fullname, c);
        }
        
        // Now add the inner classes.
        ETList<IREClass> recl = c.getAllInnerClasses();
        if (recl != null)
        {
            for (int i = 0, count = recl.size(); i < count; ++i)
                addClassInformation(recl.get(i));
        }
    }
    
    /**
     * Retrieves all of the classes found in a specific source file.
     * @param pInfo [in] The file information.
     * @param result [out] The class found in the file.
     */
    protected ETList<IREClass> retrieveClassesFromFileInfo(IFileInformation fi)
    {
        if (fi == null)
            return null;
        ETList<IREClass> rec = new ETArrayList<IREClass>();
        for (int i = 0, count = fi.getTotalClasses(); i < count; ++i)
        {
            IREClass c = fi.getClass(i);
            if (c != null)
                rec.add(c);
        }
        return rec;
    }
    
    protected IFileInformation getClassInformation(String filename)
    {
        return m_FileInfoMap.get(filename);
    }
    
    /**
     * Retrieves the file information structure that contains the specified class.
     * 
     * @param data [in] The class information.
     * @param info [out] The information about the file that contains the class.
     */
    protected IFileInformation getClassInformation(IREClass c)
    {
        if (c == null) return null;
        return getClassInformation(c.getFilename());
    }
    
    // *********************************************************************
    // Class Lookup Mehtods
    // *********************************************************************

    /**
     * Checks the map of found classes to see if the specified class 
     * has been located.
     *
     * @param name [in] The name of the class.
     * @param pData [out] The class.
     */
    protected IREClass lookupClass(String name)
    {
        if (name == null || name.length() == 0)
            return null;
        
        return m_ClassMap.get(name);
    }
    
    /**
     * Checks the map of found classes to see if the specified class 
     * has been located.  The import statments are also searched when
     * try to find the class.
     *
     * @param name [in] The name of the class.
     * @param pThisPtr [in] The <i>this</i> class used as a refernece class.
     * @param pData [out] The class.
     */
    protected IREClass lookupClass(String name, IREClass thisC)
    {
        IREClass c = lookupClass(name);
        if (c == null && (c = lookupInnerClass(name, thisC)) == null 
                && (c = lookupInPackage(name, thisC)) == null)
            c = lookupInDependencies(name, thisC);
        return c;
    }
    
    protected IREClass lookupInnerClass(String name, IREClass thisC)
    {
    	if (name == null || name.length() == 0 || thisC == null)
            return null;
        
        IREClass data = null;
        String className = thisC.getName();
        if (className != null && className.length() > 0)
        {
            String fullname = className + "::" + name;
            data = lookupClass(fullname);
        }
        
        if (data == null)
        {
        	ETList<IREClass> innerClasses = thisC.getAllInnerClasses();
            if (innerClasses != null)
            {
                for (int i = 0, count = innerClasses.size(); 
                            i < count && data == null; ++i)
                {
                    IREClass curClass = innerClasses.get(i);
                    if (curClass != null)
                        data = lookupInnerClass(name, curClass);
                }
            }
        }
        return data;
    }
    
    /**
     * Checks if the desired class has been found in the same package as the <i>this</i>
     * class.  LookupInPacakge will only check the lookup table.  LookupInPacakge 
     * does not check the file locator.
     *
     * @param name [in] The name of the class.
     * @param pThisPtr [in] The <i>this</i> class used as a refernece class.
     * @param pData [out] The class.
     */
    protected IREClass lookupInPackage(String name, IREClass thisC)
    {
        if (thisC == null) 
            return null;
        
        String pack = thisC.getPackage();
        if (pack != null && pack.length() > 0)
        {
            pack += "::" + name;
            return lookupClass(pack);
        }
        return null;
    }
    
    /**
     * Checks if the class exist in the same package as the this class.
     *
     * @param name [in] The name of the class.
     * @param pThisPtr [in] The <i>this</i> class used as a refernece class.
     * @param pData [out] The class.
     */
    protected IREClass lookupInDependencies(String name, IREClass thisC)
    {
        if (thisC == null)
            return null;
        
        IREClass data = null;
        IFileInformation fi = getClassInformation(thisC);
        if (fi != null)
        {    
            for (int i = 0, count = fi.getTotalDependencies(); 
                    i < count && data == null; ++i)
            {
                IDependencyEvent dep = fi.getDependency(i);
                if (dep == null) continue;
                
                String sup = dep.getSupplier();
                boolean isClassDep = dep.getIsClassDependency();
                if (sup != null && sup.length() > 0)
                {
                    if (isClassDep)
                    {
                        if (dep.isSameClass(name))
                            data = lookupClass(sup);
                    }
                    else
                    {
                        sup += "::" + name;
                        data = lookupClass(sup);
                    }
                }
            }
        }
        return data;
    }
    
    /**
     * Adds a new library to the classloader.  The library will be used to find out the
     * details about a class the is in a library.
     * 
     * @param newVal [in] The new library.
     */
    public void addLibrary(ILanguageLibrary lib)
    {
        if (lib == null) return ;
        m_Libraries.add(lib);
    }
    
    /**
     * Checks if any of the libraries contain a class that mathes the class name.  All of
     * the dependenies will be checked.
     * 
     * @param name [in] The name of the class.
     * @param pThisPtr [in] The <i>this</i> class used as a refernece class.
     * @param pData [out] The class.
     */
    protected IREClass checkLibraries(String className, IREClass thisC)
    {
        // Get the dependency information.  If We are not able to 
        // get the dependency information we can still check the className
        // that was passed in.
        IFileInformation fi = getClassInformation(thisC);
        int maxdeps = fi != null? fi.getTotalDependencies() : 0;
        
        IREClass c = null;
        for (int i = 0, count = m_Libraries.size(); i < count; ++i)
        {
            ILanguageLibrary curlib = m_Libraries.get(i);
            c = curlib.findClass(className);
            if (c == null && maxdeps > 0)
            {
                // Cycle the dependencies and query the current library for the 
                // fully scoped class.  If we find the class then we are done.  otherwise
                // get the next library and check the dependencies again.  Since libraries
                // can only be referenced if there are dependencies to them I am only checking
                // the dependencies.  I can not fore see a reason to check the default package
                // or the package that contains the pThisPtr.
                for (int j = 0; j < maxdeps && c == null; j++)
                {
                    IDependencyEvent dep = fi.getDependency(j);
                    String supplier = dep.getSupplier();
                    if (supplier != null && supplier.length() > 0)
                    {
                        if (dep.getIsClassDependency())
                        {
                            if (dep.isSameClass(className))
                                c = curlib.findClass(supplier);
                        }
                        else
                        {    
                            supplier += "::" + className;
                            c = curlib.findClass(supplier);
                        }
                    }
                }
            }
        }
        
        addClassInformation(c);
        return c;
    }
    
    /**
     * Search the file locators for the class definition.  The class locators will check all of the
     * dependencids to find the class.
     *
     * @param packageName [in] The package that contains the calling class.
     * @param clasName [in] The name of the class to find.
     * @param pDenpends [in] The dependencies to search.
     * @param pData [out] The class definition.
     */
    protected IREClass searchLocators(String pack, String className,
                                      ETList<IDependencyEvent> deps)
    {
        IREClass c = null;
        
        IClassLocator loc = getClassLocator();
        if (loc != null)
        {
            ETPairT<String,String> p = 
                        loc.locateFileForClass(pack, className, deps);
            String fullname = p.getParamOne(),
                   filename = p.getParamTwo();
            if (filename != null && filename.length() > 0)
                addClassInformation(c = loadClassFromFile(filename, fullname));
        }
        return c;
    }
    
    /**
     * Checks if any of the file locators are able to find the class.  All of the dependenies are
     * checked.
     * 
     * @param name [in] The name of the class.
     * @param pThisPtr [in] The <i>this</i> class used as a refernece class.
     * @param pData [out] The class.
     */
    protected IREClass checkLocators(String className, IREClass thisC)
    {
        if (thisC == null) return null;

        IREClass c = null;
        IFileInformation fi = getClassInformation(thisC);
        if (fi != null)
        {
            // Even if there are no dependencies I still want to search for 
            // the class.  The class may still reside in the same package as the
            // "this" pointer or the class name may be the fully scoped name.
            ETList<IDependencyEvent> deps = fi.getDependencies();
            String pack = "";
            if (thisC != null)
                pack = thisC.getPackage();

            c = searchLocators(pack, className, deps);
            if (c == null)
                c = lookupClass(className, thisC);
        }
        return c;
    }

    /**
     * Retrieves the errors that where found when parsing the specified file.
     * 
     * @param filename [in] The name of the file.
     */
    public ETList<IErrorEvent> getErrorInFile(String filename)
    {
        IFileInformation fi = getClassInformation(filename);
        return fi != null? fi.getErrors() : null;
    }

    // AZTEC: We may want to make this a TreeMap and define a case-insensitive
    //        comparator to handle Windows filenames naturally.
    private static class FileInformationMap 
                extends HashMap<String, IFileInformation> 
    { 
    }
    
    private static class ClassMap 
                extends TreeMap<String, IREClass> 
    {
    }
    
    private static class LibraryList
                extends ETArrayList<ILanguageLibrary>
    {
    }
    
    private IClassLocator       m_Locator;
    private FileInformationMap  m_FileInfoMap = new FileInformationMap();
    private ClassMap            m_ClassMap    = new ClassMap();
    private LibraryList         m_Libraries   = new LibraryList();
}
