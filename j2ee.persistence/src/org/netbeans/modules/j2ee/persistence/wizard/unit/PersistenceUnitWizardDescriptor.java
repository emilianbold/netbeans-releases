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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.entity.EntityWizardDescriptor;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class PersistenceUnitWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private PersistenceUnitWizardPanelDS p;
    private PersistenceUnitWizardPanelJdbc jdbcPanel;
    private PersistenceUnitWizardPanel panel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private boolean isContainerManaged;
    private static String ERROR_MSG_KEY = "WizardPanel_errorMessage";
    
    public PersistenceUnitWizardDescriptor(Project project) {
        this.project = project;
        this.isContainerManaged = Util.isSupportedJavaEEVersion(project);
    }
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public java.awt.Component getComponent() {
        if (panel == null) {
            if (isContainerManaged) {
                p = new PersistenceUnitWizardPanelDS(project, this, true);
                panel = p;
            } else {
                jdbcPanel= new PersistenceUnitWizardPanelJdbc(project, this, true);
                panel = jdbcPanel;
            }
            panel.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(PersistenceUnitWizardPanel.IS_VALID)) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            stateChanged(null);
                        }
                    }
                }
            });
        }
        return panel;
    }
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(PersistenceUnitWizardDescriptor.class);
    }
    
    public boolean isValid() {
        if (wizardDescriptor == null) {
            return true;
        }
        if (!ProviderUtil.isValidServerInstanceOrNone(project)){
            wizardDescriptor.putProperty(ERROR_MSG_KEY,
                    NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_MissingServer")); //NOI18N
            return false;
        }
        if (panel != null && !panel.isValidPanel()) {
            try {
                if (!panel.isNameUnique()){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_PersistenceUnitNameNotUnique")); //NOI18N
                }
            } catch (InvalidPersistenceXmlException ipx){
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NOI18N
                
            }
            return false;
        }
        wizardDescriptor.putProperty(ERROR_MSG_KEY, " "); //NOI18N
        return true;
    }
    
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        project = Templates.getProject(wizardDescriptor);
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    public void storeSettings(Object settings) {
    }
    
    public boolean isFinishPanel() {
        return isValid();
    }
    
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
    
    String getPersistenceUnitName() {
        return panel.getPersistenceUnitName();
    }
    
    Library getPersistenceLibrary() {
        return jdbcPanel == null ? null : jdbcPanel.getPersistenceLibrary();
    }
    
    DatabaseConnection getPersistenceConnection() {
        return jdbcPanel == null ? null : jdbcPanel.getPersistenceConnection();
    }
    
    String getDatasource() {
        return p == null ? null : p.getDatasource();
    }
    
    boolean isContainerManaged() {
        return isContainerManaged;
    }
    
    boolean isJTA() {
        return p == null ? false : p.isJTA();
    }
    
    boolean isNonDefaultProviderEnabled() {
        return p == null ? false : p.isNonDefaultProviderEnabled();
    }
    
    String getNonDefaultProvider() {
        return p == null ? null : p.getNonDefaultProvider();
    }
    
    String getTableGeneration() {
        return panel.getTableGeneration();
    }
    
    Provider getSelectedProvider(){
        return panel.getSelectedProvider();
    }
}
