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


import java.awt.Graphics2D;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geom.FInsets;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.geom.FShape;


public class BorderElement extends VisualElement {


    private FInsets insets;


    public BorderElement(FShape shape, FInsets insets) {
        super(shape);
        this.insets = insets;
    }


    public FInsets getInsets() {
        return insets;
    }


    public void paint(Graphics2D g2) {
        GUtils.draw(g2, GUtils.convert(getShape()), true);
    }
    
    public void setClientRectangle(float x, float y, float w, float h){
         
        setLocation( x - insets.left,
                     y - insets.top);
        setSize(w + insets.left + insets.right, 
                h + insets.top + insets.bottom);
        
    }
    public void setClientRectangle(FRectangle bounds){
        setClientRectangle(bounds.x, bounds.y, bounds.width, bounds.height);  
    }
}

