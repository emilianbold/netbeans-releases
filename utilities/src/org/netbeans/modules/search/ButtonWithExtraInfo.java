/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

/**
 * A radio-button with an extra information displayed by the main text.
 *
 * @author  Marian Petras
 */
class ButtonWithExtraInfo extends JRadioButton {

    private String extraInfo;
    private JLabel lblInfo;
    private JLabel lblStart, lblEnd;
    private boolean infoVisible;
    private Dimension infoPrefSize;
    private int infoBaseline = -1;
    private int infoGap = -1;
    private int startWidth = 0, endWidth = 0;

    public ButtonWithExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    private void checkInfoLabel() {
        if ((lblInfo == null) && (extraInfo != null)) {
            lblInfo = new JLabel(extraInfo);
            lblStart = new JLabel("(");                                 //NOI18N
            lblEnd = new JLabel(")");                                   //NOI18N

            final boolean enabled = isEnabled();
            lblInfo.setEnabled(enabled);
            lblStart.setEnabled(enabled);
            lblEnd.setEnabled(enabled);
        }
    }

    private void checkInfoGap() {
        if (infoGap == -1) {
            checkInfoLabel();
            infoGap = LayoutStyle.getInstance()
                      .getPreferredGap(this,
                                       lblInfo,
                                       ComponentPlacement.RELATED,
                                       SwingConstants.EAST,
                                       getParent());
            startWidth = lblStart.getPreferredSize().width;
            endWidth = lblEnd.getPreferredSize().width;
        }
    }

    private void checkInfoPrefSize() {
        if (infoPrefSize == null) {
            infoPrefSize = lblInfo.getPreferredSize();
            infoBaseline = lblInfo.getBaseline(infoPrefSize.width,
                                                         infoPrefSize.height);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (extraInfo == null) {
            return;
        }

        infoVisible = false;

        Dimension size = getSize();
        Dimension prefSize = getPreferredSize();
        if (size.width > prefSize.width) {
            int widthDelta = size.width - prefSize.width;
            checkInfoGap();
            if (widthDelta > (infoGap + startWidth + endWidth)) {
                infoVisible = true;
                Insets insets = getInsets();
                checkInfoPrefSize();
                assert infoBaseline != -1;
                int infoX = prefSize.width - insets.right + infoGap;
                int infoY = getBaseline(size.width,
                                                       size.height)
                            - infoBaseline;
                int infoWidth = Math.min(widthDelta - infoGap - (startWidth + endWidth),
                                         infoPrefSize.width);
                int infoHeight = infoPrefSize.height;
                lblInfo.setBounds(infoX,
                                  infoY,
                                  infoWidth,
                                  infoHeight);
                lblStart.setSize(startWidth, infoHeight);
                lblEnd.setSize(endWidth, infoHeight);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!infoVisible) {
            return;
        }

        Point infoLocation = lblInfo.getLocation();

        /* Paint the opening bracket: */
        g.translate(infoLocation.x, infoLocation.y);
        lblStart.paint(g);

        /* Paint the additional information: */
        g.translate(startWidth, 0);
        /*
         * If the label's text does not fit the reserved space, the text is
         * truncated and an ellipsis is painted at the end of the truncated
         * text. If no text fits, just the ellipsis is painted. If the label
         * is so narrow that even the ellipsis does not fit, the whole ellipsis
         * is still painted, thus leaking from the label's bounds. In this case,
         * the ellipsis may overlap the closing bracket. To prevent this
         * overlapping, a clip region is set to the label's area such that
         * nothing is painted over the label's bounds.
         */
        Shape originalClipShape = g.getClip();
        g.setClip(0, 0, lblInfo.getWidth(), lblInfo.getHeight());
        lblInfo.paint(g);
        g.setClip(originalClipShape);

        /* Paint the closing bracket: */
        g.translate(lblInfo.getWidth(), 0);
        lblEnd.paint(g);

        g.translate(-(infoLocation.x + startWidth + lblInfo.getWidth()),
                    -infoLocation.y);
    }

}
