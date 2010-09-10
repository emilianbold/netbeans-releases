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
package org.netbeans.modules.html.parser.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import nu.validator.htmlparser.sax.HtmlParser;
import org.netbeans.junit.NbTestCase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Not a real unit test, just for simple generation
 * of html element's descriptions.
 *
 * @author marekfukala
 */
public class GenerateElementsIndex extends NbTestCase {

    private static final String WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL = Constants.HTML5_MULTIPAGE_SPEC_BASE_URL + "section-index.html#elements-1";
    private boolean parse = true;
    private boolean intbody, intr, inth_or_td, ina;
    private int column;
    private String href;
    private final Collection<Element> elements = new LinkedList<Element>();
    private Stack<Element> currents = new Stack<Element>();
    private String LINK_URL_BASE; //if the spec contains full urls its empty

    public GenerateElementsIndex(String name) {
        super(name);
    }

    //1. try to freshest spec from whatwg.org
    //2. if unavailable, use local copy
    private InputStream getSpec() throws URISyntaxException, MalformedURLException, IOException {
        URL u = new URL(WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL);
        try {
            URLConnection con = u.openConnection();
            LINK_URL_BASE = Constants.HTML5_MULTIPAGE_SPEC_BASE_URL; //only relative paths, use the relative link url
            return con.getInputStream();
        } catch (IOException ex) {
            //cannot connect, use local copy
            u = ClassLoader.getSystemResource("org/netbeans/modules/html/parser/model/section-index_2010_09_08.html");
            assertNotNull(u);
            try {
                URLConnection con = u.openConnection();
                LINK_URL_BASE = ""; //downloaded file has resolved links to full urls
                System.err.println("Cannot download the specification from " + WHATWG_SPEC_HTML5_ELEMENTS_INDEX_URL + " using local copy.\nBEWARE, it is very likely outdated!!!");
                return con.getInputStream();
            } catch (IOException ex1) {
                throw ex1;
            }

        }
    }

    public void testGenerateElementsIndex() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        InputStream spec = getSpec();
        assertNotNull(spec);

        HtmlParser parser = new HtmlParser();
        parser.setContentHandler(new ContentHandlerAdapter() {

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                element(localName, false, null);
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                element(localName, true, atts);
            }

            private void element(String localName, boolean open, Attributes atts) throws SAXException {
                if (!parse) {
                    return; //parsing already stopped
                }
                if (localName.equals("tbody")) {
                    intbody = open;
                    if (!intbody) {
                        parse = false;
                    }
                } else if (localName.equals("tr")) {
                    if (intbody) {
                        intr = open;
                        if (open) {
                            column = -1;
                        } else {
                            //h1-h6 support (more elements in the element name cell)
                            Element filledElement = currents.pop();
                            filledElement.done();
                            for (Element e : currents) {
                                Element copy = filledElement.shallowCopy();
                                copy.name = e.name;
                                elements.add(copy);
                            }
                            elements.add(filledElement);

                            currents.clear();
                        }
                    }
                } else if (localName.equals("th") || localName.equals("td")) {
                    if (intr) {
                        inth_or_td = open;
                        if (open) {
                            column++;
                        }
                    }
                } else if (localName.equals("a")) {
                    if (inth_or_td) {
                        ina = open;
                        if (open) {
                            //get link target
                            href = atts.getValue("href");
                        } else {
                            href = null;
                        }
                    }
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                String text = new String(ch, start, length);
                switch (column) {
                    case 0:
                        //name
                        if (ina) {
                            currents.push(new Element());
                            currents.peek().name = new LLink(text, href);
                        }
                        break;
                    case 1:
                        //description
                        currents.peek().descriptionBuilder.append(text);
                        break;
                    case 2:
                        //categories
                        if (ina) {
                            currents.peek().categories.add(new LLink(text, href));
                        }
                        break;
                    case 3:
                        //parents
                        if (ina) {
                            currents.peek().parents.add(new LLink(text, href));
                        }
                        break;
                    case 4:
                        //parents
                        if (ina) {
                            currents.peek().children.add(new LLink(text, href));
                        }
                        break;
                    case 5:
                        //parents
                        if (ina) {
                            currents.peek().attributes.add(new LLink(text, href));
                        }
                        break;
                    case 6:
                        //interface
                        if (ina) {
                            currents.peek().interfacee = new LLink(text, href);
                        }
                        break;
                    default:
                        assert false;

                }
            }
        });

        InputSource input = new InputSource(spec);
        parser.parse(input);

        System.out.println("Found " + elements.size() + " elements:");
//        for(Element e : elements) {
//            System.out.println(e);
//        }


        //generate the ElementDescriptor enum class
        Writer out = new StringWriter();
        Map<String, Element> elementsMap = new HashMap<String, Element>();
        for (Element e : elements) {
            elementsMap.put(e.name.getName(), e);
        }

