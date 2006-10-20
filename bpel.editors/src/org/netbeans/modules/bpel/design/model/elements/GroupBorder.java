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


package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.geom.FInsets;
import org.netbeans.modules.bpel.design.geom.FRoundRectangle;
import org.netbeans.modules.bpel.design.geom.FShape;


public class GroupBorder extends BorderElement {

    public GroupBorder() {
        super(SHAPE, INSETS);
    }


    public void paint(Graphics2D g2) {
        GUtils.setPaint(g2, COLOR);
        GUtils.setDashedStroke(g2, 1, 3);
        GUtils.draw(g2, GUtils.convert(getShape()), true);
        
        if (isPaintText()) {
            GUtils.setPaint(g2, getTextColor());
            setTextBounds(GUtils.drawString(g2, getText(), 
                    getX() + 6, getY() + 1, getWidth() - 12));
        }
    }
    

    // Shape constants
    private static final FInsets INSETS = new FInsets(16);
    private static final FShape SHAPE = new FRoundRectangle(10, 10);
    
    
    // Rendering constants
    private static final Color COLOR = new Color(0xD0D0D0);
}
