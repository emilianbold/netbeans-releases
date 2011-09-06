/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.properties.parser;

/**
 * the object is in fact immutable since the setMinimum/MaximumOccurrences is called only just after its creation.
 */
/**
 *
 * @author marekfukala
 */
public abstract class GrammarElement {
    private GroupGrammarElement parent;
    private String path;

    public GrammarElement(GroupGrammarElement parent) {
        this.parent = parent;
    }
    
    private int minimum_occurances = 1;
    private int maximum_occurances = 1;

    void setMinimumOccurances(int i) {
        this.minimum_occurances = i;
    }

    void setMaximumOccurances(int i) {
        maximum_occurances = i;
    }

    public int getMaximumOccurances() {
        return maximum_occurances;
    }

    public int getMinimumOccurances() {
        return minimum_occurances;
    }

    public GroupGrammarElement parent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GrammarElement)) {
            return false;
        }
        GrammarElement e = (GrammarElement) o;
        return path().equalsIgnoreCase(e.path());
    }

    @Override
    public int hashCode() {
        return path().hashCode();
    }

    /** returns a name of the property from which this element comes from */
    public String origin() {
        GroupGrammarElement p = parent;
        while (p != null) {
            if (p.referenceName != null) {
                return p.referenceName;
            }
            p = p.parent();
        }
        return null;
    }

    public String getResolvedOrigin() {
        String origin = origin();
        if (origin == null) {
            return null;
        }
        if (GrammarParser.isArtificialElementName(origin)) {
            //NOI18N
            //artificial origin, get real origin from the first ancestor element with an origin
            GrammarElement parentElement = this;
            while ((parentElement = parentElement.parent()) != null) {
                if (parentElement.origin() != null && !GrammarParser.isArtificialElementName(parentElement.origin())) {
                    origin = parentElement.origin();
                    break;
                }
            }
        }
        return origin;
    }

    public synchronized String path() {
        if (path == null) {
            StringBuilder sb = new StringBuilder();
            if (parent() != null) {
                sb.append(parent().path());
                sb.append('/');
            }
            sb.append(toString());
            path = sb.toString();
        }
        return path;
    }

    @Override
    public String toString() {
        if (getMinimumOccurances() != 1 || getMaximumOccurances() != 1) {
            return "{" + getMinimumOccurances() + "," + (getMaximumOccurances() == Integer.MAX_VALUE ? "inf" : getMaximumOccurances()) + "}"; //NOI18N
        } else {
            return ""; //NOI18N
        }
    }

    public String toString2(int level) {
        return indentString(level) + toString();
    }

    protected String indentString(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append('\t');
        }
        return sb.toString();
    }
    
}
