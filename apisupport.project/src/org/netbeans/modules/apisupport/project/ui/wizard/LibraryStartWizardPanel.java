/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;
import java.awt.Component;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * first panel of  the library wrapper module wizard
 *
 * @author Milos Kleint
 */
final class LibraryStartWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private LibraryStartVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public LibraryStartWizardPanel(WizardDescriptor settings) {
        super(settings);
    }
    
    public void readSettings(Object settings) {
        visualPanel.refreshData();
    }
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new LibraryStartVisualPanel(getSettings());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_LibraryStartPanel_Title")); // NOI18N
//            visualPanel.checkForm();
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(LibraryStartWizardPanel.class);
    }
}