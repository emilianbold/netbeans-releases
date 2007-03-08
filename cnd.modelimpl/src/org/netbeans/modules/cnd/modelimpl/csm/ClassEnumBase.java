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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.ObjectBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Common ancestor for ClassImpl and EnumImpl
 * @author Vladimir Kvashin
 */
public abstract class ClassEnumBase<T> extends OffsetableDeclarationBase<T> implements Disposable, CsmCompoundClassifier<T>, CsmMember<T> {
    
    private final String name;
    
    private final String qualifiedName;
    
    // only one of namespaceOLD/namespaceUID must be used (based on USE_REPOSITORY)    
    private final NamespaceImpl namespaceOLD;
    private final CsmUID<CsmNamespace> namespaceUID;
    
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    // only one of containingClassOLD/containingClassUID must be used (based on USE_REPOSITORY)  
    private final CsmClass containingClassOLD;
    private final CsmUID<CsmClass> containingClassUID;
    
    public ClassEnumBase(String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass, AST ast) {
        super(ast, file);
        if (TraceFlags.USE_REPOSITORY) {
            namespaceUID = UIDCsmConverter.namespaceToUID(namespace);
            assert (namespaceUID != null || namespace == null);
            this.namespaceOLD = null;
        } else {
            this.namespaceOLD = namespace;
            this.namespaceUID = null;
        }        
        this.name = (name == null) ? "" : name;
        if (TraceFlags.USE_REPOSITORY) {
            containingClassUID = UIDCsmConverter.declarationToUID(containingClass);
            assert (containingClassUID != null || containingClass == null);
            this.containingClassOLD = null;
        } else {
            this.containingClassOLD = containingClass;
            this.containingClassUID = null;
        }         
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
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.put(this);
        }
        if( ProjectBase.canRegisterDeclaration(this) ) {
            registerInProject();
            NamespaceImpl ns = _getNamespaceImpl();
            if (ns != null) {
                // It can be local class
                ns.addDeclaration(this);
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

    private CsmClass _getContainingClass() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmClass containingClass = UIDCsmConverter.UIDtoDeclaration(containingClassUID);
            assert (containingClass != null || containingClassUID == null);
            return containingClass;            
        } else {
            return containingClassOLD;
        }
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
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        assert this.namespaceUID != null;
        factory.writeUID(this.namespaceUID, output);
        factory.writeUID(this.containingClassUID, output); // could be null
        output.writeBoolean(this._static);
        assert this.visibility != null;
        PersistentUtils.writeVisibility(this.visibility, output);
    }  
    
    protected ClassEnumBase(DataInput input) throws IOException {
        super(input);
        this.isValid = input.readBoolean();
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.qualifiedName = QualifiedNameCache.getString(input.readUTF());
        assert this.qualifiedName != null;
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.namespaceUID = factory.readUID(input);
        assert this.namespaceUID != null;
        this.containingClassUID = factory.readUID(input); // could be null
        this._static = input.readBoolean();
        this.visibility = PersistentUtils.readVisibility(input);
        assert this.visibility != null;
                
        assert TraceFlags.USE_REPOSITORY;
        this.containingClassOLD = null;
        this.namespaceOLD = null;
    }     
}
