/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author sdedic
 */
public class StubJavaFileManager implements StandardJavaFileManager {
    private final JavaFileManager delegate;
    private final PathFactory pathFactory = Paths::get;
    private final ClasspathInfo   cpInfo;

    public StubJavaFileManager(JavaFileManager delegate, ClasspathInfo cpInfo) {
        this.delegate = delegate;
        this.cpInfo = cpInfo;
    }
    
    @Override
    public ClassLoader getClassLoader(Location location) {
        return delegate.getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        return delegate.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return delegate.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return delegate.isSameFile(a, b);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return delegate.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
        return delegate.hasLocation(location);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        return delegate.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        return delegate.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        return delegate.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        return delegate.getFileForOutput(location, packageName, relativeName, sibling);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public Location getModuleLocation(Location location, String moduleName) throws IOException {
        return delegate.getModuleLocation(location, moduleName);
    }

    @Override
    public Location getModuleLocation(Location location, JavaFileObject fo, String pkgName) throws IOException {
        return delegate.getModuleLocation(location, fo, pkgName);
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service) throws IOException {
        return delegate.getServiceLoader(location, service);
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
        return delegate.inferModuleName(location);
    }

    @Override
    public Iterable<Set<Location>> listModuleLocations(Location location) throws IOException {
        return delegate.listModuleLocations(location);
    }

    @Override
    public int isSupportedOption(String option) {
        return delegate.isSupportedOption(option);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(Iterable<? extends File> files) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Path getPath(String first, String... more) {
        return pathFactory.getPath(first, more);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(Iterable<String> names) {
        List<Path> paths = new ArrayList<>();
        for (String name : names)
            paths.add(getPath(name));
        return getJavaFileObjectsFromPaths(paths);
    }

    @Override
    public Iterable<? extends JavaFileObject> getJavaFileObjects(String... names) {
        return getJavaFileObjectsFromStrings(Arrays.asList(names));
    }

    @Override
    public void setLocation(Location location, Iterable<? extends File> files) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterable<? extends File> getLocation(Location location) {
        org.openide.filesystems.FileObject[] roots;
        ClassPath cp = null;
        if (location == StandardLocation.SOURCE_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        } else if (location == StandardLocation.CLASS_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
        } else if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            cp = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
        }
        if (cp == null) {
            return null;
        }
        roots = cp.getRoots();
        if (roots == null || roots.length == 0) {
            return null;
        }
        List<File> res = new ArrayList<>(roots.length);
        for (org.openide.filesystems.FileObject f : roots) {
            File x = FileUtil.toFile(f);
            if (x != null) {
                res.add(x);
            }
        }
        return res;
    }
}
