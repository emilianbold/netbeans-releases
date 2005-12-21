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
import org.netbeans.spi.options.OptionsPanelController;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

  
/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class IndentationPanelController extends OptionsPanelController {
    
    
    public void update () {
        getIndentationPanel ().update ();
    }
    
    public void applyChanges () {
        getIndentationPanel ().applyChanges ();
    }
    
    public void cancel () {
        getIndentationPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getIndentationPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getIndentationPanel ().isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.editor.identation");
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getIndentationPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getIndentationPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getIndentationPanel ().removePropertyChangeListener (l);
    }

    private IndentationPanel indentationPanel = new IndentationPanel ();
    
    private IndentationPanel getIndentationPanel () {
        if (indentationPanel == null)
            indentationPanel = new IndentationPanel ();
        return indentationPanel;
    }
}
