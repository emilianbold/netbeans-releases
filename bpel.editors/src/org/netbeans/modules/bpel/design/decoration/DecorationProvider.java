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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.design.decoration;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.model.api.BpelEntity;

/**
 *
 * base class to implement DecorationProvider.
 * overriding classes should override getDecoration() method
 * and call fireDecorationChangedEvent to inform DecorationManager about changes
 */
public abstract class DecorationProvider {
    
    private DesignView view;
    
    protected DecorationProvider() {}

    protected DecorationProvider(DesignView view){
        this.view = view;
        view.getDecorationManager().attachProvider(this);
    }
    
    /** 
     * Provider should implement this method.
     * Implementation should return decoration for given diagram element
     * @entity is an element to calculate decoration for
     * @returns Decoration for this element
     **/
    
    public abstract Decoration getDecoration(BpelEntity entity);
    
    /**
     * Called when view is about to close
     * Provider can override this method to release it's resources
     **/
    public void release() {
    }
    
    /**
     * Provider should call this method to notify DecorationManager that 
     * some decorations was changed
     *
     * @entity may be null to inform DecorationManager that it
     * needs to update state for ALL elements in a tree.
     **/
    protected final void fireDecorationChanged(){
         view.getDecorationManager().decorationChanged();
    }
    
    /**
     * @return the view, which current provider was attached to
     **/
    protected DesignView getDesignView(){
        return this.view;
    }
}
