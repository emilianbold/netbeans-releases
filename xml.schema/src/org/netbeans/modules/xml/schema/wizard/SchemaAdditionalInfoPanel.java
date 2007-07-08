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

package org.netbeans.modules.xml.schema.wizard;

//java imports
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//netbeans imports
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;


/**
 * This class represents the data for the schema panel wizard.
 * Read http://performance.netbeans.org/howto/dialogs/wizard-panels.html.
 * 
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaAdditionalInfoPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private SchemaAdditionalInfoGUI gui;
    private TemplateWizard templateWizard;

    /**
     * Empty constructor.
     */
    SchemaAdditionalInfoPanel() {
        super();
    }
    
    /**
     * Returns the template wizard.
     */
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    } 

    /**
     * Returns the GUI associated with this WizardDescriptor.
     * This is where, the gui panel gets created.
     */
    public Component getComponent() {
        if (gui == null) {
            gui = new SchemaAdditionalInfoGUI();
        }
        return gui;
    }

    /**
     * Returns the help context.
     */
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * If true, enables the FINISH button, else not.
     */
    public boolean isValid() {
        return true;
    }

    /**
     * Allows addition of listeners.
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * Allows deletion of listeners.
     */
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    /**
     *
     */
    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    /**
     *
     */
    public void readSettings( Object settings ) {
        templateWizard = (TemplateWizard)settings;
	gui.attachListenerToFileName(templateWizard);
    }
    
    /**
     *
     */
    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    /**
     *
     */
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }        
    
}
