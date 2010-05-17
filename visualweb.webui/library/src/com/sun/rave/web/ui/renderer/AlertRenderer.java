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

import com.sun.rave.web.ui.component.Hyperlink;
import java.io.IOException;
import java.util.Properties;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Alert;
import com.sun.rave.web.ui.component.IconHyperlink;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renderer for an {@link Alert} component.</p>
 *
 */
public class AlertRenderer extends AbstractRenderer {

    /**
     * <p>The different types or categories of an alert.</p>
     */
    public static final String ALERT_TYPE_ERROR = "error"; //NOI18N
    public static final String ALERT_TYPE_WARN  = "warning"; //NOI18N
    public static final String ALERT_TYPE_INFO  = "information"; //NOI18N
    public static final String ALERT_TYPE_SUCCESS  = "success"; //NOI18N
    /**
     * <p>The default error type - if none is specified.</p>
     */
    public static final String ALERT_TYPE_DEFAULT = ALERT_TYPE_ERROR; //NOI18N
    
    private static final String ICON_HYPERLINK_FACTORY =
            "com.sun.rave.web.ui.component.util.factories.IconHyperlinkFactory"; //NOI18N
    
    /** Creates a new instance of AlertRenderer */
    public AlertRenderer() {
        // default constructor
    }
    
    public boolean getRendersChildren() {
        return true;
    }
    
    public  void encodeChildren(FacesContext context, UIComponent component)
    throws IOException {
        //purposefully don't want to do anything here!
        
    }
    
