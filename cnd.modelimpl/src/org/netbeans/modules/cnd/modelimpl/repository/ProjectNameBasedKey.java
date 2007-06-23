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

/**
 * A common ancestor for nearly all keys 
 */

/*package*/
abstract class ProjectNameBasedKey extends AbstractKey {
    
    protected final int unitIndex;
    
    protected ProjectNameBasedKey(String project) {
	assert project != null;
	this.unitIndex = KeyUtilities.getUnitId(project);
    }
    
    public String toString() {
	return getProjectName();
    }
    
    public int hashCode() {
	return unitIndex;
    }
    
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	ProjectNameBasedKey other = (ProjectNameBasedKey)obj;
	
	return this.unitIndex==other.unitIndex;
    }
    
    protected String getProjectName() {
	return getUnit();
    }
    
    public void write(DataOutput aStream) throws IOException {
	aStream.writeInt(this.unitIndex);
    }
    
    protected ProjectNameBasedKey(DataInput aStream) throws IOException {
	this.unitIndex = aStream.readInt();
    }
    
    public int getDepth() {
	return 0;
    }
    
    public String getAt(int level) {
	throw new UnsupportedOperationException();
    }
    
    public String getUnit() {
	// having this functionality here to be sure unit is the same thing as project
	return KeyUtilities.getUnitName(this.unitIndex);
    }
}
