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

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.awt.Component;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Convenient class for implementing {@link org.openide.WizardDescriptor.InstantiatingIterator}.
 *
 * @author Radek Matous
 */
public abstract class BasicWizardIterator implements WizardDescriptor.InstantiatingIterator {
    protected BasicWizardIterator.BasicDataModel data;
    private int position = 0;
    private BasicWizardIterator.PrivateWizardPanel[] wizardPanels;
    
    /** Create a new wizard iterator. */
    protected BasicWizardIterator() {
    }
    
    /** @return all panels provided by this {@link org.openide.WizardDescriptor.InstantiatingIterator} */
    protected abstract BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz);
    
    protected abstract String getTitle();    
    
    /** Basic visual panel.*/
    public abstract static class Panel extends BasicVisualPanel {
        
        protected Panel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        /**
         * Returned name is used by a wizard. e.g. on its left side in the
         * <em>step list</em>.
         * @return name of panel
         */
        protected abstract String getPanelName();
        
        /**
         * Gives a chance to store an instance of {@link
         * BasicWizardIterator.BasicDataModel}. It is called when a panel is
         * going to be <em>hidden</em> (e.g. when switching to next/previous
         * panel).
         */
        protected abstract void storeToDataModel();
        
        /**
         * Gives a chance to refresh a panel (usually by reading a state of an
         * instance of {@link BasicWizardIterator.BasicDataModel}. It is called
         * when a panel is going to be <em>displayed</em> (e.g. when switching
         * from next/previous panel).
         */
        protected abstract void readFromDataModel();
        
        protected abstract HelpCtx getHelp();
        
    }
    
    /** DataModel that is passed through individual panels.*/
    public static class BasicDataModel {   
    }
    
    public final void initialize(WizardDescriptor wiz) {
        // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
        wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
        wiz.setTitle(getTitle()); // NOI18N        
        String[] beforeSteps = null;
        Object prop = wiz.getProperty("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        position = 0;
        BasicWizardIterator.Panel[] panels = createPanels(wiz);
        String[] steps = BasicWizardIterator.createSteps(beforeSteps, panels);
        wizardPanels = new BasicWizardIterator.PrivateWizardPanel[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            wizardPanels[i] = new BasicWizardIterator.PrivateWizardPanel(panels[i], steps, i);
        }
    }
    
    // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
    private static String[] createSteps(String[] before, BasicWizardIterator.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getPanelName();
            }
        }
        return res;
    }
    
    public final void uninitialize(WizardDescriptor wiz) {
        wizardPanels = null;
        uninitialize();
    }
    
    public void uninitialize() {
        wizardPanels = null;
    }
    
    public String name() {
        return ((BasicWizardIterator.PrivateWizardPanel)
        current()).getPanel().getPanelName();
    }
    
    public boolean hasNext() {
        return position < (wizardPanels.length - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel current() {
        return wizardPanels[position];
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    public final void addChangeListener(ChangeListener  l) {}
    public final void removeChangeListener(ChangeListener l) {}
    

    
    private static final class PrivateWizardPanel extends BasicWizardPanel {
        
        private BasicWizardIterator.Panel panel;
        
        PrivateWizardPanel(BasicWizardIterator.Panel panel, String[] allSteps, int stepIndex) {
            super(panel.getSettings());
            panel.addPropertyChangeListener(this);
            panel.setName(panel.getPanelName()); // NOI18N
            this.panel = panel;
            panel.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(stepIndex)); // NOI18N
            // names of currently used steps
            panel.putClientProperty("WizardPanel_contentData", allSteps); // NOI18N
            panel.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
            panel.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
            panel.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        }
        
        private BasicWizardIterator.Panel getPanel() {
            return panel;
        }
        
        public Component getComponent() {
            return getPanel();
        }
        
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) ||
                    WizardDescriptor.FINISH_OPTION.equals(wiz.getValue())) {
                panel.storeToDataModel();
            }
        }
        
        public void readSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) || wiz.getValue() == null) {
                panel.readFromDataModel();
            }
        }
        
        public HelpCtx getHelp() {
            return getPanel().getHelp();
        }
        
    }

    public void setData(BasicWizardIterator.BasicDataModel data) {
        this.data = data;
    }
        
    public BasicWizardIterator.BasicDataModel getData() {
        return data;
    }

    final public Set instantiate() throws IOException {
        return null;
    }    
}

