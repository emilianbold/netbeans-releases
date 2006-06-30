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

package org.netbeans.modules.form.wizard;

import java.beans.*;
import javax.swing.event.*;
import org.netbeans.modules.form.*;

/**
 * The second panel of connection wizard - for selecting what to perform on
 * the target component (set a property, call a method or execute some user code).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel2 implements org.openide.WizardDescriptor.Panel {

    static final int METHOD_TYPE = 0;
    static final int PROPERTY_TYPE = 1;
    static final int CODE_TYPE = 2;

    private RADComponent targetComponent;

    private EventListenerList listenerList;

    private ConnectionPanel2 uiPanel;

    // -------

    ConnectionWizardPanel2(RADComponent target) {
        targetComponent = target;
    }

    RADComponent getTargetComponent() {
        return targetComponent;
    }

    int getActionType() {
        return uiPanel != null ? uiPanel.getActionType() : -1 ;
    }

    MethodDescriptor getSelectedMethod() {
        return uiPanel != null ? uiPanel.getSelectedMethod() : null;
    }

    PropertyDescriptor getSelectedProperty() {
        return uiPanel != null ? uiPanel.getSelectedProperty() : null;
    }

    // ----------
    // WizardDescriptor.Panel implementation

    public java.awt.Component getComponent() {
        if (uiPanel == null)
            uiPanel = new ConnectionPanel2(this);
        return uiPanel;
    }

    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.target"); // NOI18N
    }

    public boolean isValid() {
        return getActionType() == CODE_TYPE
               || getSelectedMethod() != null
               || getSelectedProperty() != null;
    }

    public void readSettings(java.lang.Object settings) {
    }

    public void storeSettings(java.lang.Object settings) {
    }

    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // --------

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
