/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ElementName;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttribute;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttributeType;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;
import org.netbeans.modules.html.parser.model.Attribute;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.netbeans.modules.html.parser.model.ElementDescriptorRules;

/**
 *
 * @author marekfukala
 */
public class HtmlTagProvider {

    private static final Logger LOGGER = Logger.getLogger(HtmlTagProvider.class.getName());
    private static HashMap<ElementName, HtmlTag> MAP = new HashMap<ElementName, HtmlTag>();

    public static synchronized HtmlTag getTagForElement(ElementName elementName) {
        HtmlTag impl = MAP.get(elementName);
        if (impl == null) {
            impl = new ElementName2HtmlTagAdapter(elementName);
            MAP.put(elementName, impl);
        }
        return impl;
    }

    private static class ElementName2HtmlTagAdapter implements HtmlTag {

        private ElementName element;
        private ElementDescriptor descriptor;
        private Map<String, HtmlTagAttribute> attrs; //attr name to HtmlTagAttribute instance map
        private HtmlTagType type;

        private ElementName2HtmlTagAdapter(ElementName element) {
            this.element = element;
            this.descriptor = ElementDescriptor.forElementName(element);
            this.attrs = isPureHtmlTag()
                    ? wrap(descriptor.getAttributes())
                    : Collections.<String, HtmlTagAttribute>emptyMap();
            this.type = findType();
        }

        private boolean isPureHtmlTag() {
            return descriptor != null;
        }

        private HtmlTagType findType() {
            if (isPureHtmlTag()) {
                //descriptor is available only for html tags
                return HtmlTagType.HTML;
            } else {
                if (ElementDescriptorRules.MATHML_TAG_NAMES.contains(getName())) {
                    return HtmlTagType.MATHML;
                } else if (ElementDescriptorRules.SVG_TAG_NAMES.contains(getName())) {
                    return HtmlTagType.SVG;
                } else {
                    return HtmlTagType.UNKNOWN;
                }
            }
        }

        private Map<String, HtmlTagAttribute> wrap(Collection<Attribute> attrNames) {
            if (attrNames == null) {
                return Collections.emptyMap();
            }
            Map<String, HtmlTagAttribute> attributes = new HashMap<String, HtmlTagAttribute>();
            for (Attribute an : attrNames) {
                HtmlTagAttribute hta = new HtmlTagAttributeAdapter(an);
                if (hta != null) {
                    attributes.put(an.getName(), hta);
                } else {
                    LOGGER.info(String.format("Unknown attribute %1$ requested.", an));//NOI18N
                }
            }
            return attributes;
        }

        public String getName() {
            return element.name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof HtmlTag)) {
                return false;
            }
            final HtmlTag other = (HtmlTag) obj;
            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return String.format("ElementName2HtmlTagAdapter{name=%1$}", getName());
        }

        public Collection<HtmlTagAttribute> getAttributes() {
            return attrs.values();
        }

        //TODO TBD - do we need this?
        public boolean isEmpty() {
            return false;
        }

        public boolean hasOptionalOpenTag() {
            if(isPureHtmlTag()) {
                return ElementDescriptorRules.OPTIONAL_OPEN_TAGS.contains(descriptor);
            } else {
                return false;
            }
        }

        public boolean hasOptionalEndTag() {
            if(isPureHtmlTag()) {
                return ElementDescriptorRules.OPTIONAL_END_TAGS.contains(descriptor);
            } else {
                return false;
            }
        }

        public HtmlTagAttribute getAttribute(String name) {
            return attrs.get(name);
        }

        public HtmlTagType getTagClass() {
            return type;
        }
    }

    private static class HtmlTagAttributeAdapter implements HtmlTagAttribute {

        private Attribute attr;

        public HtmlTagAttributeAdapter(Attribute name) {
            this.attr = name;
        }

        public String getName() {
            return attr.getName();
        }

        public boolean isRequired() {
            return false;
        }

        public HtmlTagAttributeType getType() {
            return HtmlTagAttributeType.GENERIC;
        }

        public Collection<String> getPossibleValues() {
            return Collections.emptyList();
        }
    }
}
