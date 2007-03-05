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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 * <p>Component that represents HTML body.</p>
 */

public class Body extends BodyBase {

    public static final String FOCUS_PARAM = "com.sun.rave.web.ui_body_focusComponent";
    public static final String JAVASCRIPT_OBJECT = "_jsObject";

    public String getFocusID(FacesContext context) {
        String id = getFocus();
        if(id != null) {
            // Get primary element id of given component.
            UIComponent comp = findComponent(id);
            if(comp != null && comp instanceof ComplexComponent) {
                id = ((ComplexComponent) comp).getPrimaryElementID(context);
            }
        } else {
            // Get client id cached in request map -- bugtraq #6316565.
            // Note: This must be a client Id to identify table children.
            id = (String) context.getExternalContext().getRequestMap().
                get(FOCUS_PARAM);
        }        
        return id;
    }
    
    public String getJavaScriptObjectName(FacesContext context) {
        return getClientId(context).replace(':', '_').concat(JAVASCRIPT_OBJECT);
    }
}
