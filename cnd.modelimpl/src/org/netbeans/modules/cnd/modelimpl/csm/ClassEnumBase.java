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

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Common ancestor for ClassImpl and EnumImpl
 * @author Vladimir Kvashin
 */
public abstract class ClassEnumBase<T> extends OffsetableDeclarationBase<T> implements Disposable, CsmCompoundClassifier<T>, CsmMember<T> {
    
    private final String name;
    
    private /*final*/ String qualifiedName;
    
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY)
    private CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;
    
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    
    protected ClassEnumBase(String name, CsmFile file, AST ast) {
        super(ast, file);
        this.name = (name == null) ? "" : name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Initialization method. 
     * Should be called immediately after object creation. 
     *
     * Descendants may override it; in this case it's a descendant's responsibility
     * to call super.init()
     */
    protected void init(CsmScope scope, AST ast) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
	    assert (this.scopeUID != null || scope == null) : "null UID for class scope " + scope;
            this.scopeRef = null;
        } else {
            this.scopeRef = scope;
            this.scopeUID = null;
        }   
	
	String qualifiedNamePostfix = getQualifiedNamePostfix();
        if(  CsmKindUtilities.isNamespace(scope) ) {
            qualifiedName = Utils.getQualifiedName(qualifiedNamePostfix, (CsmNamespace) scope);
        }
	else if( CsmKindUtilities.isClass(scope) ) {
            qualifiedName = ((CsmClass) scope).getQualifiedName() + "::" + qualifiedNamePostfix; // NOI18N
	}
        else  {
	    qualifiedName = qualifiedNamePostfix;
        }
        // can't register here, because descendant class' constructor hasn't yet finished!
        // so registering is a descendant class' responsibility
    }
    
    abstract public Kind getKind();
    
    protected void register(CsmScope scope) {
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.put(this);
        }
        if( ProjectBase.canRegisterDeclaration(this) ) {
            registerInProject();
	    
	    
	    if( getContainingClass() == null ) {
		if(  CsmKindUtilities.isNamespace(scope) ) {
		    ((NamespaceImpl) scope).addDeclaration(this);
		}
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
    
    public NamespaceImpl getContainingNamespaceImpl() {
	CsmScope scope = getScope();
	return (scope instanceof NamespaceImpl) ? (NamespaceImpl) scope : null;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

  
    public CsmScope getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            if (TraceFlags.USE_REPOSITORY) {
                scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
                assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
            }
        }
        return scope;
    }

    public void dispose() {
        super.dispose();
        onDispose();
        if (getContainingNamespaceImpl() != null) {
            getContainingNamespaceImpl().removeDeclaration(this);
        }
	unregisterInProject();
        isValid = false;
    }
        
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }
    
    public boolean isValid() {
        return isValid && getContainingFile().isValid();
    }

    public CsmClass getContainingClass() {
	CsmScope scope = getScope();
	return CsmKindUtilities.isClass(scope) ? (CsmClass) scope : null;
    }
    
//    private void setContainingClass(CsmClass cls) {
//        containingClassRef = cls;
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

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output); 
        output.writeBoolean(this.isValid);
	
        assert this.name != null;
        output.writeUTF(this.name);
	
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName);
        
	UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        
        output.writeBoolean(this._static);
	
        assert this.visibility != null;
        PersistentUtils.writeVisibility(this.visibility, output);
        
        // write UID for unnamed classifier
        if (getName().length() == 0) {
            super.writeUID(output);
        }
    }  
    
    protected ClassEnumBase(DataInput input) throws IOException {
        super(input);
        this.isValid = input.readBoolean();
	
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
	
        this.qualifiedName = QualifiedNameCache.getString(input.readUTF());
        assert this.qualifiedName != null;
	
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.scopeRef = null;
	
        this._static = input.readBoolean();
	
        this.visibility = PersistentUtils.readVisibility(input);
        assert this.visibility != null;
                
        assert TraceFlags.USE_REPOSITORY;
        
        // restore UID for unnamed classifier
        if (getName().length() == 0) {
            super.readUID(input);
        }
        
    }
}
