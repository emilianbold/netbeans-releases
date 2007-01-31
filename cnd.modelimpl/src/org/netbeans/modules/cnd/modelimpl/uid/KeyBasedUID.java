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

import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;

/**
 * help class for CsmUID based on repository Key
 * @author Vladimir Voskresensky
 */
public class KeyBasedUID implements CsmUID {
    private final Key key;
    
    public KeyBasedUID(Key key) {
        assert key != null;
        this.key = key;
    }
    
    public CsmIdentifiable getObject() {
        return RepositoryUtils.get(this);
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
    
    
}    