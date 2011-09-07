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
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
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
public class BasicEEWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator {
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    private Archetype[] archs;

    /** value is Integer index into {@link #eeLevels} */
    static final String PROP_EE_LEVEL = "eeLevel";

    private BasicEEWizardIterator(Archetype[] archs) {
        this.archs = archs;
    }

    @TemplateRegistration(folder = "Project/Maven2", position = 200, displayName = "#template.WebApp", iconBase = "org/netbeans/modules/maven/j2ee/web/maven_web_application_16.png", description = "WebAppDescription.html")
    @Messages("template.WebApp=Web Application")
    public static BasicEEWizardIterator createWebAppIterator() {
        return new BasicEEWizardIterator(EAWizardIterator.WEB_APP_ARCHS);
    }

    @TemplateRegistration(folder = "Project/Maven2", position = 250, displayName = "#template.EJB", iconBase = "org/netbeans/modules/maven/j2ee/ejb/maven_ejb_module_16.png", description = "EjbDescription.html")
    @Messages("template.EJB=EJB Module")
    public static BasicEEWizardIterator createEJBIterator() {
        return new BasicEEWizardIterator(EAWizardIterator.EJB_ARCHS);
    }
    
    @TemplateRegistration(folder = "Project/Maven2", position = 277, displayName = "#template.APPCLIENT", iconBase = "org/netbeans/modules/maven/j2ee/appclient/appclient.png", description = "AppClientDescription.html")
    @Messages("template.APPCLIENT=Enterprise Application Client")
    public static BasicEEWizardIterator createAppClientIterator() {
        return new BasicEEWizardIterator(EAWizardIterator.APPCLIENT_ARCHS);
    }
    
    @Messages({
        "LBL_JEE6=Java EE 6",
        "LBL_JEE5=Java EE 5",
        "LBL_J2EE14=J2EE 1.4"
    })
    static String[] eeLevels() {
        return new String[] {
            LBL_JEE6(),
            LBL_JEE5(),
            LBL_J2EE14()
        };
    }

    private WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new EELevelPanel(),
        };
    }
    
    @Messages("LBL_CreateProjectStep2ee=Name and Location")
    private String[] createSteps() {
        return new String[] {
            LBL_CreateProjectStep2ee(),
            LBL_EESettings(),
        };
    }
    
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N

        Archetype arch = archs[(Integer) wiz.getProperty(PROP_EE_LEVEL)];
        ArchetypeWizards.logUsage(arch.getGroupId(), arch.getArtifactId(), arch.getVersion());

        File projFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        ArchetypeWizards.createFromArchetype(projFile, vi, arch, null, true);
        return ArchetypeWizards.openProjects(projFile, null);
    }
    
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));

        panels = createPanels(vg);
        this.wiz = wiz;
        // Make sure list of steps is accurate.
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    @Messages("NameFormat={0} of {1}")
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
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
