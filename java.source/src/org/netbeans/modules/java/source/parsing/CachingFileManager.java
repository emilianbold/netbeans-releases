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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.util.Iterators;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;


/** Implementation of file manager for given classpath.
 *
 * @author Petr Hrebejk
 */
public class CachingFileManager implements JavaFileManager, PropertyChangeListener {

    protected final CachingArchiveProvider provider;
    protected final JavaFileFilterImplementation filter;
    protected final ClassPath cp;
    protected final boolean cacheFile;
    protected final boolean ignoreExcludes;
    private final boolean allowOutput;

    private static final Logger LOG = Logger.getLogger(CachingFileManager.class.getName());


    public CachingFileManager( CachingArchiveProvider provider, final ClassPath cp, boolean cacheFile, boolean ignoreExcludes) {
        this (provider, cp, null, false, cacheFile, ignoreExcludes);
    }

    /** Creates a new instance of CachingFileManager */
    public CachingFileManager( CachingArchiveProvider provider, final ClassPath cp, final JavaFileFilterImplementation filter, boolean cacheFile, boolean ignoreExcludes) {
        this (provider, cp, filter, true, cacheFile, ignoreExcludes);
    }

    private CachingFileManager(final CachingArchiveProvider provider,
            final ClassPath cp,
            final JavaFileFilterImplementation filter,
            boolean allowOutput,
            boolean cacheFile,
            boolean ignoreExcludes) {
        assert provider != null;
        assert cp != null;
        this.provider = provider;
        this.cp = cp;
        if (CacheClassPath.KEEP_JARS) {
            cp.addPropertyChangeListener(WeakListeners.propertyChange(this, cp));
        }
        this.filter = filter;
        this.allowOutput = allowOutput;
        this.cacheFile = cacheFile;
        this.ignoreExcludes = ignoreExcludes;
    }

    // FileManager implementation ----------------------------------------------
    
    // XXX omit files not of given kind
    public Iterable<JavaFileObject> list( Location l, String packageName, Set<JavaFileObject.Kind> kinds, boolean recursive ) {
     
        if (recursive) {
            throw new UnsupportedOperationException ("Recursive listing is not supported in archives");
        }
        
//        long start = System.currentTimeMillis();
        
        String folderName = FileObjects.convertPackage2Folder( packageName );
                        
        List<Iterable<JavaFileObject>> idxs = new LinkedList<Iterable<JavaFileObject>>();
        for(ClassPath.Entry entry : this.cp.entries()) {
            try {
                Archive archive = provider.getArchive( entry.getURL(), cacheFile );
                if (archive != null) {
                    Iterable<JavaFileObject> entries = archive.getFiles( folderName, ignoreExcludes?null:entry, kinds, filter);
                    idxs.add(entries);
                    if (LOG.isLoggable(Level.FINEST)) {
                        final StringBuilder urls = new StringBuilder ();
                        for (JavaFileObject jfo : entries) {
                            urls.append(jfo.toUri().toString());
                            urls.append(", ");  //NOI18N
                        }
                        LOG.finest(String.format("cache for %s (%s) package: %s type: %s files: [%s]",   //NOI18N
                                l.toString(),
                                entry.getURL().toExternalForm(),
                                packageName,
                                kinds.toString(),
                                urls.toString()));
                    }
                }
                else if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest(String.format("no cache for: %s", entry.getURL().toExternalForm()));           //NOI18N
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return Iterators.chained(idxs);
    }

    public javax.tools.FileObject getFileForInput( Location l, String pkgName, String relativeName ) {
        return findFile(pkgName, relativeName);
    }

    public JavaFileObject getJavaFileForInput (Location l, String className, JavaFileObject.Kind kind) {
        String[] namePair = FileObjects.getParentRelativePathAndName(className);
        if (namePair == null) {
            return null;
        }
        namePair[1] = namePair[1] + kind.extension;
        for( ClassPath.Entry root : this.cp.entries()) {
            try {
                Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    Iterable<JavaFileObject> files = archive.getFiles(namePair[0], ignoreExcludes?null:root, null, filter);
                    for (JavaFileObject e : files) {
                        if (namePair[1].equals(e.getName())) {
                            return e;
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }


    public javax.tools.FileObject getFileForOutput( Location l, String pkgName, String relativeName, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (!allowOutput) {
            throw new UnsupportedOperationException("Output is unsupported.");  //NOI18N
        }
        javax.tools.JavaFileObject file = findFile (pkgName, relativeName);
        if (file == null) {
            final List<ClassPath.Entry> entries = this.cp.entries();
            if (!entries.isEmpty()) {
                final String resourceName = FileObjects.getRelativePath(FileObjects.convertPackage2Folder(pkgName), relativeName);
                file = provider.getArchive(entries.get(0).getURL(), cacheFile).create(resourceName, filter);
            }
        }
        return file;    //todo: wrap to make read only
    }

    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling ) 
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ();
    }        
    
    public void flush() throws IOException {
        // XXX Do nothing?
    }

    public void close() throws IOException {
        // XXX Do nothing?
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
    
    public ClassLoader getClassLoader (final Location l) {
        return null;
    }    
    
    public String inferBinaryName (Location l, JavaFileObject javaFileObject) {        
        if (javaFileObject instanceof FileObjects.Base) {
            final FileObjects.Base base = (FileObjects.Base) javaFileObject;
            final StringBuilder sb = new StringBuilder ();
            sb.append (base.getPackage());
            sb.append('.'); //NOI18N
            sb.append(base.getNameWithoutExtension());
            return sb.toString();
        }
        else if (javaFileObject instanceof InferableJavaFileObject) {
            return ((SourceFileObject)javaFileObject).inferBinaryName();
        }
        return null;
    }
    
    //Static helpers - temporary
    
    public static URL[] getClassPathRoots (final ClassPath cp) {
       assert cp != null;
       final List<ClassPath.Entry> entries = cp.entries();
       final List<URL> result = new ArrayList<URL>(entries.size());
       for (ClassPath.Entry entry : entries) {
           result.add (entry.getURL());
       }
       return result.toArray(new URL[result.size()]);
    }            

    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {        
        return fileObject instanceof FileObjects.FileBase 
               && fileObject0 instanceof FileObjects.FileBase 
               && ((FileObjects.FileBase)fileObject).getFile().equals(((FileObjects.FileBase)fileObject0).getFile());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            provider.clear();
        }
    }

    private javax.tools.JavaFileObject findFile(final String pkgName, String relativeName) {
        assert pkgName != null;
        assert relativeName != null;
        String folderName = FileObjects.convertPackage2Folder(pkgName);
        if (relativeName.indexOf('/') != -1) {  //NOI18N
            final String[] fbn = FileObjects.getFolderAndBaseName(relativeName, '/');   //NOI18N
            folderName = FileObjects.getRelativePath(folderName,fbn[0]);
            relativeName = fbn[1];
        }
        for( ClassPath.Entry root : this.cp.entries()) {
            try {
                Archive  archive = provider.getArchive (root.getURL(), cacheFile);
                if (archive != null) {
                    Iterable<JavaFileObject> files = archive.getFiles(folderName, ignoreExcludes?null:root, null, filter);
                    for (JavaFileObject e : files) {
                        if (relativeName.equals(e.getName())) {
                            return e;
                        }
                    }
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return null;
    }
}
