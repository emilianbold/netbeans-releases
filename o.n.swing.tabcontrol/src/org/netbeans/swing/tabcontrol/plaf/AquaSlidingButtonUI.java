/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;
import org.netbeans.swing.tabcontrol.plaf.GenericGlowingChiclet;

/**
 *
 * @author  mkleint
 */
public class AquaSlidingButtonUI extends SlidingButtonUI {
    
    private static AquaSlidingButtonUI AQUA_INSTANCE = null;
    
    static Color[] rollover = new Color[]{
        new Color(222, 222, 227), new Color(220, 238, 255), new Color(190, 247, 255),
        new Color(205, 205, 205)};
    
    
    /** Creates a new instance of AquaSlidingButtonUI */
    private AquaSlidingButtonUI() {
    }
    
    
    /** Aqua ui for sliding buttons.  This class is public so it can be
     * instantiated by UIManager, but is of no interest as API. */
    public static ComponentUI createUI(JComponent c) {
        if (AQUA_INSTANCE == null) {
            AQUA_INSTANCE = new AquaSlidingButtonUI();
        }
        return AQUA_INSTANCE;
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        c.setFont (UIManager.getFont("Tree.font"));
    }
    
   private ChicletWrapper chic = new ChicletWrapper();
   protected void paintBackground(Graphics2D graph, AbstractButton b) {
        chic.setNotch(false, false);

        chic.setState(((SlidingButton) b).isBlinkState() ? GenericGlowingChiclet.STATE_ATTENTION : 0);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth(), b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw(graph);
        chic.setAllowVertical(false);
    }    
    
    protected void paintButtonPressed(Graphics graph, AbstractButton b) {
        chic.setNotch(false, false);
        int state = 0;
        state |= b.getModel().isSelected() ? GenericGlowingChiclet.STATE_SELECTED : 0;
        state |= b.getModel().isPressed() ? GenericGlowingChiclet.STATE_PRESSED : 0;
        state |= b.getModel().isRollover() ? GenericGlowingChiclet.STATE_ACTIVE : 0;
        state |= ((SlidingButton) b).isBlinkState() ? GenericGlowingChiclet.STATE_ATTENTION : 0;
        chic.setState(state);
        chic.setArcs(0.5f, 0.5f, 0.5f, 0.5f);
        chic.setBounds(0, 1, b.getWidth(), b.getHeight() - 2);
        chic.setAllowVertical(true);
        chic.draw((Graphics2D)graph);
        chic.setAllowVertical(false);
    }
    
   // ********************************
    //          Layout Methods
    // ********************************
    public Dimension getPreferredSize(JComponent c) {
        SlidingButton slide = (SlidingButton) c;
	Dimension d = new Dimension(super.getPreferredSize(c));
        Insets i = c.getInsets();
        int orientation = slide.getOrientation();
        
        if (orientation == SlideBarDataModel.SOUTH) {
            if (i.top + i.bottom < 5)
                d.height += 5;
            if (i.left + i.right < 7)
                d.width += 7;
        }
        else {
            if (i.top + i.bottom < 7)
                d.height += 7;
            if (i.left + i.right < 5)
                d.width += 5;
        }

	return d;
    }
}
