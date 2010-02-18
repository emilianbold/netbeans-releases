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
package org.netbeans.modules.web.core.syntax.completion.api;

import javax.lang.model.type.WildcardType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
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
import org.netbeans.modules.web.core.syntax.spi.ELImplicitObject;
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
    protected String expression;
    protected String resolvedExpression;

    private String replace;
    private boolean isDefferedExecution = false;
    private Document doc;
    
    private int myParseType = -1;
    
    /** EL expression is attribute value */
    private boolean isAttribute;
    /** This string contains attribute value prefix ( before EL ) 
     * if EL expression is attribute value. 
     */
    private String myAttributeValue;
    
    /**
     * @author ads
     * Lexer for facelet file doesn't inform you about attribute.
     * So  I have added this attribute which contains token text with 
     * context EL inside.
     */
    private String myXhtmlToken;
    
    private int contextOffset = -1;
    private int myStartOffset = -1;

    public ELExpression(Document doc) {
        this.doc = doc;
        this.replace = "";
    }
    
    /**
     * Expression could contain operations, keywords, ...
     * In this case context expression divided to several parts by such operations, ...
     * In this case start offset is the beginning of such part EL which 
     * includes context offset.   
     * @return start index of context expression
     */
    public int getStartOffset(){
        return myStartOffset;
    }
    
    public int getContextOffset() {
        return contextOffset;
    }

    public Document getDocument() {
        return doc;
    }
    
    public final int parse(final int offset) {
        final int[] retval = new int[1];
        ((BaseDocument)doc).render(new Runnable() {
            public void run() {
                retval[0] = doParse(offset);
            }
        });
        myParseType = retval[0];
        return myParseType;
    }
    
    public final int getParseType(){
        return myParseType;
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
        ELImplicitObject implObj = ELImplicitObjects.getELImplicitObject(beanName,
                this );

        if (implObj != null) {
            return implObj.getClazz();
        }

        return null;
    }
    
    public String getBeanName(){
        return extractBeanName();
    }
    
    public FileObject getFileObject() {
        return DataLoadersBridge.getDefault().getFileObject(doc);
    }
    
    protected CompletionInfo getPropertyCompletionInfo(String beanType, int anchor) {
        return new PropertyCompletionItemsTask(beanType, anchor);
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
        return extractBeanName(getExpression());
    }

    protected String extractBeanName(String elExp) {
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

    public String getPropertyBeingTypedName() {
        String elExp = getExpression();
        int dotPos = elExp.lastIndexOf('.');            // NOI18N
        int lBracketIndex = elExp.lastIndexOf('[');          // NOI18N
        int rBracketIndex = elExp.lastIndexOf(']');          // NOI18N

        /*
         * Fix for IZ#172413 - Unexpected error in EL with dot inside array notation
         */
        if ( rBracketIndex <lBracketIndex   ){
            return elExp.substring( lBracketIndex+ 1);
        }
        else if ( lBracketIndex > -1 && dotPos < rBracketIndex ){ 
            return elExp.substring(lBracketIndex + 1);
        }
        else if (dotPos >-1 ){
            return elExp.substring( dotPos + 1);
        }
        return null;
    }
    
    public String removeQuotes( String propertyName ) {
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
    
    public String getInsert( String propertyName , char startChar ) {
        int lBracketIndex = getExpression().lastIndexOf('[');
        int rBracketIndex = getExpression().lastIndexOf(']');
        
        if ( rBracketIndex < lBracketIndex  ){
            String quote = null;
            if ( startChar == '"' || startChar =='\''){
                quote = ""+startChar;
            }
            else if ( isAttribute && myAttributeValue!= null && 
                    myAttributeValue.length() > 0 )
            {
                quote = getQuote(  myAttributeValue.charAt( 0 ) ,
                        myAttributeValue );
            }
            else if ( myXhtmlToken != null ){
                int dQuoteIndex = myXhtmlToken.lastIndexOf('"');
                int sQuoteIndex = myXhtmlToken.lastIndexOf('\'');
                if ( sQuoteIndex> dQuoteIndex ){
                    quote = getQuote( '\'', myXhtmlToken);
                }
                else if ( dQuoteIndex>=0 ){
                    quote = getQuote( '"', myXhtmlToken);
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

    private String getQuote( char ch, String text ) {
        if ( ch == '"'){
            if ( text.indexOf("'")!=-1){
                return "\\\"";                         // NOI18N
            }
            else {
                return "'";                             // NOI18N
            }
        }
        else if( ch == '\''){
            if ( text.indexOf('"')!=-1){
                return "\\'";                         // NOI18N
            }
            else {
                return "\"";                           // NOI18N
            }
        }
        return null;
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
    
    private void setContextOffset(int offset) {
        this.contextOffset = offset;
    }
    
    protected Part[] getParts(){
        return getParts(getExpression());
    }
    
    protected Part[] getParts( final String expr ) {
        List<Part> result = new LinkedList<Part>();
        String expression = expr;
        if  ( expr.indexOf('[') == -1 ){
            String[] parts = expression.split( "\\." );     // NOI18N
            int offset = 0;
            for (int i=0; i<parts.length; i++ ) {
                result.add( new Part( offset, parts[i] ));
                offset = offset + parts[i].length() +1;
            }
            return result.toArray(new Part[result.size()] );
        }
        boolean previousDot = false;
        boolean previousLeftBracket = false;
        int i=0;
        int offset = 0;
        while( expression.length() > 0 && i < expression.length()){
            char ch = expression.charAt( i );
            if ( ch == '.'){
                if ( previousLeftBracket ){
                    addPart(result, expression.substring(i+1), i+offset +1);
                    break;
                }
                previousDot = true;
                String part = expression.substring( 0 , i );
                addPart(result, part  , offset);
                offset = offset+part.length()+1;
                expression = expression.substring( i+1);
                i=0;
                continue;
            }
            if ( ch == '['){
                if ( previousLeftBracket ){
                    addPart(result,  expression.substring(i+1) , i+offset+1);
                    break;
                }
                if ( previousDot ){
                    previousDot = false;
                }
                previousLeftBracket = true;
                String part =  expression.substring( 0 , i );
                addPart(result,  part , offset );
                offset = offset + part.length()+1;
                int index = expression.indexOf(']');
                if ( index == -1 ){
                    addPart(result,  expression.substring(i+1) , offset );
                    break;
                }
                else {
                    part = expression.substring(i+1, index );
                    String unquoted = removeQuotes( part );
                    int prefixLength = part.indexOf( unquoted );
                    addPart(result, unquoted , offset + prefixLength );
                    expression = expression.substring( index +1);
                    offset = offset + part.length() + 1;
                    previousLeftBracket = false;
                    i=0;
                    continue;
                }
            }
            i++;
        }
        /*
         *  In the end there can be case when dot was 
         *  last character ( from "." , "[", "]" list ).
         *  So we need to add current ( modified ) expression as part 
         */
        if ( previousDot ){
            addPart(result, expression, getExpression().length() - expression.length());
        }
        return result.toArray(new Part[result.size()] );
    }
    
    private void addPart(List<Part> parts, String part , int offset){
        if ( part != null && part.length() != 0 ){
            parts.add(new Part( offset, part));
        }
    }
    
    /** Parses text before offset in the document. Doesn't parse after offset.
     *  It doesn't parse whole EL expression until ${ or #{, but just simple expression.
     *  For example ${ 2 < bean.start }. If the offset is after bean.start, then only bean.start
     *  is parsed.
     */
    private final int doParse(int offset) {
        setContextOffset(offset);

        BaseDocument document = (BaseDocument) doc;
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
                    /*
                     *  This is a little hack . I don't know why NoClassDefFoundError
                     *  appears in runtime. Compilation works perfectly.
                     */
                    else if ( last.token().id().toString().equals("HTML")
                            && last.language().mimeType().equals("text/xhtml"))// NOI18N
                    {
                        myXhtmlToken = last.token().text().toString();
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
        boolean rBracket = false;
        while (rBracket ||
                (!ELTokenCategories.OPERATORS.hasCategory(ts.token().id())
                || ts.token().id() == ELTokenId.DOT ||
                    ts.token().id() == ELTokenId.LBRACKET
                    || ts.token().id() == ELTokenId.RBRACKET) &&
                ts.token().id() != ELTokenId.WHITESPACE &&
                (!ELTokenCategories.KEYWORDS.hasCategory(ts.token().id()) ||
                ELTokenCategories.NUMERIC_LITERALS.hasCategory(ts.token().id())))
        {
            if ( ts.token().id() == ELTokenId.RBRACKET ){
                rBracket = true;
            }
            else if ( ts.token().id() == ELTokenId.LBRACKET ){
                rBracket = false;
            }

            //repeat until not ( and ' ' and keyword or number
            if (expression == null) {
                expression = ts.token().text().toString();
                if ( ts.token().id() == ELTokenId.DOT  ||
                        ts.token().id() == ELTokenId.LBRACKET)
                {
                    replace = "";
                } else if (ts.token().text().length() >= (offset - ts.token().offset(hi))) {
                    if (ts.token().offset(hi) <= offset) {
                        expression = expression.substring(0, offset - ts.token().offset(hi));
                        replace = expression;
                    } else {
                        // cc invoked within EL delimiter
                        return NOT_EL;
                    }
                }
            } else {
                expression = ts.token().text().toString() + expression;
                if (ts.token().id() == ELTokenId.TAG_LIB_PREFIX) {
                    replace = expression;
                }
            }
            token = ts.token();
            myStartOffset = ts.offset();
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
            expression = null;
        } else if (expression != null) {
            return findContext(expression); //can modify the expression field!
        }
        return NOT_EL;
    }
    
    public static class Part {

        Part( int index , String part ){
            myIndex = index;
            myPart = part;
        }
        
        public String getPart(){
            return myPart;
        }
        
        public int getIndex(){
            return myIndex;
        }
        private int myIndex;
        private String myPart;
    }
    
    public abstract class InspectPropertiesTask extends BaseELTaskClass implements
            CancellableTask<CompilationController>
    {

        public InspectPropertiesTask() {
            super(getObjectClass());
        }
        
        public InspectPropertiesTask( String beanName ) {
            super( beanName);
        }

        public void execute() {
            runTask(this);
        }

        public TypeElement getTypePreceedingCaret( CompilationController controller ) throws Exception {
            return getTypePreceedingCaret(controller, false, false);
        }

        public TypeElement getTypePreceedingCaret( CompilationController controller, boolean fullExpression, boolean resolvedExpression ) throws Exception {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            return getTypePreceedingCaret(controller, resolvedExpression ? getResolvedExpression() : getExpression(), new FailHandler() {

                public void typeNotFound( int index, String propertyName ) {
                    myOffset = index;
                    myProperty = propertyName;
                }
            }, fullExpression);
        }
        
        public TypeMirror getTypePreceedingCaret( CompilationController controller ,
                String expression ) throws Exception 
        {
            controller.toPhase(Phase.ELEMENTS_RESOLVED);
            return getTypeMirrorPreceedingCaret(controller, expression , null , true );
        }

        public String getProperty() {
            return myProperty;
        }

        public int getOffset() {
            return myOffset;
        }
        
        public boolean lastProperty(){
            return isLast;
        }
        
        protected void setOffset(int offset){
            myOffset = offset;
        }
        
        protected void setProperty( String property){
            myProperty = property;
        }
        
        protected void setLast(){
            isLast = true;
        }

        private int myOffset = -1;

        private String myProperty;
        
        private boolean isLast;
    }
    
    protected abstract class BaseELTaskClass {

        protected String beanType;

        public BaseELTaskClass(String beanType) {
            this.beanType = beanType;
        }
        
        public void cancel() {
        }
        
        protected String removeQuotes(String propertyName){
            return ELExpression.this.removeQuotes(propertyName);
            
        }
        
        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo controller) {
            return getTypePreceedingCaret(controller, getExpression(), null );
        }
        
        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo controller,
                String expression) 
        {
            return getTypePreceedingCaret(controller, expression , null );
        }
        
        /**
         * bean.prop2... propN.propertyBeingTyped| - returns the type of propN
         */
        protected TypeElement getTypePreceedingCaret(CompilationInfo controller,
                String expression , FailHandler handler )
        {
            return getTypePreceedingCaret( controller , expression , handler , false);
        }
        
        protected TypeElement getTypePreceedingCaret(CompilationInfo controller,
                String expression , FailHandler handler , boolean fullexpression)
        {
            TypeMirror mirror = getTypeMirrorPreceedingCaret(controller, expression, handler, 
                    fullexpression);
            if ( mirror == null ){
                return null;
            }
            Element result = controller.getTypes().asElement( mirror);
            if ( result instanceof TypeElement ){
                return (TypeElement)result ;
            }
            else {
                return null;
            }
        }

        protected TypeMirror getTypeMirrorPreceedingCaret(CompilationInfo controller,
                String expression , FailHandler handler , boolean fullexpression)
        {
            if (beanType == null) {
                if ( handler != null ){
                    handler.typeNotFound(0, extractBeanName());
                }
                return null;
            }
            TypeElement element = controller.getElements().getTypeElement(beanType);
            // Fix for IZ#173351 - NullPointerException at org.netbeans.modules.web.core.syntax.completion.api.ELExpression$BaseELTaskClass.getTypeMirrorPreceedingCaret
            TypeMirror lastKnownType = null;
            if ( element!= null){
                lastKnownType = element.asType();
            }
            TypeMirror lastFoundType = lastKnownType;
            TypeMirror lastReturnType = null;

            Part parts[] = getParts( expression );
            // part[0] - the bean
            // part[parts.length - 1] - the property being typed (if not empty)

            int limit = parts.length - 1;

            if (fullexpression || getPropertyBeingTypedName().length() == 0) {
                limit += 1;
            }

            int i=1;
            parts:
            for ( ; i < limit; i++) {
                if (lastKnownType == null && lastReturnType == null) {
                    logger.fine("EL CC: Could not resolve type for property " //NOI18N
                            + parts[i] + " in " + expression); //NOI18N
                    if ( handler != null ){
                        handler.typeNotFound(parts[i-1].getIndex(), parts[i-1].getPart());
                    }
                    return null;
                }
                if (lastKnownType != null) {
                    String accessorName = getAccessorName(parts[i].getPart());
                    
                    //resolved expressions (iterating components) handling
                    //this means that once the type is iterable
                    //we cannot resolve methods of the iterable type itself,
                    //just type of its items
                    if(isResolvedExpression()) {
//                        TypeMirror typeParameter = extractTypeParameter(controller, lastKnownType, Iterable.class);
                        TypeMirror typeParameter = getIterableGenericType(controller, lastKnownType);
                        if(typeParameter != null) {
                            lastFoundType = lastKnownType = typeParameter;
                        }
                    }

                    //get all methods of the type
                    Element el= controller.getTypes().asElement(
                            lastKnownType);
                    
                    List<ExecutableElement> allMethods;
                    if ( el instanceof TypeElement ){
                        allMethods= ElementFilter.methodsIn(
                                controller.getElements().getAllMembers(
                                        (TypeElement) el));
                    }
                    else {
                        allMethods = Collections.emptyList();
                    }

                    TypeMirror type = lastKnownType;
                    lastKnownType = null;

                    for (ExecutableElement method : allMethods) {
                        if (accessorName.equals(method.getSimpleName()
                                .toString()))
                        {
                            ExecutableType methodType = (ExecutableType) 
                                controller.getTypes().asMemberOf(
                                        (DeclaredType)type, method);
                            TypeMirror returnType = methodType.getReturnType();
                            lastReturnType = returnType;

                            if (returnType.getKind() == TypeKind.ARRAY) {
                                continue parts;
                            }
                            else {
                                lastFoundType = lastKnownType = returnType;
                                break;
                            }
                        }

                    }

                    if (lastKnownType == null && (limit - i == 1)) {
                        //the last item may be a method, not property
                        String methodName = parts[i].getPart();
                        for (ExecutableElement method : allMethods) {
                            if (methodName.equals(method.getSimpleName().toString())) {
                                TypeMirror returnType = method.getReturnType();
                                lastReturnType = returnType;

                                if (returnType.getKind() == TypeKind.ARRAY) {
                                    continue parts;
                                } else {
                                    lastFoundType = lastKnownType = returnType;
                                    break;
                                }
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
                        if ( typeMirror.getKind() == TypeKind.ARRAY){
                            lastReturnType = typeMirror;
                            continue;
                        }
                        else {
                            lastFoundType = lastKnownType = typeMirror;
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
                                    lastFoundType = lastKnownType = typeMirror ;
                                }
                            }
                        }
                        if ( lastKnownType == null ){
                            lastFoundType = lastKnownType = controller.getElements().
                                getTypeElement(Object.class.getCanonicalName()).
                                asType();
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
                                    lastFoundType = lastKnownType = typeMirror;
                                }
                            }
                        }
                        if (lastKnownType == null) {
                            lastFoundType = lastKnownType = controller.getElements()
                                    .getTypeElement(
                                            Object.class.getCanonicalName()).
                                            asType();
                        }
                    }
                    lastReturnType = null;
                }
            }
            if ( lastKnownType == null && lastReturnType == null && handler!= null){
                handler.typeNotFound(parts[i-1].getIndex(), parts[i-1].getPart());
            }

            //resolved expressions (iterating components) handling
            //and finally process the found type for its type parameter
            if(isResolvedExpression()) {
//                TypeMirror typeParam = extractTypeParameter(controller, lastFoundType, Iterable.class);
                TypeMirror typeParam = getIterableGenericType(controller, lastFoundType);
                lastFoundType = typeParam != null ? typeParam : lastFoundType;
            }
            return lastFoundType;
        }

        private TypeMirror extractTypeParameter(CompilationInfo controller, TypeMirror lastKnownType, Class clazz) {
            TypeMirror erasedLastKnownType = controller.getTypes().erasure(lastKnownType);
            TypeMirror clazzTypeMirror = controller.getElements().getTypeElement(clazz.getCanonicalName()).asType();
            TypeMirror erasedClazzTypeMirror = controller.getTypes().erasure(clazzTypeMirror);
            
            if (controller.getTypes().isAssignable(erasedLastKnownType, erasedClazzTypeMirror)) {
                if (lastKnownType instanceof DeclaredType) {
                    List<? extends TypeMirror> typeArguments =
                            ((DeclaredType) lastKnownType).getTypeArguments();
                    if (typeArguments.size() != 0) {
                        TypeMirror typeMirror = typeArguments.get(0);
                        if (typeMirror.getKind() == TypeKind.DECLARED) {
                            return typeMirror;
                        }
                    }
                }
            }
            return null;
        }

        protected String getAccessorName(String propertyName) {
            // we do not have to handle "is" type accessors here
            // Fix for IZ#172658 - StringIndexOutOfBoundsException: String index out of range: 0
            StringBuilder suffix = new StringBuilder();
            if ( propertyName.length() == 1 ){
                suffix.append(Character.toUpperCase(propertyName.charAt(0)));   
            }
            else if ( propertyName.length() >1 ) {
                suffix.append(Character.toUpperCase(propertyName.charAt(0)));
                suffix.append( propertyName.substring(1) );
            }
            return suffix.insert( 0 , "get" ).toString();          //NOI18N
        }

        /**
         * @return property name is <code>accessorMethod<code> is property accessor, otherwise null
         */
        protected String getExpressionSuffix(ExecutableElement method,
                CompilationController controller) 
        {

            if (method.getModifiers().contains(Modifier.PUBLIC) 
                    && checkMethodParameters(method, controller )) 
            {
                String methodName = method.getSimpleName().toString();

                if (methodName.startsWith("get") && methodName.length() >3 ) { //NOI18N
                    return getPropertyName(methodName, 3);
                }

                if (methodName.startsWith("is") && methodName.length() >2) { //NOI18N
                    return getPropertyName(methodName, 2);
                }

                if (isDefferedExecution()) {
                    //  also return values for method expressions

                    if (checkMethod(method, controller )) { 
                        return methodName;
                    }
                }
            }

            return null; // not a property accessor
        }
        
        protected boolean checkMethodParameters( ExecutableElement method , 
                CompilationController controller )
        {
            return method.getParameters().size() == 0;
        }
        
        protected boolean checkMethod( ExecutableElement method , 
                CompilationController controller)
        {
            return String.class.getCanonicalName().equals( 
                    method.getReturnType().toString());
        }
    }
    
    public interface FailHandler {
        void typeNotFound( int expressionIndex , String propertyName );
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
                        controller.getElements().getAllMembers(bean))) 
                {
                    String propertyName = getExpressionSuffix(method, controller );

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
            if(beanType == null) {
                return ;
            }
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            
//            TypeElement bean = getTypePreceedingCaret(controller);
            TypeElement bean = controller.getElements().getTypeElement(beanType);

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
                    String propertyName = getExpressionSuffix(method, controller );

                    if (propertyName != null && propertyName.startsWith(prefix)) {
                        boolean isMethod = propertyName.equals(method.getSimpleName().toString());
                        String type ;
                        if ( isMethod ){
                            TypeMirror methodType = controller.getTypes().asMemberOf( 
                                    (DeclaredType)bean.asType(), method);
                            type = ((ExecutableType)methodType).getReturnType().toString();
                        }
                        else {
                            type ="";
                        }
                        CompletionItem item = ElCompletionItem.createELProperty(
                                propertyName, getInsert( propertyName , firstChar), 
                                anchorOffset, type);

                        completionItems.add(item);
                    }
                }
            }
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
     *
     *  Implementation may use getContextOffset() method if context sensitive
     */
    protected int findContext(String expr) {
        int dotIndex = expr.indexOf('.');
        int bracketIndex = expr.indexOf('[');
        int value = EL_UNKNOWN;

        if (bracketIndex == -1 && dotIndex > -1) {
            String first = expr.substring(0, dotIndex);
            if (value == EL_UNKNOWN && ELImplicitObjects.getELImplicitObjects(first
                    ,this ).size() > 0) {
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

    public String getResolvedExpression() {
        return resolvedExpression != null ? resolvedExpression : expression;
    }

    public boolean isResolvedExpression() {
	//note:getResolvedExpression cannot be non-null if getExpression is null
        return getResolvedExpression() != null && !getExpression().equals(getResolvedExpression());
    }

    public String getReplace() {
        return replace;
    }

    //copied from java.hints module org.netbeans.modules.java.hints.errors.Utilities class >>>
    /**
     *
     * @param info context {@link CompilationInfo}
     * @param iterable tested {@link TreePath}
     * @return generic type of an {@link Iterable} or {@link ArrayType} at a TreePath
     */
    private static TypeMirror getIterableGenericType(CompilationInfo info, TypeMirror iterableType) {
        if(iterableType == null) {
            return null;
        }
        TypeElement iterableElement = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterableElement == null) {
            return null;
        }
        TypeMirror designedType = null;
        if (iterableType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) iterableType;
            if (!info.getTypes().isSubtype(info.getTypes().erasure(declaredType), info.getTypes().erasure(iterableElement.asType()))) {
                return null;
            }
            ExecutableElement iteratorMethod = (ExecutableElement) iterableElement.getEnclosedElements().get(0);
            ExecutableType iteratorMethodType = (ExecutableType) info.getTypes().asMemberOf(declaredType, iteratorMethod);
            List<? extends TypeMirror> typeArguments = ((DeclaredType) iteratorMethodType.getReturnType()).getTypeArguments();
            if (!typeArguments.isEmpty()) {
                designedType = typeArguments.get(0);
            }
        } else if (iterableType.getKind() == TypeKind.ARRAY) {
            designedType = ((ArrayType) iterableType).getComponentType();
        }
        if (designedType == null) {
            return null;
        }
        return resolveCapturedType(info, designedType);
    }

    private static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);

        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }

        }

        return type;
    }

    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            if (orig.getKind() == TypeKind.WILDCARD) {
                TypeMirror extendsBound = ((WildcardType) orig).getExtendsBound();
                TypeMirror rct = SourceUtils.resolveCapturedType(extendsBound != null ? extendsBound : ((WildcardType) orig).getSuperBound());
                if (rct != null) {
                    return rct;
                }
            }
            return orig;
        }

        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();

            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }

            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;

            return info.getTypes().getArrayType(resolveCapturedTypeInt(info, at.getComponentType()));
        }

        return tm;
    }

    //<<< eof copy
    
}
