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
