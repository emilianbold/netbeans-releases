/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
    
}

