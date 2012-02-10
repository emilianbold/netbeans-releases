/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserve *
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new FXML file and optionally Java Controller and CSS file.
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
// TODO separate panels for fxml, controlles and css
// TODO isValid() should check for correctness of controller and css as well
// TODO register via annotations instead of layer.xml
// TODO logging: process exceptions
public class FXMLTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    static final String JAVA_CONTROLLER_CREATE = "JavaControllerCreate"; // NOI18N
    static final String JAVA_CONTROLLER_NAME_PROPERTY = "JavaController"; // NOI18N
    static final String CSS_NAME_PROPERTY = "CSS"; // NOI18N
    
    private WizardDescriptor wizard;
    private ConfigureFXMLPanel panel;

    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> create() {
        return new FXMLTemplateWizardIterator();
    }

    private FXMLTemplateWizardIterator() {
    }

    @Override
    public String name() {
        return panel.name();
    }
    
    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        Project project = Templates.getProject(wizard);
        if (project == null) {
                throw new IllegalStateException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Project_Null_Error")); // NOI18N
        }
        
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups == null) {
                throw new IllegalStateException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_SGs_Error")); // NOI18N
        }
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }
        
        panel = new ConfigureFXMLPanel(project, groups);
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Set instantiate() throws IOException, IllegalArgumentException {
        Set<FileObject> set = new HashSet<FileObject>(3);
        //set.addAll(delegateIterator.instantiate());

        FileObject dir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(dir);

        String targetName = Templates.getTargetName(wizard);
        boolean createController = (Boolean) wizard.getProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_CREATE);
        String controller = (String) wizard.getProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_NAME_PROPERTY);
        String css = (String) wizard.getProperty(FXMLTemplateWizardIterator.CSS_NAME_PROPERTY);

//        FileObject mainTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.java"); // NOI18N
//        DataObject dMainTemplate = DataObject.find(mainTemplate);
//        String mainName = targetName + NbBundle.getMessage(FXMLTemplateWizardIterator.class, "Templates/javafx/FXML_Main_Suffix"); //NOI18N
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("fxmlname", targetName); // NOI18N
//        DataObject dobj1 = dMainTemplate.createFromTemplate(df, mainName, params); // NOI18N
//        set.add(dobj1.getPrimaryFile());

        Map<String, String> params = new HashMap<String, String>();
        if (createController) {
            params.put("create", "true"); // NOI18N
        }
        if (controller != null) {
            params.put("controller", controller); // NOI18N
        }
        if (css != null) {
            params.put("css", css); // NOI18N
        }

        FileObject xmlTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.fxml"); // NOI18N
        DataObject dXMLTemplate = DataObject.find(xmlTemplate);
        DataObject dobj = dXMLTemplate.createFromTemplate(df, targetName, params);
        set.add(dobj.getPrimaryFile());

        if (createController && controller != null) {
            FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXMLController.java"); // NOI18N
            DataObject dJavaTemplate = DataObject.find(javaTemplate);
            DataObject dobj2 = dJavaTemplate.createFromTemplate(df, controller); // NOI18N
            set.add(dobj2.getPrimaryFile());
        }

        if (css != null) {
            FileObject cssTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.css"); // NOI18N
            DataObject dCSSTemplate = DataObject.find(cssTemplate);
            DataObject dobj3 = dCSSTemplate.createFromTemplate(df, css); // NOI18N
            set.add(dobj3.getPrimaryFile());
        }

        return set;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public boolean hasNext() {
        return false;
    }
    
    @Override
    public boolean hasPrevious() {
        return false;
    }
    
    @Override
    public void nextPanel() {
    }
    
    @Override
    public void previousPanel() {
    }
    
    @Override
    public void addChangeListener(ChangeListener l) {
        panel.addChangeListener(l);
    }
    
    @Override
    public void removeChangeListener(ChangeListener l) {
        panel.removeChangeListener(l);
    }
}
