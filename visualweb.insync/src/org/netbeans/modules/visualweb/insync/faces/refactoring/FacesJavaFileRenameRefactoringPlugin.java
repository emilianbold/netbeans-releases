/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.faces.ElBindingScanner;
import org.netbeans.modules.visualweb.insync.faces.ElementAttrValueScanner;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;

public class FacesJavaFileRenameRefactoringPlugin extends FacesRefactoringPlugin {
    
    public FacesJavaFileRenameRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
    }
    
    private RenameRefactoring getRenameRefactoring() {
        return (RenameRefactoring) getRefactoring();
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        // Don't do the checking if this is a delegated refactoring
        if (isDelegatedRefactoring(getRefactoring())) {
            return null;
        }
        
        FileObject fileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (fileObject != null) {
            if (FacesRefactoringUtils.isJavaFileObjectOfInterest(fileObject)) {
                String newName = getRenameRefactoring().getNewName();
                if (newName == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                } else {                   
                    if (FacesUnit.isImplicitBeanName(newName)) {
                        return new Problem(true,  NbBundle.getMessage(FacesJavaFileRenameRefactoringPlugin.class, "ERR_ReservedIdentifier", newName)); //NOI18N
                    }
                    if (!Character.isUpperCase(newName.charAt(0))) {
                        return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "WRN_NewNameDoesNotStartWithUppercaseLetter")); // NOI18N
                    }
                    // Is this a VW page backing bean
                    FileObject jspFileObject = FacesModel.getJspForJava(fileObject);
                    if (jspFileObject != null) {
                        FileObject siblingWithNewName = jspFileObject.getParent().getFileObject(newName, jspFileObject.getExt());
                        if (siblingWithNewName != null && siblingWithNewName.isValid()) {
                            return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameJspFileAlreadyExists")); // NOI18N
                        }
                    }
                }                
            }
        }

        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements){
        FileObject refactoringSourcefileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (refactoringSourcefileObject != null) {
            Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
            if (FacesRefactoringUtils.isJavaFileObjectOfInterest(refactoringSourcefileObject)) {                
                String newName = getRenameRefactoring().getNewName();
                
                if (newName == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                }
                
                String oldName = refactoringSourcefileObject.getName();
                FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
                if (pageBeanRoot == null) {
                    // TODO
                }
                
                FileObject fileObjectParent = refactoringSourcefileObject.getParent();
                
                // Compute relative path of parent folder to bean root.
                String parentsRelativePathWithDollars = FileUtil.getRelativePath(pageBeanRoot, fileObjectParent).replace('/', '$'); // NOI18N
                
                String oldBeanName = parentsRelativePathWithDollars 
                    + (parentsRelativePathWithDollars.length() == 0 ? "" : "$") // NOI18N
                    + oldName;
                String newBeanName = parentsRelativePathWithDollars
                    + (parentsRelativePathWithDollars.length() == 0 ? "" : "$") // NOI18N
                    + newName;
                // Fix up
                newBeanName = FacesUnit.fixPossiblyImplicitBeanName(newBeanName);
                
                String oldValueBindingPrefix = "#{" + oldBeanName + ".";
                String newValueBindingPrefix = "#{" + newBeanName + ".";
                
                FacesModelSet facesModelSet = FacesModelSet.getInstance(refactoringSourcefileObject);
                
                boolean isRefactoringSourcePageBean = false;
                String refactoringSourceFQN = null;
                Model refactoringSourceModel = facesModelSet.getModel(refactoringSourcefileObject);
                if (refactoringSourceModel instanceof FacesModel) {
                    FacesModel refactoringSourceFacesModel = (FacesModel) refactoringSourceModel;
                    isRefactoringSourcePageBean = refactoringSourceFacesModel.isPageBean();
                    if (!refactoringSourceFacesModel.isBusted()) {
                        JavaUnit javaUnit = refactoringSourceFacesModel.getJavaUnit();
                        if (javaUnit != null) {
                            refactoringSourceFQN = javaUnit.getJavaClass().getName();
                        }
                    }
                }
                
                Model[] models = facesModelSet.getModels();
                // Rename the bean name in the value binding expressions
                for (int i=0; i < models.length; i++) {
                    Model iModel = models[i];
                    if (iModel instanceof FacesModel) {
                        FacesModel iFacesModel = (FacesModel) iModel;
                        if (!iFacesModel.isBusted()) {
                            FileObject javaFileObject = iFacesModel.getJavaFile();
                            if (javaFileObject != null) {
                                try {
                                    // Replace #{oldBeanName. ...} with #{newBeanName....} in string literals in all java files of FacesModels (JavaUnits)
                                    JavaSource javaSource = JavaSource.forFileObject(javaFileObject);
                                    ModificationResult modificationResult = javaSource.runModificationTask(
                                            new StringLiteralTask(oldValueBindingPrefix, newValueBindingPrefix, StringLiteralTransformer.MatchKind.PREFIX)
                                            );
                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
                                            String old = dif.getOldText();
                                            if (old!=null) {
                                                //TODO: workaround
                                                //generator issue?
                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
                                            }
                                        }
                                    }
                                } catch (IOException ex) {
                                    throw (RuntimeException) new RuntimeException().initCause(ex);
                                }
                            }
                            
                            JavaUnit javaUnit = iFacesModel.getJavaUnit();
                            if (refactoringSourceFQN != null && javaUnit != null && !isRefactoringSourcePageBean) {
                                final String finalRefactoringSourceFQN = refactoringSourceFQN;
                                final JavaClass javaClass = javaUnit.getJavaClass();                                
                                if (javaClass != null) {
                                    Method method = javaClass.getMethod("get" + oldBeanName, new Class[0]);
                                    if (method != null) {
                                        final ElementHandle<ExecutableElement> elementHandle = method.getElementHandle();
                                        if (elementHandle != null) {
                                            FileObject facesJavaFileObject = iFacesModel.getJavaFile();
                                            JavaSource javaSource = JavaSource.forFileObject(facesJavaFileObject);
                                            if (javaSource != null) {
                                                // Rename accessor methods
                                                final RenameRefactoring[] methodRenameRefactoring = new RenameRefactoring[1];
                                                try {
                                                    javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
                                                        public void cancel() {
                                                        }

                                                        public void run(CompilationController compilationController)
                                                            throws Exception {
                                                            compilationController.toPhase(Phase.RESOLVED);
                                                            Element element = elementHandle.resolve(compilationController);
                                                            if (element != null && element.getKind() ==  ElementKind.METHOD) {
                                                                ExecutableElement executableElement = (ExecutableElement) element;
                                                                TypeMirror typeMirror = executableElement.getReturnType();
                                                                if (typeMirror != null && typeMirror.getKind() == TypeKind.DECLARED) {
                                                                    DeclaredType declaredType = (DeclaredType) typeMirror;
                                                                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                                                                    
                                                                    if (typeElement.getQualifiedName().toString().equals(finalRefactoringSourceFQN)) {
                                                                        TreePathHandle treePathHandle = TreePathHandle.create(compilationController.getTrees().getPath(element), compilationController);
                                                                        methodRenameRefactoring[0] = new RenameRefactoring(Lookups.fixed(treePathHandle));
                                                                        methodRenameRefactoring[0].getContext().add(compilationController);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }, true);                                                    
                                                    
                                                    if (methodRenameRefactoring[0] != null) {                                                        
                                                        methodRenameRefactoring[0].setNewName("get" + newBeanName);
                                                        methodRenameRefactoring[0].setSearchInComments(getRenameRefactoring().isSearchInComments());
                                                        
                                                        // Flag delegation
                                                        methodRenameRefactoring[0].getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
                                                        
                                                        Problem problem = methodRenameRefactoring[0].prepare(refactoringElements.getSession());
                                                        if (problem != null) {
                                                            return problem;
                                                        }
                                                    }
                                                } catch (IOException ex) {
                                                }
                                                
                                                try {
                                                    ModificationResult modificationResult = javaSource.runModificationTask(
                                                            new StringLiteralTask(oldBeanName, newBeanName, StringLiteralTransformer.MatchKind.EXACT)
                                                            );
                                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
                                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
                                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
                                                            String old = dif.getOldText();
                                                            if (old!=null) {
                                                                //TODO: workaround
                                                                //generator issue?
                                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
                                                            }
                                                        }
                                                    }
                                                } catch (IOException ex) {
                                                    throw (RuntimeException) new RuntimeException().initCause(ex);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Replace #{oldBeanName. ...} with #{newBeanName....} in VBEs in all jsp files of FacesModels (MarkupUnits) 
                for (int i=0; i < models.length; i++) {
                    Model iModel = models[i];
                    if (iModel instanceof FacesModel) {
                        FacesModel iFacesModel = (FacesModel) iModel;
                        if (!iFacesModel.isBusted()) {
                            MarkupUnit markupUnit = iFacesModel.getMarkupUnit();
                            if (markupUnit == null)
                                continue;
                            Document markupDocument = markupUnit.getSourceDom();
                            ElBindingScanner scanner = new ElBindingScanner();
                            int referenceCount = scanner.getReferenceCount(markupDocument, oldBeanName);
                            if (referenceCount > 0) {
                                refactoringElements.add(getRefactoring(), new RenameElExpressionReferencesRefactoringElement(iFacesModel.getFile(), markupUnit, oldBeanName, newBeanName));
                            }
                        }
                    }
                }
                
                if (isRefactoringSourcePageBean && refactoringSourceModel instanceof FacesModel) {
                	MarkupUnit markupUnit = ((FacesModel)refactoringSourceModel).getMarkupUnit();
                    if (markupUnit != null) {
                    	ElementAttrValueScanner elementScanner = new ElementAttrValueScanner();
                    	Document markupDocument = markupUnit.getSourceDom();
                    	if (elementScanner.getReferenceCount(markupDocument, "f:subview", "id", refactoringSourcefileObject.getName()) > 0) {                    		
                    		refactoringElements.add(getRefactoring(),
                    				new RenameSubViewRefactoringElement(markupUnit.getFileObject(), markupUnit, refactoringSourcefileObject.getName(), newName));
                    	}
                    }
                }                                
                
                // The renaming of bean class name is handled by NetBeans Web project JSF refcatoring.
                // Rename the managed bean name. 
                WebModule webModule = WebModule.getWebModule(refactoringSourcefileObject);
                if (webModule != null){
                    List <FacesRefactoringUtils.OccurrenceItem> items = FacesRefactoringUtils.getAllBeanNameOccurrences(webModule, oldBeanName, newBeanName);
                    for (FacesRefactoringUtils.OccurrenceItem item : items) {
                        refactoringElements.add(getRefactoring(), new JSFConfigRenameBeanNameElement(item));
                    }
                }
                
                if (!isDelegatedRefactoring(getRefactoring())) {
                    FileObject jspFileObject = FacesModel.getJspForJava(refactoringSourcefileObject);
                    if (jspFileObject != null) {
                        RenameRefactoring jspRenameRefactoring = new RenameRefactoring(Lookups.singleton(jspFileObject));
                        jspRenameRefactoring.setNewName(newName);
                        jspRenameRefactoring.setSearchInComments(getRenameRefactoring().isSearchInComments());
                        
                        // Flag delegation
                        jspRenameRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
  
                        Problem problem = jspRenameRefactoring.prepare(refactoringElements.getSession());
                        if (problem != null) {
                            return problem;
                        }
                    }
                }
            }
        }

        return null;
    }
    
    private class StringLiteralTask implements CancellableTask<WorkingCopy> {
        
        private final String oldString;
        private final String newString;
        private final StringLiteralTransformer.MatchKind matchKind;
        
        public StringLiteralTask(String oldString, String newString, StringLiteralTransformer.MatchKind matchKind) {
            super();
            this.oldString = oldString;
            this.newString = newString;
            this.matchKind = matchKind;
        }
        
        public void cancel() {
        }
        
        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
           
            StringLiteralTransformer stringLiteralTransformer = new StringLiteralTransformer(compiler, oldString, newString, matchKind);
            stringLiteralTransformer.scan(compiler.getCompilationUnit(), null);
            
            for (TreePath tree : stringLiteralTransformer.getUsages()) {
                ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
            }
            fireProgressListenerStep();
        }
    }
}
