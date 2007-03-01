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
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmObjectFactory;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

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

    public static Key createMacroKey(CsmMacro macro) {
        assert macro != null;
        return new MacroKey(macro);
    }
    
    public static Key createIncludeKey(CsmInclude incl) {
        assert incl != null;
        return new IncludeKey(incl);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    /*package*/ static final class FileKey extends ProjectFileNameBasedKey {
        public FileKey(CsmFile file) {
            super(getProjectName(file), file.getAbsolutePath());
        }
        
        /*package*/ FileKey(DataInput aStream) throws IOException {
            super(aStream);
        }
        
        private static String getProjectName(CsmFile file) {
            assert (file != null);
            CsmProject prj= file.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        public String toString() {
            return "FileKey (" + getProjectName() + ", " + getFileName() + ")"; // NOI18N
        }
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }

        public int getSecondaryDepth() {
            return 1;
        }
        
        public int getSecondaryAt(int level) {
            assert level == 0;
            return KeyObjectFactory.KEY_FILE_KEY;
        }  
    }
    
    /*package*/ static final class NamespaceKey extends ProjectNameBasedKey {
        private final String fqn;
        public NamespaceKey(CsmNamespace ns) {
            super(getProjectName(ns));
            this.fqn = ns.getQualifiedName();
        }
        
        private static String getProjectName(CsmNamespace ns) {
            CsmProject prj= ns.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        public String toString() {
            return "NSKey " + fqn + " of project " + getProjectName(); // NOI18N
        }
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
        
        public int hashCode() {
            int key = super.hashCode();
            key = 17*key + fqn.hashCode();
            return key;
        }
        
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            NamespaceKey other = (NamespaceKey)obj;
            return this.fqn.equals(other.fqn);
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
            assert fqn != null;
            aStream.writeUTF(fqn);
        }   
        
        /*package*/ NamespaceKey(DataInput aStream) throws IOException {
            super(aStream);
            fqn = QualifiedNameCache.getString(aStream.readUTF());
            assert fqn != null;
        }      
        
        public int getDepth() {
            return super.getDepth() + 1;
        }
        
        public String getAt(int level) {
            switch (level) {
                case 0:
                    return super.getAt(0);
                case 1:
                    return this.fqn;
                default:
                    throw new IllegalArgumentException("not supported level" + level); // NOI18N
            }
        }
        
        public int getSecondaryDepth() {
            return 1;
        }
        
        public int getSecondaryAt(int level) {
            assert level == 0;
            return KeyObjectFactory.KEY_NAMESPACE_KEY;
        }        
    }
    
    /*package*/ static final class ProjectKey extends ProjectNameBasedKey {
        public ProjectKey(CsmProject project) {
            super(project.getName());
        }
        
        /*package*/ ProjectKey(DataInput aStream) throws IOException {
            super(aStream);
        }
        
        public String toString() {
            String retValue;
            
            retValue = super.toString();
            return "ProjectKey " + retValue; // NOI18N
        }
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }
        
        public int getSecondaryDepth() {
            return 1;
        }

        public int getSecondaryAt(int level) {
            assert (level == 0);
            return KeyObjectFactory.KEY_PROJECT_KEY;
        }           
    }
    
    /*package*/ final static class MacroKey extends OffsetableKey {
        
        private MacroKey(CsmMacro obj) {
            super(obj, "Macro", obj.getName()); // NOI18N
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
        }
        
        /*package*/ MacroKey(DataInput aStream) throws IOException {
            super(aStream);
        }
        
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }

        public String toString() {
            String retValue;
            
            retValue = "MacroKey: " + super.toString(); // NOI18N
            return retValue;
        }
        
        public int getSecondaryDepth() {
            return super.getSecondaryDepth() + 1;
        }

        public int getSecondaryAt(int level) {
            if (level == 0) {
                return KeyObjectFactory.KEY_MACRO_KEY;
            } else {
                return super.getSecondaryAt(level - 1);
            }
        }         
    }
    
    /*package*/ final static class IncludeKey extends OffsetableKey {
        
        private IncludeKey(CsmInclude obj) {
            super(obj, "Include", obj.getIncludeName()); // NOI18N
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
        }
        
        /*package*/ IncludeKey(DataInput aStream) throws IOException {
            super(aStream);
        }
        
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }

        public String toString() {
            String retValue;
            
            retValue = "InclKey: " + super.toString(); // NOI18N
            return retValue;
        }
        
        public int getSecondaryDepth() {
            return super.getSecondaryDepth() + 1;
        }

        public int getSecondaryAt(int level) {
            if (level == 0) {
                return KeyObjectFactory.KEY_INCLUDE_KEY;
            } else {
                return super.getSecondaryAt(level - 1);
            }
        }        
    }
    
    /*package*/ final static class OffsetableDeclarationKey extends OffsetableKey {
        
        private OffsetableDeclarationKey(OffsetableDeclarationBase obj) {
            super(obj, obj.getKind().toString(), obj.getName());
            // we use name, because all other (FQN and UniqueName) could change
            // and name is fixed value
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
        }
        
        /*package*/ OffsetableDeclarationKey(DataInput aStream) throws IOException {
            super(aStream);
        }
        
        
        public PersistentFactory getPersistentFactory() {
            return CsmObjectFactory.instance();
        }

        public String toString() {
            String retValue;
            
            retValue = "OffsDeclKey: " + super.toString(); // NOI18N
            return retValue;
        }

        public int getSecondaryDepth() {
            return super.getSecondaryDepth() + 1;
        }

        public int getSecondaryAt(int level) {
            if (level == 0) {
                return KeyObjectFactory.KEY_DECLARATION_KEY;
            } else {
                return super.getSecondaryAt(level - 1);
            }
        }
    }
    
    private abstract static class OffsetableKey extends ProjectFileNameBasedKey implements Comparable {
        private final int startOffset;
        private final int endOffset;
        
        private final String kind;
        private final String name;
        
        protected OffsetableKey(CsmOffsetable obj, String kind, String name) {
            super(obj.getContainingFile());
            this.startOffset = obj.getStartOffset();
            this.endOffset = obj.getEndOffset();
            this.kind = kind;
            this.name = name;
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
            aStream.writeInt(this.startOffset);
            aStream.writeInt(this.endOffset);
            assert this.kind != null;
            aStream.writeUTF(this.kind);
            assert this.name != null;
            aStream.writeUTF(this.name);            
        }
        
        protected OffsetableKey(DataInput aStream) throws IOException {
            super(aStream);
            this.startOffset = aStream.readInt();
            this.endOffset = aStream.readInt();
            this.kind = TextCache.getString(aStream.readUTF());
            assert this.kind != null;
            this.name = TextCache.getString(aStream.readUTF());
            assert this.name != null;
        }
        
        public String toString() {
            return name + "[" + kind + " " + startOffset + "-" + endOffset + "] {" + getFileName() + "; " + getProjectName() + "}"; // NOI18N
        }
        
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            OffsetableKey other = (OffsetableKey)obj;
            return  this.startOffset == other.startOffset &&
                    this.endOffset == other.endOffset &&
                    this.kind.equals(other.kind) &&                    
                    this.name.equals(other.name);
        }
        
        public int hashCode() {
            int retValue;
            
            retValue = 17*super.hashCode() + name.hashCode();
            retValue = 17*retValue + kind.hashCode();            
            retValue = 17*super.hashCode() + startOffset;
            retValue = 17*retValue + endOffset;
            return retValue;
        }

        public int compareTo(Object o) {
            if (this == o) {
                return 0;
            }
            OffsetableKey other = (OffsetableKey)o; 
            assert (this.kind.equals(other.kind));
            assert (this.getFileName().equals(other.getFileName()));
            assert (this.getProjectName().equals(other.getProjectName()));
            int ofs1 = this.startOffset;
            int ofs2 = other.startOffset;
            if (ofs1 == ofs2) {
                return 0;
            } else {
                return (ofs1 - ofs2);
            }
        }
        
        public int getDepth() {
            return super.getDepth() + 2;
        }
        
        public String getAt(int level) {
            switch (level) {
                case 0:
                case 1:
                    return super.getAt(level);
                case 2:
                    return this.kind;
                case 3:
                    return this.name;
                default:
                    throw new IllegalArgumentException("not supported level" + level); // NOI18N
            }
        }
        
        public int getSecondaryDepth() {
            return 2;
        }
        
        public int getSecondaryAt(int level) {
            switch (level) {
                case 0:
                    return this.startOffset;
                case 1:
                    return this.endOffset;
                default:
                    throw new IllegalArgumentException("not supported level" + level); // NOI18N                    
            }
        }          
    }
    
    private abstract static class ProjectFileNameBasedKey extends ProjectNameBasedKey {
        private final String fileName;
        protected ProjectFileNameBasedKey(String prjName, String fileName) {
            super(prjName);
            assert fileName != null;
            this.fileName = fileName;
        }
        
        protected ProjectFileNameBasedKey(CsmFile file) {
            this(getProjectName(file), file.getAbsolutePath());
        }
        
        private static String getProjectName(CsmFile file) {
            assert (file != null);
            CsmProject prj= file.getProject();
            assert (prj != null);
            return prj == null ? "<No Project Name>" : prj.getName();  // NOI18N
        }
        
        public void write(DataOutput aStream) throws IOException {
            super.write(aStream);
            assert this.fileName != null;
            aStream.writeUTF(fileName);
        }      
        
        protected ProjectFileNameBasedKey(DataInput aStream) throws IOException {
            super(aStream);
            this.fileName = FilePathCache.getString(aStream.readUTF());
            assert this.fileName != null;
        }  
        
        public int hashCode() {
            int key = super.hashCode();
            key = 17*key + fileName.hashCode();
            return key;
        }
        
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            ProjectFileNameBasedKey other = (ProjectFileNameBasedKey)obj;
            
            return this.fileName.equals(other.fileName);
        }
        
        protected String getFileName() {
            return this.fileName;
        }
        
        public int getDepth() {
            return super.getDepth() + 1;
        }
        
        public String getAt(int level) {
            switch (level) {
                case 0:
                    return super.getAt(0);
                case 1:
                    return this.fileName;
                default:
                    throw new IllegalArgumentException("not supported level" + level); // NOI18N
            }
        }       
    }
    
    
    private static abstract class ProjectNameBasedKey extends AbstractKey {
        private final String projectName;
        
        protected ProjectNameBasedKey(String project) {
            assert project != null;
            this.projectName = project;
        }
        
        public String toString() {
            return projectName;
        }
        
        public int hashCode() {
            return projectName.hashCode();
        }
        
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            ProjectNameBasedKey other = (ProjectNameBasedKey)obj;
            
            return this.projectName.equals(other.projectName);
        }
        
        protected String getProjectName() {
            return this.projectName;
        }
        
        public void write(DataOutput aStream) throws IOException {
            assert this.projectName != null;
            aStream.writeUTF(this.projectName);
        }
        
        protected ProjectNameBasedKey(DataInput aStream) throws IOException {
            this.projectName = ProjectNameCache.getString(aStream.readUTF());
            assert this.projectName != null;
        }  
        
        public int getDepth() {
            return 1;
        }
        
        public String getAt(int level) {
            return this.projectName;
        }
    }
    
    // nave to be public or UID factory does not work
    private static abstract class AbstractKey implements Key, SelfPersistent {
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
        
        abstract public int getDepth();
        
        abstract public String getAt(int level);
        
        abstract public int getSecondaryDepth();
        
        abstract public int getSecondaryAt(int level);
    }  
}
