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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class SourceFileManager implements JavaFileManager {
    
    private final ClassPath sourceRoots;
    private final boolean ignoreExcludes;
    private static Logger log = Logger.getLogger(SourceFileManager.class.getName());
    
    /** Creates a new instance of SourceFileManager */
    public SourceFileManager (final ClassPath sourceRoots, final boolean ignoreExcludes) {
        this.sourceRoots = sourceRoots;
        this.ignoreExcludes = ignoreExcludes;
    }

    public List<JavaFileObject> list(final Location l, final String packageName, final Set<JavaFileObject.Kind> kinds, final boolean recursive) {
        //Todo: Caching of results, needs listening on FS
        List<JavaFileObject> result = new ArrayList<JavaFileObject> ();
        String _name = packageName.replace('.','/');    //NOI18N
        if (_name.length() != 0) {
            _name+='/';                                 //NOI18N
        }
        for (ClassPath.Entry entry : this.sourceRoots.entries()) {
            if (ignoreExcludes || entry.includes(_name)) {
                FileObject root = entry.getRoot();
                if (root != null) {
                    FileObject tmpFile = root.getFileObject(_name);
                    if (tmpFile != null && tmpFile.isFolder()) {
                        Enumeration<? extends FileObject> files = tmpFile.getChildren (recursive);
                        while (files.hasMoreElements()) {
                            FileObject file = files.nextElement();
                            if (ignoreExcludes || entry.includes(file)) {
                                JavaFileObject.Kind kind;
                                final String ext = file.getExt();
                                if (FileObjects.JAVA.equalsIgnoreCase(ext)) {
                                    kind = JavaFileObject.Kind.SOURCE;
                                }
                                else if (FileObjects.CLASS.equalsIgnoreCase(ext) || FileObjects.SIG.equalsIgnoreCase(ext)) {
                                    kind = JavaFileObject.Kind.CLASS;
                                }
                                else if (FileObjects.HTML.equalsIgnoreCase(ext)) {
                                    kind = JavaFileObject.Kind.HTML;
                                }
                                else {
                                    kind = JavaFileObject.Kind.OTHER;
                                }
                                if (kinds.contains(kind)) {                        
                                    result.add (SourceFileObject.create(file, root));
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public javax.tools.FileObject getFileForInput (final Location l, final String pkgName, final String relativeName) {
        final String rp = FileObjects.getRelativePath (pkgName, relativeName);
        final FileObject[] fileRootPair = findFile(rp);
        return fileRootPair == null ? null : SourceFileObject.create (fileRootPair[0], fileRootPair[1]);
    }

    public JavaFileObject getJavaFileForInput (Location l, final String className, JavaFileObject.Kind kind) {
        String[] namePair = FileObjects.getParentRelativePathAndName (className);
        if (namePair == null) {
            return null;
        }
        String ext = kind == JavaFileObject.Kind.CLASS ? FileObjects.SIG : kind.extension.substring(1);   //tzezula: Clearly wrong in compile on save, but "class" is also wrong
        for (ClassPath.Entry entry : this.sourceRoots.entries()) {
            FileObject root = entry.getRoot();
            if (root != null) {
                FileObject parent = root.getFileObject(namePair[0]);
                if (parent != null) {
                    FileObject[] children = parent.getChildren();
                    for (FileObject child : children) {
                        if (namePair[1].equals(child.getName()) && ext.equalsIgnoreCase(child.getExt()) && (ignoreExcludes || entry.includes(child))) {
                            return SourceFileObject.create (child, root);
                        }
                    }
                }
            }
        }
        return null;
    }

    public javax.tools.FileObject getFileForOutput(final Location l, final String pkgName, final String relativeName, final javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (StandardLocation.SOURCE_PATH != l) {
            throw new UnsupportedOperationException("Only StandardLocation.SOURCE_PATH is supported."); // NOI18N
        }
        final String rp = FileObjects.getRelativePath (pkgName, relativeName);
        final FileObject[] fileRootPair = findFile(rp);
        if (fileRootPair == null) {
            final FileObject[] roots = this.sourceRoots.getRoots();
            if (roots.length == 0) {
                return null;
            }
            final File rootFile = FileUtil.toFile(roots[0]);
            if (rootFile == null) {
                return null;
            }
            return FileObjects.nbFileObject(new File(rootFile,FileObjects.convertFolder2Package(rp, File.separatorChar)).toURI().toURL(), roots[0]); //Todo: wrap to protect from write
        }
        else {
            return SourceFileObject.create (fileRootPair[0], fileRootPair[1]); //Todo: wrap to protect from write
        }
    }

    public JavaFileObject getJavaFileForOutput (Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException("The SourceFileManager does not support write operations."); // NOI18N
    }       
    
    public void flush() throws java.io.IOException {
        //Nothing to do
    }

    public void close() throws java.io.IOException {
        //Nothing to do
    }            
    
    public int isSupportedOption(String string) {
        return -1;
    }
    
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }
 
    public boolean hasLocation(Location location) {
        return true;
    }
       
    public ClassLoader getClassLoader (Location l) {
        return null;
    }

    public String inferBinaryName (final Location l, final JavaFileObject jfo) {
        try {
            if (jfo instanceof InferableJavaFileObject) {
                final String result = ((InferableJavaFileObject)jfo).inferBinaryName();
                if (result != null) {
                    return result;
                }
            }
            FileObject fo = URLMapper.findFileObject(jfo.toUri().toURL());
            FileObject root = null;
            if (root == null) {
                for (FileObject rc : this.sourceRoots.getRoots()) {
                    if (FileUtil.isParentOf(rc,fo)) {
                        root = rc;
                    }
                }
            }

            if (root != null) {
                String relativePath = FileUtil.getRelativePath(root,fo);
                int index = relativePath.lastIndexOf('.');
                assert index > 0;
                final String result = relativePath.substring(0,index).replace('/','.');
                return result;
            }
        } catch (MalformedURLException e) {
            if (log.isLoggable(Level.SEVERE))
                log.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public boolean isSameFile(javax.tools.FileObject fileObject, javax.tools.FileObject fileObject0) {
        return
            fileObject instanceof SourceFileObject  &&
            fileObject0 instanceof SourceFileObject &&
            ((SourceFileObject)fileObject).handle.file != null &&
            ((SourceFileObject)fileObject).handle.file == ((SourceFileObject)fileObject0).handle.file;
    }

    private FileObject[] findFile (final String relativePath) {
        for (ClassPath.Entry entry : this.sourceRoots.entries()) {
            if (ignoreExcludes || entry.includes(relativePath)) {
                FileObject root = entry.getRoot();
                if (root != null) {
                    FileObject file = root.getFileObject(relativePath);
                    if (file != null) {
                        return new FileObject[] {file, root};
                    }
                }
            }
        }
        return null;
    }
}
