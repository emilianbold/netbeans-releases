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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class FileSystemClassLocator implements IFileSystemClassLocator
{
    private List<String> m_BaseDirectories = new ArrayList<String>();

    /**
     * The File System Locator will search a file system for a file that 
     * contains the file.
     *
     * @param value [in] The new base directory.
     */
    public void addBaseDirectory(String dir)
    {
        if (dir != null && (dir = dir.trim()).length() > 0 &&
                !m_BaseDirectories.contains(dir))
            m_BaseDirectories.add(dir);
    }

    /**
     * The File System Locator will search a file system for a file that 
     * contains the class.
     *
     * @param value [in] The new base directory.
     */
    public ETPairT<String,String> locateFileForClass(
            String pack, 
            String className,
            ETList<IDependencyEvent> depends)
    {
        String path = searchForFile("", className);
        
        // If failed to find the class then search in the package that contains the 
        // calling class.
        String fullName = null;
        if (path == null)
        {
            fullName   = pack;
            path       = searchForFile(fullName, className);
        }
        
        // If the path still has not been found then search the dependencies.
        if (path == null && depends != null && depends.size() > 0)
        {
            ETPairT<String,String> p = 
                searchDependencies(className, depends);
            path       = p.getParamOne();
            fullName   = p.getParamTwo();
        }
        
        if (path != null)
        {
            if (fullName != null)
                fullName += "::" + className;
            else
                fullName = className;
        }
        
        return new ETPairT<String,String>(fullName, path);
    }

    /**
     * Retrieve the full path to the specified file name.
     * 
     * @param filename [in] The short form of the file name.
     * @param fullpath [out] The full path to the specified file.
     *                       If the class is not located fullpath wil be an
     *                       empty string.
     */
    public String locateFile(String filename)
    {
        return searchForFile(filename);
    }
    
    /**
     * Searches for the class in the file system.  The specified name
     * needs to be the fully scoped name of the class.
     * 
     * @param  name [in] The name of the class.
     * @return The file name.
     */
    protected String searchForFile(String name)
    {
        // First remove all UML scope seperators. Replace them with directory
        // separators.
        String filename = StringUtilities.splice(name, "::", File.separator);
        
        String ret = checkBaseDirectories(filename + ".java");
        if (ret == null && 
                (ret = checkBaseDirectories(filename + ".cls")) == null)
            ret = checkBaseDirectories(filename + ".frm");
        return ret;
    }
    
    /**
     * Search the base directories to see if a file with the same name can be 
     * located.
     *
     * @param filename [in] The file to locate.
     *
     * @return The full path to the file or null.
     */
    protected String checkBaseDirectories(String filename)
    {
        for (int i = 0, count = m_BaseDirectories.size(); i < count; ++i)
        {
            String path = m_BaseDirectories.get(i);
            File f = new File(path, filename);
            if (f.exists())
            {
                try
                {
                    return f.getAbsoluteFile().getCanonicalPath();
                }
                catch (IOException e)
                {
                    // We can't canonicalize the path? Use what we have, then.
                    // This may mess up some of our file-handling code.
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }
    
    /**
     * Searches for the class with in the specified package.
     * 
     * @param package [in] The name of the package.
     * @param clsName [in] The name of the class.
     * @return The file name.
     */
    protected String searchForFile(String pack, String clsName)
    {
        if (clsName == null) return null;
        return searchForFile( pack != null && pack.length() > 0? 
                                        pack + "::" + clsName : clsName );
    }
    
    protected ETPairT<String,String> searchDependencies(String name,
                                        ETList<IDependencyEvent> depends)
    {
        String ret = null;
        String pack = null;
        for (int i = 0, count = depends.size(); i < count; ++i)
        {
            IDependencyEvent dep = depends.get(i);
            if (dep == null) continue;
            
            String depPack = dep.getSupplier();
            if (depPack != null && depPack.length() > 0)
            {
                // If the dependency is a class dependecy and it is the 
                // class that we are looking for then use that dependency;
                if (dep.getIsClassDependency())
                {
                    if (dep.isSameClass(name))
                    {
                        pack = depPack;
                        ret  = searchForFile(pack, "");
                        
                        // Now remove the name of the class.  I only need the package name.
                        int pos = pack.lastIndexOf("::");
                        if (pos != -1)
                            pack = pack.substring(0, pos);
                        break;
                    }
                }
                else
                {
                    pack = depPack;
                    ret  = searchForFile(pack, name);
                }
            }
        }
        return new ETPairT<String, String>(ret, pack);
    }
}