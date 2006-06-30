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

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.cookies.LinkCookie;

import javax.swing.*;

/** LinkButton.java
 *
 * Created on November 19, 2004, 8:06 AM
 * @author mkuchtiak
 */
public class LinkButton extends JButton {

    /** Creates a new instance of LinkButton */
    public LinkButton(LinkCookie panel, Object ddBean, String ddProperty) {
        super();
        initLinkButton(this, panel, ddBean, ddProperty);
    }

    public static void initLinkButton(final AbstractButton button, LinkCookie panel, Object ddBean, String ddProperty) {
        button.setForeground(SectionVisualTheme.hyperlinkColor);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new java.awt.Insets(2, 2, 2, 2));
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        String text = "<html><b><u>" + button.getText() + "</u></b></html>";
        button.setAction(new LinkAction(panel, ddBean, ddProperty));
        button.setText(text);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                button.setForeground(SectionVisualTheme.hyperlinkColorFocused);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setCursor(java.awt.Cursor.getDefaultCursor());
                button.setForeground(SectionVisualTheme.hyperlinkColor);
            }
        });
    }

    public void setText(String text) {
        super.setText("<html><b><u>"+text+"</u></b></html>");
    }

    public static class LinkAction extends AbstractAction {
        LinkCookie panel;
        Object ddBean;
        String ddProperty;

        public LinkAction(LinkCookie panel, Object ddBean, String ddProperty) {
            this.panel=panel;
            this.ddBean=ddBean;
            this.ddProperty=ddProperty;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            panel.linkButtonPressed(ddBean, ddProperty);
        }
    }
}
