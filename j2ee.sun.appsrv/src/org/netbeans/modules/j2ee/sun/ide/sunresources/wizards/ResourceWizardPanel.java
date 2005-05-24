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
