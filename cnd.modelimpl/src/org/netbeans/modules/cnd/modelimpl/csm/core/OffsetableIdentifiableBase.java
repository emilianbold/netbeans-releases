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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * class to present object that has unique ID and is offsetable
 * unique ID is used to long-time stored references on Csm Objects
 * 
 * @see CsmUID
 * @author Vladimir Voskresensky
 */
public abstract class OffsetableIdentifiableBase<T> extends OffsetableBase implements CsmIdentifiable<T>, Persistent, SelfPersistent {
    
    protected OffsetableIdentifiableBase(AST ast, CsmFile file) {
        super(ast, file);
    }
    
    protected OffsetableIdentifiableBase(CsmFile file) {
        super(file);
    }
    
    protected OffsetableIdentifiableBase(CsmFile containingFile, CsmOffsetable pos) {
        super(containingFile, pos);
    }
    
    protected OffsetableIdentifiableBase(CsmFile file, CsmOffsetable.Position start, CsmOffsetable.Position end) {
        super(file, start, end);
    }    
    
    protected abstract CsmUID createUID();
    
    public CsmUID<T> getUID() {
        if (uid == null) {
            uid = createUID();
        }
        return uid;
    }
    
    protected void cleanUID() {
        // this.uid = null;
    }
    
    private CsmUID uid = null;       

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }
    
    protected OffsetableIdentifiableBase(DataInput input) throws IOException {
        super(input);
    }    
}
