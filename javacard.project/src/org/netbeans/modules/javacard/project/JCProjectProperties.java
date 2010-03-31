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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider.Receiver;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;
import org.openide.util.Cancellable;

/**
 *
 * @author Tim Boudreau
 */
public class JCProjectProperties implements PlatformAndDeviceProvider {

    protected final JCProject project;
    protected String platformName = "";
    protected String activeDevice = JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME;
    public DefaultTableModel SOURCE_ROOTS_MODEL;
    private SourceLevelComboBoxModel JAVAC_SOURCE_MODEL;
    private String sourceEncoding;
    private String javacSourceLevel;
    private String javacTargetLevel;
    public DefaultListModel classPathModel;
    public ToggleButtonModel COMPILE_ON_SAVE_BUTTON_MODEL;
    public Document ADDITIONAL_COMPILER_OPTIONS_DOCUMENT;
    public Document KEYSTORE_ALIAS_DOCUMENT;
    public Document KEYSTORE_PASSWORD_DOCUMENT;
    public Document KEYSTORE_ALIAS_PASSWORD_DOCUMENT;
    public ToggleButtonModel GENERATE_DEBUG_INFO_BUTTON_MODEL;
    public ToggleButtonModel ENABLE_DEPRECATION_BUTTON_MODEL;
    public ToggleButtonModel SIGN_JAR_BUTTON_MODEL;
    public Document KEYSTORE_DOCUMENT;
    private JCStoreGroup group = new JCStoreGroup();
    private StoreGroup passwordsGroup = new StoreGroup();

    public JCProjectProperties(JCProject project) {
        this.project = project;
        //will be null if we're using a dummy properties to hold values
        //for a new project wizard (SelectPlatormAndDevicePanel needs it)
        if (project != null) {
            init(project);
        }
    }

    /**
     * For use in the new wizard, where we reuse panels also used in the
     * project properties dialog - creates a fake project properties
     * to store values in (but most methods must not be called).
     */
    public JCProjectProperties() {
        this(null);
    }

