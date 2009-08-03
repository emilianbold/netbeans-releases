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
package org.netbeans.modules.web.core.syntax.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.el.lexer.api.ELTokenId.ELTokenCategories;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
/**
 *  This is a helper class for parsing and obtaining items for code completion of expression
 *  language.
 */
public class ELExpression {

    private static final Logger logger = Logger.getLogger(ELExpression.class.getName());
    /** it is not Expession Language */
    public static final int NOT_EL = 0;
    /** This is start of an EL expression */
    public static final int EL_START = 1;
    /** The expression is bean */
    public static final int EL_BEAN = 2;
    /** The expression is implicit language */
    public static final int EL_IMPLICIT = 3;
    /** The expression is EL function */
    public static final int EL_FUNCTION = 4;
    /** It is EL but we are not able to recognize it */
    public static final int EL_UNKNOWN = 5;
    /** The expression - result of the parsing */
    private String expression;
    protected JspSyntaxSupport sup;
    private String replace;
    private boolean isDefferedExecution = false;

    public ELExpression(JspSyntaxSupport sup) {
        this.sup = sup;
        this.replace = "";
    }

    /** Parses text before offset in the document. Doesn't parse after offset.
     *  It doesn't parse whole EL expression until ${ or #{, but just simple expression.
     *  For example ${ 2 < bean.start }. If the offset is after bean.start, then only bean.start
     *  is parsed.
     */
    public int parse(int offset) {
        BaseDocument document = sup.getDocument();
        String value = null;
        document.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(document);
            //find EL token sequence and its superordinate sequence
            TokenSequence ts = hi.tokenSequence();
            TokenSequence last = null;
            for (;;) {
                if (ts == null) {
                    break;
                }
                if (ts.language() == ELTokenId.language()) {
                    //found EL
                    isDefferedExecution = last.token().text().toString().startsWith("#{"); //NOI18N
                    break;
                } else {
                    //not el, scan next embedded token sequence
                    ts.move(offset);
                    if (ts.moveNext() || ts.movePrevious()) {
                        last = ts;
                        ts = ts.embedded();
                    } else {
                        //no token, cannot embed
                        return NOT_EL;
                    }
                }
            }

            if(ts == null) {
                return NOT_EL;
            }


            int diff = ts.move(offset);
            if (diff == 0) {
                if (!ts.movePrevious()) {
                    return EL_START;
                }
            } else if (!ts.moveNext()) {
                return EL_START;
            }

            // Find the start of the expression. It doesn't have to be an EL delimiter (${ #{)
            // it can be start of the function or start of a simple expression.
            Token token = ts.token();
            while ((!ELTokenCategories.OPERATORS.hasCategory(ts.token().id()) || ts.token().id() == ELTokenId.DOT) &&
                    ts.token().id() != ELTokenId.WHITESPACE &&
                    (!ELTokenCategories.KEYWORDS.hasCategory(ts.token().id()) ||
                    ELTokenCategories.NUMERIC_LITERALS.hasCategory(ts.token().id()))) {

                //repeat until not ( and ' ' and keyword or number
                if (value == null) {
                    value = ts.token().text().toString();
                    if (ts.token().id() == ELTokenId.DOT) {
                        replace = "";
                    } else if (ts.token().text().length() >= (offset - ts.token().offset(hi))) {
                        if (ts.token().offset(hi) <= offset) {
                            value = value.substring(0, offset - ts.token().offset(hi));
                            replace = value;
                        } else {
                            // cc invoked within EL delimiter
                            return NOT_EL;
                        }
                    }
                } else {
                    value = ts.token().text().toString() + value;
                    if (ts.token().id() == ELTokenId.TAG_LIB_PREFIX) {
                        replace = value;
                    }
                }
                token = ts.token();
                if (!ts.movePrevious()) {
                    //we are on the beginning of the EL token sequence
                    break;
                }
            }

            if (token.id() == ELTokenId.WHITESPACE || token.id() == ELTokenId.LPAREN) {
                return EL_START;
            }

            if (token.id() != ELTokenId.IDENTIFIER && token.id() != ELTokenId.TAG_LIB_PREFIX) {
                value = null;
            } else if (value != null) {
                return findContext(value);
            }
        } finally {
            document.readUnlock();
            expression = value;
        }
        return NOT_EL;
    }

    public List<CompletionItem> getPropertyCompletionItems(String beanType, int anchor) {
        PropertyCompletionItemsTask task = new PropertyCompletionItemsTask(beanType, anchor);
        runTask(task);

        return task.getCompletionItems();
    }

    public boolean gotoPropertyDeclaration(String beanType) {
        GoToSourceTask task = new GoToSourceTask(beanType);
        runTask(task);
        return task.wasSuccessful();
    }

    /**
     *  @return the class of the top-level object used in the expression
     */
    public String getObjectClass() {
        String beanName = extractBeanName();

        BeanData[] allBeans = sup.getBeanData();
        if (allBeans != null) {
            for (BeanData beanData : allBeans) {
                if (beanData.getId().equals(beanName)) {
                    return beanData.getClassName();
                }

            }
        }

        // not found within declared beans, try implicit objects
        ELImplicitObjects.ELImplicitObject implObj = ELImplicitObjects.getELImplicitObject(beanName);

        if (implObj != null) {
            return implObj.getClazz();
        }

        return null;
    }

    protected void runTask(CancellableTask task) {
        ClasspathInfo cpInfo = ClasspathInfo.create(sup.getFileObject());
        JavaSource source = JavaSource.create(cpInfo, Collections.EMPTY_LIST);

        try {
            source.runUserActionTask(task, true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public String extractBeanName() {
        String elExp = getExpression();

        if (elExp != null && !elExp.equals("")) {
            if (elExp.indexOf('.') > -1) {
                String beanName = elExp.substring(0, elExp.indexOf('.'));
                return beanName;
            }

        }

        return null;
    }

    public boolean isDefferedExecution() {
        return isDefferedExecution;
    }

    protected String getPropertyBeingTypedName() {
        String elExp = getExpression();
        int dotPos = elExp.lastIndexOf('.');

        return dotPos == -1 ? null : elExp.substring(dotPos + 1);
    }

    static String getPropertyName(String methodName, int prefixLength) {
        String propertyName = methodName.substring(prefixLength);
        String propertyNameWithoutFL = propertyName.substring(1);

        if (propertyNameWithoutFL.length() > 0) {
            if (propertyNameWithoutFL.equals(propertyNameWithoutFL.toUpperCase())) {
                //property is in uppercase
                return propertyName;
            }

        }

        return Character.toLowerCase(propertyName.charAt(0)) + propertyNameWithoutFL;
    }

    protected abstract class BaseELTaskClass {

        protected String beanType;

        public BaseELTaskClass(String beanType) {
            this.beanType = beanType;
        }

        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo parameter) {
            if (beanType == null) {
                return null;
            }

            TypeElement lastKnownType = parameter.getElements().getTypeElement(beanType);

            String parts[] = getExpression().split("\\.");
            // part[0] - the bean
            // part[parts.length - 1] - the property being typed (if not empty)

            int limit = parts.length - 1;

            if (getPropertyBeingTypedName().length() == 0) {
                limit += 1;
            }

            for (int i = 1; i < limit; i++) {
                if (lastKnownType == null) {
                    logger.fine("EL CC: Could not resolve type for property " //NOI18N
                            + parts[i] + " in " + getExpression()); //NOI18N

                    return null;
                }

                String accessorName = getAccessorName(parts[i]);
                List<ExecutableElement> allMethods = ElementFilter.methodsIn(lastKnownType.getEnclosedElements());
                lastKnownType = null;

                for (ExecutableElement method : allMethods) {
                    if (accessorName.equals(method.getSimpleName().toString())) {
                        TypeMirror returnType = method.getReturnType();

                        if (returnType.getKind() == TypeKind.DECLARED) { // should always be true
                            lastKnownType = (TypeElement) parameter.getTypes().asElement(returnType);
                            break;
                        }
                    }

                }
            }

            return lastKnownType;
        }

        protected String getAccessorName(String propertyName) {
            // we do not have to handle "is" type accessors here
            return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }

        /**
         * @return property name is <code>accessorMethod<code> is property accessor, otherwise null
         */
        protected String getExpressionSuffix(ExecutableElement method) {

            if (method.getModifiers().contains(Modifier.PUBLIC) && method.getParameters().size() == 0) {
                String methodName = method.getSimpleName().toString();

                if (methodName.startsWith("get")) { //NOI18N
                    return getPropertyName(methodName, 3);
                }

                if (methodName.startsWith("is")) { //NOI18N
                    return getPropertyName(methodName, 2);
                }

                if (isDefferedExecution()) {
                    //  also return values for method expressions

                    if ("java.lang.String".equals(method.getReturnType().toString())) { //NOI18N
                        return methodName;
                    }
                }
            }

            return null; // not a property accessor
        }

        public void cancel() {
        }
    }

    /**
     * Go to the java source code of expression
     * - a getter in case of
     */
    private class GoToSourceTask extends BaseELTaskClass implements CancellableTask<CompilationController> {

        private boolean success = false;

        GoToSourceTask(String beanType) {
            super(beanType);
        }

        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement bean = getTypePreceedingCaret(parameter);

            if (bean != null) {
                String suffix = getPropertyBeingTypedName();

                for (ExecutableElement method : ElementFilter.methodsIn(bean.getEnclosedElements())) {
                    String propertyName = getExpressionSuffix(method);

                    if (propertyName != null && propertyName.equals(suffix)) {
                        success = UiUtils.open(parameter.getClasspathInfo(), method);
                        break;
                    }
                }
            }
        }

        public boolean wasSuccessful() {
            return success;
        }
    }

    private class PropertyCompletionItemsTask extends BaseELTaskClass implements CancellableTask<CompilationController> {

        private List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        private int anchorOffset;

        PropertyCompletionItemsTask(String beanType, int anchor) {
            super(beanType);
            this.anchorOffset = anchor;
        }

        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            TypeElement bean = getTypePreceedingCaret(parameter);

            if (bean != null) {
                String prefix = getPropertyBeingTypedName();

                for (ExecutableElement method : ElementFilter.methodsIn(parameter.getElements().getAllMembers(bean))) {
                    String propertyName = getExpressionSuffix(method);

                    if (propertyName != null && propertyName.startsWith(prefix)) {
                        boolean isMethod = propertyName.equals(method.getSimpleName().toString());
                        String type = isMethod ? "" : method.getReturnType().toString(); //NOI18N

                        CompletionItem item = ElCompletionItem.createELProperty(propertyName, anchorOffset, type);

                        completionItems.add(item);
                    }
                }
            }
        }

        public List<CompletionItem> getCompletionItems() {
            return completionItems;
        }
    }

    /** Return context, whether the expression is about a bean, implicit object or
     *  function.
     */
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        int value = EL_UNKNOWN;

        if (bracketIndex == -1 && dotIndex > -1) {
            String first = expr.substring(0, dotIndex);
            BeanData[] beans = sup.getBeanData();
            if (beans != null) {
                for (int i = 0; i <
                        beans.length; i++) {
                    if (beans[i].getId().equals(first)) {
                        value = EL_BEAN;
                        continue;

                    }




                }
            }
            if (value == EL_UNKNOWN && ELImplicitObjects.getELImplicitObjects(first).size() > 0) {
                value = EL_IMPLICIT;
            }

        } else if (bracketIndex == -1 && dotIndex == -1) {
            value = EL_START;
        }

        return value;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getReplace() {
        return replace;
    }
}
