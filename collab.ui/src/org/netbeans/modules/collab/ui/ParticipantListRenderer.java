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
package org.netbeans.modules.collab.ui;

import java.awt.Component;
import javax.swing.*;

import com.sun.collablet.CollabPrincipal;

/**
 *
 *
 */
public class ParticipantListRenderer extends DefaultListCellRenderer {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private ListModel model = null;

    /**
     *
     *
     */
    public ParticipantListRenderer(ListModel model) {
        super();
        this.model = model;
        setOpaque(false);
    }

    /**
     *
     *
     */
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
    ) {
        if (model != null) {
            // Note, always draw the cell as if it's not selected
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, false, false);
            label.setText(model.getName(value));
            label.setOpaque(false);
            label.setIcon(model.getIcon(value));
            ToolTipManager.sharedInstance().registerComponent(list);

            // This should always be true
            if (value instanceof CollabPrincipal) {
                CollabPrincipal principal = (CollabPrincipal) value;
                list.setToolTipText(principal.getDisplayName());
            }

            return this;
        } else {
            return super.getListCellRendererComponent(list, value, index, false, false);
        }
    }
}
