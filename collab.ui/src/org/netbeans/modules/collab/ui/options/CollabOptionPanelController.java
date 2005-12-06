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
package org.netbeans.modules.collab.ui.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author catlan
 */
final class CollabOptionPanelController extends OptionsPanelController {
    private final CollabOptionPanel panel = new CollabOptionPanel();
    
    public void update() {
        panel.update();
    }

    public void applyChanges() {
        panel.applyChanges();
    }

    public void cancel() {
        panel.cancel();
    }

    public boolean isValid() {
        return true;
    }

    public boolean isChanged() {
        return panel.isChanged();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx ("netbeans.optionsDialog.advanced.collab");
    }

    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        panel.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        panel.removePropertyChangeListener (l);
    }
    
}
