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