    /**
     * Renders the outer div which contains the alert.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderOuterDiv(FacesContext context,
            Alert alert, ResponseWriter writer) throws IOException {
        String styleClass = alert.getStyleClass();
        String style = alert.getStyle();
        String id = alert.getClientId(context);
        
        writer.startElement("div", alert); //NOI18N
        
        // Write a id only if a style/class was specified?
        if (id != null) {
            writer.writeAttribute("id", id, null);  //NOI18N
        }
        
        if (style != null) {
            writer.writeAttribute("style", style, null);  //NOI18N
        }
        
        RenderingUtilities.renderStyleClass(context, writer, (UIComponent) alert,
                styleClass);
    }
    
    /**
     * Renders the attributes for the outer table containing the inline alert.
     * TODO: Use div's instead of tables for layout as soon as I can find a
     * solution that works for IE.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param theme The Theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderOpeningTable(FacesContext context,
            Alert alert, Theme theme,
            ResponseWriter writer) throws IOException {
        writer.startElement("table", alert); //NOI18N
        
        // <RAVE>
        // align attribute is deprecated and it causes incosistent result in IE and Mozilla when it is set (see bug 6327647)
        //writer.writeAttribute("align", "center", null); //NOI18N
        // <RAVE>
        writer.writeAttribute("border", "0", null); //NOI18N
        writer.writeAttribute("cellspacing", "0", null); //NOI18N
        writer.writeAttribute("cellpadding", "0", null); //NTOI18N
        writer.writeAttribute("title", "", null); //NOI18N
        
        // Set the containing table style based on the theme
        String tableStyle = theme.getStyleClass(ThemeStyles.ALERT_TABLE);
        writer.writeAttribute("class", tableStyle, null); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        writer.startElement("tr", alert); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        writer.startElement("td", alert); //NOI18N
        writer.writeAttribute("valign", "middle", null);  //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    /**
     * Renders the icon associated with an inline alert message.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param theme The theme to use
     * @param type The type of alert. Default is ALERT_TYPE_ERROR.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertIcon(FacesContext context,
            Alert alert, String type, Theme theme,
            ResponseWriter writer) throws IOException {
        UIComponent alertIcon = alert.getAlertIcon();
        RenderingUtilities.renderComponent(alertIcon, context);
    }
    
    /**
     * Renders the summary message of the inline alert.
     *
     * @param alert The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertSummaryText(Alert alert,
            ResponseWriter writer) throws IOException {
        // Render the summary text
        String summary = alert.getSummary();
        
        // Check if it should be HTML escaped (true by default).
        writer.writeText(summary, null);
    }
    
    /**
     * Renders the optional detail message of the inline alert.
     *
     * @param alert The Alert object to use
     * @param theme The theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertDetailArea(FacesContext context,
            Alert alert, Theme theme,
            ResponseWriter writer) throws IOException {
        // Get the detail text
        String detail = alert.getDetail();
        
        // Get the children, if any.
        List children = alert.getChildren();
        if ((detail == null || detail.trim().length() == 0)
        && children.size() <= 0)
            return;
        
        // Set the style
        writer.startElement("div", alert); //NOI18N
        writer.writeAttribute("class", // NOI18N
                theme.getStyleClass(ThemeStyles.ALERT_MESSAGE_TEXT), null);
        
        
        // Check if it should be HTML escaped (true by default).
        if (detail != null) {
            writer.writeText(detail, null);
        }
        
        // render any children
        super.encodeChildren(context, alert);
        // Close the div
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    /**
     * Renders the optional link at the end of the alert.
     *
     * @param context The current FacesContext
     * @param alert The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAlertLink(FacesContext context,
            Alert alert, Theme theme, ResponseWriter writer)
            throws IOException {
        UIComponent link = alert.getAlertLink();
        if (link == null)
            return;
        if (Hyperlink.class.isAssignableFrom(link.getClass()) && ((Hyperlink) link).getText() == null &&
                ((Hyperlink) link).getUrl() == null)
            return;
        writer.startElement("div", alert); //NOI18N
        writer.writeAttribute("class", //NOI18N
                theme.getStyleClass(ThemeStyles.ALERT_LINK_DIV), null);
        RenderingUtilities.renderComponent(link, context);
        writer.endElement("div"); //NOI18N
    }
    
    /**
     * Renders the optional detail message of the inline alert.
     *
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderClosingTags(ResponseWriter writer)
    throws IOException {
        writer.writeText("\n", null); //NOI18N
        writer.endElement("td"); //NOI18N
        writer.endElement("tr"); //NOI18N
        writer.endElement("table"); //NOI18N
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
    }
    
    
    
    /**
     * Renders the inline alert component.
     *
     * @param context The current FacesContext
     * @param component The Alert object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        // Render end of alert
        Alert alert = (Alert) component;
        String summary = alert.getSummary();
        
        // If a summary message is not specified, nothing to render.
        if (summary == null || summary.trim().length() == 0)
            return;
        
        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);
        
        // Render the outer div that wraps the alert
        renderOuterDiv(context, alert, writer);
        
        // Render the opening table
        renderOpeningTable(context, alert, theme, writer);
        
        // Get the text style based on the type of alert
        String type = alert.getType();
        // Set the default type
        if (type == null)
            type = ALERT_TYPE_DEFAULT;
        type = type.toLowerCase();
        
        String textStyle = getAlertTextStyle(type, theme);
        writer.startElement("div", alert); //NOI18N
        writer.writeAttribute("class", textStyle, null); //NOI18N
        
        // Render the alert icon
        renderAlertIcon(context, alert, type, theme, writer);
        
        // Render the summary text
        renderAlertSummaryText(alert, writer);
        
        // Close the div
        writer.endElement("div"); //NOI18N
        writer.writeText("\n", null); //NOI18N
        
        // Render the detailed text
        renderAlertDetailArea(context, alert, theme, writer);
        // Render the optional link, if specified
        renderAlertLink(context, alert, theme, writer);
        
        // Render the closing tags
        renderClosingTags(writer);
    }
    
    // Private helper methods.
    
    /**
     * Create an alert link component with the apecified properties
     * The link returned includes the "icon" associated the link.
     *
     * @param alert The alert object
     * @param text The alert link text
     * @param url The alert link url
     * @param theme The theme to use
     *
     * @return the hyperlink
     */
    private IconHyperlink getAlertLink(Alert alert, String text, String url,
            Theme theme) {
        // Assign properties
        Properties props = new Properties();
        props.setProperty("styleClass", //NOI18N
                theme.getStyleClass(ThemeStyles.ALERT_LINK));
        props.setProperty("text", text); //NOI18N
        props.setProperty("textPosition", "right"); //NOI18N
        props.setProperty("icon", ThemeImages.HREF_LINK); //NOI18N
        props.put("border", new Integer(0));
        props.setProperty("alt", ""); //NOI18N
        props.setProperty("url", url); //NOI18N
        String prop = alert.getLinkTarget();
        if (prop != null && prop.length() > 0) {
            props.setProperty("target", prop); //NOI18N
        }
        prop = alert.getLinkToolTip();
        if (prop != null && prop.length() > 0) {
            props.setProperty("toolTip", prop); //NOI18N
        }
        IconHyperlink link = (IconHyperlink) Util.getChild(alert,
                alert.getId() + "_alertLink", ICON_HYPERLINK_FACTORY, //NOI18N
                props); //NOI18N
        return link;
    }
    
    
    
    /**
     * Return the text style for the inline alert message, based on the
     * type of alert.
     *
     * @param type The type of alert. Default is ALERT_TYPE_ERROR.
     * @param theme The theme to use.
     *
     * @return The alert text style.
     */
    private String getAlertTextStyle(String type, Theme theme) {
        String style;
        
        if (type.equals(ALERT_TYPE_INFO)) {
            // Info
            style = theme.getStyleClass(ThemeStyles.ALERT_INFORMATION_TEXT);
        } else if (type.equals(ALERT_TYPE_SUCCESS)) {
            // Success - no style defined.
            style = theme.getStyleClass(ThemeStyles.ALERT_INFORMATION_TEXT);
        } else if (type.equals(ALERT_TYPE_WARN)) {
            // Warning
            style = theme.getStyleClass(ThemeStyles.ALERT_WARNING_TEXT);
        } else {
            // Error
            style = theme.getStyleClass(ThemeStyles.ALERT_ERROR_TEXT);
        }
        return style;
    }
}
