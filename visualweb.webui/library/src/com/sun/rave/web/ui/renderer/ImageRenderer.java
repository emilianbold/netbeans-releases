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

import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ClientSniffer;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.beans.Beans;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;

/**
 * Image renderer
 *
 * @author  Sean Comerford
 */
public class ImageRenderer extends AbstractRenderer {

    /**
     * <p>The set of integer pass-through attributes to be rendered.</p>
     */
    private static final String integerAttributes[] = { "border", //NOI18N
            "hspace", "vspace" }; //NOI18N

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] = { "align", //NOI18N
    "onClick", "onDblClick", //NO18N
    "onMouseDown", "onMouseMove", "onMouseOut", "onMouseOver" }; //NOI18N

    /** Creates a new instance of ImageRenderer */
    public ImageRenderer() {
        // default constructor
    }

    /**
     * Render the start of the image element
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        // render start of image
        ImageComponent image = (ImageComponent) component;
        writer.startElement("img", image);  //NOI18N
    }

    /**
     * Render the image element's attributes
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        // render image attrs
        ImageComponent image = (ImageComponent) component;

        String clientId = image.getClientId(context);
        if (clientId != null) {
            writer.writeAttribute("id", clientId, null);  //NOI18N
        }

        String url = image.getUrl();
        String icon = image.getIcon();
        int height = image.getHeight();
        int width = image.getWidth();

        
        if (image instanceof Icon || (icon != null && url == null)) {
            Icon themeIcon = ThemeUtilities.getTheme(context).getIcon(icon);
            url = themeIcon.getUrl();
            // height
            int dim = themeIcon.getHeight();
            if (height < 0 && dim >= 0) {
                height = dim;
            }
            // width
            dim = themeIcon.getWidth();
            if (width < 0 && dim >= 0) {
                width = dim;
            }
        }  else if (url == null) {
            if (!Beans.isDesignTime()) {
                // log an error
                if (LogUtil.warningEnabled(ImageRenderer.class)) {
                    LogUtil.warning(ImageRenderer.class, "  URL  was not " +
                            "specified and generally should be"); // NOI18N
                }
            }
        } else {
            url = context.getApplication().getViewHandler()
                    .getResourceURL(context, url);
        }
        
        //<RAVE>
        //if (url == null) {
        //    url = "";
        //}
         
        //must encode the url (even though we call the function later)!
        //url = context.getExternalContext().encodeResourceURL(url);   
        
        if (url == null || url.trim().length() == 0) {
            url = "";
        } else {
            //must encode the url (even though we call the function later)!
            url = context.getExternalContext().encodeResourceURL(url);
        }
        //<RAVE>
        
        StringBuffer styleAdditions = null;
        String style = image.getStyle();
        
        
        if (isPngAndIE(context, url)) {
            // need to add stuff to the style attribute
            styleAdditions = new StringBuffer(200);
            
            //take care of width
            styleAdditions.append("width:"); // NOI18N
            if (width > 0) {
                styleAdditions.append(width);
                styleAdditions.append("px;"); // NOI18N
            } else  {
               styleAdditions.append("100px;"); // NOI18N
            }
            
            //take care of height
            styleAdditions.append("height:"); // NOI18N
            if (height > 0) {
                styleAdditions.append(height);
                styleAdditions.append("px;");   // NOI18N
            } else { 
               styleAdditions.append("100px;"); // NOI18N
            }
            // append special filter stuff.
            styleAdditions.append("filter:progid:DXImageTransform.Microsoft."); // NOI18N
            styleAdditions.append("AlphaImageLoader(src='"); // NOI18N
            styleAdditions.append(url);
            styleAdditions.append("', sizingMethod='scale')"); // NOI18N
            
            Theme theme = ThemeUtilities.getTheme(context);
            url = theme.getIcon(ThemeImages.DOT).getUrl();
            if (style == null) {
                style = styleAdditions.toString();
            } else {
                if (!style.endsWith(";")) { // NOI18N
                    style += ";"; // NOI18N
                }
                style += styleAdditions;
            }

        }
        
        //write style class and style info
        RenderingUtilities.renderStyleClass(context, writer, image, null);
        if (style != null) {
            writer.writeAttribute("style", style, null); // NOI18N
        }
        
        RenderingUtilities.renderURLAttribute(context, writer, image, "src", //NO18N
                url, "url"); //NO18N

        // the alternateText property should be rendered as the image's alt attr
        String alt = image.getAlt();

        if (alt != null) {
            writer.writeAttribute("alt", alt, null); // NOI18N
        } else {
            // alt is a required for XHTML compliance so output empty string
            // IS THIS ELSE NEEDED NOW THAT DESCRIPTION IS A REQUIRED PROPERTY?
            writer.writeAttribute("alt", "", null);  //NOI18N
        }

        // render the tooltip property as the image title attribute
        String toolTip = image.getToolTip();
        if (toolTip != null) {
            writer.writeAttribute("title", toolTip, null);   //NOI18N
        }

        // render the longDescription property as the image longdesc attribute
        String longDesc = image.getLongDesc();
        if (longDesc != null) {
            writer.writeAttribute("longdesc", longDesc, null); // NOI18N
        }

        // render height
        if (height >= 0) {
            writer.writeAttribute("height", Integer.toString(height), null); // NOI18N
        }

        // render width
        if (width >= 0) {
            writer.writeAttribute("width", Integer.toString(width), null); // NOI18N
        }
        
        addIntegerAttributes(context, component, writer, integerAttributes);
        addStringAttributes(context, component, writer, stringAttributes);        
    }

    /**
     * Render the end of the image element
     *
     * @param context The current FacesContext
     * @param component The ImageComponent object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        // render end of image
        ImageComponent image = (ImageComponent) component;

        writer.endElement("img");
    }
    
    private boolean isPngAndIE(FacesContext context, String url) {
        ClientSniffer cs = ClientSniffer.getInstance(context);
        //Some time encodeResourceURL(url) adds the sessiod to the
        // image URL, make sure to take that in to account
        //<RAVE>
        if (url.indexOf("sessionid") != -1){
            if (url.substring(0,url.indexOf(';')).endsWith(".png")&& cs.isIe5up()) {
                return true;
            }
        }else{ //</RAVE>    
            if (url.endsWith(".png") && cs.isIe5up()) {
                return true;
            }
        }
        
        return false;
    }
}
