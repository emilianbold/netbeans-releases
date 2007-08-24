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

package org.netbeans.modules.cnd.modelimpl.uid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.api.model.CsmBuiltIn;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.modelimpl.csm.core.Unresolved;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * utilities to create CsmUID for CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDUtilities {
    
    /** Creates a new instance of UIDUtilities */
    private UIDUtilities() {
    }
 
    public static CsmUID<CsmProject> createProjectUID(CsmProject prj) {
        return UIDManager.instance().getSharedUID(new ProjectUID(prj));
    } 
    
    public static CsmUID<CsmFile> createFileUID(CsmFile file) {
        return UIDManager.instance().getSharedUID(new FileUID(file));
    } 

    public static CsmUID<CsmNamespace> createNamespaceUID(CsmNamespace ns) {
        return UIDManager.instance().getSharedUID(new NamespaceUID(ns));
    }

    public static <T extends CsmOffsetableDeclaration> CsmUID<T> createDeclarationUID(T declaration) {
        assert (! (declaration instanceof CsmBuiltIn)) : "built-in have own UIDs";
        CsmUID<T> uid;
        if (!ProjectBase.canRegisterDeclaration(declaration)) {
            uid = handleUnnamedDeclaration((CsmOffsetableDeclaration)declaration);
        } else {
            if (declaration instanceof CsmTypedef) {
                uid = new TypedefUID((CsmTypedef)declaration);
            } else if (declaration instanceof CsmClassifier) {
                uid = new ClassifierUID(declaration);
            } else {
                uid = new DeclarationUID(declaration);
            }
        }
        return UIDManager.instance().getSharedUID(uid);
    }
    
    public static CsmUID<CsmMacro> createMacroUID(CsmMacro macro) {
        return UIDManager.instance().getSharedUID(new MacroUID(macro));
    }    

    public static CsmUID<CsmInclude> createIncludeUID(CsmInclude incl) {
        return UIDManager.instance().getSharedUID(new IncludeUID(incl));
    }    
    
    public static CsmUID<CsmClass> createUnresolvedClassUID(String name, CsmProject project) {
	return UIDManager.instance().getSharedUID(new UnresolvedClassUID(name, project));
    }

    public static CsmUID<CsmFile> createUnresolvedFileUID(CsmProject project) {
	return UIDManager.instance().getSharedUID(new UnresolvedFileUID(project));
    }

    public static CsmUID<CsmNamespace> createUnresolvedNamespaceUID(CsmProject project) {
	return UIDManager.instance().getSharedUID(new UnresolvedNamespaceUID(project));
    }
    
    private static CsmUID handleUnnamedDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_UNNAMED_DECLARATIONS) {
            System.err.print("\n\ndeclaration with empty name '" + decl.getUniqueName() + "'");
            new CsmTracer().dumpModel(decl);
        }
        if (decl instanceof CsmClassifier) {
            return new UnnamedClassifierUID(decl, UnnamedID.incrementAndGet());
        } else {
            return new UnnamedOffsetableDeclarationUID(decl, UnnamedID.incrementAndGet());
        }
    }
    
    private static AtomicInteger UnnamedID = new AtomicInteger(0);
    //////////////////////////////////////////////////////////////////////////
    // impl details
    
    /**
     * UID for CsmProject
     */
    /* package */ static final class ProjectUID extends KeyBasedUID<CsmProject> {
        public ProjectUID(CsmProject project) {
            super(KeyUtilities.createProjectKey(project));
        }
        
        /* package */ ProjectUID (DataInput aStream) throws IOException {
            super(aStream);
        }
    }    
  
    /**
     * UID for CsmNamespace
     */
    /* package */ static final class NamespaceUID extends KeyBasedUID<CsmNamespace> {
        public NamespaceUID(CsmNamespace ns) {
            super(KeyUtilities.createNamespaceKey(ns));
        }
        
        /* package */ NamespaceUID (DataInput aStream) throws IOException {
            super(aStream);
        }
    }    
    
    /**
     * UID for CsmFile
     */
    /* package */ static final class FileUID extends KeyBasedUID<CsmFile> {
        public FileUID(CsmFile file) {
            super(KeyUtilities.createFileKey(file));
        }
        
        /* package */ FileUID (DataInput aStream) throws IOException {
             super(aStream);
        }
    }
    
    /**
     * base UID for CsmDeclaration
     */
    private static abstract class OffsetableDeclarationUIDBase<T extends CsmOffsetableDeclaration> extends KeyBasedUID<T> {
        public OffsetableDeclarationUIDBase(T declaration) {
            this(KeyUtilities.createOffsetableDeclarationKey((OffsetableDeclarationBase)declaration));       
        }
        
        protected OffsetableDeclarationUIDBase(Key key) {
            super(key);
        }
        
        /* package */ OffsetableDeclarationUIDBase (DataInput aStream) throws IOException {
            super(aStream);
        }

        public String toString() {
            String retValue = getToStringPrefix() + ":" + super.toString(); // NOI18N
            return retValue;
        }
        
        protected String getToStringPrefix() {
            return "UID for OffsDecl"; // NOI18N
        }
    }  
    
    /**
     * UID for CsmTypedef
     */
    /* package */ static final class TypedefUID<T extends CsmTypedef> extends OffsetableDeclarationUIDBase<T> {
        public TypedefUID(T typedef) {
            super(typedef);
//            assert typedef instanceof RegistarableDeclaration;
//            if (!((RegistarableDeclaration)typedef).isRegistered()) {
//                System.err.print("\n\nunregistered declaration'" + typedef.getUniqueName() + "'");
//                new CsmTracer().dumpModel(typedef);
//            }
//            assert ((RegistarableDeclaration)typedef).isRegistered();            
        }
        
        /* package */ TypedefUID (DataInput aStream) throws IOException {
            super(aStream);
        }
        
        protected String getToStringPrefix() {
            return "TypedefUID"; // NOI18N
        }
        
    }
    
    /**
     * UID for CsmMacro
     */
    /* package */ static final class MacroUID extends KeyBasedUID<CsmMacro> {
        public MacroUID(CsmMacro macro) {
            super(KeyUtilities.createMacroKey(macro));
        }
        
        /* package */ MacroUID (DataInput aStream) throws IOException {
            super(aStream);
        }
    }
    
    /**
     * UID for CsmInclude
     */
    /* package */ static final class IncludeUID extends KeyBasedUID<CsmInclude> {
        public IncludeUID(CsmInclude incl) {
            super(KeyUtilities.createIncludeKey(incl));
        }
        
        /* package */ IncludeUID (DataInput aStream) throws IOException {
            super(aStream);
        }
    }
    
    /**
     * UID for CsmClassifier
     */
    /* package */ static final class DeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {
        public DeclarationUID(T decl) {
            super(decl);
        }
        
        /* package */ DeclarationUID( DataInput aStream) throws IOException {
            super(aStream);
        }
        
        protected String getToStringPrefix() {
            return "DeclarationUID"; // NOI18N
        }
        
    } 
    
    /**
     * UID for CsmClassifier
     */
    /* package */ static final class ClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {
        public ClassifierUID(T classifier) {
            super(classifier);
        }
        
        /* package */ ClassifierUID( DataInput aStream) throws IOException {
            super(aStream);
        }
        
        protected String getToStringPrefix() {
            return "ClassifierUID"; // NOI18N
        }
        
    }     
    
    /**
     * UID for CsmClassifier with empty getName()
     */    
    /* package */ static final class UnnamedClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {
        public UnnamedClassifierUID(T classifier, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase)classifier, index));
        }

        /* package */ UnnamedClassifierUID (DataInput aStream) throws IOException {
            super(aStream);
        }
        
        protected String getToStringPrefix() {
            return "<UNNAMED CLASSIFIER UID>"; // NOI18N
        }        
    }
    
    /**
     * UID for CsmDeclaration with empty getName()
     */    
    /* package */ static final class UnnamedOffsetableDeclarationUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUIDBase<T> {
        public UnnamedOffsetableDeclarationUID(T decl, int index) {
            super(KeyUtilities.createUnnamedOffsetableDeclarationKey((OffsetableDeclarationBase)decl, index));
        }

        /* package */ UnnamedOffsetableDeclarationUID (DataInput aStream) throws IOException {
            super(aStream);
        }
        
        protected String getToStringPrefix() {
            return "<UNNAMED OFFS-DECL UID>"; // NOI18N
        }        
    } 
    
    /**
     * Abstract base class for Unresolved* UIDs.
     */    
    /* package */ static abstract class UnresolvedUIDBase<T> implements CsmUID<T>, SelfPersistent {
	
	private CsmUID<CsmProject> projectUID;
	
        public UnresolvedUIDBase(CsmProject project) {
            projectUID = project.getUID();
        }
	
	protected ProjectBase getProject() {
	    return (ProjectBase) projectUID.getObject();
	}

        /* package */ UnresolvedUIDBase (DataInput aStream) throws IOException {
            projectUID = UIDObjectFactory.getDefaultFactory().readUID(aStream);
        }

	public abstract T getObject();

	public void write(DataOutput output) throws IOException {
	    UIDObjectFactory.getDefaultFactory().writeUID(projectUID, output);
	}
	
        protected String getToStringPrefix() {
            return "<UNRESOLVED UID>"; // NOI18N
        }        
    }
    
    /* package */ static final class UnresolvedClassUID<T> extends UnresolvedUIDBase<CsmClass> {
	
	private String name;
	
	public UnresolvedClassUID(String name, CsmProject project) {
	    super(project);
	    this.name = name;
	}
	
	public CsmClass getObject() {
            return getProject().getDummyForUnresolved(name);
	}

	public UnresolvedClassUID(DataInput input) throws IOException {
	    super(input);
	    name = PersistentUtils.readUTF(input);
	}
	
	@Override
	public void write(DataOutput output) throws IOException {
	    super.write(output);
            PersistentUtils.writeUTF(name, output);
	}
	
    }
    
    /* package */ static final class UnresolvedNamespaceUID extends UnresolvedUIDBase<CsmNamespace> {
	
        public UnresolvedNamespaceUID(CsmProject project) {
            super(project);
	}
	
	public UnresolvedNamespaceUID(DataInput input) throws IOException {
	    super(input);
	}

	public CsmNamespace getObject() {
	    return getProject().getUnresolvedNamespace();
	}
    }
	
    /* package */ static final class UnresolvedFileUID extends UnresolvedUIDBase<CsmFile> {
	
        public UnresolvedFileUID(CsmProject project) {
            super(project);
	}
	
	public UnresolvedFileUID(DataInput input) throws IOException {
	    super(input);
	}

	public CsmFile getObject() {
	    return getProject().getUnresolvedFile();
	}
    }
}
