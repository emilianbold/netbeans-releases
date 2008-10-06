/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midpnb.components.svg.parsers;

import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;
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
public final class SVGMenuImageParser extends SVGComponentImageParser {

    private static final String MENU_ELEMENT_SEARCH_PATTERN = "menuItem_.*"; // NOI18N

    public void parse(InputStream svgInputStream, DesignComponent svgComponent) {
        parseSVGMenu(svgInputStream, svgComponent);
    }

    public static void parseSVGMenu(final InputStream svgInputStream, final DesignComponent svgComponent) {
        
        final String[] menuItems = getMenuItems(svgInputStream);
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

    private static class NamedElementsContentHandler extends AbstractElementsContentHandler {

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

        @Override
        public final void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            // get id attribute value
            final String value = atts.getValue("id"); // NOI18N
            if ((value != null) && regex.matcher(value).matches()) {
                foundElements.add(value);
            }
        }

    }
}