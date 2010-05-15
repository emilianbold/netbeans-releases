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

import java.awt.Color;
import java.awt.Paint;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FInsets;
import org.netbeans.modules.bpel.design.geometry.FShape;

/**
 *
 * @author anjeleevich
 */
public abstract class BorderElement extends VisualElement {
    
    
    private FInsets insets;
    
    
    public BorderElement(FShape shape, FInsets insets) {
        super(shape);
        this.insets = insets;
    }
    
    
    public FInsets getInsets() {
        return insets;
    }
    
    
    public void setClientRectangle(double x, double y, double w, double h) {
        setBounds(x - insets.left, y - insets.top,
                w + insets.left + insets.right, 
                h + insets.top + insets.bottom);
    }
    
    
    public void setClientRectangle(FBounds bounds){
        setClientRectangle(bounds.x, bounds.y, bounds.width, bounds.height);  
    }
    
    
    
    public void setSize(double width, double height) {
        shape = shape.resize(width, height);
    }
    
    
    public void setBounds(double x, double y, double w, double h) {
        shape = shape.reshape(x, y, w, h);
    }
}
