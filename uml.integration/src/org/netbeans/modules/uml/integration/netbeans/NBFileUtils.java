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

package org.netbeans.modules.uml.integration.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.project.AssociatedSourceProvider;


public class NBFileUtils
{
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.uml.integration");
    /**
     * Retrieves the FileSystem that contains the specified file or null if no
     * available FileSystem contains the file.  <p> The FileSystem is located by
     * finding the first FileSystem in the repository whose root directory is
     * the parent of the given file. This match is case-insensitive and <em>
     * will not work on operating systems with case-sensitive filenames</em>.
     *
     * @param filename The name of the file to retrieve a FileSystem for.
     * @return         The FileSystem that contains the file, or null if no
     *                 FileSystem was found for the file.
     */
    public static FileSystem getFileSystem(String filename)
    {
        return getFileSystem(filename, false);
    }
    
    /**
     * Retrieves the FileSystem that contains the specified file or null if no
     * available FileSystem contains the file.  <p> The FileSystem is located by
     * finding the first FileSystem in the repository whose root directory is
     * the parent of the given file. This match is case-insensitive and <em>
     * will not work on operating systems with case-sensitive filenames</em>.
     *
     * @param filename The name of the file to retrieve a FileSystem for.
     * @param exact    If <code>true</code>, find a FileSystem with the root as
     *                 the given filename.
     * @return         The FileSystem that contains the file, or null if no
     *                 FileSystem was found for the file.
     */
    public static FileSystem getFileSystem(String fileName, boolean exact)
    {
        if (fileName != null && fileName.length() > 0)
        {
            File rootFile = new File(fileName);
            String lfile = fileName.toLowerCase();
            Repository repos = Repository.getDefault();
            
            FileSystem[] fileSystems = repos.toArray();
            // Hunt through each file system, checking whether the file system
            // root contains this file.
            try
            {
                for (int i = 0; i < fileSystems.length; i++)
                {
                    File root = FileUtil.toFile(fileSystems[i].getRoot());
                    if (root == null)
                        continue;
                    if (exact)
                    {
                        Log.out("getFileSystem(" + fileName + "): trying "
                            + root);
                    }
                    if (exact ? root.equals(rootFile) : lfile.startsWith(root
                        .toString().toLowerCase()))
                        return fileSystems[i];
                    if (exact)
                    {
                        Log.out("getFileSystem(" + fileName + "): rejected "
                            + root);
                    }
                }
            }
            catch (NullPointerException ignored)
            {
            }
        }
        
        return null;
    }
    
    /**
     * finds a file object in a FileSystem.  Does not create the missing packages
     * and DataObjects.
     * @param root The FileSystem to search.
     * @param path The path to the FileObject.  Package are seperated by '/' or '\'
     * @return The FileObject or <code>null</code> if the object was not found.
     */
    public FileObject findFileObject(FileSystem root, String path)
    {
        Log.entry("Entering function NBFileUtils::findFileObject");
        
        StringTokenizer tokenizer = new StringTokenizer(path, File.separator);
        FileObject retVal = root.getRoot();
        while ((tokenizer.hasMoreTokens() == true) && (retVal != null))
        {
            String token = tokenizer.nextToken();
            retVal = retVal.getFileObject(token);
        }
        return retVal;
    }
    
    public static FileSystem getFS(String fileName)
    {
        if (fileName == null || fileName.length() <= 0)
            return null;
        String root = new File(fileName).getParent();
        FileObject fileobj = FileUtil.toFileObject(new File(root));
        try
        {
            return fileobj != null ? fileobj.getFileSystem() : null;
        }
        catch (FileStateInvalidException e)
        {
            logger.log(Level.WARNING, null, e);
        }
        return null;
    }
    
