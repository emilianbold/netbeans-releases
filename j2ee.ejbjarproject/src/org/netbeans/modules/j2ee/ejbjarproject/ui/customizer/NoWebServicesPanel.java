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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class NoWebServicesPanel extends JPanel implements HelpCtx.Provider {

    private JLabel label;

    /** Creates a new instance of NoWebServiceClientsPanel */
    public NoWebServicesPanel() {
        this(NbBundle.getMessage(NoWebServicesPanel.class, "LBL_CustomizeWsServiceHost_NoWebServices")); //NOI18N
    }
    
    /** Creates a new instance of NoWebServiceClientsPanel */
    public NoWebServicesPanel(String text) {
        setLayout(new GridBagLayout());

        label = new JLabel(text);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(label, gridBagConstraints);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerWSServiceHost.class.getName() + "Disabled"); // NOI18N
    }

}
