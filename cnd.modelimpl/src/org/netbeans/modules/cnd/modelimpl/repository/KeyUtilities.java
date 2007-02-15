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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
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
    
    public static Key createOffsetableDeclarationKey(OffsetableDeclarationBase obj) {
        assert obj != null;
        return new OffsetableDeclarationKey(obj);
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    /*package*/ static final class FileKey extends TwoStringsBasedKey {
        public FileKey(CsmFile file) {
            super(getProjectName(file), file.getAbsolutePath());
        }
        
        /*package*/ FileKey () {
            
        }

        private static String getProjectName(CsmFile file) {
            assert (file != null);
            CsmProject prj= file.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        public String toString() {
            return "FileKey (" + this.getStringKey() + ", " + this.getSecondKey() + ")"; // NOI18N
        }

        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
    }

    /*package*/ static final class NamespaceKey extends TwoStringsBasedKey {
        public NamespaceKey(CsmNamespace ns) {
            super(getProjectName(ns), ns.getQualifiedName());
        }
        
        /*package*/ NamespaceKey() {
            
        }

        private static String getProjectName(CsmNamespace ns) {
            CsmProject prj= ns.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        public String toString() {
            return "NSKey " + getSecondKey() + " of project " + getStringKey(); // NOI18N
        }
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
        
    }

    /*package*/ static final class ProjectKey extends StringBasedKey {
        public ProjectKey(CsmProject project) {
            super(project.getName());
        }
        
        /*package*/ ProjectKey () {
            
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
   
    private final static class OffsetableDeclarationKey extends TwoStringsBasedKey {
        private /*final */ int startOffset;
        private /*final */ int endOffset;
        private /*final */ String kind;
        private /*final */ String name;
        
        public OffsetableDeclarationKey(OffsetableDeclarationBase obj) {
            super(getProjectName(obj), getFileName(obj));
            this.startOffset = obj.getStartOffset();
            this.endOffset = obj.getEndOffset();
            this.kind = obj.getKind().toString();
            // we use name, because all other (FQN and UniqueName) could change
            // and name is fixed value
            this.name = obj.getName();
        }
        
        /*package*/ OffsetableDeclarationKey() {
            
        }
        
        public void write (DataOutput aStream) throws IOException, IllegalArgumentException {
            super.write(aStream);
            aStream.writeInt(startOffset);
            aStream.writeInt(endOffset);
            aStream.writeUTF(kind);
            aStream.writeUTF(name);
        }
        
        public void read (DataInput aStream) throws IOException, IllegalArgumentException {
            super.read(aStream);
            startOffset = aStream.readInt();
            endOffset = aStream.readInt();
            kind = aStream.readUTF();
            name = aStream.readUTF();
        }        
        
        private static String getProjectName(OffsetableDeclarationBase obj) {
            CsmFile file= obj.getContainingFile();
            assert (file != null);
            CsmProject prj= file.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        private static String getFileName(OffsetableDeclarationBase obj) {
            CsmFile file= obj.getContainingFile();
            assert (file != null);
            return file == null ? "<No File Name>" : file.getAbsolutePath();  // NOI18N
        }
        
        public String toString() {
            return "OffsDeclKey: " + name + " [" + kind + ":" + startOffset + "-" + endOffset + "] {" + /*file*/getSecondKey() + "; " + /*project */getStringKey() + "}"; // NOI18N
        }        
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }        

        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            OffsetableDeclarationKey other = (OffsetableDeclarationKey)obj;
            return  this.kind.equals(other.kind) &&
                    this.startOffset == other.startOffset && 
                    this.endOffset == other.endOffset &&                    
                    this.name.equals(other.name);
        }

        public int hashCode() {
            int retValue;
            
            retValue = 17*super.hashCode() + kind.hashCode();
            retValue = 17*retValue + startOffset;
            retValue = 17*retValue + endOffset;
            retValue = 17*retValue + name.hashCode();
            return retValue;
        }
    }
    
    private abstract static class TwoStringsBasedKey extends StringBasedKey {
        private /*final */ String secondKey;
        public TwoStringsBasedKey(String firstKey, String secondKey) {
            super(firstKey);
            this.secondKey = secondKey;
        }
        
        /*package*/ TwoStringsBasedKey() {
            
        }
        
        public void write (DataOutput aStream) throws IOException, IllegalArgumentException {
            super.write(aStream);
            aStream.writeUTF(secondKey);
        }
        
        public void read (DataInput aStream) throws IOException, IllegalArgumentException {
            super.read(aStream);
            secondKey = aStream.readUTF();
        }
        
        public int hashCode() {
            int key = super.hashCode();
            key = 17*key + secondKey.hashCode();
            return key;
        }

        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            TwoStringsBasedKey other = (TwoStringsBasedKey)obj;
            
            return this.secondKey.equals(other.secondKey);
        }
        
        protected String getSecondKey() {
            return this.secondKey;
        }        
    }
    
    
    private static abstract class StringBasedKey extends AbstractKey {
        private /*final */ String key;
        
        public StringBasedKey(String key) {
            assert key != null;
            this.key = key;
        }
        
        /*package*/ StringBasedKey() {
           
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

        protected String getStringKey() {
            return this.key;
        }

        public int getDepth() {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public String getAt(int level) {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }      
        
        public int getSecondaryDepth() {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
        public int getSecondaryAt(int level) {
             throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
        
        public void write (DataOutput aStream) throws IOException, IllegalArgumentException {
            aStream.writeUTF(key);
        }
        
        public void read (DataInput aStream) throws IOException, IllegalArgumentException {
            key = aStream.readUTF();
        }

    }        
    
    /*package*/ static abstract class AbstractKey implements Key {
        /** 
         * must be implemented in child
         */
        public abstract void write (DataOutput aStream)  throws IOException, IllegalArgumentException ;
        
        /** 
         * must be implemented in child
         */
        public abstract void read (DataInput aStream)  throws IOException, IllegalArgumentException ;
        
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
}
