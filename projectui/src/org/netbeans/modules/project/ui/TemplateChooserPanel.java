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
import org.netbeans.api.project.SourceGroup;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

// XXX I18N

/**
 *
 * @author  Petr Hrebejk
 */

final class TemplateChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private TemplateChooserPanelGUI gui;

    private Project p;
    private String[] recommendedTypes;

    TemplateChooserPanel( Project p, String recommendedTypes[] ) {
        this.p = p;
        this.recommendedTypes = recommendedTypes;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new TemplateChooserPanelGUI(p, recommendedTypes);
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

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings(Object settings) {
        if ( gui != null ) {
            gui.initValues( p );
        }
        
        TemplateWizard wd = (TemplateWizard)settings;
        wd.setTitle( "New File - Choose File Type" );
        wd.putProperty( "WizardPanel_contentData", new String[] { "Choose Template", "..." } ); // NOI18N
        wd.putProperty( "WizardPanel_contentSelectedIndex", new Integer( 0 ) ); // NOI18N
    }

    public void storeSettings(Object settings) {
            
        TemplateWizard templateWizard = (TemplateWizard)settings;
        
        Object value = templateWizard.getValue();
        
        if ( NotifyDescriptor.CANCEL_OPTION != value &&
             NotifyDescriptor.CLOSED_OPTION != value ) {        
            try { 

                templateWizard.putProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT, gui.getProject() );
                templateWizard.setTemplate( DataObject.find( gui.getTemplate() ) );
            }
            catch( DataObjectNotFoundException e ) {
                // XXX
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
    