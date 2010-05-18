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
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;
import com.sun.rave.web.ui.component.Field;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.Upload;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;


/**
 * <p>Renderer for {@link com.sun.rave.web.ui.component.TextField} components.</p>
 */
public class FieldRenderer extends Renderer {

    /**
     * <p>The list of attribute names in the HTML 4.01 Specification that
     * correspond to the entity type <em>%events;</em>.</p>
     */
    public static final String[] STRING_ATTRIBUTES = 
        { "onBlur", "onChange", "onClick", "onDblClick",        //NOI18N
	  "onFocus", "onMouseDown", "onMouseUp", "onMouseOver", //NOI18N
	  "onMouseMove", "onMouseOut",                          //NOI18N
	  "onKeyDown", "onKeyPress", "onKeyUp", "onSelect"      //NOI18N
	};

    public static final String SPACER_ID = "_spacer";
    
    private final static boolean DEBUG = false;       
    
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
          
        if(!(component instanceof Field)) { 
            Object[] params = { component.toString(), 
                                this.getClass().getName(), 
                                Field.class.getName() };
            String message = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                 "Renderer.component", params);              //NOI18N
            throw new FacesException(message);  
        }

        renderField(context, (Field)component, "text", getStyles(context));
    }
     
    /**
     * <p>Render the TextField depending on the value of the
     * <code>type</code> property.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @exception IOException if an input/output error occurs
     */
    public boolean renderField(FacesContext context, Field component, String type, 
                            String[] styles)
        throws IOException {
        
        String id = component.getClientId(context); 
        ResponseWriter writer = context.getResponseWriter();
        UIComponent label = component.getLabelComponent(context, styles[3]);
        boolean spanRendered = false; 

        if( label != null) {
            renderOpeningSpan(component, id, styles[2], writer);
	    spanRendered = true; 
            writer.writeText("\n", null);
            RenderingUtilities.renderComponent(label, context);
            writer.writeText("\n", null);
            
            // Add a 10 px spacer image after the label text so that the
            // label has the right distance (10px) from the field, since
            // a developer has no control over the layout in this case.                       
            Theme theme = ThemeUtilities.getTheme(context); 
            Icon icon = theme.getIcon(ThemeImages.DOT); 
            icon.setId(component.getId().concat(SPACER_ID));
            icon.setHeight(1);
            icon.setWidth(10);               
            RenderingUtilities.renderComponent(icon, context);
            writer.writeText("\n", null); //NOI18N	            
            
            // Set the ID to use on the inner component
            if(component instanceof Upload) {
                id = id.concat(Upload.INPUT_ID);
            } else {
                id = id.concat(Field.INPUT_ID);
            }
        }
        if(component.isReadOnly()) {
            UIComponent text = component.getReadOnlyComponent(context);
            if(label == null) { 
               text.getAttributes().put("style", component.getStyle());
               text.getAttributes().put("styleClass", component.getStyleClass());
            }
            RenderingUtilities.renderComponent(text, context);
        } else {
            renderInput(component, type, id, label == null, styles, context, writer);
        }
        if( label != null) {
            writer.writeText("\n", null);
            writer.endElement("span");
        }
	return spanRendered;
    }
       
    /**
     * <p>Render the TextField depending on the value of the
     * <code>type</code> property.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @exception IOException if an input/output error occurs
     */
    private void renderOpeningSpan(Field component, String id, 
                                   String hiddenStyle, ResponseWriter writer) 
        throws IOException {
        
        writer.startElement("span", component); //NOI18N
        writer.writeAttribute("id", id , "id"); //NOI18N
        
        
        String style = component.getStyle();
        if(style != null && style.length() > 0) {
            writer.writeAttribute("style", style, "style"); //NOI18N
        }
        
        style = getStyleClass(component, hiddenStyle); 
        if(style != null) {
            writer.writeAttribute("class", style, "class"); //NOI18N
        }
    }

    protected void renderInput(Field component, String type, String id, 
                             boolean renderUserStyles, String[] styles,
                             FacesContext context, ResponseWriter writer)
        throws IOException {
        
        if(component instanceof TextArea) {
            writer.startElement("textarea", component); //NOI18N        
            int rows = ((TextArea)component).getRows();
            if(rows > 0) {
                writer.writeAttribute("rows", String.valueOf(rows), "rows"); // NOI18N
            }
                    int columns = component.getColumns(); 
                    
            if (columns > 0) {
                writer.writeAttribute("cols",     //NOI18N
                    String.valueOf(columns),
                    "columns"); //NOI18N
            }
        } 
        else {
            writer.startElement("input", component); //NOI18N
            writer.writeAttribute("type", type, null); //NOI18N
            
            int columns = component.getColumns(); 
            if (columns > 0) {
                writer.writeAttribute("size",     //NOI18N
                        String.valueOf(columns),
                        "columns"); //NOI18N
            }
        }
        
        writer.writeAttribute("id", id , null); //NOI18N
        writer.writeAttribute("name", id, null); //NOI18N
        
        if (component.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", null); //NOI18N
        }
        

               
        int maxlength = component.getMaxLength();
        if (maxlength > 0) {
            writer.writeAttribute("maxlength",
                    String.valueOf(maxlength),
                    "maxLength"); // NOI18N
        }
        
        // Render attributes based on the "toolTip" properties
        String toolTip = component.getToolTip();
        if (toolTip != null && toolTip.length() > 0) {
            writer.writeAttribute("title", toolTip, "toolTip"); // NOI18N
        }
        
        int tabIndex  = component.getTabIndex();
        if(tabIndex > 0) {
            writer.writeAttribute("tabindex", // NOI18N
                    String.valueOf(tabIndex),
                    "tabIndex"); // NOI18N
        }
       
        RenderingUtilities.writeStringAttributes(component, writer,
                STRING_ATTRIBUTES);
        
        String styleClass = null; 
        if(component.isDisabled()) {
            styleClass = styles[1];
        } else {
            styleClass = styles[0];
        }
        
        String style = null; 
        
        if(renderUserStyles) { 
            
            String compStyleClass = getStyleClass(component, styles[2]); 
            if(compStyleClass != null) { 
                styleClass = compStyleClass + " " + styleClass; 
            }
            
            style = component.getStyle(); 
            if(style != null && style.length() == 0) { 
                style = null; 
            } 
        } 
       
        writer.writeAttribute("class", styleClass, null); //NOI18N
        if(style != null) { 
            writer.writeAttribute("style", style, null); //NOI18N
        } 
           
	// Record the value that is rendered.
	// Note that getValueAsString conforms to JSF conventions
	// for NULL values, in that it returns "" if the component
	// value is NULL. This value cannot be trusted since
	// the fidelity of the data must be preserved, i.e. if the
	// value is null, it must remain null if the component is unchanged
	// by the user..
	//
	// If submittedValue is not null then the component's
	// model value or local value has not been updated
	// therefore assume that this is an immediate or premature
	// render response. Therefore just assume that if the rendered
	// value was null, the saved information is still valid.
	// 
	if (component.getSubmittedValue() == null) {
	    ConversionUtilities.setRenderedValue(component,
		component.getValue());
	}

	// Still call the component's getValueAsString method
	// in order to render it.
	//
	String value = component.getValueAsString(context);
        if(component instanceof TextArea) {
            writer.writeText(value, "value"); //NOI18N
            writer.endElement("textarea");
        }
        else { 
            if(value != null) {
                 writer.writeAttribute("value", value, "value"); //NOI18N             
            }        
            writer.endElement("input");
        }
    }
    
    // Prepend the hidden style to the user's added style if necessary
    protected String getStyleClass(Field component, String hiddenStyle) {
        
        String style = component.getStyleClass();
        if(style != null && style.length() == 0) {
            style = null;
        }
        
        if(!component.isVisible()) {
            if(style == null) {
                style = hiddenStyle;
            } else {
                style = style + " " + hiddenStyle;
            }
        }
        return style;
    }
    
    String[] getStyles(FacesContext context) {
        Theme theme = ThemeUtilities.getTheme(context);
        String[] styles = new String[4];
        styles[0] = theme.getStyleClass(ThemeStyles.TEXT_FIELD);
        styles[1] = theme.getStyleClass(ThemeStyles.TEXT_FIELD_DISABLED);     
        styles[2] = theme.getStyleClass(ThemeStyles.HIDDEN);
        styles[3] = "";
        return styles;
    }
    
    /**
     * Log an error - only used during development time.
     */
    void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }

    /**
     * Decode the component component
     * @param context The FacesContext associated with this request
     * @param component The TextField component to decode
     */
    public void decode(FacesContext context, UIComponent component) {
        HiddenFieldRenderer.decodeInput(context, component); 
    }

    /**
     * <p>No-op.</p>
     * 
     * @param context {@link FacesContext} for the response we are creating
     * @param component {@link UIComponent} whose children are to be rendered
     * 
     * @exception IOException if an input/output error occurs while rendering
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public void encodeChildren(FacesContext context, UIComponent component) {
        return;
    }

    /**
     * <p>No-op</p>
     * 
     * @param context {@link FacesContext} for the request we are processing
     * @param component {@link UIComponent} to be rendered
     */
    public void encodeBegin(FacesContext context, UIComponent component) {
        return;
    }

    /**
     * <p>Returns true, meaning that the FieldRenderer is responsible
     * for rendering the children the component it is asked to render.</p>
     * @return false;
     */
    public boolean getRendersChildren() {
        return true;
    }
    
}
