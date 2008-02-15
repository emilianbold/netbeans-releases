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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
public class WebProjectClassPathModifier extends ProjectClassPathModifierImplementation
        implements PropertyChangeListener {

    // XXX ok or not?
    private static final String JAVAC_CLASSPATH = "javac.classpath"; // NOI18N
    private static final String WAR_CONTENT_ADDITIONAL = "war.content.additional"; // NOI18N
    private static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    private static final String TAG_WEB_MODULE_ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; // NOI18N
    private static final String DEFAULT_WEB_MODULE_ELEMENT_NAME = WebClassPathSupport.TAG_WEB_MODULE_LIBRARIES;

    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;
    private final WebClassPathSupport classPathSupport;
    private final ProjectClassPathModifierSupport<WebClassPathSupport.Item> classPathModifierSupport;

    private volatile boolean projectDeleted;

    private boolean dontFireChange = false;

    private final PropertyChangeListener listener = WeakListeners.propertyChange(this, null);

    // XXX javadoc
    public static WebProjectClassPathModifier create(Project project, UpdateHelper helper, PropertyEvaluator eval,
            ReferenceHelper refHelper, WebClassPathSupport classPathSupport, SourceRoots sourceRoots,
            SourceRoots testSourceRoots, Properties properties) {
        Parameters.notNull("project", project);
        Parameters.notNull("helper", helper);
        Parameters.notNull("eval", eval);
        Parameters.notNull("classPathSupport", classPathSupport);

        return new WebProjectClassPathModifier(project, helper, eval, refHelper, classPathSupport, sourceRoots,
                testSourceRoots, properties);
    }

    WebProjectClassPathModifier(Project project, UpdateHelper helper, PropertyEvaluator eval, ReferenceHelper refHelper,
            WebClassPathSupport classPathSupport, SourceRoots sourceRoots, SourceRoots testSourceRoots,
            Properties properties) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        assert classPathSupport != null;

        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.classPathSupport = classPathSupport;
        classPathModifierSupport = ProjectClassPathModifierSupport.<WebClassPathSupport.Item>create(project, helper,
                eval, refHelper, sourceRoots, testSourceRoots, properties);
        //#56140
        eval.addPropertyChangeListener(listener); //listen for changes of libraries list
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                registerLibraryListeners();
            }
        });
    }

    final class WebClassPathItemProvider implements ClassPathItemProvider<WebClassPathSupport.Item> {

        private final String elementName;

        public WebClassPathItemProvider(String elementName) {
            this.elementName = elementName;
        }

        public WebClassPathSupport.Item createClassPathItem(File file, String property) {
            String pathInWar = null;
            if (file.isDirectory()) {
                pathInWar = WebClassPathSupport.Item.PATH_IN_WAR_DIR;
            } else {
                pathInWar = WebClassPathSupport.Item.PATH_IN_WAR_LIB;
            }
            return WebClassPathSupport.Item.create(file, property, pathInWar);
        }

        public WebClassPathSupport.Item createClassPathItem(AntArtifact antArtifact, URI antArtifactURI, String property) {
            return WebClassPathSupport.Item.create(antArtifact, antArtifactURI, property,
                    WebClassPathSupport.Item.PATH_IN_WAR_LIB);
        }

        public WebClassPathSupport.Item createClassPathItem(Library library, String property) {
            // not used, handled by this class itself
            throw new UnsupportedOperationException("This method is not supported");
        }

        public List<WebClassPathSupport.Item> getClassPathItems(String reference) {
            return classPathSupport.itemsList(reference, elementName);
        }

        public String[] encodeToStrings(List<WebClassPathSupport.Item> items) {
            return classPathSupport.encodeToStrings(items, elementName);
        }

        public String getLibraryReference(WebClassPathSupport.Item item) {
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
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, Operation.REMOVE,
                new WebClassPathItemProvider(getElementName(classPathProperty)));
    }

    @Override
    protected boolean addRoots(final URL[] classPathRoots, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, Operation.ADD,
                new WebClassPathItemProvider(getElementName(classPathProperty)));
    }

    // used by ClassPathExtender
    protected boolean handleRoots(URL[] classPathRoots, String classPathProperty, String elementName,
            Operation operation) throws IOException, UnsupportedOperationException {
        return classPathModifierSupport.handleRoots(classPathRoots, classPathProperty, operation,
                new WebClassPathItemProvider(elementName));
    }

    @Override
    protected boolean removeAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements, classPathProperty,
                Operation.REMOVE, new WebClassPathItemProvider(getElementName(classPathProperty)));
    }

    @Override
    protected boolean addAntArtifacts(final AntArtifact[] artifacts, final URI[] artifactElements,
            final SourceGroup sourceGroup, final String type) throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return classPathModifierSupport.handleAntArtifacts(artifacts, artifactElements, classPathProperty,
                Operation.ADD, new WebClassPathItemProvider(getElementName(classPathProperty)));
    }

    @Override
    protected boolean removeLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return handleLibraries(libraries, classPathProperty, getElementName(classPathProperty), Operation.REMOVE);
    }

    @Override
    protected boolean addLibraries(final Library[] libraries, final SourceGroup sourceGroup, final String type)
            throws IOException, UnsupportedOperationException {
        String classPathProperty = classPathModifierSupport.getClassPathProperty(sourceGroup, type);
        return handleLibraries(libraries, classPathProperty, getElementName(classPathProperty), Operation.ADD);
    }

    protected boolean handleLibraries(final Library[] libraries, final String classPathProperty,
            final String elementName, final Operation operation) throws IOException,
            UnsupportedOperationException {
        List<WebClassPathSupport.Item> items = new ArrayList<WebClassPathSupport.Item>(libraries.length);
        for (Library library : libraries) {
            items.add(WebClassPathSupport.Item.create(library, null, WebClassPathSupport.Item.PATH_IN_WAR_LIB));
        }
        return handleLibraryClassPathItems(items, classPathProperty, elementName, operation, true);
    }

    public boolean handleLibraryClassPathItems(final List<WebClassPathSupport.Item> items,
            final String classPathProperty, final String elementName, final Operation operation,
            final boolean saveProject) throws IOException {
        assert items != null : "Libraries cannot be null";
        assert classPathProperty != null;

        // if the caller doesn't wish to save the project, it is expected to do it later,
        // in which case it must have PM.mutex() write access to avoid race conditions
        assert saveProject || ProjectManager.mutex().isWriteAccess();
        try {
            dontFireChange = true;
            unregisterLibraryListeners();
            return ProjectManager.mutex().writeAccess(
                new Mutex.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException {
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        String raw = props.getProperty(classPathProperty);
                        List<WebClassPathSupport.Item> resources = classPathSupport.itemsList(raw,
                                elementName);
                        List<WebClassPathSupport.Item> changed = new ArrayList<WebClassPathSupport.Item>(items.size());
                        for (WebClassPathSupport.Item item : items) {
                            assert item != null;
                            assert item.getType() == ClassPathItem.Type.LIBRARY;

                            if (operation == Operation.ADD && !resources.contains(item)) {
                                resources.add(item);
                                changed.add(item);
                            } else if (operation == Operation.REMOVE && resources.contains(item)) {
                                resources.remove(item);
                                changed.add(item);
                            }
                        }
                        if (!changed.isEmpty()) {
                            String[] itemRefs = classPathSupport.encodeToStrings(resources, elementName);
                            // PathParser may change the EditableProperties
                            props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.setProperty(classPathProperty, itemRefs);
                            if (operation == Operation.ADD) {
                                for (WebClassPathSupport.Item item : changed) {
                                    String prop = classPathSupport.getLibraryReference(item);
                                    // XXX make a PropertyUtils method for this!
                                    prop = prop.substring(2, prop.length() - 1);
                                    ClassPathSupport.relativizeLibraryClassPath(props,
                                            helper.getAntProjectHelper(), prop);
                                }
                            }
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            // update lib references in private properties
                            EditableProperties privateProps = helper.getProperties(
                                    AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            List<WebClassPathSupport.Item> l = new ArrayList<WebClassPathSupport.Item>();
                            l.addAll(resources);
                            l.addAll(classPathSupport.itemsList(props.getProperty(WAR_CONTENT_ADDITIONAL),
                                    TAG_WEB_MODULE_ADDITIONAL_LIBRARIES));
                            storeLibrariesLocations(l.iterator(), privateProps);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                            if (saveProject) {
                                ProjectManager.getDefault().saveProject(project);
                            }
                            registerLibraryListeners(props);
                            dontFireChange = false;
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

    private void unregisterLibraryListeners() {
        Library[] libs = LibraryManager.getDefault().getLibraries();
        for (Library library : libs) {
            library.removePropertyChangeListener(listener);
        }
    }

    private void registerLibraryListeners() {
        // reread the properties, PathParser changes them
        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        registerLibraryListeners(props);
    }

    private void registerLibraryListeners(EditableProperties props) {
        unregisterLibraryListeners();
        Set<WebClassPathSupport.Item> set = new HashSet<WebClassPathSupport.Item>();
        set.addAll(classPathSupport.itemsList(props.getProperty(JAVAC_CLASSPATH), TAG_WEB_MODULE_LIBRARIES));
        set.addAll(classPathSupport.itemsList(props.getProperty(WAR_CONTENT_ADDITIONAL),
                TAG_WEB_MODULE_ADDITIONAL_LIBRARIES));
        for (WebClassPathSupport.Item item : set) {
            if (item.getType() == ClassPathItem.Type.LIBRARY && !item.isBroken()) {
                item.getLibrary().addPropertyChangeListener(listener);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (projectDeleted) {
            return;
        }
        if (dontFireChange) {
            return;
        }

        if (e.getSource().equals(eval) && (e.getPropertyName().equals(JAVAC_CLASSPATH)
                || e.getPropertyName().equals(WAR_CONTENT_ADDITIONAL))) {
            // reread the properties, PathParser changes them
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String javacCp = props.getProperty(JAVAC_CLASSPATH);
            if (javacCp != null) {
                registerLibraryListeners(props);
                if (ProjectManager.getDefault().isValid(project)) {
                    storeLibLocations();
                }
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
                        EditableProperties privateProps = helper.getProperties(
                                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                        List<WebClassPathSupport.Item> wmLibs = classPathSupport.itemsList(
                                props.getProperty(JAVAC_CLASSPATH), TAG_WEB_MODULE_LIBRARIES);
                        List<WebClassPathSupport.Item> additionalLibs = classPathSupport.itemsList(
                                props.getProperty(WAR_CONTENT_ADDITIONAL), TAG_WEB_MODULE_ADDITIONAL_LIBRARIES);
                        classPathSupport.encodeToStrings(wmLibs, TAG_WEB_MODULE_LIBRARIES);
                        classPathSupport.encodeToStrings(additionalLibs, TAG_WEB_MODULE_ADDITIONAL_LIBRARIES);
                        Set<WebClassPathSupport.Item> set = new HashSet<WebClassPathSupport.Item>();
                        set.addAll(wmLibs);
                        set.addAll(additionalLibs);
                        storeLibrariesLocations(set.iterator(), privateProps);
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
        eval.removePropertyChangeListener(listener);
    }

    // #123223
    /**
     * We have to decide whether update project.xml file as well or not; project.xml file is updated if
     * and only if the classpath property is "javac.classpath".
     */
    private String getElementName(String classpathProperty) {
        if (JAVAC_CLASSPATH.equals(classpathProperty)) {
            return DEFAULT_WEB_MODULE_ELEMENT_NAME;
        }
        return null;
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
    public static void storeLibrariesLocations(Iterator<WebClassPathSupport.Item> classpath,
            EditableProperties privateProps) {
        List<String> exLibs = new ArrayList<String>();
        for (String key : privateProps.keySet()) {
            if (key.endsWith(".libdirs") || key.endsWith(".libfiles") || // NOI18N
                    (key.indexOf(".libdir.") > 0) || (key.indexOf(".libfile.") > 0)) { // NOI18N
                exLibs.add(key);
            }
        }
        while (classpath.hasNext()) {
            WebClassPathSupport.Item item = classpath.next();
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            WebClassPathSupport.getFilesForItem(item, files, dirs);
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
