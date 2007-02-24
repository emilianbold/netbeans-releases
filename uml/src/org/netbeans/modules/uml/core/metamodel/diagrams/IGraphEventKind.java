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


package org.netbeans.modules.uml.core.metamodel.diagrams;


public interface IGraphEventKind
{

    // Unknown event
    public final int GEK_UNKNOWN = 0;

    // Graph Object has just been created
    public final int GEK_POST_CREATE = 1;

    // A move is about to occur
    public final int GEK_PRE_MOVE = 2;

    // A move has ocurred
    public final int GEK_POST_MOVE = 3;

    // A move has ocurred from the smartdraw tool
    public final int GEK_POST_SMARTDRAW_MOVE = 4;

    // A resize is about to occur    
    public final int GEK_PRE_RESIZE = 5;
    
    // A resize has ocurred    
    public final int GEK_POST_RESIZE = 6;
    
    // A layout is about to occur    
    public final int GEK_PRE_LAYOUT = 7;
    
    // A layout has ocurred    
    public final int GEK_POST_LAYOUT = 8;
    
    // A delete is about to occur select any other objects that must be deleted as well ie bridges   
    public final int GEK_PRE_DELETEGATHERSELECTED = 9;
    
    // A delete is about to occur    
    public final int GEK_PRE_DELETE = 10;
    
    // A delete was canceled    
    public final int GEK_DELETECANCELED = 11;
    
    // A delete has occured.
    public final int GEK_POST_DELETE = 22;
    
    // A copy is about to occur   
    public final int GEK_PRE_COPY = 12;
    
    // A copy has ocurred    
    public final int GEK_POST_COPY = 13;
    
    // A paste has ocurred    
    public final int GEK_POST_PASTE_VIEW = 14;
    
    // A paste has ocurred from within the same diagram.  This view has just been pasted    
    public final int GEK_POST_PASTE_ALL = 15;
    
    // The entire paste operation has been completed    
    public final int GEK_POST_CROSS_DIAGRAM_PASTE = 16;
    
    // A post select has ocurred    
    public final int GEK_POST_SELECT = 17;
    
    // The diagram namespace has been changed    
    public final int GEK_DIAGRAM_NAMESPACECHANGE = 18;
    
    // Fired after all required edge re-routing has been performed after a move, transfer, or resize operation    
    public final int GEK_POST_DROPOBJECTS = 19;
    
    // A sequence diagram has just been scrolled and/or zoomed    
    public final int GEK_SQD_DIAGRAM_POST_SCROLLZOOM = 20;
    
     // The presentation element owned by this drawengine was reparented due to a name collision
    public final int GEK_NAME_COLLISION_REPARENTED = 21;
    
	// A pre select has ocurred    
	 public final int GEK_PRE_SELECT = 17;
    
}
