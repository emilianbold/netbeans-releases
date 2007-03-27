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
package org.netbeans.modules.bpel.samples;

import java.awt.GridBagConstraints;
import org.openide.util.NbBundle;

public class TravelReservationServicePanelVisual extends SampleWizardPanelVisual{
    private static final long serialVersionUID = 1L;

    public TravelReservationServicePanelVisual(SampleWizardPanel panel) {
        super(panel);
    }

    protected String getDefaultProjectName() {
        return "TravelReservationService"; // NOI18N
    }

    protected void initAdditionalComponents() {
        projectNoteArea = new javax.swing.JTextArea();
        projectNoteArea.setEditable(false);
        projectNoteArea.setLineWrap(true);
        projectNoteArea.setWrapStyleWord(true);
        projectNoteArea.setOpaque(false);
        projectNoteArea.setFocusable(false);
        projectNoteArea.setText(NbBundle.getMessage(TravelReservationServicePanelVisual.class,"MSG_TRSProjectNote"));

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(projectNoteArea, gridBagConstraints);
    }
    
    private javax.swing.JTextArea projectNoteArea;
}
