/*
 * Accessor.java
 *
 * Created on April 22, 2004, 1:50 PM
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
        Object o = MultiViewElementCallback.class;
    }    
    
    public abstract MultiViewElementCallback createCallback(MultiViewElementCallbackDelegate delegate);
    
    public abstract CloseOperationHandler createDefaultCloseHandler();
    
}
