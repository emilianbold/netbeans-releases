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
package org.netbeans.modules.html.editor.lib.html4parser;

import java.util.*;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.*;
import org.netbeans.modules.html.editor.lib.dtd.*;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Attribute;
import org.netbeans.modules.html.editor.lib.dtd.DTD.Element;

/**
 *
 * XXX Maybe the DTD.Element could implement HtmlTag directly instead of the wrapping
 *
 * @author marekfukala
 */
public class DTD2HtmlTag {

    private static final Logger LOGGER = Logger.getLogger(DTD2HtmlTag.class.getName());

    private static HashMap<DTD.Element, HtmlTag> MAP = new HashMap<DTD.Element, HtmlTag>();
    private static HashMap<Attribute, HtmlTagAttribute> ATTRS_MAP = new HashMap<Attribute, HtmlTagAttribute>();

    public static synchronized HtmlTag getTagForElement(DTD dtd, DTD.Element elementName) {
        HtmlTag impl = MAP.get(elementName);
        if (impl == null) {
            impl = new DTDElement2HtmlTagAdapter(dtd, elementName);
            MAP.put(elementName, impl);
        }
        return impl;
    }

    public static synchronized Collection<HtmlTag> convert(DTD dtd, Collection<DTD.Element> elements) {
        Collection<HtmlTag> converted = new ArrayList<HtmlTag>();
        for(DTD.Element element : elements) {
            converted.add(getTagForElement(dtd, element));
        }
        return converted;
    }

    private static synchronized HtmlTagAttribute getHtmlTagAttribute(Attribute attribute) {
        HtmlTagAttribute attr = ATTRS_MAP.get(attribute);
        if(attr == null) {
            attr = new Attribute2HtmlTagAttribute(attribute);
            ATTRS_MAP.put(attribute, attr);
        }
        return attr;
    }


    private static class DTDElement2HtmlTagAdapter implements HtmlTag {

        private DTD.Element element;
        private DTD dtd; //needed just because of the html-body child hack
        private Collection<HtmlTagAttribute> attrs;
        private Collection<HtmlTag> children;

        public DTDElement2HtmlTagAdapter(DTD dtd, Element element) {
            this.dtd = dtd;
            this.element = element;
            this.attrs = wrap(element.getAttributeList(null));
        }

         private Collection<HtmlTagAttribute> wrap(Collection<Attribute> attrNames) {
            if(attrNames == null) {
                return Collections.emptyList();
            }
            Collection<HtmlTagAttribute> attributes = new LinkedList<HtmlTagAttribute>();
            for(Attribute an : attrNames) {
                HtmlTagAttribute hta = getHtmlTagAttribute(an);
                if(hta != null) {
                    attributes.add(hta);
                } else {
                    LOGGER.info("Unknown attribute " + an + " requested.");//NOI18N
                }
            }
            return attributes;
        }


        @Override
        public String getName() {
            return element.getName();
        }

        @Override
        public Collection<HtmlTagAttribute> getAttributes() {
            return attrs;
        }

        @Override
        public boolean isEmpty() {
            return element.isEmpty();
        }

        @Override
        public boolean hasOptionalOpenTag() {
            return element.hasOptionalStart();
        }

        @Override
        public boolean hasOptionalEndTag() {
            return element.hasOptionalEnd();
        }

        @Override
        public HtmlTagAttribute getAttribute(String name) {
            Attribute attr = element.getAttribute(name);
            if(attr == null) {
                return null;
            }
            return getHtmlTagAttribute(attr);
        }

        @Override
        public HtmlTagType getTagClass() {
            return HtmlTagType.HTML;
        }

        @Override
        public synchronized Collection<HtmlTag> getChildren() {
            //logic copied from David Konecny's HtmlIndenter.
            if (children == null) {
                Set<Element> set = new HashSet<Element>();
                for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getIncludes()) {
                    if (el != null) {
                        set.add(el);
                    }
                }
                for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getExcludes()) {
                    if (el != null) {
                        set.remove(el);
                    }
                }
                for (DTD.Element el : (Set<DTD.Element>) element.getContentModel().getContent().getPossibleElements()) {
                    if (el != null) {
                        set.add(el);
                    }
                }
                if (element.getName().equalsIgnoreCase("HTML")) {
                    // XXXXXXXXXXXXXXXXX TODO:
                    set.add(dtd.getElement("BODY"));
                }
                children = convert(dtd, set);
            }
            return children;
        }

        @Override
        public HelpItem getHelp() {
            return null;
        }
        

    }

     private static class Attribute2HtmlTagAttribute implements HtmlTagAttribute {

        private Attribute attr;

        public Attribute2HtmlTagAttribute(Attribute attr) {
            this.attr = attr;
        }

        @Override
        public String getName() {
            return attr.getName();
        }

        @Override
        public boolean isRequired() {
            return attr.isRequired();
        }

        @Override
        public HtmlTagAttributeType getType() {
            switch(attr.getType()) {
                case Attribute.TYPE_BOOLEAN:
                    return HtmlTagAttributeType.BOOLEAN;
                case Attribute.TYPE_SET:
                    return HtmlTagAttributeType.SET;
                case Attribute.TYPE_BASE:
                    return HtmlTagAttributeType.GENERIC;
                default:
                    return HtmlTagAttributeType.GENERIC;
            }
        }

        @Override
        public Collection<String> getPossibleValues() {
            Collection<DTD.Value> values = attr.getValueList(null);
            if(values == null) {
                return Collections.emptyList();
            }
            Collection<String> res = new LinkedList<String>();
            for(DTD.Value v : values) {
                res.add(v.getName());
            }
            return res;
        }

        @Override
        public HelpItem getHelp() {
            return null;
        }



    }
}
