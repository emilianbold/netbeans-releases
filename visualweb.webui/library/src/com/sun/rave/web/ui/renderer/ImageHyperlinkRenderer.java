/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.io.IOException;
import javax.faces.component.UIComponent;


import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;


/**
 * <p>This class is responsible for rendering the {@link ImageHyperlink} component for the
 * HTML Render Kit.</p> <p> The {@link ImageHyperlink} component can be used as an anchor, a
 * plain hyperlink or a hyperlink that submits the form depending on how the
 * properites are filled out for the component </p>
 */
public class ImageHyperlinkRenderer extends HyperlinkRenderer {

    // -------------------------------------------------------- Static Variables
    
    private static final String IMAGEFACTORY =
            "com.sun.rave.web.ui.component.util.factories.ImageComponentFactory"; //NOI18N
    
    // for positioning of the label.
    private static final String LABEL_LEFT="left"; //NOI18N
    private static final String LABEL_RIGHT="right"; //NOI18N
    
    // -------------------------------------------------------- Renderer Methods
    protected void finishRenderAttributes(FacesContext context,
            UIComponent component,
            ResponseWriter writer)
            throws IOException {
        //create an image component based on image attributes
        //write out image as escaped text
        //TODO: suppress the text field from the XML
        ImageHyperlink ilink = (ImageHyperlink) component;
        
        // ImageURL
        ImageComponent ic = ilink.getImageFacet();
        
        
        //TODO: use static text
        // <RAVE>
        // String label = ilink.getText();

        // If there is no text property set, then label == null which prevents
        // rendering anything at all
        Object text = ilink.getText();
        String label = (text == null) ? null : ConversionUtilities
                .convertValueToString(component, text);
        // </RAVE>
        if (label != null && ilink.getTextPosition().equalsIgnoreCase(LABEL_LEFT)) {
            writer.writeText(label, null);
            // <RAVE>
            writer.write("&nbsp;");
            // </RAVE>
        }
        if (ic != null) {
            RenderingUtilities.renderComponent(ic, context);
        }
        
        if (label != null && ilink.getTextPosition().equalsIgnoreCase(LABEL_RIGHT)) {
            // <RAVE>
            writer.write("&nbsp;");
            // </RAVE>
            writer.writeText(label, null);
        }
        
    }
    
    // --------------------------------------------------------- Private Methods
    
}
