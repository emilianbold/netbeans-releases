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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author Pavel Buzek
 */
public final class EntitySelectionPanel extends AbstractPanel {
    private EntitySelectionPanelVisual component;
    
    /** Create the wizard panel descriptor. */
    public EntitySelectionPanel(String panelName, WizardDescriptor wizardDescriptor) {
        super(panelName, wizardDescriptor);
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isFinishPanel() {
        return false;
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        RestSupport support = project.getLookup().lookup(RestSupport.class);
        if(support == null) {
            setErrorMessage("MSG_EntitySelectionPanel_NotWebProject");
            return false;
        } else {
            /*if(!support.hasSwdpLibrary()) {
                setErrorMessage("MSG_EntitySelectionPanel_NoSWDP");
                return false;
            }*/
            if (getPersistenceUnitName(project) == null) {
                setErrorMessage("MSG_EntitySelectionPanel_NoPersistenceUnit");
                return false;
            }
        }
        return component.valid(wizardDescriptor);
    }

    public Component getComponent() {
        if (component == null) {
            component = new EntitySelectionPanelVisual(panelName);
            component.addChangeListener(this);
        }
        return component;
    }
    
    private String getPersistenceUnitName(Project project) {
        return getPersistenceUnitName(wizardDescriptor, project);
    }
    
    static String getPersistenceUnitName(WizardDescriptor wizard, Project project) {
        String puName = (String) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT_NAME);
        if (puName == null || puName.trim().length() == 0) {
            puName = PersistenceHelper.getPersistenceUnitName(project);
            wizard.putProperty(WizardProperties.PERSISTENCE_UNIT_NAME, puName);
        }
        return puName;
    }

}
