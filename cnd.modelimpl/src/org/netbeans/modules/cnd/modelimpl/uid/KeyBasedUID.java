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
import org.netbeans.modules.cnd.modelimpl.repository.KeyHolder;
import org.netbeans.modules.cnd.modelimpl.repository.KeyObjectFactory;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * help class for CsmUID based on repository Key
 * @author Vladimir Voskresensky
 */
public abstract class KeyBasedUID<T extends CsmIdentifiable> implements CsmUID<T>, KeyHolder, SelfPersistent, Comparable {
    private final Key key;
    
    protected KeyBasedUID(Key key) {
        assert key != null;
        this.key = key;
    }
    
    public T getObject() {
        return (T) RepositoryUtils.get(this);
    }

    public Key getKey() {
        return key;
    }
    
    public String toString() {
        String retValue;
        
        retValue = key.toString();
        return "KeyBasedUID on " + retValue; // NOI18N
    }

    public int hashCode() {
        int retValue;
        
        retValue = key.hashCode();
        return retValue;
    }

    public boolean equals(Object obj) {
        boolean retValue;
        
        retValue = key.equals(obj);
        return retValue;
    }

    public void write(DataOutput aStream) throws IOException {
        KeyObjectFactory.getDefaultFactory().writeKey(key, aStream);
    }

    /* package */ KeyBasedUID(DataInput aStream) throws IOException {
        key = KeyObjectFactory.getDefaultFactory().readKey(aStream);
    }

    public int compareTo(Object o) {
        assert o != null;
        assert o instanceof KeyBasedUID;
        Comparable o1 = (Comparable)this.key;
        Comparable o2 = (Comparable)((KeyBasedUID)o).key;
        return o1.compareTo(o2);
    }
}    