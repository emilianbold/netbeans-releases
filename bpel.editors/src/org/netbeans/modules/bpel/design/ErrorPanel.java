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

package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.NbBundle;


public class ErrorPanel extends JEditorPane {

    private DesignView designView;
    private boolean installed = false;
    private static final long serialVersionUID = 1;

    public ErrorPanel(DesignView designView) {
        this.designView = designView;
        
        setEditorKitForContentType("text/html",new HTMLEditorKit()); // NOI18N
        setEditable(false);
        setPreferredSize(new Dimension(200, 200));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentType("text/html"); // NOI18N
        setBackground(designView.getBackground());
    }


    public boolean isInstalled() {
        return installed;
    }
    
    
    public void install() {
        if (!installed) {
            Component c = getDesignView().getParent();
            
            if (c != null) {
                JScrollPane scrollPane = (JScrollPane) c.getParent();
                scrollPane.setViewportView(this);
            }
        }
            
//        String errorMessage = getDesignView().getBPELModel().getModelError();
        
        StringBuffer s = new StringBuffer();
        s.append("<html><body><font face=sans-serif size=3 color=#99000>"); // NOI18N
        s.append(NbBundle.getMessage(getClass(), "LBL_ErrorPanel_Message")); // NOI18N
        s.append("</font><br><br></body></html>"); // NOI18N
        setText(s.toString());

        installed = true;
    }
    
    
    public void uninstall() {
        if (!installed) return;
        
        JScrollPane scrollPane = (JScrollPane) getParent().getParent();
        scrollPane.setViewportView(getDesignView());
        
        installed = false;
    }
    
    
    public DesignView getDesignView() {
        return designView;
    }

    
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2);
    }
}
