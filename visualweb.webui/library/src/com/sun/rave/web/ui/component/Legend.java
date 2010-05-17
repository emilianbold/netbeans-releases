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
