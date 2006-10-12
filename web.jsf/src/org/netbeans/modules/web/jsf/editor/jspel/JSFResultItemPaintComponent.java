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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.web.core.syntax.completion.ResultItemPaintComponent;

/**
 *
 * @author Petr Pisl
 */
public class JSFResultItemPaintComponent {
    
   public static class JSFBeanPaintComponent extends ResultItemPaintComponent.ELBeanPaintComponent {
        private static final String BEAN_PATH = "org/netbeans/modules/web/jsf/editor/jspel/resources/jsf_bean_16.png";  //NOI18N
        private String typeName;
        
        protected Icon getIcon(){
            return new ImageIcon(org.openide.util.Utilities.loadImage(BEAN_PATH));
        }
   }
   
   public static class JSFMethodPaintComponent extends ResultItemPaintComponent.ELPropertyPaintComponent {
        private static final String METHOD_PATH = "org/netbeans/modules/web/jsf/editor/jspel/resources/method_16.png";      //NOI18N
        private String typeName;
        
        protected Icon getIcon(){
            return new ImageIcon(org.openide.util.Utilities.loadImage(METHOD_PATH));
        }
   }
    
}
