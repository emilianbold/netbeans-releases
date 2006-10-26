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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.TokenItem;
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.ELTokenContext;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.loaders.DataObject;

/**
 *
 * @author Petr Pisl
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
     *  is pardsed.
     */
    public int parse(int offset){
        String value = null;
        int result = NOT_EL;
        boolean middle;
        try {
            TokenItem token = sup.getTokenChain(offset > 0 ? offset-1 : offset, offset > 0 ? offset : offset + 1);
            TokenItem lastToken = null;
            // Is the offset in EL?
            if (token != null && token.getTokenContextPath().contains(ELTokenContext.contextPath)){
                // Find the start of the expression. It doesn't have to be an EL delimiter (${ #{)
                // it can be start of the function or start of a simple expression. 
                while (token != null 
                        && token.getTokenID().getNumericID() != ELTokenContext.LPAREN_ID
                        && token.getTokenID().getNumericID() != ELTokenContext.WHITESPACE_ID
                        && token.getTokenID().getNumericID() != ELTokenContext.EL_DELIM_ID
                        && (token.getTokenID().getCategory() == null
                        || (token.getTokenID().getCategory().getNumericID() != ELTokenContext.KEYWORDS_ID
                        && token.getTokenID().getCategory().getNumericID() != ELTokenContext.NUMERIC_LITERALS_ID))){
                    if (value == null){
                        value = token.getImage();
                        if (token.getTokenID().getNumericID() == ELTokenContext.DOT_ID){
                            replace="";
                            middle = true;
                        }
                        else if (token.getImage().length() >= (offset-token.getOffset())){
                            value = value.substring(0, offset-token.getOffset());
                            replace = value;
                        }
                    }
                    else {
                        value = token.getImage() + value;
                        if (token.getTokenID().getNumericID() == ELTokenContext.TAG_LIB_PREFIX_ID)
                            replace = value;
                    }
                    lastToken = token;
                    token = token.getPrevious();
                }
                if (lastToken != null && lastToken.getTokenID().getNumericID() != ELTokenContext.IDENTIFIER_ID
                        && lastToken.getTokenID().getNumericID() != ELTokenContext.TAG_LIB_PREFIX_ID )
                    value = null;
                if (lastToken == null && token != null 
                        && (token.getTokenID().getNumericID() == ELTokenContext.WHITESPACE_ID
                        ||token.getTokenID().getNumericID() == ELTokenContext.LPAREN_ID))
                    result = EL_START;
                if (lastToken == null && token.getTokenID().getNumericID() == ELTokenContext.EL_DELIM_ID 
                        && token.getImage().indexOf('{')>0){
                    value = "";
                    replace = "";
                    result = EL_START;
                }
//                else 
//                   if (value != null){
//                     result = findContext(value);
//                   } 
            }
            
        } catch (BadLocationException ex) {
            // TODO inform about this
            value = null;
        }
        expression = value;
        return result;
    }
    
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
