/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    public String getName() {
        return getPanelDescription();
    }
    
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
        putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,  // NOI18N
                new Integer(getPanelIndex()));
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N
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
