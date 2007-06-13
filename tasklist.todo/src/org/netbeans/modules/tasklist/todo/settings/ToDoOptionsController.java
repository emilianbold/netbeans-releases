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

package org.netbeans.modules.tasklist.todo.settings;

import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author S. Aubrecht
 */
public class ToDoOptionsController extends OptionsPanelController {
    
    public void update() {
        getCustomizer().update();
    }

    public void applyChanges() {
        getCustomizer().applyChanges();
    }

    public void cancel() {
        //do nothing
    }

    public boolean isValid() {
        return getCustomizer().isDataValid();
    }

    public boolean isChanged() {
        return getCustomizer().isChanged();
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getCustomizer();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx ("netbeans.optionsDialog.advanced.todo");
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getCustomizer().addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getCustomizer().removePropertyChangeListener( l );
    }

    private ToDoCustomizer customizer;
    
    private ToDoCustomizer getCustomizer() {
        if( null == customizer ) {
            customizer = new ToDoCustomizer();
        }
        return customizer;
    }
}
