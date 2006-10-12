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

import java.awt.Component;
import org.netbeans.modules.web.core.syntax.completion.JspCompletionItem;

/**
 *
 * @author Petr Pisl
 */
public class JSFResultItem {
    
    public static class JSFBean extends JspCompletionItem.ELBean {
        
        private static JSFResultItemPaintComponent.JSFBeanPaintComponent paintComponent = null;
        
        JSFBean( String text, String type ) {
            super(text, type);
        }
        
        public int getSortPriority() {
            return 5;
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new JSFResultItemPaintComponent.JSFBeanPaintComponent();
            paintComponent.setString(text);
            paintComponent.setTypeName(type);
            return paintComponent;
        }
    }
    
    public static class JSFMethod extends JspCompletionItem.ELBean {
        
        private static JSFResultItemPaintComponent.JSFMethodPaintComponent paintComponent = null;
        
        JSFMethod( String text, String type ) {
            super(text, type);
        }
        
        
        public Component getPaintComponent(boolean isSelected) {
            if (paintComponent == null)
                paintComponent = new JSFResultItemPaintComponent.JSFMethodPaintComponent();
            paintComponent.setString(text);
            paintComponent.setTypeName(type);
            return paintComponent;
        }
    }
}
