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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * CsmEnumerator implementation
 * @author Vladimir Kvashin
 */
public final class EnumeratorImpl extends OffsetableDeclarationBase<CsmEnumerator> implements CsmEnumerator {
    private final CharSequence name;
    
    // only one of enumerationRef/enumerationUID must be used (USE_UID_TO_CONTAINER)    
    private /*final*/ CsmEnum enumerationRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmEnum> enumerationUID;

    public EnumeratorImpl(AST ast, EnumImpl enumeration) {
        super(ast, enumeration.getContainingFile());
        this.name = NameCache.getManager().getString(ast.getText());
        // set parent enum, do it in constructor to have final fields
        this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
        this.enumerationRef = null;
    }
    
    public CharSequence getName() {
        return name;
    }

    public CsmExpression getExplicitValue() {
        return null;
    }

    public CsmEnum getEnumeration() {
        return _getEnumeration();
    }
    
    public CsmScope getScope() {
        return getEnumeration();
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUMERATOR;
    }

    public CharSequence getQualifiedName() {
	return CharSequences.create(_getEnumeration().getQualifiedName() + "::" + getQualifiedNamePostfix()); // NOI18N
    }

    private synchronized CsmEnum _getEnumeration() {
        CsmEnum enumeration = this.enumerationRef;
        if (enumeration == null) {
            enumeration = UIDCsmConverter.UIDtoDeclaration(this.enumerationUID);
            assert (enumeration != null || this.enumerationUID == null) : "null object for UID " + this.enumerationUID;
        }
        return enumeration;
    }    

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
    } 
    
    private synchronized void onDispose() {
        if (enumerationRef == null) {
            // restore container from it's UID
            this.enumerationRef = UIDCsmConverter.UIDtoDeclaration(this.enumerationUID);
            assert this.enumerationRef != null || this.enumerationUID == null : "no object for UID " + this.enumerationUID;
        }
    }    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
    
        // not null UID
        assert this.enumerationUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.enumerationUID, output);
    }
    
    public EnumeratorImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.enumerationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.enumerationUID != null;
        this.enumerationRef = null;
    }
}
