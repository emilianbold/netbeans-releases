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

package org.netbeans.modules.web.core.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class UseBean implements ActiveEditorDrop {

    private static final int BEAN_DEFAULT = -1;

    public static final String[] scopes = new String[] { "page", "request", "session", "application" }; // NOI18N
    public static final int SCOPE_DEFAULT = 0;
    
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String clazz = "";
    private int scopeIndex = SCOPE_DEFAULT;
    
    private String[] beans;
   
    public UseBean() {
        beans = findBeans();
        if (beans.length > 0)
            beanIndex = 0;
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        UseBeanCustomizer c = new UseBeanCustomizer(this, targetComponent);
        JSPPaletteUtilities.getAllBeans(targetComponent);
        boolean accept = c.showDialog(JSPPaletteUtilities.getAllBeans(targetComponent));
        if (accept) {
            String body = createBody();
            try {
                JSPPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                accept = false;
            }
        }
        
        return accept;
    }

    private String createBody() {
        
        String strBean = " id=\"\""; // NOI18N
        if (beanIndex == -1)
            strBean = " id=\"" + bean + "\""; // NOI18N
        else 
            strBean = " id=\"" + beans[beanIndex] + "\""; // NOI18N
        
        String strClass = " class=\"" + clazz + "\""; // NOI18N
        
        String strScope = " scope=\"" + scopes[scopeIndex] + "\""; // NOI18N

        String ub = "<jsp:useBean" + strBean + strScope + strClass + " />"; // NOI18N
        
        return ub;
    }
        
    private String[] findBeans() {
         
        //TODO retrieve existing beans
        String[] beans = new String[] {};
        
        return beans;
    }

    public int getBeanIndex() {
        return beanIndex;
    }

    public void setBeanIndex(int beanIndex) {
        this.beanIndex = beanIndex;
    }

    public String getBean() {
        return bean;
    }

    public void setBean(String bean) {
        this.bean = bean;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public int getScopeIndex() {
        return scopeIndex;
    }

    public void setScopeIndex(int scopeIndex) {
        this.scopeIndex = scopeIndex;
    }

    public String[] getBeans() {
        return beans;
    }

    public void setBeans(String[] beans) {
        this.beans = beans;
    }
    
}
