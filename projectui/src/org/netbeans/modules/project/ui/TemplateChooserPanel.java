/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
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
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
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
    