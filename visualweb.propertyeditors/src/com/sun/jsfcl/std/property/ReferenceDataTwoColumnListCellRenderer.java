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
package com.sun.jsfcl.std.property;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import com.sun.jsfcl.std.reference.ReferenceDataItem;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReferenceDataTwoColumnListCellRenderer extends JPanel implements ListCellRenderer {
    protected static final int COLUMN_GAP = 5;
    protected static final Dimension MAX_DIMENSION = new Dimension(Short.MAX_VALUE, 0);
    protected static final Dimension MIN_DIMENSION = new Dimension(0, 0);
    protected static final Dimension COLUMN_GAP_DIMENSION = new Dimension(COLUMN_GAP, 0);

    protected int leftColumnWidth = -1;
    protected JLabel leftLabel;
    protected boolean wantsSecondLabel = true;
    protected JLabel rightLabel;

    public static String[] getLabels(ReferenceDataItem item) {

        String itemNameString = item.getName();
        Object itemValue = item.getValue();
        String itemValueString = " "; // NOI18N
        if (itemValue instanceof String) {
            itemValueString = (String)itemValue;
            if (itemValueString.equals(itemNameString)) {
                itemValueString = " "; //NOI18N
            } else {
                itemValueString = "[" + itemValueString + "]"; // NOI18N
            }
        }
        return new String[] {
            itemNameString, itemValueString};
    }

    public ReferenceDataTwoColumnListCellRenderer() {

        leftLabel = new JLabel();
        leftLabel.setOpaque(true);
        rightLabel = new JLabel();
        rightLabel.setOpaque(true);

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
//        add(Box.createRigidArea(new Dimension(2, 0)));
        add(leftLabel);
        add(new Box.Filler(null, null, null) {
            public Dimension getMinimumSize() {

                if (wantsSecondLabel) {
                    return COLUMN_GAP_DIMENSION;
                }
                return MIN_DIMENSION;
            }

            public Dimension getPreferredSize() {

                if (wantsSecondLabel) {
                    if (leftColumnWidth != -1) {
                        return new Dimension(Math.max(leftColumnWidth + COLUMN_GAP -
                            (int)leftLabel.getPreferredSize().getWidth(), COLUMN_GAP), 0);
                    }
                    return COLUMN_GAP_DIMENSION;
                }
                return MIN_DIMENSION;
            }

            public Dimension getMaximumSize() {

                if (wantsSecondLabel) {
                    if (leftColumnWidth == -1) {
                        return MAX_DIMENSION;
                    }
                    return new Dimension(Math.max(leftColumnWidth + COLUMN_GAP -
                        (int)leftLabel.getPreferredSize().getWidth(), COLUMN_GAP), 0);
                }
                return MIN_DIMENSION;
            }
        });
        add(rightLabel);
//        add(Box.createRigidArea(new Dimension(2, 0)));
        setOpaque(true);
    }

    public void adjustLeftColumnWidthIfNecessary() {

        int w = (int)leftLabel.getPreferredSize().getWidth();
        if (w > leftColumnWidth) {
            leftColumnWidth = w;
            invalidate();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
        ReferenceDataItem item;

        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
        item = (ReferenceDataItem)value;
        Color backgroundColor, foregroundColor;
        if (isSelected) {
            backgroundColor = list.getSelectionBackground();
            foregroundColor = list.getSelectionForeground();
        } else {
            backgroundColor = list.getBackground();
            foregroundColor = list.getForeground();
        }
        setBackground(backgroundColor);
        leftLabel.setBackground(backgroundColor);
        rightLabel.setBackground(backgroundColor);
        setForeground(foregroundColor);
        leftLabel.setForeground(foregroundColor);
        rightLabel.setForeground(foregroundColor);

        Font font = list.getFont();
        setFont(font);
        leftLabel.setFont(font);
        rightLabel.setFont(font);

        String[] labels = getLabels(item);
        leftLabel.setText(labels[0]);
        rightLabel.setText(labels[1]);
        invalidate();
        return this;
    }

    public void resetLeftColumnWidth() {

        leftColumnWidth = -1;
    }

}
