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
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
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
public class JSFWhereUsedPlugin implements RefactoringPlugin{
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    private static final Logger LOGGER = Logger.getLogger(JSFWhereUsedPlugin.class.getName());
    
    private final WhereUsedQuery refactoring;
    private TreePathHandle treePathHandle = null;
    
    /** Creates a new instance of JSFWhereUsedPlugin */
    public JSFWhereUsedPlugin(WhereUsedQuery refactoring) {
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
            //TODO: should be improved.
            Object element = refactoring.getRefactoringSource().lookup(Object.class);
            LOGGER.fine("Prepare refactoring: " + element);                 // NOI18N
            
            if (element instanceof TreePathHandle) {
                treePathHandle = (TreePathHandle)element;
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
                                LOGGER.log(Level.WARNING, "Exception in JSFWhereUsedPlugin", ex);
                            }
                        }
                        info = refactoring.getContext().lookup(CompilationInfo.class);
                        Element resElement = treePathHandle.resolveElement(info);
                        TypeElement type = (TypeElement) resElement;
                        String fqnc = type.getQualifiedName().toString();
                        List <Occurrences.OccurrenceItem> items = Occurrences.getAllOccurrences(webModule, fqnc,"");
                        for (Occurrences.OccurrenceItem item : items) {
                            refactoringElements.add(refactoring, new JSFWhereUsedElement(item));
                        }
                    }
                }
            }
            semafor.set(null);
        }
        return null;
    }
    
    public class JSFWhereUsedElement extends SimpleRefactoringElementImpl  {
        
        private final Occurrences.OccurrenceItem item;
        
        public JSFWhereUsedElement(Occurrences.OccurrenceItem item){
            this.item = item;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public String getDisplayText() {
            return item.getWhereUsedMessage();
        }
        
        public void performChange() {
        }
        
        public Element getJavaElement() {
            return null;
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
