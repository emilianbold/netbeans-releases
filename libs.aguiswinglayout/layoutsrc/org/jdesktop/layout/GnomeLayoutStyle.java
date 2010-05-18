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
