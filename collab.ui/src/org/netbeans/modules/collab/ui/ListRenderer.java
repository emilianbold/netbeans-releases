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

import java.awt.*;
import javax.swing.*;

import com.sun.collablet.CollabPrincipal;
import com.sun.collablet.Conversation;

public final class ListRenderer extends DefaultListCellRenderer {
    private ListModel _model = null;
    private Color _fontColor = null;
    private int _fontSize = 0;

    public ListRenderer(ListModel m) {
        super();
        this._model = m;
        setOpaque(false);
    }

    public void setFontColor(Color color) {
        _fontColor = color;
    }

    public void setFontSize(int size) {
        _fontSize = size;
    }

    public void setFontSize(String size) {
        setFontSize(Integer.parseInt(size));
    }

    final public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
    ) {
        if (_model != null) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setText(_model.getName(value));
            lbl.setOpaque(isSelected);

            // set the icon for login or never login people..
            lbl.setIcon(_model.getIcon(value));
            ToolTipManager.sharedInstance().registerComponent(list);

            if (value instanceof CollabPrincipal) {
                CollabPrincipal nu = (CollabPrincipal) value;

                list.setToolTipText(nu.getDisplayName());
            } else if (value instanceof Conversation) {
                Conversation c = (Conversation) value;
                list.setToolTipText(c.getDisplayName());
            } else if (value instanceof String) {
                list.setToolTipText((String) value);
            }

            if ((_fontColor != null) && (!isSelected)) {
                lbl.setForeground(_fontColor);
            }

            if (_fontSize > 0) {
                lbl.setFont(lbl.getFont().deriveFont((float) _fontSize));
            }

            return this;
        } else {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
