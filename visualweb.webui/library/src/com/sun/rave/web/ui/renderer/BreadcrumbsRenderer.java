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

import com.sun.rave.web.ui.component.Breadcrumbs;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.beans.Beans;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;

/**
 * <p>Renderer for a {@link Breadcrumbs} component.</p>
 * <p>This class renders a breadcrumb or parentage path.</p>
 */
public class BreadcrumbsRenderer extends AbstractRenderer {
    
    /** Creates a new instance of BreadcrumbsRenderer */
    public BreadcrumbsRenderer() {
        // default constructor
    }    

    /**
     * Overrides encodeChildren of Renderer to do nothing. This
     * class renders its own children, but not through this
     * method. 
     *
     * @param context The FacesContext of the request
     * @param component The component associated with the
     * renderer. 
     */
    public void encodeChildren(FacesContext context, 
            UIComponent component) throws java.io.IOException { 
	return;
    } 
    
    /**
     * Returns a flag indicating that this component is responsible 
     * for rendering it's children.
     * 
     */
    public boolean getRendersChildren() {
	return true;
    }    

    /**
     * Renders the attributes for the table containing the breadcrumbs.
     * 
     * @param context The current FacesContext
     * @param breadcrumbs The Breadcrumbs object to use
     * @param theme The theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderContainingTable(FacesContext context, 
                                         Breadcrumbs breadcrumbs, 
                                         Theme theme, 
                                         ResponseWriter writer) 
        throws IOException {
        // Render the opening table
        
        writer.startElement("div", breadcrumbs); //NOI18N
        String tdStyle = theme.getStyleClass(ThemeStyles.BREADCRUMB_WHITE_DIV);        
        addCoreAttributes(context, breadcrumbs, writer, tdStyle);
        writer.writeText("\n", null); //NOI18N           
    }
    
    /**
     * Renders the separator between the elements of the breadcrumbs.
     * 
     * @param context The current FacesContext
     * @param breadcrumbs The Breadcrumbs object to use
     * @param theme The theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderBreadcrumbsSeparator(FacesContext context, 
                                              Breadcrumbs breadcrumbs, 
                                              Theme theme, 
                                              ResponseWriter writer) 
        throws IOException {
        writer.startElement("span", breadcrumbs); //NOI18N
        String separatorStyle = 
            theme.getStyleClass(ThemeStyles.BREADCRUMB_SEPARATOR);
        writer.writeAttribute("class", separatorStyle, null); //NOI18N        
        // TODO: Replace spearator with themeable glyph/text
        writer.write("&gt;"); //NOI18N         
        writer.endElement("span"); //NOI18N
    }

    /**
     * Renders the hyperlinks which make up the breadcrumbs.
     *
     * @param context The current FacesContext
     * @param link The component representing a page in the breadcrumbs.
     * @param theme The theme to use
     * This must be a Hyperlink or subclass of a Hyperlink.
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderBreadcrumbsLink(FacesContext context,
                                         Hyperlink link, 
                                         Theme theme) 
        throws IOException {
	String linkStyle = theme.getStyleClass(ThemeStyles.BREADCRUMB_LINK);        
        
	Map attributes = link.getAttributes();
	if (attributes != null && 
	    	attributes.get("styleClass") == null) { //NOI18N
	    attributes.put("styleClass", linkStyle); //NOI18N
	}
	RenderingUtilities.renderComponent(link, context);
    }

    /**
     * Renders the final breadcrumb text which represents the current page.
     *
     * @param context The current FacesContext
     * @param breadcrumbs The Breadcrumbs object to use
     * @param pageName The current page name.
     * @param theme The theme to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderBreadcrumbsText(FacesContext context, 
                                         Hyperlink crumb, 
                                         Theme theme, 
                                         ResponseWriter writer) 
        throws IOException {
        
        // <RAVE>
        // String pageName = crumb.getText();
        String pageName = ConversionUtilities.convertValueToString(crumb, crumb.getText());
        // </RAVE>
        if (pageName == null || pageName.length() <= 0)
            return;
        
        writer.startElement("span", crumb); //NOI18N
        String textStyle = theme.getStyleClass(ThemeStyles.BREADCRUMB_TEXT);
        writer.writeAttribute("class", textStyle, null); //NOI18N        
        writer.writeText(pageName, null);
        writer.endElement("span"); //NOI18N
        writer.endElement("div"); //NOI18N
    }

    /**
     * Renders the breadcrumbs.
     * 
     * @param context The current FacesContext
     * @param component The Breadcrumbs object to use
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurss
     */
    protected void renderEnd(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        if (context == null || component == null || writer == null) {
            throw new NullPointerException();
        }
        
        Breadcrumbs breadcrumbs = (Breadcrumbs) component;
              
        if (!breadcrumbs.isRendered()) {
            return;
        }

        // Get the list of pages. If pages are supplied as an array of hyperlink
        // components, temporarily set their parent to this breadcrumb, so that
        // when they render they will find this component's ancestor form
	UIComponent[] pages = null;
        if (breadcrumbs.getPages() != null) {
	    pages = breadcrumbs.getPages();
            for (int i = 0; i < pages.length; i++)
                pages[i].setParent(component);
        } else if (breadcrumbs.getChildren() != null) {
	    List list = breadcrumbs.getChildren();
	    pages = (UIComponent[]) list.
		toArray(new UIComponent[list.size()]);
        }
        
        if (pages == null || (pages.length <= 1 && !Beans.isDesignTime()))
            return;
        
        Theme theme = ThemeUtilities.getTheme(context); 

	// Render containing table
	renderContainingTable(context, breadcrumbs, theme, writer);
	
	int length = pages.length;
	// Iterate through the array of hyperlinks
	for (int i = 0; i < length; i++) {
	    // pages must be hyperlinks (or a subclass of hyperlinks)
	    Hyperlink crumb = (Hyperlink) pages[i];

	    // Check if this is the last breadcrumb
	    if (i < (length - 1)) {
		// Render a hyperlink 
		renderBreadcrumbsLink(context, crumb, theme);
		renderBreadcrumbsSeparator(context, breadcrumbs, theme, writer);
	    } else {
		// Render static text - for the final page in the breadcrumbs
                renderBreadcrumbsText(context, crumb, theme, writer);
	    }
	}
    }
}
