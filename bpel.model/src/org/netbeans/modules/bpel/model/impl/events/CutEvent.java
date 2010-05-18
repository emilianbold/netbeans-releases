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

/**
 *
 */
package org.netbeans.modules.bpel.model.impl.events;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.OutOfModelEvent;

/**
 * @author ads
 */
public class CutEvent<T extends BpelEntity> extends TreeCreatedEvent<T>
        implements OutOfModelEvent
{

    private static final long serialVersionUID = 6380898393602526639L;

    public CutEvent( T parent ) {
        super(parent);
    }

    public void setOutOfModelEntity( BpelEntity entity ) {
        myEntity = entity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.events.OutOfModelEvent#getOutOfModelEntity()
     */
    public BpelEntity getOutOfModelEntity() {
        return myEntity;
    }

    private BpelEntity myEntity;

}
