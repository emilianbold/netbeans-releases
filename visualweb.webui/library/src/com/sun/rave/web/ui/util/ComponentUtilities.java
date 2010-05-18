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
package com.sun.rave.web.ui.util;

import javax.faces.component.UIComponent;

/**
 * Methods for general component manipulation.
 */
public class ComponentUtilities {

    private final static String USCORE = "_"; //NOI18N

    /** Creates a new instance of ComponentUtilities. */
    public ComponentUtilities() {
    }

    /**
     * Store an internally created component utilizing the
     * internal facet naming convention by mapping the facet
     * to the name returned by <code>createPrivateFacetName()</code>.
     * Add the component to the parent's facets map.
     *
     * @param parent the component that created the facet
     * @param facetName the public facet name
     * @param facet the private facet component instance
     */
    public static void putPrivateFacet(UIComponent parent,
	    String facetName, UIComponent facet) {

	if (parent == null || facet == null || facetName == null) {
	    return;
	}
	parent.getFacets().put(createPrivateFacetName(facetName), facet);
    }

    /**
     * Remove an internally created component utilizing the
     * internal facet naming convention by mapping the facet
     * to the name returned by <code>createPrivateFacetName()</code>.
     * Remove the component from the parent's facets map.
     *
     * @param parent the component that created the facet
     * @param facetName the public facet name
     */
    public static void removePrivateFacet(UIComponent parent,
	    String facetName) {

	if (parent == null || facetName == null) {
	    return;
	}
	parent.getFacets().remove(createPrivateFacetName(facetName));
    }
    /**
     * Return a private facet from the the parent component's facet map.
     * Look for a private facet name by calling
     * <code>createPrivateFacetName()</code> on the facetName parameter.
     * <p>
     * If the matchId parameter is true, verify that the facet that is found
     * has an id that matches the value of
     * <code>getPrivateFacetId(parent.getId(), facetName)</code>.
     * If the id's do not match return null and remove the existing facet.</br>
     * If matchId is false, return the facet if found or null.
     *
     * @param parent the component that contains the facet
     * @param facetName the public facet name
     * @parem matchId verify a the id of the facet
     * @return a UIComponent if the facet is found else null.
     */
    public static UIComponent getPrivateFacet(UIComponent parent,
	    String facetName, boolean matchId) {

	if (parent == null || facetName == null) {
	    return null;
	}

	String pfacetName = createPrivateFacetName(facetName);
	UIComponent facet = (UIComponent)parent.getFacets().get(pfacetName);
	if (facet == null) {
	    return null;
	}

	if (matchId == false) {
	    return facet;
	}

	// Will never be null as long as facetName is not null.
	//
	String id = createPrivateFacetId(parent, facetName);
	if (!id.equals(facet.getId())) {
	    parent.getFacets().remove(pfacetName);
	    return null;
	}
	return facet;
    }

    /**
     * Prefix the facetName parameter with an "_".
     *
     * @param facetName the public facet name
     * @return a private facet name
     */
    public static String createPrivateFacetName(String facetName) {
	return USCORE.concat(facetName);
    }

    /**
     * Return an id using the convention</br>
     * <code>parent.getId() + "_" + facetName</code>
     * If <code>parent.getId()</code> is null, <code>"_" + facetName </code>
     * is returned.
     *
     * @param parent the component that contains the facet
     * @param facetName the public facet name
     * @return an id for a private facet.
     */
    public static String createPrivateFacetId(UIComponent parent,
	    String facetName) {

	String pfacetName = createPrivateFacetName(facetName);
	String id = parent.getId();
	if (id != null) {
	    pfacetName = id.concat(pfacetName);
	}
	return pfacetName;
    }
}
