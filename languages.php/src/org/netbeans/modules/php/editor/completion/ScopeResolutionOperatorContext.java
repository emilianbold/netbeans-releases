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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.gsf.CompletionProposal;
import org.netbeans.modules.languages.php.lang.Operators;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassMemberReference;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.ObjectDefinition;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the 
 * Scope Resolution Operator (::) context.
 * 
 * <p><b>Note that this implementation is not synchronized.</b></p> 
 * 
 * @see PHP Manual / Example 19.12. :: from outside the class definition
 * @see PHP Manual / Example 19.13. :: from inside the class definition
 * 
 * @author Victor G. Vasilyev 
 */
public class ScopeResolutionOperatorContext extends ASTBasedProvider 
        implements CompletionResultProvider {

    private ClassDefinition referencedClass;
    private int insertOffset;
    
    private static final String SCOPE_RESOLUTION_OPERATOR = 
            Operators.SCOPE_RESOLUTION.value();

    private static final Set<ExpectedToken> PREV_TOKENS = new HashSet<ExpectedToken>();
    static {
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.OPERATOR.value(), 
                        SCOPE_RESOLUTION_OPERATOR));
    }   

    /**
     * Returns <code>true</code> iif the specified <code>context</code>
     * is applicable for completing the Scope Resolution Operator (::),
     * i.e. caret is located after this operator.
     *  
     * E.g. see PHP Manual / Example 19.12. :: from outside the class definition
     * <p><code> 
     * <b>&lt;?php</b>
     * class MyClass {
     *     const CONST_VALUE = 'A constant value';
     * }
     * 
     * echo MyClass::<span style="color: rgb(255, 0, 0);"><blink>|</blink></span>CONST_VALUE;
 
     *     ... <b>?&gt;</b>
     * </code></p> 
     * 
     * @param context the <code>CodeCompletionContext</code>.
     * @return
     */
    public boolean isApplicable(CodeCompletionContext context) {
        assert context != null;
        myContext = context; // caching context!
        SourceElement e = myContext.getSourceElement();       
        if(e instanceof Constant) {
            if(!isScopeResolutionExpression(e)) {
                return false;
            }
            // i.e. constant expr like this: SomeClass::x
            referencedClass = getReferencedClass((Constant)e);
        } else if(isApplicableIncompleteExpression()) {
            referencedClass = getReferencedClass(context);
        }
        // This provider is applicble iif it possible to find referencedClass.
        if(referencedClass == null) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        if (context != myContext) {
            throw new IllegalStateException("The isApplicable method MUST BE called before.");
        }
        String prefix = context.getPrefix();
        insertOffset = calcInsertOffset();
        List<CompletionProposal> list = new LinkedList<CompletionProposal>();

        // Example 19.13. :: from inside the class definition
        // This also applies to Constructors and Destructors, Overloading, 
        // and Magic method definitions. 

        // Example 19.12. :: from outside the class definition
        // <ClassName>::<PublicStaticClassMemberOrConstant>

        ClassBody cb = referencedClass.getBody();

        // constants
        List<ConstDeclaration> cdList = cb.getChildren(ConstDeclaration.class);
        for (ConstDeclaration cDecl : cdList) {
            for (ClassConst cc : cDecl.getDeclaredConstants()) {
                String name = cc.getName();
                if (SCOPE_RESOLUTION_OPERATOR.equals(prefix) ||
                        startsWith(name, prefix)) {
                    list.add(new VariableItem(name, insertOffset,
                            VariableItem.VarTypes.CONSTANT,
                            myContext.getFormatter(), false));

                }
            }
        }
        
        // public static methods
        // It seems ClassFunctionDeclaration should be used instead.
        // In this case, modifiers can be shown in the proposal list.
        List<FunctionDefinition> fdList = cb.getChildren(FunctionDefinition.class);
        for (FunctionDefinition fd : fdList) {
            // TODO ??? select public static only
            FunctionDeclaration decl = fd.getDeclaration();
            String name = decl.getName();
            if (isApplicableIncompleteExpression() || startsWith(name, prefix)) {
                list.add(new UserDefinedMethodItem(decl,
                        insertOffset, myContext.getFormatter()));
            }
        }

        return list;
    }
   
    /**
     * 
     * @return <code>true</code> if a constant value is not specified after
     * scope resolution operator.
     */
    private boolean isApplicableIncompleteExpression() {
        String prefix = myContext.getPrefix();
        if(prefix != null && prefix.endsWith(SCOPE_RESOLUTION_OPERATOR)) {
            return true;
        }
        return false;
    }
    
    private static ClassDefinition getReferencedClass(CodeCompletionContext context) {
        // Context description:
        // SomeClassIdentifier::
        // i.e. Identifier of the member is not presented.
        // In this case the PHP Model don't interpret the expression as
        // the scope resolution expression.
        String referencedClassName = getReferencedClassName(context);
        PhpModel model = context.getSourceElement().getModel();
        return findClassDefinition(model, referencedClassName);
    }

    private static String getReferencedClassName(CodeCompletionContext context) {
        // TODO implement me!
        SourceElement e = context.getSourceElement();
        String text = e.getText();
        int opIndex = text.lastIndexOf(SCOPE_RESOLUTION_OPERATOR);
        String className = null;
        if(opIndex > -1) {
          className = text.substring(0, opIndex);
        }
        return className;
    }

    private boolean isScopeResolutionExpression(SourceElement e) {
        String text = e.getText();
        if (text != null &&
                text.contains(Operators.SCOPE_RESOLUTION.value())) {
            return true;
        }
        return false;
    }
    
    private static boolean startsWith(String name, String prefix) {
        if(name == null || prefix == null) {
            return false;
        }
        return name.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    // TODO: move to PHPModelUtil
    private static ClassDefinition getReferencedClass(Constant c) {
        ClassMemberReference<SourceElement> classReference = c.getClassConstant();
        if(classReference == null) {
          return null;
        }
        return findClassDefinition(c.getModel(), classReference);
    }

    private static ClassDefinition findClassDefinition(PhpModel model, 
                                ClassMemberReference<SourceElement> reference) {
        assert model != null;
        assert reference != null;
        ObjectDefinition od = reference.getObject();
        if(od instanceof ClassDefinition) {
            return (ClassDefinition)od;
        }
        return null;
    }
    
    private static ClassDefinition findClassDefinition(PhpModel model, String className) {
        List<ClassDefinition> cdList = model.getStatements(ClassDefinition.class);
        for (ClassDefinition cd : cdList) {
            if (cd.getName().equals(className)) {
                return cd;
            }
        }
        return null;
    }

    
    private int calcInsertOffset() {
        String prefix = myContext.getPrefix();
        if(prefix == null || SCOPE_RESOLUTION_OPERATOR.equals(prefix)) {
            return myContext.getCaretOffset();
        }
        return myContext.getCaretOffset() - myContext.getPrefix().length();
    }
        
}
