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

package org.netbeans.modules.cnd.modelimpl.repository;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;

/**
 * help methods to create repository keys
 * @author Vladimir Voskresensky
 */
public class KeyUtilities {
    
    /** Creates a new instance of KeyUtils */
    private KeyUtilities() {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // key generators
    
    public static Key createFileKey(CsmFile file) {
        return new FileKey(file);
    }
    
    public static Key createNamespaceKey(CsmNamespace ns) {
        return new NamespaceKey(ns);
    }    

    public static Key createProjectKey(CsmProject project) {
        return new ProjectKey(project);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static final class FileKey extends StringBasedKey {
        public final String prjKey;
        public FileKey(CsmFile file) {
            super(file.getAbsolutePath());
            this.prjKey = file.getProject().getName();
        }

        public String toString() {
            String retValue;
            
            retValue = super.toString();
            return "FileKey (" + prjKey + ", " + retValue + ")"; // NOI18N
        }

        public int hashCode() {
            int retValue;
            
            retValue = super.hashCode();
            retValue = 17*retValue + prjKey.hashCode();
            return retValue;
        }

        public boolean equals(Object obj) {
            boolean retValue;
            
            if (super.equals(obj)) {
                FileKey other = (FileKey)obj;
                return this.prjKey.equals(other.prjKey);
            } else {
                return false;
            }
        }        

        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
    }

    private static final class NamespaceKey extends StringBasedKey {
        private String project;
        public NamespaceKey(CsmNamespace ns) {
            super(ns.getQualifiedName());
        }

        public String toString() {
            String retValue;
            
            retValue = super.toString();
            return "NSKey " + retValue; // NOI18N
        }
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
        
    }

    private static final class ProjectKey extends StringBasedKey {
        public ProjectKey(CsmProject project) {
            super(project.getName());
        }
        
        public String toString() {
            String retValue;
            
            retValue = super.toString();
            return "ProjectKey " + retValue; // NOI18N
        }        

        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
        
    }
   
    private static abstract class StringBasedKey extends AbstractKey {
        private final String key;
        
        public StringBasedKey(String key) {
            assert key != null;
            this.key = key;
        }

        public String toString() {
            return key;
        }

        public int hashCode() {
            return key.hashCode();
        }

        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            StringBasedKey other = (StringBasedKey)obj;
            
            return this.key.equals(other.key);
        }
        

        public int getDepth() {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int getAt(int level) {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }        
    }        
    
    private static abstract class AbstractKey implements Key {
        /** 
         * must be implemented in child
         */
        public abstract String toString();
        
        /** 
         * must be implemented in child
         */
        public abstract int hashCode();
        
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || (this.getClass() != obj.getClass())) {
                return false;
            }
            return true;
        }        
    }    
    
////////////////////////////////////////////////////////////////////////////////
//    public static Key getStringKey(String str) {
//        return new StringKey(str);
//    }
//    
//    public static Key getOffsetableDeclarationKey(OffsetableDeclarationBase obj) {
//        assert obj != null;
//        if (obj.getUniqueName() != null) {
//            return getStringKey(obj.getUniqueName());
//        } else if (obj.getQualifiedName() != null) {
//            return getStringKey(obj.getQualifiedName());
//        } else {
//            return new OffsetableKey(obj);
//        }
//    }
    
//    public static Key getModelKey(ModelImpl model) {
//        return MODEL_KEY;
//    }
//    private static final Key MODEL_KEY = new StringKey("$$ModelKey$$"); // NOI18N
    
//    private static final class StringKey extends StringBasedKey {
//        public StringKey(String str) {
//            super(str);
//        }
//    }
    
//    private static final class OffsetableKey extends AbstractKey {
//        private final String file;
//        private final int offset;
//        
//        public OffsetableKey(CsmOffsetable obj) {
//            assert obj != null;
//            this.file = obj.getContainingFile().getAbsolutePath();
//            this.offset = obj.getStartOffset();
//        }
//
//        public String toString() {
//            return "OffsetableKey (" + file + ", " + offset + ")"; // NOI18N
//        }
//
//        public int hashCode() {
//            int code = file.hashCode();
//            code += code*17 + offset;
//            return code;
//        }
//
//        public boolean equals(Object obj) {
//            if (!super.equals(obj)) {
//                return false;
//            }
//            OffsetableKey other = (OffsetableKey)obj;
//            if (this.offset != other.offset) {
//                return false;
//            } else {
//                return this.file.equals(other.file);
//            }
//        }
//    } 
//        
}
