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
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;


/**
 * File and offset -based key
 */

/*package*/
abstract class OffsetableKey extends ProjectFileNameBasedKey implements Comparable {
    
    private final int startOffset;
    private final int endOffset;
    
    private final String kind;
    private final String name;
    
    protected OffsetableKey(CsmOffsetable obj, String kind, String name) {
	super((FileImpl) obj.getContainingFile());
	this.startOffset = obj.getStartOffset();
	this.endOffset = obj.getEndOffset();
	this.kind = kind;
	this.name = name;
    }
    
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	aStream.writeInt(this.startOffset);
	aStream.writeInt(this.endOffset);
	assert this.kind != null;
	aStream.writeUTF(this.kind);
	assert this.name != null;
	aStream.writeUTF(this.name);
    }
    
    protected OffsetableKey(DataInput aStream) throws IOException {
	super(aStream);
	this.startOffset = aStream.readInt();
	this.endOffset = aStream.readInt();
	this.kind = TextCache.getString(aStream.readUTF());
	assert this.kind != null;
	this.name = TextCache.getString(aStream.readUTF());
	assert this.name != null;
    }
    
    public String toString() {
	return name + "[" + kind + " " + startOffset + "-" + endOffset + "] {" + getFileNameSafe() + "; " + getProjectName() + "}"; // NOI18N
    }
    
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	OffsetableKey other = (OffsetableKey)obj;
	return  this.startOffset == other.startOffset &&
		this.endOffset == other.endOffset &&
		this.kind.equals(other.kind) &&
		this.name.equals(other.name);
    }
    
    public int hashCode() {
	int retValue;
	
	retValue = 17*super.hashCode() + name.hashCode();
	retValue = 17*retValue + kind.hashCode();
	retValue = 17*super.hashCode() + startOffset;
	retValue = 17*retValue + endOffset;
	return retValue;
    }
    
    public int compareTo(Object o) {
	if (this == o) {
	    return 0;
	}
	OffsetableKey other = (OffsetableKey)o;
	assert (this.kind.equals(other.kind));
	//FUXUP assertion: unit and file tables should be deserialized before files deserialization.
	//instead compare indexes.
	//assert (this.getFileName().equals(other.getFileName()));
	//assert (this.getProjectName().equals(other.getProjectName()));
	assert (this.unitIndex == other.unitIndex);
	assert (this.fileNameIndex == other.fileNameIndex);
	int ofs1 = this.startOffset;
	int ofs2 = other.startOffset;
	if (ofs1 == ofs2) {
	    return 0;
	} else {
	    return (ofs1 - ofs2);
	}
    }
    
    public int getDepth() {
	return super.getDepth() + 2;
    }
    
    public String getAt(int level) {
	int superDepth = super.getDepth();
	if (level < superDepth) {
	    return super.getAt(level);
	} else {
	    switch(level - superDepth) {
		case 0:
		    return this.kind;
		case 1:
		    return this.name;
		default:
		    
		    throw new IllegalArgumentException("not supported level" + level); // NOI18N
	    }
	}
    }
    
    public int getSecondaryDepth() {
	return 2;
    }
    
    public int getSecondaryAt(int level) {
	switch(level) {
	    case 0:
		
		return this.startOffset;
	    case 1:
		
		return this.endOffset;
	    default:
		
		throw new IllegalArgumentException("not supported level" + level); // NOI18N
	}
    }
}
