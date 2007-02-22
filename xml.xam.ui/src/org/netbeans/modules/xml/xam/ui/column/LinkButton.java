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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * A button that looks and feels like a hyperlink.
 *
 * <p><em>Based on HtmlTextLinkButton from ide/welcome module in
 * NetBeans IDE.</em></p>
 *
 * @author  S. Aubrecht
 * @author  Nathan Fiedler
 */
public class LinkButton extends JButton implements FocusListener, MouseListener {
    private static final long serialVersionUID = 1L;
    private static final Color LINK_COLOR = new Color(0x00, 0x00, 0xFF);
    private static final int FONT_SIZE;
    private boolean ignoreRevalidate = false;
    private Color defaultColor;

    static {
        Font defaultFont = UIManager.getFont("TextField.font"); // NOI18N
        FONT_SIZE = defaultFont != null ? defaultFont.getSize() : 12;
    }

    public LinkButton(String label) {
        super(label);
        setBorder(new EmptyBorder(1, 1, 1, 1));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setHorizontalAlignment(JLabel.LEFT);
        addMouseListener(this);
        setFocusable(true);
        setMargin(new Insets(0, 0, 0, 0));
        setBorderPainted(false);
        // Focus needs to be painted for accessibility purposes.
//        setFocusPainted(false);
        setRolloverEnabled(true);
        setContentAreaFilled(false);
        addFocusListener(this);
        defaultColor = getForeground();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        this.ignoreRevalidate = true;
        setForeground(LINK_COLOR);
        this.ignoreRevalidate = false;
    }

    public void mouseExited(MouseEvent e) {
        this.ignoreRevalidate = true;
        setForeground(defaultColor);
        this.ignoreRevalidate = false;
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Map rhints = (Map) Toolkit.getDefaultToolkit().getDesktopProperty(
                "awt.font.desktophints"); // NOI18N
        if (rhints == null && Boolean.getBoolean("swing.aatext")) { // NOI18N
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else if (rhints != null) {
            g2.addRenderingHints(rhints);
        }
        super.paintComponent(g2);
    }

    public void revalidate() {
        if (!ignoreRevalidate) {
            super.revalidate();
        }
    }

    public void invalidate() {
        if (!ignoreRevalidate) {
            super.invalidate();
        }
    }

    public void focusGained(FocusEvent e) {
        Rectangle rect = getBounds();
        rect.grow(0, FONT_SIZE);
        scrollRectToVisible(rect);
    }

    public void focusLost(FocusEvent e) {
    }
}
