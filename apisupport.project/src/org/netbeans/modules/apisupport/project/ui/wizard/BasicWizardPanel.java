/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Basic wizard panel for APISupport projects.
 *
 * @author Martin Krauskopf
 */
public abstract class BasicWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {
    
    private boolean valid = true;
    private WizardDescriptor settings;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    protected BasicWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    public void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    protected WizardDescriptor getSettings() {
        return settings;
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    protected void fireChange() {
        changeSupport.fireChange();
    }
    
    /**
     * Convenience method for accessing Bundle resources from this package.
     */
    protected final String getMessage(String key) {
        return NbBundle.getMessage(getClass(), key);
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public void storeSettings(WizardDescriptor settings) {}
    
    public void readSettings(WizardDescriptor settings) {}
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Mainly for receiving events from wrapped component about its validity.
     * Firing events further to Wizard descriptor so it will reread this panel's
     * state and reenable/redisable its next/prev/finish/... buttons.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("valid".equals(evt.getPropertyName())) { // NOI18N
            boolean nueValid = ((Boolean) evt.getNewValue()).booleanValue();
            if (nueValid != valid) {
                valid = nueValid;
                fireChange();
            }
        }
    }
    
    abstract static class NewTemplatePanel extends BasicWizardPanel {
        
        private final NewModuleProjectData data;
        
        NewTemplatePanel(final NewModuleProjectData data) {
            super(data.getSettings());
            this.data = data;
        }
        
        abstract void reloadData();
        abstract void storeData();
        
        public NewModuleProjectData getData() {
            return data;
        }
        
        public @Override void readSettings(WizardDescriptor settings) {
            reloadData();
        }
        
        public @Override void storeSettings(WizardDescriptor settings) {
            storeData();
        }
        
        protected String getWizardTypeString() {
            String helpId = null;
            switch (data.getWizardType()) {
                case SUITE:
                    helpId = "suite"; // NOI18N
                    break;
                case APPLICATION:
                    helpId = "application"; // NOI18N
                    break;
                case MODULE:
                case SUITE_COMPONENT:
                    helpId = "module"; // NOI18N
                    break;
                case LIBRARY_MODULE:
                    helpId = "library"; // NOI18N
                    break;
                default:
                    assert false : "Unknown wizard type = " + data.getWizardType();
            }
            return helpId;
        }
        
    }
    
}
