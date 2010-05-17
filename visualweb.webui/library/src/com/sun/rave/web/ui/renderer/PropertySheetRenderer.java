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

import com.sun.rave.web.ui.component.Anchor;
import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.component.IconHyperlink;
import com.sun.rave.web.ui.component.Legend;
import com.sun.rave.web.ui.component.PropertySheet;
import com.sun.rave.web.ui.component.PropertySheetSection;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Renders a PropertySheet component.
 */
public class PropertySheetRenderer extends javax.faces.render.Renderer {

    public static final String JUMPTOSECTIONTOOLTIP =
		    "propertySheet.jumpToSectionTooltip";

    public static final String JUMPTOTOPTOOLTIP =
		    "propertySheet.jumpToTopTooltip";

    public static final String JUMPTOTOP =
		    "propertySheet.jumpToTop";

    /**
     * Creates a new instance of PropertySheetRenderer.
     */
    public PropertySheetRenderer() {
    }
    
    /**
     * This renderer renders the component's children.
     * @returns true
     */
    public boolean getRendersChildren() {
	return true;
    }

    /**
     * Render a property sheet.
     * 
     * @param context The current FacesContext
     * @param component The PropertySheet object to render
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

	PropertySheet propertySheet = (PropertySheet) component;

	// Get the theme
	//
	Theme theme = ThemeUtilities.getTheme(context);

	writer.startElement("div", propertySheet);
	writer.writeAttribute("id",
	    component.getClientId(context), "id");//NOI18N

	String propValue = RenderingUtilities.getStyleClasses(context,
		component, theme.getStyleClass(ThemeStyles.PROPERTY_SHEET));
	writer.writeAttribute("class", propValue, null);

        propValue = propertySheet.getStyle();
	if (propValue != null) {
	    writer.writeAttribute("style", propValue, 
		"style");
	}

	renderJumpLinks(context, propertySheet, theme, writer);
	renderRequiredFieldsLegend(context, propertySheet, theme, writer);
	renderPropertySheetSections(context, propertySheet, theme, writer);

        writer.endElement("div");
    }    

    /**
     * Render the property sheet sections.
     * 
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderPropertySheetSections(FacesContext context,
	    PropertySheet propertySheet, Theme theme, ResponseWriter writer)
	    throws IOException {

	// From propertysheetsection template
	// 
	// <!-- Separator before Section (except first one, unless jumplinks
	// are rendered) -->
	// This has to be done here since we know if jumpllinks were 
	// rendered, not the section. There was probably a request map
	// attribute set to convey this. We control the spacer too.
	//
	List sections = propertySheet.getVisibleSections();
	boolean haveJumpLinks = propertySheet.isJumpLinks() &&
		sections.size() > 1;
	boolean renderSpacer = false;
        Iterator sectionsIterator = sections.iterator();
	while (sectionsIterator.hasNext()) {
            PropertySheetSection section = (PropertySheetSection) sectionsIterator.next();
	    renderAnchor(context, section, writer);

	    // Orginally the spacer came after the section's 
	    // opening div. Let's do it before. It should be equivalent.
	    // And the PropertySheet should control separators.
	    //
	    // If there are jumplinks render a spacer if there is more
	    // than one section.
	    // If there are no jumplinks, render a spacer unless
	    // it is the first section.
	    //
	    if (haveJumpLinks || renderSpacer) {
		renderSpacer(context, section, theme, writer);
	    } else {
		renderSpacer = true;
	    }
	    RenderingUtilities.renderComponent((UIComponent)section, context);

	    if (haveJumpLinks && sections.size() > 1) {
		renderJumpToTopLink(context, propertySheet, theme, writer);
	    }
	}
    }

    /**
     * Render a required fields legend.
     * If <code>propertySheet.getRequiredFields</code> returns null
     * a spacer is rendered.
     * 
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderRequiredFieldsLegend(FacesContext context,
	    PropertySheet propertySheet, Theme theme, ResponseWriter writer)
	    throws IOException {

	// This should be a facet.
	//
	String requiredFields = propertySheet.getRequiredFields();
	// Why isn't this boolean ?
	//
	if (requiredFields != null && 
		requiredFields.equalsIgnoreCase("true")) {//NOI18N
	    Legend legend = new Legend();
	    legend.setId(propertySheet.getId() + "_legend"); //NOI18N
	    //legend.setText(requiredFields);
	    // FIXME : This MUST become a CSS selector.
	    //
	    legend.setStyle("margin:0pt 10px 0pt 0px"); //NOI18N
	    RenderingUtilities.renderComponent(legend, context);
	} else {
	    // FIXME : Needs to be theme.
	    //
	    Icon spacer = theme.getIcon(ThemeImages.DOT);
	    spacer.setHeight(20);
	    spacer.setWidth(1);
	    RenderingUtilities.renderComponent(spacer, context);
	}
    }

    /**
     * Render a set of jump links.
     * 
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet object to render
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderJumpLinks(FacesContext context,
	    PropertySheet propertySheet, Theme theme, ResponseWriter writer)
	    throws IOException {

	// Don't render any jump links if they are not requested.
	//
	if (!propertySheet.isJumpLinks()) {
	    return;
	}
	// Or if there are no visible sections
	//
	List sections = propertySheet.getVisibleSections();
	int numSections = sections.size();
	if (numSections <= 1) {
	    return;
	}

	// There seems to be a distinction if there are 4, 5 to 9,
	// and greater than 9 property sheet sections. This should be a
	// theme configurable parameter.
	//
	// If there are less than 5 sections, there will be
	// 2 jump links per row.
	// If there are greater than 5 sections there will be
	// 3 junmp links per row.
	// If there are more than 10 sections, there will be
	// 4 jump links per row.
	//
	// Determine the number of sections
	//
	// Start the layout for the property sheet sections
	// jump link area
	//
	int jumpLinksPerRow = numSections < 5 ? 2 : 
		(numSections > 4 && numSections < 10 ? 3 : 4);

	// Start a div for the jump links table
	//
	writer.startElement("div", propertySheet);
	writer.writeAttribute("class", 
	    theme.getStyleClass(ThemeStyles.CONTENT_JUMP_SECTION_DIV), null);

	writer.startElement("table", propertySheet);
	writer.writeAttribute("border", "0", null);
	writer.writeAttribute("cellspacing", "0", null);
	writer.writeAttribute("cellpadding", "0", null);
	writer.writeAttribute("title", "", null); //NOI18N

	// Optimize, just get the needed selectors once.
	//
	String jumpLinkDivStyle =
	    theme.getStyleClass(ThemeStyles.CONTENT_JUMP_LINK_DIV);
	String jumpLinkStyle =
	    theme.getStyleClass(ThemeStyles.JUMP_LINK);

	Iterator sectionIterator = sections.iterator();
	while (sectionIterator.hasNext()) {

	    writer.startElement("tr", propertySheet);

	    for (int i = 0; i < jumpLinksPerRow; ++i) {

		PropertySheetSection section = 
		    (PropertySheetSection)sectionIterator.next();

		writer.startElement("td", propertySheet);
		writer.startElement("span", propertySheet);
		writer.writeAttribute("class", jumpLinkDivStyle,
		    null);

		IconHyperlink jumpLink = new IconHyperlink();
		jumpLink.setId(section.getId() + "_jumpLink");//NOI18N
		jumpLink.setParent(propertySheet);
		jumpLink.setIcon(ThemeImages.HREF_ANCHOR);
		jumpLink.setBorder(0);
		// Shouldn't this come from the section ?
		//
		String propValue = theme.getMessage(JUMPTOSECTIONTOOLTIP);
		if (propValue != null) {
		    jumpLink.setAlt(propValue);
		    jumpLink.setToolTip(propValue);
		}
	
		propValue = section.getLabel();
		if (propValue != null) {
		    jumpLink.setText(propValue);
		}
		jumpLink.setUrl("#_" + section.getId());
		jumpLink.setStyleClass(jumpLinkStyle);

		// Render the jump link
		// 
		RenderingUtilities.renderComponent(jumpLink, context);

		writer.endElement("span");
		writer.endElement("td");

		// If we haven't created enough cells, we should.
		//
		if (!sectionIterator.hasNext()) {
		    while (++i < jumpLinksPerRow) {
			writer.startElement("td", propertySheet);
			writer.startElement("span", propertySheet);
			writer.writeAttribute("class", 
			    jumpLinkDivStyle, null);
			writer.endElement("span");
			writer.endElement("td");
		    }
		    break;
		}
	    }
	    writer.endElement("tr");
	}
	writer.endElement("table");
	writer.endElement("div");
    }

    /**
     * Does not participate in rendering a PropertySheet.
     * 
     * @param context The current FacesContext
     * @param component The PropertySheet object to render
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

    /**
     * Render an anchor to the section.
     * 
     * @param context The current FacesContext
     * @param propertySheetSection The PropertySheetSection about to be rendered.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderAnchor(FacesContext context,
	    PropertySheetSection propertySheetSection, ResponseWriter writer)
	    throws IOException {

	Anchor anchor = new Anchor();
	anchor.setParent(propertySheetSection);
	anchor.setId("_" + propertySheetSection.getId());//NOI18N
	RenderingUtilities.renderComponent(anchor, context);
    }

    /**
     * Render a spacer before the section.
     * 
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet being rendered
     * @param propertySheetSection The PropertySheetSection about to be rendered.
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderSpacer(FacesContext context,
	    PropertySheetSection propertySheetSection, Theme theme,
	    ResponseWriter writer)
	    throws IOException {

	Icon spacer = theme.getIcon(ThemeImages.DOT);
	writer.startElement("div", null);
	writer.writeAttribute("class",
		theme.getStyleClass(ThemeStyles.CONTENT_LIN), null);

	spacer.setId(propertySheetSection.getId() + "_dot1"); //NOI18N
	spacer.setParent(propertySheetSection);
	spacer.setHeight(1);
	spacer.setWidth(1);
	RenderingUtilities.renderComponent(spacer, context);
	writer.endElement("div");
    }

    /**
     * Render the back to top link
     * 
     * @param context The current FacesContext
     * @param propertySheet The PropertySheet being rendered
     * @param propertySheetSection The PropertySheetSection about to be rendered.
     * @param theme The Theme to reference.
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    private void renderJumpToTopLink(FacesContext context,
	    PropertySheet propertySheet, Theme theme, ResponseWriter writer)
	    throws IOException {

	writer.startElement("div", propertySheet);
	writer.writeAttribute("class",
	    theme.getStyleClass(ThemeStyles.CONTENT_JUMP_TOP_DIV), null);

	// Should be facets ?
	//
	IconHyperlink jumpLink = new IconHyperlink();
	jumpLink.setIcon(ThemeImages.HREF_TOP);
	jumpLink.setBorder(0);
	// Shouldn't this come from the section ?
	//
	String propValue = theme.getMessage(JUMPTOTOPTOOLTIP);
	if (propValue != null) {
	    jumpLink.setAlt(propValue);
	    jumpLink.setToolTip(propValue);
	}

	propValue = theme.getMessage(JUMPTOTOP);
	if (propValue != null) {
	    jumpLink.setText(propValue);
	}
	jumpLink.setUrl("#"); //NOI18N
	jumpLink.setStyleClass(theme.getStyleClass(
	    ThemeStyles.JUMP_TOP_LINK));

	// Render the jump link
	// 
	RenderingUtilities.renderComponent(jumpLink, context);

	writer.endElement("div");
    }
}
