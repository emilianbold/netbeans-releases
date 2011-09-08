/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.newproject;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import static org.netbeans.modules.maven.j2ee.newproject.Bundle.*;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 *
 *@author Dafe Simonek
 */
@TemplateRegistration(folder = "Project/Maven2", position = 270, displayName = "#template.EA", iconBase = "org/netbeans/modules/maven/j2ee/ear/maven_enterprise_application_16.png", description = "EADescription.html")
@Messages("template.EA=Enterprise Application")
public class EAWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator {
    
    static final Archetype[] WEB_APP_ARCHS;
    static final Archetype[] EJB_ARCHS;
    static final Archetype[] EAR_ARCHS;
    static final Archetype[] APPCLIENT_ARCHS;
    static final Archetype EA_ARCH;

    static {
        WEB_APP_ARCHS = new Archetype[3];

        Archetype arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("webapp-javaee6"); //NOI18N
        WEB_APP_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("webapp-jee5"); //NOI18N
        WEB_APP_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("webapp-j2ee14"); //NOI18N
        WEB_APP_ARCHS[2] = arch;

        EJB_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("ejb-javaee6"); //NOI18N
        EJB_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("ejb-jee5"); //NOI18N
        EJB_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.3"); //NOI18N
        arch.setArtifactId("ejb-j2ee14"); //NOI18N
        EJB_ARCHS[2] = arch;

        APPCLIENT_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0"); //NOI18N
        arch.setArtifactId("appclient-javaee6"); //NOI18N
        APPCLIENT_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.0"); //NOI18N
        arch.setArtifactId("appclient-jee5"); //NOI18N
        APPCLIENT_ARCHS[1] = arch;

//        arch = new Archetype();
//        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
//        arch.setVersion("1.0"); //NOI18N
//        arch.setArtifactId("appclient-javaee14"); //NOI18N
        APPCLIENT_ARCHS[2] = arch;

        EAR_ARCHS = new Archetype[3];
        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.5"); //NOI18N
        arch.setArtifactId("ear-javaee6"); //NOI18N
        EAR_ARCHS[0] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.4"); //NOI18N
        arch.setArtifactId("ear-jee5"); //NOI18N
        EAR_ARCHS[1] = arch;

        arch = new Archetype();
        arch.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        arch.setVersion("1.4"); //NOI18N
        arch.setArtifactId("ear-j2ee14"); //NOI18N
        EAR_ARCHS[2] = arch;

        EA_ARCH = new Archetype();
        EA_ARCH.setGroupId("org.codehaus.mojo.archetypes"); //NOI18N
        EA_ARCH.setVersion("1.1"); //NOI18N
        EA_ARCH.setArtifactId("pom-root"); //NOI18N
    }

    private static final long serialVersionUID = 1L;
    
    static final String PROPERTY_CUSTOM_CREATOR = "customCreator"; //NOI18N
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private final List<ChangeListener> listeners;
    
    public EAWizardIterator() {
        listeners = new ArrayList<ChangeListener>();
    }
    
    private WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new EAWizardPanel(vg)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            LBL_CreateProjectStep2ee(),
            LBL_EESettings()
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        ProjectInfo ear_vi = (ProjectInfo) wiz.getProperty("ear_versionInfo"); //NOI18N
        assert ear_vi != null;
        // enterprise application wizard, multiple archetypes to run
        ProjectInfo web_vi = (ProjectInfo) wiz.getProperty("web_versionInfo"); //NOI18N
        ProjectInfo ejb_vi = (ProjectInfo) wiz.getProperty("ejb_versionInfo"); //NOI18N
        File rootFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N
        ArchetypeWizards.createFromArchetype(rootFile, vi, EA_ARCH, null, true);
        File earFile = FileUtil.normalizeFile((File) wiz.getProperty("ear_projdir")); // NOI18N
        ArchetypeWizards.createFromArchetype(earFile, ear_vi, (Archetype) wiz.getProperty("ear_archetype"), null, false); //NOI18N
        if (web_vi != null) {
            ArchetypeWizards.createFromArchetype(FileUtil.normalizeFile((File) wiz.getProperty("web_projdir")), web_vi, //NOI18N
                    (Archetype) wiz.getProperty("web_archetype"), null, false); //NOI18N
        }
        if (ejb_vi != null) {
            ArchetypeWizards.createFromArchetype(FileUtil.normalizeFile((File) wiz.getProperty("ejb_projdir")), ejb_vi, //NOI18N
                    (Archetype) wiz.getProperty("ejb_archetype"), null, false); //NOI18N
        }
        addEARDeps((File) wiz.getProperty("ear_projdir"), ejb_vi, web_vi);
        return ArchetypeWizards.openProjects(rootFile, earFile);
    }
    
    private static void addEARDeps (File earDir, ProjectInfo ejbVi, ProjectInfo webVi) {
        FileObject earDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(earDir));
        if (earDirFO == null) {
            return;
        }
        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
        if (ejbVi != null) {
            // EAR ---> ejb
            operations.add(ArchetypeWizards.addDependencyOperation(ejbVi, "ejb"));
        }
        if (webVi != null) {
            // EAR ---> war
            operations.add(ArchetypeWizards.addDependencyOperation(webVi, "war"));
        }

        Utilities.performPOMModelOperations(earDirFO.getFileObject("pom.xml"), operations);
    }

    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = createPanels(vg);
        updateSteps();
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null); //NOI18N
        this.wiz.putProperty("name",null); //NOI18N
        this.wiz = null;
        panels = null;
        listeners.clear();
    }
    
    public String name() {
        return NameFormat(index + 1, panels.length);
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
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
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChange() {
        synchronized (listeners) {
            for (ChangeListener list : listeners) {
                list.stateChanged(new ChangeEvent(this));
            }
        }
    }

    private void updateSteps() {
        // Make sure list of steps is accurate.
        String[] steps = new String[panels.length];
        String[] basicOnes = createSteps();
        System.arraycopy(basicOnes, 0, steps, 0, basicOnes.length);
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (i >= basicOnes.length || steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) {
                // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
}
