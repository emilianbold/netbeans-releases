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

package org.netbeans.modules.options.indentation;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsCategory.PanelController;
import org.openide.util.HelpCtx;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class IndentationPanelController extends PanelController {

    private IndentationPanel indentationPanel = new IndentationPanel ();
    
    
    public void applyChanges () {
        indentationPanel.applyChanges ();
    }
    
    public void cancel () {
        indentationPanel.cancel ();
    }
    
    public boolean isValid () {
        return indentationPanel.dataValid ();
    }
    
    public boolean isChanged () {
        return indentationPanel.isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.editor.identation");
    }
    
    public JComponent getComponent () {
        return indentationPanel;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        indentationPanel.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        indentationPanel.removePropertyChangeListener (l);
    }
}