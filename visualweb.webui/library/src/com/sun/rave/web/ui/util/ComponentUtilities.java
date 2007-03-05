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
