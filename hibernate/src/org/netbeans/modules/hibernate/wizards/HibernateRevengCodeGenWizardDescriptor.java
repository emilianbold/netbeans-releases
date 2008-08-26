/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.wizards;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateRevengCodeGenWizardDescriptor implements WizardDescriptor.Panel, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private HibernateRevengCodeGenerationPanel component;
    private boolean componentInitialized;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private String title;

    public HibernateRevengCodeGenWizardDescriptor(Project project, String title) {
        this.project = project;
        this.title = title;
    }

    public HibernateRevengCodeGenerationPanel getComponent() {
        if (component == null) {
            component = new HibernateRevengCodeGenerationPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateRevengCodeGenWizardDescriptor.class);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        wizardDescriptor.putProperty("NewFileWizard_Title", title);
        
        HibernateRevengWizardHelper helper = HibernateRevengWizard.getHelper(wizardDescriptor);

        if (!componentInitialized) {
            componentInitialized = true;
            project = Templates.getProject(wizardDescriptor);
            FileObject targetFolder = Templates.getTargetFolder(wizardDescriptor);
            getComponent().initialize(project, targetFolder);
        }
        getComponent().update(helper.getTableClosure());
    }

    public boolean isValid() {
        SourceGroup sourceGroup = getComponent().getLocationValue();
        String packageName = getComponent().getPackageName();
        if (sourceGroup == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengCodeGenerationPanel.class, "ERR_JavaTargetChooser_SelectSourceGroup")); // NOI18N
            return false;
        }

        if (packageName.trim().equals("")) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengCodeGenerationPanel.class, "ERR_JavaTargetChooser_CantUseDefaultPackage")); // NOI18N
            return false;
        }
        if (!JavaIdentifiers.isValidPackageName(packageName)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengCodeGenerationPanel.class, "ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
            return false;
        }

        if (!SourceGroups.isFolderWritable(sourceGroup, packageName)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(HibernateRevengCodeGenerationPanel.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
            return false;
        }

        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        return true;
    }

    public void storeSettings(Object settings) {
        Object buttonPressed = ((WizardDescriptor) settings).getValue();
        if (buttonPressed.equals(WizardDescriptor.NEXT_OPTION) ||
                buttonPressed.equals(WizardDescriptor.FINISH_OPTION)) {

            HibernateRevengWizardHelper helper = HibernateRevengWizard.getHelper(wizardDescriptor);
            helper.setSelectedTables(getComponent().getSelectedTables());
            helper.setLocation(getComponent().getLocationValue());
            helper.setPackageName(getComponent().getPackageName());
            helper.setDomainGen(getComponent().getChkDomain());
            helper.setHbmGen(getComponent().getChkHbm());
            helper.setJavaSyntax(getComponent().getChkJava());
            helper.setEjbAnnotation(getComponent().getChkEjb());

        }
    }

    public void stateChanged(ChangeEvent event) {
        changeSupport.fireChange();
    }

    private void setErrorMessage(String errorMessage) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMessage); // NOI18N

    }
}

