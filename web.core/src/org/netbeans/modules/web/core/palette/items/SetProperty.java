/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette.items;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class SetProperty implements ActiveEditorDrop {
    
    public static final String[] implicitBeans = new String[] {  // NOI18N
        "request",
        "response", 
        "pageContext", 
        "session", 
        "application", 
        "out", 
        "config", 
        "page", 
        "exception" 
    };
    public static final int BEAN_DEFAULT = 0;
    public static final String[] implicitTypes = new String[] { // NOI18N
        "javax.servlet.http.HttpServletRequest", 
        "javax.servlet.http.HttpServletResponse",
        "javax.servlet.jsp.PageContext",
        "javax.servlet.http.HttpSession",
        "javax.servlet.ServletContext",
        "javax.servlet.jsp.JspWriter",
        "javax.servlet.ServletConfig",
        "java.lang.Object",
        "java.lang.Throwable" 
    };
    
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String property = "";
    private String value = "";
    
    public SetProperty() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {

        SetPropertyCustomizer c = new SetPropertyCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
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
        
        String strBean = " name=\"\""; // NOI18N
        if (beanIndex == -1)
            strBean = " name=\"" + bean + "\""; // NOI18N
        else 
            strBean = " name=\"" + implicitBeans[beanIndex] + "\""; // NOI18N
        
        String strProperty = " property=\"" + property + "\""; // NOI18N
        
        String strValue = " value=\"" + value + "\""; // NOI18N
        
        String sp = "<jsp:setProperty" + strBean + strProperty + strValue + " />"; // NOI18N
        
        return sp;
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

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
        
}
