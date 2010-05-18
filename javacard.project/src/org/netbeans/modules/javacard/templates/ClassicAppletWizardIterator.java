/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.templates;

import com.sun.javacard.AID;
import com.sun.javacard.filemodels.DeploymentXmlAppletEntry;
import com.sun.javacard.filemodels.DeploymentXmlInstanceEntry;
import com.sun.javacard.filemodels.DeploymentXmlModel;
import com.sun.javacard.filemodels.ParseErrorHandler;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.*;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.constants.FileWizardConstants;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.refactoring.AppletXMLRefactoringSupport;
import static org.netbeans.modules.javacard.templates.ClassicAppletDeploymentWizardPanel.PROP_APPLET_NAME;
import org.netbeans.modules.javacard.wizard.ProjectWizardIterator;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

import javax.swing.event.ChangeListener;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.javacard.spi.ProjectKind;

public final class ClassicAppletWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] panels;
    private boolean needsToCancel;
    private boolean isClassicAppletTemplate;
    private ProjectKind kind;

    private WizardDescriptor.Panel createJavaSourcePanel(
            WizardDescriptor wizardDescriptor) {

        Project project = Templates.getProject(wizardDescriptor);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert groups != null : "Cannot return null from Sources.getSourceGroups: " + sources;
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            return Templates.createSimpleTargetChooser(project, groups);
        } else {
            FakePanel pnl = new FakePanel();
            WizardDescriptor.Panel<WizardDescriptor> p = JavaTemplates.createPackageChooser(project, groups, pnl, true);
            return p;
        }
    }

    private static final class FakePanel implements WizardDescriptor.Panel<WizardDescriptor> {
        //Useless class required by JavaTemplates.createPackageChooser
        public Component getComponent() {
            return new JLabel("");
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void readSettings(WizardDescriptor settings) {
            //do nothing
        }

        public void storeSettings(WizardDescriptor settings) {
            //do nothing
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
            //do nothing
        }

        public void removeChangeListener(ChangeListener l) {
            //do nothing
        }
    }

    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<org.openide.WizardDescriptor>[] getPanels() {
        if (needsToCancel) {
            return new WizardDescriptor.Panel[]{
                        new ErrorPanel(NbBundle.getMessage(ClassicAppletWizardIterator.class,
                        isClassicAppletTemplate ? "CLASSIC_APPLET_APPLICATIONS" : //NOI18N
                            "EXTENDED_APPLET_APPLICATIONS")), //NOI18N
                    };
        }
        if (panels == null) {
            WizardDescriptor.Panel javaSourcePanel = createJavaSourcePanel(wizard);
            ClassicAppletDeploymentWizardPanel servletPanel = new ClassicAppletDeploymentWizardPanel(wizard);
            panels = new WizardDescriptor.Panel[]{
                        javaSourcePanel, servletPanel
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
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public Set instantiate() throws IOException {
        FileObject dir = Templates.getTargetFolder(wizard);
        String targetName = Templates.getTargetName(wizard);

        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wizard);

        FileObject createdFile = null;
        Project p = Templates.getProject(wizard);
        JCProject jcProject = p == null ? null : p.getLookup().lookup(JCProject.class);
        if (jcProject != null && dir != null) {
            boolean found = false;
            for (FileObject fo : jcProject.getSourceClassPath().getRoots()) {
                if (fo != null) { //???
                    if (fo.equals(dir)) {
                        throw new IOException (NbBundle.getMessage(ClassicAppletWizardIterator.class,
                                "ERR_NO_DEFAULT_PACKAGE")); //NOI18N
                    }
                    found |= FileUtil.isParentOf(fo, dir);
                }
            }
            if (jcProject.getProjectDirectory().equals(dir)) {
                throw new IOException (NbBundle.getMessage(ClassicAppletWizardIterator.class,
                        "ERR_NO_DEFAULT_PACKAGE")); //NOI18N
            }
            if (!found) {
                throw new IOException (NbBundle.getMessage(ClassicAppletWizardIterator.class,
                        "ERR_NOT_ON_CLASSPATH", dir.getName())); //NOI18N
            }
        }

        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, targetName);
        createdFile = dobj.getPrimaryFile();
        if (jcProject != null) {
            addInfoToWebDescriptor(createdFile, jcProject);
            createScriptFile(createdFile, jcProject);
            updatePropertiesToCreateInstance(createdFile, jcProject);
        }
        return Collections.singleton( createdFile );
    }

    private void updatePropertiesToCreateInstance(FileObject cls, JCProject jcProject) {
        if("false".equals(wizard.getProperty(FileWizardConstants.CREATE_INSTANCE))) {
            return;
        }
        EditableProperties props = jcProject.getAntProjectHelper().getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        jcProject.getAntProjectHelper().putProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
    }

    private void createScriptFile(FileObject cls, JCProject jcProject) {
        if("false".equals(wizard.getProperty(FileWizardConstants.CREATE_SCRIPT))) {
            return;
        }
        String clsName = cls.getName();
        File f = FileUtil.toFile(jcProject.getProjectDirectory());
        f = new File(f, JCConstants.SCRIPTS_DIR_PATH + clsName.toLowerCase() + ".scr");
        Utils.createAPDUScript(f, clsName, (String) wizard.getProperty(PROP_APPLET_NAME));
    }

    private void addInfoToWebDescriptor(final FileObject createdFile, final JCProject jcProject) {
        Project p = Templates.getProject(wizard);
        final String appletAid = (String) wizard.getProperty(PROP_APPLET_NAME);

        if (appletAid != null) {
            final String instanceAid = AID.parse(appletAid).increment().toString();
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                    try {
                        ClassPathProvider cpProvider = jcProject.getLookup().lookup(ClassPathProvider.class);
                        String className = createdFile.getName();
                        if (cpProvider != null) {
                            className = cpProvider.findClassPath(
                                    createdFile, ClassPath.SOURCE)
                                            .getResourceName(createdFile, '.', false);
                        }

                        FileObject webObj = jcProject.getProjectDirectory().getFileObject(JCConstants.APPLET_DESCRIPTOR_PATH);
                        AppletXMLRefactoringSupport web = AppletXMLRefactoringSupport.fromFile(FileUtil.toFile(webObj));
                        web.addAppletInfo(appletAid, className);
                        OutputStream out = webObj.getOutputStream();
                        XMLUtil.write(web.getDocument(), out, "UTF-8"); //NOI18N
                        out.close();
                        updateDeploymentXml(jcProject, appletAid, className, instanceAid, createdFile);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    }

    private void updateDeploymentXml(JCProject jcProject, String appletAid, String className, String instanceAid, FileObject createdFile) throws IOException {
        FileObject depl = jcProject.getProjectDirectory().getFileObject(JCConstants.DEPLOYMENT_XML_PATH);
        DeploymentXmlModel mdl;
        if (depl == null) {
            depl = FileUtil.createData(jcProject.getProjectDirectory(), JCConstants.DEPLOYMENT_XML_PATH);
            mdl = new DeploymentXmlModel();
        } else {
            InputStream in = depl.getInputStream();
            mdl = new DeploymentXmlModel(in, ParseErrorHandler.NULL);
            //The original will be closed to additions, so create
            //a new one
            DeploymentXmlModel nue = new DeploymentXmlModel();
            for (DeploymentXmlAppletEntry e : mdl.getData()) {
                nue.add((DeploymentXmlAppletEntry) e.clone());
            }
            mdl = nue;
        }
        DeploymentXmlAppletEntry toAdd = new DeploymentXmlAppletEntry();
        toAdd.setAppletAid(AID.parse(appletAid));
        toAdd.setClazzHint(className);
        toAdd.setDisplayNameHint(ProjectWizardIterator.unbicapitalize(createdFile.getName()));
        DeploymentXmlInstanceEntry instance = new DeploymentXmlInstanceEntry(AID.parse(instanceAid), "", mdl.getData().size());
        toAdd.add(instance);
        mdl.add (toAdd);
        mdl.close();
        FileLock lock = depl.lock();
        OutputStream outStream = depl.getOutputStream(lock);
        PrintWriter p = new PrintWriter(outStream);
        try {
            p.println(mdl.toXml());
        } finally {
            p.close();
            outStream.close();
            lock.releaseLock();
        }
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        Project project = Templates.getProject(wizard);
        FileObject template = Templates.getTemplate(wizard);
        isClassicAppletTemplate = "ClassicApplet".equals(template.getName());
        if (project != null) {
            JCProject jc = project.getLookup().lookup(JCProject.class);
            kind = jc == null ? null : jc.kind();
        }
        needsToCancel = (isClassicAppletTemplate && kind != ProjectKind.CLASSIC_APPLET) ||
                (!isClassicAppletTemplate && kind != ProjectKind.EXTENDED_APPLET) ||
                kind == null;
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (index == 0) {
            // Settings from 1st pannel (class name) are needed in 2nd panel
            panels[0].storeSettings(wizard);
        }
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
        //do nothing
    }

    public void removeChangeListener(ChangeListener l) {
        //do nothing
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
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
}
