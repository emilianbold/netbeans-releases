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

import com.sun.rave.web.ui.el.ConstantMethodBinding;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.beans.Beans;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

/**
 * <p>The inline Alert component.</p>
 */
public class Alert extends AlertBase {
    /**
     * String for the appended id of the facet name for image
     */
    public static final String ALERT_IMAGE_FACET = "alertImage"; //NOI18N
    /**
     * String for the appended id of the facet name for the link
     */
    public static final String ALERT_LINK_FACET = "alertLink"; //NOI18N
    
    /**
     * Gets the alert icon facet for this component.
     * If none exists it will create one with the appropriate type.
     * The default is a warning icon.
     * @return always returns an icon component, will return a facet
     * if it exists or a newly created one if it doesn't
     */
    public UIComponent getAlertIcon() {
        // First check if a buttons facet was defined
        UIComponent imageFacet =  getFacet(ALERT_IMAGE_FACET);
        if (imageFacet == null) {
            Theme theme =
                    ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
            imageFacet = theme.getIcon(getIconIdentifier());
            imageFacet.setId(getId() + "_" + ALERT_IMAGE_FACET); // NOI18N
        }
        if (imageFacet instanceof Icon) {
            Icon icon = (Icon) imageFacet;
            icon.setAlt(getAlt());
            icon.setIcon(getIconIdentifier());
            // <RAVE>
            // getFacets().put(ALERT_IMAGE_FACET, imageFacet);
            if (!Beans.isDesignTime())
                getFacets().put(ALERT_IMAGE_FACET, imageFacet);
            // </RAVE>
        }
        
        return imageFacet;
    }
    
    /**
     * Gets the alert hyperlink facet for this component.
     * If none exists it will create
     * one with the appropriate type.  The default is a warning icon.
     * If the facet id is the internal facet id, it will update the links
     * and text
     * If the facet id is any other id then this component will just
     * return the facet
     * @return always returns a Hyperlink component, will return a facet
     * if it exists or a newly created one if it doesn't
     */
    public UIComponent getAlertLink() {
        UIComponent linkFacet =  getFacet(ALERT_LINK_FACET);
        String newId = getId() + "_" + ALERT_LINK_FACET;
        if ((linkFacet == null || linkFacet.getId().equals(newId)) &&
                getLinkText() != null) {
            IconHyperlink h = new IconHyperlink();
            h.setId(newId); // NOI18N
            h.setTarget(getLinkTarget());
            h.setText(getLinkText());
            h.setToolTip(getLinkToolTip());
            h.setUrl(getLinkURL());
            h.setIcon(ThemeImages.HREF_LINK);
            
            MethodBinding action = getLinkAction();
            if (action != null) {
                h.setAction(action);
            }
            linkFacet = h;
            // <RAVE>
            // getFacets().put(ALERT_LINK_FACET, linkFacet);
            if (!Beans.isDesignTime())
                getFacets().put(ALERT_LINK_FACET, linkFacet);
            // </RAVE>
        }
        return linkFacet;
    }
    
    private String getIconIdentifier() {
        
        String type = getType();
        if (type != null) {
            String lower = type.toLowerCase();
            
            if (lower.startsWith("warn")) { // NOI18N
                return ThemeImages.ALERT_WARNING_LARGE;
            } else if (lower.startsWith("ques")) { // NOI18N
                return ThemeImages.ALERT_HELP_LARGE;
            } else if (lower.startsWith("info")) { // NOI18N
                return ThemeImages.ALERT_INFO_LARGE;
            } else if (lower.startsWith("succ")) { // NOI18N
                return ThemeImages.ALERT_SUCCESS_LARGE;
            }
        }
        return ThemeImages.ALERT_ERROR_LARGE;
    }

    public void setLinkURL(String linkURL) {
        super.setLinkURL(linkURL);
        UIComponent link = getFacet(ALERT_LINK_FACET);
        if (link != null && link instanceof IconHyperlink)
            ((IconHyperlink) link).setUrl(linkURL);
    }

    public void setLinkToolTip(String linkToolTip) {
        super.setLinkToolTip(linkToolTip);
        UIComponent link = getFacet(ALERT_LINK_FACET);
        if (link != null && link instanceof IconHyperlink)
            ((IconHyperlink) link).setToolTip(linkToolTip);
    }

    public void setLinkText(String linkText) {
        super.setLinkText(linkText);
        UIComponent link = getFacet(ALERT_LINK_FACET);
        if (link != null && link instanceof IconHyperlink)
            ((IconHyperlink) link).setText(linkText);
    }

    public void setLinkTarget(String linkTarget) {
        super.setLinkTarget(linkTarget);
        UIComponent link = getFacet(ALERT_LINK_FACET);
        if (link != null && link instanceof IconHyperlink)
            ((IconHyperlink) link).setTarget(linkTarget);
    }

    public void setLinkAction(MethodBinding linkAction) {
        super.setLinkAction(linkAction);
        UIComponent link = getFacet(ALERT_LINK_FACET);
        if (link != null && link instanceof IconHyperlink)
            ((IconHyperlink) link).setAction(linkAction);
    }
    
    
    
}
