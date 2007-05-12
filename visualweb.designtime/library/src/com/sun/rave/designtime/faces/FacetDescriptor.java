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
