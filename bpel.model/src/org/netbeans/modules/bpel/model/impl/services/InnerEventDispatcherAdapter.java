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
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher;


/**
 * Adapter for InnerEventDispatcher interface.
 * @author ads
 *
 */
public class InnerEventDispatcherAdapter implements InnerEventDispatcher {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#isApplicable(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public boolean isApplicable( ChangeEvent event ) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#preDispatch(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void preDispatch( ChangeEvent event ) throws VetoException {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#postDispatch(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void postDispatch( ChangeEvent event ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.xam.spi.InnerEventDispatcher#reset(org.netbeans.modules.bpel.model.api.events.ChangeEvent)
     */
    public void reset( ChangeEvent event ) {
    }

}
