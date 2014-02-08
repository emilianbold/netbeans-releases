/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.wizard;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * @author Roman Svitanic
 */
public class J2MESampleProjectIterator implements TemplateWizard.Iterator {

    private static final Logger LOG = Logger.getLogger(J2MESampleProjectIterator.class.getName());

    private static final long serialVersionUID = 4L;

    static final String PROJECT_DIR = "projdir"; //NOI18N
    static final String NAME = "name"; //NOI18N
    static final String SOURCE_ROOT = "sourceRoot"; //NOI18N
    static final String JDK_PLATFORM = "jdk"; //NOI18N
    static final String PLATFORM = "platform"; //NOI18N
    static final String DEVICE = "device"; //NOI18N
    static final String CONFIGURATION = "config"; //NOI18N
    static final String PROFILE = "profile"; //NOI18N
    static final String OPTIONAL_API = "optionalApi"; //NOI18N
    static final String PLATFORM_BOOTCLASSPATH = "bootclasspath"; //NOI18N

    int currentIndex;
    WizardDescriptor.Panel<WizardDescriptor> basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new J2MESampleProjectIterator();
    }

    public J2MESampleProjectIterator() {
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener changeListener) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public org.openide.WizardDescriptor.Panel<WizardDescriptor> current() {
        return basicPanel;
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
    public void initialize(org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); //NOI18N
        }
        templateWizard.putProperty(NAME, name);
        templateWizard.putProperty("NewProjectWizard_Title", templateWizard.getTemplate().getNodeDelegate().getDisplayName()); //NOI18N
        basicPanel = new PanelConfigureProject(J2MEProjectWizardIterator.WizardType.SAMPLE);
        currentIndex = 0;
        updateStepsList();
    }

    @Override
    public void uninitialize(org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty(PROJECT_DIR, null);
        this.wiz.putProperty(NAME, null);
    }

    @Override
    public java.util.Set<DataObject> instantiate(org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(PROJECT_DIR);
        String name = (String) wiz.getProperty(NAME);
        String j2mePlatform = (String) ((JavaPlatform) wiz.getProperty(PLATFORM)).getProperties().get(J2MEProjectProperties.PLATFORM_ANT_NAME);
        String configuration = (String) wiz.getProperty(CONFIGURATION);
        String profile = (String) wiz.getProperty(PROFILE);
        String device = (String) wiz.getProperty(DEVICE);
        String jdk = (String) ((JavaPlatform) wiz.getProperty(JDK_PLATFORM)).getProperties().get(J2MEProjectProperties.PLATFORM_ANT_NAME);
        String optionalApi = (String) wiz.getProperty(OPTIONAL_API);
        String bootclasspath = (String) wiz.getProperty(PLATFORM_BOOTCLASSPATH);
        String platformType = ((J2MEPlatform) wiz.getProperty(PLATFORM)).getType();

        FileObject templateFO = templateWizard.getTemplate().getPrimaryFile();
        FileObject prjLoc = J2MESampleProjectGenerator.createProjectFromTemplate(
                templateFO, projectLocation, name, jdk, j2mePlatform,
                device, configuration, profile, optionalApi, bootclasspath, platformType);
        java.util.Set<DataObject> set = new java.util.HashSet<>();
        set.add(DataObject.find(prjLoc));

        // open file from the project specified in the "defaultFileToOpen" attribute
        Object openFile = templateFO.getAttribute("defaultFileToOpen"); // NOI18N
        if (openFile instanceof String) {
            FileObject openFO = prjLoc.getFileObject((String) openFile);
            set.add(DataObject.find(openFO));
        }

        return set;
    }

    @Override
    public String name() {
        return current().getComponent().getName();
    }

    @Override
    public void nextPanel() {
        throw new NoSuchElementException();
    }

    @Override
    public void previousPanel() {
        throw new NoSuchElementException();
    }

    void updateStepsList() {
        JComponent component = (JComponent) current().getComponent();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[]{
            NbBundle.getMessage(J2MESampleProjectIterator.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(currentIndex)); // NOI18N
    }

}
