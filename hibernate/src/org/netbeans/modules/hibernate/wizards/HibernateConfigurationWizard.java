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

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
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
   
    public static HibernateConfigurationWizard create() {
        return new HibernateConfigurationWizard();
    }

    public String name() {
        return NbBundle.getMessage(HibernateConfigurationWizard.class, "LBL_WizardTitle");
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
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
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }

        return res;
    }

    public void initialize(WizardDescriptor wizard) {
        // obtaining target folder
        this.wizard = wizard;
        project = Templates.getProject(wizard);
        FileObject fileObject = Templates.getTargetFolder(wizard); 
        
        if (fileObject == null) {
            FileObject targetFolder = Util.getSourceRoot(project);
            Templates.setTargetFolder(wizard, targetFolder);          
        }  

        descriptor = new HibernateConfigurationWizardDescriptor(project);
        panels = new WizardDescriptor.Panel[]{descriptor};
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(HibernateConfigurationWizard.class, "Templates/Hibernate/HibernateCfgTemplate.xml"));

        // Creating steps.
        Object prop = wizard.getProperty("WizardPanel_contentData");
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent) panels[i].getComponent();
            if (steps[i] == null) {
                steps[i] = jc.getName();
            }
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        }
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
        try {
            HibernateCfgDataObject hdo = (HibernateCfgDataObject) newOne; 
            hdo.addSessionFactory(sFactory);
            hdo.save();
            return Collections.singleton(hdo.getPrimaryFile());

        } catch (Exception e) {
            return Collections.EMPTY_SET; 
        }


    }
    
}
