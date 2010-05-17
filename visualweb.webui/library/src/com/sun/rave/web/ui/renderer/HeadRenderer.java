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
import java.net.URL;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Head;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 * <p>Renderer for a {@link Head} component.</p>
 */

public class HeadRenderer extends AbstractRenderer {


    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] = { "profile" }; //NOI18N

    /**
     * <p>Render the appropriate element start, depending on whether the
     * <code>for</code> property is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to render.
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
                               ResponseWriter writer) throws IOException {

        // Start the appropriate element
	if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("head", component); //NOI18N
	}
    }


    /**
     * <p>Render the appropriate element attributes, 
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component component to be rendered
     *  submitted value is to be stored
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, 
				    UIComponent component, 
                                    ResponseWriter writer) 
	throws IOException {
        
        Head head = (Head) component;
        
        if (!RenderingUtilities.isPortlet(context)) {
            
	    // Profile
            addStringAttributes(context, component, writer, stringAttributes);

	    // Meta tags
	    writer.write("\n"); //NOI18N
	    renderMetaTag("no-cache", "Pragma", writer, head); 
	    renderMetaTag("no-cache", "Cache-Control", writer, head); 
	    renderMetaTag("no-store", "Cache-Control", writer, head); 
	    renderMetaTag("max-age=0", "Cache-Control", writer, head); 
	    renderMetaTag("1", "Expires", writer, head); 

            // Title
            String title = head.getTitle();            
            if (title == null) {
                title = "";
            }
            
	    writer.startElement("title",  head);
            writer.write(title);
            writer.endElement("title");
            writer.write("\n"); //NOI18N
            
	    // Base
            if(head.isDefaultBase()) {
                writer.startElement("base", head); //NOI18N
                // TODO - verify the requirements w.r.t. printing this href
                writer.writeURIAttribute("href", Util.getBase(context), null); //NOI18N
                writer.endElement("base"); //NOI18N
                writer.write("\n"); //NOI18N
            }

	    // Link and Scripts
            Theme theme = ThemeUtilities.getTheme(context);
            //master link to always write out
            RenderingUtilities.renderJavaScript(head, theme, context, writer);
            RenderingUtilities.renderStyleSheetLink(head, theme, context, writer);
            
            writer.write(getCookieScript(context));           
	}
    }

    private String getCookieScript(FacesContext context) { 
        String viewId = context.getViewRoot().getViewId(); 
        String urlString = 
            context.getApplication().getViewHandler().getActionURL(context, viewId);
      
        StringBuffer jsBuffer = new StringBuffer(256); 
        jsBuffer.append("\n<script type=\"text/javascript\">\nvar "); 
        jsBuffer.append("sjwuic_ScrollCookie"); 
        jsBuffer.append(" = new sjwuic_ScrollCookie('"); 
        jsBuffer.append(viewId);
        jsBuffer.append("', '"); 
        jsBuffer.append(urlString); 
        jsBuffer.append("'); \n</script>\n"); 
        return jsBuffer.toString();       
    } 
    
    /**
     * <p>Render the appropriate element end, depending on whether the
     * <code>for</code> property is set or not.</p>
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

        // Start the appropriate element
        if (!RenderingUtilities.isPortlet(context)) {
            writer.endElement("head"); //NOI18N
            writer.write("\n"); //NOI18N
        }
    }


    private void renderMetaTag(String content, String httpEquivalent, 
			       ResponseWriter writer, Head head) 
            throws IOException { 

	writer.startElement("meta", head); 
	writer.writeAttribute("content", content, null); 
	writer.writeAttribute("http-equiv", httpEquivalent, null); 
	writer.endElement("meta"); 
	writer.writeText("\n", null); 
    } 
    

}
