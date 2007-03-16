/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.ui.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

public final class ToolsPanelController extends OptionsPanelController {

    private ToolsPanel panel = new ToolsPanel();

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
        return panel.dataValid();
    }
    
    public boolean isChanged() {
        return panel.isChanged();
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.advanced.formEditor"); // NOI18N
    }
    
    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        panel.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        panel.removePropertyChangeListener(l);
    }
}
