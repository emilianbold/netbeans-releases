/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
