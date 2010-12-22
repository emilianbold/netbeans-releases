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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.editor.ext.html.parser.spi.DefaultHelpItem;
import org.netbeans.editor.ext.html.parser.spi.HelpItem;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttribute;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagAttributeType;
import org.netbeans.editor.ext.html.parser.spi.HtmlTagType;
import org.netbeans.modules.html.parser.model.Attribute;
import org.netbeans.modules.html.parser.model.ContentType;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.netbeans.modules.html.parser.model.ElementDescriptorRules;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class HtmlTagProvider {

    //global maps
    private static HashMap<String, HtmlTag> TAGS = new HashMap<String, HtmlTag>();
    private static Map<Attribute, HtmlTagAttribute> ATTRS = new EnumMap<Attribute, HtmlTagAttribute>(Attribute.class);

    public static synchronized HtmlTag getTagForElement(String name) {
        assert name != null;
        HtmlTag impl = TAGS.get(name);
        if (impl == null) {
            impl = new ElementName2HtmlTagAdapter(name);
            TAGS.put(name, impl);
        }
        return impl;
    }

     public static synchronized Collection<HtmlTag> convert(Collection<ElementDescriptor> elements) {
        Collection<HtmlTag> converted = new LinkedList<HtmlTag>();
        for(ElementDescriptor element : elements) {
            converted.add(getTagForElement(element.getName()));
        }
        return converted;
    }

    private static synchronized HtmlTagAttribute getHtmlTagAttributeInstance(Attribute attr) {
        HtmlTagAttribute htmlTagAttribute = ATTRS.get(attr);
        if(htmlTagAttribute == null) {
            htmlTagAttribute = new HtmlTagAttributeAdapter(attr);
            ATTRS.put(attr, htmlTagAttribute);
        }
        return htmlTagAttribute;
    }

    private static class ElementName2HtmlTagAdapter implements HtmlTag {

        private String elementName;
        private ElementDescriptor descriptor;
        private Map<String, HtmlTagAttribute> attrs; //attr name to HtmlTagAttribute instance map
        private HtmlTagType type;
        private Collection<HtmlTag> children;

        private ElementName2HtmlTagAdapter(String elementName) {
            this.elementName = elementName;
            this.descriptor = ElementDescriptor.forName(elementName);
            this.type = findType();
        }

        private boolean isPureHtmlTag() {
            return descriptor != null;
        }

        private HtmlTagType findType() {
            return isPureHtmlTag() ? descriptor.getTagType() : HtmlTagType.UNKNOWN;
        }

        private void initAttributes() {
            if(isPureHtmlTag()) {
                attrs = new HashMap<String, HtmlTagAttribute>();
                for (Attribute attr : descriptor.getAttributes()) {
                    attrs.put(attr.getName(), getHtmlTagAttributeInstance(attr));
                }
            } else {
                attrs = Collections.emptyMap();
            }
        }

        public String getName() {
            return elementName;
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
            return String.format("ElementName2HtmlTagAdapter{name=%s}", getName());//NOI18N
        }

        public synchronized Collection<HtmlTagAttribute> getAttributes() {
            if(attrs == null) {
                initAttributes();
            }
            return attrs.values();
        }

        public boolean isEmpty() {
            return isPureHtmlTag() ? descriptor.isEmpty() : false;
        }

        public boolean hasOptionalOpenTag() {
            return isPureHtmlTag() ? descriptor.hasOptionalOpenTag() : false;
        }

        public boolean hasOptionalEndTag() {
             return isPureHtmlTag() ? descriptor.hasOptionalEndTag() : false;
        }

        public synchronized HtmlTagAttribute getAttribute(String name) {
            if(attrs == null) {
                initAttributes();
            }
            return attrs.get(name);
        }

        public HtmlTagType getTagClass() {
            return type;
        }

        public synchronized Collection<HtmlTag> getChildren() {
            if (children == null) {
                if (isPureHtmlTag()) {
                    //add all directly specified children
                    Collection<ElementDescriptor> directChildren = descriptor.getChildrenElements();
                    children = new LinkedList<HtmlTag>(convert(directChildren));
                    //add all members of children content types
                    for(ContentType ct : descriptor.getChildrenTypes()) {
                        Collection<ElementDescriptor> contentTypeChildren = ElementDescriptorRules.getElementsByContentType(ct);
                        children.addAll(convert(contentTypeChildren));
                    }
                } else {
                    //no children info
                    children = Collections.emptyList();
                }
            }
            return children;
        }

        public HelpItem getHelp() {
            StringBuilder header = new StringBuilder();
            header.append("<h2>");
            header.append(NbBundle.getMessage(HtmlTagProvider.class, "MSG_ElementPrefix"));//NOI18N
            header.append(" '");//NOI18N
            header.append(descriptor.getName());
            header.append("'</h2>");//NOI18N

            return isPureHtmlTag() && descriptor.getHelpLink() != null
                    ? new DefaultHelpItem(
                    HtmlDocumentation.getDefault().resolveLink(descriptor.getHelpLink()),
                    HtmlDocumentation.getDefault(),
                    header.toString())
                    : null;

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

        public HelpItem getHelp() {
            StringBuilder header = new StringBuilder();
            header.append("<h2>");//NOI18N
            header.append(NbBundle.getMessage(HtmlTagProvider.class, "MSG_ElementPrefix"));//NOI18N
            header.append(" '");//NOI18N
            header.append(attr.getName());
            header.append("'</h2>");//NOI18N

            return new DefaultHelpItem(
                    HtmlDocumentation.getDefault().resolveLink(attr.getHelpLink()),
                    HtmlDocumentation.getDefault(),
                    header.toString());
        }
    }
}
