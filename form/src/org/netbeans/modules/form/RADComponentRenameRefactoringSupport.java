/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.form;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.modules.form.codestructure.CodeVariable;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Class used to hook form component/variables renaming into refactoring.
 * @author Wade Chandler
 * @version 1.0
 */
public class RADComponentRenameRefactoringSupport {

    private static class MemberVisitor
            extends TreePathScanner<Void, Void>
            implements CancellableTask<CompilationController>{
        
        private CompilationInfo info;
        private String member = null;
        private TreePathHandle handle = null;

        boolean findUsages;
        private Element variableElement;
        private List<Integer> usagesPositions;

        public TreePathHandle getHandle() {
            return handle;
        }
        
        public void setHandle(TreePathHandle handle) {
            this.handle = handle;
        }
        
        public MemberVisitor(String member, boolean findUsages) {
            this.member = member;
            this.findUsages = findUsages;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            if (variableElement == null) {
                // try to find the component's field variable in the class
                List<? extends Tree> members = (List<? extends Tree>) t.getMembers();
                Iterator<? extends Tree> it = members.iterator();
                while(it.hasNext()){
                    Tree tr = it.next();
                    if (tr.getKind() == Tree.Kind.VARIABLE) {
                        Trees trees = info.getTrees();
                        TreePath path = new TreePath(getCurrentPath(), tr);
                        Element el = trees.getElement(path);
                        String sname = el.getSimpleName().toString();
                        if(sname.equals(this.member)){
                            this.handle = TreePathHandle.create(path, info);
                            variableElement = el;
                            if (findUsages) {
                                usagesPositions = new ArrayList<Integer>();
                            }
                        }
                    }
                }
            }
            if (findUsages) {
                super.visitClass(t, v);
            }
            return null;
        }

        @Override
        public Void visitIdentifier(IdentifierTree tree, Void v) {
            if (findUsages) {
                Element el = info.getTrees().getElement(getCurrentPath());
                if (variableElement != null && variableElement.equals(el)) {
                    int pos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                    usagesPositions.add(pos);
                }
            }
            return super.visitIdentifier(tree, v);
        }

        @Override
        public void cancel() {
        }
        
        @Override
        public void run(CompilationController parameter) throws IOException {
            this.info = parameter;
            parameter.toPhase(Phase.RESOLVED);
            this.scan(parameter.getCompilationUnit(), null);
        }
    }

    static void renameComponent(RADComponent component, String newName) {
        int varType = component.getCodeExpression().getVariable().getType();
        if ((varType & CodeVariable.SCOPE_MASK) == CodeVariable.LOCAL) {
            // local variable - no refactoring needed, no renaming out of generated (guarded) code
            FormRefactoringUpdate.renameComponentInCustomCode(component, newName);
            component.setName(newName);
        } else { // field variable
            boolean privateField = ((varType & CodeVariable.ACCESS_MODIF_MASK) == CodeVariable.PRIVATE);
            FormDataObject formDO = FormEditor.getFormDataObject(component.getFormModel());
            JavaSource js = JavaSource.forFileObject(formDO.getPrimaryFile());
            MemberVisitor scanner = new MemberVisitor(component.getName(), privateField);
            try {
                js.runUserActionTask(scanner, true);
                if (privateField) {
                    // private field - need to rename occurrences out of generated code, but
                    // within the same class, so full and slow RenameRefactoring can be avoided
                    boolean codeChanged = renameOutOfGuardedCode(
                                            (GuardedDocument) scanner.info.getDocument(),
                                            scanner.usagesPositions,
                                            component.getName(), newName);
                    FormRefactoringUpdate.renameComponentInCustomCode(component, newName);
                    component.setName(newName);
                    if (codeChanged) {
                        // changed some references to the variable out of generated
                        // code - need to regenerate variables so the references
                        // are correct (and so e.g. found in subsequent renaming)
                        ((JavaCodeGenerator)FormEditor.getCodeGenerator(component.getFormModel()))
                                .regenerateVariables();
                    }
                } else { // need to run full RenameRefactoring
                    doRenameRefactoring(formDO, newName, scanner.getHandle());
                }
            } catch (IOException e) {
                Logger.getLogger(RADComponentRenameRefactoringSupport.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    private static boolean renameOutOfGuardedCode(final GuardedDocument doc,
                          final List<Integer> positions,
                          final String oldName, final String newName)
            throws IOException {
        if (positions == null || positions.isEmpty()) {
            return false;
        }

        final boolean[] codeChanged = { false };

        // rename the private variable occurrences in user code out of guarded blocks
        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                int positionDiff = 0;
                int len = oldName.length();
                try {
                    for (int pos : positions) {
                        pos += positionDiff;
                        if ((doc.getGuardedBlockChain().compareBlock(pos, pos+len) & MarkBlock.OVERLAP) == 0) {
                            // not in guarded block
                            doc.remove(pos, len);
                            doc.insertString(pos, newName, null);
                            positionDiff += newName.length() - len;
                            codeChanged[0] = true;
                        }
                    }
                } catch (BadLocationException ex) {
                    Logger.getLogger(RADComponentRenameRefactoringSupport.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                }
            }
        });

        return codeChanged[0];
    }

    private static void doRenameRefactoring(FormDataObject dao, String newName, TreePathHandle handle) throws IOException {
        if(handle==null){
            //this would only happen if setName were called without the correct component being
            //selected some how...
            return;
        }
        FormEditorSupport fes = dao.getFormEditorSupport();
        if (fes.isModified()) {
            fes.saveDocument();
        }
        //ok, so we are now ready to actually setup our RenameRefactoring...we need the element TreePathHandle
        Lookup rnl = Lookups.singleton(handle);
        RefactoringSession renameSession = RefactoringSession.create("Change variable name");//NOI18N
        RenameRefactoring refactoring = new RenameRefactoring(rnl);
        Problem pre = refactoring.preCheck();
        if(pre!=null&&pre.isFatal()){
            Logger.getLogger("global").log(Level.WARNING, "There were problems trying to perform the refactoring.");
        }

        Problem p = null;

        if( (!(pre!=null&&pre.isFatal())) && !emptyOrWhite(newName) ){
            refactoring.setNewName(newName);
            p = refactoring.prepare(renameSession);
        }

        if( (!(p!=null&&p.isFatal())) && !emptyOrWhite(newName) ){
            renameSession.doRefactoring(true);
        }
    }

    private static boolean emptyOrWhite(String s){
        if(s==null){
            return true;
        }
        if(s.trim().length()<0){
            return true;
        }
        return false;
    }
    
}
