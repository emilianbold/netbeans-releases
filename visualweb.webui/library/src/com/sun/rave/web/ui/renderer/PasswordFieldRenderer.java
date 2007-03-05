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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.component.PasswordField;
import com.sun.rave.web.ui.util.MessageUtil;

/**
 * <p>Renderer for PasswordFieldRenderer {@link PasswordField} component.</p>
 */

public class PasswordFieldRenderer extends FieldRenderer {

    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        if(!(component instanceof PasswordField)) {
            Object[] params = { component.toString(),
                                this.getClass().getName(),
                                PasswordField.class.getName() };
            String message = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                 "Renderer.component", params);              //NOI18N
            throw new FacesException(message);  
        }

        super.renderField(context, (PasswordField)component, "password", getStyles(context));
    }
}
