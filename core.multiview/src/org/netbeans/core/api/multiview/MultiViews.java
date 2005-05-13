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

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.netbeans.core.multiview.MultiViewTopComponent;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/** Factory class for handling multi views.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public final class MultiViews {
    
    /** Factory class, no instances. */
    private MultiViews() {
    }

    /**
     * For advanced manupulation with Multiview component, the handler can be requested
     * @return handle that one can use for manipulation with multiview component.
     */
    public static MultiViewHandler findMultiViewHandler(TopComponent tc) {
        if ( tc != null) {
            if (tc instanceof MultiViewTopComponent) {
                return new MultiViewHandler(((MultiViewTopComponent)tc).getMultiViewHandlerDelegate());
            }
            if (tc instanceof MultiViewCloneableTopComponent) {
                return new MultiViewHandler(((MultiViewCloneableTopComponent)tc).getMultiViewHandlerDelegate());
            }
        }
        return null;
    }
    
}
