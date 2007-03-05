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
package com.sun.rave.web.ui.component;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.sun.rave.web.ui.component.Icon;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.theme.ThemeImages;

/**
 * <p>The Legend component.</p>
 */
public class Legend extends LegendBase {

    /**
     * Facet name and id
     */
    public static final String LEGEND_IMAGE_FACET = "legendImage"; //NOI18N
    public static final String LEGEND_IMAGE_FACET_ID = "_legendImage"; //NOI18N

    /**
     * Get or create the legend image.
     * Return the facet if specified, or else create one using the default
     * required field indicator, and return it.
     *
     * @return A legend image (or facet)
     */
    public UIComponent getLegendImage() {
	// First check if an image facet was specified
	UIComponent imageFacet = getFacet(LEGEND_IMAGE_FACET);
	// If not create one with the default icon.
	if (imageFacet == null) {
	    Theme theme = ThemeUtilities.
		getTheme(FacesContext.getCurrentInstance());
            Icon icon = theme.getIcon(ThemeImages.LABEL_REQUIRED_ICON);
	    icon.setId(getId() + LEGEND_IMAGE_FACET_ID); // NOI18N
            icon.setAlt(theme.getMessage("Other.requiredAltText"));
	    getFacets().put(LEGEND_IMAGE_FACET, icon);
	    imageFacet = (UIComponent) icon;
        }
	return imageFacet;
    }

}
