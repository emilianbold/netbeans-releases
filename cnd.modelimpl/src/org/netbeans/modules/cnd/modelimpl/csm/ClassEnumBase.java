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
    
    // only one of namespaceRef/namespaceUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private final NamespaceImpl namespaceRef;
    private final CsmUID<CsmNamespace> namespaceUID;
    
    private boolean isValid = true;

    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    // only one of containingClassRef/containingClassUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private final CsmClass containingClassRef;
    private final CsmUID<CsmClass> containingClassUID;
    
    public ClassEnumBase(String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass, AST ast) {
        super(ast, file);
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            namespaceUID = UIDCsmConverter.namespaceToUID(namespace);
            assert (namespaceUID != null || namespace == null)  : "null UID for namespace " + namespace;
            this.namespaceRef = null;
        } else {
            this.namespaceRef = namespace;
            this.namespaceUID = null;
        }        
        this.name = (name == null) ? "" : name;
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            containingClassUID = UIDCsmConverter.declarationToUID(containingClass);
            assert (containingClassUID != null || containingClass == null);
            this.containingClassRef = null;
        } else {
            this.containingClassRef = containingClass;
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

    private NamespaceImpl _getNamespaceImpl() {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            NamespaceImpl ns = (NamespaceImpl)UIDCsmConverter.UIDtoNamespace(namespaceUID);
            assert (ns != null || namespaceUID == null) : "null object for UID " + namespaceUID ;
            return ns;            
        } else {
            return namespaceRef;
        }
    }

    private CsmClass _getContainingClass() {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.USE_UID_TO_CONTAINER) {
            CsmClass containingClass = UIDCsmConverter.UIDtoDeclaration(containingClassUID);
            assert (containingClass != null || containingClassUID == null) : "null object for UID " + containingClassUID;
            return containingClass;            
        } else {
            return this.containingClassRef;
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
        CsmUID<CsmNamespace> writeNamespaceUID;
        CsmUID<CsmClass> writeContainingClassUID;
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            // could be null for structs defined in method body
            writeNamespaceUID = this.namespaceUID;
            writeContainingClassUID = this.containingClassUID;
        } else {
            // save reference
            writeNamespaceUID = UIDCsmConverter.namespaceToUID(this.namespaceRef);
            assert writeNamespaceUID != null || this.namespaceRef == null;
            writeContainingClassUID = UIDCsmConverter.declarationToUID(this.containingClassRef);
            assert writeContainingClassUID != null || this.containingClassRef == null;
        }
        factory.writeUID(writeNamespaceUID, output);
        factory.writeUID(writeContainingClassUID, output);
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
        CsmUID<CsmNamespace> readNamespaceUID = factory.readUID(input);
        CsmUID<CsmClass> readContainingClassUID = factory.readUID(input);
        if (TraceFlags.USE_UID_TO_CONTAINER) {
            // could be null for structs defined in method body
            this.namespaceUID = readNamespaceUID;
            // could be null
            this.containingClassUID = readContainingClassUID;

            this.containingClassRef = null;
            this.namespaceRef = null;
        } else {
            // restore reference
            this.namespaceRef = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(readNamespaceUID);
            assert this.namespaceRef != null || readNamespaceUID == null : "no object for UID " + readNamespaceUID;

            this.containingClassRef = UIDCsmConverter.UIDtoDeclaration(readContainingClassUID);
            assert this.containingClassRef != null || readContainingClassUID == null : "no object for UID " + readContainingClassUID;
            
            this.namespaceUID = null;
            this.containingClassUID = null;
        }
        this._static = input.readBoolean();
        this.visibility = PersistentUtils.readVisibility(input);
        assert this.visibility != null;
                
        assert TraceFlags.USE_REPOSITORY;
    }
}
