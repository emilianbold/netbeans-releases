/*
 * AccessorImpl.java
 *
 * Created on April 22, 2004, 1:50 PM
 */

package org.netbeans.core.api.multiview;

import org.netbeans.core.multiview.Accessor;
import org.netbeans.core.multiview.MultiViewHandlerDelegate;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
 *
 * @author  mkleint
 */
class AccessorImpl extends Accessor {
    
    
    /** Creates a new instance of AccessorImpl */
    private AccessorImpl() {
    }
    
    static void createAccesor() {
        if (DEFAULT == null) {
            DEFAULT= new AccessorImpl();
        }
    }
    
//    public MultiViewPerspectiveComponent createPersComponent(MultiViewElement elem) {
//        return new MultiViewPerspectiveComponent(elem);
//    }
    
    public MultiViewPerspective createPerspective(MultiViewDescription desc) {
        return new MultiViewPerspective(desc);
    }
    
    public MultiViewHandler createHandler(MultiViewHandlerDelegate delegate) {
        return new MultiViewHandler(delegate);
    }
    
    public MultiViewDescription extractDescription(MultiViewPerspective perspective) {
        return perspective.getDescription();
    }
    
//    public MultiViewElement extractElement(MultiViewPerspectiveComponent comp) {
//        return comp.getElement();
//    }
    
}
