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
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * Common ancestor for ClassImpl and EnumImpl
 * @author Vladimir Kvashin
 */
public abstract class ClassEnumBase<T> extends OffsetableDeclarationBase<T> implements Disposable, CsmCompoundClassifier<T>, CsmMember<T> {
    
    private final String name;
    
    private String qualifiedName;
    
    // only one of namespaceOLD/namespaceUID must be used (based on USE_REPOSITORY)    
    private NamespaceImpl namespaceOLD;
    private CsmUID<CsmNamespace> namespaceUID;
    
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    // only one of containingClassOLD/containingClassUID must be used (based on USE_REPOSITORY)  
    private CsmClass containingClassOLD = null;
    private CsmUID<CsmClass> containingClassUID = null;
    
    public ClassEnumBase(String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass, AST ast) {
        super(ast, file);
        this._setNamespaceImpl(namespace);
        this.name = (name == null) ? "" : name;
        this._setContainingClass(containingClass);
        if( containingClass == null ) {
            qualifiedName = Utils.getQualifiedName(getQualifiedNamePostfix(), namespace);
        }
        else {
            qualifiedName = containingClass.getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
        }
        // can't register here, because descendant class' constructor hasn't yet finished!
        // so registering is a descendant class' responsibility
    }
    
    public String getName() {
        return name;
    }
    
    abstract public Kind getKind();
    
    protected void register() {
        if( ProjectBase.canRegisterDeclaration(this) ) {
            registerInProject();
            NamespaceImpl ns = _getNamespaceImpl();
            if (ns != null) {
                // It can be local class
                ns.addDeclaration(this);
            }
        } else {
            if (TraceFlags.USE_REPOSITORY) {
                RepositoryUtils.put(this);
            }
        }
    }
    
    private void registerInProject() {
        ((ProjectBase) getContainingFile().getProject()).registerDeclaration(this);
    }
    
    private void unregisterInProject() {
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }
    
    public CsmNamespace getContainingNamespace() {
        return _getNamespaceImpl();
    }
    
    public NamespaceImpl getContainingNamespaceImpl() {
        return _getNamespaceImpl();
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

  
    public CsmScope getScope() {
        // TODO: think over: containing class?
        // TODO: what if declared in a statement?
        return getContainingNamespace(); 
    }

    public void dispose() {
        if (getContainingNamespaceImpl() != null) {
            getContainingNamespaceImpl().removeDeclaration(this);
        }
	unregisterInProject();
        isValid = false;
    }
        
    public boolean isValid() {
        return isValid;
    }
    
    public CsmClass getContainingClass() {
        return _getContainingClass();
    }
    
//    private void setContainingClass(CsmClass cls) {
//        containingClass = cls;
//        qualifiedName = cls.getQualifiedName() + "::" + getName();
//    }
    
    public CsmVisibility getVisibility() {
        return visibility;
    }
    
    public void setVisibility(CsmVisibility visibility) {
        this.visibility = visibility;
    }
    
    public boolean isStatic() {
        return _static;
    }
    
    public void setStatic(boolean _static) {
        this._static = _static;
    }

    private NamespaceImpl _getNamespaceImpl() {
        if (TraceFlags.USE_REPOSITORY) {
            NamespaceImpl ns = (NamespaceImpl)UIDCsmConverter.UIDtoNamespace(namespaceUID);
            assert (ns != null || namespaceUID == null);
            return ns;            
        } else {
            return namespaceOLD;
        }
    }

    private void _setNamespaceImpl(NamespaceImpl ns) {
        if (TraceFlags.USE_REPOSITORY) {
            namespaceUID = UIDCsmConverter.namespaceToUID(ns);
            assert (namespaceUID != null || ns == null);
        } else {
            this.namespaceOLD = ns;
        }
    }

    private CsmClass _getContainingClass() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmClass containingClass = UIDCsmConverter.UIDtoDeclaration(containingClassUID);
            assert (containingClass != null || containingClassUID == null);
            return containingClass;            
        } else {
            return containingClassOLD;
        }
    }

    private void _setContainingClass(CsmClass containingClass) {
        if (TraceFlags.USE_REPOSITORY) {
            containingClassUID = UIDCsmConverter.declarationToUID(containingClass);
            assert (containingClassUID != null || containingClass == null);
        } else {
            this.containingClassOLD = containingClass;
        }     
    }

}
