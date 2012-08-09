/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.navigator;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;

/**
 * Source or DOM node "description".
 * 
 * Used as a Children.Keys' key in the nodes view.
 * 
 * The descriptions reflects the source elements and DOM elements and as such
 * their comprises a tree like structure.
 *
 * @author marekfukala
 */
public abstract class Description {
    
    private static Description EMPTY_SOURCE_DESCRIPTION = new EmptySourceDescription();
    private static Description EMPTY_DOM_DESCRIPTION = new EmptyDOMNodeDescription();
    
    /**
     * Gets empty Description of the given type.
     */
    public static Description empty(int type) {
        switch(type) {
            case SOURCE:
                return EMPTY_SOURCE_DESCRIPTION;
            case DOM:
                return EMPTY_DOM_DESCRIPTION;
            default:
                throw new IllegalStateException();
        }
    }
    
    /**
     * Source type of the Description. Represents source elements descriptions.
     */
    public static final int SOURCE = 1;
    /**
     * DOM type of the Description. Represents DOM elements descriptions.
     */
    public static final int DOM = 2;

    /**
     * Gets children descriptions.
     */
    public abstract Collection<? extends Description> getChildren();

    /**
     * Gets parent description.
     */
    public abstract Description getParent();
    
    /**
     * Name of the description (element).
     */
    public abstract String getName();

    /**
     * Type of the description, should be {@link #SOURCE} or {@link #DOM}
     */
    public abstract int getType();

    /**
     * Get's description path from the root of the tree to the element itself.
     * 
     * Example: html/body/table/thead/th
     * @see ElementUtils#encodeToString(org.netbeans.modules.html.editor.lib.api.elements.TreePath) 
     */
    protected abstract String getElementPath();

    /**
     * Map all description's attributes in key-value form.
     * @return 
    */
    protected abstract Map<String, String> getAttributes();

    /**
     * Gets value of specified attribute name.
     * 
     * @return may return null if there's no such attribute or it have no value.
     * (<div class), but <div class=""> should return empty string as the value.
     */
    public final String getAttributeValue(String attributeName) {
        return getAttributes().get(attributeName);
    }

    private int getAttributesHash() {
        int hash = 11;
        for (Entry<String, String> a : getAttributes().entrySet()) {
            hash = 37 * hash + a.getKey().hashCode();
            CharSequence value = a.getValue();
            hash = 37 * hash + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }


    @Override
    public String toString() {
        return new StringBuilder()
                .append(getType() == Description.SOURCE ? "source" : "dom")
                .append(':')
                .append(getName())
                .append("(ahash=")
                .append(getAttributesHash())
                .append(", idx=")
                .append(Diff.getIndexInParent(this, false))
                .append(')')
                .toString();
    }
    
    
    private final static class EmptySourceDescription extends SourceDescription {

        @Override
        public Collection<? extends Description> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return "empty_source_node_description";
        }

        @Override
        protected String getElementPath() {
            return "";
        }

        @Override
        protected Map<String, String> getAttributes() {
            return Collections.emptyMap();
        }

        public ElementType getElementType() {
            return ElementType.ERROR;
        }

        @Override 
        public int getFrom() {
            return 0;
        }

        @Override
        public int getTo() {
            return 0;
        }

        @Override
        public Description getParent() {
            return null;
        }
        
    }
    
    private final static class EmptyDOMNodeDescription extends DOMNodeDescription {

        @Override
        public Collection<? extends Description> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return "empty_dom_node_description";
        }

        @Override
        protected String getElementPath() {
            return "";
        }

        @Override
        protected Map<String, String> getAttributes() {
            return Collections.emptyMap();
        }
        
        @Override
        public Description getParent() {
            return null;
        }
    }
    
        
}
