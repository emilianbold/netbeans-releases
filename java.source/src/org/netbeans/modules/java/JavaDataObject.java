/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.modules.java.source.ActivatedDocumentListener;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.windows.CloneableOpenSupport;

public final class JavaDataObject extends MultiDataObject {
    
    private JavaEditorSupport jes;
    
    public JavaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                createJavaEditorSupport().saveAs( folder, fileName );
            }
        });
        getCookieSet().add(JavaEditorSupport.class, new CookieSet.Factory() {
            public <T extends Cookie> T createCookie(Class<T> klass) {
                return klass.cast(createJavaEditorSupport ());
            }
        });
    }

    public @Override Node createNodeDelegate() {
        return JavaDataSupport.createJavaNode(getPrimaryFile());
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    protected DataObject handleCopyRename(DataFolder df, String name, String ext) throws IOException {
        FileObject fo = getPrimaryEntry ().copyRename (df.getPrimaryFile (), name, ext);
        DataObject dob = DataObject.find( fo );
        //TODO invoke refactoring here (if needed)
        return dob;
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
                    ActivatedDocumentListener.removeFromModified(getDataObject().getPrimaryFile());
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
                ActivatedDocumentListener.addToModified(getDataObject().getPrimaryFile());
            }
            
            public void removeSaveCookie() {
                JavaDataObject javaData = (JavaDataObject) this.getDataObject();
                if (javaData.getCookie(SaveCookie.class) != null) {
                    javaData.getCookieSet().remove(this.saveCookie);
                    javaData.setModified(false);
                }

                ActivatedDocumentListener.addToModified(getDataObject().getPrimaryFile());
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

        @Override
        protected StyledDocument createStyledDocument(EditorKit kit) {
            final StyledDocument document = super.createStyledDocument(kit);
            document.addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    updated();
                }
                public void removeUpdate(DocumentEvent e) {
                    updated();
                }
                private void updated() {
                    Object sourceProperty = document.getProperty(Document.StreamDescriptionProperty);

                    if (!(sourceProperty instanceof DataObject))
                        return ;

                    DataObject source = (DataObject) sourceProperty;

                    if (source == null)
                        return ;

                    FileObject file = source.getPrimaryFile();

                    if (file != null && DataObject.getRegistry().getModifiedSet().contains(source)) {
                        ActivatedDocumentListener.addToModified(file);
                    }
                }
                public void changedUpdate(DocumentEvent e) {}
            });
            return document;
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

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
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
        };
        javaSource.runModificationTask(task).commit();
    }
}
