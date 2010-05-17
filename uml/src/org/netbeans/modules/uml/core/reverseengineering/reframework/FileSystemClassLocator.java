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
