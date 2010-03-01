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

package org.netbeans.modules.form.project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Describes a source (i.e. classpath) of a component class to be used in form
 * editor.
 *
 * @author Tomas Pavek, Jesse Glick
 */
public final class ClassSource {

    private final String className;
    private final String typeParameters;
    private final Collection<? extends Entry> entries;

    /**
     * @param className name of the class, can be null
     */
    public ClassSource(String className, Entry... entries) {
        this(className, Arrays.asList(entries));
    }
    public ClassSource(String className, Collection<? extends Entry> entries) {
        this(className, entries, null);
    }
    public ClassSource(String className, Collection<? extends Entry> entries, String typeParameters) {
        this.className = className;
        this.entries = entries;
        this.typeParameters = typeParameters;
    }

    public String getClassName() {
        return className;
    }

    public String getTypeParameters() {
        return typeParameters;
    }

    public Collection<? extends Entry> getEntries() {
        return entries;
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    /** Union of {@link ClassSource.Entry#getClasspath}. */
    public List<URL> getClasspath() {
        List<URL> cp = new ArrayList<URL>();
        for (Entry entry : entries) {
            cp.addAll(entry.getClasspath());
        }
        for (URL u : cp) {
            assert u.toExternalForm().endsWith("/") : u;
        }
        return cp;
    }

    /** Calls all {@link ClassSource.Entry#addToProjectClassPath} in turn. */
    public boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException {
        for (Entry entry : entries) {
            if (!entry.addToProjectClassPath(projectArtifact, classPathType)) {
                return false;
            }
        }
        return true;
    }

    private static final String TYPE_JAR = "jar"; // NOI18N
    private static final String TYPE_LIBRARY = "library"; // NOI18N
    private static final String TYPE_PROJECT = "project"; // NOI18N

    /**
     * Reconstruct from serialized form.
     * Used by {@link PaletteItemDataObject} for storing *.palette_item files.
     * @param type as in {@link Entry#getPicklingType}
     * @param name as in {@link Entry#getPicklingName}
     */
    public static Entry unpickle(String type, String name) {
        if (type.equals(TYPE_JAR)) {
            return new JarEntry(new File(name));
        } else if (type.equals(TYPE_LIBRARY)) {
            Library lib;
            int hash = name.indexOf('#');
            if (hash != -1) {
                try {
                    lib = LibraryManager.forLocation(new URL(name.substring(0, hash))).getLibrary(name.substring(hash + 1));
                } catch (IllegalArgumentException x) {
                    Exceptions.printStackTrace(x);
                    return null;
                } catch (MalformedURLException x) {
                    Exceptions.printStackTrace(x);
                    return null;
                }
            } else {
                lib = LibraryManager.getDefault().getLibrary(name);
            }
            return lib != null ? new LibraryEntry(lib) : null;
        } else if (type.equals(TYPE_PROJECT)) {
            AntArtifact aa = AntArtifactQuery.findArtifactFromFile(new File(name));
            return aa != null ? new ProjectEntry(aa) : null;
        } else {
            return null;
        }
    }

    /**
     * One logical component of the classpath.
     */
    public static abstract class Entry {
        private Entry() {}
        /** List of folder URLs (dirs or roots of JARs) making up the classpath. */
        public abstract List<URL> getClasspath();
        /** Tries to add the classpath entries to a project, as with {@link ProjectClassPathModifier}. 
         * @return null if operation was aborted or true if classpath was modified or false if it was not
         */
        public abstract Boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException;
        /** A label suitable for display. */
        public abstract String getDisplayName();
        /** @see #unpickle */
        public abstract String getPicklingType();
        /** @see #unpickle */
        public abstract String getPicklingName();
        public final @Override int hashCode() {
            return getClasspath().hashCode();
        }
        public final @Override boolean equals(Object obj) {
            return obj instanceof Entry && getClasspath().equals(((Entry) obj).getClasspath());
        }
        public final @Override String toString() {
            return super.toString() + getClasspath();
        }
    }

    private static URL translateURL(URL u) {
        if (FileUtil.isArchiveFile(u)) {
            return FileUtil.getArchiveRoot(u);
        } else {
            return u;
        }
    }

    /** Entry based on a single JAR file. */
    public static final class JarEntry extends Entry {
        private final File jar;
        public JarEntry(File jar) {
            assert jar != null;
            this.jar = jar;
        }
        public File getJar() {
            return jar;
        }
        @Override
        public List<URL> getClasspath() {
            try {
                return Collections.singletonList(translateURL(jar.toURI().toURL()));
            } catch (MalformedURLException x) {
                assert false : x;
                return Collections.emptyList();
            }
        }
        @Override
        public Boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException {
            URL u = jar.toURI().toURL();
            FileObject jarFile = FileUtil.toFileObject(jar);
            if (jarFile == null) {
                return Boolean.FALSE; // Issue 147451
            }
            if (FileUtil.isArchiveFile(jarFile)) {
                u = FileUtil.getArchiveRoot(u);
            }
            return Boolean.valueOf(ProjectClassPathModifier.addRoots(new URL[] {u}, projectArtifact, classPathType));
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ClassSource.class, "FMT_JarSource", jar.getAbsolutePath());
        }
        @Override
        public String getPicklingType() {
            return TYPE_JAR;
        }
        @Override
        public String getPicklingName() {
            return jar.getAbsolutePath();
        }
    }