    /**
     * Creates a full package structure.  If the specified path contians a file name
     * (ie anything with an extension) the file will <i>not</i> be created.
     *  <p><b>Example</b>
     * <code>util.createPackageStructure(fs, "test/test1/TestFile.java"</code>
     * The packages <i>test</i> and <i>test1</i> will be created but TestFile.java
     * will not be created.
     * @param rootSystem The FileSystem used to create the packages.
     * @param path The path used as a guide to create the package structure.
     * @return The last package created.
     */
    public FileObject createPackageSturcture(FileSystem rootSystem, String path)
    {
        Log.entry("Entering function NBFileUtils::createPackageSturcture");
        
        FileObject retVal = null;
        FileObject curFO = null;
        
        try
        {
            retVal = rootSystem.getRoot();
            StringTokenizer tokenizer = new StringTokenizer(path,
                File.separator);
            while (tokenizer.hasMoreTokens() == true)
            {
                if (retVal != null)
                    retVal.refresh();
                
                String token = tokenizer.nextToken();
                curFO = retVal.getFileObject(token);
                if ((curFO == null) && (token.indexOf('.') < 0))
                {
                    curFO = retVal.createFolder(token);
                }
                else if (curFO == null)
                {
                    Log.entry("Entering function NBFileUtils::if");
                    
                    break;
                }
                retVal = curFO;
            }
        }
        catch (DataObjectNotFoundException doE)
        {
            logger.log(Level.WARNING, null, doE);
        }
        catch (Exception ioE)
        {
            logger.log(Level.WARNING, null, ioE);
        }
        
        return retVal;
    }
    
    public static String normalizeFile(String filename)
    {
        if(filename!=null && filename.length()>0)
            filename = filename.replace("/", File.separator ).trim();
        return filename;
    }
    
    /**
     * Searchs the NetBeans file systems for the specified file.  If the file
     * is not found then the failure will be displayed in the status bar.
     * @param filename The file to locate.
     * @return The NetBeans SourceElement or null if not found.
     * @deprecated Use findResource() instead.
     */
    /* NB60TBD
    public SourceElement findSourceFile(String filename)
    {
        Log.entry("Entering function NBFileUtils::findSourceFile");
        if(filename == null||filename.length()<=0)
            return null;
        filename = normalizeFile(filename);
        SourceElement retVal = null;
        FileObject fo = FileUtil.toFileObject(new File(filename));
//FIXME:    FileObject fo = findFileObject(filename);
        
        try
        {
            if (fo != null)
            {
                DataObject dObj = DataObject.find(fo);
                
                SourceCookie cookie = (SourceCookie) dObj
                    .getCookie(SourceCookie.class);
                if (cookie != null)
                {
                    retVal = cookie.getSource();
                }
            }
            else
            {
                // The file did not exist.  Therefore ask if the user wants to create
                // the file.
            }
        }
        catch (Exception E)
        {
            logger.log(Level.WARNING, null, E);
        }
        return retVal;
    }
    */


    public static JavaSource findResource(String filename)
    {
        Log.entry("Entering function NBFileUtils::findResource(String)");

        if(filename == null || filename.length() <= 0) return null;
        
        filename = normalizeFile(filename);
        JavaSource retVal = null;
        FileObject fo = FileUtil.toFileObject(new File(filename));
        if(fo!=null)
            retVal = JavaSource.forFileObject(fo);
        return retVal;
    }
    
   
    public static FileObject findFileObject(String fullPath)
    {
        Log.entry("Entering function NBFileUtils::findFileObject");
        
        File f = new File(fullPath);
        return FileUtil.toFileObject(f);
    }
    
    /**
     *  Find a DataFolder by searching NetBeans file systems.  The DataFolder will
     *  be created if one does not exist.
     *  @param fullPath The path to the folder.
     */
    public DataFolder findFolder(String fullPath)
    {
        Log.entry("Entering function NBFileUtils::findFolder");
        
        //FIXME:       DataFolder retVal =  DataFolder.findFolder(FileUtil.toFileObject(new File(fullPath)));
        DataFolder retVal = null;
        
        try
        {
            FileObject fo = findFileObject(fullPath);
            if (fo != null)
            {
                retVal = (DataFolder) DataFolder.find(fo);
            }
            else
            { // We have to create the file folder.
                FileSystem fs = getFileSystem(fullPath, false);
                if (fs != null)
                {
                    //String rootName = fs.getDisplayName();
                    File rootFile = FileUtil.toFile(fs.getRoot());
                    String rootName = rootFile.getAbsolutePath();
                    
                    String folderName = fullPath
                        .substring(rootName.length() + 1);
                    FileObject packageFO = createPackageSturcture(fs,
                        folderName);
                    retVal = DataFolder.findFolder(packageFO);
                }
            }
        }
        catch (DataObjectNotFoundException e)
        {
            logger.log(Level.WARNING, null, e);
            retVal = null;
        }
        
        return retVal;
    }
    
