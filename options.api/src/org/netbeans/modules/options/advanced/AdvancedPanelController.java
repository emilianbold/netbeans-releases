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
public final class AdvancedPanelController extends OptionsPanelController {

        
    public void update () {
        getAdvancedPanel ().update ();
    }
    
    public void applyChanges () {
        getAdvancedPanel ().applyChanges ();
    }
    
    public void cancel () {
        getAdvancedPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getAdvancedPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getAdvancedPanel ().isChanged ();
    }
        
    public Lookup getLookup () {
        return getAdvancedPanel ().getLookup ();
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        getAdvancedPanel ().setLoookup (masterLookup);
        return getAdvancedPanel ();
    }
    
    public HelpCtx getHelpCtx () {
        return getAdvancedPanel ().getHelpCtx ();
    }
    
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAdvancedPanel ().removePropertyChangeListener (l);
    }

    private AdvancedPanel advancedPanel;
    
    private AdvancedPanel getAdvancedPanel () {
        if (advancedPanel == null)
            advancedPanel = new AdvancedPanel ();
        return advancedPanel;
    }
}
