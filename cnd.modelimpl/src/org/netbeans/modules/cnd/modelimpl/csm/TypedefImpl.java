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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.CsmObjectAccessor;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Implements CsmTypedef
 * @author vk155633
 */
public class TypedefImpl extends OffsetableDeclarationBase<CsmTypedef>  implements CsmTypedef, Disposable, CsmScopeElement {
    
    private final String name;
    private final CsmType type;
    
    // only one of containerOLD/containerUID must be used (based on USE_REPOSITORY)
    private CsmObject containerOLD;
    private CsmObjectAccessor    containerAccessor;
            
    public TypedefImpl(AST ast, CsmFile file, CsmObject container, CsmType type, String name) {
        super(ast, file);
	this._setContainer(container);
        if (type == null) {
            this.type = createType(ast);
        } else {
            this.type = type;
        }
        this.name = name;
    }
    
//    Moved to OffsetableDeclarationBase
//    public String getUniqueName() {
//        return getQualifiedName();
//    }
    
    public CsmScope getScope() {
        // TODO: ???
        //return getContainingFile();
        CsmObject container = _getContainer();
        if( container instanceof CsmNamespace ) {
            return (CsmNamespace) container;
        }
        else if( container instanceof CsmClass ) {
            return (CsmClass)container;
        }
        else {
            return getContainingFile();
        }
    }

    public void dispose() {
        CsmScope scope = getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }

    public String getQualifiedName() {
        CsmObject container = _getContainer();
        if( CsmKindUtilities.isClass(container) ) {
	    return ((CsmClass) container).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else if( CsmKindUtilities.isNamespace(container) ) {
	    String nsName = ((CsmNamespace) container).getQualifiedName();
	    if( nsName != null && nsName.length() > 0 ) {
		return nsName + "::" + getQualifiedNamePostfix(); // NOI18N
	    }
	}
        return getName();
    }

    public String getName() {
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
            for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
//                if( token.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND || 
//                        token.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
//                    classifier = token;
//                    break;
//                }
                switch( token.getType() ) {
                    case CPPTokenTypes.CSM_TYPE_COMPOUND:
                    case CPPTokenTypes.CSM_TYPE_BUILTIN:
                        classifier = token;
                        break;
                    case CPPTokenTypes.LITERAL_struct:
                        AST next = token.getNextSibling();
                        if( next != null && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
                            classifier = next;
                            break;
                        }
                        break;
                }
                if( classifier != null ) {
                    break;
                }
            }
            if( classifier != null ) {
                return TypeImpl.createType(classifier, getContainingFile(), ptrOperator, arrayDepth);
            }
        //}
            return null;
    }
    
    public CsmType getType() {
        return type;
    }

    private CsmObject _getContainer() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmObject container = UIDCsmConverter.accessorToObject(this.containerAccessor);
            assert (container != null || containerAccessor == null);
            return container;
        } else {
            return containerOLD;
        }
    }

    private void _setContainer(CsmObject container) {
        if (TraceFlags.USE_REPOSITORY) {
            this.containerAccessor = UIDCsmConverter.objectToAccessor(container);
            assert (containerAccessor != null || container == null);
        } else {
            this.containerOLD = container;
        }
    }
    
}
