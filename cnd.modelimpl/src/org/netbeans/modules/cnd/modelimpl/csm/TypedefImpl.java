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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;

/**
 * Implements CsmTypedef
 * @author vk155633
 */
public class TypedefImpl extends OffsetableDeclarationBase<CsmTypedef> implements CsmTypedef, Disposable, CsmScopeElement {

    private final CharSequence name;
    private final CsmType type;
    private boolean typeUnnamed = false;
    // only one of containerRef/containerUID must be used (based on USE_REPOSITORY)
    private /*final*/ CsmObject containerRef;// can be set in onDispose or contstructor only
    private /*final*/ CsmUID<CsmIdentifiable> containerUID;

    public TypedefImpl(AST ast, CsmFile file, CsmObject container, CsmType type, String name, boolean global) {

        super(ast, file);

        if (UIDCsmConverter.isIdentifiable(container)) {
            this.containerUID = UIDCsmConverter.identifiableToUID((CsmIdentifiable) container);
            assert (containerUID != null || container == null);
            this.containerRef = null;
        } else {
            // yes, that's possible if it's somewhere within body
            this.containerRef = container;
        }

        if (type == null) {
            this.type = createType(ast);
        } else {
            this.type = type;
        }
        if (name.length()==0) {
            name = fixBuiltInTypedef(ast);
        }
        this.name = QualifiedNameCache.getManager().getString(name);
        if (!global) {
            Utils.setSelfUID(this);
        }
    }

    private String fixBuiltInTypedef(AST ast){
        // typedef cannot be unnamed
        AST first = ast.getFirstChild();
        if (first != null) {
            AST last = null;
            while(first != null) {
                if (first.getType() == CPPTokenTypes.COMMA || first.getType() == CPPTokenTypes.SEMICOLON) {
                    break;
                }
                last = first;
                first = first.getNextSibling();
            }
            if (last != null) {
                first = last.getFirstChild();
                while(first != null) {
                    if (first.getText() != null && first.getText().length()>0) {
                        if (Character.isJavaIdentifierStart(first.getText().charAt(0))){
                            last = first;
                        }
                    }
                    first = first.getNextSibling();
                }
                if (last.getText() != null && last.getText().length()>0) {
                   if (Character.isJavaIdentifierStart(last.getText().charAt(0))){
                        return last.getText();
                    }
                }
            }
        }
        return "";
    }

    public boolean isTypeUnnamed() {
        return typeUnnamed;
    }

    public void setTypeUnnamed() {
        typeUnnamed = true;
    }

//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    public CsmScope getScope() {
        // TODO: ???
        //return getContainingFile();
        CsmObject container = _getContainer();
        if (container instanceof CsmNamespace) {
            return (CsmNamespace) container;
        } else if (container instanceof CsmClass) {
            return (CsmClass) container;
        } else {
            return getContainingFile();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        if (this.type != null && this.type instanceof Disposable) {
            ((Disposable) this.type).dispose();
        }
        CsmScope scope = getScope();
        if (scope instanceof MutableDeclarationsContainer) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
        FileImpl file = (FileImpl) getContainingFile();
        file.getProjectImpl(true).unregisterDeclaration(this);
    }

    private synchronized void onDispose() {
        if (this.containerRef == null) {
            // restore container from it's UID when needed
            this.containerRef = this.containerRef != null ? this.containerRef : UIDCsmConverter.UIDtoIdentifiable(this.containerUID);
            assert (this.containerRef != null || this.containerUID == null) : "null object for UID " + this.containerUID;
        }
    }

    public CharSequence getQualifiedName() {
        CsmObject container = _getContainer();
        if (CsmKindUtilities.isClass(container)) {
            return CharSequences.create(((CsmClass) container).getQualifiedName() + "::" + getQualifiedNamePostfix()); // NOI18N
        } else if (CsmKindUtilities.isNamespace(container)) {
            CharSequence nsName = ((CsmNamespace) container).getQualifiedName();
            if (nsName != null && nsName.length() > 0) {
                return CharSequences.create(nsName.toString() + "::" + getQualifiedNamePostfix()); // NOI18N
            }
        }
        return getName();
    }

