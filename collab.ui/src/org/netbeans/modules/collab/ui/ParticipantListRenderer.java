/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabPrincipal;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;


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
