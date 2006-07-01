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

package org.netbeans.core.api.multiview;

import java.util.TooManyListenersException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.Lookup;

/** View element for multi view, provides the UI components to the multiview component.
 * Gets notified by the enclosing component about the changes in the lifecycle.
 *
 * @author  Milos Kleint
 */

// IS NOT PUBLIC, SHOULD NOT BE USED, ONLY IN EMERGENCY CASE!
final class MultiViewPerspectiveComponent {

    static {
        AccessorImpl.createAccesor();
    }
    
    private MultiViewElement element;
    
    MultiViewPerspectiveComponent(MultiViewElement elem) {
        element = elem;
    }
    
    // package private, access through Accessor
    MultiViewElement getElement() {
        return element;
    }

    /** Returns Swing visual representation of this multi view element. Should be relatively fast
     * and always return the same component.
     */
    public JComponent getVisualRepresentation () {
        return element.getVisualRepresentation();
    }
    
    /**
     * Returns the visual component with the multi view element's toolbar.Should be relatively fast as it's called
     * everytime the current perspective is switched.
     */
    public JComponent getToolbarRepresentation () {
        return element.getToolbarRepresentation();
    }
    
    /** Gets the actions which will appear in the popup menu of this component.
     * <p>Subclasses are encouraged to use add the default TopComponent actions to 
     * the array of their own. These are accessible by calling MultiViewElementCallback.createDefaultActions()
     * @return array of actions for this component
     */
    public Action[] getActions() {
        return element.getActions();
    }

    /**
     * Lookup for the MultiViewElement. Will become part of the TopComponent's lookup.
     * @return the lookup to use when the MultiViewElement is active.
     */
    public Lookup getLookup() {
        return element.getLookup();
    }
    
   
}