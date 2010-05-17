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
import java.util.List;

/**
 *
 */
public class ImageHyperlink extends ImageHyperlinkBase {

    /**
     * Used for identifying the facet in the facet map associated with this component
     * This is used as a suffix combined with the id of the component.
     */
    final protected String IMAGE_FACET_SUFFIX = "_image"; //NOI18N


    /**
     * Get Image Facet for this ImageHyperlink.  This facet will always reset the
     * properties on embedded facet.  This facet is not meant to be overridden
     * by others, but is only used as a storage bin for keeping the image associated
     * with the hyperlink
     * @return an {@link ImageComponent} to render
     */
    public ImageComponent getImageFacet() {
        ImageComponent image = null;
        String facetName;
        String facetId = getId();
        
        String imageURL = getImageURL();
        String icon = getIcon();
        
        if (imageURL != null || icon != null) {
            if (facetId == null) {
                facetId = IMAGE_FACET_SUFFIX;
            } else {
                facetId += IMAGE_FACET_SUFFIX;
            }

            image = (ImageComponent) getFacet(facetId);
            if (image == null) {
                if (imageURL != null) {
                    image = new ImageComponent();
                } else {
                    image = new Icon();
                }
            }

            //always reset all the properties

            image.setIcon(icon);
            image.setUrl(imageURL);

            setAttributes(facetId, image);
        }
        
        return image;
    }
    
    protected void setAttributes(String facetId, ImageComponent image) {
                //must reset the id always due to a side effect in JSF and putting
        //components in a table.
        
        image.setId(facetId);
                
        // align
        String align = getAlign();
        if (align != null) {
            image.setAlign(align);
        }
        // border
        int dim = getBorder();
        if (dim >= 0) {
            image.setBorder(dim);
        }
        // description
        String description = getAlt();
        if (description != null) {
            image.setAlt(description);
        }
        // height
        dim = getHeight();
        if (dim >= 0) {
            image.setHeight(dim);
        }
        // hspace
        dim = getHspace();
        if (dim >= 0) {
            image.setHspace(dim);
        }
        // vspace
        dim = getVspace();
        if (dim >= 0) {
            image.setVspace(dim);
        }
        // width
        dim = getWidth();
        if (dim >= 0) {
            image.setWidth(dim);
        }
        // disabled (based on parent)
        Boolean disabled =
                (Boolean) getAttributes().get("disabled"); //NOI18N
        if (disabled != null) {
            image.getAttributes().put("disabled", String.valueOf(disabled)); //NOI18N
        }
        // <RAVE>
        // getFacets().put(facetId, image);
        if (!Beans.isDesignTime())
            getFacets().put(facetId, image);
        // </RAVE>
    }

}
