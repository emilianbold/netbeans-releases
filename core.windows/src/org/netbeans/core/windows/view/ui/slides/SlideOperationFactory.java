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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Rectangle;

/**
 * Factory for possible types of sliding operations with asociated effect.
 *
 * Operations are designed to be sent to winsys to be runned.
 *
 * @author Dafe Simonek
 */
public final class SlideOperationFactory {

    private static final SlidingFx slideInFx = new ScaleFx(0.1f, 0.9f, true);
    private static final SlidingFx slideOutFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoEdgeFx = new ScaleFx(0.9f, 0.1f, false);
    private static final SlidingFx slideIntoDesktopFx = new ScaleFx(1.0f, 1.0f, true);

    /** true when slide effects should be applied, false otherwise */
    static final boolean EFFECTS_ENABLED = Boolean.getBoolean("nb.winsys.sliding.effects"); //NOI18N
    
    private SlideOperationFactory() {
        // no need to instantiate
    }
    
    public static SlideOperation createSlideIn(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_IN, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideInFx : null,
                requestActivation);
                
        return result;
    }

    public static SlideOperation createSlideOut(Component component, 
        int orientation, boolean useEffect, boolean requestActivation) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_OUT, 
                component, orientation, useEffect && EFFECTS_ENABLED ? slideOutFx : null,
                requestActivation);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoEdge(Component component, 
        String side, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_EDGE,
                component, side, useEffect && EFFECTS_ENABLED ? slideIntoEdgeFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideIntoDesktop(Component component, 
        int orientation, boolean useEffect) {
            
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_INTO_DESKTOP,
                component, orientation, useEffect && EFFECTS_ENABLED ? slideIntoDesktopFx : null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, int orientation) {
        SlideOperation result = new SlideOperationImpl(SlideOperation.SLIDE_RESIZE,
                component, orientation, null, false);
                
        return result;
    }
    
    public static SlideOperation createSlideResize(Component component, String side) {
        return createSlideResize( component, SlideOperationImpl.side2Orientation( side ) );
    }
}