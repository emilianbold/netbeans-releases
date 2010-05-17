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
package org.netbeans.modules.bpel.model.impl;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 * @author ads
 */
public class UniqueIdImpl implements UniqueId {

    UniqueIdImpl( BpelEntityImpl entity, long id ) {
        myEntity = entity;
        myId = id;
    }

    @Override
    public boolean equals( Object object )
    {
        if (object instanceof UniqueIdImpl) {
            UniqueIdImpl impl = (UniqueIdImpl) object;
            if (impl.myEntity == null) {
                return (myEntity == null) && (impl.myId == myId);
            }
            return impl.myEntity.equals(myEntity) && impl.myId == myId;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return (int) myId;
    }

    @Override
    public String toString()
    {
        return "" + myId;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.UniqueId#getModel()
     */
    public BpelModel getModel() {
        if ( myEntity == null ) {
            return null;
        }
        else {
            return myEntity.getBpelModel();
        }
    }

    void setEntity( BpelEntityImpl entity ) {
        myEntity = entity;
    }

    BpelEntityImpl getEntity() {
        return myEntity;
    }

    private BpelEntityImpl myEntity;

    private long myId;

}
