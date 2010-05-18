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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.constants.FileWizardConstants;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.refactoring.WebXMLRefactoringSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Collection;

public class ServletDeploymentWizardPanel implements WizardDescriptor.Panel {

    WizardDescriptor wizard;
    Collection<String> allServletNames;

    public ServletDeploymentWizardPanel(WizardDescriptor wizard) {
        this.wizard = wizard;
        // Read the content of web descriptor and store all defined servlet names
        Project p = Templates.getProject(wizard);
        final JCProject jcProject = p.getLookup().lookup(JCProject.class);
        if (jcProject != null) {
            ProjectManager.mutex().readAccess(new Runnable() {
                public void run() {
                    FileObject webObj = jcProject.getProjectDirectory()
                            .getFileObject(JCConstants.WEB_DESCRIPTOR_PATH);
                    WebXMLRefactoringSupport web = 
                            WebXMLRefactoringSupport.fromFile(FileUtil.toFile(webObj));
                    allServletNames = web.getAllServletNames();
                }
            });
        }
    }
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ServletDeploymentVisualPanel component;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new ServletDeploymentVisualPanel(this);
        }
        return component;
    }

    public void setClassName(String className) {
        component.setClassName(className);
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
    // If you have context help:
    // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        String servletName = component.getServletName();
        
        if (servletName.equals("")) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServletDeploymentWizardPanel.class,
                            "LBL_servlet_name_empty"));
            return false;
        }
        if (allServletNames != null && allServletNames.contains(servletName)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServletDeploymentWizardPanel.class,
                            "LBL_servlet_name_exists"));
            return false;
        }
        if (!component.getUrlPattern().startsWith("/")) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(ServletDeploymentWizardPanel.class,
                            "LBL_url_pattern_invalid"));
            return false;
        }
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        return true;
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        component.setClassName(Templates.getTargetName(wiz));
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wiz = (WizardDescriptor) settings;
        if (component.isAddInfoSelected()) {
            wiz.putProperty(FileWizardConstants.PROP_URL_PATTERN, component.getUrlPattern());
            wiz.putProperty(FileWizardConstants.PROP_SERVLET_NAME, component.getServletName());
        }
    }
}

