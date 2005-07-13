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

package org.netbeans.core.multiview;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.CloseOperationHandler;

/**
 *
 * @author  mkleint
 */
public abstract class SpiAccessor {
    
    protected static SpiAccessor DEFAULT = null;
    
    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Class c = MultiViewElementCallback.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    
    
    public abstract MultiViewElementCallback createCallback(MultiViewElementCallbackDelegate delegate);
    
    public abstract CloseOperationHandler createDefaultCloseHandler();
    
}
