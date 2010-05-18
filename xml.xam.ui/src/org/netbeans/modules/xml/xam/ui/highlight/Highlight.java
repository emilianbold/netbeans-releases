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

package org.netbeans.modules.xml.xam.ui.highlight;

import org.netbeans.modules.xml.xam.Component;

/**
 * Represents a highlighted component.
 *
 * @author Nathan Fiedler
 */
public class Highlight {
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
