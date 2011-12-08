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

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 * Implements CsmEnum
 * @author Vladimir Kvashin
 */
public final class EnumImpl extends ClassEnumBase<CsmEnum> implements CsmEnum {
    
    private final List<CsmUID<CsmEnumerator>> enumerators;
    
    private EnumImpl(AST ast, NameHolder name, CsmFile file) {
        super(name, file, ast);
        enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    }

    private EnumImpl(String name, String qName, CsmFile file, int startOffset, int endOffset) {
        super(name, qName, file, startOffset, endOffset);
        enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    }
    
    private void init(CsmScope scope, AST ast, final CsmFile file, boolean register) {
	initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initEnumeratorList(ast, file, register);
        if (register) {
            register(scope, true);
        }
    }
    
    public static EnumImpl create(AST ast, CsmScope scope, final CsmFile file, boolean register) {
        NameHolder nameHolder = NameHolder.createEnumName(ast);
	EnumImpl impl = new EnumImpl(ast, nameHolder, file);
	impl.init(scope, ast, file, register);
        nameHolder.addReference(file, impl);
	return impl;
    }

    void addEnumerator(String name, int startOffset, int endOffset, boolean register) {
        EnumeratorImpl ei = EnumeratorImpl.create(this, name, startOffset, endOffset, register);
        CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
        enumerators.add(uid);
    }
    
    private void initEnumeratorList(AST ast, final CsmFile file, boolean global){
        //enum A { a, b, c };
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token, file, global);
                return;
            }
        }
        AST token = ast.getNextSibling();
        if( token != null) {
            AST enumList = null;
            if (token.getType() == CPPTokenTypes.ID) {
                //typedef enum C { a2, b2, c2 } D;
                token = token.getNextSibling();
            }
            if (token.getType() == CPPTokenTypes.LCURLY ) {
                //typedef enum { a1, b1, c1 } B;
                enumList = token.getNextSibling();
            }
            if (enumList != null && enumList.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST) {
                addList(enumList, file, global);
            }
        }
    }
    
    private void addList(AST token, final CsmFile file, boolean global){
        for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.ID ) {
                EnumeratorImpl ei = EnumeratorImpl.create(t, file, this, global);
                CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
                enumerators.add(uid);
            }
        }
    }
    
    @Override
    public Collection<CsmEnumerator> getEnumerators() {
        Collection<CsmEnumerator> out = UIDCsmConverter.UIDsToDeclarations(enumerators);
        return out;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection)getEnumerators();
    }
    
    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        _clearEnumerators();
    }
    
    private void _clearEnumerators() {
        Collection<CsmEnumerator> enumers = getEnumerators();
        Utils.disposeAll(enumers);
        RepositoryUtils.remove(enumerators);
    }
    
////////////////////////////////////////////////////////////////////////////
// impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.enumerators, output, false);
    }
    
    public EnumImpl(RepositoryDataInput input) throws IOException {
        super(input);
        int collSize = input.readInt();
        if (collSize < 0) {
            enumerators = new ArrayList<CsmUID<CsmEnumerator>>(0);
        } else {
            enumerators = new ArrayList<CsmUID<CsmEnumerator>>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.enumerators, input, collSize);
    }
}
