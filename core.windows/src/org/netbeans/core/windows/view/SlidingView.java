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

import java.awt.Rectangle;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.slides.SlideBarContainer;
import org.openide.windows.TopComponent;


/**
 * Model of sliding mode element for GUI hierarchy. 
 *
 * @author  Dafe Simonek
 */
public class SlidingView extends ModeView {
    
    /** Orientation of sliding view, means side where it is located */
    private final String side;
    private Rectangle slideBounds;
    
    public SlidingView(Controller controller, WindowDnDManager windowDnDManager, 
                        TopComponent[] topComponents, TopComponent selectedTopComponent, String side) {
        super(controller);
        this.side = side;
        // mkleint - needs to be called after side is defined.
        this.container = new SlideBarContainer(this, windowDnDManager);
        setTopComponents(topComponents, selectedTopComponent);
    }
    
    public String getSide() {
        return side;
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return ((SlideBarContainer)this.container).getTabBounds(tabIndex);
    }

    public Rectangle getSlideBounds() {
        return slideBounds;
    }

    public void setSlideBounds(Rectangle slideBounds) {
        this.slideBounds = slideBounds;
    }
    
    

}

