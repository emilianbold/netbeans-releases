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

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import com.sun.rave.web.ui.component.FrameSet;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * <p>Renderer for a {@link FrameSet} component.</p>
 */

public class FrameSetRenderer extends AbstractRenderer {


    // ======================================================== Static Variables

     /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "rows", "cols", "borderColor"}; //NOI18N
    private static final String integerAttributes[] =
    { "border", "frameSpacing"}; //NOI18N
    private static final String booleanAttributes[] =
    { "frameBorder" }; //NOI18N
    
    // -------------------------------------------------------- Renderer Methods
    
    
    /**
     * <p>Render the appropriate element start for the outermost
     * element.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        FrameSet frameset = (FrameSet) component;
        
        // I don't think this is the correct way to write the XML
        // header /avk
        
        if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("frameset", component);
       }
        
    }
    
    
    
    /**
     * <p>Render the appropriate element attributes, followed by the
     * nested <code>&lt;head&gt;</code> element, plus the beginning
     * of a nested <code>&lt;body&gt;</code> element.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        FrameSet frameset = (FrameSet) component;
        
        // Render a nested "head" element
        if (!RenderingUtilities.isPortlet(context)) {
            //id
            String id=frameset.getClientId(context);
            if (id != null) {
                writer.writeAttribute("id", id, null); //NOI18N
            }
            //class
            String styleClass = frameset.getStyleClass();
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, null); //NOI18N
            }
            //style
            String style = frameset.getStyle();
            if (style != null) {
                writer.writeAttribute("style", style, null); //NOI18N
            }
            //tooltip
            String toolTip = frameset.getToolTip();
            if (toolTip != null) {
                writer.writeAttribute("title", toolTip, "toolTip"); //NOI18N
            }
            //write out the rest of the attributes
            addStringAttributes(context, component, writer, stringAttributes);
            addBooleanAttributes(context, component, writer, booleanAttributes);
            addIntegerAttributes(context, component, writer, integerAttributes);
            writer.write("\n"); //NOI18N
         }
        
    }
    /**
     * <p>Render the appropriate element end.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        FrameSet frameset = (FrameSet) component;
        
        // End the outermost "html" element
        if (!RenderingUtilities.isPortlet(context)) {
            writer.endElement("frameset"); //NOI18N
            writer.write("\n"); //NOI18N
        }
                
    }
    
    
    // --------------------------------------------------------- Private Methods
}
