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


import org.netbeans.core.windows.ModeImpl;
import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Class which is used as an access point to data wchih View is responsible
 * to process.
 *
 * @author  Peter Zavadsky
 */
interface ModeAccessor extends ElementAccessor {

    public String getName();
    
    public int getState();
    
    public int getKind();
    
    public Rectangle getBounds();
    
    public int getFrameState();
    
    public TopComponent getSelectedTopComponent();
    
    public TopComponent[] getOpenedTopComponents();
    
    public double getResizeWeight();
    
    public ModeImpl getMode();
    
}

