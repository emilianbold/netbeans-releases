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

import org.netbeans.modules.cnd.api.model.CsmBuiltIn;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
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

/**
 * utilities to create CsmUID for CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDUtilities {
    
    /** Creates a new instance of UIDUtilities */
    private UIDUtilities() {
    }
 
    public static CsmUID<CsmProject> createProjectUID(CsmProject prj) {
        return new ProjectUID(prj);
    } 
    
    public static CsmUID<CsmFile> createFileUID(CsmFile file) {
        return new FileUID(file);
    } 

    public static CsmUID<CsmNamespace> createNamespaceUID(CsmNamespace ns) {
        return new NamespaceUID(ns);
    }

    public static <T extends CsmOffsetableDeclaration> CsmUID<T> createDeclarationUID(T declaration) {
        assert (! (declaration instanceof CsmBuiltIn)) : "built-in have own UIDs";
        if (!ProjectBase.canRegisterDeclaration(declaration)) {
            return handleUnnamedDeclaration((CsmOffsetableDeclaration)declaration);
        } else {
            if (declaration instanceof CsmTypedef) {
                return new TypedefUID((CsmTypedef)declaration);
            } else if (declaration instanceof CsmClassifier) {
                return new ClassifierUID(declaration);
            } else {
                return new OffsetableDeclarationUID(declaration);
            }
        }
    }

//    public static <T extends CsmOffsetableDeclaration> CsmUID<T> createClassifierUID(T classifier, CsmProject prj) {
//        assert (! (classifier instanceof CsmBuiltIn)) : "built-in have own UIDs";
//        if (classifier.getName().length() == 0) {
//            return handleUnnamedDeclaration((CsmOffsetableDeclaration)classifier);
//        } else {
//            return new ClassifierUID(classifier, prj);
//        }
//    }
    
    private static CsmUID handleUnnamedDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.TRACE_UNNAMED_DECLARATIONS) {
            System.err.print("\n\ndeclaration with empty name '" + decl.getUniqueName() + "'");
            new CsmTracer().dumpModel(decl);
        }
        if (decl instanceof CsmClassifier) {
            return new UnnamedClassifierUID((CsmClassifier)decl);
        } else {
            return new ObjectBasedUID(decl);
        }
    }
    //////////////////////////////////////////////////////////////////////////
    // impl details
    
    /**
     * UID for CsmProject
     */
    /* package */ static final class ProjectUID extends KeyBasedUID<CsmProject> {
        public ProjectUID(CsmProject project) {
            super(KeyUtilities.createProjectKey(project));
        }
        
        /* package */ ProjectUID (){
            
        }
    }    
  
    /**
     * UID for CsmNamespace
     */
    /* package */ static final class NamespaceUID extends KeyBasedUID<CsmNamespace> {
        public NamespaceUID(CsmNamespace ns) {
            super(KeyUtilities.createNamespaceKey(ns));
        }
        
        /* package */ NamespaceUID () {
            
        }
    }    
    
    /**
     * UID for CsmFile
     */
    /* package */ static final class FileUID extends KeyBasedUID<CsmFile> {
        public FileUID(CsmFile file) {
            super(KeyUtilities.createFileKey(file));
        }
        
        /* package */ FileUID () {
            
        }
    }
    
    /**
     * UID for CsmDeclaration
     */
    private static class OffsetableDeclarationUID<T extends CsmOffsetableDeclaration> extends KeyBasedUID<T> {
        public OffsetableDeclarationUID(T declaration) {
            super(KeyUtilities.createOffsetableDeclarationKey((OffsetableDeclarationBase)declaration));       
        }
        
        /* package */ OffsetableDeclarationUID (){
            
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
    /* package */ static final class TypedefUID<T extends CsmTypedef> extends OffsetableDeclarationUID<T> {
        public TypedefUID(T typedef) {
            super(typedef);
//            assert typedef instanceof RegistarableDeclaration;
//            if (!((RegistarableDeclaration)typedef).isRegistered()) {
//                System.err.print("\n\nunregistered declaration'" + typedef.getUniqueName() + "'");
//                new CsmTracer().dumpModel(typedef);
//            }
//            assert ((RegistarableDeclaration)typedef).isRegistered();            
        }
        
        /* package */ TypedefUID() {
            
        }
        
        protected String getToStringPrefix() {
            return "TypedefUID"; // NOI18N
        }
        
    }
    
    /**
     * UID for CsmClassifier
     */
    /* package */ static final class ClassifierUID<T extends CsmOffsetableDeclaration> extends OffsetableDeclarationUID<T> {
        public ClassifierUID(T classifier) {
            super(classifier);
//            assert classifier instanceof RegistarableDeclaration;
//            if (!((RegistarableDeclaration)classifier).isRegistered()) {
//                System.err.print("\n\nunregistered declaration'" + classifier.getUniqueName() + "'");
//                new CsmTracer().dumpModel(classifier);
//            }
//            assert ((RegistarableDeclaration)classifier).isRegistered();
        }
        
        /* package */ ClassifierUID(){
            
        }
        
        protected String getToStringPrefix() {
            return "ClassifierUID"; // NOI18N
        }
        
    }     
    
    /**
     * UID for CsmClassifier with empty getName()
     */    
    private static final class UnnamedClassifierUID<T extends CsmClassifier> extends ObjectBasedUID {
        public UnnamedClassifierUID(T classifier) {
            super(classifier);
        }

        public String toString() {
            String retValue = "<UNNAMED CLASSIFIER UID> " + super.toString(); // NOI18N
            return retValue;
        } 
    }
}
