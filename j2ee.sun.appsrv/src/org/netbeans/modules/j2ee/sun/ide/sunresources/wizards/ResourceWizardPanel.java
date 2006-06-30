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

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;

public abstract class ResourceWizardPanel extends javax.swing.JPanel implements WizardDescriptor.FinishablePanel {

    private ArrayList list;

    /** Default preferred width of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_WIDTH = 600;
    /** Default preferred height of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_HEIGHT = 390;
      
    public ResourceWizardPanel() {
        list = new ArrayList();
    }

    /** @return preferred size of the wizard panel - it should be the same for all panels within one Wizard
    * so that the wizard dialog does not change its size when switching between panels */
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public HelpCtx getHelp() {
        return null; // HelpCtx.DEFAULT_HELP;
    }

    public java.awt.Component getComponent() {
        return this;
    }

    public void fireChange (Object source) {
        ArrayList lst;

        synchronized (this) {
            lst = (ArrayList) this.list.clone();
        }

        ChangeEvent event = new ChangeEvent(source);
        for (int i=0; i< lst.size(); i++){
            ChangeListener listener = (ChangeListener) lst.get(i);
            listener.stateChanged(event);
        }
    }

    public synchronized void addChangeListener (ChangeListener listener) {
        list.add(listener);
    }

    public synchronized void removeChangeListener (ChangeListener listener) {
        list.remove(listener);
    }
    
    public boolean isFinishPanel() {
        return false;
    }
}
