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

import java.util.HashSet;
import java.util.Set;

/**
 * A collection of Highlight objects, which are shown or hidden as a group.
 *
 * @author Nathan Fiedler
 */
public class HighlightGroup {
    /** Type of highlight group for search results (includes parents). */
    public static final String SEARCH = "search";
    /** Set of Highlight instances. */
    private Set<Highlight> highlights;
    /** The type of this group. */
    private String type;
    /** True if this group is shown, false if hidden. */
    private boolean showing;

    /**
     * Creates a new instance of HighlightGroup.
     *
     * @param  type  highlight group type.
     */
    public HighlightGroup(String type) {
        assert type != null;
        this.type = type;
        highlights = new HashSet<Highlight>();
    }

    /**
     * Add the given Highlight to this group.
     *
     * @param  hl  Highlight to be added.
     */
    public void addHighlight(Highlight hl) {
        highlights.add(hl);
    }

    /**
     * Return the group type.
     *
     * @return  group type.
     */
    public String getType() {
        return type;
    }

    /**
     * Return true if group is showing, false otherwise.
     *
     * @return  true if group is showing, false otherwise.
     */
    public boolean isShowing() {
        return showing;
    }

    /**
     * Return the set of highlights in this group.
     *
     * @return  highlight set.
     */
    public Set<Highlight> highlights() {
        return highlights;
    }

    /**
     * Set the showing property for tracking this group's visibility.
     *
     * @param  showing  true if showing, false if hidden.
     */
    public void setShowing(boolean showing) {
        this.showing = showing;
    }
}
