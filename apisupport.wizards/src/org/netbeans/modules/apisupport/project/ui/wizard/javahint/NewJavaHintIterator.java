/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.ui.wizard.javahint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@TemplateRegistration(folder=UIUtil.TEMPLATE_FOLDER, position=1500, displayName="#template_hint", iconBase="org/netbeans/modules/apisupport/project/ui/wizard/javahint/suggestion.png", description="javaHint.html", category=UIUtil.TEMPLATE_CATEGORY)
@Messages("template_hint=Java Hint")
public class NewJavaHintIterator extends BasicWizardIterator {

    private DataModel data;

    @Override
    protected Panel[] createPanels(WizardDescriptor wiz) {
        data = new DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new JavaHintDataPanel(wiz, data),
            new JavaHintLocationPanel(wiz, data)
        };
    }

    @Override
    public Set<?> instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }

    static void generateFileChanges(DataModel model) {
        CreatedModifiedFiles cmf = new CreatedModifiedFiles(model.getProject());

        //add module dependency
        cmf.add(cmf.addModuleDependency("org.netbeans.modules.java.source")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.spi.java.hints")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.spi.editor.hints")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.netbeans.libs.javacapi")); // NOI18N
        cmf.add(cmf.addTestModuleDependency("org.netbeans.modules.java.hints.test"));
        cmf.add(cmf.addTestModuleDependency("org.netbeans.libs.junit4"));
        
        String className = model.getClassName();
        FileObject hintTemplate = CreatedModifiedFiles.getTemplate("javaHint.java"); // NOI18N
        assert hintTemplate != null;

        String hintPath = model.getDefaultPackagePath(className + ".java", false); // NOI18N

        Map<String,String> replaceTokens = new HashMap<String,String>();
        replaceTokens.put("CLASS_NAME", className); // NOI18N
        replaceTokens.put("PACKAGE_NAME", model.getPackageName()); // NOI18N
        replaceTokens.put("GENERATE_FIX", model.isDoFix() ? "true" : null); // NOI18N
        replaceTokens.put("DISPLAY_NAME", model.getDisplayName()); // NOI18N
        replaceTokens.put("DESCRIPTION", model.getDescription()); // NOI18N
        replaceTokens.put("WARNING_MESSAGE", model.getWarningMessage()); // NOI18N
        if (model.isDoFix()) {
            replaceTokens.put("FIX_MESSAGE", model.getFixText()); // NOI18N
        }

        cmf.add(cmf.createFileWithSubstitutions(hintPath, hintTemplate, replaceTokens));

        String testPath = model.getDefaultPackagePath(className + "Test.java", false, true); // NOI18N
        FileObject testTemplate = CreatedModifiedFiles.getTemplate("javaHintTest.java"); // NOI18N
        assert testTemplate != null;

        cmf.add(cmf.createFileWithSubstitutions(testPath, testTemplate, replaceTokens));

        //at the end
        model.setCreatedModifiedFiles(cmf);
    }

    static final class DataModel extends BasicWizardIterator.BasicDataModel {

        private CreatedModifiedFiles files;
        private String className;
        private String displayName;
        private String descriptor;
        private String warningMessage;
        private boolean doFix;
        private String fixText;
        private String codeNameBase;

        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }

        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return files;
        }

        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getDescription() {
            return descriptor;
        }

        public void setDescription(String descriptor) {
            this.descriptor = descriptor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public CreatedModifiedFiles getFiles() {
            return files;
        }

        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public boolean isDoFix() {
            return doFix;
        }

        public void setDoFix(boolean doFix) {
            this.doFix = doFix;
        }

        public String getFixText() {
            return fixText;
        }

        public void setFixText(String fixText) {
            this.fixText = fixText;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public void setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
        }

        public @Override String getPackageName() {
            String retValue;
            retValue = super.getPackageName();
            if (retValue == null) {
                retValue = getCodeNameBase();
                super.setPackageName(retValue);
            }
            return retValue;
        }

        private String getCodeNameBase() {
            if (codeNameBase == null) {
                NbModuleProvider mod = getProject().getLookup().lookup(NbModuleProvider.class);
                codeNameBase = mod.getCodeNameBase();
            }
            return codeNameBase;
        }
    }

}
