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
package org.netbeans.modules.vmd.api.model.presenters;

import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;

/**
 * @author Karol Harezlak
 */
public abstract  class TestPresenter extends DynamicPresenter {
    private boolean presenterChangedFlag = false;
    private boolean eventFilterFlag = false;

    public TestPresenter() {
    }

    protected void designChanged(DesignEvent event) {
        System.out.println("Design Changed" + this); // NOI18N
        firePresenterChanged();
        presenterChangedFlag = true;
    }

    protected void presenterChanged(PresenterEvent event) {
        System.out.println("Presenter changed" + this); // NOI18N
    }
    
    public DesignEventFilter getEventFilter() {
        eventFilterFlag = true;
        return new DesignEventFilter().addComponentFilter(getComponent(),false); 
    }
    
    public boolean isPresenterChangedFlag(){
        return presenterChangedFlag;
    }
    
    public boolean isDesignChangedFlag(){
        return eventFilterFlag;
    }
    
    public boolean isEventFilterFlag(){
        return eventFilterFlag;
    }

}
