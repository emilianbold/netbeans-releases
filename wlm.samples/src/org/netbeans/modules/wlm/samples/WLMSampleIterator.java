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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.samples;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.modules.xml.samples.SampleIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileLock;

/**
 *
 * @author anjeleevich
 */
public class WLMSampleIterator extends SampleIterator {

    protected WizardDescriptor wizard;

    public WLMSampleIterator(String name) {
        super("wlm", name); // NOI18N
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        super.initialize(wizard);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard = null;
        super.uninitialize(wizard);
    }

    public static WizardDescriptor.InstantiatingIterator
            createPurchaseOrder() {
        return new WLMSampleIterator("PurchaseOrder"); // NOI18N
    }

    public static WizardDescriptor.InstantiatingIterator
            createAdvancedPurchaseOrder() {
        return new WLMSampleIterator("AdvancedPurchaseOrder"); // NOI18N
    }

    @Override
    public Set<FileObject> instantiate() throws IOException {
        final Set<FileObject> set = new LinkedHashSet<FileObject>();

        File dir = FileUtil.normalizeFile((File) wizard.getProperty(PROJECT_DIR));
        dir.mkdirs();

        FileObject baseFolder = FileUtil.toFileObject(dir);
        FileObject template = Templates.getTemplate(wizard);

        String projectName = (String) wizard.getProperty(PROJECT_NAME);

        String wlmName = projectName + WLM_SUFFIX;
        String bpelName = projectName + BPEL_SUFFIX;
        String compAppName = projectName + COMP_APP_SUFFIX;

        FileObject wlmFolder = baseFolder.createFolder(wlmName);
        FileObject bpelFolder = baseFolder.createFolder(bpelName);
        FileObject compAppFolder = baseFolder.createFolder(compAppName);

        String templateName = template.getName();

        String templateBpelName = FOLDER + templateName
                + BPEL_SUFFIX + ".zip"; // NOI18N
        String templateCompAppName = FOLDER + templateName
                + COMP_APP_SUFFIX + ".zip"; // NOI18N

        unzip(template, wlmFolder);
        unzip(FileUtil.getConfigFile(templateBpelName), bpelFolder);
        unzip(FileUtil.getConfigFile(templateCompAppName),
                compAppFolder);

        changeName(wlmFolder, wlmName, templateName + WLM_SUFFIX);
        changeName(bpelFolder, bpelName, templateName + BPEL_SUFFIX);
        renameCompApp(compAppFolder, compAppName, templateName
                + COMP_APP_SUFFIX);

        addModule(compAppFolder, wlmFolder);
        addModule(compAppFolder, bpelFolder);

        set.add(wlmFolder);
        set.add(bpelFolder);
        set.add(compAppFolder);

        return set;
    }

    @Override
    protected void addArtifact(Project project, AntArtifact artifact) {
        new AddProjectAction().addProject(project, artifact);
    }

    protected static final String FOLDER
            = "/org-netbeans-modules-wlm-samples-resources-zip/"; // NOI18N

    protected static final String BPEL_SUFFIX = "BPEL"; // NOI18N
    protected static final String WEB_SUFFIX = "Web"; // NOI18N
    protected static final String WLM_SUFFIX = "WLM"; // NOI18N
    protected static final String COMP_APP_SUFFIX = "CompApp"; // NOI18N

    protected static final String PROJECT_DIR = "project.dir"; // NOI18N
    protected static final String PROJECT_NAME = "project.name"; // NOI18N
}
