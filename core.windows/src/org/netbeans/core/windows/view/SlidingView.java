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

import java.awt.Rectangle;
import java.util.Map;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.slides.SlideBarContainer;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Model of sliding mode element for GUI hierarchy.
 *
 * @author  Dafe Simonek
 */
public class SlidingView extends ModeView {

    /** Orientation of sliding view, means side where it is located */
    private final String side;
    private Rectangle slideBounds;
    private Map<TopComponent,Integer> slideInSizes;

    public SlidingView(Controller controller, WindowDnDManager windowDnDManager, 
                        TopComponent[] topComponents, 
                        TopComponent selectedTopComponent, 
                        String side, Map<TopComponent,Integer> slideInSizes) {
        super(controller);
        this.side = side;
        this.slideInSizes = slideInSizes;
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
        Rectangle res = slideBounds;
//        res.height = 0;
//        res.width = 0;
        
        TopComponent tc = getSelectedTopComponent();
        //check if the slided-in TopComponent has a custom size defined
        if( null != tc ) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            String tcID = wm.findTopComponentID( tc );
            if( wm.isTopComponentMaximizedWhenSlidedIn( tcID ) ) {
                //force maximum size when the slided-in window is maximized,
                //the DesktopImpl will adjust the size to fit the main window
                if( Constants.BOTTOM.equals( side ) ) {
                    res.height = Integer.MAX_VALUE;
                } else {
                    res.width = Integer.MAX_VALUE;
                }
            } else {
                Integer prevSlideSize = slideInSizes.get( tc );
                if( null != prevSlideSize ) {
                    if( null == res )
                        res = tc.getBounds();
                    if( Constants.BOTTOM.equals( side ) ) {
                        res.height = prevSlideSize.intValue();
                    } else {
                        res.width = prevSlideSize.intValue();
                    }
                }
            }
        }
        return res;
    }

    public void setSlideBounds(Rectangle slideBounds) {
        this.slideBounds = slideBounds;
    }
    
    public void setSlideInSizes(Map<TopComponent,Integer> slideInSizes) {
        this.slideInSizes = slideInSizes;
    }
}

