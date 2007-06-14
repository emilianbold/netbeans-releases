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

import com.sun.source.tree.Tree.Kind;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author Petr Pisl
 */
public class JSFSafeDeletePlugin implements RefactoringPlugin{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private static final Logger LOGGER = Logger.getLogger(JSFSafeDeletePlugin.class.getName());
    
    private final SafeDeleteRefactoring refactoring;
    
    /** Creates a new instance of JSFWhereUsedPlugin */
    public JSFSafeDeletePlugin(SafeDeleteRefactoring refactoring) {
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
            TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
            if (treePathHandle != null && treePathHandle.getKind() == Kind.CLASS){
                WebModule webModule = WebModule.getWebModule(treePathHandle.getFileObject());
                if (webModule != null){
                    CompilationInfo info = refactoring.getContext().lookup(CompilationInfo.class);
                    if (refactoring.getContext().lookup(CompilationInfo.class) == null){
                        final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
                        JavaSource source = JavaSource.create(cpInfo, new FileObject[]{treePathHandle.getFileObject()});
                        try{
                            source.runUserActionTask(new AbstractTask<CompilationController>() {
                                
                                public void run(CompilationController co) throws Exception {
                                    co.toPhase(JavaSource.Phase.RESOLVED);
                                    refactoring.getContext().add(co);
                                }
                            }, false);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Exception in JSFSafeDeletePlugin", ex); //NOI18NN
                        }
                        info = refactoring.getContext().lookup(CompilationInfo.class);
                    }
                    Element resElement = treePathHandle.resolveElement(info);
                    TypeElement type = (TypeElement) resElement;
                    String fqcn = type.getQualifiedName().toString();
                    List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(webModule, fqcn, null);
                    for (Occurrences.OccurrenceItem item : items) {
                        refactoringElements.add(refactoring, new JSFSafeDeleteClassElement(item));
                    }
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    public static class JSFSafeDeleteClassElement extends SimpleRefactoringElementImplementation {
        private final Occurrences.OccurrenceItem item;
        
        JSFSafeDeleteClassElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getSafeDeleteMessage();
        }
        
        public void performChange() {
            item.performSafeDelete();
        }
        
        public Lookup getLookup() {
            return Lookups.singleton(item.getFacesConfig());
        }
        
        public FileObject getParentFile() {
            return item.getFacesConfig();
        }
        
        public PositionBounds getPosition() {
            return item.getChangePosition();
        }
        
        @Override
        public void undoChange() {
            item.undoSafeDelete();
        }
        
    }
}
