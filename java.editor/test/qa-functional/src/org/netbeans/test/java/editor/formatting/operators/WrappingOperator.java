/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.formatting.operators;

import java.util.Arrays;
import java.util.List;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.util.NbBundle;

/**
 *
 * @author jprox
 */
public class WrappingOperator extends FormattingPanelOperator {

    public static final String NEVER = NbBundle.getMessage(FmtOptions.class, "LBL_wrp_WRAP_NEVER");
    public static final String ALWAYS = NbBundle.getMessage(FmtOptions.class, "LBL_wrp_WRAP_ALWAYS");
    public static final String IF_LONG = NbBundle.getMessage(FmtOptions.class, "LBL_wrp_WRAP_IF_LONG");

    private JComboBoxOperator extendImplementsKeyWord;
    
    private JComboBoxOperator extendsImplementsList;
    
    private JComboBoxOperator methodParameters;
    
    private JComboBoxOperator methodCallArguments;
    
    private JComboBoxOperator annotationArguments;
    
    private JComboBoxOperator chainedMethodCalls;
    
    private JCheckBoxOperator wrapAfterDot;
    
    private JComboBoxOperator throwsKeyword;
    
    private JComboBoxOperator throwsList;
    
    private JComboBoxOperator arrayInitializer;
    
    private JComboBoxOperator tryResources;
    
    private JComboBoxOperator disjunctiveCatchTypes;
    
    private JComboBoxOperator forArgs;
    
    private JComboBoxOperator forStatement;
    
    private JComboBoxOperator ifStatement;
    
    private JComboBoxOperator whileStatment;
    
    private JComboBoxOperator doWhileStatements;
    
    private JComboBoxOperator caseStatements;
    
    private JComboBoxOperator assertStatement;
    
    private JComboBoxOperator enumConstants;
    
    private JComboBoxOperator annotations;
    
    private JComboBoxOperator binaryOperators;
    
    private JCheckBoxOperator wrapAfterBinaryOperators;
    
    private JComboBoxOperator ternaryOperators;
    
    private JCheckBoxOperator wrapAfterTernaryOperators;
    
    private JComboBoxOperator assignmentOperators;
    
    private JCheckBoxOperator wrapAfterAssignmentOperators;
    
    private JComboBoxOperator lambdaParameters; 
    
    private JComboBoxOperator lambdaArrow;
    
    private JCheckBoxOperator wrapAfterLambdaArrow;
    
