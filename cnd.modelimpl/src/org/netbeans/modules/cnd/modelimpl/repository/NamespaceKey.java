/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Key.Behavior;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/*package*/
class NamespaceKey extends ProjectNameBasedKey {
    
    private final CharSequence fqn;
    private final int hashCode; // cashed hash code
    
    public NamespaceKey(CsmNamespace ns) {
	super(getProjectName(ns));
	this.fqn = ns.getQualifiedName();
        hashCode = _hashCode();
    }
    
    private static String getProjectName(CsmNamespace ns) {
	ProjectBase prj = (ProjectBase) ns.getProject();
	assert (prj != null) : "no project in namespace";
	return prj == null ? "<No Project Name>" : prj.getUniqueName().toString();  // NOI18N
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
        return hashCode;
    }

    private int _hashCode() {
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
	aStream.writeUTF(fqn.toString());
    }
    
    /*package*/ NamespaceKey(DataInput aStream) throws IOException {
	super(aStream);
	fqn = QualifiedNameCache.getManager().getString(aStream.readUTF());
	assert fqn != null;
        hashCode = _hashCode();
    }
    
    @Override
    public int getDepth() {
	assert super.getDepth() == 0;
	return 1;
    }
    
    @Override
    public CharSequence getAt(int level) {
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
