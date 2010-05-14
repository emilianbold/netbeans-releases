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


package org.netbeans.modules.bpel.design.geometry;

/**
 *
 * @author anjeleevich
 */
public class FInsets {
    
    public final float top;
    public final float left;
    public final float bottom;
    public final float right;
    
    public FInsets(double top, double left, double bottom, double right) {
        this.top = (top < 0.0) ? 0.0f : (float) top;
        this.left = (left < 0.0) ? 0.0f : (float) left;
        this.bottom = (bottom < 0.0) ? 0.0f : (float) bottom;
        this.right = (top < 0.0) ? 0.0f : (float) right;
    }
    
    
    public float getTop() { return top; }
    public float getLeft() { return left; }
    public float getBottom() { return bottom; }
    public float getRight() { return right; }
}
