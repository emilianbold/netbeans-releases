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

package org.netbeans.modules.mobility.cldcplatform.wizard;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Adam Sotona
 */
public class FindWizardPanel implements WizardDescriptor.FinishablePanel {
    
    ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
    FindPanel component = new FindPanel(this);
    
    public boolean isFinishPanel() {
        return false;
    }
    
    public Component getComponent() {
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(DetectWizardPanel.class);
    }
    
    public void readSettings(final Object object) {
        component.readSettings((WizardDescriptor) object);
    }
    
    public void storeSettings(final Object object) {
        component.storeSettings((WizardDescriptor) object);
    }
    
    public boolean isValid() {
        return component.isValid();
    }
    
    public void addChangeListener(final ChangeListener changeListener) {
        listeners.add(changeListener);
    }
    
    public void removeChangeListener(final ChangeListener changeListener) {
        listeners.remove(changeListener);
    }
    
    public void fireChanged() {
        final ChangeEvent e = new ChangeEvent(this);
        final Object[] la = listeners.toArray();
        for (int i = 0; i < la.length; i++) {
            final ChangeListener l = (ChangeListener) la[i];
            l.stateChanged(e);
        }
    }
    
}
