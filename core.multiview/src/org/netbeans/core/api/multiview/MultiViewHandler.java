/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.api.multiview;

import org.netbeans.core.multiview.MultiViewHandlerDelegate;

/**
 * A handler for the  multiview's TopComponent, obtainable via MultiViewFactory, that allows
 * examination of Component's content and programatic changes in visible/activated elements.
 * @author  mkleint
 */
public final class MultiViewHandler {

    static {
        AccessorImpl.createAccesor();
    }
    
    private MultiViewHandlerDelegate del;
    
    MultiViewHandler(MultiViewHandlerDelegate delegate) {
        del = delegate;
    }
    /**
     * Returns the array of MultiViewDescriptions that the TopComponent is composed of.
     *
     */
    public MultiViewPerspective[] getPerspectives() {
        return del.getDescriptions();
    }
    
    /**
     * Returns the currently selected MultiViewDescription in the TopComponent.
     * It's element can be either visible or activated.
     */
    public MultiViewPerspective getSelectedPerspective() {
        return del.getSelectedDescription();
    }
    
    /**
     * returns the MultiViewElement for the given Description if previously created,
     * otherwise null.
     */
// SHOULD NOT BE USED, ONLY IN EMERGENCY CASE!    
//    public MultiViewPerspectiveComponent getElementForPerspective(MultiViewPerspective desc) {
//        return del.getElementForDescription(desc);
//    }
    
    /**
     * Requests focus for the MultiViewDescription passed as parameter, if necessary
     * will switch from previously selected MultiViewDescription
     */
    public void requestActive(MultiViewPerspective desc) {
        del.requestActive(desc);
    }
    
    /**
     * Changes the visible MultiViewDescription to the one passed as parameter.
     *
     */
    
    public void requestVisible(MultiViewPerspective desc) {
        del.requestVisible(desc);
    }
    
    
 
}
