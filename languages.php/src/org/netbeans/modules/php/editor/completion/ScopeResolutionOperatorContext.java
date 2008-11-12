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
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompletionProposal;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.languages.php.lang.Operators;
import org.netbeans.modules.languages.php.lang.SpecialKeywords;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.AttributesDeclaration;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassFunctionDefinition;
import org.netbeans.modules.php.model.ClassMemberReference;
import org.netbeans.modules.php.model.Constant;
import org.netbeans.modules.php.model.Modifier;
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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class ScopeResolutionOperatorContext extends MemberAccessExpressionScope 
        implements CompletionResultProvider {

    private static final Logger LOG = 
            Logger.getLogger(ScopeResolutionOperatorContext.class.getName());

    /**
     * This contains a member access expression if it is defined by the context,
     * oherwise (i.e. an incomplete expression is defined) - <code>null</code>.
     */
    private Constant expression;
    

    private static final Set<ExpectedToken> PREV_TOKENS = new HashSet<ExpectedToken>();
    static {
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.OPERATOR.value(), 
                        Operators.SCOPE_RESOLUTION.value()));
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
        init(context);
        SourceElement e = myContext.getSourceElement();       
        if(e instanceof Constant) {
            if(!isScopeResolutionExpression(e)) {
                expression = (Constant)e;
                LOG.log(Level.INFO, 
                        "{0} provider is NOT applicable." + 
                                        " expression.getText() =[{1}]", 
                        new Object[] {"ScopeResolutionOperatorContext", 
                                      expression.getText()});
                return false;
            }
            // i.e. constant expr like this: SomeClass::x
            referencedClass = getReferencedClass((Constant)e);
        } else if(isApplicableIncompleteExpression()) {
            referencedClass = getReferencedClass(context);
        } else {
            referencedClass = null;
        }
        // This provider is applicble iif it possible to find referencedClass.
        if(referencedClass == null) {
            LOG.log(Level.INFO, 
                    "{0} provider is NOT applicable. referencedClass =[{1}]", 
                    new Object[] {"ScopeResolutionOperatorContext", 
                                  referencedClass});
            return false;
        }
        LOG.log(Level.INFO, 
                "{0} provider is applicable. referencedClass.getName() =[{1}]", 
                new Object[] {"ScopeResolutionOperatorContext", 
                              referencedClass.getName()});
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        assert context == myContext;
        assert referencedClass != null;

        // PHP 5.3.0: Should the static access mode be processed differently?
        if (isLocalReference()) {
            // Should the self access mode be processed differently?

            // Example 19.13. :: from inside the class definition
            addConstants();
            // This also applies to Constructors and Destructors, Overloading, 
            // and Magic method definitions. 
            // (private|protected|public) (static|default), i.e. ANY
            addMethods(null);
            // (private|protected|public) (static|default), i.e. ANY
            addProperties(null);
        } else {
            if (isParentAccess()) {  // i.e. parent::xxx
                // all constants
                addConstants();
                // !private=(protected|public|default) (static|default) methods
                addMethods(new Filter<ClassFunctionDefinition>() {

                    @Override
                    public boolean isApplicable(ClassFunctionDefinition fd) {
                        List<Modifier> modifiers = fd.getModifiers();
                        int actualFlags = Modifier.toFlags(modifiers);
                        if ((actualFlags & Modifier.PRIVATE.flag()) != 0) {
                            return false;
                        }
                        return true;
                    }
                });
                // !private=(protected|public) static properties
                addProperties(new Filter<AttributesDeclaration>() {

                    @Override
                    public boolean isApplicable(AttributesDeclaration ad) {
                        List<Modifier> modifiers = ad.getModifiers();
                        int actualFlags = Modifier.toFlags(modifiers);
                        if ((actualFlags & Modifier.PRIVATE.flag()) != 0) {
                            return false;
                        }
                        if ((actualFlags & Modifier.STATIC.flag()) == 0) {
                            return false;
                        }
                        return true;
                    }
                });

            } else {
                // Example 19.12. :: from outside the class definition
                // <ClassName>::<PublicStaticClassMemberOrConstant>
                addConstants();
                // Wow! It is possible to call a non-static method via ::
                // from outside the class definition. So, all public methods of
                // the specified class should be collected.
                addMethods(new Filter<ClassFunctionDefinition>() {

                    @Override
                    public boolean isApplicable(ClassFunctionDefinition fd) {
                        List<Modifier> modifiers = fd.getModifiers();
                        int actualFlags = Modifier.toFlags(modifiers);
                        int logicalFlags = Modifier.toLogicalFlags(actualFlags);
                        int expectedFlags = Modifier.PUBLIC.flag();
                        if ((logicalFlags & expectedFlags) != expectedFlags) {
                            return false;
                        }
                        return true;
                    }
                });
                addProperties(new Filter<AttributesDeclaration>() {

                    @Override
                    public boolean isApplicable(AttributesDeclaration ad) {
                        List<Modifier> modifiers = ad.getModifiers();
                        int actualFlags = Modifier.toFlags(modifiers);
                        int logicalFlags = Modifier.toLogicalFlags(actualFlags);
                        int expectedFlags = Modifier.STATIC.flag() |
                                Modifier.PUBLIC.flag();
                        if (logicalFlags != expectedFlags) {
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
        
        return proposals;
    }
       
    /**
     * Retirns <code>true</code> if the member access expression defines access
     * to the parent's member via the special keyword - <code>parent</code>.
     * @return <code>true</code> if expression in the context is like this:
     * <code>parent::someMember</code>, otherwise <code>false</code>.
     */
    private boolean isParentAccess() {
        if(expression != null) {
            ClassMemberReference<SourceElement> ref = 
                                                  expression.getClassConstant();
            if(ref == null) {
                return false;
            }
            String name = ref.getObjectName();
            if(SpecialKeywords.PARENT.value().equals(name)) {
                return true;
            }
        }
        // try to find the "parent" word in the incomplete expression
        SourceElement e = myContext.getSourceElement();
        if(e != null) {
            String text = e.getText();
            return text.startsWith(SpecialKeywords.PARENT.value());
        }
        return false;
    }
    
    /**
     * Retirns <code>true</code> if the member access expression refers to the
     * class where it is placed itself.
     * <p>
     * <b>Note:</b> This method returns <code>false</code> for the expression
     * like this:
     * <code>parent::someMemeber</code>
     * </p>
     * 
     * @return <code>true</code> if the context is located inside the referred
     * class, otherwise <code>false</code>.
     */
    private boolean isLocalReference() {
        SourceElement e = myContext.getSourceElement();
        while(e!=null) {
            if(e == referencedClass) {
                return true;
            }
            e = e.getParent();
        }
        return false;
    }
    
    private static ClassDefinition getReferencedClass(CodeCompletionContext context) {
        // Context description:
        // SomeClassIdentifier::
        // i.e. Identifier of the member is not presented.
        // In this case the PHP Model doesn't interpret the expression as
        // the scope resolution expression.
        String referencedClassName = getReferencedClassName(context);
        PhpModel model = context.getSourceElement().getModel();
        return findClassDefinition(model, referencedClassName);
    }

    private static String getReferencedClassName(CodeCompletionContext context) {
        // TODO implement me!
        SourceElement e = context.getSourceElement();
        String text = e.getText();
        int opIndex = text.lastIndexOf(Operators.SCOPE_RESOLUTION.value());
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

    
    @Override
    protected int calcInsertOffset() {
        if(prefix == null || getOperator().equals(prefix)) {
            return myContext.getCaretOffset();
        }
        return myContext.getCaretOffset() - prefix.length();
    }

    @Override
    protected String getOperator() {
        return Operators.SCOPE_RESOLUTION.value();
    }
        
}
