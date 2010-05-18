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

package org.netbeans.modules.soa.ui.tnv.impl;

/**
 * Keeps the current position and size of the Tnumbnail view and the visible area.
 *
 * @author supernikita
 */
public class ThumbnailPositionState {
    //
    public double mvSize;
    //
    public double tnvPosition;
    public double tnvSize;
    //
    public double vaPosition;
    public double vaHalfSize;
    
    /**
     * Checks if the visible area with the center in the specified point 
     * is inside of thumbnail view region.
     * Returns 0 If the visible area inside of the TNV region. 
     * Returns -1 If the visible area overlaps minimum edge of TNV region.
     * Returns -1 If the visible area overlaps maximum edge of TNV region.
     */
    public int isInsideOfTnv(double point) {
        if (tnvPosition + vaHalfSize > point) {
            return -1;
        }
        if (tnvPosition + tnvSize - vaHalfSize < point) {
            return 1;
        }
        return 0;
    }
    
    public String toString() {
        return "MV Size: " + mvSize + "; " +
                "TNV: [" + tnvPosition + ", " + tnvSize + "]; " + 
                "VA: [" + vaPosition + ", " + vaHalfSize * 2d + "]; ";
                
    }
}
