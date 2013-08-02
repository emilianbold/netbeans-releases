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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.AptCacheForSourceQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class OutputFileManager extends CachingFileManager {

    private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
    private static final String OUTPUT_ROOT = "output-root";   //NOI18N
    private static final Logger LOG = Logger.getLogger(OutputFileManager.class.getName());
    /**
     * Exception used to signal that the sourcepath is broken (project is deleted)
     */
    public class InvalidSourcePath extends IllegalStateException {
    }

    private ClassPath scp;
    private ClassPath apt;
    private String outputRoot;
    private final SiblingProvider siblings;
    private final FileManagerTransaction tx;

    /** Creates a new instance of CachingFileManager */
    public OutputFileManager(
            @NonNull final CachingArchiveProvider provider,
            @NonNull final ClassPath outputClassPath,
            @NonNull final ClassPath sourcePath,
            @NullAllowed final ClassPath aptPath,
            @NonNull final SiblingProvider siblings,
            @NonNull final FileManagerTransaction tx) {
        super (provider, outputClassPath, false, true);
        assert outputClassPath != null;
        assert sourcePath != null;
        assert siblings != null;
        assert tx != null;
	this.scp = sourcePath;
        this.apt = aptPath == null ? EMPTY_PATH : aptPath;
        this.siblings = siblings;
        this.tx = tx;
    }

    @Override
    public Iterable<JavaFileObject> list(Location l, String packageName, Set<Kind> kinds, boolean recursive) {
        final Iterable<JavaFileObject> sr =  super.list(l, packageName, kinds, recursive);
        return tx.filter(l, packageName, sr);
    }

    public @Override JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (kind != JavaFileObject.Kind.CLASS) {
            throw new IllegalArgumentException ();
        } else {
            File activeRoot = null;
            if (outputRoot != null) {
                activeRoot = new File(outputRoot);
            } else {
                final String baseName = FileObjects.convertPackage2Folder(className);
                activeRoot = getClassFolderForSource(sibling, baseName);
                if (activeRoot == null) {
                    activeRoot = getClassFolderForApt(sibling, baseName);
                    if (activeRoot == null) {
                        //Deleted project
                        if (this.scp.getRoots().length > 0) {
                            LOG.log(
                                Level.WARNING,
                                "No output for class: {0} sibling: {1} srcRoots: {2}",    //NOI18N
                                new Object[]{
                                    className,
                                    sibling,
                                    this.scp
                                });
                        }
                        throw new InvalidSourcePath ();
                    }
                }
            }
            String baseName = className.replace('.', File.separatorChar);       //NOI18N
            String nameStr = baseName + '.' + FileObjects.SIG;            
            final File f = new File (activeRoot, nameStr);
            if (isValidClassName(className)) {
                return tx.createFileObject(l, f, activeRoot, null, null);
            } else {
                LOG.log(
                    Level.WARNING,
                    "Invalid class name: {0} sibling: {1}", //NOI18N
                    new Object[]{
                        className,
                        sibling
                    });
                return FileObjects.nullWriteFileObject(FileObjects.fileFileObject(f, activeRoot, null, null));
            }
        }
    }

    public @Override javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        assert pkgName != null;
        assert relativeName != null;
        URL siblingURL = siblings.hasSibling() ? siblings.getSibling() : sibling == null ? null : sibling.toUri().toURL();
        if (siblingURL == null) {
            throw new IllegalArgumentException ("sibling == null");
        }
        File activeRoot = getClassFolderForSourceImpl (siblingURL);
        if (activeRoot == null) {
            activeRoot = getClassFolderForApt(siblingURL);
            if (activeRoot == null) {
                //Deleted project
                throw new InvalidSourcePath ();
            }
        }
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
        return tx.createFileObject(l, file, activeRoot,null,null);
    }

    
    @Override
    public javax.tools.FileObject getFileForInput(Location l, String pkgName, String relativeName) {
        javax.tools.FileObject fo = tx.readFileObject(l, pkgName, relativeName);
        if (fo != null) {
            return fo;
        }
        return super.getFileForInput(l, pkgName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location l, String className, Kind kind) {
        if (kind == JavaFileObject.Kind.CLASS) {
            int dot = className.lastIndexOf('.');
            String dir = dot == -1 ? "" : FileObjects.convertPackage2Folder(className.substring(0, dot));
            javax.tools.FileObject fo = tx.readFileObject(l, dir, className.substring(dot + 1));
            if (fo != null) {
                return (JavaFileObject)fo;
            }
        }
        return super.getJavaFileForInput(l, className, kind);
    }

    /**
     * Prevents <error>, <any> from being generated.
     * @param fqn to check
     * @return true if the name does not contain '<'
     */
    private static boolean isValidClassName(@NonNull final String fqn) {
        int ld = fqn.lastIndexOf('.');      //NOI18N
        return fqn.indexOf('<', ld) < 0;    //NOI18N
    }

    private File getClassFolderForSource (final javax.tools.FileObject sibling, final String baseName) throws IOException {
        return sibling == null ? getClassFolderForSourceImpl(baseName) : getClassFolderForSourceImpl(sibling.toUri().toURL());
    }

    private File getClassFolderForSourceImpl (final URL sibling) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if ( eSize == 1) {
            return getClassFolder(entries.get(0).getURL());
        }
        if (eSize == 0) {
            return null;
        }
        try {
            for (ClassPath.Entry entry : entries) {
                URL rootUrl = entry.getURL();
                if (FileObjects.isParentOf(rootUrl, sibling)) {
                    return getClassFolder(rootUrl);
                }
            }
        } catch (IllegalArgumentException e) {
            //Logging for issue #151416
            String message = String.format("uri: %s", sibling.toString());
            throw Exceptions.attachMessage(e, message);
        }
        return null;
    }

    private File getClassFolderForSourceImpl (String baseName) throws IOException {
        List<ClassPath.Entry> entries = this.scp.entries();
        int eSize = entries.size();
        if (eSize == 1) {
            return getClassFolder(entries.get(0).getURL());
        }
        if (eSize == 0) {
            return null;
        }
        final String[] parentName = splitParentName(baseName);
        for (ClassPath.Entry entry : entries) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        return getClassFolder(entry.getURL());
                    }
                }
            }
        }
	return null;
    }
        
    private File getClassFolderForApt(final javax.tools.FileObject sibling, final String baseName) throws IOException {
        return sibling == null ? getClassFolderForApt(baseName) : getClassFolderForApt(sibling.toUri().toURL());
    }

    private File getClassFolderForApt(final @NonNull URL surl) {
        for (ClassPath.Entry entry : apt.entries()) {
            if (FileObjects.isParentOf(entry.getURL(), surl)) {
                final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                if (classFolder != null) {
                    try {
                        return Utilities.toFile(classFolder.toURI());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return null;
    }

    private File getClassFolderForApt(final String baseName) {
        String[] parentName = splitParentName(baseName);
        for (ClassPath.Entry entry : this.apt.entries()) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parentFile = root.getFileObject(parentName[0]);
                if (parentFile != null) {
                    if (parentFile.getFileObject(parentName[1], FileObjects.JAVA) != null) {
                        final URL classFolder = AptCacheForSourceQuery.getClassFolder(entry.getURL());
                        if (classFolder != null) {
                            try {
                                return Utilities.toFile(classFolder.toURI());
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
    
    private File getClassFolder(final URL url) throws IOException {
        final File result = JavaIndex.getClassFolder(url, false);
        assert result != null : "No class folder for source root: " + url;
        return result;
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
        } else {
            return super.handleOption(head, tail);
        }
    }
}
