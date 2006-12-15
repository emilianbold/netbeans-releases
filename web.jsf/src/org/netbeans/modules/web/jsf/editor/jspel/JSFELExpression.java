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

package org.netbeans.modules.web.jsf.editor.jspel;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.ELExpression;
import org.netbeans.modules.web.core.syntax.deprecated.ELTokenContext;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;
import org.openide.loaders.DataObject;

/**
 *
 * @author Petr Pisl
 */
public class JSFELExpression extends ELExpression{
    
    public static final int EL_JSF_BEAN = 100;
    
    private WebModule wm;
    
    public JSFELExpression (WebModule wm, JspSyntaxSupport sup){
        super(sup);
        this.wm = wm;
    }
    
    protected int findContext(String expr){
        int dotIndex = expr.indexOf('.');
        int value = EL_UNKNOWN;
        
        if (dotIndex > -1){
            String first = expr.substring(0, dotIndex);
            List /*<JSFBean>*/beans = JSFBeanCache.getBeans(wm);
            
            for (int i = 0; i < beans.size(); i++)
                if (((ManagedBean)beans.get(i)).getManagedBeanName().equals(first)){
                    value = EL_JSF_BEAN;
                    continue;
                }
        }
        else if (dotIndex == -1)
            value = EL_START;
        return value;
    }
    
    public boolean onlyJSFExpression(int offset){
        boolean value = false;
        try {
            TokenItem token = sup.getTokenChain(offset-1, offset);
            if (token != null && token.getTokenContextPath().contains(ELTokenContext.contextPath)){
                while (token != null && token.getTokenID().getNumericID() != ELTokenContext.EL_DELIM_ID)
                    token = token.getPrevious();
                if (token != null)
                    value = token.getImage().startsWith("#{");
            }
        } catch (BadLocationException ex) {
            // TODO
        }
        return value;
    }
//TODO: RETOUCHE    
    /*public JavaClass getBean(String elExp){
        JavaClass javaClass = null;
        DataObject obj = NbEditorUtilities.getDataObject(sup.getDocument());
        
        if (elExp != null && !elExp.equals("") && obj != null){
            if (elExp.indexOf('.')> -1){
                String beanName = elExp.substring(0,elExp.indexOf('.'));
                List <JSFBean>beans = JSFBeanCache.getBeans(wm);
                ManagedBean bean;
                for (int i = 0; i < beans.size(); i++) {
                    bean = (ManagedBean) beans.get(i);
                    if (bean.getManagedBeanName().equals(beanName)){
                        javaClass = JMIUtil.findClass(bean.getManagedBeanClass(), ClassPath.getClassPath(obj.getPrimaryFile(), ClassPath.EXECUTE));
                        continue;
                    }
                }
            }  
        }
        return javaClass;
    }
  */  
    //TODO: RETOUCHE
    /*public List <String> getMethods(String elExp, JavaClass bean){
        List methodList = new ArrayList();
        JavaClass javaClass = findLastJavaClass(elExp, bean);
        
        
        // don't include methods that doesn't returns String                           
        if (javaClass != null && !javaClass.getName().equals("java.lang.String")){
            Method methods [] = JMIUtil.getMethods(javaClass);
            for (int j = 0; j < methods.length; j++) {
                String name = methods[j].getName();
                if (!(name.startsWith("set") || name.startsWith("get"))
                        && (methods[j].getType() instanceof JavaClass
                        && ((JavaClass)methods[j].getType()).getName().equals("java.lang.String"))
                        && methods[j].getParameters().size() == 0){
                    methodList.add(name);
                    methodList.add(methods[j].getType().getName());
                }
            }
        }
        return methodList;
    }*/
    
//TODO: RETOUCHE
    /*  Returns a JMI object which corresponds to the property in the source file. 
     */
    /*public Object getMethodDeclaration (String elExp, JavaClass bean){
      JavaClass javaClass = findLastJavaClass(elExp, bean);;
        String method = null;
        if (elExp.lastIndexOf('.') > -1)
            method = elExp.substring(elExp.lastIndexOf('.')+1);
        if (javaClass != null && method != null){
            Method methods [] = JMIUtil.getMethods(javaClass);
            for (int j = 0; j < methods.length; j++) {
                if (methods[j].getName().equals(method) && methods[j].getParameters().size() == 0
                        && methods[j].getType() instanceof JavaClass 
                        && ((JavaClass)methods[j].getType()).getName().equals("java.lang.String")) {
                    return methods[j];
                }
            }
        }
        return null;
    }*/
}
