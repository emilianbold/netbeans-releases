/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(ModuleFileManager.class.getName());

    private final CachingArchiveProvider cap;
    private final ClassPath modulePath;
    private final boolean cacheFile;


    public ModuleFileManager(
            @NonNull final CachingArchiveProvider cap,
            @NonNull final ClassPath modulePath,
            final boolean cacheFile) {
        assert cap != null;
        assert modulePath != null;
        this.cap = cap;
        this.modulePath = modulePath;
        this.cacheFile = cacheFile;
    }

    // FileManager implementation ----------------------------------------------

    @Override
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recursive ) {
        final ModuleLocation ml = asModuleLocation(l);
        final String folderName = FileObjects.convertPackage2Folder(packageName);
        try {
            final Archive archive = cap.getArchive(ml.getModuleRoot(), cacheFile);
            if (archive != null) {
                final Iterable<JavaFileObject> entries = archive.getFiles(folderName, null, kinds, null, recursive);
                if (LOG.isLoggable(Level.FINEST)) {
                    final StringBuilder urls = new StringBuilder ();
                    for (JavaFileObject jfo : entries) {
                        urls.append(jfo.toUri().toString());
                        urls.append(", ");  //NOI18N
                    }
                    LOG.log(
                        Level.FINEST,
                        "Cache for {0} package: {1} type: {2} files: [{3}]",   //NOI18N
                        new Object[] {
                            l,
                            packageName,
                            kinds,
                            urls
                        });
                }
                return entries;
            } else if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(
                    Level.FINEST,
                    "No cache for: {0}",               //NOI18N
                    ml.getModuleRoot());
            }
        } catch (final IOException e) {
            Exceptions.printStackTrace(e);
        }
        return Collections.emptySet();
    }

    @Override
    public FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName ) {
        return findFile(asModuleLocation(l), pkgName, relativeName);
    }

    @Override
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) {
        final ModuleLocation ml = asModuleLocation(l);
        final String[] namePair = FileObjects.getParentRelativePathAndName(className);
        try {
            final Archive  archive = cap.getArchive (ml.getModuleRoot(), cacheFile);
            if (archive != null) {
                final Iterable<JavaFileObject> files = archive.getFiles(namePair[0], null, null, null, false);
                for (JavaFileObject e : files) {
                    final String ename = e.getName();
                    if (namePair[1].equals(FileObjects.stripExtension(ename)) &&
                        kind == FileObjects.getKind(FileObjects.getExtension(ename))) {
                        return e;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }


    @Override
    public FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String pkgName,
            @NonNull final String relativeName,
            @NullAllowed final FileObject sibling ) throws IOException {
        throw new UnsupportedOperationException("Output is unsupported.");  //NOI18N
    }

    @Override
    public JavaFileObject getJavaFileForOutput( Location l, String className, JavaFileObject.Kind kind, FileObject sibling )
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException ("Output is unsupported.");
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int isSupportedOption(String string) {
        return -1;
    }

    @Override
    public boolean handleOption (final String head, final Iterator<String> tail) {
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        return true;
    }

    @Override
    public ClassLoader getClassLoader (final Location l) {
        return null;
    }

    @Override
    public String inferBinaryName (Location l, JavaFileObject javaFileObject) {
        if (javaFileObject instanceof InferableJavaFileObject) {
            return ((InferableJavaFileObject)javaFileObject).inferBinaryName();
        }
        return null;
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a instanceof FileObjects.FileBase
               && b instanceof FileObjects.FileBase
               && ((FileObjects.FileBase)a).getFile().equals(((FileObjects.FileBase)b).getFile());
    }

    @Override
    @NonNull
    public Iterable<Set<Location>> listModuleLocations(@NonNull final Location location) throws IOException {
        final Set<Set<Location>> moduleRoots = new HashSet<>();
        for (ClassPath.Entry e : modulePath.entries()) {
            final URL root = e.getURL();
            final String moduleName = SourceUtils.getModuleName(root);
            if (moduleName != null) {
                moduleRoots.add(Collections.singleton(ModuleLocation.create(location, root, moduleName)));
            }
        }
        return moduleRoots;
    }

    @Override
    @NullUnknown
    public String inferModuleName(@NonNull final Location location) throws IOException {
        final ModuleLocation ml = asModuleLocation(location);
        return ml.getModuleName();
    }

    @Override
    @CheckForNull
    public Location getModuleLocation(Location location, JavaFileObject fo, String pkgName) throws IOException {
        //todo: Only for Source Module Path & Output Path
        return null;
    }

    @Override
    @CheckForNull
    public Location getModuleLocation(Location location, String moduleName) throws IOException {
        //todo: Only for Source Module Path & Output Path
        return null;
    }

    private JavaFileObject findFile(
            @NonNull final ModuleLocation ml,
            @NonNull final String pkgName,
            @NonNull final String relativeName) {
        assert ml != null;
        assert pkgName != null;
        assert relativeName != null;
        final String resourceName = FileObjects.resolveRelativePath(pkgName,relativeName);
        try {
            final Archive  archive = cap.getArchive (ml.getModuleRoot(), cacheFile);
            if (archive != null) {
                final JavaFileObject file = archive.getFile(resourceName);
                if (file != null) {
                    return file;
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    @NonNull
    private static ModuleLocation asModuleLocation (@NonNull final Location l) {
        if (l.getClass() != ModuleLocation.class) {
            throw new IllegalArgumentException (String.valueOf(l));
        }
        return (ModuleLocation) l;
    }
}
