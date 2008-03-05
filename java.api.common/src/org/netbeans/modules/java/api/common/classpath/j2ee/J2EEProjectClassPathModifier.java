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

package org.netbeans.modules.java.api.common.classpath.j2ee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport.Operation;
import org.netbeans.modules.java.api.common.classpath.ClassPathItem;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport.ClassPathItemProvider;
import org.netbeans.modules.java.api.common.classpath.ProjectClassPathModifierSupport.Properties;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

// will be moved to j2ee.common
/**
 *@author Tomas Zezula, Tomas Mysik
 */
public class J2EEProjectClassPathModifier extends ProjectClassPathModifierImplementation
        implements PropertyChangeListener {

    // XXX ok or not?
    private static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    private static final String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N

    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;
    private final J2EEClassPathSupport classPathSupport;
    private final ProjectClassPathModifierSupport<J2EEClassPathSupport.Item> classPathModifierSupport;
    private final ClassPathItemProvider<J2EEClassPathSupport.Item> provider;

    private volatile boolean projectDeleted;

    private final PropertyChangeListener listener = WeakListeners.propertyChange(this, null);

    // XXX javadoc
    public static J2EEProjectClassPathModifier create(Project project, UpdateHelper helper, PropertyEvaluator eval,
            ReferenceHelper refHelper, J2EEClassPathSupport classPathSupport, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {
        Parameters.notNull("project", project);
        Parameters.notNull("helper", helper);
        Parameters.notNull("eval", eval);
        Parameters.notNull("classPathSupport", classPathSupport);

        return new J2EEProjectClassPathModifier(project, helper, eval, refHelper, classPathSupport, sourceRoots,
                testSourceRoots, properties);
    }

    J2EEProjectClassPathModifier(Project project, UpdateHelper helper, PropertyEvaluator eval,
            ReferenceHelper refHelper, J2EEClassPathSupport classPathSupport, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert classPathSupport != null;

        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.classPathSupport = classPathSupport;
        classPathModifierSupport = ProjectClassPathModifierSupport.<J2EEClassPathSupport.Item>create(project, helper,
                eval, refHelper, sourceRoots, testSourceRoots, properties);
        provider = new J2EEClassPathItemProvider(ELEMENT_INCLUDED_LIBRARIES);
        //#56140
        eval.addPropertyChangeListener(listener); //listen for changes of libraries list
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                registerLibraryListeners();
            }
        });
    }

    final class J2EEClassPathItemProvider implements ClassPathItemProvider<J2EEClassPathSupport.Item> {

        private final String elementName;

        public J2EEClassPathItemProvider(String elementName) {
            this.elementName = elementName;
        }

        public J2EEClassPathSupport.Item createClassPathItem(File file, String property) {
            return J2EEClassPathSupport.Item.create(file, null, property != null);
        }

        public J2EEClassPathSupport.Item createClassPathItem(AntArtifact antArtifact, URI antArtifactURI, String property) {
            return J2EEClassPathSupport.Item.create(antArtifact, antArtifactURI, null, property != null);
        }

        public J2EEClassPathSupport.Item createClassPathItem(Library library, String property) {
            return J2EEClassPathSupport.Item.create(library, null, property != null);
        }

        public List<J2EEClassPathSupport.Item> getClassPathItems(String reference) {
            return classPathSupport.itemsList(reference, elementName);
        }

        public String[] encodeToStrings(List<J2EEClassPathSupport.Item> items) {
            return classPathSupport.encodeToStrings(items, elementName);
        }

        public String getLibraryReference(J2EEClassPathSupport.Item item) {
            return classPathSupport.getLibraryReference(item);
        }
    }

    @Override
    protected SourceGroup[] getExtensibleSourceGroups() {
        return classPathModifierSupport.getExtensibleSourceGroups();
    }

    @Override
    protected String[] getExtensibleClassPathTypes(SourceGroup sourceGroup) {
        return classPathModifierSupport.getExtensibleClassPathTypes(sourceGroup);
    }

    @Override
    protected boolean removeRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, Operation.ADD, provider);
    }

    @Override
    protected boolean addRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, Operation.ADD, provider);
    }

    // used by ClassPathExtender
    protected boolean handleRoots(URL[] classPathRoots, String classPathProperty, String elementName,
            Operation operation) throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, operation, provider);
    }

    @Override
    protected boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements, classPathProperty,
                Operation.REMOVE, provider);
    }

    @Override
    protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements, classPathProperty,
                Operation.ADD, provider);
    }

    @Override
    protected boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return handleLibraries(libraries, classPathProperty, Operation.REMOVE, provider);
    }

    @Override
    protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return handleLibraries(libraries, classPathProperty, Operation.ADD, provider);
    }

    // used by ClassPathExtender
    protected boolean handleLibraries(final Library[] libraries, final String classPathProperty,
            final String elementName, final Operation operation) throws IOException,
            UnsupportedOperationException {
        return handleLibraries(libraries, classPathProperty, operation, new J2EEClassPathItemProvider(elementName));
    }

    // XXX check this - see saving of private properties
    public boolean handleLibraries(final Library[] libraries, final String classPathProperty, final Operation operation,
            final ClassPathItemProvider<J2EEClassPathSupport.Item> provider) throws IOException,
            UnsupportedOperationException {
        assert libraries != null : "Libraries cannot be null";
        assert classPathProperty != null;

        try {
            return ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {
                        public Boolean run() throws IOException {
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            String raw = props.getProperty(classPathProperty);
                            List<J2EEClassPathSupport.Item> resources = provider.getClassPathItems(raw);
                            List<J2EEClassPathSupport.Item> changed = new ArrayList<J2EEClassPathSupport.Item>(
                                    libraries.length);
                            for (Library library : libraries) {
                                assert library != null;

                                J2EEClassPathSupport.Item item = provider.createClassPathItem(library, null);
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
                                    for (J2EEClassPathSupport.Item item : changed) {
                                        String prop = provider.getLibraryReference(item);
                                        // XXX make a PropertyUtils method for this!
                                        prop = prop.substring(2, prop.length() - 1);
                                        ClassPathSupport.relativizeLibraryClassPath(props, helper.getAntProjectHelper(),
                                                prop);
                                    }
                                }
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

                                // XXX really needed? if not, ProjectClassPathModifierSupport could be used
                                // update lib references in private properties
                                EditableProperties privateProps = helper.getProperties(
                                        AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                List<J2EEClassPathSupport.Item> libs = new ArrayList<J2EEClassPathSupport.Item>();
                                libs.addAll(resources);
                                storeLibrariesLocations(libs.iterator(), privateProps);
                                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

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

    private void registerLibraryListeners() {
        // reread the properties, PathParser changes them
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Library[] libs = LibraryManager.getDefault().getLibraries();
        for (Library library : libs) {
            library.removePropertyChangeListener(this);
        }
        Iterator<J2EEClassPathSupport.Item> i = classPathSupport.itemsIterator(props.getProperty(JAVAC_CLASSPATH),
                ELEMENT_INCLUDED_LIBRARIES);
        while (i.hasNext()) {
            J2EEClassPathSupport.Item item = i.next();
            if (item.getType() == ClassPathItem.Type.LIBRARY && !item.isBroken()) {
                item.getLibrary().addPropertyChangeListener(this);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (projectDeleted) {
            return;
        }
        if (e.getSource().equals(eval) && (e.getPropertyName().equals(JAVAC_CLASSPATH))) {
            // reread the properties, PathParser changes them
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String javacCp = props.getProperty(JAVAC_CLASSPATH);
            if (javacCp != null) {
                registerLibraryListeners();
                storeLibLocations();
            }
        } else if (e.getPropertyName().equals(Library.PROP_CONTENT)) {
            storeLibLocations();
        }
    }

    private void storeLibLocations() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        // reread the properties, PathParser changes them
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        // update lib references in private properties
                        EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                        List<J2EEClassPathSupport.Item> wmLibs = classPathSupport.itemsList(
                                props.getProperty(JAVAC_CLASSPATH), ELEMENT_INCLUDED_LIBRARIES);
                        classPathSupport.encodeToStrings(wmLibs, ELEMENT_INCLUDED_LIBRARIES);
                        storeLibrariesLocations(wmLibs.iterator(), privateProps);
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);

                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
        });
    }

    public void notifyDeleting() {
        projectDeleted = true;
        eval.removePropertyChangeListener(this);
    }

    /** Store locations of libraries in the classpath param that have more the one
     * file into the properties in the following format:
     *
     * <ul>
     * <li>libs.foo.classpath.libdir.1=C:/foo
     * <li>libs.foo.classpath.libdirs=1
     * <li>libs.foo.classpath.libfile.1=C:/bar/a.jar
     * <li>libs.foo.classpath.libfile.2=C:/bar/b.jar
     * <li>libs.foo.classpath.libfiles=1
     * </ul>
     * This is needed for the Ant copy task as it cannot copy more the one file
     * and it needs different handling for files and directories.
     * <br>
     * It removes all properties that match this format that were in the {@link #properties}
     * but are not in the {@link #classpath}.
     */
    public static void storeLibrariesLocations(Iterator<J2EEClassPathSupport.Item> classpath,
            EditableProperties privateProps) {
        List<String> exLibs = new ArrayList<String>();
        for (String key : privateProps.keySet()) {
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || // NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { // NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            J2EEClassPathSupport.Item item = classpath.next();
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            J2EEClassPathSupport.getFilesForItem(item, files, dirs);
            String key;
            if (files.size() > 1 || (files.size() > 0 && dirs.size() > 0)) {
                String ref = item.getType() == ClassPathItem.Type.LIBRARY ? item.getRaw() : item.getReference();
                for (int i = 0; i < files.size(); i++) {
                    File f = files.get(i);
                    key = CommonProjectUtils.getAntPropertyName(ref) + ".libfile." + (i + 1); // NOI18N
                    privateProps.setProperty(key, "" + f.getAbsolutePath()); // NOI18N
                    exLibs.remove(key);
                }
            }
            if (dirs.size() > 1 || (files.size() > 0 && dirs.size() > 0)) {
                String ref = item.getType() == ClassPathItem.Type.LIBRARY ? item.getRaw() : item.getReference();
                for (int i = 0; i < dirs.size(); i++) {
                    File f = dirs.get(i);
                    key = CommonProjectUtils.getAntPropertyName(ref) + ".libdir." + (i + 1); // NOI18N
                    privateProps.setProperty(key, "" + f.getAbsolutePath()); // NOI18N
                    exLibs.remove(key);
                }
            }
        }
        Iterator unused = exLibs.iterator();
        while (unused.hasNext()) {
            privateProps.remove(unused.next());
        }
    }
}
