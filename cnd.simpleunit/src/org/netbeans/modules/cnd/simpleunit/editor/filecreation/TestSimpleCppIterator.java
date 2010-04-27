/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.simpleunit.editor.filecreation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.CCFSrcFileIterator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * @author Nikolay Krasilnikov (http://nnnnnk.name)
 */
public class TestSimpleCppIterator extends CCFSrcFileIterator {

    private static final String C_HEADER_MIME_TYPE = "text/x-c/text/x-h"; // NOI18N

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataFolder targetFolder = wiz.getTargetFolder();

        Set<DataObject> dataObjects = new HashSet<DataObject>();

        DataObject formDataObject = NewTestSimpleCppPanel.getTemplateDataObject("cppsimpletest.cc"); // NOI18N

        if(getTestFileName() != null) {
            dataObjects.add(formDataObject.createFromTemplate(targetFolder, getTestFileName()));
        }

        Project project = Templates.getProject(wiz);
        Folder testsRoot = getTestsRootFolder(project);

        //Folder test = testsRoot.addNewFolder("zzz", "zzz", true, Folder.Kind.TEST);

        //getExplorerManager().setSelectedNodes(new Node[]{node});

        //final Action[] actions = f.getItemsAsArray()[0].getDataObject().getNodeDelegate().getActions(true);



//
//        if (getSourceFileName() != null) {
//            DataObject sourceDataObject = NewQtFormPanel.getTemplateDataObject("form.cc"); // NOI18N
//            dataObjects.add(sourceDataObject.createFromTemplate(targetFolder, getSourceFileName()));
//
//            DataObject headerDataObject = NewQtFormPanel.getTemplateDataObject("form.h"); // NOI18N
//            dataObjects.add(headerDataObject.createFromTemplate(targetFolder, getHeaderFileName()));
//        }

        return dataObjects;
    }

    private static Folder getTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();

        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if(folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        return testRootFolder;
    }

    @Override
    protected Panel<WizardDescriptor> createPanel(TemplateWizard wiz) {
        DataObject dobj = wiz.getTemplate();
        FileObject fobj = dobj.getPrimaryFile();
        String mimeType = fobj.getMIMEType();
        MIMEExtensions extensions = MIMEExtensions.get(mimeType);
        if (extensions != null) {
            Project project = Templates.getProject(wiz);
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (MIMENames.HEADER_MIME_TYPE.equals(extensions.getMIMEType())) {
                // this is the only place where we want to differ c headers from cpp headers (creation of new one)
                if (dobj.getPrimaryFile().getAttribute(C_HEADER_MIME_TYPE) != null) {
                    MIMEExtensions cHeaderExtensions = MIMEExtensions.get(C_HEADER_MIME_TYPE);
                    if ((cHeaderExtensions == null) || !C_HEADER_MIME_TYPE.equals(cHeaderExtensions.getMIMEType())) {
                        System.err.println("not found extensions for C Headers"); // NOI18N
                    } else {
                        extensions = cHeaderExtensions;
                    }
                }
            }
            String defaultExt = null; // let the chooser panel decide default extension
            if (mimeType.equals(MIMENames.SHELL_MIME_TYPE)) {
                // for shell scripts set default extension explicitly
                defaultExt = fobj.getExt();
            } else if (mimeType.equals(MIMENames.HEADER_MIME_TYPE) && fobj.getExt().length() == 0) {
                // for standard header without extension
                defaultExt = fobj.getExt();
            }

            NewTestSimpleCppPanel panel = new NewTestSimpleCppPanel(project, groups, null, extensions, defaultExt);
            return panel;
        } else {
            return wiz.targetChooser();
        }
    }

    private String getTestFileName() {
        return ((NewTestSimpleCppPanelGUI)targetChooserDescriptorPanel.getComponent()).getTestFileName();
    }


//    private FormType getFormType() {
//        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getFormType();
//    }
//
//    private String getSourceFileName() {
//        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getSourceFileName();
//    }
//
//    private String getHeaderFileName() {
//        return ((NewQtFormPanelGUI)targetChooserDescriptorPanel.getComponent()).getHeaderFileName();
//    }

}

