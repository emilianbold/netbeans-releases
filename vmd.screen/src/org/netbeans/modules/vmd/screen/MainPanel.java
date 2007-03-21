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
 *
 */

package org.netbeans.modules.vmd.screen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import org.netbeans.modules.vmd.screen.resource.ResourcePanel;
import org.netbeans.modules.vmd.screen.device.DevicePanel;


/**
 * 
 * @author David Kaspar
 */
public class MainPanel extends JPanel {
    
    public MainPanel(DevicePanel devicePanel, ResourcePanel resourcePanel) {
        setLayout(new GridBagLayout());
        setBackground(ResourcePanel.BACKGROUND_COLOR);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.insets = new Insets(12, 12, 12, 6);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridy = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        add(devicePanel, constraints);

        constraints.weightx = 1.0;
        constraints.insets = new Insets(12, 6, 12, 12);
        add(resourcePanel, constraints);
    }
    
}
