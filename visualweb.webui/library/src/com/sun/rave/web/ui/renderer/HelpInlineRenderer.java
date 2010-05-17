/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
