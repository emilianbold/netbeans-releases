/* {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved. Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package com.sun.rave.designtime.faces;

import java.beans.FeatureDescriptor;

/**
 * The FacetDescriptor describes a single Java Server Faces facet - like the "header" or "footer" on
 * an HtmlDataTable component.  An array of FacetDescriptor(s) are "stuffed" into the BeanDescriptor
 * using the name-value pair storage: FeatureDescriptor.setValue(String key, Object value).  The key
 * is defined by Constants.BeanDescriptor.FACET_DESCRIPTORS, or literally "facetDescriptors".  If an
 * array of FacetDescriptors is found in a bean's BeanDescriptor, the IDE will show them as options
 * where appropriate.
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see java.beans.FeatureDescriptor#setValue(String, Object)
 * @see com.sun.rave.designtime.Constants.BeanDescriptor#FACET_DESCRIPTORS
 */
public class FacetDescriptor extends FeatureDescriptor {

    /**
     * Constructs a default FacetDescriptor with no settings
     */
    public FacetDescriptor() {}

    /**
     * Constructs a FacetDescriptor with the specified name
     *
     * @param name The desired facet name
     */
    public FacetDescriptor(String name) {
        setName(name);
    }

    public boolean equals(Object o) {
        if (o instanceof FacetDescriptor) {
            FacetDescriptor ad = (FacetDescriptor)o;
            return ad == this || ad.getName() != null && ad.getName().equals(getName());
        }
        return false;
    }
}
