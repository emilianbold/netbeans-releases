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

import java.util.List;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.completion.ELExpression;
import org.netbeans.modules.web.jsf.config.model.ManagedBean;

/**
 *
 * @author Petr Pisl
 */
public class JSFELExpression extends ELExpression{
    
    public static final int EL_JSF_BEAN = 100;
    
    private WebModule wm;
    
    public JSFELExpression(WebModule wm, JspSyntaxSupport sup){
        super(sup);
        this.wm = wm;
    }
    
    protected int findContext(String expr){
        int dotIndex = expr.indexOf('.');
        int value = EL_UNKNOWN;
        
        if (dotIndex > -1){
            String first = expr.substring(0, dotIndex);
            List <ManagedBean>beans = JSFBeanCache.getBeans(wm);
            
            for (int i = 0; i < beans.size(); i++)
                if (beans.get(i).getManagedBeanName().equals(first)){
                    value = EL_JSF_BEAN;
                    continue;
                }
        } else if (dotIndex == -1)
            value = EL_START;
        return value;
    }
    
    @Override public String getObjectClass(){
        String beanName = extractBeanName();
        
        List <ManagedBean>beans = JSFBeanCache.getBeans(wm);
        
        for (ManagedBean bean : beans){
            if (beanName.equals(bean.getManagedBeanName())){
                return bean.getManagedBeanClass();
            }
        }
        
        return null;
    }
}
