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

package org.netbeans.modules.vmd.midpnb.components.svg.util;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;

/**
 *
 * @author Anton Chechel
 */
public final class SVGUtils {

    private static final String MENU_ELEMENT_SEARCH_PATTERN = "menuItem_.*"; // NOI18N

    private SVGUtils() {
    }

    public static void parseSVGMenu(final InputStream svgInputStream, final DesignComponent svgComponent) {
        final String[] menuItems = SVGUtils.getMenuItems(svgInputStream);
        if (menuItems != null) {
            svgComponent.getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    List<PropertyValue> list = new ArrayList<PropertyValue>(menuItems.length);
                    for (String item : menuItems) {
                        DesignComponent es = svgComponent.getDocument().createComponent(SVGMenuElementEventSourceCD.TYPEID);
                        es.writeProperty(SVGMenuElementEventSourceCD.PROP_STRING, MidpTypes.createStringValue(item));
                        list.add(PropertyValue.createComponentReference(es));
                        svgComponent.addComponent(es);
                    }
                    svgComponent.writeProperty(SVGMenuCD.PROP_ELEMENTS, PropertyValue.createArray(SVGMenuElementEventSourceCD.TYPEID, list));
                }
            });
        }
    }

    private static String[] getMenuItems(final InputStream svgInputStream) {
        NamedElementsContentHandler ch = new NamedElementsContentHandler(MENU_ELEMENT_SEARCH_PATTERN);
        try {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(ch);
            parser.setEntityResolver(ch);
            parser.parse(new InputSource(svgInputStream));
        } catch (IOException ex) {
            Debug.warning(ex);
        } catch (SAXException ex) {
            Debug.warning(ex);
        }
        ch.sortNamedElements();
        return ch.getFoundElements();
    }

    private static class NamedElementsContentHandler implements ContentHandler, EntityResolver {

        private ArrayList<String> foundElements;
        private Pattern regex;

        public NamedElementsContentHandler(String regex) {
            this.foundElements = new ArrayList<String>();
            this.regex = Pattern.compile(regex);
        }

        public void sortNamedElements() {
            Collections.sort(foundElements);
        }

        public String[] getFoundElements() {
            return foundElements.toArray(new String[foundElements.size()]);
        }

        public final void resetFoundElements() {
            foundElements.clear();
        }

        public final void endDocument() throws SAXException {
        }

        public final void startDocument() throws SAXException {
        }

        public final void characters(char[] ch, int start, int length) throws SAXException {
        }

        public final void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public final void endPrefixMapping(String prefix) throws SAXException {
        }

        public final void skippedEntity(String name) throws SAXException {
        }

        public final void setDocumentLocator(Locator locator) {
        }

        public final void processingInstruction(String target, String data) throws SAXException {
        }

        public final void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        public final void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        }

        public final void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            // get id attribute value
            final String value = atts.getValue("id"); // NOI18N
            if ((value != null) && regex.matcher(value).matches()) {
                foundElements.add(value);
            }
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    }
}