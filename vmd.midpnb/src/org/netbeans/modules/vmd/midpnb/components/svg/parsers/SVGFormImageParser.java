/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.components.svg.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.netbeans.modules.mobility.svgcore.util.SVGComponentsSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGCheckBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComboBoxCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGLabelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGRadioButtonCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGSliderCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGSpinnerCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGTextFieldCD;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author avk
 */
public class SVGFormImageParser extends SVGComponentImageParser {

    private static final String DIGITS = "_\\d+$"; //NOI18N
    private static final String PREFIX = "^"; //NOI18N
    private static final Pattern FORM_COMPONENT_ID_BUTTON = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_BUTTON + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_LABEL = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_LABEL + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_RADIO = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_RADIOBUTTON + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_CHECKBOX = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_CHECKBOX + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_COMBOBOX = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_COMBOBOX + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_LIST = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_LIST + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_SLIDER = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_SLIDER + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_SPINNER = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_SPINNER + DIGITS);
    private static final Pattern FORM_COMPONENT_ID_TEXTFIELD = Pattern.compile(PREFIX + SVGComponentsSupport.ID_PREFIX_TEXTFIELD + DIGITS); // NOI18N

    public synchronized static void parseSVGForm(final InputStream svgInputStream, final DesignComponent svgForm) {
        final SVGFormComponent[] srcComponents = getFormComponents(svgInputStream);
        if (srcComponents != null) {
            svgForm.getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {

                    Collection<DesignComponent> components = new HashSet(svgForm.getComponents());
                    DescriptorRegistry registry = svgForm.getDocument().getDescriptorRegistry();
                    for (DesignComponent component : components) {
                        if (registry.isInHierarchy(SVGComponentCD.TYPEID, component.getType())) {
                            removeSVGButtonEventSource(component, svgForm);
                            svgForm.getDocument().deleteComponent(component);
                        }
                    }
                    for (SVGFormComponent srcComponent : srcComponents) {
                        DesignComponent es = srcComponent.createComponent(svgForm);
                        svgForm.addComponent(es);
                    }
                }
            });
        }
    }

    public void parse(InputStream svgInputStream, DesignComponent svgComponent) {
        parseSVGForm(svgInputStream, svgComponent);
    }

    private static SVGFormComponent[] getFormComponents(final InputStream svgInputStream) {
        NamedElementsContentHandler ch = new NamedElementsContentHandler();
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
        return ch.getFoundElements();
    }

    public abstract static class SVGFormComponent {

        public static SVGFormComponent create(final String id, final TypeID type) {
            return new SVGFormComponent(id, type) {

                @Override
                public DesignComponent createComponent(DesignComponent parentComponent) {
                    DesignComponent dc = parentComponent.getDocument().createComponent(type);
                    dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
                    return dc;
                }
            };
        }

        public static SVGFormComponent createSVGButton(final String id, final TypeID type) {
            return new SVGFormComponent(id, type) {

                @Override
                public DesignComponent createComponent(DesignComponent parentComponent) {
                    DesignComponent dc = parentComponent.getDocument().createComponent(type);
                    DesignComponent svgBES = parentComponent.getDocument().createComponent(SVGButtonEventSourceCD.TYPEID);
                    svgBES.writeProperty(SVGButtonEventSourceCD.PROP_SVGBUTTON, PropertyValue.createComponentReference(dc));
                    parentComponent.addComponent(svgBES);
                    dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
                    return dc;
                }
            };
        }
        private String id;
        private TypeID type;

        SVGFormComponent(String id, TypeID type) {
            this.type = type;
            this.id = id;
        }

        public abstract DesignComponent createComponent(DesignComponent parentComponent);

        String getId() {
            return id;
        }

        TypeID getTypeID() {
            return type;
        }
    }

    private static class NamedElementsContentHandler extends AbstractElementsContentHandler {

        private ArrayList<SVGFormComponent> foundElements;

        public NamedElementsContentHandler() {
            this.foundElements = new ArrayList<SVGFormComponent>();
        }

        public SVGFormComponent[] getFoundElements() {
            return foundElements.toArray(new SVGFormComponent[foundElements.size()]);
        }

        public final void resetFoundElements() {
            foundElements.clear();
        }

        @Override
        public final void startElement(String namespaceURI, String localName,
                String qName, Attributes atts)
                throws SAXException {
            // get id attribute value
            final String value = atts.getValue("id"); // NOI18N
            if (value == null) {
                return;
            }
            if (FORM_COMPONENT_ID_BUTTON.matcher(value).find()) {
                foundElements.add(SVGFormComponent.createSVGButton(value, SVGButtonCD.TYPEID));
            } else if (FORM_COMPONENT_ID_CHECKBOX.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGCheckBoxCD.TYPEID));
            } else if (FORM_COMPONENT_ID_COMBOBOX.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGComboBoxCD.TYPEID));
            } else if (FORM_COMPONENT_ID_LABEL.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGLabelCD.TYPEID));
            } else if (FORM_COMPONENT_ID_LIST.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGListCD.TYPEID));
            } else if (FORM_COMPONENT_ID_RADIO.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGRadioButtonCD.TYPEID));
            } else if (FORM_COMPONENT_ID_SLIDER.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGSliderCD.TYPEID));
            } else if (FORM_COMPONENT_ID_SPINNER.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGSpinnerCD.TYPEID));
            } else if (FORM_COMPONENT_ID_TEXTFIELD.matcher(value).find()) {
                foundElements.add(SVGFormComponent.create(value, SVGTextFieldCD.TYPEID));
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
        }
    }

    /**
     * Search for SVGComponents in the given SVG image (Tiny)
     * @param svgInputStream - SVG image
     * @return Array of svg id components with SVGCOmponent ID
     */
    public static final String[][] getComponentsInformation(final InputStream svgInputStream) {
        SVGFormComponent[] components = getFormComponents(svgInputStream);
        String[][] values = new String[components.length][2];
        for (int i = 0; i < components.length; i++) {
            values[i][1] = components[i].getId();
            values[i][0] = MidpTypes.getSimpleClassName(components[i].getTypeID());
        }

        return values;
    }

    public static void removeSVGButtonEventSource(DesignComponent svgButton, DesignComponent svgForm) {
        if (svgButton.getType() != SVGButtonCD.TYPEID) {
            return;
        }
        Collection<DesignComponent> components = new HashSet(svgForm.getComponents());
        for (DesignComponent potentialButtonEventSource : components) {
            if (potentialButtonEventSource.getType() != SVGButtonEventSourceCD.TYPEID) {
                continue;
            }
            PropertyValue value = potentialButtonEventSource.readProperty(SVGButtonEventSourceCD.PROP_SVGBUTTON);
            if (value != null && value.getComponent() != null && value.getComponent() == svgButton) {
                potentialButtonEventSource.getDocument().deleteComponent(potentialButtonEventSource);
            }
        }
    }
}

