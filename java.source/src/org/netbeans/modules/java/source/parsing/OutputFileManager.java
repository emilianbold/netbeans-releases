/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.util.Iterators;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileManager extends CachingFileManager {

    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    private static final String OUTPUT_ROOT = "output-root";   //NOI18N
    /**
     * Exception used to signal that the sourcepath is broken (project is deleted)
     */
    public class InvalidSourcePath extends IllegalStateException {
    }

    private ClassPath scp;
    private ClassPath apt;
    private final Set<File> filteredFiles = new HashSet<File>();
    private boolean filtered;
    private String outputRoot;
    private URL explicitSibling;

    /** Creates a new instance of CachingFileManager */
    public OutputFileManager(final CachingArchiveProvider provider,
            final ClassPath outputClassPath,
            final ClassPath sourcePath,
            final ClassPath aptPath) {
        super (provider, outputClassPath, false, true);
        assert outputClassPath != null;
        assert sourcePath != null;
	this.scp = sourcePath;
        this.apt = aptPath == null ? EMPTY_PATH : aptPath;
    }

    public final boolean isFiltered () {
        return this.filtered;
    }

    public final synchronized void setFilteredFiles (final Set<File> files) {
        assert files != null;
        this.filteredFiles.clear();
        this.filteredFiles.addAll(files);
        this.filtered = true;
    }

    public final synchronized void clearFilteredFiles () {
        this.filteredFiles.clear();
        this.filtered = false;
    }

    @Override
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<Kind> kinds, boolean recursive) {
        Iterable sr =  super.list(l, packageName, kinds, recursive);
        if (this.filteredFiles.isEmpty()) {
            return sr;
        }
        else {
            Iterable<JavaFileObject> res = Iterators.filter (sr,new Comparable<JavaFileObject>() {
                public int compareTo(JavaFileObject o) {
                    File f = ((FileObjects.FileBase)o).f;
                    return filteredFiles.contains(f) ? 0 : -1;
                }
            });
            return res;
        }
    }

    public @Override JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {


        if (kind != JavaFileObject.Kind.CLASS) {
            throw new IllegalArgumentException ();
        }
        else {
            File activeRoot = null;
            if (outputRoot != null) {
                activeRoot = new File(outputRoot);
            } else {
                final String baseName = FileObjects.convertPackage2Folder(className);
                activeRoot = getRootForApt(sibling, baseName);
                if (activeRoot == null) {
                    int index = getActiveRoot(sibling, baseName);
                    if (index == -1) {
                        //Deleted project
                        throw new InvalidSourcePath ();
                    }
                    if (index < 0) {
                        //Deleted project or source path changed during the scan, log & ignore it
                        Logger.getLogger(OutputFileManager.class.getName()).warning(
                            "No output for class: " + className +" sibling: " + sibling +" srcRoots: " + this.scp + " cacheRoots: "  + this.cp);    //NOI18N
                        throw new InvalidSourcePath ();
                    }
                    assert index < this.cp.entries().size() : "index "+ index +" class: " + className +" sibling: " + sibling +" srcRoots: " + this.scp + " cacheRoots: " + this.cp;
                    activeRoot = new File (URI.create(this.cp.entries().get(index).getURL().toExternalForm()));
                }
            }
            String baseName = className.replace('.', File.separatorChar);       //NOI18N
            String nameStr = baseName + '.' + FileObjects.SIG;
            int nameComponentIndex = nameStr.lastIndexOf(File.separatorChar);
            if (nameComponentIndex != -1) {
                String pathComponent = nameStr.substring(0, nameComponentIndex);
                new File (activeRoot, pathComponent).mkdirs();
            }
            else {
                activeRoot.mkdirs();
            }
            File f = FileUtil.normalizeFile(new File (activeRoot, nameStr));
            return FileObjects.fileFileObject(f, activeRoot, null, null);
        }
    }

    public @Override javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        assert pkgName != null;
        assert relativeName != null;
        URL siblingURL = explicitSibling != null ? explicitSibling : sibling == null ? null : sibling.toUri().toURL();
        if (siblingURL == null) {
            throw new IllegalArgumentException ("sibling == null");
        }
        final int index = getActiveRootImpl (siblingURL);
        if (index == -1) {
            //Deleted project
            throw new InvalidSourcePath ();
        }
        assert index >= 0 && index < this.cp.entries().size();
        File activeRoot = new File (URI.create(this.cp.entries().get(index).getURL().toExternalForm()));
        if (File.separatorChar != '/') {    //NOI18N
            relativeName = relativeName.replace('/', File.separatorChar);   //NOI18N
        }
        final StringBuilder  path = new StringBuilder();
        if (pkgName.length() > 0) {
            path.append(FileObjects.convertPackage2Folder(pkgName, File.separatorChar));
            path.append(File.separatorChar);
        }
        path.append(relativeName);
        final File file = FileUtil.normalizeFile(new File (activeRoot,path.toString()));
        return FileObjects.fileFileObject(file, activeRoot,null,null);
    }


    private int getActiveRoot (final javax.tools.FileObject sibling, final String baseName) throws IOException {
        return sibling == null ? getActiveRootImpl(baseName) : getActiveRootImpl(sibling.toUri().toURL());
    }

    private int getActiveRootImpl (final URL sibling) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if ( eSize == 1) {
            return 0;
        }
        if (eSize == 0) {
            return -1;
        }
        Iterator<ClassPath.Entry> it = entries.iterator();
        //Logging for issue #151416
        try {
            for (int i = 0; it.hasNext(); i++) {
                URL rootUrl = it.next().getURL();
                if (FileObjects.isParentOf(rootUrl, sibling)) {
                    return i;
                }
            }
        } catch (IllegalArgumentException e) {
            //Logging for issue #151416
            String message = String.format("uri: %s", sibling.toString());
            throw Exceptions.attachMessage(e, message);
        }
        return -2;
    }

    private int getActiveRootImpl (String baseName) {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if (eSize == 1) {
            return 0;
        }
        if (eSize == 0) {
            return -1;
        }
        final String[] parentName = splitParentName(baseName);
        Iterator<ClassPath.Entry> it = entries.iterator();
        for (int i=0; it.hasNext(); i++) {
            FileObject root = it.next().getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        return i;
                    }
                }
            }
        }
	return -2;
    }

    private File getRootForApt(final javax.tools.FileObject sibling, final String baseName) {
        return sibling == null ? getRootForAptImpl(baseName) : getRootForAptImpl(sibling);
    }

    private File getRootForAptImpl(final javax.tools.FileObject sibling) {
        try {
            final URL surl = sibling.toUri().toURL();
            for (ClassPath.Entry entry : apt.entries()) {
                if (FileObjects.isParentOf(entry.getURL(), surl)) {
                    final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                    if (classFolder != null) {
                        try {
                            return new File(classFolder.toURI());
                        } catch (URISyntaxException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private File getRootForAptImpl(final String baseName) {
        String[] parentName = splitParentName(baseName);
        for (ClassPath.Entry entry : this.scp.entries()) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                        if (classFolder != null) {
                            try {
                                return new File(classFolder.toURI());
                            } catch (URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }
	return null;
    }

    private String[] splitParentName(final String baseName) {
        String name, parent = null;
	int index = baseName.lastIndexOf('/');              //NOI18N
	if (index<0) {
            parent = "";
            name = baseName;
	}
	else {
            parent = baseName.substring(0, index);
            name = baseName.substring(index+1);
	}
        index = name.indexOf('$');                          //NOI18N
	if (index > 0) {
	    name = name.substring(0,index);
	}
        return new String[] {parent, name};
    }

    @Override
    public boolean handleOption(String head, Iterator<String> tail) {
        if (OUTPUT_ROOT.equals(head)) { //NOI18N
            if (!tail.hasNext())
                throw new IllegalArgumentException();
            outputRoot = tail.next();
            if (outputRoot.length() <= 0)
                outputRoot = null;
            return true;
        } if (AptSourceFileManager.ORIGIN_FILE.equals(head)) {
            if (!tail.hasNext()) {
                throw new IllegalArgumentException("The apt-origin requires folder.");    //NOI18N
            }
            final String aptOrigin = tail.next();
            if (aptOrigin.length() == 0) {
                    explicitSibling = null;
            }
            else {
                try {
                    explicitSibling = new URL(aptOrigin);
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException("Invalid path argument: " + aptOrigin);    //NOI18N
                }
            }
            return false;   //Pass the option to all FileManagers
        }
        else {
            return super.handleOption(head, tail);
        }
    }
}
