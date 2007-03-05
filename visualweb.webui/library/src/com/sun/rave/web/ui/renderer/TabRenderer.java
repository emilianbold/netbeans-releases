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

import com.sun.rave.web.ui.util.ConversionUtilities;
import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIComponent;

import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.TabSet;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>Renders a Tab component.</p>
 *
 * <p>A Tab is a Hyperlink that, when clicked, also udpates the
 * lastSelectedChild value of any parent Tab instance as well as the selected
 * value of the enclosing TabSet component.</p>
 *
 * @author  Sean Comerford
 */
public class TabRenderer extends HyperlinkRenderer {
    
    /** Default constructor */
    public TabRenderer() {
        super();
    }
    
    /**
     * This method is always called by the base class (HyperlinkRenderer)
     * renderEnd method. TabRenderer should NOT render any Tab children as the
     * enclosing TabSet component will do so (if necessary).
     *
     * @param context The current FacesContext
     * @param component The current component
     */
    protected void renderChildren(FacesContext context, UIComponent component)
    throws IOException {
        // do nothing
    }
    
    /**
     * <p>Render the start of an anchor (hyperlink) tag.</p>
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>UIComponent</code> to be rendered
     * @param writer <code>ResponseWriter</code> to which the element
     * start should be rendered
     * @exception IOException if an input/output error occurs
     */
    protected void renderStart(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        super.renderStart(context, component, writer);
        
        // ensure that this tab's parent is either a Tab or TabSet
        UIComponent parent = component.getParent();
        
        if (!(parent instanceof Tab || parent instanceof TabSet)) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info(TabRenderer.class, "WEBUI0006",
                        new String[] { component.getId() });
            }
        }
    }
    
    protected void finishRenderAttributes(FacesContext context, UIComponent component,
            ResponseWriter writer) throws IOException {
        
        Tab tab = (Tab) component;
        
        // Set up local variables we will need
        int tabIndex = tab.getTabIndex();
        if (tabIndex >= 0) {
            writer.writeAttribute("tabIndex", Integer.toString(tabIndex), null);
        }
        super.finishRenderAttributes(context, component, writer);
    }
    
    /**
     * This function returns the style classes necessary to display the {@link Hyperlink} component as it's state indicates
     * @return the style classes needed to display the current state of the component
     */
    protected String getStyles(FacesContext context, UIComponent component) {
        Tab link = (Tab) component;
        
        StringBuffer sb = new StringBuffer(200);
        String tabStyleClass = link.getExtraStyles();
        if (tabStyleClass != null) {
            sb.append(tabStyleClass);
        }
        
        Theme theme = ThemeUtilities.getTheme(context);
        if (link.isDisabled()) {
            sb.append(" "); //NOI18N
            sb.append(theme.getStyleClass(ThemeStyles.LINK_DISABLED));
        }
        
        // <RAVE>
        // if (link.getText() != null && link.getText().length() <= 6) {
        //     sb.append(" "); // NOI18N
        //     sb.append(theme.getStyleClass(ThemeStyles.TAB_PADDING));
        // }
        Object value = link.getText();
        if (value != null) {
            String text = ConversionUtilities.convertValueToString(link, link.getText());
            if (text.length() <= 6) {
                sb.append(" "); // NOI18N
                sb.append(theme.getStyleClass(ThemeStyles.TAB_PADDING));
            }
        }
        // </RAVE>
        
        return (sb.length() > 0) ? sb.toString() : null;
    }
}
