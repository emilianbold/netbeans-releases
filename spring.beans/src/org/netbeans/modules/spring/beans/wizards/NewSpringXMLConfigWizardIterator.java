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
 *
 * Portions Copyrighted 2008 Craig MacKay.
 */

package org.netbeans.modules.spring.beans.wizards;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.ProjectSpringScopeProvider;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;

public final class NewSpringXMLConfigWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project p = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            ConfigFileManager manager = getConfigFileManager(p);
            List<ConfigFileGroup> configFileGroups = manager != null ? manager.getConfigFileGroups() : null;
            SpringXMLConfigGroupPanel configGroupPanel = configFileGroups != null && !configFileGroups.isEmpty() ? new SpringXMLConfigGroupPanel(configFileGroups) : null;
            WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups, configGroupPanel);

            panels = new WizardDescriptor.Panel[] {
                targetChooser,
                new SpringXMLConfigNamespacesPanel(),
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    public Set instantiate() throws IOException {
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final String targetName = Templates.getTargetName(wizard);

        final FileObject[] createdFile = { null };

        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {

            public void run() throws IOException {
                createdFile[0] = targetFolder.createData(targetName, Templates.getTemplate(wizard).getExt());
                String[] incNamespaces = (String[]) wizard.getProperty(SpringXMLConfigNamespacesPanel.INCLUDED_NAMESPACES);
                generateFileContents(createdFile[0], incNamespaces);
            }
        });
        boolean addSpringToClassPath = (Boolean) wizard.getProperty(SpringXMLConfigNamespacesPanel.ADD_SPRING_TO_CLASSPATH);
        if (addSpringToClassPath) {
            Library[] libraries = { (Library) wizard.getProperty(SpringXMLConfigNamespacesPanel.SPRING_LIBRARY) };
            addLibrariesToClassPath(libraries);
        }
        
        @SuppressWarnings("unchecked")
        Set<ConfigFileGroup> selectedGroups = (Set<ConfigFileGroup>) wizard.getProperty(SpringXMLConfigGroupPanel.CONFIG_FILE_GROUPS);
        addFileToConfigFileManager(selectedGroups != null ? selectedGroups : Collections.<ConfigFileGroup>emptySet(), FileUtil.toFile(createdFile[0]));
        
        return Collections.singleton(createdFile[0]);
    }
    
    private void addLibrariesToClassPath(Library[] libraries) throws IOException {
        FileObject artifact = getSourceGroupArtifact(Templates.getProject(wizard), Templates.getTargetFolder(wizard));
        if (artifact != null) {
            ProjectClassPathModifier.addLibraries(libraries, artifact, ClassPath.COMPILE);
        }
    }
    
    private void addFileToConfigFileManager(final Set<ConfigFileGroup> selectedGroups, final File file) throws IOException {
        final ConfigFileManager manager = getConfigFileManager(Templates.getProject(wizard));
        try {
            manager.mutex().writeAccess(new ExceptionAction<Void>() {
                public Void  run() throws IOException {
                    List<File> origFiles = manager.getConfigFiles();
                    List<File> newFiles = new ArrayList<File>(origFiles);
                    newFiles.add(file);
                    List<ConfigFileGroup> origGroups = manager.getConfigFileGroups();
                    List<ConfigFileGroup> newGroups = null;
                    if (selectedGroups.size() > 0) {
                        newGroups = new ArrayList<ConfigFileGroup>(origGroups.size());
                        for (ConfigFileGroup group : origGroups) {
                            if (selectedGroups.contains(group)) {
                                ConfigFileGroup newGroup = addFileToConfigGroup(group, file);
                                newGroups.add(newGroup);
                            } else {
                                newGroups.add(group);
                            }
                        }
                    } else {
                        newGroups = origGroups;
                    }
                    manager.putConfigFilesAndGroups(newFiles, newGroups);
                    manager.save();
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    private ConfigFileGroup addFileToConfigGroup(ConfigFileGroup group, File file) {
        List<File> files = group.getFiles();
        files.add(file);
        return ConfigFileGroup.create(group.getName(), files);
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (Templates.getTargetFolder(wizard) == null) {
            Project project = Templates.getProject(wizard);
            SpringConfigFileLocationProvider provider = project != null ? project.getLookup().lookup(SpringConfigFileLocationProvider.class) : null;
            FileObject location = provider != null ? provider.getLocation() : null;
            if (location != null) {
                Templates.setTargetFolder(wizard, location);
            }
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length; // NOI18N
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    private void generateFileContents(final FileObject targetFile, String[] incNamespaces) {
        StringBuilder sb = generateXML(incNamespaces);

        try {
            Class<?> kitClass = CloneableEditorSupport.getEditorKit(SpringConstants.CONFIG_MIME_TYPE).getClass();
            final BaseDocument doc = new BaseDocument(true, SpringConstants.CONFIG_MIME_TYPE);
            final Reformat reformat = Reformat.get(doc);
            
            doc.remove(0, doc.getLength());
            doc.insertString(0, sb.toString(), null);
            reformat.lock();
            try {
                doc.runAtomic(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } finally {
                reformat.unlock();
            }
            
            sb.replace(0, sb.length(), doc.getText(0, doc.getLength()));
            final String text = sb.toString();

            FileLock lock = targetFile.lock();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        targetFile.getOutputStream(lock)));
                bw.write(text);
                bw.close();
            } finally {
                lock.releaseLock();
            }
        } catch (FileAlreadyLockedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private StringBuilder generateXML(String[] incNamespaces) {
        String sep = System.getProperty("line.separator"); // NOI18N
        StringBuilder schemaLoc = new StringBuilder();
        schemaLoc.append("       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd"); // NOI18N

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(sep); // NOI18N
        sb.append("<beans xmlns=\"http://www.springframework.org/schema/beans\"").append(sep); // NOI18N
        sb.append("       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"").append(sep); // NOI18N

        for (String cur : incNamespaces) {
            String prefix = cur.substring(0, cur.indexOf("-")).trim(); // NOI18N
            String schemaName = cur.substring(cur.indexOf("-") + 1).trim(); // NOI18N
            if(!schemaName.equals("http://www.springframework.org/schema/p")) { // NOI18N
                String namespace = schemaName.substring(0, schemaName.lastIndexOf("/")); // NOI18N
                sb.append("       xmlns:").append(prefix).append("=\"").append(namespace).append("\"").append(sep); // NOI18N
                schemaLoc.append(sep);
                schemaLoc.append("       ").append(namespace).append(" ").append(schemaName); // NOI18N
            } else {
                sb.append("       xmlns:").append(prefix).append("=\"").append(schemaName).append("\"").append(sep); // NOI18N
            }
        }

        sb.append(schemaLoc).append("\""); // NOI18N
        sb.append(">").append(sep).append("    ").append(sep); // NOI18N
        sb.append("</beans>"); // NOI18N

        return sb;
    }
    
    static ConfigFileManager getConfigFileManager(Project p) {
        ProjectSpringScopeProvider scopeProvider = p.getLookup().lookup(ProjectSpringScopeProvider.class);
        return scopeProvider != null ?  scopeProvider.getSpringScope().getConfigFileManager() : null;
    }
    
    static FileObject getSourceGroupArtifact(Project project, FileObject preferredArtifact) {
        SourceGroup[] groups = SourceGroups.getJavaSourceGroups(project);
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            if (preferredArtifact.equals(root) || (FileUtil.isParentOf(root, preferredArtifact) && group.contains(preferredArtifact))) {
                return preferredArtifact;
            }
        }
        // Otherwise just get the first source group.
        for (SourceGroup group : groups) {
            return group.getRootFolder();
        }
        return null;
    }
}
