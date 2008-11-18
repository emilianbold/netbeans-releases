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
package org.netbeans.modules.bpel.model.impl.services;

import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.impl.events.BuildEvent;


/**
 * @author ads
 *
 * This service set flag for element that means element was built via builder.
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher.class)
public class MarkBuilderElement extends InnerEventDispatcherAdapter {

    public static final String CLASS_MARK_NAME = MarkBuilderElementKey.class
            .getCanonicalName();

    public MarkBuilderElement() {
        myMark = new MarkBuilderElementKey();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        // only events from Builder are accepted.
        return event.getClass().equals( BuildEvent.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
        event.getParent().setCookie( myMark , myMark );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#reset(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void reset( ChangeEvent event ) {
        event.getParent().removeCookie( myMark );
    }
    
    static MarkBuilderElementKey getMark() {
        return myMark;
    }
    
    class MarkBuilderElementKey {};
    
    private static MarkBuilderElementKey myMark;
    
}
