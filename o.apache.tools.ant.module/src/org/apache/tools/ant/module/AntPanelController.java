/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module;

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
public final class AntPanelController extends OptionsPanelController {
    
    @Override
    public void update () {
        getAntCustomizer ().update ();
    }
    
    @Override
    public void applyChanges () {
        getAntCustomizer ().applyChanges ();
    }
    
    @Override
    public void cancel () {
        getAntCustomizer ().cancel ();
    }
    
    @Override
    public boolean isValid () {
        return getAntCustomizer ().dataValid ();
    }
    
    @Override
    public boolean isChanged () {
        return getAntCustomizer ().isChanged ();
    }
    
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.advanced.ant");
    }
    
    @Override
    public JComponent getComponent (Lookup lookup) {
        return getAntCustomizer ();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().addPropertyChangeListener (l);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener l) {
        getAntCustomizer ().removePropertyChangeListener (l);
    }

    
    private AntCustomizer antCustomizer;
    
    private AntCustomizer getAntCustomizer () {
        if (antCustomizer == null)
            antCustomizer = new AntCustomizer ();
        return antCustomizer;
    }
}
