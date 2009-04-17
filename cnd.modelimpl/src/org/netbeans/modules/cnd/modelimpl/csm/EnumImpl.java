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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.* ;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Implements CsmEnum
 * @author Vladimir Kvashin
 */
public class EnumImpl extends ClassEnumBase<CsmEnum> implements CsmEnum {
    
    private final List<CsmUID<CsmEnumerator>> enumerators = new ArrayList<CsmUID<CsmEnumerator>>();
    
    private EnumImpl(AST ast, CsmFile file) {
        super(findName(ast), file, ast);
    }
    
    private void init(CsmScope scope, AST ast, boolean register) {
	initScope(scope, ast);
        initQualifiedName(scope, ast);
        if (register) {
            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
        } else {
            Utils.setSelfUID(this);
        }
        initEnumeratorList(ast, register);
        if (register) {
            register(scope, true);
        }
    }
    
    public static EnumImpl create(AST ast, CsmScope scope, CsmFile file, boolean register) {
	EnumImpl impl = new EnumImpl(ast, file);
	impl.init(scope, ast, register);
	return impl;
    }
    
    private static String findName(AST ast){
        String name = AstUtil.findId(ast, CPPTokenTypes.RCURLY);
        if (name == null || name.length()==0){
            AST token = ast.getNextSibling();
            if( token != null) {
                if (token.getType() == CPPTokenTypes.ID) {
                    //typedef enum C { a2, b2, c2 } D;
                    name = token.getText();
                }
            }
        }
        return name;
    }
    
    private void initEnumeratorList(AST ast, boolean global){
        //enum A { a, b, c };
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if( token.getType() == CPPTokenTypes.CSM_ENUMERATOR_LIST ) {
                addList(token, global);
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
                addList(enumList, global);
            }
        }
    }
    
    private void addList(AST token, boolean global){
        for( AST t = token.getFirstChild(); t != null; t = t.getNextSibling() ) {
            if( t.getType() == CPPTokenTypes.ID ) {
                EnumeratorImpl ei = new EnumeratorImpl(t, this);
                if (global) {
                    RepositoryUtils.put(ei);
                } else {
                    Utils.setSelfUID(ei);
                }
                CsmUID<CsmEnumerator> uid = UIDCsmConverter.<CsmEnumerator>objectToUID(ei);
                enumerators.add(uid);
            }
        }
    }
    
    public Collection<CsmEnumerator> getEnumerators() {
        Collection<CsmEnumerator> out = UIDCsmConverter.UIDsToDeclarations(enumerators);
        return out;
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection)getEnumerators();
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUM;
    }
    
    @Override
    public void dispose() {
        _clearEnumerators();
        super.dispose();
    }
    
    private void _clearEnumerators() {
        Collection<CsmEnumerator> enumers = getEnumerators();
        Utils.disposeAll(enumers);
        RepositoryUtils.remove(enumerators);
    }
    
////////////////////////////////////////////////////////////////////////////
// impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(this.enumerators, output, false);
    }
    
    public EnumImpl(DataInput input) throws IOException {
        super(input);
        UIDObjectFactory.getDefaultFactory().readUIDCollection(this.enumerators, input);
    }
}
