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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.el.lexer.api.ELTokenId;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;

/**
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 */


/**
 *  This is a helper class for parsing and obtaining items for code completion of expression
 *  language.
 */
public class ELExpression {
    
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
                return EL_UNKNOWN; //no token found
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
            } else if (token.id() == ELTokenId.WHITESPACE || token.id() == ELTokenId.LPAREN) {
                result = EL_START;
            }

//fixme: Retouche
//                else
//                   if (value != null){
//                     result = findContext(value);
//                   }
            
        } finally {
            document.readUnlock();
        }
        expression = value;
        return result;
    }

//fixme: Retouche
//    /* Returns the JavaClass of the bean which is in the expression. Returns null, when
//     *  the appropriate class is not found.
//     */
//    public JavaClass getBean(String elExp){
//        JavaClass javaClass = null;
//        DataObject obj = NbEditorUtilities.getDataObject(sup.getDocument());
//
//        if (elExp != null && !elExp.equals("") && obj != null){
//            if (elExp.indexOf('.')> -1){
//                String beanName = elExp.substring(0,elExp.indexOf('.'));
//                BeanData[] beans = sup.getBeanData();
//                for (int i = 0; i < beans.length; i++) {
//                    if (beans[i].getId().equals(beanName)){
//                        javaClass = JMIUtil.findClass(beans[i].getClassName(), ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.EXECUTE));
//                        break;
//                    }
//                }
//            }
//        }
//        return javaClass;
//    }
//
//    /* Returns list of strings in form property name1, property type1 .....
//     */
//    public List /*<String>*/ getProperties(String elExp, JavaClass bean){
//        List properties = new ArrayList();
//        JavaClass javaClass = findLastJavaClass(elExp, bean);
//
//        if (javaClass != null && !javaClass.getName().equals("java.lang.String")){
//            Method methods [] = JMIUtil.getMethods(javaClass);
//            for (int j = 0; j < methods.length; j++) {
//                if ((methods[j].getName().startsWith("get") || methods[j].getName().startsWith("is"))
//                        && methods[j].getParameters().size() == 0
//                        && ((methods[j].getModifiers() & Modifier.PUBLIC) != 0)) {
//                    String name = methods[j].getName();
//                    if (name.startsWith("get"))
//                        name = name.substring(3);
//                    else
//                        name = name.substring(2);
//
//                    name = name.substring(0,1).toLowerCase()+name.substring(1);
//                    properties.add(name);
//                    properties.add(methods[j].getType().getName());
//                }
//            }
//        }
//        return properties;
//    }
//
//    /*  Returns a JMI object which corresponds to the property in the source file.
//     */
//    public Object getPropertyDeclaration (String elExp, JavaClass bean){
//        JavaClass javaClass = findLastJavaClass(elExp, bean);;
//        String property = null;
//        if (elExp.lastIndexOf('.') > -1)
//            property = elExp.substring(elExp.lastIndexOf('.')+1);
//        if (javaClass != null && property != null){
//            Method methods [] = JMIUtil.getMethods(javaClass);
//            for (int j = 0; j < methods.length; j++) {
//                if ((methods[j].getName().startsWith("get") || methods[j].getName().startsWith("is"))
//                        && methods[j].getParameters().size() == 0
//                        && ((methods[j].getModifiers() & Modifier.PUBLIC) != 0)) {
//                    String name = methods[j].getName();
//                    if (name.startsWith("get"))
//                        name = name.substring(3);
//                    else
//                        name = name.substring(2);
//                    name = name.substring(0,1).toLowerCase()+name.substring(1);
//                    if (name.equals(property)){
//                        return methods[j];
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    /** Return context, whether the expression is about a bean, implicit object or
//     *  function.
//     */
//    protected int findContext(String expr){
//        int dotIndex = expr.indexOf('.');
//        int bracketIndex = expr.indexOf('[');
//        int value = EL_UNKNOWN;
//
//        if (bracketIndex == -1 && dotIndex > -1){
//            String first = expr.substring(0, dotIndex);
//            BeanData[] beans = sup.getBeanData();
//            if (beans != null) {
//                for (int i = 0; i < beans.length; i++)
//                    if (beans[i].getId().equals(first)){
//                        value = EL_BEAN;
//                        continue;
//                    }
//            }
//            if (value == EL_UNKNOWN && ELImplicitObjects.getELImplicitObjects(first).size()>0)
//                value = EL_IMPLICIT;
//        }
//        else if (bracketIndex == -1 && dotIndex == -1)
//            value = EL_START;
//        return value;
//    }
//
//    /*  Returns the last java class which is in the expression.
//     *  Usefutl for bean.property1.property2
//     */
//    protected JavaClass findLastJavaClass(String elExp, JavaClass bean){
//        JavaClass javaClass = bean;
//        if (elExp != null && !elExp.equals("") && elExp.indexOf('.')> -1){
//            String pos = elExp.substring(elExp.indexOf('.')+1);
//
//            //find the last known class
//            if (javaClass != null && pos != null && !pos.equals("") && pos.lastIndexOf('.') > - 1){
//                StringTokenizer st = new StringTokenizer(pos.substring(0, pos.lastIndexOf('.')), ".");
//
//                while(st.hasMoreTokens()){
//                    String text = st.nextToken();
//                    if (javaClass != null){
//                        Method methods [] = JMIUtil.getMethods(javaClass);
//                        //reset the java class. Will be setup, if the property is found
//                        javaClass = null;
//                        for (int j = 0; j < methods.length; j++) {
//                            if (methods[j].getName().startsWith("get")) {
//                                String name = methods[j].getName().substring(3);
//                                name = name.substring(0,1).toLowerCase()+name.substring(1);
//                                if (name.equals(text)){
//                                    if (methods[j].getType() instanceof JavaClass)
//                                        javaClass = (JavaClass)methods[j].getType();
//                                    else
//                                        javaClass = null;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return javaClass;
//    }
    
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
