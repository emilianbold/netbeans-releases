/*
 * Accessor.java
 *
 * Created on April 22, 2004, 1:50 PM
 */

package org.netbeans.core.multiview;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
 *
 * @author  mkleint
 */
public abstract class Accessor {
    
    protected static Accessor DEFAULT = null;
    
    static {
        // invokes static initializer of Item.class
        // that will assign value to the DEFAULT field above
        Object o = MultiViewPerspective.class;
    }    
    
    public abstract MultiViewPerspective createPerspective(MultiViewDescription desc);
    
//    public abstract MultiViewPerspectiveComponent createPersComponent(MultiViewElement elem);
    
    public abstract MultiViewHandler createHandler(MultiViewHandlerDelegate delegate);
    
//    public abstract MultiViewElement extractElement(MultiViewPerspectiveComponent comp);
    
    public abstract MultiViewDescription extractDescription(MultiViewPerspective perspective);
    
}
