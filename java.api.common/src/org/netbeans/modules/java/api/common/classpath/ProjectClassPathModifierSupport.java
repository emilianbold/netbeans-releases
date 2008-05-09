/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.api.common.classpath;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;

/**
 * @author Tomas Zezula, Tomas Mysik
 */
public final class ProjectClassPathModifierSupport<T extends ClassPathItem> {

    public static enum Operation {
        ADD,
        REMOVE
    }

    private final Project project;
    private final UpdateHelper helper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final Properties properties;

    // XXX javadoc
    public static <T extends ClassPathItem> ProjectClassPathModifierSupport<T> create(Project project,
            UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {

        return new ProjectClassPathModifierSupport<T>(project, helper, eval, refHelper, sourceRoots, testSourceRoots,
                properties);
    }

    private ProjectClassPathModifierSupport(Project project, UpdateHelper helper, PropertyEvaluator eval,
            ReferenceHelper refHelper, SourceRoots sourceRoots, SourceRoots testSourceRoots, Properties properties) {
        Parameters.notNull("project", project);
        Parameters.notNull("helper", helper);
        Parameters.notNull("eval", eval);
        Parameters.notNull("refHelper", refHelper);
        Parameters.notNull("sourceRoots", sourceRoots);
        Parameters.notNull("testSourceRoots", testSourceRoots);
        Parameters.notNull("properties", properties);

        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.refHelper = refHelper;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        this.properties = properties;
    }

    public SourceGroup[] getExtensibleSourceGroups() {
        Sources s = project.getLookup().lookup(Sources.class);
        assert s != null;
        return s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
    }

    public String[] getExtensibleClassPathTypes (SourceGroup sg) {
        return new String[] {
            ClassPath.COMPILE,
            ClassPath.EXECUTE
        };
    }

    public boolean handleRoots(final URL[] classPathRoots, final String classPathProperty, final Operation operation,
            final ClassPathItemProvider<T> provider) throws IOException, UnsupportedOperationException {
        assert classPathRoots != null : "The classPathRoots cannot be null";
        assert classPathProperty != null;

        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);
                            List<T> resources = provider.getClassPathItems(raw);
                            boolean changed = false;
                            for (URL classPathRoot : classPathRoots) {
                                assert classPathRoot != null;
                                assert classPathRoot.toExternalForm().endsWith("/");

                                URL toAdd = FileUtil.getArchiveFile(classPathRoot);
                                if (toAdd == null) {
                                    toAdd = classPathRoot;
                                }
                                File f = FileUtil.normalizeFile(new File(URI.create(toAdd.toExternalForm())));
                                if (f == null) {
                                    throw new IllegalArgumentException("The file must exist on disk");
                                }
                                T item = provider.createClassPathItem(f, null);
                                if (operation == Operation.ADD && !resources.contains(item)) {
                                    resources.add(item);
                                    changed = true;
                                } else if (operation == Operation.REMOVE) {
                                    if (resources.remove(item)) {
                                        changed = true;
                                    } else {
                                        for (Iterator<T> it = resources.iterator(); it.hasNext();) {
                                            T itm = it.next();
                                            if (itm.isBroken()
                                                    && itm.getType() == ClassPathItem.Type.JAR
                                                    && f.equals(itm.getFile())) {
                                                it.remove();
                                                changed = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (changed) {
                                String[] itemRefs = provider.encodeToStrings(resources);
                                // PathParser may change the EditableProperties
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.setProperty(classPathProperty, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                    }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public boolean handleAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final String classPathProperty, final Operation operation, final ClassPathItemProvider<T> provider)
            throws IOException, UnsupportedOperationException {
        assert artifacts != null : "Artifacts cannot be null";
        assert artifactElements != null : "ArtifactElements cannot be null";
        assert artifacts.length == artifactElements.length : "Each artifact has to have corresponding artifactElement";
        assert classPathProperty != null;

        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws Exception {
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);
                            List<T> resources = provider.getClassPathItems(raw);
                            boolean changed = false;
                            for (int i = 0; i < artifacts.length; i++) {
                                assert artifacts[i] != null;
                                assert artifactElements[i] != null;

                                T item = provider.createClassPathItem(artifacts[i], artifactElements[i], null);
                                if (operation == Operation.ADD && !resources.contains(item)) {
                                    resources.add(item);
                                    changed = true;
                                } else if (operation == Operation.REMOVE && resources.contains(item)) {
                                    resources.remove(item);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                String[] itemRefs = provider.encodeToStrings(resources);
                                // reread the properties, PathParser changes them
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.setProperty(classPathProperty, itemRefs);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                    }
            );
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                IOException t = new IOException();
                t.initCause(e);
                throw t;
            }
        }
    }

    public boolean handleLibraries(final Library[] libraries, final String classPathProperty, final Operation operation,
            final ClassPathItemProvider<T> provider) throws IOException, UnsupportedOperationException {
        assert libraries != null : "Libraries cannot be null";
        assert classPathProperty != null;

        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws IOException {
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);
                            List<T> resources = provider.getClassPathItems(raw);
                            List<T> changed = new ArrayList<T>(libraries.length);
                            for (Library library : libraries) {
                                assert library != null;

                                T item = provider.createClassPathItem(library, null);
                                if (operation == Operation.ADD && !resources.contains(item)) {
                                    resources.add(item);
                                    changed.add(item);
                                } else if (operation == Operation.REMOVE && resources.contains(item)) {
                                    resources.remove(item);
                                    changed.add(item);
                                }
                            }
                            if (!changed.isEmpty()) {
                                String[] itemRefs = provider.encodeToStrings(resources);
                                // PathParser may change the EditableProperties
                                props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.setProperty(classPathProperty, itemRefs);
                                if (operation == Operation.ADD) {
                                    for (T item : changed) {
                                        String prop = provider.getLibraryReference(item);
                                        // XXX make a PropertyUtils method for this!
                                        prop = prop.substring(2, prop.length() - 1);
                                        ClassPathSupport.relativizeLibraryClassPath(props, helper.getAntProjectHelper(),
                                                prop);
                                    }
                                }
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                ProjectManager.getDefault().saveProject(project);
                                return true;
                            }
                            return false;
                        }
                    }
            );
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

    public String getClassPathProperty(final SourceGroup sg, final String type) throws UnsupportedOperationException {
        assert sg != null : "SourceGroup cannot be null";
        assert type != null : "Type cannot be null";

        final String classPathProperty = getPropertyName(sg, type);
        if (classPathProperty == null) {
            throw new UnsupportedOperationException("Modification of [" + sg.getRootFolder().getPath() + ", "
                    + type + "] is not supported");
        }
        return classPathProperty;
    }

    // XXX used to be in ClassPathProviderImpl
    /**
     * Get the name of the property of the classpath for the given source group and classpath type.
     * The property is searched in sources as well as tests and can be <code>null</code> if the root of the given
     * source group is not found.
     * @param sg source group the property is searched for.
     * @param type classpath type - compile or execute, see {@link ClassPath} for more information.
     * @return the property name or <code>null</code> if nothing found.
     */
    private String getPropertyName(SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        for (FileObject fo : sourceRoots.getRoots()) {
            if (root.equals(fo)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return properties.sourceCompileTimeClassPath;
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return properties.sourceRunTimeClassPath;
                }
                return null;
            }
        }
        for (FileObject fo : testSourceRoots.getRoots()) {
            if (root.equals(fo)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return properties.testSourceCompileTimeClassPath;
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return properties.testSourceRunTimeClassPath;
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Class holding different properties like "javac.classpath" etc.
     */
    public static final class Properties {
        final String sourceCompileTimeClassPath;
        final String sourceRunTimeClassPath;
        final String testSourceCompileTimeClassPath;
        final String testSourceRunTimeClassPath;

        public Properties(String sourceCompileTimeClassPath, String sourceRunTimeClassPath,
                String testSourceCompileTimeClassPath, String testSourceRunTimeClassPath) {
            Parameters.notNull("sourceCompileTimeClassPath", sourceCompileTimeClassPath);
            Parameters.notNull("sourceRunTimeClassPath", sourceRunTimeClassPath);
            Parameters.notNull("testSourceCompileTimeClassPath", testSourceCompileTimeClassPath);
            Parameters.notNull("testSourceRunTimeClassPath", testSourceRunTimeClassPath);

            this.sourceCompileTimeClassPath = sourceCompileTimeClassPath;
            this.sourceRunTimeClassPath = sourceRunTimeClassPath;
            this.testSourceCompileTimeClassPath = testSourceCompileTimeClassPath;
            this.testSourceRunTimeClassPath = testSourceRunTimeClassPath;
        }
    }

    // XXX javadoc
    public interface ClassPathItemProvider<T> {
        T createClassPathItem(File file, String property);

        T createClassPathItem(AntArtifact antArtifact, URI antArtifactURI, String property);

        T createClassPathItem(Library library, String property);

        List<T> getClassPathItems(String reference);

        String[] encodeToStrings(List<T> items);

        String getLibraryReference(T item);
    }
}
