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
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ActionEvent;

/**
 * <p>Renderer for a {@link Button} component.</p>
 */
public class ButtonRenderer extends AbstractRenderer {
    /**
     * <p>The set of integer pass-through attributes to be rendered.</p>
     */
    private static final String integerAttributes[] = { "tabIndex" }; //NOI18N

    /**
     * <p>The set of String pass-through attributes to be rendered.</p>
     */
    private static final String stringAttributes[] = {
        "dir", //NOI18N
        "lang", //NOI18N
        "onClick", //NOI18N
        "onDblClick", //NOI18N
        "onKeyDown", //NOI18N
        "onKeyPress", //NOI18N
        "onKeyUp", //NOI18N
        "onMouseDown", //NOI18N
        "onMouseUp", //NOI18N
        "onMouseMove", //NOI18N
        "style", //NOI18N
    };
    
    /**
     * The set of pass-through attributes rendered for input elements.
     */
    private static final String inputAttributes[] = {
        "alt", //NOI18N
        "align" //NOI18N
    };
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Renderer Methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * <p>Determine if this was the component that submitted the form.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be decoded
     *
     * @exception NullPointerException if <code>context</code> or
     *  <code>component</code> is <code>null</code>
     */
    public void decode(FacesContext context, UIComponent component) {
        // Enforce NPE requirements in the Javadocs
        if (context == null || component == null) {
            throw new NullPointerException();
        }
        
        Button button = (Button) component;
        
        // Do not process disabled or reset components.
        if (button.isReset()) {
            return;
        }
        
        // Was our command the one that caused this submission?
        String clientId = button.getClientId(context);
        Map map = context.getExternalContext().getRequestParameterMap();
        
        if (map.containsKey(clientId) ||
                (map.containsKey(clientId + ".x")
                && map.containsKey(clientId + ".y"))) {
            button.queueEvent(new ActionEvent(button));
            
            // since this button submitted the form, we need to make it have
            // focus next time through. To do this, we will set an attribute
            // in the request map.
            RenderingUtilities.setLastClientID(context,
                    button.getPrimaryElementID(context));
        }
    }
    