    public WrappingOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Wrapping");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) Settings.values());
    }

    public JComboBoxOperator getExtendImplementsKeyWord() {
        if (extendImplementsKeyWord == null) {
            extendImplementsKeyWord = formattingOperator.getComboBoxByLabel("Extends/Implements Keyword:");
            storeDefaultValue(Settings.EXTENDSKEYWORD);
        }
        return extendImplementsKeyWord;
    }
    
    public JComboBoxOperator getExtendsImplementsList() {
        if (extendsImplementsList == null) {
            extendsImplementsList = formattingOperator.getComboBoxByLabel("Extends/Implements List:");
            storeDefaultValue(Settings.EXTENDSLIST);
        }
        return extendsImplementsList;
    }
    
    public JComboBoxOperator getMethodParameters() {
        if (methodParameters == null) {
            methodParameters = formattingOperator.getComboBoxByLabel("Method Parameters");
            storeDefaultValue(Settings.METHODPARAMETERS);
        }
        return methodParameters;
    }
    
    public JComboBoxOperator getMethodCallArguments() {
        if (methodCallArguments == null) {
            methodCallArguments = formattingOperator.getComboBoxByLabel("Method Call Arguments:");
            storeDefaultValue(Settings.METHODCALLARGUMENTS);
        }
        return methodCallArguments;
    }
    
    public JComboBoxOperator getAnnotationArguments() {
        if (annotationArguments == null) {
            annotationArguments = formattingOperator.getComboBoxByLabel("Annotation Arguments");
            storeDefaultValue(Settings.ANNOTATIONARGUMENTS);
        }
        return annotationArguments;
    }
    
    public JComboBoxOperator getChainedMethodCalls() {
        if (chainedMethodCalls == null) {
            chainedMethodCalls = formattingOperator.getComboBoxByLabel("Chained Method Calls:");
            storeDefaultValue(Settings.CHAINEDMETHODCALLS);
        }
        return chainedMethodCalls;
    }
    
    public JCheckBoxOperator getWrapAfterDot() {
        if (wrapAfterDot == null) {
            wrapAfterDot = formattingOperator.getCheckboxOperatorByLabel("Wrap After Dot In Chained Method Call");
            storeDefaultValue(Settings.WRAPAFTERDOT);
        }
        return wrapAfterDot;
    }
    
    public JComboBoxOperator getThrowsKeyword() {
        if (throwsKeyword == null) {
            throwsKeyword = formattingOperator.getComboBoxByLabel("Throws Keyword:");
            storeDefaultValue(Settings.THROWSKEYWORD);
        }
        return throwsKeyword;
    }
    
    public JComboBoxOperator getThrowsList() {
        if (throwsList == null) {
            throwsList = formattingOperator.getComboBoxByLabel("Throws List:");
            storeDefaultValue(Settings.THROWSLIST);
        }
        return throwsList;
    }
    
    public JComboBoxOperator getArrayInitializer() {
        if (arrayInitializer == null) {
            arrayInitializer = formattingOperator.getComboBoxByLabel("Array Initializer:");
            storeDefaultValue(Settings.ARRAYINITIALIZER);
        }
        return arrayInitializer;
    }
    
    public JComboBoxOperator getTryResources() {
        if (tryResources == null) {
            tryResources = formattingOperator.getComboBoxByLabel("Try Resources:");
            storeDefaultValue(Settings.TRYRESOURCES);
        }
        return tryResources;        
    }
    
    public JComboBoxOperator getDisjunctiveCatchTypes() {
        if (disjunctiveCatchTypes == null) {
            disjunctiveCatchTypes = formattingOperator.getComboBoxByLabel("Disjunctive Catch Types:");
            storeDefaultValue(Settings.DISJUNCTIVECATCHTYPES);
        }
        return disjunctiveCatchTypes;
    }
    
    public JComboBoxOperator getForArgs() {
        if (forArgs == null) {
            forArgs = formattingOperator.getComboBoxByLabel("For:");
            storeDefaultValue(Settings.FOR);
        }
        return forArgs;
    }
    
    public JComboBoxOperator getForStatement() {
        if (forStatement == null) {
            forStatement = formattingOperator.getComboBoxByLabel("For Statement:");
            storeDefaultValue(Settings.FORSTATEMENT);
        }
        return forStatement;
    }
    
    public JComboBoxOperator getIfStatement() {
        if (ifStatement == null) {
            ifStatement = formattingOperator.getComboBoxByLabel("If Statement:");
            storeDefaultValue(Settings.IFSTATEMENT);
        }
        return ifStatement;
    }
    
    public JComboBoxOperator getWhileStatment() {
        if (whileStatment == null) {
            whileStatment = formattingOperator.getComboBoxByLabel("While Statement:");
            storeDefaultValue(Settings.WHILESTATEMENT);
        }
        return whileStatment;
    }
    
    public JComboBoxOperator getDoWhileStatements() {
        if (doWhileStatements == null) {
            doWhileStatements = formattingOperator.getComboBoxByLabel("Do ... While Statement:");
            storeDefaultValue(Settings.DOWHILESTATEMENT);
        }
        return doWhileStatements;
    }
    
    public JComboBoxOperator getCaseStatements() {
        if (caseStatements == null) {
            caseStatements = formattingOperator.getComboBoxByLabel("Case Statements:");
            storeDefaultValue(Settings.CASESTATEMENTS);
        }
        return caseStatements;
    }
    
    public JComboBoxOperator getAssertStatement() {
        if (assertStatement == null) {
            assertStatement = formattingOperator.getComboBoxByLabel("Assert:");
            storeDefaultValue(Settings.ASSERT);
        }
        return assertStatement;
    }
    
    public JComboBoxOperator getEnumConstants() {
        if (enumConstants == null) {
            enumConstants = formattingOperator.getComboBoxByLabel("Enum Constants:");
            storeDefaultValue(Settings.ENUMCONSTANTS);
        }
        return enumConstants;
    }
    
    public JComboBoxOperator getAnnotations() {
        if (annotations == null) {
            annotations = formattingOperator.getComboBoxByLabel("Annotations:");
            storeDefaultValue(Settings.ANNOTATIONS);
        }
        return annotations;
    }
    
    public JComboBoxOperator getBinaryOperators() {
        if (binaryOperators == null) {
            binaryOperators = formattingOperator.getComboBoxByLabel("Binary Operators:");
            storeDefaultValue(Settings.BINARYOPERATORS);
        }
        return binaryOperators;
    }
    
    public JCheckBoxOperator getWrapAfterBinaryOperators() {
        if (wrapAfterBinaryOperators == null) {
            wrapAfterBinaryOperators = formattingOperator.getCheckboxOperatorByLabel("Wrap After Binary Operators");
            storeDefaultValue(Settings.WRAPAFTERBINARYOPERATORS);
        }
        return wrapAfterBinaryOperators;
    }
    
    public JComboBoxOperator getTernaryOperators() {
        if (ternaryOperators == null) {
            ternaryOperators = formattingOperator.getComboBoxByLabel("Ternary Operators:");
            storeDefaultValue(Settings.TERNARYOPERATORS);
        }
        return ternaryOperators;
    }
    
    public JCheckBoxOperator getWrapAfterTernaryOperators() {
        if (wrapAfterTernaryOperators == null) {
            wrapAfterTernaryOperators = formattingOperator.getCheckboxOperatorByLabel("Wrap After Ternary Operators");
            storeDefaultValue(Settings.WRAPAFTERTERNARYOPERATORS);
        }
        return wrapAfterTernaryOperators;
    }
    
    public JComboBoxOperator getAssignmentOperators() {
        if (assignmentOperators == null) {
            assignmentOperators = formattingOperator.getComboBoxByLabel("Assignment Operators:");
            storeDefaultValue(Settings.ASSIGNMENTOPERATORS);
        }
        return assignmentOperators;
    }
    
    public JCheckBoxOperator getWrapAfterAssignmentOperators() {
        if (wrapAfterAssignmentOperators == null) {
            wrapAfterAssignmentOperators = formattingOperator.getCheckboxOperatorByLabel("Warp After Assignment Operators:");
            storeDefaultValue(Settings.WRAPAFTERASSIGNMENTOPERATORS);
        }
        return wrapAfterAssignmentOperators;
    }
    
    public JComboBoxOperator getLambdaParameters() {
        if (lambdaParameters == null) {
            lambdaParameters = formattingOperator.getComboBoxByLabel("Lambda Parameters:");
            storeDefaultValue(Settings.LAMBDAPARAMETERS);
        }
        return lambdaParameters;
    }
    
    public JComboBoxOperator getLambdaArrow() {
        if (lambdaArrow == null) {
            lambdaArrow = formattingOperator.getComboBoxByLabel("Lambda Arrow:");
            storeDefaultValue(Settings.LAMBDAARROW);
        }
        return lambdaArrow;
    }
    
    public JCheckBoxOperator getWrapAfterLambdaArrow() {
        if (wrapAfterLambdaArrow == null) {
            wrapAfterLambdaArrow = formattingOperator.getCheckboxOperatorByLabel("Wrap After Lambda Arrow");
            storeDefaultValue(Settings.WRAPAFTERLAMBDAARROW);
        }
        return wrapAfterLambdaArrow;
    }
    

    private enum Settings implements OperatorGetter {

        EXTENDSKEYWORD, EXTENDSLIST, METHODPARAMETERS, METHODCALLARGUMENTS, 
        ANNOTATIONARGUMENTS, CHAINEDMETHODCALLS, WRAPAFTERDOT,
        THROWSKEYWORD, THROWSLIST, ARRAYINITIALIZER, TRYRESOURCES, DISJUNCTIVECATCHTYPES,
        FOR, FORSTATEMENT, IFSTATEMENT, WHILESTATEMENT, DOWHILESTATEMENT,
        CASESTATEMENTS, ASSERT, ENUMCONSTANTS, ANNOTATIONS, BINARYOPERATORS,
        WRAPAFTERBINARYOPERATORS, TERNARYOPERATORS, WRAPAFTERTERNARYOPERATORS, 
        ASSIGNMENTOPERATORS, WRAPAFTERASSIGNMENTOPERATORS, LAMBDAPARAMETERS, LAMBDAARROW, WRAPAFTERLAMBDAARROW;

        @Override
        public Operator getOperator(FormattingPanelOperator fpo) {
            WrappingOperator wo = (WrappingOperator) fpo;
            switch (this) {
                case EXTENDSKEYWORD:
                    return wo.getExtendImplementsKeyWord();
                case EXTENDSLIST:
                    return wo.getExtendsImplementsList();
                case METHODPARAMETERS:
                    return wo.getMethodParameters();
                case METHODCALLARGUMENTS:
                    return wo.getMethodCallArguments();
                case ANNOTATIONARGUMENTS:
                    return wo.getAnnotationArguments();
                case CHAINEDMETHODCALLS:
                    return wo.getChainedMethodCalls();
                case WRAPAFTERDOT:
                    return wo.getWrapAfterDot();
                case THROWSKEYWORD:
                    return wo.getThrowsKeyword();
                case THROWSLIST:
                    return wo.getThrowsList();
                case ARRAYINITIALIZER:
                    return wo.getArrayInitializer();
                case TRYRESOURCES:
                    return wo.getTryResources();
                case DISJUNCTIVECATCHTYPES:
                    return wo.getDisjunctiveCatchTypes();
                case FOR:
                    return wo.getForArgs();
                case FORSTATEMENT:
                    return wo.getForStatement();
                case IFSTATEMENT:
                    return wo.getIfStatement();
                case WHILESTATEMENT:
                    return wo.getWhileStatment();
                case DOWHILESTATEMENT:
                    return wo.getDoWhileStatements();
                case CASESTATEMENTS:
                    return wo.getCaseStatements();
                case ASSERT:
                    return wo.getAssertStatement();
                case ENUMCONSTANTS:
                    return wo.getEnumConstants();
                case ANNOTATIONS:
                    return wo.getAnnotations();
                case BINARYOPERATORS:
                    return wo.getBinaryOperators();
                case WRAPAFTERBINARYOPERATORS:
                    return wo.getWrapAfterBinaryOperators();
                case TERNARYOPERATORS:
                    return wo.getTernaryOperators();
                case WRAPAFTERTERNARYOPERATORS:
                    return wo.getWrapAfterTernaryOperators();
                case ASSIGNMENTOPERATORS:
                    return wo.getAssignmentOperators();
                case WRAPAFTERASSIGNMENTOPERATORS:
                    return wo.getWrapAfterAssignmentOperators();
                case LAMBDAPARAMETERS:
                    return wo.getLambdaParameters();
                case LAMBDAARROW:
                    return wo.getLambdaArrow();
                case WRAPAFTERLAMBDAARROW:
                    break;
                default:
                    throw new AssertionError(this.name());                                
            }
            return null;       
        }

        @Override
        public String key() {
            return this.name();
        }

    }

}