    /** Entry based on a (global or project) library. */
    public static final class LibraryEntry extends Entry {
        private final Library lib;
        public LibraryEntry(Library lib) {
            assert lib != null;
            this.lib = lib;
        }
        public Library getLibrary() {
            return lib;
        }
        @Override
        public List<URL> getClasspath() {
            // No need to translate to jar protocol; Library.getContent should have done this already.
            return lib.getContent("classpath"); // NOI18N
        }
        @Override
        public Boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException {
            return  Boolean.valueOf(ProjectClassPathModifier.addLibraries(new Library[] {lib}, projectArtifact, classPathType));
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ClassSource.class, "FMT_LibrarySource", lib.getDisplayName());
        }
        @Override
        public String getPicklingType() {
            return TYPE_LIBRARY;
        }
        @Override
        public String getPicklingName() {
            // For backward compatibility with old *.palette_item files, treat bare names as global libraries.
            // Project libraries are given as e.g. "file:/some/where/libs/index.properties#mylib"
            LibraryManager mgr = lib.getManager();
            if (mgr == LibraryManager.getDefault()) {
                return lib.getName();
            } else {
                return mgr.getLocation() + "#" + lib.getName(); // NOI18N
            }
        }
    }

    /** Entry based on a (sub-)project build artifact. */
    public static final class ProjectEntry extends Entry {
        private final AntArtifact artifact;
        public ProjectEntry(AntArtifact artifact) {
            assert artifact != null;
            this.artifact = artifact;
        }
        public AntArtifact getArtifact() {
            return artifact;
        }
        @Override
        public List<URL> getClasspath() {
            List<URL> cp = new ArrayList<URL>();
            for (URI loc : artifact.getArtifactLocations()) {
                try {
                    cp.add(translateURL(artifact.getScriptLocation().toURI().resolve(loc).normalize().toURL()));
                } catch (MalformedURLException x) {
                    assert false : x;
                }
            }
            return cp;
        }
        @Override
        public Boolean addToProjectClassPath(FileObject projectArtifact, String classPathType) throws IOException, UnsupportedOperationException {
            if (artifact.getProject() != FileOwnerQuery.getOwner(projectArtifact)) {
                return Boolean.valueOf(ProjectClassPathModifier.addAntArtifacts(new AntArtifact[] {artifact}, artifact.getArtifactLocations(), projectArtifact, classPathType));
            }
            return Boolean.FALSE;
        }
        @Override
        public String getDisplayName() {
            Project p = artifact.getProject();
            return NbBundle.getMessage(ClassSource.class, "FMT_ProjectSource",
                    p != null ? FileUtil.getFileDisplayName(p.getProjectDirectory()) : artifact.getScriptLocation().getAbsolutePath());
        }
        @Override
        public String getPicklingType() {
            return TYPE_PROJECT;
        }
        @Override
        public String getPicklingName() {
            if (artifact.getArtifactLocations().length > 0) {
                return new File(artifact.getScriptLocation().toURI().resolve(artifact.getArtifactLocations()[0]).normalize()).getAbsolutePath();
            } else {
                return "";
            }
        }
    }

}
