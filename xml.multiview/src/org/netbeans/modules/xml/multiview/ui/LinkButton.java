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

/** LinkButton.java
 *
 * Created on November 19, 2004, 8:06 AM
 * @author mkuchtiak
 */
public class LinkButton extends javax.swing.JButton {
    
    /** Creates a new instance of LinkButton */
    public LinkButton(LinkCookie panel, Object ddBean, String ddProperty) {
        super();
        setForeground(SectionVisualTheme.hyperlinkColor);
        setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        setMargin(new java.awt.Insets(2, 2, 2, 2));
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setAction(new LinkAction(panel, ddBean, ddProperty));
    }
    
    public void setText(String text) {
        super.setText("<html><b><u>"+text+"</u></b></html>");
    }
    
    static class LinkAction extends javax.swing.AbstractAction {
        LinkCookie panel;
        Object ddBean;
        String ddProperty;
        
        LinkAction(LinkCookie panel, Object ddBean, String ddProperty) {
            this.panel=panel;
            this.ddBean=ddBean;
            this.ddProperty=ddProperty;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            panel.linkButtonPressed(ddBean, ddProperty);
        }
    }

}