    public static FileObject findFileObject(String pack, String className,
        String extn)
    {
        
        if (className == null || className.trim().length() == 0)
            className = null;
        if (pack == null)
            pack = "";
        
        pack = pack.replace('.', '/').trim();
        if (pack.length() > 0)
            pack += "/";
        
        if (extn != null && className != null)
            className += "." + extn;
        
        String resourceName = pack;
        if (className != null)
            resourceName += className;
        FileObject obj = FileUtil.toFileObject(new File(resourceName));
        ClassPath cp = ClassPath.getClassPath(obj, ClassPath.SOURCE);
        
        if (cp != null)
        {
            return cp.findResource(resourceName);
        }
        return null;
    }
    
    /**
     * Determines whether this Java VM has permission to write to the given
     * directory.
     *
     * @param dir A <code>File</code> object for the directory concerned.
     * @return <code>true</code> iff 'dir' is non-null, represents a directory,
     *         and this VM has permission to create files in that directory.
     */
    public static boolean isWritable(File dir)
    {
        if ( dir == null)
        {
            return false;
        }         
        FileObject folderObj = FileUtil.toFileObject(dir);
        return (folderObj != null && folderObj.isFolder() && folderObj.canWrite());
    }
    
    /**
     *  Asserts that the given ClassInfo's filename is that of a writable file.
     * If the ClassInfo itself is null, or has a null filename, or is associated
     * with a non-writable file, this method throws an UnwritableFileException
     * and displays an error dialog to the user, otherwise the method returns
     * silently.
     *
     * @param clazz The ClassInfo referencing the class to be modified.
     * @throws UnwritableFileException if the file associated with the ClassInfo
     *                                 is not writable.
     */
    public static void checkWritable(ClassInfo clazz)
    throws UnwritableFileException
    {
        String filename = clazz == null ? null : clazz.getFilename();
//        clazz.getClassElement().getSourceFiles()
        try
        {
            File file = new File(filename);
            // Early exit; all's well.
            if (file.canWrite() || !file.exists())
                return;
            
            if (!JavaClassUtils.needToCheckOut(clazz))
            {
                NBUtils.showSpamlessErrCode("Errors.Roundtrip.ReadOnly",
                    new Object[] { filename },
                    "Errors.Roundtrip.ReadOnly.Title");
            }
            if (!file.canWrite())
                throw new UnwritableFileException(filename);
        }
        catch (Exception e)
        {
            throw new UnwritableFileException(filename);
        }
    }
    
    /**
     * Finds the ClassElement that represents the class symbol.  The method
     * will only operate on CLD_Class symbols.
     * @param sym The symbol used to find a ClassElement.
     * @deprecated Use findJavaClass() instead.
     */
    /* NB60TBD
    public ClassElement findClass(ClassInfo clazz)
    {
        Log.entry("Entering function NBFileUtils::findClassFromSymbol");
        
        if (clazz.getChangeType() != ClassInfo.DELETE)
            clazz.updateFilename(null);
        
        ClassElement retVal = null;
        if (clazz != null)
        {
            try
            {
                ClassInfo outerClass = clazz.getOuterClass();
                String className = JavaClassUtils.getInnerClassName(clazz
                    .getName());
                Identifier classNameID = Identifier.create(className);
                
                // If the symbol is a inner class then we have to start with the outer
                // class and search down to find the inner class of interest.
                // This is a perfect example of recursion.  Therefore, I will be
                // using recursion to accomplish the task.
                if (outerClass != null)
                {
                    ClassElement outerElement = findClass(outerClass);
                    if (outerElement != null)
                    {
                        retVal = outerElement.getClass(classNameID);
                    }
                }
                else
                {
                    SourceElement source = findSourceFile(clazz);
                    if (source != null)
                    {
                        retVal = source.getClass(classNameID);
                    }
                }
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, null, ex);
            }
        }
        return retVal;
    }
    */


