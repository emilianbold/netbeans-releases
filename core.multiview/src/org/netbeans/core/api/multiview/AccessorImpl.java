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

import org.netbeans.core.multiview.Accessor;
import org.netbeans.core.multiview.MultiViewHandlerDelegate;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

/**
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
