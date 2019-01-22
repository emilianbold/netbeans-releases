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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.examples;

import java.io.File;
import java.util.Collections;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class JMXSampleProjectIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new JMXSampleProjectIterator();
    }
    
    public JMXSampleProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    public void initialize (org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        String name = templateWizard.getTemplate().getNodeDelegate().getDisplayName();
        if (name != null) {
            name = name.replaceAll(" ", ""); //NOI18N
        }
        templateWizard.putProperty (WizardProperties.NAME, name);
        basicPanel = new PanelConfigureProject();
        currentIndex = 0;
        updateStepsList ();
    }
    
    public void uninitialize (org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
    }
    
    public java.util.Set instantiate (org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        File projectLocation = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);

        FileObject prjLoc = null;
        prjLoc = JMXSampleProjectGenerator.createProjectFromTemplate(templateWizard.getTemplate().getPrimaryFile(), projectLocation, name);
        return Collections.singleton(DataObject.find(prjLoc));
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, list); // NOI18N
        component.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (currentIndex)); // NOI18N
    }
    
}
