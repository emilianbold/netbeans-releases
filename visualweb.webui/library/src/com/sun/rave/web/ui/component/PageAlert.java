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

import java.beans.Beans;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>The Page Alert component.</p>
 */
public class PageAlert extends PageAlertBase {
    /**
     * The facets...
     */
    public static final String PAGEALERT_INPUT_FACET = "pageAlertInput"; //NOI18N
    public static final String PAGEALERT_TITLE_FACET = "pageAlertTitle"; //NOI18N
    public static final String PAGEALERT_BUTTONS_FACET = "pageAlertButtons"; //NOI18N
    public static final String PAGEALERT_SEPARATOR_FACET = "pageAlertSeparator"; //NOI18N
    public static final String PAGEALERT_IMAGE_FACET = "pageAlertImage"; //NOI18N


    /**
     * Get the page alert input facet.
     * 
     * @return A Back button (or a facet with buttons).
     */
    public UIComponent getPageAlertInput() {
	return getFacet(PAGEALERT_INPUT_FACET);
    }
    
    /** 
     * Get the page alert title facet.
     * 
     * @return A Back button (or a facet with buttons).
     */
    public UIComponent getPageAlertTitle() {
        UIComponent titleFacet = getFacet(PAGEALERT_TITLE_FACET);
        String id = getId() + "_pageAlertTitle";
        if (titleFacet == null) {
            // create a facet for the title and add it:
            titleFacet = new StaticText();
	    titleFacet.setId(id);
            // <RAVE>
            // getFacets().put(PAGEALERT_TITLE_FACET, titleFacet);
            if (!Beans.isDesignTime())
	        getFacets().put(PAGEALERT_TITLE_FACET, titleFacet);
            // </RAVE>
        }
        
        if (titleFacet.getId() == id) {
            //we created this facet so make sure the text is updated in case of
            //value bindings
           ((StaticText) titleFacet).setText(getSafeTitle());
        }
        return titleFacet;
    }

    /** 
     * Get buttons for the Page Alert.
     * Return a set of buttons if they were sepecifed in tha facet
     * 
     * @return A Back button (or a facet with buttons).
     */
    public UIComponent getPageAlertButtons() {
	// First check if a buttons facet was defined 
	UIComponent buttonFacet = getFacet(PAGEALERT_BUTTONS_FACET);	
	return buttonFacet;
    }

    /** 
     * Get or create the separator for the Page Alert.
     * 
     * 
     * @return a PageSeparator component
     */
    public UIComponent getPageAlertSeparator() {
	// First check if a buttons facet was defined 
	UIComponent separatorFacet = getFacet(PAGEALERT_SEPARATOR_FACET);
	if (separatorFacet == null) {
	    separatorFacet = new PageSeparator();
	    separatorFacet.setId(getId() + "_pageAlertSeparator");
            // <RAVE>
            // getFacets().put(PAGEALERT_SEPARATOR_FACET, separatorFacet);
            if (!Beans.isDesignTime())
	        getFacets().put(PAGEALERT_SEPARATOR_FACET, separatorFacet);
            // </RAVE>
	    
	}
	return separatorFacet;
    }
    
    /** 
     * Get or create the separator for the Page Alert.
     * 
     * 
     * @return a PageSeparator component
     */
    public UIComponent getPageAlertImage() {
	// First check if a buttons facet was defined 
	UIComponent imageFacet = getFacet(PAGEALERT_IMAGE_FACET);
	if (imageFacet == null) {
            
	    Icon icon = (Icon) getTheme().getIcon(getIconIdentifier());
            icon.setAlt(getAlt());
	    icon.setId(getId() + "_pageAlertImage"); // NOI18N
            imageFacet = icon;
            // <RAVE>
            // getFacets().put(PAGEALERT_IMAGE_FACET, imageFacet);
            if (!Beans.isDesignTime())
	        getFacets().put(PAGEALERT_IMAGE_FACET, imageFacet);
            // </RAVE>
	    
	}
	return imageFacet;
    }
    
    public String getSafeTitle() {
        String title = getTitle();
        if (title == null) {
            title = getAlt();
            if (title == null) {
                title = "";
            }
        }
        return title;
    }
    
    private String getIconIdentifier() {
        String type = getType();
        if (type != null) {
            type.toLowerCase();

            if (type.startsWith("warn")) { // NOI18N
                return ThemeImages.ALERT_WARNING_LARGE;
            } else if (type.startsWith("ques")) { // NOI18N
                return ThemeImages.ALERT_HELP_LARGE;
            } else if (type.startsWith("info")) { // NOI18N
                return ThemeImages.ALERT_INFO_LARGE;
            }
        }
        return ThemeImages.ALERT_ERROR_LARGE;
    }
    /*
     * Utility to get theme.
     */
    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

}
