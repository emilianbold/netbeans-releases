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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.HashMap;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author jdeva
 */
public class Refactor {
    
    public static class ElementRenamer extends TreePathScanner<Tree, Element>{
        private WorkingCopy workingCopy;
        private String newName;
        
        /** Creates a new instance of Refactor */
        public ElementRenamer(WorkingCopy workingCopy, String newName) {
            this.newName = newName;
            this.workingCopy = workingCopy;
        }
        
        @Override
        public Tree visitIdentifier(IdentifierTree node,Element elemToRename) {
            renameIfMatch(getCurrentPath(), node, elemToRename);
            return super.visitIdentifier(node, elemToRename);
        }
        
        @Override
        public Tree visitMemberSelect(MemberSelectTree node,Element elemToRename) {
            renameIfMatch(getCurrentPath(), node,elemToRename);
            return super.visitMemberSelect(node, elemToRename);
        }
        
        @Override
        public Tree visitMethod(MethodTree tree,Element elemToRename) {
            renameIfMatch(getCurrentPath(), tree, elemToRename);
            return super.visitMethod(tree, elemToRename);
        }
        
        
        @Override
        public Tree visitVariable(VariableTree tree,Element elemToRename) {
            renameIfMatch(getCurrentPath(), tree, elemToRename);
            return super.visitVariable(tree, elemToRename);
        }
        
        private void renameIfMatch(TreePath path, Tree tree, Element elemToRename) {
            if (workingCopy.getTreeUtilities().isSynthetic(path))
                return;
            Element el = workingCopy.getTrees().getElement(path);
            if (el != null && el.equals(elemToRename)) {
                Tree nju = workingCopy.getTreeMaker().setLabel(tree, newName);
                workingCopy.rewrite(tree, nju);
            }
        }
    }
    
   public static class LiteralRenamer extends TreePathScanner<Tree, Void>{
        private WorkingCopy workingCopy;
        private String newName, oldName;
        
        public LiteralRenamer(WorkingCopy workingCopy, String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
            this.workingCopy = workingCopy;
        }
        
        @Override
        public Tree visitLiteral(LiteralTree tree, Void v) {
            renameIfMatch(tree);
            return super.visitLiteral(tree, v);
        }
        
        private void renameIfMatch(LiteralTree tree) {
            if(tree.getKind() == Tree.Kind.STRING_LITERAL && oldName.equals(tree.getValue())) {
                Tree nju = workingCopy.getTreeMaker().Literal(newName);
                workingCopy.rewrite(tree, nju);             
            }
        }
    }
   
   /**
    * Visitor class to update all return statements returning the old string literal 
    * with the new string literal
    * */
   public static class ReturnStatementLiteralRenamer extends TreePathScanner<Tree, Void>{
        private WorkingCopy workingCopy;
        private String newName, oldName;
        
        public ReturnStatementLiteralRenamer(WorkingCopy workingCopy, String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
            this.workingCopy = workingCopy;
        }
        
        @Override
        public Tree visitReturn(ReturnTree tree, Void v) {
            renameIfMatch(tree);
            return super.visitReturn(tree, v);
        }

        private void renameIfMatch(ReturnTree tree) {
            ExpressionTree expr = tree.getExpression();
            if(expr.getKind() == Tree.Kind.STRING_LITERAL) {
                LiteralTree literal = (LiteralTree)expr;
                if(oldName.equals(literal.getValue())) {
                    Tree newLiteral = workingCopy.getTreeMaker().Literal(newName);
                    workingCopy.rewrite(literal, newLiteral);
                }           
            }
        }
    }   
   
    public static class ElementsRenamer extends TreePathScanner<Tree, Void>{
        private final WorkingCopy workingCopy;
        private final TreeMaker make;
        private HashMap<Element, String> elementAndNames = new HashMap<Element, String>();
        
        /** Creates a new instance of Refactor */
        public ElementsRenamer(WorkingCopy workingCopy, HashMap<? extends ElementHandle, String> handleAndNames) {
            this.workingCopy = workingCopy;
            make = workingCopy.getTreeMaker();
            for(ElementHandle elemHandle : handleAndNames.keySet()){
                Element elem = elemHandle.resolve(workingCopy);
                elementAndNames.put(elem, handleAndNames.get(elemHandle));
            }            
        }
        
        @Override
        public Tree visitIdentifier(IdentifierTree tree, Void v) {
            renameIfMatch(getCurrentPath(), tree);
            return super.visitIdentifier(tree, v);
        }
        
        @Override
        public Tree visitMemberSelect(MemberSelectTree tree, Void v) {
            renameIfMatch(getCurrentPath(), tree);
            return super.visitMemberSelect(tree, v);
        }
        
        @Override
        public Tree visitMethod(MethodTree tree, Void v) {
            renameIfMatch(getCurrentPath(), tree);
            return super.visitMethod(tree, v);
        }
        
        
        @Override
        public Tree visitVariable(VariableTree tree, Void v) {
            renameIfMatch(getCurrentPath(), tree);
            return super.visitVariable(tree, v);
        }
        
        private void renameIfMatch(TreePath path, Tree tree) {
            if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                return;
            }
            Element el = workingCopy.getTrees().getElement(path);
            if(el != null && elementAndNames.containsKey(el)) {
                Tree nju = make.setLabel(tree, elementAndNames.get(el));
                workingCopy.rewrite(tree, nju);
            }
        }
    }   
}
