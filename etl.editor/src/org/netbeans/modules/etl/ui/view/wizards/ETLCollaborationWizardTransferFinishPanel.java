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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.Dimension;
import java.util.Collection;
import java.util.List;

import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;

/**
 * Extends ETLCollaborationWizardTransferPanel, implementing the interface
 * WizardDescriptor.FinishPanel to allows its containing wizard to enable the "Finish"
 * button.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ETLCollaborationWizardTransferFinishPanel extends ETLCollaborationWizardTransferPanel implements WizardDescriptor.FinishablePanel {

    private boolean enableNext = false;

    /**
     * Creates a new instance of ETLCollaborationWizardTransferPanel using the given
     * ListModels to initially populate the source and destination panels.
     * 
     * @param title String to be displayed as title of this panel
     * @param dsList List of DatabaseModels used to populate datasource panel
     * @param destColl Collection of DBTables used to populate table panel
     * @param sourceOTD true if this panel displays available selections for source OTDs;
     *        false if it displays available destination OTDsl
     */
    public ETLCollaborationWizardTransferFinishPanel(String title, List dsList, Collection destColl, boolean sourceOTD) {
        super(title, dsList, destColl, sourceOTD);
        this.setPreferredSize(new Dimension(575, 425));
        this.setSize(575, 425);
    }

    /**
     * Creates a new instance of ETLCollaborationWizardTransferPanel using the given
     * ListModels to initially populate the source and destination panels.
     * 
     * @param title String to be displayed as title of this panel
     * @param dsList List of DatabaseModels used to populate datasource panel
     * @param destColl Collection of DBTables used to populate table panel
     * @param sourceOTD true if this panel displays available selections for source OTDs;
     *        false if it displays available destination OTDsl
     * @param enableNext true if Next button should always be enabled for this panel.
     */
    public ETLCollaborationWizardTransferFinishPanel(String title, List dsList, Collection destColl, boolean sourceOTD, boolean enableNext) {
        super(title, dsList, destColl, sourceOTD);
        this.enableNext = enableNext;
        this.setPreferredSize(new Dimension(575, 425));
        this.setSize(575, 425);
    }

    /**
     * @see ETLCollaborationWizardTransferPanel#addChangeListener
     */
    public void addChangeListener(ChangeListener l) {
        super.addChangeListener(l);
    }

    /**
     * @see org.openide.WizardDescriptor.Panel#isValid
     */
    public boolean isValid() {
        if (enableNext) {
            return true;
        }

        return super.isValid();
    }

    /**
     * @see ETLCollaborationWizardTransferPanel#readSettings
     */
    public void readSettings(Object settings) {
        super.readSettings(settings);
    }

    /**
     * @see ETLCollaborationWizardTransferPanel#removeChangeListener
     */
    public void removeChangeListener(ChangeListener l) {
        super.removeChangeListener(l);
    }

    /**
     * @see ETLCollaborationWizardTransferPanel#storeSettings
     */
    public void storeSettings(Object settings) {
        super.storeSettings(settings);
    }

    public boolean isFinishPanel() {
        return true;
    }
}

