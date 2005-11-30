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

package org.netbeans.modules.form;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class FormEditorPanelController extends OptionsPanelController {

    private FormEditorCustomizer customizer = new FormEditorCustomizer ();
    private boolean initialized = false;
    
    
    public void update () {
        initialized = true;
        customizer.update ();
    }
    
    public void applyChanges () {
        if (initialized) {
            customizer.applyChanges ();
        }
        initialized = false;
    }
    
    public void cancel () {
        customizer.cancel ();
        initialized = false;
    }
    
    public boolean isValid () {
        return customizer.dataValid ();
    }
    
    public boolean isChanged () {
        return customizer.isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.advanced.formEditor"); // NOI18N
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return customizer;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        customizer.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        customizer.removePropertyChangeListener (l);
    }
}
