/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.genericserver.ide;

import java.awt.Component;
import java.io.IOException;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.HelpCtx;

/**
 *
 * @author Martin Adamek
 */
public class GSInstantiatingIterator implements WizardDescriptor.InstantiatingIterator {

    private InstallPanel panel;
    
    public void removeChangeListener(ChangeListener l) {
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public void initialize(WizardDescriptor wizard) {
    }

    public void previousPanel() {
    }

    public void nextPanel() {
    }

    public String name() {
        return "Generic Server AddInstanceIterator";
    }

    public Set instantiate() throws IOException {
        return null;
    }

    public boolean hasPrevious() {
        return false;
    }

    public boolean hasNext() {
        return false;
    }

    public Panel current() {
        if (panel == null) {
            panel = new InstallPanel();
        }
        return panel;
    }
    
    private static class InstallPanel implements WizardDescriptor.Panel {
        public void removeChangeListener(ChangeListener l) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void storeSettings(Object settings) {
        }

        public void readSettings(Object settings) {
        }

        public boolean isValid() {
            return true;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public Component getComponent() {
            return new JPanel();
        }
        
    }
}
