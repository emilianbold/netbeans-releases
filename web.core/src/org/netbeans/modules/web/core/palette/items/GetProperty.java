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
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.web.core.palette.JSPPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.openide.text.ActiveEditorDrop;


/**
 *
 * @author Libor Kotouc
 */
public class GetProperty implements ActiveEditorDrop {

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
    protected List<BeanDescr> allBeans = new ArrayList<BeanDescr>();
    private int beanIndex = BEAN_DEFAULT;
    private String bean = "";
    private String property = "";
    
    public GetProperty() {
    }

    public boolean handleTransfer(JTextComponent targetComponent) {
        allBeans = initAllBeans(targetComponent);
        GetPropertyCustomizer c = new GetPropertyCustomizer(this, targetComponent);
        
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
            strBean = " name=\"" + allBeans.get(beanIndex).getId() + "\""; // NOI18N
        
        String strProperty = " property=\"" + property + "\""; // NOI18N
        
        String gp = "<jsp:getProperty" + strBean + strProperty + " />"; // NOI18N
        
        return gp;
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

    protected List<BeanDescr> initAllBeans(JTextComponent targetComponent) {
       ArrayList<BeanDescr> res = new ArrayList<BeanDescr>();
        for (int i = 0; i < implicitBeans.length; i++) {
            String id = implicitBeans[i];
            String fqcn = implicitTypes[i];
            res.add(new BeanDescr(id, fqcn));
        }
        PageInfo.BeanData[] bd = JSPPaletteUtilities.getAllBeans(targetComponent);
        for (int i = 0; i < bd.length; i++) {
            BeanData beanData = bd[i];
            res.add(new BeanDescr(beanData.getId(), beanData.getClassName()));
        }

        return res;
    }
      
    class BeanDescr {
        private String id;
        private String fqcn;

        public void setFqcn(String fqcn) {
            this.fqcn = fqcn;
        }

        public String getFqcn() {
            return fqcn;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public BeanDescr(String id, String fqcn) {
            this.id = id;
            this.fqcn = fqcn;
        }

        @Override
        public String toString() {
            return id;
        }
        
    }
    
    public List<BeanDescr> getAllBeans(){
        return allBeans;
    }
}
