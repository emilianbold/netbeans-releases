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

package org.apache.tools.ant.module;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory.PanelController;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class AntPanelController extends PanelController {

    private AntCustomizer antCustomizer = new AntCustomizer ();
    
    
    public void applyChanges () {
        antCustomizer.applyChanges ();
    }
    
    public void cancel () {
        antCustomizer.cancel ();
    }
    
    public boolean isValid () {
        return antCustomizer.dataValid ();
    }
    
    public boolean isChanged () {
        return antCustomizer.isChanged ();
    }
    
    public JComponent getComponent () {
        return antCustomizer;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        antCustomizer.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        antCustomizer.removePropertyChangeListener (l);
    }
}