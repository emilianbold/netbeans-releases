/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * CsmEnumerator implementation
 * @author Vladimir Kvashin
 */
public final class EnumeratorImpl extends OffsetableDeclarationBase<CsmEnumerator> implements CsmEnumerator {
    private final CharSequence name;
    
    // only one of enumerationRef/enumerationUID must be used (USE_UID_TO_CONTAINER)    
    private /*final*/ CsmEnum enumerationRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmEnum> enumerationUID;

    private EnumeratorImpl(AST ast, NameHolder name, EnumImpl enumeration) {
        super(enumeration.getContainingFile(), getStartOffset(ast), getEndOffset(ast));
        this.name = NameCache.getManager().getString(name.getName());
        // set parent enum, do it in constructor to have final fields
        this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
        this.enumerationRef = null;
    }

    public static EnumeratorImpl create(AST ast, EnumImpl enumeration, boolean global) {
        NameHolder holder = NameHolder.createSimpleName(ast);
        EnumeratorImpl ei = new EnumeratorImpl(ast, holder, enumeration);
        postObjectCreateRegistration(global, ei);
        holder.addReference(enumeration.getContainingFile(), ei);
        return ei;
    }

    private EnumeratorImpl(EnumImpl enumeration, String name, int startOffset, int endOffset) {
        super(enumeration.getContainingFile(), startOffset, endOffset);
        this.name = NameCache.getManager().getString(name);
        // set parent enum, do it in constructor to have final fields
        this.enumerationUID = UIDCsmConverter.declarationToUID((CsmEnum)enumeration);
        this.enumerationRef = null;
    }
    
    public static EnumeratorImpl create(EnumImpl enumeration, String name, int startOffset, int endOffset, boolean global) {
        EnumeratorImpl ei = new EnumeratorImpl(enumeration, name, startOffset, endOffset);
        postObjectCreateRegistration(global, ei);
        return ei;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CsmExpression getExplicitValue() {
        return null;
    }

    @Override
    public CsmEnum getEnumeration() {
        return _getEnumeration();
    }
    
    @Override
    public CsmScope getScope() {
        return getEnumeration();
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUMERATOR;
    }

    @Override
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
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
    
        // not null UID
        assert this.enumerationUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.enumerationUID, output);
    }
    
    public EnumeratorImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.enumerationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.enumerationUID != null;
        this.enumerationRef = null;
    }
}
