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
package org.netbeans.modules.bpel.mapper.multiview;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JEditorPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.soa.mappercore.Mapper;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MessagePanel extends JEditorPane {

    private static final long serialVersionUID = 1;
    public static final String MESSAGE_PANEL_ID = "messagePanelId"; // NOI18N
    private Mapper myParent;

    public MessagePanel(Mapper mapper) {
        myParent = mapper;
        setEditable(false);
        setPreferredSize(new Dimension(200, 200));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentType("text/html"); // NOI18N
        setBackground(myParent.getBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        super.paintComponent(g2);
    }
}

