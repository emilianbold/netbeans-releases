/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.advanced;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory.PanelController;
import org.openide.util.HelpCtx;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AdvancedPanelController extends PanelController {

    private AdvancedPanel advancedPanel = new AdvancedPanel ();

    
    public void applyChanges () {
        advancedPanel.applyChanges ();
    }
    
    public void cancel () {
        advancedPanel.cancel ();
    }
    
    public boolean isValid () {
        return advancedPanel.dataValid ();
    }
    
    public boolean isChanged () {
        return advancedPanel.isChanged ();
    }
    
    public JComponent getComponent () {
        return advancedPanel;
    }
    
    public HelpCtx getHelpCtx () {
        return advancedPanel.getHelpCtx ();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        advancedPanel.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        advancedPanel.removePropertyChangeListener (l);
    }
}