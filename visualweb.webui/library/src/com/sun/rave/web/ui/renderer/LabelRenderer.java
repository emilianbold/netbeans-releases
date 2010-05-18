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

import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


/**
 * <p>Renderer for a {@link Label} component.</p>
 */

public class LabelRenderer extends Renderer {


    // ======================================================== Static Variables

    /**
     * <p>The set of additional String pass-through attributes to be rendered
     * if we actually create a <code>&lt;label&gt;</code> element.</p>
     */
    private static final String[] EVENT_NAMES = {
        "onClick",  "onMouseDown", "onMouseUp", // NOI18N
	"onMouseOver", "onMouseMove",  "onMouseOut" // NOI18N
    };
    
    public boolean getRendersChildren() {
         return true;
    }
    
    public void encodeChildren(FacesContext context, UIComponent component) 
        throws IOException { 
        return;
    }
   
    /**
     * <p>Render the appropriate element attributes, followed by the
     * label content, depending on whether the <code>for</code> property
     * is set or not.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>EditableValueHolder</code> component whose
     *  submitted value is to be stored
     * @exception IOException if an input/output error occurs
     */
     public void encodeEnd(FacesContext context, UIComponent component) 
         throws IOException {
         
         if(!(component instanceof Label)) { 
            Object[] params = { component.toString(), 
                                this.getClass().getName(), 
                                Label.class.getName() }; 
            String message = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
                 "Label.renderer", params);              //NOI18N
            throw new FacesException(message);  
        }  
     
        Label label = (Label)component;
      
        if (LogUtil.fineEnabled(LabelRenderer.class)) {
            LogUtil.fine(LabelRenderer.class, "Label.renderAttributes", // NOI18N
                         new Object[] { label.getId(), label.getFor() });
        }
                
        EditableValueHolder comp = label.getLabeledComponent();
        boolean requiredFlag = label.isRequiredIndicator();
        boolean errorFlag = false; 
        
        if(!label.isHideIndicators() && comp != null) {
            Object o = ((UIComponent)comp).getAttributes().get("readOnly"); 
            if(o != null && o instanceof Boolean && o.equals(Boolean.TRUE)) { 
                requiredFlag = false; 
                errorFlag = false; 
            } 
           else {
                requiredFlag = comp.isRequired();
                errorFlag = !comp.isValid();
           }
        }
        
        Theme theme = ThemeUtilities.getTheme(context);  
        String styleClass = getThemeStyleClass(label, theme, errorFlag);    
        ResponseWriter writer = context.getResponseWriter();
        
        startElement(label, (UIComponent)comp, styleClass, writer, context);
        
        if(requiredFlag) { 
            writer.write("\n"); //NOI18N   
            RenderingUtilities.renderComponent
                    (label.getRequiredIcon(theme, context), context);
          
        }
        if(errorFlag) { 
           writer.write("\n"); //NOI18N   
           RenderingUtilities.renderComponent
                    (label.getErrorIcon(theme, context, false), context);
                  }

        // Render the label text
        String value = formatLabelText(context, label);
        if(value != null) {
            writer.write("\n"); //NOI18N   
            writer.writeText(value, "text"); //NOI18N
            writer.writeText("\n", null); //NOI18N
        }
           
        // Note: the for attribute has been set, so we render the end of 
        // the label tag *before* we render the children. Otherwise we 
        // will inadvertently set the font for the child components.
        writer.endElement(label.getElement()); 
         
        Iterator children = label.getChildren().iterator(); 
        while(children.hasNext()) { 
            RenderingUtilities.renderComponent((UIComponent)children.next(),
                                               context); 
            writer.writeText("\n", null); //NOI18N
        }
        
       
        
        if (LogUtil.finestEnabled(LabelRenderer.class)) {
            LogUtil.finest(LabelRenderer.class, "Label.renderEnd"); // NOI18N
        }
    }
     
    private void startElement(Label label, UIComponent labeledComponent, 
                              String styleClass, ResponseWriter writer, 
			      FacesContext context)    
        throws IOException { 
        
        writer.startElement(label.getElement(), label);
        writer.writeAttribute("id", label.getClientId(context), "id"); //NOI18N
	String id = label.getLabeledComponentId(context); 
        if(id != null) { 
            writer.writeAttribute("for", id, "for");
        }
        
        RenderingUtilities.renderStyleClass(context, writer, label, styleClass);
        
        if (label.getStyle() != null) {
            writer.writeAttribute("style", label.getStyle(), "style"); //NOI18N
        }
        // Render the "toolTip" properties
        String toolTip = label.getToolTip();
        if (toolTip != null) {
            writer.writeAttribute("title", toolTip, "toolTip"); // NOI18N
        }
        writeEvents(label, writer);
    }

  
    /**
     * <p>Return the text to be rendered for this label.  This will be either
     * the literal value of the <code>text</code> property, or the use of
     * that value as a <code>MessageFormat</code> string, using nested
     * <code>UIParameter</code> children as the source of replacement values.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     * @param label <code>Label</code> we are rendering
     */
    private String formatLabelText(FacesContext context, Label label) {
        String text = ConversionUtilities.convertValueToString
                (label, label.getValue());
        text = text.concat(" ");
        if (label.getChildCount() == 0) {
            return text;
        }
        List list = new ArrayList();
        Iterator kids = label.getChildren().iterator();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (kid instanceof UIParameter) {
                list.add(((UIParameter) kid).getValue());
            }
        }
        if (list.size() == 0) {
            return text;
        }
        return MessageFormat.format(text, list.toArray(new Object[list.size()]));
    }
    
    private String getThemeStyleClass(Label label, 
                                      Theme theme,
                                      boolean errorFlag) { 

        String style = null;
        int level = label.getLabelLevel();
        
        if (errorFlag) {
            style = theme.getStyleClass(ThemeStyles.CONTENT_ERROR_LABEL_TEXT);
        } else if (level == 1) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_ONE_TEXT);
        } else if (level == 2) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_TWO_TEXT);
        } else if (level == 3) {
            style = theme.getStyleClass(ThemeStyles.LABEL_LEVEL_THREE_TEXT);
        }
        return style;
    }
 
    private void writeEvents(Label label, ResponseWriter writer)       
           throws IOException {
      
        Map attributes = label.getAttributes();
        Object value;
        int length = EVENT_NAMES.length;
        for(int i = 0; i < length; i++) {
            value = attributes.get(EVENT_NAMES[i]);
            if(value != null) {
                if(value instanceof String) {
                    writer.writeAttribute(EVENT_NAMES[i].toLowerCase(),
                            (String) value, EVENT_NAMES[i]);
                } 
                else {
                    writer.writeAttribute(EVENT_NAMES[i].toLowerCase(),
                            value.toString(), EVENT_NAMES[i]);
                }
            }
        }       
    }    
}
