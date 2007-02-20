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

package org.netbeans.modules.web.jsf.refactoring;


import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 *
 * @author Petr Pisl
 */

//TODO need to be handled the rename packages for manged beans
public class JSFRenamePlugin implements RefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    private TreePathHandle treePathHandle = null;
    
    private static final Logger LOGGER = Logger.getLogger(JSFRenamePlugin.class.getName());
    
    private final RenameRefactoring refactoring;
    
    /** Creates a new instance of WicketRenameRefactoringPlugin */
    public JSFRenamePlugin(RenameRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        LOGGER.fine("preCheck() called.");
        return null;
    }
    
    public Problem checkParameters() {
        LOGGER.fine("checkParameters() called.");
        return null;
    }
    
    public Problem fastCheckParameters() {
        LOGGER.fine("fastCheckParameters() called.");
        return null;
    }
    
    public void cancelRequest() {
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            //TODO: Lookup stuff should be improved
            Object element = refactoring.getRefactoringSource().lookup(Object.class);
            LOGGER.fine("Prepare refactoring: " + element);                 // NOI18N
            
            if (element instanceof FileObject){
                JavaSource source = JavaSource.forFileObject((FileObject) element);
                // Can be null, if it is just folder. Should be handled as well and found
                // whether is not a part of a package name. 
                if (source != null){
                    try {
                        source.runUserActionTask(new AbstractTask<CompilationController>() {
                            public void cancel() {
                            }

                            public void run(CompilationController co) throws Exception {
                                co.toPhase(JavaSource.Phase.RESOLVED);
                                CompilationUnitTree cut = co.getCompilationUnit();
                                treePathHandle = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                                refactoring.getContext().add(co);
                            }
                        }, false);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Exception in JSFRenamePlugin", ex);
                    }
                }
            }
            else 
                if (element instanceof TreePathHandle)
                    treePathHandle = (TreePathHandle)element;
            
            if (treePathHandle != null && treePathHandle.getKind() == Kind.CLASS){
                WebModule webModule = WebModule.getWebModule(treePathHandle.getFileObject());
                if (webModule != null){
                    CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
                    Element resElement = treePathHandle.resolveElement(info);
                    TypeElement type = (TypeElement) resElement;
                    String oldFQN = type.getQualifiedName().toString();
                    String newFQN = renameClass(oldFQN, refactoring.getNewName());
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(webModule, oldFQN, newFQN);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFConfigRenameClassElement(item));
                    }
                }
            }

            semafor.set(null);
        }
        return null;
    }
    
    /**
     * @return true if given str is null or empty.
     */
    private static boolean isEmpty(String str){
        return str == null || "".equals(str.trim());
    }
    
    /**
     * Constructs new name for given class.
     * @param originalFullyQualifiedName old fully qualified name of the class.
     * @param newName new unqualified name of the class.
     * @return new fully qualified name of the class.
     */
    private static String renameClass(String originalFullyQualifiedName, String newName){
        if (isEmpty(originalFullyQualifiedName) || isEmpty(newName)){
            throw new IllegalArgumentException("Old and new name of the class must be given.");
        }
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        if (lastDot <= 0){
            // no package
            return newName;
        }
        return originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    public static class JSFConfigRenameClassElement extends SimpleRefactoringElementImpl {
        private final Occurrences.OccurrenceItem item;
        
        JSFConfigRenameClassElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getRenameMessage();
        }
        
        public void performChange() {
            //JavaMetamodel.getManager().registerExtChange(this);
            LOGGER.fine("JSFConfigRenameClassElement.performChange()");
            item.performRename();
        }
        
        public void undoExternalChange() {
            item.undoRename();
        }
        
        
        
        public FileObject getParentFile() {
            return item.getFacesConfig();
        }
        
        public PositionBounds getPosition() {
            return item.getClassDefinitionPosition();
        }
        
        public Object getComposite() {
            return item.getFacesConfig();
        }
    }
    
}
