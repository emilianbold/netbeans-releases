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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.event.MouseEvent;

/*
 * Controller for slide related user actions.
 *
 * @author Dafe Simonek
 */
public interface SlideBarController extends SlideController {
    
    public void userClickedSlidingButton(Component clickedButton);
    
    public void userTriggeredPopup(MouseEvent mouseEvent, Component clickedButton);
    
    public boolean userTriggeredAutoSlideIn(Component sourceButton);

    public void userTriggeredAutoSlideOut();

    
}