    /**
     * <p>Render the appropriate element start, depending on the value of the
     * <code>type</code> property.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  start should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        Button button = (Button) component;
        
        // Start the appropriate element
        if (button.isEscape()) {
            writer.startElement("input", button); //NOI18N
        } else {
            writer.startElement("button", button); //NOI18N
        }
    }
    
    
    /**
     * <p>Render the appropriate element attributes depending on
     * the value of the <code>imageURL</code> attribute.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  attributes should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        Button button = (Button) component;
        
        // Render client id and name.
        //
        // Note: Null is used when output is different than the original value
        // of the component.
        writer.writeAttribute("id", button.getClientId(context), null); //NOI18N
        writer.writeAttribute("name", button.getClientId(context), null); //NOI18N
        
        // Render style classes.
        String style =  getStyle(button, ThemeUtilities.getTheme(context));
        RenderingUtilities.renderStyleClass(context, writer, button, style);
        
        String js = getJavascript(button.getOnBlur(), "onblur"); //NOI18N
        if (js != null) {
            writer.writeAttribute("onblur", js, "onBlur"); //NOI18N
        }
        
        js = getJavascript(button.getOnFocus(), "onfocus");  //NOI18N
        if (js != null) {
            writer.writeAttribute("onfocus", js, "onFocus"); //NOI18N
        }
        
        js = getJavascript(button.getOnMouseOut(), "onmouseout");  //NOI18N
        if (js != null) {
            writer.writeAttribute("onmouseout", js, "onMouseOut"); //NOI18N
        }
        
        js = getJavascript(button.getOnMouseOver(), "onmouseover");  //NOI18N
        if (js != null) {
            writer.writeAttribute("onmouseover", js, "onMouseOver"); //NOI18N
        }

        // Render tooltip.
 	if (button.getToolTip() != null) {
 	    writer.writeAttribute("title", button.getToolTip(), "toolTip"); //NOI18N
 	}
        
        // Render disabled attribute.
        if (button.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null); //NOI18N
        }
        
        // Render pass through attributes.
        addIntegerAttributes(context, component, writer, integerAttributes);
        addStringAttributes(context, component, writer, stringAttributes);

        // Render pass through attributes for input elements.
        if (button.isEscape()) {
            addStringAttributes(context, component, writer, inputAttributes);
        }

        // Note: Text attributes must be assigned last because the starting
        // element may be closed here -- see bugtraq #6315893.
        String url = button.getImageURL();
        if (url != null) {
            renderIconAttributes(context, component, writer, url);
        } else {
            renderTextAttributes(context, component, writer);
        }
    }
    
    /**
     * <p>Render the appropriate element end, depending on the value of the
     * <code>type</code> property.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  end should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        Button button = (Button) component;
        
        // End the appropriate element
        if (button.isEscape()) {
            writer.endElement("input"); //NOI18N
        } else {
            writer.endElement("button"); //NOI18N
        }
        
        // Initialize the JavaScript variable
        StringBuffer jsBuffer = new StringBuffer(200);
        jsBuffer.append("sjwuic_assign_button(");
        jsBuffer.append("'"); //NOI18N
        jsBuffer.append(button.getClientId(context));
        jsBuffer.append("', ");              //NOI18N
        jsBuffer.append((button.getImageURL() == null) ? "defaultButtonStrings"  //NOI18N
                : "defaultImgButtonStrings"); // NOI18N
        jsBuffer.append(", ");              //NOI18N
        jsBuffer.append(!button.isPrimary());
        jsBuffer.append(", ");              //NOI18N
        jsBuffer.append(button.isMini());
        jsBuffer.append(", ");              //NOI18N
        jsBuffer.append(button.isDisabled());
        jsBuffer.append(");");              //NOI18N
        
        writer.startElement("script", button); // NOI18N
        writer.writeAttribute("type", "text/javascript", null); // NOI18N
        writer.writeText(jsBuffer.toString(), null);
        writer.endElement("script");         // NOI18N
    }
    
    /**
     * <p>Render the appropriate element attributes for an icon button.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  attributes should be rendered
     * @param url The image URL
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderIconAttributes(FacesContext context,
            UIComponent component, ResponseWriter writer, String url)
            throws IOException {
        Button button = (Button) component;
        
        // Render type and source attributes.
        writer.writeAttribute("type", "image", null); //NOI18N
        
        // <RAVE>
        // Append context path to relative URLs
        url = context.getApplication().getViewHandler().
                getResourceURL(context, url);
        // <RAVE>
        
        RenderingUtilities.renderURLAttribute(context,
                writer,
                component,
                "src", //NOI18N
                url,
                "imageURL"); //NOI18N
    }
    
    /**
     * <p>Render the appropriate element attributes for a text button.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     *  attributes should be rendered
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderTextAttributes(FacesContext context,
            UIComponent component, ResponseWriter writer) throws IOException {
        Button button = (Button) component;
        
        // Is this a reset button?
        //
        // Note: the button type is not supported for usability (isReset and
        // isButton values would conflict).
        if (button.isReset()) {
            writer.writeAttribute("type", "reset", null); //NOI18N
        } else {
            writer.writeAttribute("type", "submit", null); //NOI18N
        }
        
        // Get the textual label of the button.
        String text = ConversionUtilities.convertValueToString
                (button, button.getValue());
        if (text == null || text.trim().length() == 0) {
            return;
        }
        
        // Pad the text if needed...
        //
        // Note: HCI_Admin used this code in their examples, but it may have
        // been for Netscape 4.x. We may be able to do this with styles instead.
        if (!button.isNoTextPadding()) {
            if (text.trim().length() <= 3) {
                text = "  " + text + "  "; //NOI18N
            } else if (text.trim().length() == 4) {
                text = " " + text + " "; //NOI18N
            }
        }
        
        // Render button text.
        if (button.isEscape()) {
            writer.writeAttribute("value", text, "text"); //NOI18N
        } else {
            // Note: This will close the starting tag -- see bugtraq #6315893.
            writer.write(text);
        }
    }
    
    /**
     * Get onblur style class.
     *
     * @param button <code>Button</code> to be rendered
     * @param theme <code>Theme</code> for the component
     */
    protected String getOnBlurStyle(Button button, Theme theme) {
        String style = null;
        
        
        if (button.getImageURL() != null) {
            style = theme.getStyleClass(ThemeStyles.BUTTON3);
        } else if (button.isMini() && !button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI);
        } else if(button.isMini()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI);
        } else if(!button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2);
        } else {
            style = theme.getStyleClass(ThemeStyles.BUTTON1);
        }
        
        return style;
    }
    
    /**
     * Get onfocus style class.
     *
     * @param button <code>Button</code> to be rendered
     *@param theme <code>Theme</code> for the component
     */
    protected String getOnFocusStyle(Button button, Theme theme) {
        String style = null;
        
        if (button.getImageURL() != null) {
            style = theme.getStyleClass(ThemeStyles.BUTTON3_HOVER);
        } else if (button.isMini() && !button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_MINI_HOVER);
        } else if(button.isMini()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_MINI_HOVER);
        } else if(!button.isPrimary()) {
            style = theme.getStyleClass(ThemeStyles.BUTTON2_HOVER);
        } else {
            style = theme.getStyleClass(ThemeStyles.BUTTON1_HOVER);
        }
        
        return style;
    }
    
    /**
     * Get onmouseover style class.
     *
     * @param button <code>Button</code> to be rendered
     * @param theme <code>Theme</code> for the component
     */
    protected String getOnMouseOverStyle(Button button, Theme theme) {
        // The getOnfocusStyle method shares the same style classes.
        return getOnFocusStyle(button, theme);
    }
    
    /**
     * Get onmouseout style class.
     *
     * @param button <code>Button</code> to be rendered
     * @param theme <code>Theme</code> for the component
     */
    protected String getOnMouseOutStyle(Button button, Theme theme) {
        // The getOnblurStyle method shares the same style classes.
        return getOnBlurStyle(button, theme);
    }
    
    /**
     * Get style class.
     *
     * @param button <code>Button</code> to be rendered
     * @param theme <code>Theme</code> for the component
     */
    protected String getStyle(Button button, Theme theme) {
        // styles should always be appended
        String style;  // button style from theme.
        if (button.getImageURL() != null) {
            style = ThemeStyles.BUTTON3;
        } else if (button.isMini() && !button.isPrimary()) {
            style = button.isDisabled()
            ? theme.getStyleClass(ThemeStyles.BUTTON2_MINI_DISABLED)
            : theme.getStyleClass(ThemeStyles.BUTTON2_MINI);
        } else if(button.isMini()) {
            style = button.isDisabled()
            ? theme.getStyleClass(ThemeStyles.BUTTON1_MINI_DISABLED)
            : theme.getStyleClass(ThemeStyles.BUTTON1_MINI);
        } else if(!button.isPrimary()) {
            style = button.isDisabled()
            ? theme.getStyleClass(ThemeStyles.BUTTON2_DISABLED)
            : theme.getStyleClass(ThemeStyles.BUTTON2);
        } else {
            style = button.isDisabled()
            ? theme.getStyleClass(ThemeStyles.BUTTON1_DISABLED)
            : theme.getStyleClass(ThemeStyles.BUTTON1);
        }
        
        return style;
    }
    
    
    /**
     * Helper method to set style classes during Javascript events such as
     * onblur, onfocus, onmouseover, and onmouseout.
     *
     * @param value The existing attribute value to append Javascript to.
     * @param jsmethod The JS event to invoke.
     */
    protected String getJavascript(String value, String jsmethod) {
        if (jsmethod == null) {
            return value;
        }
        
        String event = "return this" + ".my" + jsmethod +"();"; //NOI18N
        
        return (value != null) ? value + ";" + event : event; //NOI18N
    }
}
