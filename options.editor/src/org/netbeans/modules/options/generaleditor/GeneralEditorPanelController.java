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

package org.netbeans.modules.options.generaleditor;

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
public final class GeneralEditorPanelController extends OptionsPanelController {

    
    public void update () {
        getGeneralEditorPanel ().update ();
    }
    
    public void applyChanges () {
        getGeneralEditorPanel ().applyChanges ();
    }
    
    public void cancel () {
        getGeneralEditorPanel ().cancel ();
    }
    
    public boolean isValid () {
        return getGeneralEditorPanel ().dataValid ();
    }
    
    public boolean isChanged () {
        return getGeneralEditorPanel ().isChanged ();
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("netbeans.optionsDialog.editor.general");
    }
    
    public JComponent getComponent (Lookup masterLookup) {
        return getGeneralEditorPanel ();
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        getGeneralEditorPanel ().addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        getGeneralEditorPanel ().removePropertyChangeListener (l);
    }
    
    
    private GeneralEditorPanel generalEditorPanel;
    
    private GeneralEditorPanel getGeneralEditorPanel () {
        if (generalEditorPanel == null)
            generalEditorPanel = new GeneralEditorPanel ();
        return generalEditorPanel;
    }
}

