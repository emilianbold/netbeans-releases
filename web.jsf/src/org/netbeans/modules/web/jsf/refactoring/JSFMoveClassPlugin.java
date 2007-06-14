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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.net.URL;
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
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
/**
 *
 * @author Petr Pisl
 */
public class JSFMoveClassPlugin implements RefactoringPlugin{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private TreePathHandle treePathHandle = null;
    private static final Logger LOGGER = Logger.getLogger(JSFMoveClassPlugin.class.getName());
    
    private final  AbstractRefactoring refactoring;
    
    public JSFMoveClassPlugin(MoveRefactoring refactoring) {
        this.refactoring = refactoring;
    }
    
    public Problem preCheck() {
        return null;
    }
    
    public Problem checkParameters() {
        return null;
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    public void cancelRequest() {
        
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() == null) {
            semafor.set(new Object());
            Object element = refactoring.getRefactoringSource().lookup(Object.class);
            if (element instanceof FileObject){
                JavaSource source = JavaSource.forFileObject((FileObject) element);
                if (source != null){
                    try {
                        source.runUserActionTask(new AbstractTask<CompilationController>() {
                            @Override
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
                        LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", ex);  //NOI18N
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", ex);  //NOI18N
                    }
                }
            } else
                if (element instanceof TreePathHandle) {
                    treePathHandle = (TreePathHandle)element;
                }
            if (treePathHandle != null && treePathHandle.getKind() == Kind.CLASS){
                WebModule webModule = WebModule.getWebModule(treePathHandle.getFileObject());
                if (webModule != null){
                    CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
                    Element resElement = treePathHandle.resolveElement(info);
                    TypeElement type = (TypeElement) resElement;
                    String oldFQN = type.getQualifiedName().toString();
                    String newFQN = JSFRefactoringUtils.getPackageName(((MoveRefactoring)refactoring).getTarget().lookup(URL.class))
                            + "." +type.getSimpleName();
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(webModule, oldFQN, newFQN);
                    
                    Modifications modification = new Modifications();
                    for (Occurrences.OccurrenceItem item : items) {
                        Modifications.Difference difference = new Modifications.Difference(
                                Modifications.Difference.Kind.CHANGE, item.getChangePosition().getBegin(),
                                item.getChangePosition().getEnd(), item.getOldValue(), item.getNewValue(), item.getRenamePackageMessage());
                        modification.addDifference(item.getFacesConfig(), difference);
                        refactoringElements.add(refactoring, new DiffElement.ChangeFQCNElement(difference, item, modification));
                    }
                }
            }
        }
        return null;
    }
    
    
    
}
