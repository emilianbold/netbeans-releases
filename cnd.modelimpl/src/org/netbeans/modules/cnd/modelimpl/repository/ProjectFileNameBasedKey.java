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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 * A common ancestor for keys 
 * that are based on (project, file) pair
 */

/*package*/
abstract class ProjectFileNameBasedKey extends ProjectNameBasedKey {
    
    protected final int fileNameIndex;
    
    protected ProjectFileNameBasedKey(String prjName, String fileName) {
	super(prjName);
	assert fileName != null;
	this.fileNameIndex = KeyUtilities.getFileIdByName(unitIndex, fileName);
    }
    
    protected ProjectFileNameBasedKey(FileImpl file) {
	this(getProjectName(file), file.getAbsolutePath());
    }
    
    protected static String getProjectName(FileImpl file) {
	assert (file != null);
	ProjectBase prj = file.getProjectImpl();
	assert (prj != null);
	return prj == null ? "<No Project Name>" : prj.getUniqueName();  // NOI18N
    }
    
    @Override
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	aStream.writeInt(fileNameIndex);
    }
    
    protected ProjectFileNameBasedKey(DataInput aStream) throws IOException {
	super(aStream);
	this.fileNameIndex = aStream.readInt();
    }
    
    @Override
    public int hashCode() {
	int key = super.hashCode();
	key = 17*key + fileNameIndex;
	return key;
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	ProjectFileNameBasedKey other = (ProjectFileNameBasedKey)obj;
	
	return this.fileNameIndex==other.fileNameIndex;
    }
    
    protected String getFileName() {
	return KeyUtilities.getFileNameById(unitIndex, this.fileNameIndex);
    }
    
    /** A special safe method, mainly for toString / tracing */
    protected String getFileNameSafe() {
	return KeyUtilities.getFileNameByIdSafe(unitIndex, this.fileNameIndex);
    }
    
    @Override
    public int getDepth() {
	assert super.getDepth() == 0;
	return 1;
    }
    
    @Override
    public String getAt(int level) {
	assert super.getDepth() == 0 && level < getDepth();
	return getFileName();
    }
}
