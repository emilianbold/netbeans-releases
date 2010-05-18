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
package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.screen.device.DevicePanel;
import org.netbeans.modules.vmd.screen.resource.ResourcePanel;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;


/**
 *
 * @author David Kaspar
 */
public class MainPanel extends JPanel {

    public static final Color BACKGROUND_COLOR = new Color (0xFBF9F3);

    public static final Color SELECT_COLOR = new Color (0xFF8500);
    public static final Color HOVER_COLOR = new Color (0x5B67B0);
    
    private static final Font LABEL_FONT = new Font("Dialog", Font.BOLD, 16); // NOI18N
    private static final Color LABEL_COLOR = new Color (0x88A3CF);
    
    private DevicePanel devicePanel;
    
    public MainPanel(DevicePanel devicePanel, ResourcePanel resourcePanel) {
        this.devicePanel = devicePanel;
        addMouseListener(new SelectionListener());
        
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(12, 12, 6, 6);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        JLabel devLabel = new JLabel(NbBundle.getMessage (MainPanel.class, "DISP_DeviceScreen")); // NOI18N
        devLabel.setForeground(LABEL_COLOR);
        devLabel.setHorizontalAlignment(JLabel.CENTER);
        devLabel.setFont(LABEL_FONT);
        add(devLabel, constraints);
        
        constraints.insets = new Insets(6, 12, 12, 6);
        constraints.gridy = 1;
        constraints.weighty = 0.0;
        add(devicePanel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weighty = 0.0;
        constraints.insets = new Insets(12, 6, 6, 12);
        JLabel resLabel = new JLabel(NbBundle.getMessage (MainPanel.class, "DISP_AssignedResources")); // NOI18N
        resLabel.setForeground(LABEL_COLOR);
        resLabel.setHorizontalAlignment(JLabel.CENTER);
        resLabel.setFont(LABEL_FONT);
        add(resLabel, constraints);
        
        constraints.gridy = 1;
        constraints.insets = new Insets(6, 6, 12, 12);
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        add(resourcePanel, constraints);

        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        JPanel filler = new JPanel ();
        filler.setBackground (BACKGROUND_COLOR);
        add(filler, constraints);

        // vlv: print
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
    }
    
    private class SelectionListener extends MouseAdapter implements Runnable {
        private DesignDocument document;
        
        public void mouseClicked(MouseEvent e) {
            document = devicePanel.getController().getDocument();
            document.getTransactionManager().writeAccess(this);
        }
        
        public void run() {
            if (document == null) {
                return;
            }
            document.setSelectedComponents(ScreenViewController.SCREEN_ID, Collections.<DesignComponent>emptySet());
        }
    }
}
