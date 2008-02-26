/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author gowri
 */
public class HibernateConfigurationWizard implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private Project project;
    private WizardDescriptor wizard;
    private HibernateConfigurationWizardDescriptor descriptor;
    private WizardDescriptor.Panel[] panels;
    private final String dialect = "hibernate.dialect";
    private final String driver = "hibernate.connection.driver_class";
    private final String url = "hibernate.connection.url";
    private final String userName = "hibernate.connection.username";
    private final String password = "hibernate.connection.password";

    public static HibernateConfigurationWizard create() {
        return new HibernateConfigurationWizard();
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project p = Templates.getProject(wizard);
            SourceGroup[] groups = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            WizardDescriptor.Panel targetChooser = Templates.createSimpleTargetChooser(p, groups);

            panels = new WizardDescriptor.Panel[]{
                targetChooser,
                descriptor
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }

    public String name() {
        return NbBundle.getMessage(HibernateConfigurationWizard.class, "LBL_ConfWizardTitle");
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        project = Templates.getProject(wizard);
        descriptor = new HibernateConfigurationWizardDescriptor(project);        
        FileObject sourceRoot = Util.getSourceRoot(project);        
        Templates.setTargetFolder(wizard, sourceRoot);         
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public Set instantiate() throws IOException {
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        DataFolder targetDataFolder = DataFolder.findFolder(targetFolder);
        String targetName = Templates.getTargetName(wizard);
        FileObject templateFileObject = Templates.getTemplate(wizard);
        DataObject templateDataObject = DataObject.find(templateFileObject);
        
        
        DataObject newOne = templateDataObject.createFromTemplate(targetDataFolder, targetName);

        SessionFactory sFactory = new SessionFactory();
        if (descriptor.getDialectName() != null && !"".equals(descriptor.getDialectName())) {
            int row = sFactory.addProperty2(descriptor.getDialectName());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", dialect);
        }

        if (descriptor.getDriver() != null && !"".equals(descriptor.getDriver())) {
            int row = sFactory.addProperty2(descriptor.getDriver());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", driver);
        }
        if (descriptor.getURL() != null && !"".equals(descriptor.getURL())) {
            int row = sFactory.addProperty2(descriptor.getURL());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", url);
        }
        if (descriptor.getUserName() != null && !"".equals(descriptor.getUserName())) {
            int row = sFactory.addProperty2(descriptor.getUserName());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", userName);
        }
        if (descriptor.getPassword() != null && !"".equals(descriptor.getPassword())) {
            int row = sFactory.addProperty2(descriptor.getPassword());
            sFactory.setAttributeValue(SessionFactory.PROPERTY2, row, "name", password);
        }
        try {
            HibernateCfgDataObject hdo = (HibernateCfgDataObject) newOne;
            hdo.addSessionFactory(sFactory);
            hdo.save();
            // Register Hibernate Library in the project if its not already registered.
            HibernateEnvironment hibernateEnvironment = project.getLookup().lookup(HibernateEnvironment.class);
            System.out.println("Library registered : " + hibernateEnvironment.addHibernateLibraryToProject(hdo.getPrimaryFile()));
            
            return Collections.singleton(hdo.getPrimaryFile());

        } catch (Exception e) {
            System.err.println("Error**************************" + e);
            return Collections.EMPTY_SET;
        }


    }
}
