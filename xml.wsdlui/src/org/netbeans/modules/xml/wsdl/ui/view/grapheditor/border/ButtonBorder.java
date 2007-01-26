/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import org.netbeans.api.visual.border.Border;

/**
 *
 * @author anjeleevich
 */
public class ButtonBorder implements Border {
    private int top;
    private int left;
    private int bottom;
    private int right;

    public ButtonBorder(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Insets getInsets() {
        return new Insets(top, left, bottom, right);
    }

    public void paint(Graphics2D g2, Rectangle rect) {
        Paint oldPaint = g2.getPaint();

        g2.setPaint(BUTTON_BORDER_COLOR);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2.setPaint(BUTTON_BACKGROUND_COLOR);
        g2.fillRect(rect.x + 1, rect.y + 1, rect.width - 2, rect.height - 2);

        g2.setPaint(oldPaint);
    }

    public boolean isOpaque() {
        return true;
    }
    
    
    public static ButtonBorder createTextButtonBorder() {
        return new ButtonBorder(2, 8, 2, 8);
    }


    public static ButtonBorder createImageButtonBorder() {
        return new ButtonBorder(2, 2, 2, 2);
    }
    
    
    private static final Color BUTTON_BORDER_COLOR = new Color(0x7F9DB9);
    private static final Color BUTTON_BACKGROUND_COLOR = new Color(0xEBEBE4);
}
