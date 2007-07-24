/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schemas;
import org.netbeans.modules.xml.jaxb.util.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author $Author$
 * @author lgao
 */
public class JAXBWizardIterator implements TemplateWizard.Iterator  {
    private WizardDescriptor wizardDescriptor;    
    private WizardDescriptor.Panel[] panels = null;
    private int cursor;
    private Project project;

    public JAXBWizardIterator() {
    }
    
    public JAXBWizardIterator(Project project) {
        this.project = project;
        initWizardPanels();
    }
    
    public static JAXBWizardIterator create() {
        return new JAXBWizardIterator();
    }
  
    private void initWizardPanels() {
        cursor = 0;
        panels = new WizardDescriptor.Panel[] {
            new JAXBWizBindingCfgPanel(),
        };
    }

    public void addChangeListener(ChangeListener changeListener) {
    }

    public void removeChangeListener(ChangeListener changeListener) {
    }

    public WizardDescriptor.Panel current() {
        return panels[ cursor ];
    }

    public boolean hasNext() {
        return cursor < panels.length - 1;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public String name() {
        return NbBundle.getMessage(this.getClass(),"LBL_JAXBWizTitle");// NOI18N                
    }
    
    public void nextPanel() {
        cursor ++;
    }

    public void previousPanel() {
        cursor --;
    }

    public boolean hasPrevious() {
        return cursor > 0;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wizardDescriptor = wiz;
        
        Object prop = wiz.getProperty("WizardPanel_contentData"); //NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", //NOI18N
                                                    new Integer(i)); 
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }        
    }

    public Set instantiate() throws IOException {
        return new HashSet();
    }

    public void uninitialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = null;
    }

    // TemplateWizard specific - Start
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                        
        return Collections.singleton(dTemplate);
    }

    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    public void initialize(TemplateWizard wiz) {
        project = Templates.getProject(wiz);
        List<String> schemas = ProjectHelper.getSchemaNames(project);
        wiz.putProperty(JAXBWizModuleConstants.EXISTING_SCHEMA_NAMES, schemas);
        initWizardPanels();     

        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);
        String name = ProjectUtils.getInformation(project).getName();
         wiz.putProperty(JAXBWizModuleConstants.PROJECT_NAME, name);
        // Make sure list of steps is accurate.
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", //NOI18N
                        new Integer(i)); 
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
        
    }

    public void uninitialize(TemplateWizard wiz) {
        if ( wiz.getValue() == TemplateWizard.FINISH_OPTION ) {
                String pkgName = (String) wiz.getProperty(
                        JAXBWizModuleConstants.PACKAGE_NAME);
                try {
                    Schema nSchema = ProjectHelper.importResources(project, 
                            wiz, null);
                    ProjectHelper.addSchema2Model(project, nSchema);                    
                    ProjectHelper.compileXSDs(project, pkgName, true);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
                this.project = null;
        }
    }
    // TemplateWizard specific - End
}