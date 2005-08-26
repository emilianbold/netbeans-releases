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

package org.netbeans.modules.options.colors;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory.PanelController;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class FontAndColorsPanelController extends PanelController {

    private FontAndColorsPanel fontAndColorsPanel = new FontAndColorsPanel ();
    
    
    public void applyChanges () {
        fontAndColorsPanel.applyChanges ();
    }
    
    public void cancel () {
        fontAndColorsPanel.cancel ();
    }
    
    public boolean isValid () {
        return fontAndColorsPanel.dataValid ();
    }
    
    public boolean isChanged () {
        return fontAndColorsPanel.isChanged ();
    }
    
    public JComponent getComponent () {
        return fontAndColorsPanel;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        fontAndColorsPanel.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        fontAndColorsPanel.removePropertyChangeListener (l);
    }
}