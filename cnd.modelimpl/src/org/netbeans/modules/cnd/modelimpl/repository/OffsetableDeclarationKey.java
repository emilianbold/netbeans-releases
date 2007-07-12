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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;


/**
 * File and offset -based key for declarations
 */

/*package*/
final class OffsetableDeclarationKey extends OffsetableKey {
    
    public OffsetableDeclarationKey(OffsetableDeclarationBase obj) {
	super(obj, obj.getKind().toString(), obj.getName());
	// we use name, because all other (FQN and UniqueName) could change
	// and name is fixed value
    }
    
    public OffsetableDeclarationKey(OffsetableDeclarationBase obj, int index) {
	super(obj, obj.getKind().toString(), Integer.toString(index));
	// we use index for unnamed objects
    }
    
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
    }
    
    /*package*/ OffsetableDeclarationKey(DataInput aStream) throws IOException {
	super(aStream);
    }
    
    
    public PersistentFactory getPersistentFactory() {
	return CsmObjectFactory.instance();
    }
    
    public String toString() {
	String retValue;
	
	retValue = "OffsDeclKey: " + super.toString(); // NOI18N
	return retValue;
    }
    
    public int getSecondaryDepth() {
	return super.getSecondaryDepth() + 1;
    }
    
    public int getSecondaryAt(int level) {
	if (level == 0) {
	    return KeyObjectFactory.KEY_DECLARATION_KEY;
	}  else {
	    return super.getSecondaryAt(level - 1);
	}
    }
}
