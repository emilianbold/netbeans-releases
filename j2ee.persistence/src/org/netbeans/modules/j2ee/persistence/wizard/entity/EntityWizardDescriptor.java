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
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
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
        if (ProviderUtil.isSourceLevel14orLower(project)) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage",
                    NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NeedProperSourceLevel")); // NOI18N
            return false;
        }
        if (p.getPrimaryKeyClassName().trim().equals("")) { //NOI18N
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityWizardDescriptor.class,"ERR_PrimaryKeyNotEmpty")); //NOI18N
            return false;
        }
        try{
            if (!isPersistenceUnitDefined()) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NoPersistenceUnit"));
                return true; // just warning
            }
        } catch (InvalidPersistenceXmlException ipx){
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_InvalidPersistenceXml", ipx.getPath()));
            return true; // just a warning
        }
        
        wizardDescriptor.putProperty("WizardPanel_errorMessage", " "); //NOI18N
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
                wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_NoPersistenceUnit"));
                p.setPersistenceUnitButtonVisibility(true);
            }
        } catch (InvalidPersistenceXmlException ipx){
            wizardDescriptor.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_InvalidPersistenceXml", ipx.getPath()));
            p.setPersistenceUnitButtonVisibility(false);
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

