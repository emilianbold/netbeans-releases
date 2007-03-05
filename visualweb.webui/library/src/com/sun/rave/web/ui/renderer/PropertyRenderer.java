/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.renderer;

import com.sun.rave.web.ui.component.Property;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


public class PropertyRenderer extends Renderer {

    /**
     * Creates a new instance of PropertyRenderer.
     */
    public PropertyRenderer() {
    }

    /**
     * This renderer renders the component's children.
     */
    public boolean getRendersChildren() {
	return true;
    }

    /**
     * Render a property component.
     *
     * @param context The current FacesContext
     * @param component The Property object to render
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeEnd(FacesContext context, UIComponent component)
            throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

	if (!component.isRendered()) {
	    return;
	}

	ResponseWriter writer = context.getResponseWriter();
	Property property = (Property)component;

	// Get the theme
	//
	Theme theme = ThemeUtilities.getTheme(context);

	// Ideally the PropertyRenderer shouldn't have to know 
	// what it is contained by. But this implementation assumes
	// that a table has been used as the container and therefore
	// the responsibility for rendering rows and cells
	// is left to the PropertyRenderer. This is where a 
	// LayoutComponent could be useful. The PropertyLayoutComponent
	// knows that a Property consists of a Label and some other
	// components and lays it out accordingly.
	//
	writer.startElement("tr", property);

	// Its not clear if the developer realizes that the styleClass
	// and style attributes are applied to a "tr".
	//
        String propValue = RenderingUtilities.getStyleClasses(context,
		component, null);
	if (propValue != null) {
	    writer.writeAttribute("class", propValue, null);
	}

        propValue = property.getStyle();
	if (propValue != null) {
	    writer.writeAttribute("style", propValue, 
		"style");
	}

	writer.startElement("td", property);
	writer.writeAttribute("valign", "top", null);//NOI18N

	// Always render the nowrap, and labelAlign even if there
	// isn't a label.
	//
	boolean nowrap = property.isNoWrap();
	if (nowrap) {
	    writer.writeAttribute("nowrap", "nowrap", null); //NOI18N
	}
	String labelAlign = property.getLabelAlign();
	if (labelAlign != null) {
	    writer.writeAttribute("align", labelAlign, null);
	}

	// If overlapLabel is true, then render the TD with colspan 2.
	// and the property components share the div.
	// If overlapLabel is false then render two TD's and two DIV's
	//
	boolean overlapLabel = property.isOverlapLabel();
	if (overlapLabel) {
	    writer.writeAttribute("colspan", "2", null);//NOI18N
	}

	writer.startElement("div", property);
	writer.writeAttribute("class", 
	    theme.getStyleClass(ThemeStyles.CONTENT_TABLE_COL1_DIV), null);

	renderLabel(context, property, theme, writer);


	// If overlapLabel is false, close the label TD and DIV
	// and open another TD and DIV for the property components.
	//
	if (!overlapLabel) {
	    writer.endElement("div");
	    writer.endElement("td");
	    writer.startElement("td", property);
	    writer.startElement("div", property);
	    writer.writeAttribute("class",
		theme.getStyleClass(ThemeStyles.CONTENT_TABLE_COL2_DIV), null);
	}

	renderPropertyComponents(context, property, theme, writer);
	renderHelpText(context, property, theme, writer);
	
	writer.endElement("div");
	writer.endElement("td");
	writer.endElement("tr");
    }

    /**
     * Render the property components.
     * If the <code>content</code> facet it defined it takes precendence
     * over the existence of child components. If there is not
     * <code>content</code> facet, then the children are rendered.
     * 
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderPropertyComponents(FacesContext context,
	    Property property, Theme theme, 
	    ResponseWriter writer) throws IOException {

	// The presence of a content facet takes priority over
	// children. I'm not sure why there is both a content facet
	// and children.
	//
	UIComponent content = property.getContentComponent();
	if (content != null) {
	    RenderingUtilities.renderComponent(content, context);
	    return;
	}

	// If there isn't a content facet, render any children.
	//
	Iterator childrenIterator = property.getChildren().iterator();
	while (childrenIterator.hasNext()) {
            UIComponent child = (UIComponent) childrenIterator.next();
	    RenderingUtilities.renderComponent(child, context);
	}
    }

    /**
     * Render a label for the propertySheetSection
     * 
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderLabel(FacesContext context,
	    Property property, Theme theme,
	    ResponseWriter writer) throws IOException {
	
	// Render a label facet if there is one, else a span
	// and "&nbsp".
	//
	UIComponent label = property.getLabelComponent();
	if (label != null) {
	    RenderingUtilities.renderComponent(label, context);
	    return;
	}

	writer.startElement("span", property);

	// The class selector should be minimally themeable. 
	// Not sure why we even need it for "nbsp".
	// Other components offer a labelLevel attribute as well.
	//
	writer.writeAttribute("class", 
	    theme.getStyleClass(ThemeStyles.LABEL_LEVEL_TWO_TEXT), null);
	writer.write("&nbsp;"); // NOI18N
	writer.endElement("span");
    }

    /**
     * Render help text for the property
     * 
     * @param context The current FacesContext
     * @param property The Property object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderHelpText(FacesContext context,
	    Property property, Theme theme,
	    ResponseWriter writer) throws IOException {

	// Render a help text facet if there is one
	//
	UIComponent helpText = property.getHelpTextComponent();
	if (helpText != null) {
	    RenderingUtilities.renderComponent(helpText, context);
	}
    }

    /**
     * Does nothing.
     * 
     * @param context The current FacesContext
     * @param component The Property object to render
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    public void encodeChildren(FacesContext context, UIComponent component)
            throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }    
}
