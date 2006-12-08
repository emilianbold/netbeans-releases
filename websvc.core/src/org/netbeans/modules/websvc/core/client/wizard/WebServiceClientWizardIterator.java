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

package org.netbeans.modules.websvc.core.client.wizard;

import java.io.*;
import java.util.*;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.websvc.core.ClientCreator;
import org.netbeans.modules.websvc.core.CreatorProvider;
import org.netbeans.modules.websvc.core.jaxws.JaxWsUtils;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/** Wizard for adding web service clients to an application
 */
public class WebServiceClientWizardIterator implements TemplateWizard.Iterator {

    private int index = 0;
    private WizardDescriptor.Panel [] panels;

    private TemplateWizard wiz;
    // !PW FIXME How to handle freeform???
    private Project project;

    /** Entry point specified in layer
     */
    public static WebServiceClientWizardIterator create() {
        return new WebServiceClientWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new WebServiceClientWizardDescriptor()
        };
    }

    public void initialize(TemplateWizard wizard) {
        wiz = wizard;
        project = Templates.getProject(wiz);

        index = 0;
        panels = createPanels();

        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = JaxWsUtils.createSteps (beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }

            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            // Step name (actually the whole list for reference).
            jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        }
    }

    public void uninitialize(TemplateWizard wizard) {
        wiz = null;
        panels = null;
    }
    
    public Set/*FileObject*/ instantiate(TemplateWizard wiz) throws IOException {
        FileObject template = Templates.getTemplate( wiz );
        DataObject dTemplate = DataObject.find( template );                
        // jax-rpc split
        //new WebServiceClientCreator(project,wiz).create();
        ClientCreator creator = CreatorProvider.getClientCreator(project, wiz);
        if (creator!=null) creator.createClient();
                
        return Collections.singleton(dTemplate);
    }

    public String name() {
        return NbBundle.getMessage(WebServiceClientWizardIterator.class, "LBL_WebServiceClient"); // NOI18N
    }

    public WizardDescriptor.Panel current() {
       return panels[index];
    }

    public boolean hasNext() {
        return index < panels.length - 1;  
    }

    public void nextPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index++;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void previousPanel() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }

        index--;
    }

    public void addChangeListener(ChangeListener l) {
        // nothing to do yet
    }

    public void removeChangeListener(ChangeListener l) {
        // nothing to do yet
    }
}
