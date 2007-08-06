/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory.options;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.modules.localhistory.LocalHistorySettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public final class LocalHistoryOptionsController extends OptionsPanelController {
    
    private final LocalHistoryOptionsPanel panel;

    public LocalHistoryOptionsController() {
        panel = new LocalHistoryOptionsPanel();
    }   
        
    public void update() {        
        panel.daysTextField.setText(Long.toString(LocalHistorySettings.getInstance().getTTL()));
    }

    public void applyChanges() {        
        LocalHistorySettings.getInstance().setTTL(Integer.parseInt(panel.daysTextField.getText()));
    }

    public void cancel() {
        // do nothing
    }

    public boolean isValid() {
        try {            
            Long.parseLong(panel.daysTextField.getText());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean isChanged() {       
        String ttl = Long.toString(LocalHistorySettings.getInstance().getTTL());        
        return ttl.equals(panel.daysTextField.getText());
    }

    public JComponent getComponent(Lookup masterLookup) {
        return panel;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        // do nothing
    }

}
