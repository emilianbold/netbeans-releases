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
import javax.faces.context.ResponseWriter;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for TextAreaRenderer {@link TextArea} component.</p>
 */

public class TextAreaRenderer extends FieldRenderer {


    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        if(!(component instanceof TextArea)) {
            Object[] params = { component.toString(),
                                this.getClass().getName(), 
                                TextArea.class.getName() };
            String message = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                 "Renderer.component", params);              //NOI18N
            throw new FacesException(message);  
        }

        super.renderField(context, (TextArea)component, "textarea", getStyles(context));
    }
    
    String[] getStyles(FacesContext context) {
        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = new String[4];
        styles[0] = theme.getStyleClass(ThemeStyles.TEXT_AREA);
        styles[1] = theme.getStyleClass(ThemeStyles.TEXT_AREA_DISABLED);        
        styles[2] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[3] = theme.getStyleClass(ThemeStyles.LIST_ALIGN);
        return styles;
    }
}
