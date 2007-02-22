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

package org.netbeans.modules.xml.xam.ui.highlight;

import org.netbeans.modules.xml.xam.Component;

/**
 * Represents a highlighted component.
 *
 * @author Nathan Fiedler
 */
public abstract class Highlight {
    /** The type for a search result highlight. */
    public static final String SEARCH_RESULT = "searchResult";
    /** The type for the parent of a search result highlight. */
    public static final String SEARCH_RESULT_PARENT = "searchResultParent";
    /** The type for a find usages result highlight. */
    public static final String FIND_USAGES_RESULT_PARENT = "find-usages-result-parent";
    /** The type for the parent of a find usages result highlight. */
    public static final String FIND_USAGES_RESULT = "find-usages-result";
    /** The highlighted component. */
    private Component component;
    /** Type of highlight. */
    private String type;

    /**
     * Creates a new instance of Highlight.
     *
     * @param  component  highlighted component.
     * @param  type       highlight type.
     */
    public Highlight(Component component, String type) {
        assert component != null;
        assert type != null;
        this.component = component;
        this.type = type;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Highlight) {
            Highlight oh = (Highlight) obj;
            // We assert that these fields are non-null.
            return type.equals(oh.type) && component.equals(oh.component);
        }
        return false;
    }

    /**
     * Return the component that is highlighted.
     *
     * @return  highlighted component.
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Return the type of this highlight.
     *
     * @return  type of highlight.
     */
    public String getType() {
        return type;
    }

    public int hashCode() {
        return type.hashCode() + component.hashCode();
    }
}
