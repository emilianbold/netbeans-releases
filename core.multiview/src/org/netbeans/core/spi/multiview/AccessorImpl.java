/*
 * AccessorImpl.java
 *
 * Created on April 22, 2004, 1:50 PM
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
