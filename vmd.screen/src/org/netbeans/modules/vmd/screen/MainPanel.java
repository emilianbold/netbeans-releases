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

import org.netbeans.modules.vmd.screen.resource.ResourcePanel;
import org.netbeans.modules.vmd.screen.device.DevicePanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class MainPanel extends JPanel {

    public MainPanel (DevicePanel devicePanel, ResourcePanel resourcePanel) {
        setLayout (new BorderLayout ());
        setBackground (new Color (0xFCFAF5));
        add (devicePanel, BorderLayout.WEST);
        add (resourcePanel, BorderLayout.CENTER);
    }

}