    public CharSequence getName() {
        /*if( name == null ) {
        AST tokId = null;
        for( AST token = getAst().getFirstChild(); token != null; token = token.getNextSibling() ) {
        if( token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
        AST child = token.getFirstChild();
        if( child != null && child.getType() == CPPTokenTypes.ID ) {
        name = child.getText();
        }
        }
        }
        }
        if( name == null ) {
        name = "";
        }*/
        return name;
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.TYPEDEF;
    }

    private final CsmType createType(AST node) {
        //
        // TODO: replace this horrible code with correct one
        //
        //if( type == null ) {
        AST ptrOperator = null;
        int arrayDepth = 0;
        AST classifier = null;
        for (AST token = node.getFirstChild(); token != null; token = token.getNextSibling()) {
//                if( token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND || 
//                        token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//                    classifier = token;
//                    break;
//                }
            switch (token.getType()) {
                case CPPTokenTypes.CSM_TYPE_COMPOUND:
                case CPPTokenTypes.CSM_TYPE_BUILTIN:
                    classifier = token;
                    break;
                case CPPTokenTypes.LITERAL_struct:
                    AST next = token.getNextSibling();
                    if (next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        classifier = next;
                        break;
                    }
                    break;
            }
            if (classifier != null) {
                break;
            }
        }
        if (classifier != null) {
            return TypeFactory.createType(classifier, getContainingFile(), ptrOperator, arrayDepth);
        }
        //}
        return null;
    }

    public CsmType getType() {
        return type;
    }

    private synchronized CsmObject _getContainer() {
        CsmObject container = this.containerRef;
        if (container == null) {
            container = UIDCsmConverter.UIDtoIdentifiable(this.containerUID);
            assert (container != null || this.containerUID == null) : "null object for UID " + this.containerUID;
        }
        return container;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    private static final byte UNNAMED_TYPE_FLAG = 1;
    private static final byte UNNAMED_TYPEDEF_FLAG = 1 << 1;
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        byte unnamedState = 0;
        if (typeUnnamed) {
            unnamedState |= UNNAMED_TYPE_FLAG;
        }
        boolean unnamed = getName().length() == 0;
        if (unnamed) {
            unnamedState |= UNNAMED_TYPEDEF_FLAG;
        }
        output.writeByte(unnamedState);
        if (unnamed) {
            super.writeUID(output);
        }
        assert this.type != null;
        PersistentUtils.writeType(this.type, output);

        // not null
        if (this.containerUID == null) {
            System.err.println("trying to write non-writable typedef:" + this.getContainingFile() + toString()); // NOI18N
            if (this.containerRef == null) {
                System.err.println("typedef doesn't have container at all"); // NOI18N
            }
        } else {
            UIDObjectFactory.getDefaultFactory().writeUID(this.containerUID, output);
        }
    }

    public TypedefImpl(DataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        byte unnamedState = input.readByte();
        typeUnnamed = false;
        if ((unnamedState & UNNAMED_TYPE_FLAG) == UNNAMED_TYPE_FLAG) {
            typeUnnamed = true;
        }
        boolean unnamed = false;
        if ((unnamedState & UNNAMED_TYPEDEF_FLAG) == UNNAMED_TYPEDEF_FLAG) {
            unnamed = true;
        }
        if (unnamed) {
            super.readUID(input);
        }
        this.type = PersistentUtils.readType(input);
        assert this.type != null;

        this.containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // should not be null UID
        if (this.containerUID == null) {
            System.err.println("non-writable object was read:" + this.getContainingFile() + toString()); // NOI18N
        }
        this.containerRef = null;
    }
}