    /**
     * Finds the Element that represents the class symbol.  The method
     * will only operate on CLD_Class symbols.
     * @param sym The symbol used to find a ClassElement.
     */
    public static ElementAndFile findJavaClass(ClassInfo clazz)
    {
        Log.entry("Entering function NBFileUtils::findJavaClass");
                
        ElementAndFile retVal = null;
        if (clazz != null)
        {
            try
            {
		JavaSource source = findResource(clazz);
		if (source != null)
                {
		    String fullName = clazz.getPackage();
		    if (fullName != null && fullName.length() > 0) {
			fullName += ".";
		    }
		    fullName += clazz.getName();
		    retVal = getTypeElement(source, fullName);
		}		
            }
            catch (Exception ex)
            {
                logger.log(Level.WARNING, null, ex);
            }
        }
        return retVal;
    }


    public static ElementAndFile getTypeElement(JavaSource resource, final String name)
    {
	final ElementAndFile[] retVal = new ElementAndFile[1];
	try {
	    resource.runUserActionTask(new CancellableTask<CompilationController>() {
		public void run(CompilationController cc) {
		    Elements els = cc.getElements();
		    TypeElement classFound = els.getTypeElement(name);
		    if (classFound != null) {
			retVal[0] = new ElementAndFile(ElementHandle.create(classFound),
						       cc.getFileObject());
			Iterator<? extends Element> iter = els.getAllMembers(classFound).iterator();
			while(iter.hasNext()) {
			    Element nxt = iter.next();
			}
		    }
		}
		public void cancel() {
		}
	    }, true);
	} catch (IOException ioex) {
            logger.log(Level.WARNING, null, ioex);		    
	}
	return retVal[0];
    }
    

    /**
     * Retrieve the source file that contains the implementation of the Describe
     * class symbol.  The class symbol must be a CLD_Class symbol.
     * @param sym The Describe symbol.
     * @return The source element or null if the file is not found.
     * @deprecated Use findResource() instead.
     */
    /* NB60TBD
    public SourceElement findSourceFile(ClassInfo clazz)
    {
        Log.entry("Entering function NBFileUtils::findSourceFileFromSymbol");
        
        if (clazz.getChangeType() != ClassInfo.DELETE)
            clazz.updateFilename(null);
        SourceElement retVal = null;
        try
        {
            if (clazz != null)
            {
                //String      fileName  = sym.getSourceFile(1, null);
                String fileName = clazz.getFilename();
                
                retVal = findSourceFile(fileName);
            }
        }
        catch (Exception E)
        {
            logger.log(Level.WARNING, null, E);
        }
        
        return retVal;
    }
    */    


    public static JavaSource findResource(ClassInfo clazz)
    {
        Log.entry("Entering function NBFileUtils::findResource");

        String fileName = clazz.getFilename();
        
	if (fileName == null || fileName.length() == 0) {
                clazz.updateFilename(null);
		fileName = clazz.getFilename();
        }
        
	ClassInfo outerClass = clazz.getOuterClass();
	if (outerClass != null) {
	    return findResource(outerClass);
	}
	
        JavaSource retVal = null;
        try
        {
            if (clazz != null)
                retVal = findResource(fileName);
        }

        catch (Exception ex)
        {
            logger.log(Level.WARNING, null, ex);
        }
        
        return retVal;
    }
    


    //    /**
    //     * get the Associated
    //     *
    //     */
    //    public void getAssSourceGroup(Project project,String pack)
    //    {
    //        AssociatedSourceProvider provider =
    //            (AssociatedSourceProvider)project.getLookup().lookup(AssociatedSourceProvider.class);
    //        SourceGroup[] groups = provider.getSourceGroups();
    //        for (int i = 0; i < groups.length; i++) {
    //            FileObject fileobj = groups[i].getRootFolder();
    //            fileobj.getFileObject();
    //        }
    //    }
    
    public static Project getAssociatedSourceProject(Project project)
    {
        AssociatedSourceProvider provider = (AssociatedSourceProvider) project
            .getLookup().lookup(AssociatedSourceProvider.class);
        Project proj = provider.getAssociatedSourceProject();
        return proj;
        
    }
    
