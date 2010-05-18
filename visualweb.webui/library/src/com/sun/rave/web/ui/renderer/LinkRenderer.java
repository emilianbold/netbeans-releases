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


import com.sun.rave.web.ui.component.Link;
//import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.util.RenderingUtilities;
import java.beans.Beans;

import java.io.IOException;
import java.lang.NullPointerException;
import java.lang.StringBuffer;
import java.net.URL;
import java.util.Map;

import javax.faces.component.UIComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * <p>This class is responsible for rendering the link component for the
 * HTML Render Kit.</p> <p> The link component can be used as an Link</p>
 */
public class LinkRenderer extends AbstractRenderer {

    // -------------------------------------------------------- Static Variables

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] =
    { "charset", "media", "rel", "type"}; //NOI18N

      
      // -------------------------------------------------------- Renderer Methods
         
      
      /**
       * <p>Render the start of an Link (Link) tag.</p>
       * @param context <code>FacesContext</code> for the current request
       * @param component <code>UIComponent</code> to be rendered
       * @param writer <code>ResponseWriter</code> to which the element
       * start should be rendered
       * @exception IOException if an input/output error occurs
       */
      protected void renderStart(FacesContext context, UIComponent component,
      ResponseWriter writer) throws IOException {      
          //intentionally empty
      }
      
    /**
     * <p>Render the attributes for an Link tag.  The onclick attribute will contain
     * extra javascript that will appropriately submit the form if the URL field is
     * not set.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * attributes should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
    ResponseWriter writer) throws IOException {

        //intentionally empty
    }
      
    /**
     * <p>Close off the Link tag.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * end should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
        ResponseWriter writer) throws IOException {
        // End the appropriate element

         Link link = (Link) component;
         
         if (!RenderingUtilities.isPortlet(context)) {
            writer.startElement("link", link);
            addCoreAttributes(context, component, writer, null);
            addStringAttributes(context, component, writer, stringAttributes);
	    String lang = link.getUrlLang();
            if (null != lang) {
                writer.writeAttribute("hreflang", lang, "lang"); //NOI18N
            }
            // the URL is the tough thing because it needs to be encoded:
            
            String url = link.getUrl();
            
            if (url != null) {          
                writer.writeAttribute("href", //NOI18N
                    context.getApplication().getViewHandler()
                        .getResourceURL(context, url), 
                    "url"); //NOI18N
            }

            writer.endElement("link"); //NOI18N
            writer.write("\n"); //NOI18N
        }

    }
            
      // --------------------------------------------------------- Private Methods
      
}