    private void init(JCProject project) {
        SOURCE_ROOTS_MODEL = SourceRootsUi.createModel(project.getRoots());
        PropertyEvaluator eval = project.evaluator();
        COMPILE_ON_SAVE_BUTTON_MODEL = group.createToggleButtonModel(eval, ProjectPropertyNames.PROJECT_PROP_COMPILE_ON_SAVE);
        ADDITIONAL_COMPILER_OPTIONS_DOCUMENT = group.createStringDocument(eval, ProjectPropertyNames.PROJECT_PROP_JAVAC_ADDITIONAL_ARGS);
        GENERATE_DEBUG_INFO_BUTTON_MODEL = group.createToggleButtonModel(eval, ProjectPropertyNames.PROJECT_PROP_JAVAC_DEBUG);
        ENABLE_DEPRECATION_BUTTON_MODEL = group.createToggleButtonModel(eval, ProjectPropertyNames.PROJECT_PROP_JAVAC_DEPRECATION);
        SIGN_JAR_BUTTON_MODEL = group.createToggleButtonModel(eval, ProjectPropertyNames.PROJECT_PROP_SIGN_JAR);
        KEYSTORE_DOCUMENT = group.createResolvingDocument(eval,
                ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PATH,
                JavacardPlatformKeyNames.PLATFORM_HOME,
                JavacardPlatformKeyNames.PLATFORM_RI_HOME,
                "basedir"); //NOI18N
        KEYSTORE_ALIAS_DOCUMENT = group.createStringDocument (eval, ProjectPropertyNames.PROJECT_PROP_KEYSTORE_ALIAS);
        KEYSTORE_PASSWORD_DOCUMENT = passwordsGroup.createStringDocument (eval, ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PASSWORD);
        KEYSTORE_ALIAS_PASSWORD_DOCUMENT = passwordsGroup.createStringDocument (eval, ProjectPropertyNames.PROJECT_PROP_KEYSTORE_ALIAS_PASSWORD);
        sourceEncoding = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING);
        javacSourceLevel = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_SOURCE);
        javacTargetLevel = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_JAVAC_TARGET);
        activeDevice = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
        platformName = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        if (platformName == null) {
            platformName = JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME;
        }
        if (activeDevice == null) {
//            activeDevice = PlatformTemplateWizardKeys.PROJECT_TEMPLATE_DEVICE_NAME_KEY;
            activeDevice = "Default Device";
        }
        if (javacSourceLevel == null) {
            javacSourceLevel = "1.6"; //NOI18N
        }
        if (javacTargetLevel == null) {
            javacTargetLevel = "1.6"; //NOI18N
        }
        if (sourceEncoding == null) {
            sourceEncoding = "UTF-8"; //NOI18N
        }

        JAVAC_SOURCE_MODEL = new SourceLevelComboBoxModel(this, javacSourceLevel,
                javacTargetLevel, new SpecificationVersion("1.3"));

        classPathModel = new DefaultListModel();
        String str = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
        if (str == null) {
            str = "";
        }
        for (String s : str.split(File.pathSeparator)) {
            classPathModel.addElement(s);
        }
    }

    public JComboBox createSourceLevelComboBox() {
        JComboBox box = new JComboBox(JAVAC_SOURCE_MODEL);
        box.setRenderer(new SourceLevelComboBoxModel.SourceLevelListCellRenderer());
        return box;
    }

    public String getJavacSourceLevel() {
        return javacSourceLevel;
    }

    public void setJavacSourceLevel(String javacSourceLevel) {
        this.javacSourceLevel = javacSourceLevel;
    }

    public String getJavacTargetLevel() {
        return javacTargetLevel;
    }

    public void setJavacTargetLevel(String javacTargetLevel) {
        this.javacTargetLevel = javacTargetLevel;
    }

    public JCProject getProject() {
        return project;
    }

    public String getActiveDevice() {
        return activeDevice;
    }

    public void setActiveDevice(String val) {
        activeDevice = val;
    }

    public void setSourceEncoding(String encName) {
        this.sourceEncoding = encName;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public final void storeProperties() {
        try {
            onBeforeStoreProperties();
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction<Boolean>() {

                        @Override
                        public Boolean run() throws IOException {
                            EditableProperties props =
                                    project.getAntProjectHelper().
                                    getProperties(
                                    AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            EditableProperties priv =
                                    project.getAntProjectHelper().
                                    getProperties(
                                    AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            storeCommonProperties(props, priv);
                            Boolean result = onStoreProperties(props);
                            if (result) {
                                project.getAntProjectHelper().
                                        putProperties(
                                        AntProjectHelper.PROJECT_PROPERTIES_PATH,
                                        props);
                                project.getAntProjectHelper().
                                        putProperties(
                                        AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                                        priv);

                            }
                            storeDependencies();
                            return result;
                        }
                    });

        } catch (MutexException me) {
            Exceptions.printStackTrace(me);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    private void storeDependencies() throws IOException {
        ResolvedDependencies d;
        synchronized (this) {
            d = this.deps;
        }
        if (d != null && d.isModified()) {
            d.save();
            legacyStoreDependencies(d);
        }
    }

    protected Boolean onStoreProperties(EditableProperties props) throws IOException {
        //do nothing
        return true;
    }

    /**
     * Called before entering ProjectManager.mutex()
     * @throws java.io.IOException
     */
    protected void onBeforeStoreProperties() throws IOException {
    }

    private void storeCommonProperties(EditableProperties props, EditableProperties priv) throws IOException {
        props.put(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING, sourceEncoding);
        props.put(ProjectPropertyNames.PROJECT_PROP_JAVAC_SOURCE, javacSourceLevel);
        props.put(ProjectPropertyNames.PROJECT_PROP_JAVAC_TARGET, javacTargetLevel);
        props.put(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE, activeDevice);
        props.put(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM, platformName);
        StringBuilder sb = new StringBuilder();
        for (Object o : classPathModel.toArray()) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(o);
        }
        props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH, sb.toString());
        passwordsGroup.store(priv);
        storeRoots(project.getRoots(), SOURCE_ROOTS_MODEL, props);
        group.store(props);
        if (!SIGN_JAR_BUTTON_MODEL.isSelected()) {
            props.remove(ProjectPropertyNames.PROJECT_PROP_SIGN_JAR);
        }
    }

    @SuppressWarnings("unchecked") //NOI18N
    private void storeRoots(SourceRoots roots, DefaultTableModel tableModel, EditableProperties props) throws MalformedURLException {
        Vector<?> data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String[] rootLabels = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            Vector<?> v = (Vector<?>) data.elementAt(i);
            File f = (File) v.elementAt(0);
            rootURLs[i] = getRootURL(f, null);
            rootLabels[i] = (String) v.elementAt(1);
        }
        roots.putRoots(rootURLs, rootLabels);
        String[] rootPropertyNames = roots.getRootProperties();
        FileObject[] rootFolders = roots.getRoots();
        //rootPropertyNames will contain anything starting with "src." - this can include unrelated stuff
        //key off rootFolders, not rootPropertyNames
        List <Integer> foreignRootIndices = new LinkedList<Integer>();
        FileObject projectDir = project.getProjectDirectory();
        for (int i=0; i < rootFolders.length; i++) {
            String rootProp = rootPropertyNames[i];
            if (FileUtil.isParentOf(projectDir, rootFolders[i])) {
                String relPath = FileUtil.getRelativePath(projectDir, rootFolders[i]);
                props.setProperty(rootProp, relPath);
            } else {
                foreignRootIndices.add (i);
            }
        }
        
        if (!foreignRootIndices.isEmpty()) {
            EditableProperties privProps = project.getAntProjectHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ReferenceHelper rh = project.getReferenceHelper();
            for (int i : foreignRootIndices) {
                String rootProp = rootPropertyNames[i];
                File file = FileUtil.toFile (rootFolders[i]);
                String val = rh.createForeignFileReference(file,
                        JavaProjectConstants.SOURCES_TYPE_JAVA);
                privProps.put(rootProp, val);
            }
            project.getAntProjectHelper().putProperties(
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                    privProps);
        }
    }

    public static URL getRootURL(File root, String offset) throws MalformedURLException {
        //copied from J2SEProjectUtil
        URL url = FileUtil.urlForArchiveOrDir(root);
        if (url == null) {
            throw new IllegalArgumentException(root.getAbsolutePath());
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }

    public final DefaultListModel getClassPathModel() {
        return classPathModel;
    }

    public final String getPlatformName() {
        return platformName;
    }

    public final JavacardPlatform getPlatform() {
        return platformName == null ? null : JCUtil.findPlatformNamed(platformName);
    }

    public void setPlatformName(String platformName) {
        if (!platformName.equals(this.platformName)) {
            this.platformName = platformName;
            if (JAVAC_SOURCE_MODEL != null) {
                JAVAC_SOURCE_MODEL.platformChanged();
            }
        }
    }

    /**
     * Get the dependencies for the project, resolving all libraries and
     * projects.  Since this involves extensive I/O, reading project.xml
     * and project.properties and looking up files and projects to match
     * them, this method is asynchronous.  You pass in a receiver;  the I/O
     * will be done on a background thread, and when completed, the receiver
     * will be passed the resulting dependencies.
     * <p/>
     * After the first call, the dependencies are held permanently for the
     * lifetime of this object (so they may be modified, checked for modifications
     * and saved if necessary).  Subsequent calls will result in synchronous
     * calls to the passed receiver, passing in the dependencies in question.
     * <p/>
     * Note:  As noted in the Javadoc for ResolvedDependencies, it is not
     * a good idea to hold a reference to an instance of ResolvedDependencies
     * unless you need to because it may be modified.  It holds references
     * to projects, etc. which take up memory.
     *
     * @param r An object which will be passed the resolved dependencies
     * once they have been fully resolved
     * @return A Cancellable if the dependencies have not been fetched yet;
     * otherwise it will invoke r.receive() with the previously stored
     * dependencies and return null.
     */
    public Cancellable getDependencies(DependenciesProvider.Receiver r) {
        ResolvedDependencies d = null;
        synchronized (this) {
            if (deps != null) {
                d = deps;
            }
        }
        if (d != null) {
            r.receive(d);
            return null;
        }
        DependenciesProvider p = project.getLookup().lookup(DependenciesProvider.class);
        if (p != null) {
            R receiver = new R(r);
            return p.requestDependencies(receiver);
        }
        return null;
    }

    ResolvedDependencies deps;

    @Deprecated
    private void legacyStoreDependencies(ResolvedDependencies d) {
        //XXX DELETE THIS ONCE BUILD SCRIPT IS UPDATED.  THIS IS ONLY
        //SO THAT EXISTING BUILDS WITH DEPEDENCIES WILL WORK WITH THE NEW
        //METADATA USING THE OLD build-impl.xsl that looks for a hardcoded
        //class.path property
        StringBuilder sb = new StringBuilder();
        for (ResolvedDependency dep : d.all()) {
            String path = dep.getPath(ArtifactKind.ORIGIN);
            if (path != null) {
                if (sb.length() > 0) {
                    sb.append (File.pathSeparatorChar);
                    sb.append (path);
                }
            }
        }
        EditableProperties p = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        p.put("class.path", sb.toString()); //NOI18N
    }

    private class R implements DependenciesProvider.Receiver {
        private final Receiver r;

        private R(Receiver r) {
            this.r = r;
        }
        public void receive (ResolvedDependencies deps) {
            ResolvedDependencies d = deps;
            synchronized (JCProjectProperties.this) {
                if (JCProjectProperties.this.deps == null) {
                    d = JCProjectProperties.this.deps = deps;
                } else {
                    d = JCProjectProperties.this.deps;
                }
            }
            r.receive(d);
        }

        public boolean failed (Throwable failure) {
            return r.failed(failure);
        }
    }
}
