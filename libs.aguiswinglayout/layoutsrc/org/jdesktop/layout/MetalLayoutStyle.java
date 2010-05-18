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
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.lang.reflect.*;

/**
 * An implementation of <code>LayoutStyle</code> for the java look and feel.
 * This information comes from the
 * <a href="http://java.sun.com/products/jlf/ed2/book/HIG.Visual2.html">
 * The Java Look and Feel Design Guidelines</a>.
 *
 * @version $Revision$
 */
class MetalLayoutStyle extends LayoutStyle {
    /**
     * Whether or not we're using ocean, the default metal theme in 1.5.
     */
    private boolean isOcean;

    public MetalLayoutStyle() {
        isOcean = false;
        try {
            Method method = MetalLookAndFeel.class.
                getMethod("getCurrentTheme", (Class[])null);
            isOcean = ((MetalTheme)method.invoke(null, (Object[])null)).
                      getName() == "Ocean";
        } catch (NoSuchMethodException nsme) {
        } catch (IllegalAccessException iae) {
        } catch (IllegalArgumentException iae2) {
        } catch (InvocationTargetException ite) {
        }
    }

    // NOTE: The JLF makes reference to a number of guidelines in terms of
    // 6 pixels - 1 pixel.  The rationale is because steel buttons have
    // a heavy border followed by a light border, and so that if you pad
    // by 6 pixels it'll look like 7.  Using 5 pixels than produces an effect
    // of 6 pixels.  With Ocean things are different, the only component
    // that you want this behavior to happen with is checkboxs.

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
                return 12;
            }
            // Treat vertical INDENT as RELATED
            type = RELATED;
        }
        
        String sourceCID = source.getUIClassID();
        String targetCID = target.getUIClassID();
        int offset;

        if (type == RELATED) {
            if (sourceCID == "ToggleButtonUI" &&
                      targetCID == "ToggleButtonUI") {
                ButtonModel sourceModel = ((JToggleButton)source).getModel();
                ButtonModel targetModel = ((JToggleButton)target).getModel();
                if ((sourceModel instanceof DefaultButtonModel) &&
                    (targetModel instanceof DefaultButtonModel) &&
                    (((DefaultButtonModel)sourceModel).getGroup() ==
                     ((DefaultButtonModel)targetModel).getGroup()) &&
                        ((DefaultButtonModel)sourceModel).getGroup() != null) {
                    // When toggle buttons are exclusive (that is, they form a
                    // radio button set), separate them with 2 pixels. This
                    // rule applies whether the toggle buttons appear in a
                    // toolbar or elsewhere in the interface.
                    // Note: this number does not appear to include any borders
                    // and so is not adjusted by the border of the toggle
                    // button
                    return 2;
                }
                // When toggle buttons are independent (like checkboxes)
                // and used outside a toolbar, separate them with 5
                // pixels.
                if (isOcean) {
                    return 6;
                }
                return 5;
            }
            offset = 6;
        }
        else {
            offset = 12;
        }
        if ((position == SwingConstants.EAST ||
             position == SwingConstants.WEST) &&
            ((sourceCID == "LabelUI" && targetCID != "LabelUI") ||
             (sourceCID != "LabelUI" && targetCID == "LabelUI"))) {
            // Insert 12 pixels between the trailing edge of a
            // label and any associated components. Insert 12
            // pixels between the trailing edge of a label and the
            // component it describes when labels are
            // right-aligned. When labels are left-aligned, insert
            // 12 pixels between the trailing edge of the longest
            // label and its associated component
            return getCBRBPadding(source, target, position, offset + 6);
        }
        return getCBRBPadding(source, target, position, offset);
    }

    int getCBRBPadding(JComponent source, JComponent target, int position,
                       int offset) {
        offset = super.getCBRBPadding(source, target, position, offset);
        if (offset > 0) {
            int buttonAdjustment = getButtonAdjustment(source, position);
            if (buttonAdjustment == 0) {
                buttonAdjustment = getButtonAdjustment(target,
                                                       flipDirection(position));
            }
            offset -= buttonAdjustment;
        }
        if (offset < 0) {
            return 0;
        }
        return offset;
    }

    private int getButtonAdjustment(JComponent source, int edge) {
        String uid = source.getUIClassID();
        if (uid == "ButtonUI" || uid == "ToggleButtonUI") {
            if (!isOcean && (edge == SwingConstants.EAST ||
                             edge == SwingConstants.SOUTH)) {
                return 1;
            }
        }
        else if (edge == SwingConstants.SOUTH) {
            if (uid == "RadioButtonUI" || (!isOcean && uid == "CheckBoxUI")) {
                return 1;
            }
        }
        return 0;
    }

    public int getContainerGap(JComponent component, int position,
            Container parent) {
        super.getContainerGap(component, position, parent);
        // Here's the rules we should be honoring:
        //
        // Include 11 pixels between the bottom and right
        // borders of a dialog box and its command
        // buttons. (To the eye, the 11-pixel spacing appears
        // to be 12 pixels because the white borders on the
        // lower and right edges of the button components are
        // not visually significant.)
        // NOTE: this last text was designed with Steel in mind, not Ocean.
        //
        // Insert 12 pixels between the edges of the panel and the
        // titled border. Insert 11 pixels between the top of the
        // title and the component above the titled border. Insert 12
        // pixels between the bottom of the title and the top of the
        // first label in the panel. Insert 11 pixels between
        // component groups and between the bottom of the last
        // component and the lower border.
        return getCBRBPadding(component, position, 12 -
                getButtonAdjustment(component, position));
    }
}
