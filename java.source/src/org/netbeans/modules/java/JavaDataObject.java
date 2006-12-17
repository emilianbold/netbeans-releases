/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.TreeMaker;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;

public final class JavaDataObject extends MultiDataObject {
    
    private JavaEditorSupport jes;
    
    public JavaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    public @Override Node createNodeDelegate() {
        return JavaDataSupport.createJavaNode(getPrimaryFile());
    }

    public @Override <T extends Cookie> T getCookie(Class<T> type) {
        if (type.isAssignableFrom(JavaEditorSupport.class)) {
            return type.cast(createJavaEditorSupport ());
        }
        return super.getCookie(type);
    }
    
    private synchronized JavaEditorSupport createJavaEditorSupport () {
        if (jes == null) {
            jes = new JavaEditorSupport (this);
        }
        return jes;
    }            
    
    public static final class JavaEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {
        
        private static final class Environment extends DataEditorSupport.Env {
            
            private static final long serialVersionUID = -1;
            
            private transient SaveSupport saveCookie = null;
            
            private final class SaveSupport implements SaveCookie {
                public void save() throws java.io.IOException {
                    ((JavaEditorSupport)findCloneableOpenSupport()).saveDocument();
                    getDataObject().setModified(false);
                }
            }
            
            public Environment(JavaDataObject obj) {
                super(obj);
            }
            
            protected FileObject getFile() {
                return this.getDataObject().getPrimaryFile();
            }
            
            protected FileLock takeLock() throws java.io.IOException {
                return ((MultiDataObject)this.getDataObject()).getPrimaryEntry().takeLock();
            }
            
            public @Override CloneableOpenSupport findCloneableOpenSupport() {
                return (CloneableEditorSupport) ((JavaDataObject)this.getDataObject()).getCookie(EditorCookie.class);
            }
            
            
            public void addSaveCookie() {
                JavaDataObject javaData = (JavaDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) == null) {
                    if (this.saveCookie == null)
                        this.saveCookie = new SaveSupport();
                    javaData.getCookieSet().add(this.saveCookie);
                    javaData.setModified(true);
                }
            }
            
            public void removeSaveCookie() {
                JavaDataObject javaData = (JavaDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }
            }
        }
        
        public JavaEditorSupport(JavaDataObject dataObject) {
            super(dataObject, new Environment(dataObject));
            setMIMEType("text/x-java"); // NOI18N
        }
        
        
        protected boolean notifyModified() {
            if (!super.notifyModified())
                return false;
            ((Environment)this.env).addSaveCookie();
            return true;
        }
        
        
        protected @Override void notifyUnmodified() {
            super.notifyUnmodified();
            ((Environment)this.env).removeSaveCookie();
        }

        protected @Override CloneableEditor createCloneableEditor() {
            return new JavaEditor(this);
        }
        
        public @Override boolean close(boolean ask) {
            return super.close(ask);
        }

    }
    
    private static final class JavaEditor extends CloneableEditor {
        
        private static final long serialVersionUID = -1;        
        
        public JavaEditor() {
        }
        
        public JavaEditor(JavaEditorSupport sup) {
            super(sup);
        }
        
    }
    
    /**
     * XXX: Change this when there will be a write model.
     * When there will be a refactoring it shoud be called only in case of handleCreateFromTemplate
     */    
    static void renameFO(final FileObject fileToUpdate, 
            final String packageName, 
            final String newName, 
            final String originalName) throws IOException 
    {
        JavaSource javaSource = JavaSource.forFileObject (fileToUpdate);

        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
                // change the package when file was move to different dir.
                CompilationUnitTree cutCopy = make.CompilationUnit(
                        "".equals(packageName) ? null : make.Identifier(packageName),
                        compilationUnitTree.getImports(),
                        compilationUnitTree.getTypeDecls(),
                        compilationUnitTree.getSourceFile()
                );
                workingCopy.rewrite(compilationUnitTree, cutCopy);
                // go to rename also the top level class too...
                if (originalName != null && !originalName.equals(newName)) {
                    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            ClassTree clazz = (ClassTree) typeDecl;
                            if (originalName.contentEquals(clazz.getSimpleName())) {
                                Tree copy = make.setLabel(typeDecl, newName);
                                workingCopy.rewrite(typeDecl, copy);
                            }
                        }
                    }
                }
            }
                
            public void cancel() {
            }
        };
        javaSource.runModificationTask(task).commit();
    }
}
