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
 *
 * @author supernikita
 */
public class DndStartPositionState {
    
    // The point at which the mouse was at the start of DnD in TNV coordinates
    public final int eventPoint; 
    
    // The point at which the mouse was at the start of DnD in MV coordinates
    public final double mvPoint; 
    
    // The position of the thumbnail view at the start of DnD in MV coordinates
    public final double tnvPosition;
    
    // Minimum edge of the DnD Srart TNV region with accounting of half of the VA size
    public final double tnvMinEdge; 
    
    // Maximum edge of the DnD Srart TNV region with accounting of half of the VA size
    public final double tnvMaxEdge; 
    
    public DndStartPositionState(int eventPoint, double startMvPoint, 
            double tnvPosition, double tnvMinEdge, double tnvMaxEdge) {
        this.eventPoint = eventPoint;
        this.mvPoint = startMvPoint;
        this.tnvPosition = tnvPosition;
        this.tnvMinEdge = tnvMinEdge;
        this.tnvMaxEdge = tnvMaxEdge;
    }
    
}
