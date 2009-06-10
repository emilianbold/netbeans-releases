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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EntityWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private EntityWizardPanel p;
    private List changeListeners = new ArrayList();
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new EntityWizardPanel(this);
            p.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(EntityWizardPanel.IS_VALID)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            stateChanged(null);
                        }
                    }
                }
            });
        }
        return p;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(EntityWizardDescriptor.class);
    }
    
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        if (SourceLevelChecker.isSourceLevel14orLower(project)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NeedProperSourceLevel")); // NOI18N
            return false;
        }
        if (p.getPrimaryKeyClassName().trim().equals("")) { //NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(EntityWizardDescriptor.class,"ERR_PrimaryKeyNotEmpty")); //NOI18N
            return false;
        }
        
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = Templates.getProject(wizardDescriptor);
            p.setProject(project);
        }
        
        try{
            if (ProviderUtil.isValidServerInstanceOrNone(project) && !isPersistenceUnitDefined()) {
                String warning = NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NoPersistenceUnit");
                p.setPersistenceUnitButtonVisibility(true, warning);
            } else {
                p.setPersistenceUnitButtonVisibility(false, null);
            }
        } catch (InvalidPersistenceXmlException ipx){
            String warning = NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_InvalidPersistenceXml", ipx.getPath());
            p.setPersistenceUnitButtonVisibility(false, warning);
        }
    }
    
    private boolean isPersistenceUnitDefined() throws InvalidPersistenceXmlException {
        return ProviderUtil.persistenceExists(project) || getPersistenceUnit() != null;
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.remove(l);
    }
    
    public void storeSettings(Object settings) {
        
    }
    
    public String getPrimaryKeyClassName() {
        return p.getPrimaryKeyClassName();
    }
    
    public PersistenceUnit getPersistenceUnit(){
        return p.getPersistenceUnit();
    }
    public boolean isFinishPanel() {
        return isValid();
    }
    
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (changeListeners) {
            it = new HashSet(changeListeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}

