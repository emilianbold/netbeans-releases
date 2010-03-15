/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.modelimpl.csm.ForwardClass;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;


/**
 * File and offset -based key for declarations
 */

/*package*/
final class OffsetableDeclarationKey extends OffsetableKey {
    
    public OffsetableDeclarationKey(OffsetableDeclarationBase<?> obj) {
	super((FileImpl) obj.getContainingFile(), obj.getStartOffset(), getSmartEndOffset(obj), Utils.getCsmDeclarationKindkey(obj.getKind()), obj.getName());
	// we use name, because all other (FQN and UniqueName) could change
	// and name is fixed value
    }
    
    public OffsetableDeclarationKey(OffsetableDeclarationBase<?> obj, int index) {
	super((FileImpl) obj.getContainingFile(), obj.getStartOffset(), getSmartEndOffset(obj), Utils.getCsmDeclarationKindkey(obj.getKind()), Integer.toString(index));
	// we use index for unnamed objects
    }
    
    private static int getSmartEndOffset(OffsetableDeclarationBase<?> obj) {
        // #132865 ClassCastException in Go To Type -
        // ensure that members and non-members has different keys
        // also make sure that function and fake function has different keys
        int result = obj.getEndOffset();
        if (obj instanceof ForwardClass) {
            result |= 0x80000000;
        } else if( obj instanceof CsmMember) {
            // do nothing
        } else if (FunctionImplEx.isFakeFunction(obj)) {
            result |= 0x80000000;
        } else {
            result |= 0x40000000;
        }
        return result;
    }

    @Override
    int getEndOffset() {
        return super.getEndOffset() & 0x3FFFFFFF;
    }
    
    /*package*/ OffsetableDeclarationKey(DataInput aStream) throws IOException {
	super(aStream);
    }
    
    
    @Override
    public PersistentFactory getPersistentFactory() {
	return CsmObjectFactory.instance();
    }
    
    @Override
    public String toString() {
	String retValue;
	
	retValue = "OffsDeclKey: " + super.toString(); // NOI18N
	return retValue;
    }
    
    @Override
    public int getSecondaryDepth() {
	return super.getSecondaryDepth() + 1;
    }
    
    @Override
    public int getSecondaryAt(int level) {
	if (level == 0) {
	    return KeyObjectFactory.KEY_DECLARATION_KEY;
	}  else {
	    return super.getSecondaryAt(level - 1);
	}
    }
}