        for (Element e : elements) {
            out.write(e.name.getName().toUpperCase());
            out.write("(\n");
            out.write("\tnew Link(\"");
            out.write(e.name.getName());
            out.write("\", \"");
            out.write(e.name.getUrl().toExternalForm());
            out.write("\"),\n\t \"");
            out.write(e.description);
            out.write("\", ");

            out.write("\n\t");

            //categories - content type of form associated element category
            writeContentTypes(e.categories, out);
            out.write("\n\t");
            writeFormAssociatedElementsCategoryTypes(e.categories, out);
            out.write("\n\t");

            //parents - content type or element
            writeContentTypes(e.parents, out);
            out.write("\n\t");
            writeElements(e.parents, elementsMap, out);
            out.write("\n\t");
            
            //children - content type or element
            writeContentTypes(e.children, out);
            out.write("\n\t");
            writeElements(e.children, elementsMap, out);
            out.write("\n\t");

            //attributes
            //XXX implement!!!
            out.write("Collections.<Attribute>emptyList(),");
            out.write("\n\t");

            //dom interface
            out.write("new Link(\"");
            out.write(e.interfacee.getName());
            out.write("\", \"");
            out.write(e.interfacee.getUrl().toExternalForm());
            out.write("\")");

            out.write("\n), \n\n");
        }


        System.out.println(out);
    }

    private void writeElements(Collection<Link> links, Map<String, Element> elementsMap, Writer out) throws IOException {
        Collection<Element> els = new ArrayList<Element>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();

            Element el = elementsMap.get(cat.getName());
            if (el != null) {
                els.add(el);
            }

        }

        writeElementsCollection(els, out);
    }

    private void writeElementsCollection(Collection<Element> elements, Writer out) throws IOException {
        out.write("new String[]{");
        for (Iterator<Element> i = elements.iterator(); i.hasNext();) {
            Element e = i.next();
            out.write("\"");
            out.write(e.name.getName());
            out.write("\"");
            if (i.hasNext()) {
                out.write(", ");
            }
        }
        out.write("}, ");
    }

    private void writeContentTypes(Collection<Link> links, Writer out) throws IOException {
        Collection<ContentType> ctypes = new ArrayList<ContentType>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();
            String nameInUpperCase = cat.getName().toUpperCase();

            //ContentType
            try {
                ContentType contentType = ContentType.valueOf(nameInUpperCase);
                ctypes.add(contentType);
            } catch (IllegalArgumentException ex) {
                //the element doesn't represent content type
            }

        }

        writeEnumCollection(ctypes, "ContentType", out);
    }

    private void writeFormAssociatedElementsCategoryTypes(Collection<Link> links, Writer out) throws IOException {
        Collection<FormAssociatedElementsCategory> fasecs = new ArrayList<FormAssociatedElementsCategory>();

        for (Iterator<Link> i = links.iterator(); i.hasNext();) {
            Link cat = i.next();
            String nameInUpperCase = cat.getName().toUpperCase();

            try {
                FormAssociatedElementsCategory fasec = FormAssociatedElementsCategory.valueOf(nameInUpperCase);
                fasecs.add(fasec);
            } catch (IllegalArgumentException ex) {
                //the element doesn't represent FormAssociatedElementsCategory
            }

        }

        writeEnumCollection(fasecs, "FormAssociatedElementsCategory", out);
    }

    private void writeEnumCollection(Collection<? extends Enum> enumCollection, String enumName, Writer out) throws IOException {
        if (enumCollection.isEmpty()) {
            out.write("EnumSet.noneOf(");
            out.write(enumName);
            out.write(".class), ");
        } else {
            //EnumSet.of(...) up to 5 arguments only!, if more modify to use the vararg version
            assert enumCollection.size() < 6;
            out.write("EnumSet.of(");
            for (Iterator<? extends Enum> i = enumCollection.iterator(); i.hasNext();) {
                Enum ct = i.next();
                out.write(enumName);
                out.write(".");
                out.write(ct.name());
                if (i.hasNext()) {
                    out.write(", ");
                }
            }
            out.write("), ");
        }
    }

    private class LLink extends Link {

        public LLink(String name, String url) {
            super(name, LINK_URL_BASE + url);
        }
    }

    private static class Element {

        public Link name;
        public StringBuilder descriptionBuilder = new StringBuilder();
        public String description;
        public Collection<Link> categories = new PrinteableArrayList<Link>();
        public Collection<Link> parents = new PrinteableArrayList<Link>();
        public Collection<Link> children = new PrinteableArrayList<Link>();
        public Collection<Link> attributes = new PrinteableArrayList<Link>();
        public Link interfacee;

        @Override
        public String toString() {
            return "Element{" + "name=" + name + ", description=" + description
                    + ", categories=" + categories + ", parents=" + parents
                    + ", children=" + children + ", attributes=" + attributes
                    + ", interfacee=" + interfacee + '}';
        }

        public Element shallowCopy() {
            Element copy = new Element();
            copy.name = name;
            copy.description = description;
            copy.categories = categories;
            copy.parents = parents;
            copy.children = children;
            copy.attributes = attributes;
            copy.interfacee = interfacee;
            return copy;
        }

        private void done() {
            //called when the element is complete
            description = descriptionBuilder.toString().trim();
        }
    }

    private static class PrinteableArrayList<T> extends ArrayList<T> {

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            for (T t : this) {
                b.append(t.toString());
                b.append(',');
            }
            if (b.length() > 0) {
                b.deleteCharAt(b.length() - 1);
            }
            return b.toString();
        }
    }

    private static class ContentHandlerAdapter implements ContentHandler {

        public void setDocumentLocator(Locator locator) {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void skippedEntity(String name) throws SAXException {
        }
    }
}
