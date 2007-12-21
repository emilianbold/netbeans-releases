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


package org.netbeans.modules.bpel.design.model.elements;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.netbeans.modules.bpel.design.geometry.FEllipse;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.model.elements.icons.PlaceholderIcon2D;


public class PlaceHolderElement extends ContentElement {

    public PlaceHolderElement() {
        super(SHAPE, PlaceholderIcon2D.INSTANCE);
    }


    public void paint(Graphics2D g2) {
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);

        FPoint center = getShape().getNormalizedCenter(g2);
        g2.translate(center.x, center.y);
        getIcon().paint(g2);
        g2.translate(-center.x, -center.y);
    }
    
    
    public void paintThumbnail(Graphics2D g2) {}
    
    
    public static final FShape SHAPE = new FEllipse(20);
}
