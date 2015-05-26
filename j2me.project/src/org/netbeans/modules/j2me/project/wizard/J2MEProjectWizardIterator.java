/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.j2me.project.api.J2MEProjectBuilder;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Roman Svitanic
 */
public class J2MEProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    public static enum WizardType { APPLICATION, EXISTING, SAMPLE };

    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    private static final String MANIFEST_FILE = "manifest.mf"; //NOI18N
    static final String MIDLET_CLASS = "mainClass"; // NOI18N
    static final String SHARED_LIBRARIES = "sharedLibraries"; // NOI18N
    static final String JDK_PLATFORM = "jdk"; //NOI18N
    static final String PLATFORM = "platform"; //NOI18N
    public static final String DEVICE = "device"; //NOI18N
    static final String CONFIGURATION = "config"; //NOI18N
    static final String PROFILE = "profile"; //NOI18N
    static final String OPTIONAL_API = "optionalApi"; //NOI18N
    static final String PLATFORM_BOOTCLASSPATH = "bootclasspath"; //NOI18N
    private static final long serialVersionUID = 1L;
    private final WizardType type;

    private J2MEProjectWizardIterator(WizardType type) {
        this.type = type;
    }

    @TemplateRegistration(folder = "Project/JavaME", position = 100, displayName = "#template_app", iconBase = "org/netbeans/modules/j2me/project/ui/resources/j2meProject.gif", description = "../ui/resources/emptyProject.html")
    @NbBundle.Messages("template_app=Java ME Embedded Application")
    public static J2MEProjectWizardIterator application() {
        return new J2MEProjectWizardIterator(WizardType.APPLICATION);
    }

    @Override
    public Set<?> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(4);
        Set<FileObject> resultSet = new HashSet<>();
        File dirF = (File) wiz.getProperty("projdir"); //NOI18N
        if (dirF == null) {
            throw new NullPointerException("projdir == null, props:" + wiz.getProperties()); //NOI18N
        }
        dirF = FileUtil.normalizeFile(dirF);
        String name = (String) wiz.getProperty("name"); //NOI18N
        String midletClass = (String) wiz.getProperty(MIDLET_CLASS); //NOI18N
        String librariesDefinition = (String) wiz.getProperty(SHARED_LIBRARIES);
        JavaPlatform platform = (JavaPlatform) wiz.getProperty(PLATFORM);
        String configuration = (String) wiz.getProperty(CONFIGURATION);
        String profile = (String) wiz.getProperty(PROFILE);
        String device = (String) wiz.getProperty(DEVICE);
        JavaPlatform jdk = (JavaPlatform) wiz.getProperty(JDK_PLATFORM);
        String optionalApi = (String) wiz.getProperty(OPTIONAL_API);
        String bootclasspath = (String) wiz.getProperty(PLATFORM_BOOTCLASSPATH);
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME;
        }
        // put J2ME-specific project properties
        Map<String, String> props = new HashMap<>();
        props.put(J2MEProjectProperties.PROP_RUN_METHOD, "STANDARD"); //NOI18N
        props.put(J2MEProjectProperties.PROP_DEBUGGER_TIMEOUT, "30000"); //NOI18N
        props.put(J2MEProjectProperties.PROP_PLATFORM_CONFIGURATION, configuration);
        props.put(J2MEProjectProperties.PROP_PLATFORM_PROFILE, profile);
        props.put(J2MEProjectProperties.PROP_PLATFORM_TYPE, ((J2MEPlatform) platform).getType());
        props.put(J2MEProjectProperties.PROP_PLATFORM_DEVICE, device);
        props.put(J2MEProjectProperties.PROP_PLATFORM_APIS, optionalApi);
        props.put(J2MEProjectProperties.PROP_PLATFORM_BOOTCLASSPATH, bootclasspath);
        props.put("manifest.others", "MIDlet-Vendor: " + System.getProperty("user.name") + "\nMIDlet-Name: " + name + "\nMIDlet-Version: 1.0\n"); //NOI18N
        if (midletClass != null && !midletClass.isEmpty()) {
            props.put("manifest.midlets", "MIDlet-1: " + name + ",," + midletClass + "\n"); //NOI18N
        }
        // end of J2ME-specific properties
        handle.progress(NbBundle.getMessage(J2MEProjectWizardIterator.class, "LBL_NewJ2MEProjectWizardIterator_WizardProgress_CreatingProject"), 1); //NOI18N
        switch (type) {
            default:
                String midletTemplate = "Templates/j2me/MEEPlet.java"; //NOI18N
                AntProjectHelper h = J2MEProjectBuilder.forDirectory(dirF, name, platform).
                        addDefaultSourceRoots().
                        setMainMIDLetName(midletClass).
                        setMainMIDLetTemplate(midletTemplate).
                        setLibrariesDefinitionFile(librariesDefinition).
                        setSDKPlatform(jdk).
                        addCustomProjectProperties(props).
                        build();
                handle.progress(2);
                if (midletClass != null && !midletClass.isEmpty()) {
                    final FileObject sourcesRoot = h.getProjectDirectory().getFileObject("src"); //NOI18N
                    if (sourcesRoot != null) {
                        final FileObject midletFo = getClassFO(sourcesRoot, midletClass);
                        if (midletFo != null) {
                            resultSet.add(midletFo);
                        }
                    }
                }
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        switch (type) {
            case APPLICATION:
                createManifest(dir, false);
                break;
        }
        handle.progress(3);

        // Returning FileObject of project diretory.
        // Project will be open and set as main
        final Integer ind = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        if (ind != null) {
            switch (type) {
                case APPLICATION:
                    WizardSettings.setNewApplicationCount(ind);
                    break;
            }
        }
        resultSet.add(dir);
        handle.progress(NbBundle.getMessage(J2MEProjectWizardIterator.class, "LBL_NewJ2MEProjectWizardIterator_WizardProgress_PreparingToOpen"), 4); //NOI18N
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder(dirF);
        }

        SharableLibrariesUtils.setLastProjectSharable(librariesDefinition != null);
        return resultSet;
    }
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;
        panels = createPanels();
        String[] steps = createSteps();

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]); //NOI18N
        this.wiz.putProperty("testRoot", new File[0]); //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir", null); //NOI18N
            this.wiz.putProperty("name", null); //NOI18N
            this.wiz.putProperty(MIDLET_CLASS, null);
            this.wiz.putProperty(SHARED_LIBRARIES, null);
            this.wiz.putProperty(JDK_PLATFORM, null);
            this.wiz.putProperty(PLATFORM, null);
            this.wiz.putProperty(DEVICE, null);
            this.wiz.putProperty(CONFIGURATION, null);
            this.wiz.putProperty(PROFILE, null);
            this.wiz.putProperty(OPTIONAL_API, null);
            this.wiz.putProperty(PLATFORM_BOOTCLASSPATH, null);
            switch (type) {
                case EXISTING:
                    this.wiz.putProperty("sourceRoot", null); //NOI18N
                    this.wiz.putProperty("testRoot", null); //NOI18N
            }
            this.wiz = null;
            panels = null;
        }
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(J2MEProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length); //NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel[] createPanels() {
        switch (type) {
            default:
                return new WizardDescriptor.Panel[]{
                    new PanelConfigureProject(type)
                };
        }
    }

    private String[] createSteps() {
        switch (type) {
            default:
                return new String[]{
                    NbBundle.getMessage(J2MEProjectWizardIterator.class, "LAB_ConfigureProject") // NOI18N
                };
        }
    }

    private FileObject getClassFO(FileObject sourcesRoot, String className) {
        className = className.replace('.', '/'); // NOI18N
        return sourcesRoot.getFileObject(className + ".java"); // NOI18N
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     *
     * @param dir the directory to create it in
     * @throws IOException in case of problems
     */
    private static void createManifest(final FileObject dir, final boolean skipIfExists) throws IOException {
        if (skipIfExists && dir.getFileObject(MANIFEST_FILE) != null) {
            return;
        } else {
            FileObject manifest = dir.createData(MANIFEST_FILE);
            FileLock lock = manifest.lock();
            try {
                OutputStream os = manifest.getOutputStream(lock);
                try {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println("Manifest-Version: 1.0"); // NOI18N
                    pw.println(); // safest to end in \n\n due to JRE parsing bug
                    pw.flush();
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }
}
