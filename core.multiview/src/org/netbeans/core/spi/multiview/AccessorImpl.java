/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import org.netbeans.core.multiview.Accessor;
import org.netbeans.core.multiview.MultiViewElementCallbackDelegate;
import org.netbeans.core.multiview.MultiViewHandlerDelegate;
import org.netbeans.core.multiview.SpiAccessor;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
 *
 * @author  mkleint
 */
class AccessorImpl extends SpiAccessor {
    
    
    /** Creates a new instance of AccessorImpl */
    private AccessorImpl() {
    }
    
    static void createAccesor() {
        if (DEFAULT == null) {
            DEFAULT= new AccessorImpl();
        }
    }
    
    public MultiViewElementCallback createCallback(MultiViewElementCallbackDelegate delegate) {
        return new MultiViewElementCallback(delegate);
    }    
    
    public CloseOperationHandler createDefaultCloseHandler() {
        return MultiViewFactory.createDefaultCloseOpHandler();
    }
    
}
