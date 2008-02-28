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

package org.netbeans.modules.php.editor.completion;

import java.util.List;
import java.util.LinkedList;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.php.model.Attribute;
import org.netbeans.modules.php.model.AttributesDeclaration;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassFunctionDefinition;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.Modifier;
import org.netbeans.modules.php.model.SourceElement;

/**
 * An abstraction to support basic methods of the code completion in the scope 
 * of the member access expressions, like this:
 * <code>
 * Class1::$attr
 * Class1::CONST1
 * $instanceRef->func1(); 
 * <code>
 *  
 * @author Victor G. Vasilyev
 */
public abstract class MemberAccessExpressionScope  extends ASTBasedProvider {
    protected List<CompletionProposal> proposals;
    protected String prefix;
    protected int insertOffset;

    protected ClassDefinition referencedClass;
    
    protected abstract class Filter<T extends SourceElement> {
        public abstract boolean isApplicable(T e);
    }

    protected void init(CodeCompletionContext context) {
        proposals = new LinkedList<CompletionProposal>();
        assert context != null;
        myContext = context;
        prefix = context.getPrefix();
        insertOffset = calcInsertOffset();
    }
    
    protected abstract int calcInsertOffset();
    
    protected abstract String getOperator();
    

    protected void addConstants() {
        ClassBody cb = referencedClass.getBody();
        List<ConstDeclaration> cdList = cb.getChildren(ConstDeclaration.class);
        for (ConstDeclaration cDecl : cdList) {
            for (ClassConst cc : cDecl.getDeclaredConstants()) {
                String name = cc.getName();
                if (getOperator().equals(prefix) || 
                                                     startsWith(name, prefix)) {
                    proposals.add(new VariableItem(name, insertOffset, 
                                              VariableItem.VarTypes.CONSTANT, 
                                              myContext.getFormatter(), false));
                }
            }
        }
    }

    protected void addMethods(Filter<ClassFunctionDefinition> f) {
        ClassBody cb = referencedClass.getBody();
        List<ClassFunctionDefinition> fdList = 
                                 cb.getChildren(ClassFunctionDefinition.class);
        for (ClassFunctionDefinition fd : fdList) {
            // TODO ??? select public static only
            // int actualFlags = Modifier.toFlags(fd.getModifiers());           
            List<Modifier> modifiers = fd.getModifiers();
            if(f != null){
                if(!f.isApplicable(fd)) {
                    continue;
                }
            }
            FunctionDeclaration decl = fd.getDeclaration();
            String name = decl.getName();
            if (isApplicableIncompleteExpression() || startsWith(name, prefix)) {
                proposals.add(new UserDefinedMethodItem(decl, insertOffset, 
                                                   myContext.getFormatter()));
            }
        }
    }

    /**
     * Adds properties.
     */
    protected void addProperties(Filter<AttributesDeclaration> f) {
        ClassBody cb = referencedClass.getBody();
        List<AttributesDeclaration> adList = 
                                    cb.getChildren(AttributesDeclaration.class);
        for (AttributesDeclaration ad : adList) {
            List<Modifier> modifiers = ad.getModifiers();
            if(f != null){
                if(!f.isApplicable(ad)) {
                    continue;
                }
            }
            for (Attribute at : ad.getDeclaredAttributes()) {
                String name = at.getName();
                if (getOperator().equals(prefix) || startsWith(name, prefix)) {
                    proposals.add(new VariableItem(name, insertOffset, 
                                              VariableItem.VarTypes.CONSTANT, 
                                              myContext.getFormatter(), false));
                }
            }
        }
    }
    
   
    private static boolean startsWith(String name, String prefix) {
        if(name == null || prefix == null) {
            return false;
        }
        return name.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    /**
     * 
     * @return <code>true</code> if a constant value is not specified after
     * scope resolution operator.
     */
    protected boolean isApplicableIncompleteExpression() {
        if(prefix != null && prefix.endsWith(getOperator())) {
            return true;
        }
        return false;
    }
    
}