    public static FileObject getPackageFileObject(ClassInfo clazz)
    {
        FileObject srcFileObject = getSrcDirFileObject(clazz);
        FileObject packFileObject = null;
        String pack = "";
        try
        {
            pack = clazz.getPackage();
            if (pack == null)
                pack = "";
            
            if (pack.length() == 0)
                return srcFileObject;
            
            
            else
            {
                pack = pack.replace('.', '/').trim();
                packFileObject = srcFileObject.getFileObject(pack);
                if (packFileObject == null)
                {
                    String allTokens = "";
                    StringTokenizer token = new StringTokenizer(pack,"/");
                    FileObject curFileObj = srcFileObject;
                    while(token.hasMoreTokens())
                    {
                        String curToken=token.nextToken();
                        allTokens = allTokens + "/" + curToken;
                        FileObject tmpFileObj = srcFileObject.getFileObject(allTokens);
                        if (tmpFileObj != null)
                            curFileObj = tmpFileObj;
                        if (tmpFileObj == null)
                        {
                            packFileObject = curFileObj.createFolder(curToken);
                            curFileObj = packFileObject;
                        }
                    }
                }
            }
        }
        catch (Exception aeE)
        {
            logger.log(Level.WARNING, null, aeE);
        }
        return packFileObject;
        
    }
    
    public static FileObject getExportPackageFileObject(ClassInfo clazz)
    {
        // FileObject srcFileObject = getSrcDirFileObject(clazz);
        FileObject srcFileObject = clazz.getExportSourceFolderFileObject();
        FileObject packFileObject = null;
        String pack = "";
        
        try
        {
            pack = clazz.getPackage();
            if (pack == null)
                pack = "";
            
            if (pack.length() == 0)
                return srcFileObject;
            
            
            else
            {
                pack = pack.replace('.', '/').trim();
                packFileObject = srcFileObject.getFileObject(pack);
                
                if (packFileObject == null)
                {
                    String allTokens = "";
                    StringTokenizer token = new StringTokenizer(pack,"/");
                    FileObject curFileObj = srcFileObject;
                
                    while(token.hasMoreTokens())
                    {
                        String curToken=token.nextToken();
                        allTokens = allTokens + "/" + curToken;
                        
                        FileObject tmpFileObj = 
                            srcFileObject.getFileObject(allTokens);
                    
                        if (tmpFileObj != null)
                            curFileObj = tmpFileObj;
                        
                        if (tmpFileObj == null)
                        {
                            packFileObject = curFileObj.createFolder(curToken);
                            curFileObj = packFileObject;
                        }
                    }
                }
            }
        }
        catch (Exception aeE)
        {
            logger.log(Level.WARNING, null, aeE);
        }
        return packFileObject;
        
    }
    
    /**
     * Creates a new file folder. If the name of the package is a fully qualified
     * name createPackage will insure that the entire package structure is present.
     * @param fo The parent of the package.
     * @param pName The name of the package.
     */
    protected FileObject createPackage(FileObject fo, String pName)
    {
        Log.entry("Entering function NBEventProcessor::createPackage");
        FileObject retVal = fo;
        StringTokenizer packages = new StringTokenizer(pName, ".");
        try
        {
            //        boolean isClearCase = isClearCaseFilesystem(retVal);
            while (packages.hasMoreTokens() == true)
            {
                String newPackage = packages.nextToken();
                
                //            if(isClearCase) {
                //                createFolderClearCaseHack(retVal, newPackage, isClearCase);
                //            } else {
                FileObject p = retVal.getFileObject(newPackage);
                retVal = (p == null || !p.isFolder()) ? retVal
                    .createFolder(newPackage) : p;
                //            }
            }
        }
        catch (Exception ioE)
        {
            logger.log(Level.WARNING, null, ioE);
        }
        
        return retVal;
    }
    
    /**
     * Retrive the source directory associated with the given
     * file
     *
     */
    public static FileObject getSrcDirFileObject(ClassInfo clazz)
    {
        String root = clazz.getProject().getBaseDirectory();
        FileObject fileobj = FileUtil.toFileObject(new File(root));
        DataObject dObj = null;
        
        try
        {
            dObj = DataObject.find(fileobj);
        }
        
        catch (DataObjectNotFoundException e)
        {
            logger.log(Level.WARNING, null, e);
        }
        
        Project currentProj = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        Project srcProject = getAssociatedSourceProject(currentProj);
        
        AssociatedSourceProvider provider = (AssociatedSourceProvider) currentProj
            .getLookup().lookup(AssociatedSourceProvider.class);
        
        SourceGroup[] groups = provider.getSourceGroups();
        int currentNumberOfMatchedPackages = 0;
        int numberOfMatchPackages = 0;
        String pack = clazz.getPackage();
        SourceGroup matchedgroup = groups[0];
        
        for (int i = 0; i < groups.length; i++)
        {
            numberOfMatchPackages = 
                getNumberOfPackagesThatMatch(groups[i], pack);
            
            if ( numberOfMatchPackages > currentNumberOfMatchedPackages)
            {
                currentNumberOfMatchedPackages = numberOfMatchPackages;
                matchedgroup = groups[i];
            }
        }
        
        FileObject fileO = matchedgroup.getRootFolder();
        return fileO;
    }
    
