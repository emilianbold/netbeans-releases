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
     * @param sourceDb true if this panel displays available selections for source Databases;
     *        false if it displays available destination Databases
     */
    public ETLCollaborationWizardTransferFinishPanel(String title, List dsList, Collection destColl, boolean sourceDb) {
        super(title, dsList, destColl, sourceDb);
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
     * @param sourceDb true if this panel displays available selections for source Databases;
     *        false if it displays available destination Databases
     * @param enableNext true if Next button should always be enabled for this panel.
     */
    public ETLCollaborationWizardTransferFinishPanel(String title, List dsList, Collection destColl, boolean sourceDb, boolean enableNext) {
        super(title, dsList, destColl, sourceDb);
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

