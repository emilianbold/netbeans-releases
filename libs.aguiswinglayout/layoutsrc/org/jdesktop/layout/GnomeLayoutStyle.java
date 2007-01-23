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

package org.jdesktop.layout;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.SwingConstants;


/**
 * An implementation of <code>LayoutStyle</code> for Gnome.  This information
 * comes from:
 * http://developer.gnome.org/projects/gup/hig/2.0/design-window.html#window-layout-spacing
 *
 * @version $Revision$
 */
class GnomeLayoutStyle extends LayoutStyle {
    public int getPreferredGap(JComponent source, JComponent target,
                          int type, int position, Container parent) {
        // Invoke super to check arguments.
        super.getPreferredGap(source, target, type, position, parent);

        if (type == INDENT) {
            if (position == SwingConstants.EAST || position == SwingConstants.WEST) {
                int gap = getButtonChildIndent(source, position);
                if (gap != 0) {
                    return gap;
                }
                // Indent group members 12 pixels to denote hierarchy and
                // association.
                return 12;
            }
            // Treat vertical INDENT as RELATED
            type = RELATED;
        }
        // Between labels and associated components, leave 12 horizontal
        // pixels.
        if (position == SwingConstants.EAST ||
                        position == SwingConstants.WEST) {
            boolean sourceLabel = (source.getUIClassID() == "LabelUI");
            boolean targetLabel = (target.getUIClassID() == "LabelUI");
            if ((sourceLabel && !targetLabel) || 
                    (!sourceLabel && targetLabel)) {
                return 12;
            }
        }
        // As a basic rule of thumb, leave space between user
        // interface components in increments of 6 pixels, going up as
        // the relationship between related elements becomes more
        // distant. For example, between icon labels and associated
        // graphics within an icon, 6 pixels are adequate. Between
        // labels and associated components, leave 12 horizontal
        // pixels. For vertical spacing between groups of components,
        // 18 pixels is adequate.
        //
        // The first part of this is handled automatically by Icon (which
        // won't give you 6 pixels).
        if (type == RELATED) {
            return 6;
        }
        return 12;
    }

    public int getContainerGap(JComponent component, int position,
            Container parent) {
        super.getContainerGap(component, position, parent);
        // A general padding of 12 pixels is
        // recommended between the contents of a dialog window and the
        // window borders.
        //
        // Indent group members 12 pixels to denote hierarchy and association.
        return 12;
    }
}
