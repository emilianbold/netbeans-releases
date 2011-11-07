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
import java.util.Set;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.modules.maven.api.archetype.ArchetypeWizards;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import org.netbeans.modules.maven.j2ee.newproject.archetype.J2eeArchetypeFactory;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;

/**
 * This class is responsible for creating Ejb, Web and App client projects
 * 
 *@author Dafe Simonek, Martin Janicek
 */
public class EEWizardIterator extends BaseWizardIterator {

    /** value is one of Profile instance */
    public static final String PROP_EE_LEVEL = "eeLevel"; // NOI18N
    private J2eeModule.Type projectType;
    
    
    private EEWizardIterator(J2eeModule.Type projectType) {
        this.projectType = projectType;
    }
    

    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=200, displayName="#template.WebApp", iconBase="org/netbeans/modules/maven/j2ee/resources/maven_web_application_16.png", description="../resources/WebAppDescription.html")
    @Messages("template.WebApp=Web Application")
    public static EEWizardIterator createWebAppIterator() {
        return new EEWizardIterator(J2eeModule.Type.WAR);
    }

    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=250, displayName="#template.EJB", iconBase="org/netbeans/modules/maven/j2ee/resources/maven_ejb_module_16.png", description="../resources/EjbDescription.html")
    @Messages("template.EJB=EJB Module")
    public static EEWizardIterator createEJBIterator() {
        return new EEWizardIterator(J2eeModule.Type.EJB);
    }
    
    @TemplateRegistration(folder=ArchetypeWizards.TEMPLATE_FOLDER, position=277, displayName="#template.APPCLIENT", iconBase="org/netbeans/modules/maven/j2ee/resources/appclient.png", description="../resources/AppClientDescription.html")
    @Messages("template.APPCLIENT=Enterprise Application Client")
    public static EEWizardIterator createAppClientIterator() {
        return new EEWizardIterator(J2eeModule.Type.CAR);
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        ProjectInfo vi = new ProjectInfo((String) wiz.getProperty("groupId"), (String) wiz.getProperty("artifactId"), (String) wiz.getProperty("version"), (String) wiz.getProperty("package")); //NOI18N
        
        Profile profile = (Profile) wiz.getProperty(PROP_EE_LEVEL);
        Archetype archetype = J2eeArchetypeFactory.getInstance().findArchetypeFor(projectType, profile);
        ArchetypeWizards.logUsage(archetype.getGroupId(), archetype.getArtifactId(), archetype.getVersion());

        File rootFile = FileUtil.normalizeFile((File) wiz.getProperty("projdir")); // NOI18N
        ArchetypeWizards.createFromArchetype(rootFile, vi, archetype, null, true);
        
        Set<FileObject> projects = ArchetypeWizards.openProjects(rootFile, rootFile);
        for (FileObject projectFile : projects) {
            saveSettingsToNbConfiguration(projectFile);
        }
        
        return projects;
    }
    
    @Override
    protected WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            ArchetypeWizards.basicWizardPanel(vg, false, null),
            new EELevelPanel(projectType),
        };
    }
}
