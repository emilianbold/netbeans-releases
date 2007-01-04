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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.ServiceCreator;

import org.openide.WizardDescriptor;

import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebServiceFromWSDLWizardIterator implements TemplateWizard.Iterator {
    
    private Project project;

    /** Create a new wizard iterator. */
    public NewWebServiceFromWSDLWizardIterator() {
    }
        
    public static NewWebServiceFromWSDLWizardIterator create() {
        return new NewWebServiceFromWSDLWizardIterator();
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        
        //new WebServiceCreator(project, wiz).createFromWSDL();
        ServiceCreator creator = CreatorProvider.getServiceCreator(project, wiz);
        if (creator!=null) {
            creator.createServiceFromWsdl();
        }                
        return Collections.singleton(dTemplate);
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;
    private transient WizardDescriptor.Panel bottomPanel;

    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;

        project = Templates.getProject(wiz);

        SourceGroup[] sourceGroups = Util.getJavaSourceGroups(project);
        bottomPanel = new WebServiceFromWSDL(wiz, project);
        WizardDescriptor.Panel firstPanel; //special case: use Java Chooser
        if (sourceGroups.length == 0)
            firstPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, bottomPanel));
        else
            firstPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, bottomPanel, true));

        JComponent comp = (JComponent) firstPanel.getComponent();
        Util.changeLabelInComponent(comp, NbBundle.getMessage(NewWebServiceFromWSDLWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_ClassName_Label"), NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_Webservice_Name") );
        Util.hideLabelAndLabelFor(comp, NbBundle.getMessage(NewWebServiceFromWSDLWizardIterator.class, "LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));        
        
        panels = new WizardDescriptor.Panel[] {
            firstPanel,
        };
        
        // Creating steps.
        Object prop = this.wiz.getProperty("WizardPanel_contentData"); // NOI18N
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(TemplateWizard wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.WEB_SERVICE_TYPE, null);
        }
        panels = null;
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

    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebServiceWizardIterator.class, "LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

}
