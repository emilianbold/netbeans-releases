/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * help class for CsmUID based on CsmObject
 * @author Vladimir Voskresensky
 */
public abstract class ObjectBasedUID<T extends CsmIdentifiable> implements CsmUID<T>, SelfPersistent {
    private final T ref;
    
    protected ObjectBasedUID(T ref) {
        this.ref = ref;
    }
    
    public T getObject() {
        return this.ref;
    }
    
    public String toString() {
        String retValue = "UID for " + ref.toString(); // NOI18N
        return retValue;
    }
    
    public int hashCode() {
        return ref.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ObjectBasedUID other = (ObjectBasedUID)obj;
        return this.ref.equals(other.ref);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl for Persistent 
    
    public void write(DataOutput output) throws IOException {
        assert ref == null || ref instanceof Persistent;
        CsmObjectFactory.instance().write(output, (Persistent)ref);
    }
    
    public ObjectBasedUID(DataInput input) throws IOException {
        ref = (T)CsmObjectFactory.instance().read(input);
    }
}
