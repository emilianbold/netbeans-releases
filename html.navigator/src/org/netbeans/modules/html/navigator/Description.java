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

/**
 *
 * @author marekfukala
 */
public abstract class Description {
    
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
    
    public static Description EMPTY_SOURCE_DESCRIPTION = new EmptySourceDescription();
    
    public static Description EMPTY_DOM_DESCRIPTION = new EmptyDOMNodeDescription();
    
    public static final int SOURCE = 1;
    public static final int DOM = 2;

    public abstract Collection<? extends Description> getChildren();
    
    public abstract String getName();
    
    public abstract int getType();

    protected abstract String getElementPath();

    protected abstract Map<String, String> getAttributes();
    
    public final String getAttributeValue(String attributeName) {
        return getAttributes().get(attributeName);
    }

    protected final int getAttributesHash() {
        int hash = 11;
        for (Entry<String, String> a : getAttributes().entrySet()) {
            hash = 37 * hash + a.getKey().hashCode();
            CharSequence value = a.getValue();
            hash = 37 * hash + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }

    final int hashCode2() {
        int hash = 7;
        hash = 41 * hash + getElementPath().hashCode();
        hash = 41 * hash + getAttributesHash();
        return hash;
    }

    final boolean equals2(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Description)) {
            return false;
        }
        final Description other = (Description) obj;
        if (!getElementPath().equals(other.getElementPath())) {
            return false;
        }
        if ((getAttributesHash() != other.getAttributesHash())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append(getElementPath())
                .append('/')
                .append(getAttributesHash())
                .toString();
    }
    
    
    private final static class EmptySourceDescription extends SourceDescription {

        @Override
        public Collection<? extends Description> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return null;
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
        
    }
    
    private final static class EmptyDOMNodeDescription extends DOMNodeDescription {

        @Override
        public Collection<? extends Description> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        protected String getElementPath() {
            return "";
        }

        @Override
        protected Map<String, String> getAttributes() {
            return Collections.emptyMap();
        }
        
    }
    
        
}