    private static int getNumberOfPackagesThatMatch(
        SourceGroup group, String pack)
    {
        int retVal = 0;
        FileObject root = group.getRootFolder();
        if (pack != null && pack.length() > 0)
        {
            StringTokenizer packages = new StringTokenizer(pack, ".");
            while (packages.hasMoreTokens() == true)
            {
                String newPackage = packages.nextToken();
                FileObject p = root.getFileObject(newPackage);
                if (p == null)
                    break;
                ++retVal;
                root = p;
            }
        }
        return retVal;
    }
    
    /**
     * Retrieve the name of the file that contains the class element.  The name
     * of the class is retrieved by concating the packages of the class and and
     * the file system that contains the file.
     * @param clazz The class used to retrieve the file name.
     */
    /* NB60TBD
    public static String getFilename(ClassElement clazz)
    {
        Log.entry("Entering function NBFileUtils::getFilename");
        String retVal = "";
        SourceElement source = clazz.getSource();
        DataObject dObj = (DataObject) source.getCookie(DataObject.class);
        FileObject fo = dObj.getPrimaryFile();
        File file = FileUtil.toFile(fo);
        if(file != null)
        {
            retVal = file.getAbsolutePath();
        }
        return retVal;
    }
    */
    
    /**
     *  Signals that a file that was expected to be writable was found read-only.
     *
     * @author  Darshan
     * @version 1.0
     */
    public static class UnwritableFileException extends RuntimeException
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        
        public UnwritableFileException(String file)
        {
            super("The file '" + file + "' is unwritable."); // NOI18N
        }
    }
    /* NB60TBD
    public static String getFilename(JavaClass clazz)
    {
        String retVal = null;
        if(clazz==null)
            return null;
        Resource resource = clazz.getResource();
        if(resource!=null)
        {
            FileObject fileObj = JavaModel.getFileObject(resource);
            if(fileObj!=null)
                retVal = FileUtil.toFile(fileObj).getPath();
            retVal = NBFileUtils.normalizeFile(retVal);
        }
        return retVal;
    }
    */
    
    public static String getSimpleName(String resource)
    {
        String retVal = resource;
        int index = resource.lastIndexOf(".");
        if(index>0)
            retVal = resource.substring(index+1);
        return retVal;
    }
    
    public static String getPackage(String resource)
    {
        String retVal = "";
        int index = resource.lastIndexOf(".");
        if(index>0)
            retVal = resource.substring(0,index);
        return retVal;
    }
    
    public static String getPackageForResource(String resource)
    {
        String retVal = "";
        int index = resource.lastIndexOf("/");
        if(index>0)
        {
            retVal = resource.substring(0,index);
            retVal = retVal.replace("/",".");
        }
        return retVal;
    }
    
    public static String getSimpleNameForResource(String resource)
    {
//        String retVal = resource;
        int index = resource.lastIndexOf("/");
        if(index!=-1)
            resource = resource.substring(index+1);
        int javaIndex = resource.indexOf(".java");
        resource = resource.substring(0,javaIndex);
        return resource;
    }
    
    /* NB60TBD
    public static String getFileNameForResource(Resource resource)
    {
        String retVal = null;
        if(resource==null)
            return null;
        JavaModelPackage pkg = (JavaModelPackage) resource.refImmediatePackage();
        Codebase cbs = (Codebase)pkg.getCodebase().refAllOfClass().iterator().next();
        String cpr = cbs.getName();
        retVal = cpr.substring(cpr.indexOf("/")+1,cpr.lastIndexOf("/")+1)+resource.getName();
        retVal = NBFileUtils.normalizeFile(retVal);
        retVal = retVal.replace("%20"," ");
        return retVal;
    }
    */
}
