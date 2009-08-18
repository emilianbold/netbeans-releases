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
package org.netbeans.modules.web.core.syntax.completion.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.el.lexer.api.ELTokenId.ELTokenCategories;
import org.netbeans.modules.web.core.syntax.completion.ELImplicitObjects;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.openide.filesystems.FileObject;

/** 
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 * @author ads
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
    private String replace;
    private boolean isDefferedExecution = false;
    private Document doc;
    
    /** EL expression is attribute value */
    private boolean isAttribute;
    /** This string contains attribute value prefix ( before EL ) 
     * if EL expression is attribute value. 
     */
    private String myAttributeValue;

    public ELExpression(Document doc) {
        this.doc = doc;
        this.replace = "";
    }

    /** Parses text before offset in the document. Doesn't parse after offset.
     *  It doesn't parse whole EL expression until ${ or #{, but just simple expression.
     *  For example ${ 2 < bean.start }. If the offset is after bean.start, then only bean.start
     *  is parsed.
     */
    public int parse(int offset) {
        BaseDocument document = (BaseDocument) doc;
        String value = null;
        document.readLock();
        try {
            TokenHierarchy<BaseDocument> hi = TokenHierarchy.get(document);
            //find EL token sequence and its superordinate sequence
            TokenSequence<?> ts = hi.tokenSequence();
            TokenSequence<?> last = null;
            for (;;) {
                if (ts == null) {
                    break;
                }
                if (ts.language() == ELTokenId.language()) {
                    //found EL
                    isDefferedExecution = last.token().text().toString().startsWith("#{"); //NOI18N
                    if ( last.movePrevious() ){
                        if ( JspTokenId.ATTR_VALUE == last.token().id() ){
                            isAttribute = true;
                            myAttributeValue = last.token().text().toString();
                        }
                    }
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

            if (ts == null) {
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
            Token<?> token = ts.token();
            while ((!ELTokenCategories.OPERATORS.hasCategory(ts.token().id()) 
                    || ts.token().id() == ELTokenId.DOT || 
                        ts.token().id() == ELTokenId.LBRACKET
                        || ts.token().id() == ELTokenId.RBRACKET) &&
                    ts.token().id() != ELTokenId.WHITESPACE &&
                    (!ELTokenCategories.KEYWORDS.hasCategory(ts.token().id()) ||
                    ELTokenCategories.NUMERIC_LITERALS.hasCategory(ts.token().id()))) 
            {

                //repeat until not ( and ' ' and keyword or number
                if (value == null) {
                    value = ts.token().text().toString();
                    if (ts.token().id() == ELTokenId.DOT || 
                            ts.token().id() == ELTokenId.LBRACKET) 
                    {
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

            if (ELTokenCategories.OPERATORS.hasCategory(token.id() )
                    || token.id() == ELTokenId.WHITESPACE || token.id() == ELTokenId.LPAREN) 
            {
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

    public List<CompletionItem> getPropertyCompletionItems(String beanType, 
            int anchor) 
    {
        CompletionInfo task = getPropertyCompletionInfo(beanType, anchor);
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

        // not found within declared beans, try implicit objects
        ELImplicitObjects.ELImplicitObject implObj = ELImplicitObjects.getELImplicitObject(beanName);

        if (implObj != null) {
            return implObj.getClazz();
        }

        return null;
    }
    
    protected CompletionInfo getPropertyCompletionInfo(String beanType, int anchor) {
        return new PropertyCompletionItemsTask(beanType, anchor);
    }

    protected FileObject getFileObject() {
        return DataLoadersBridge.getDefault().getFileObject(doc);
    }
            

    protected void runTask(CancellableTask<CompilationController> task) {
        
        if(getFileObject() == null) {
            return ;
        }
        ClasspathInfo cpInfo = ClasspathInfo.create(getFileObject());
        JavaSource source = JavaSource.create(cpInfo, Collections.EMPTY_LIST);

        try {
            source.runUserActionTask(task, true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    protected String extractBeanName() {
        String elExp = getExpression();

        if (elExp != null && !elExp.equals("")) {
            int dotIndex =  elExp.indexOf('.');             // NOI18N
            int bracketIndex = elExp.indexOf('[');          // NOI18N
            if (dotIndex > -1 || bracketIndex >-1) {
                String beanName = elExp.substring(0, getPositiveMin( dotIndex, 
                        bracketIndex));
                return beanName;
            }

        }

        return null;
    }
    
    protected int getPositiveMin( int a , int b ){
        if ( a < 0 ){
            return b>=0? b : 0;
        }
        else if ( b<0 ){
            return a;
        }
        else {
            return Math.min(a, b);
        }
    }

    public boolean isDefferedExecution() {
        return isDefferedExecution;
    }

    protected String getPropertyBeingTypedName() {
        String elExp = getExpression();
        int dotPos = elExp.lastIndexOf('.');            // NOI18N
        int bracketIndex = elExp.lastIndexOf('[');          // NOI18N

        if ( bracketIndex >-1 || dotPos >-1 ){
            return elExp.substring( Math.max( bracketIndex, dotPos) + 1);
        }
        return null;
    }
    
    protected String removeQuotes( String propertyName ) {
        if ( propertyName.length() >0 ){
            char first = propertyName.charAt(0);
            if ( (first == '"' || first == '\'' )&& propertyName.length() >1 
                    && propertyName.charAt( propertyName.length()-1) == first)
            {
                return propertyName.substring( 1 , propertyName.length() -1 );
            }
            else if ( first == '\\'){
                if ( propertyName.length() >=4 ){
                    char second = propertyName.charAt(1);
                    if ( (second == '"' || second =='\'') && 
                            propertyName.charAt( propertyName.length()-1)==second)
                    {
                        if ( propertyName.charAt( propertyName.length() -2 ) 
                                == '\\')
                        {
                            propertyName = propertyName.substring( 2 , 
                                    propertyName.length() -2);
                        }
                    }
                }
            }
        }
        return propertyName;
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
    
    private String[] getParts() {
        if ( getExpression().indexOf('[') == -1 ){
            return getExpression().split("\\.");            // NOI18N
        }
        List<String> result = new LinkedList<String>();
        boolean previousDot = false;
        boolean previousLeftBracket = false;
        String expression = getExpression();
        int i=0;
        while( expression.length() > 0 ){
            char ch = expression.charAt( i );
            if ( ch == '.'){
                if ( previousLeftBracket ){
                    addPart(result, expression.substring(i+1));
                    break;
                }
                previousDot = true;
                addPart(result,  expression.substring( 0 , i ));
                expression = expression.substring( i+1);
                i=0;
                continue;
            }
            if ( ch == '['){
                if ( previousLeftBracket ){
                    addPart(result,  expression.substring(i+1) );
                    break;
                }
                if ( previousDot ){
                    previousDot = false;
                }
                previousLeftBracket = true;
                addPart(result,  expression.substring( 0 , i ));
                int index = expression.indexOf(']');
                if ( index == -1 ){
                    addPart(result,  expression.substring(i+1) );
                    break;
                }
                else {
                    addPart(result, removeQuotes( expression.substring(i+1, index )));
                    expression = expression.substring( index +1);
                    i=0;
                    continue;
                }
            }
            i++;
        }
        return result.toArray(new String[result.size()] );
    }
    
    private void addPart(List<String> parts, String part ){
        if ( part != null && part.length() != 0 ){
            parts.add(part);
        }
    }
    
    protected abstract class BaseELTaskClass {

        protected String beanType;

        public BaseELTaskClass(String beanType) {
            this.beanType = beanType;
        }

        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo controller) {
            if (beanType == null) {
                return null;
            }

            TypeElement lastKnownType = controller.getElements().
                getTypeElement(beanType);
            TypeMirror lastReturnType = null;

            String parts[] = getParts();
            // part[0] - the bean
            // part[parts.length - 1] - the property being typed (if not empty)

            int limit = parts.length - 1;

            if (getPropertyBeingTypedName().length() == 0) {
                limit += 1;
            }

            parts:
            for (int i = 1; i < limit; i++) {
                if (lastKnownType == null && lastReturnType == null) {
                    logger.fine("EL CC: Could not resolve type for property " //NOI18N
                            + parts[i] + " in " + getExpression()); //NOI18N

                    return null;
                }
                if (lastKnownType != null) {
                    String accessorName = getAccessorName(parts[i]);
                    List<ExecutableElement> allMethods = ElementFilter
                            .methodsIn(lastKnownType.getEnclosedElements());
                    
                    lastKnownType = null;

                    for (ExecutableElement method : allMethods) {
                        if (accessorName.equals(method.getSimpleName()
                                .toString()))
                        {
                            TypeMirror returnType = method.getReturnType();
                            lastReturnType = returnType;

                            if (returnType.getKind() == TypeKind.DECLARED) { // should always be true
                                lastKnownType = (TypeElement) controller
                                        .getTypes().asElement(returnType);
                                break;
                            }
                            else if (returnType.getKind() == TypeKind.ARRAY) {
                                continue parts;
                            }
                        }

                    }
                }
                if ( lastKnownType== null  && lastReturnType != null ) 
                {
                    /* 
                     * property name could be:
                     * 1) index ( in array or collection )
                     * 2) key in hash map 
                     */
                    if ( lastReturnType.getKind() == TypeKind.ARRAY)
                    {
                        TypeMirror typeMirror = ((ArrayType)lastReturnType).
                            getComponentType();
                        if ( typeMirror.getKind() == TypeKind.DECLARED){
                            lastKnownType = (TypeElement) controller.getTypes().
                                asElement(typeMirror);
                        }
                        else if ( typeMirror.getKind() == TypeKind.ARRAY){
                            lastReturnType = typeMirror;
                            continue;
                        }
                    }
                    else if ( controller.getTypes().isAssignable( 
                            controller.getTypes().erasure(lastReturnType), 
                                controller.getElements().getTypeElement(
                                        List.class.getCanonicalName()).asType()))
                    {
                        if ( lastReturnType instanceof DeclaredType ){
                            List<? extends TypeMirror> typeArguments = 
                                ((DeclaredType)lastReturnType).getTypeArguments();
                            if ( typeArguments.size() != 0 ){
                                TypeMirror typeMirror = typeArguments.get(0);
                                if ( typeMirror.getKind() == TypeKind.DECLARED){
                                    lastKnownType = (TypeElement) controller.getTypes().
                                        asElement( typeMirror );
                                }
                            }
                        }
                        if ( lastKnownType == null ){
                            lastKnownType = controller.getElements().
                                getTypeElement(Object.class.getCanonicalName());
                        }
                    }
                    else if (controller.getTypes().isAssignable(
                            controller.getTypes().erasure(lastReturnType),
                                controller.getElements().getTypeElement(
                                        Map.class.getCanonicalName()).asType()))
                    {
                        if (lastReturnType instanceof DeclaredType) {
                            List<? extends TypeMirror> typeArguments = 
                                ((DeclaredType) lastReturnType)
                                    .getTypeArguments();
                            if (typeArguments.size() == 2) {
                                TypeMirror typeMirror = typeArguments.get(1);
                                if (typeMirror.getKind() == TypeKind.DECLARED) {
                                    lastKnownType = (TypeElement) controller
                                            .getTypes().asElement(typeMirror);
                                }
                            }
                        }
                        if (lastKnownType == null) {
                            lastKnownType = controller.getElements()
                                    .getTypeElement(
                                            Object.class.getCanonicalName());
                        }
                    }
                    lastReturnType = null;
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
    private class GoToSourceTask extends BaseELTaskClass implements 
        CancellableTask<CompilationController> 
    {

        private boolean success = false;

        GoToSourceTask(String beanType) {
            super(beanType);
        }

        public void run(CompilationController controller) throws Exception {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            TypeElement bean = getTypePreceedingCaret(controller);

            if (bean != null) {
                String suffix = removeQuotes(getPropertyBeingTypedName());

                for (ExecutableElement method : ElementFilter.methodsIn(
                        bean.getEnclosedElements())) 
                {
                    String propertyName = getExpressionSuffix(method);

                    if (propertyName != null && propertyName.equals(suffix)) {
                        success = UiUtils.open(controller.getClasspathInfo(), method);
                        break;
                    }
                }
            }
        }

        public boolean wasSuccessful() {
            return success;
        }
    }
    
    protected interface CompletionInfo extends CancellableTask<CompilationController> {
        
        List<CompletionItem> getCompletionItems();
        
        String getTypeOnCaretQualifiedName();
    }

    private class PropertyCompletionItemsTask extends BaseELTaskClass 
        implements  CompletionInfo 
    {

        private List<CompletionItem> completionItems = new ArrayList<CompletionItem>();
        private int anchorOffset;

        PropertyCompletionItemsTask(String beanType, int anchor) {
            super(beanType);
            this.anchorOffset = anchor;
        }

        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
            TypeElement bean = getTypePreceedingCaret(controller);

            if (bean != null) {
                myQName = bean.getQualifiedName().toString();
                String prefix = getPropertyBeingTypedName();
                char firstChar =0;
                if ( prefix.length()> 0){
                    firstChar = prefix.charAt(0);
                }
                if ( firstChar == '"' || firstChar == '\''){
                    prefix = prefix.substring( 1 );
                }

                for (ExecutableElement method : ElementFilter.methodsIn(
                        controller.getElements().getAllMembers(bean))) 
                {
                    String propertyName = getExpressionSuffix(method);

                    if (propertyName != null && propertyName.startsWith(prefix)) {
                        boolean isMethod = propertyName.equals(method.getSimpleName().toString());
                        String type = isMethod ? "" : method.getReturnType().toString(); //NOI18N
                        CompletionItem item = ElCompletionItem.createELProperty(
                                propertyName, getInsert( propertyName , firstChar), 
                                anchorOffset, type);

                        completionItems.add(item);
                    }
                }
            }
        }

        private String getInsert( String propertyName , char startChar ) {
            int bracketIndex = getExpression().lastIndexOf("[");    // NOI18N
            int dotIndex = getExpression().lastIndexOf(".");        // NOI18N
            
            if ( dotIndex < bracketIndex ){
                String quote = null;
                if ( startChar == '"' || startChar =='\''){
                    quote = ""+startChar;
                }
                else if ( isAttribute && myAttributeValue!= null && 
                        myAttributeValue.length() > 0 )
                {
                    char firstChar = myAttributeValue.charAt( 0 );
                    if ( firstChar == '"'){
                        if ( myAttributeValue.indexOf("'")!=-1){
                            quote = "\\\"";                         // NOI18N
                        }
                        else {
                            quote ="'";                             // NOI18N
                        }
                    }
                    else if( firstChar == '\''){
                        if ( myAttributeValue.indexOf('"')!=-1){
                            quote = "\\'";                         // NOI18N
                        }
                        else {
                            quote ="\"";                           // NOI18N
                        }
                    }
                }
                if ( quote == null ){
                    quote = "\"";                   // NOI18N
                }
                StringBuilder builder = new StringBuilder( quote);
                builder.append( propertyName );
                builder.append( quote );
                builder.append("]");                    // NOI18N
                return builder.toString();
            }
            return propertyName;
        }

        public List<CompletionItem> getCompletionItems() {
            return completionItems;
        }
        
        public String getTypeOnCaretQualifiedName() {
            return myQName;
        }
        
        private String myQName;
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
