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
