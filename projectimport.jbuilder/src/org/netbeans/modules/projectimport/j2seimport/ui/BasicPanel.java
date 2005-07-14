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

package org.netbeans.modules.projectimport.j2seimport.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Radek Matous
 */
public abstract class BasicPanel extends JPanel {
    private BasicWizardPanel wiardPanel;
    private boolean isOK = false;

    public abstract int getPanelIndex();
    public abstract String getPanelDescription();    
    protected abstract void storeWizardData(WizardData data);
    protected abstract void readWizardData(WizardData data);
    
    public abstract void validateContent() throws org.openide.WizardValidationException;
    
    
    public final boolean isOK() {
        return isOK;
    }
    
    public final void setValid(boolean valid) {
        boolean fire = (isOK() != valid);
        isOK = valid;
        if (fire) {
            wiardPanel.fireChange();
        }
    }
            
    
    public final WizardDescriptor.Panel getWizardPanel() {
        if (wiardPanel == null) {            
            initPanel();
            wiardPanel = new BasicWizardPanel();
        }
        return wiardPanel;
    }
    
    final void initPanel() {
        putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
        putClientProperty("WizardPanel_contentSelectedIndex",  // NOI18N
                new Integer(getPanelIndex()));
        putClientProperty("WizardPanel_contentData", new String[] { // NOI18N
            getPanelDescription()
        });
        setPreferredSize(new java.awt.Dimension(500, 380));
    }

    
    public static class WizardData {
        private ErrorMessages errorMessages;
        public final void setErrorMessages(ErrorMessages errorMessages) {
            this.errorMessages = errorMessages;
        }
        
        public final ErrorMessages getErrorMessages() {
            return errorMessages;
        }
    }
    
    public interface ErrorMessages {
        void setError(String message);
    }
    
    
    private class BasicWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel {
        
        /** Registered ChangeListeners */
        private List changeListeners;
        
        /** Creates a new instance of BasicWizardPanel */
        public BasicWizardPanel() {
        }
        
        public void addChangeListener(ChangeListener l) {
            if (changeListeners == null) {
                changeListeners = new ArrayList(2);
            }
            changeListeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            if (changeListeners != null) {
                if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                    changeListeners = null;
                }
            }
        }
        
        public void fireChange() {
            if (changeListeners != null) {
                ChangeEvent e = new ChangeEvent(this);
                for (Iterator i = changeListeners.iterator(); i.hasNext(); ) {
                    ((ChangeListener) i.next()).stateChanged(e);
                }
            }
        }
        
        public final void storeSettings(Object settings) {
            BasicPanel.this.storeWizardData((BasicPanel.WizardData)settings);
        }
        
        public final void readSettings(Object settings) {
            BasicPanel.this.readWizardData((BasicPanel.WizardData)settings);
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return null;
        }
        
        
        public java.awt.Component getComponent() {
            return BasicPanel.this;
        }
        
        public boolean isValid() {
            return BasicPanel.this.isOK();
        }
        
        public void validate() throws org.openide.WizardValidationException {
            BasicPanel.this.validateContent();
        }
    }
    
    
}
