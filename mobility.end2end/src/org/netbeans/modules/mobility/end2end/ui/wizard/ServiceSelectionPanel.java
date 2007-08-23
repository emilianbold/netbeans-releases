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

/*
 * ServiceSelectionPanel.java
 *
 * Created on August 4, 2005, 3:45 PM
 *
 */
package org.netbeans.modules.mobility.end2end.ui.wizard;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.Project;
import org.netbeans.modules.mobility.end2end.classdata.ClassData;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.netbeans.modules.mobility.end2end.multiview.ServicesPanel;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class ServiceSelectionPanel implements TemplateWizard.Panel, ChangeListener {
    
    private ServicesPanel gui;
    
    private TemplateWizard templateWizard;
    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static final HelpCtx HELP_CTX = new HelpCtx( "me.wcb_serviceselection" ); // NOI18N
    
    public java.awt.Component getComponent() {
        if( gui == null ) {
            gui = new ServicesPanel(null, null);
            //gui.setName( NbBundle.getMessage( WebApplicationPanel.class, "TITLE_serviceSelectionStep" ));
            gui.addChangeListener(this);
        }
        return gui;
    }
    
    public HelpCtx getHelp() {
        return HELP_CTX;
    }
    
    public void readSettings( final Object settings ) {
        templateWizard = (TemplateWizard)settings;
        
        final Configuration configuration = (Configuration)templateWizard.
                getProperty( GenericServiceIterator.PROP_CONFIGURATION );
        ((ServicesPanel)getComponent()).setConfiguration(configuration);
        gui.setServerProjectFolder(
                ((Project)templateWizard.getProperty(GenericServiceIterator.PROP_SERVER_PROJECT)).getProjectDirectory());
        
        // Check for validity
        isValid();
    }
    
    public void storeSettings(final Object settings) {
        templateWizard = (TemplateWizard)settings;
        templateWizard.putProperty( GenericServiceIterator.PROP_CONFIGURATION, gui.getConfiguration() );
    }
    
    public boolean isValid() {
        final List<ClassData> data = gui.getConfiguration().getServices().get(0).getData();
        final boolean valid = !( data == null || data.size() == 0 );
        if( valid ){
            templateWizard.putProperty( "WizardPanel_errorMessage", " " ); // NOI18N`
        } else {
            templateWizard.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage( ServiceSelectionPanel.class, "ERR_NoService" )); // NOI18N
        }
        return valid;
    }
    
    public void addChangeListener( final ChangeListener changeListener ) {
        listeners.add( changeListener );
    }
    
    public void removeChangeListener( final ChangeListener changeListener ) {
        listeners.remove( changeListener );
    }
    
    public void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        fireChange();
    }
}
