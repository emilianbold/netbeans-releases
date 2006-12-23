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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk
 */

final class TemplateChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private static String lastCategory = null;
    private static String lastTemplate = null;

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private TemplateChooserPanelGUI gui;

    private Project project;
    // private String[] recommendedTypes;

    TemplateChooserPanel( Project p /*, String recommendedTypes[] */ ) {
        this.project = p;
        /* this.recommendedTypes = recommendedTypes; */
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new TemplateChooserPanelGUI();
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {
        return gui != null && gui.getTemplate() != null;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> templist;
        synchronized (this) {
            templist = new ArrayList<ChangeListener> (listeners);
        }
	for (ChangeListener l: templist) {
            l.stateChanged(e);
        }
    }

    public void readSettings(Object settings) {
        TemplateChooserPanelGUI panel = (TemplateChooserPanelGUI) this.getComponent();
        panel.readValues( project, lastCategory, lastTemplate );
        ((WizardDescriptor)settings).putProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        ((WizardDescriptor)settings).putProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (TemplateChooserPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
                NbBundle.getBundle (TemplateChooserPanel.class).getString ("LBL_TemplatesPanel_Dots")}); // NOI18N
        // bugfix #44400: wizard title always changes
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }

    public void storeSettings(Object settings) {
            
        WizardDescriptor wd = (WizardDescriptor)settings;
        
        Object value = wd.getValue();
        
        if ( NotifyDescriptor.CANCEL_OPTION != value &&
             NotifyDescriptor.CLOSED_OPTION != value ) {        
            try { 

                Project newProject = gui.getProject ();
                if (!project.equals (newProject)) {
                    project = newProject;
                    wd.putProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT, newProject );
                }
                
                if (gui.getTemplate () == null) {
                    return ;
                }
                
                if (wd instanceof TemplateWizard) {
                    ((TemplateWizard)wd).setTemplate( DataObject.find( gui.getTemplate() ) );
                } else {
                    wd.putProperty( ProjectChooserFactory.WIZARD_KEY_TEMPLATE, gui.getTemplate () );
                }

                lastCategory = gui.getCategoryName();
                lastTemplate = gui.getTemplateName();
            }
            catch( DataObjectNotFoundException e ) {
                ErrorManager.getDefault().notify (e);
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        /*
        FileObject template = gui.getTemplate();
        p = gui.getProject();
        if (template != null) {
            setDelegate(findTemplateWizardIterator(template, p));
        } else {
            setDelegate(null);
        }
         */
        fireChange();
        
    }

}    
    