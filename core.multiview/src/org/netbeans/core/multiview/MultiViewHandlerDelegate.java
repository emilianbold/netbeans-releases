/*
 * MultiViewHandlerDelegate.java
 *
 * Created on April 22, 2004, 2:00 PM
 */

package org.netbeans.core.multiview;

import org.netbeans.core.api.multiview.MultiViewPerspective;

/**
 * delegate to implement by MultiViewTopComponent
 *
 * @author  mkleint
 */
public interface MultiViewHandlerDelegate {
    
    
        
        MultiViewPerspective[] getDescriptions();
        
        MultiViewPerspective getSelectedDescription();
        
//        MultiViewPerspectiveComponent getElementForDescription(MultiViewPerspective desc);
        
        void requestActive(MultiViewPerspective desc);
        
        void requestVisible(MultiViewPerspective desc);
}
