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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.j2ee.newproject.archetype.J2eeArchetypeFactory;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * This class is responsible for creating new EAR projects
 * 
 *@author Dafe Simonek, Martin Janicek
 */
@TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=270, displayName="#template.EA", iconBase="org/netbeans/modules/maven/j2ee/resources/maven_enterprise_application_16.png", description="../resources/EADescription.html")
@Messages("template.EA=Enterprise Application")
public class EAWizardIterator extends BaseWizardIterator {
    

    @Override
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo ear_vi = (ProjectInfo) wiz.getProperty("ear_versionInfo"); //NOI18N
        assert ear_vi != null;
        
        // enterprise application wizard, multiple archetypes to run
        ProjectInfo web_vi = (ProjectInfo) wiz.getProperty("web_versionInfo"); //NOI18N
        ProjectInfo ejb_vi = (ProjectInfo) wiz.getProperty("ejb_versionInfo"); //NOI18N
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N
        File rootFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        File earFile = FileUtil.normalizeFile((File) wiz.getProperty("ear_projdir")); // NOI18N
        
        ArchetypeWizards.createFromArchetype(rootFile, vi, J2eeArchetypeFactory.getInstance().getAnyArchetypeFor(J2eeModule.Type.RAR), null, true);
        ArchetypeWizards.createFromArchetype(earFile, ear_vi, (Archetype) wiz.getProperty("ear_archetype"), null, false); //NOI18N
        if (web_vi != null) {
            ArchetypeWizards.createFromArchetype(FileUtil.normalizeFile((File) wiz.getProperty("web_projdir")), web_vi, //NOI18N
                    (Archetype) wiz.getProperty("web_archetype"), null, false); //NOI18N
        }
        if (ejb_vi != null) {
            ArchetypeWizards.createFromArchetype(FileUtil.normalizeFile((File) wiz.getProperty("ejb_projdir")), ejb_vi, //NOI18N
                    (Archetype) wiz.getProperty("ejb_archetype"), null, false); //NOI18N
        }
        addEARDependencies((File) wiz.getProperty("ear_projdir"), ejb_vi, web_vi); // NOI18N
        
        // For every single created project we need to setup server correctly
        Set<FileObject> projects = ArchetypeWizards.openProjects(rootFile, earFile);
        for (FileObject projectFile : projects) {
            saveSettingsToNbConfiguration(projectFile);

            // We don't want to set server in pom.xml for pom-packaging module
            String projectDirName = projectFile.getName();
            if (projectDirName.endsWith("-ejb") || projectDirName.endsWith("-ear") || projectDirName.endsWith("-web")) { // NOI18N
                saveServerToPom(ProjectManager.getDefault().findProject(projectFile));
            }
        }
        
        return projects;
    }
    
    /**
     * Creates dependencies between EAR ---> Ejb module and EAR ---> Web module
     * 
     * @param earDir ear module directory
     * @param ejbInfo ejb project informations
     * @param webInfo web project informations
     */
    private void addEARDependencies (File earDir, ProjectInfo ejbInfo, ProjectInfo webInfo) {
        FileObject earDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(earDir));
        if (earDirFO == null) {
            return;
        }
        List<ModelOperation<POMModel>> operations = new ArrayList<ModelOperation<POMModel>>();
        if (ejbInfo != null) {
            operations.add(ArchetypeWizards.addDependencyOperation(ejbInfo, "ejb")); // NOI18N
        }
        if (webInfo != null) {
            operations.add(ArchetypeWizards.addDependencyOperation(webInfo, "war")); // NOI18N
        }

        Utilities.performPOMModelOperations(earDirFO.getFileObject("pom.xml"), operations); // NOI18N
    }
    
    @Override
    protected WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new EAWizardPanel(vg)
        };
    }
}
