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

import com.sun.rave.web.ui.util.ConversionUtilities;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;

import com.sun.rave.web.ui.component.HelpInline;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;


/**
 * Renders an instance of the {@link HelpInline} component.
 *
 * @author Sean Comerford
 */
public class HelpInlineRenderer extends AbstractRenderer {

    /** Creates a new instance of HelpInlineRenderer */
    public HelpInlineRenderer() {
    }

    /**
     * Render the start of the HelpInline component.
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderStart(FacesContext context, UIComponent component, 
            ResponseWriter writer) throws IOException {
        // render start of HelpInline
        HelpInline help = (HelpInline) component;
        Theme theme = ThemeUtilities.getTheme(context);
        
        writer.startElement("div", help);
        
        String style = null;
        
        if (help.getType().equals("page")) {
            style = theme.getStyleClass(ThemeStyles.HELP_PAGE_TEXT);
        } else {
            style = theme.getStyleClass(ThemeStyles.HELP_FIELD_TEXT);
        }
        
        addCoreAttributes(context, help, writer, style);
        
        // <RAVE>
        // String text = help.getText();
        String text = ConversionUtilities.convertValueToString(help, help.getText());
        // </RAVE>
        
        if (text != null) {
            writer.write(text);
            writer.write("&nbsp;&nbsp;");
        }
    }
    
    /**
     * Render the end of the HelpInline component.
     * 
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderEnd(FacesContext context, UIComponent component, 
            ResponseWriter writer) throws IOException {
        writer.endElement("div");
    }
}
