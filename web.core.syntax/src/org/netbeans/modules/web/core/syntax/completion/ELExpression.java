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
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.el.lexer.api.ELTokenId;
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
    public static final int EL_IMPLICIT =3;
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
    public int parse(int offset){
        String value = null;
        int result = NOT_EL;
        boolean middle;
        
        BaseDocument document = sup.getDocument();
        document.readLock();
        try {
            
            int tunedOffset = offset > 0 ? offset-1 : offset;
            
            TokenHierarchy hi = TokenHierarchy.get(document);
            TokenSequence ts = JspSyntaxSupport.tokenSequence(hi, ELTokenId.language(), tunedOffset);
            if(ts == null) {
                //no EL token sequence
                return EL_UNKNOWN;
            }
            int diff = ts.move(tunedOffset);
            if(diff == Integer.MAX_VALUE) {
                return EL_START; //TODO: why?
            }
            
            // Find the start of the expression. It doesn't have to be an EL delimiter (${ #{)
            // it can be start of the function or start of a simple expression.
            Token<ELTokenId> token = ts.token();
            while (token.id() != ELTokenId.LPAREN
                    && token.id() != ELTokenId.WHITESPACE
                    && (!token.id().language().nonPrimaryTokenCategories(token.id()).contains(ELTokenId.ELTokenCategories.KEYWORDS.name())
                    || token.id().language().nonPrimaryTokenCategories(token.id()).contains(ELTokenId.ELTokenCategories.NUMERIC_LITERALS.name()))) {
                token = ts.token();
                if (value == null){
                    value = token.text().toString();
                    if (token.id() == ELTokenId.DOT){
                        replace="";
                        middle = true;
                    } else if (token.text().length() >= (offset-token.offset(hi))){
                        value = value.substring(0, offset-token.offset(hi));
                        replace = value;
                    }
                } else {
                    value = token.text().toString() + value;
                    if (token.id() == ELTokenId.TAG_LIB_PREFIX)
                        replace = value;
                }
                if(!ts.movePrevious()) {
                    break; //break the loop, we are on the beginning of the EL token sequence
                }
            }
            if (token.id() != ELTokenId.IDENTIFIER && token.id() != ELTokenId.TAG_LIB_PREFIX ) {
                value = null;
            } else
                if (token.id() == ELTokenId.WHITESPACE || token.id() == ELTokenId.LPAREN) {
                    result = EL_START;
                } else
                    if (value != null){
                        result = findContext(value);
                    }
            
            // move to the beginning 
            TokenSequence jspTokenSequence = hi.tokenSequence();
            
            if (jspTokenSequence.move(token.offset(hi)) != Integer.MAX_VALUE){
                isDefferedExecution = jspTokenSequence.token().text().toString().startsWith("#{"); //NOI18N
            }
            
        } finally {
            document.readUnlock();
        }
        expression = value;
        return result;
    }
    
    public List<CompletionItem> getPropertyCompletionItems(String beanType){
        PropertyCompletionItemsTask task = new PropertyCompletionItemsTask(beanType);
        runTask(task);
        
        return task.getCompletionItems();
    }
    
    public void gotoPropertyDeclaration(String beanType){
        GoToSourceTask task = new GoToSourceTask(beanType);
        runTask(task);
    }
    
    /**
     *  @return the class of the top-level object used in the expression
     */
    public String getObjectClass(){
        String beanName = extractBeanName();
        
        BeanData[] allBeans = sup.getBeanData();
        for (BeanData beanData : allBeans) {
            if (beanData.getId().equals(beanName)){
                return beanData.getClassName();
            }
        }
        
        // not found within declared beans, try implicit objects
        ELImplicitObjects.ELImplicitObject implObj = ELImplicitObjects.getELImplicitObject(beanName);
        
        if (implObj != null){
            return implObj.getClazz();
        }
        
        return null;
    }
    
    private void runTask(CancellableTask task){
        ClasspathInfo cpInfo = ClasspathInfo.create(sup.getFileObject());
        JavaSource source = JavaSource.create(cpInfo, Collections.EMPTY_LIST);
        
        try{
            source.runUserActionTask(task, true);
        } catch (IOException e){
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String extractBeanName(){
        String elExp = getExpression();
        
        if (elExp != null && !elExp.equals("")){
            if (elExp.indexOf('.')> -1){
                String beanName = elExp.substring(0,elExp.indexOf('.'));
                return beanName;
            }
        }
        
        return null;
    }
    
    public boolean isDefferedExecution(){
        return isDefferedExecution;
    }
    
    private String getPropertyBeingTypedName(){
        String elExp = getExpression();
        int dotPos = elExp.lastIndexOf('.');
        
        return dotPos == -1 ? null : elExp.substring(dotPos + 1);
    }
    
    private abstract class BaseELTaskClass{
        protected String beanType;
        
        BaseELTaskClass(String beanType){
            this.beanType = beanType;
        }
        
        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo parameter){
            if (beanType == null){
                return null;
            }
            
            TypeElement lastKnownType = parameter.getElements().getTypeElement(beanType);
            
            String parts[] = getExpression().split("\\.");
            // part[0] - the bean
            // part[parts.length - 1] - the property being typed (if not empty)
            
            int limit = parts.length - 1;
            
            if (getPropertyBeingTypedName().length() == 0){
                limit += 1;
            }
            
            for (int i = 1; i < limit; i ++){
                if (lastKnownType == null){
                    logger.fine("EL CC: Could not resolve type for property " //NOI18N
                            + parts[i] + " in " + getExpression()); //NOI18N
                    
                    return null;
                }
                
                String accessorName = getAccessorName(parts[i]);
                List<ExecutableElement> allMethods = ElementFilter.methodsIn(lastKnownType.getEnclosedElements());
                lastKnownType = null;
                
                for (ExecutableElement method : allMethods){
                    if (accessorName.equals(method.getSimpleName().toString())){
                        TypeMirror returnType = method.getReturnType();
                        
                        if (returnType.getKind() == TypeKind.DECLARED){ // should always be true
                            lastKnownType = (TypeElement) parameter.getTypes().asElement(returnType);
                            break;
                        }
                    }
                    
                }
            }
            
            return lastKnownType;
        }
        
        protected String getAccessorName(String propertyName){
            // we do not have to handle "is" type accessors here
            return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        }
        
        /**
         * @return property name is <code>accessorMethod<code> is property accessor, otherwise null
         */
        protected String getExpressionSuffix(ExecutableElement method){
            
            if (method.getModifiers().contains(Modifier.PUBLIC)
                    && method.getParameters().size() == 0){
                String methodName = method.getSimpleName().toString();
                
                if (methodName.startsWith("get")){ //NOI18N
                    return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                }
                
                if (methodName.startsWith("is")){ //NOI18N
                    return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
                }
                
                if (isDefferedExecution()){
                    //  also return values for method expressions
                    
                    if ("java.lang.String".equals(method.getReturnType().toString())){ //NOI18N
                        return methodName;
                    }
                }
            }
            
            return null; // not a property accessor
        }
        
        public void cancel() {}
    }
    
    /**
     * Go to the java source code of expression
     * - a getter in case of 
     */
    private class GoToSourceTask extends BaseELTaskClass implements CancellableTask<CompilationController>{
        GoToSourceTask(String beanType){
            super(beanType);
        }
        
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement bean = getTypePreceedingCaret(parameter);
            
            if (bean != null){
                String suffix = getPropertyBeingTypedName();
                
                for (ExecutableElement method : ElementFilter.methodsIn(bean.getEnclosedElements())){
                    String propertyName = getExpressionSuffix(method);
                    
                    if (propertyName != null && propertyName.equals(suffix)){
                        UiUtils.open(parameter.getClasspathInfo(), method);
                        break;
                    }
                }
            }
        }
    }
    
    private class PropertyCompletionItemsTask extends BaseELTaskClass implements CancellableTask<CompilationController>{
        
        private List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        
        PropertyCompletionItemsTask(String beanType){
            super(beanType);
        }
        
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
            TypeElement bean = getTypePreceedingCaret(parameter);
            
            if (bean != null){
                String prefix = getPropertyBeingTypedName();
                
                for (ExecutableElement method : ElementFilter.methodsIn(bean.getEnclosedElements())){
                    String propertyName = getExpressionSuffix(method);
                    
                    if (propertyName != null && propertyName.startsWith(prefix)){
                        boolean isMethod = propertyName.equals(method.getSimpleName().toString());
                        String type = isMethod ? "" : method.getReturnType().toString(); //NOI18N
                        
                        CompletionItem item = new JspCompletionItem.ELProperty(
                                propertyName, type);
                        
                        completionItems.add(item);
                    }
                }
            }
        }
        
        public List<CompletionItem> getCompletionItems(){
            return completionItems;
        }
    }
    
    /** Return context, whether the expression is about a bean, implicit object or
     *  function.
     */
    protected int findContext(String expr){
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        int value = EL_UNKNOWN;
        
        if (bracketIndex == -1 && dotIndex > -1){
            String first = expr.substring(0, dotIndex);
            BeanData[] beans = sup.getBeanData();
            if (beans != null) {
                for (int i = 0; i < beans.length; i++)
                    if (beans[i].getId().equals(first)){
                        value = EL_BEAN;
                        continue;
                    }
            }
            if (value == EL_UNKNOWN && ELImplicitObjects.getELImplicitObjects(first).size()>0)
                value = EL_IMPLICIT;
        } else if (bracketIndex == -1 && dotIndex == -1)
            value = EL_START;
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
