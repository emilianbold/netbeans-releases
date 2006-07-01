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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view;


import java.awt.*;


/**
 * Class which represents one element in ViewHierarchy.
 * It could be split, mode, or editor type element.
 *
 * @author  Peter Zavadsky
 */
public abstract class ViewElement {

    private final Controller controller;

    private final double resizeWeight;


    public ViewElement(Controller controller, double resizeWeight) {
        this.controller = controller;
        this.resizeWeight = resizeWeight;
    }


    public final Controller getController() {
        return controller;
    }

    public abstract Component getComponent();
    
    public final double getResizeWeight() {
        return resizeWeight;
    }
    
    /**
     * lets the visual components adjust to the current state.
     * @returns true if a change was performed.
     */
    public abstract boolean updateAWTHierarchy(Dimension availableSpace);
    
    
}

