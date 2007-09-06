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
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Key.Behavior;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/*package*/
final class NamespaceKey extends ProjectNameBasedKey {
    
    private final String fqn;
    
    public NamespaceKey(CsmNamespace ns) {
	super(getProjectName(ns));
	this.fqn = ns.getQualifiedName();
    }
    
    private static String getProjectName(CsmNamespace ns) {
	CsmProject prj = ns.getProject();
	assert (prj != null) : "no project in namespace";
	return prj == null ? "<No Project Name>" : prj.getUniqueName();  // NOI18N
    }
    
    @Override
    public String toString() {
	return "NSKey " + fqn + " of project " + getProjectName(); // NOI18N
    }
    
    public PersistentFactory getPersistentFactory() {
	return CsmObjectFactory.instance();
    }
    
    @Override
    public int hashCode() {
	int key = super.hashCode();
	key = 17*key + fqn.hashCode();
	return key;
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!super.equals(obj)) {
	    return false;
	}
	NamespaceKey other = (NamespaceKey)obj;
	return this.fqn.equals(other.fqn);
    }
    
    @Override
    public void write(DataOutput aStream) throws IOException {
	super.write(aStream);
	assert fqn != null;
	aStream.writeUTF(fqn);
    }
    
    /*package*/ NamespaceKey(DataInput aStream) throws IOException {
	super(aStream);
	fqn = QualifiedNameCache.getString(aStream.readUTF());
	assert fqn != null;
    }
    
    @Override
    public int getDepth() {
	assert super.getDepth() == 0;
	return 1;
    }
    
    @Override
    public String getAt(int level) {
	assert super.getDepth() == 0 && level < getDepth();
	return this.fqn;
    }
    
    public int getSecondaryDepth() {
	return 1;
    }
    
    public int getSecondaryAt(int level) {
	assert level == 0;
	return KeyObjectFactory.KEY_NAMESPACE_KEY;
    }
    
    @Override
    public Key.Behavior getBehavior() {
	return Behavior.LargeAndMutable;
    }
}
