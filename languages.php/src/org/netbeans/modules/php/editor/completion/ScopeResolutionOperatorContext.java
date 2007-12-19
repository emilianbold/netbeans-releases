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
import org.netbeans.modules.languages.php.lang.SpecialKeywords;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassReference;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
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

    private Constant constant;
    private ClassDefinition referencedClass;
    private int insertOffset;
    
    private static final String SCOPE_RESOLUTION_OPERATOR = "::";

    private static final Set<ExpectedToken> PREV_TOKENS = new HashSet<ExpectedToken>();
    static {
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.OPERATOR.value(), 
                        SCOPE_RESOLUTION_OPERATOR));
    }   

    /**
     * Returns <code>true</code> iif the specified <code>context</code>
     * is applicable for completing the Scope Resolution Operator (::),
     * i.e. caret is located immediately after this operator.
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
        myContext = context;
        if(!isScopeResolutionOperatorContext()) {
            return false;
        }
        SourceElement e = myContext.getSourceElement();
        if(!(e instanceof Constant)) {
          return false;
        }
        constant = (Constant)e;
        ClassReference<SourceElement> classReference = constant.getClassConstant();
        if(classReference == null) {
          return false;
        }
        referencedClass = findClassDefinition(getModel(), classReference);
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
        insertOffset = calcInsertOffset();
        List<CompletionProposal> list = new LinkedList<CompletionProposal>();
        
        if(isFromInsideClassDefinition()) {
           // Example 19.13. :: from inside the class definition
            // TODO Process special keywords self and parent that are used to access
            // members or methods from inside the class definition when :: is used 
            // from inside the class definition.
            // This also applies to Constructors and Destructors, Overloading, 
            // and Magic method definitions. 

        }
        else { // i.e. From Outside Class Definition
           // Example 19.12. :: from outside the class definition
           // <ClassName>::<PublicStaticClassMemberOrConstant>
            
            ClassBody cb = referencedClass.getBody();
            
            // constants
            List<ConstDeclaration> cdList = cb.
                    getChildren(ConstDeclaration.class);
            for(ConstDeclaration cDecl: cdList) {
                for(ClassConst cc: cDecl.getDeclaredConstants()) {
                    String name = cc.getName();
    //                if (isMatchedConstant(decl.getName(), prefix)) 
    //                {
                        list.add(new VariableItem(name, insertOffset, 
                                            VariableItem.VarTypes.ATTRIBUTE,
                                            myContext.getFormatter(), false));

    //                }
                }
            }
            // public static methods
            List<FunctionDefinition> fdList = cb.getChildren(FunctionDefinition.class);
            for(FunctionDefinition fd: fdList) {
                // TODO select public static only
                FunctionDeclaration decl = fd.getDeclaration();
//                if (isMatchedFunction(decl.getName(), prefix)) 
//                {
                    list.add(new UserDefinedMethodItem(decl, 
                             insertOffset, myContext.getFormatter() ));
//                }
            }
            
        }
        return list;
    }
    
    private static ClassDefinition findClassDefinition(PhpModel model, ClassReference<SourceElement> reference) {
        assert model != null;
        assert reference != null;
        String className = reference.getObjectName();
        if(className == null) {
            return null;
        }
        if(SpecialKeywords.SELF.value().equals(className)) {
            // TODO: find nearest outer class
            return null;
        }
        else if(SpecialKeywords.PARENT.value().equals(className)) {
            // TODO: find superclass for the nearest outer class
            return null;
        }
        return findClassDefinition(model, className);
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
        if(SCOPE_RESOLUTION_OPERATOR.equals(myContext.getPrefix())) {
            return myContext.getCaretOffset();
        }
        return myContext.getCaretOffset() - myContext.getPrefix().length();
    }
    
    private boolean isScopeResolutionOperatorContext() {
        if(SCOPE_RESOLUTION_OPERATOR.equals(myContext.getPrefix())) {
            return true;
        }
        
// TODO: the following use case doesn't work, because:
// java.lang.AssertionError
// at org.netbeans.modules.php.model.impl.ClassReferenceImpl.initIds(ClassReferenceImpl.java:141)
// at org.netbeans.modules.php.model.impl.ClassReferenceImpl.getObjectName(ClassReferenceImpl.java:98)
// at org.netbeans.modules.php.editor.completion.ScopeResolutionOperatorContext.getProposals(ScopeResolutionOperatorContext.java:146)
// when Class1::f|
//
//        if(isMatchedCL1(ANY_TOKEN, PREV_TOKENS)) {
//            return true;
//        }
        return false;
    }
    
    private boolean isFromInsideClassDefinition() {
        SourceElement e = constant;
        while(e!=null) {
            if(e == referencedClass) {
                return true;
            }
            e = e.getParent();
        }
        return false;
    }

    
}
