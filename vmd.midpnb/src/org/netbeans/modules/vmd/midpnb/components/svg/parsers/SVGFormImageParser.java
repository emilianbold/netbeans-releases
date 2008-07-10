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
import java.util.Stack;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGButtonCD;
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

    private static final String FORM_COMPONENT_ID_BUTTON = "button_ok"; // NOI18N
    private static final String FORM_COMPONENT_ID_LABEL = "label"; // NOI18N
    private static final String FORM_COMPONENT_ID_RADIO = "radio_"; // NOI18N
    private static final String FORM_COMPONENT_ID_CHECKBOX = "checkbox_online"; // NOI18N
    private static final String FORM_COMPONENT_ID_COMBOBOX = "country_combobox"; // NOI18N
    private static final String FORM_COMPONENT_ID_LIST = "list"; // NOI18N
    private static final String FORM_COMPONENT_ID_SLIDER = "size_slider"; // NOI18N
    private static final String FORM_COMPONENT_ID_SPINNER = "age_spinner"; // NOI18N
    private static final String FORM_COMPONENT_ID_TEXTFIELD = "textfield_name"; // NOI18N

    public void parse(InputStream svgInputStream, DesignComponent svgComponent) {
        parseSVGForm(svgInputStream, svgComponent);
    }

    public static void parseSVGForm(final InputStream svgInputStream, final DesignComponent svgComponent) {
        final SVGFormComponent[] srcComponents = getFormComponents(svgInputStream);
        if (srcComponents != null) {
            svgComponent.getDocument().getTransactionManager().writeAccess(new Runnable() {

                public void run() {
                    //List<PropertyValue> list = new ArrayList<PropertyValue>(srcComponents.length);
                    
                    //Clean Up all components
                    Collection<DesignComponent> components = new HashSet(svgComponent.getComponents());
                    DescriptorRegistry registry = svgComponent.getDocument().getDescriptorRegistry();
                    for (DesignComponent component : components) {
                        if (registry.isInHierarchy(SVGComponentCD.TYPEID, component.getType())) {
                            svgComponent.getDocument().deleteComponent(component);
                        }
                    }
                    for (SVGFormComponent srcComponent : srcComponents) {
                        DesignComponent es = srcComponent.createComponent(svgComponent);

                        //list.add(PropertyValue.createComponentReference(es));
                        svgComponent.addComponent(es);
                    }
                //svgComponent.writeProperty(SVGFormCD.PROP_ELEMENTS, PropertyValue.createArray(SVGComponentCD.TYPEID, list));
                //svgComponent.writeProperty(SVGFormCD.PROP_ELEMENTS_COUNT, MidpTypes.createIntegerValue(list.size()));
                }
            });
        }
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

//    private static class SVGButton extends SVGFormComponent{
//
//        SVGButton(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGButtonCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGLabel extends SVGFormComponent{
//
//        SVGLabel(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGLabelCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGRadioButton extends SVGFormComponent{
//
//        SVGRadioButton(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGRadioButtonCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGCheckBox extends SVGFormComponent{
//
//        SVGCheckBox(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGCheckBoxCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGComboBox extends SVGFormComponent{
//
//        SVGComboBox(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGComboBoxCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGList extends SVGFormComponent{
//
//        SVGList(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGListCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGSlider extends SVGFormComponent{
//
//        SVGSlider(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGSliderCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGSpinner extends SVGFormComponent{
//
//        SVGSpinner(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGSpinnerCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
//    private static class SVGTextField extends SVGFormComponent{
//
//        SVGTextField(String id) {
//            super(id);
//        }
//
//        @Override
//        DesignComponent createComponent(DesignComponent parentComponent) {
//            DesignComponent dc = parentComponent.getDocument().createComponent(SVGTextFieldCD.TYPEID);
//            dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
//            return dc;
//        }
//    }
    private abstract static class SVGFormComponent {

        private static SVGFormComponent create(final String id, final TypeID type) {
            return new SVGFormComponent(id) {

                @Override
                DesignComponent createComponent( DesignComponent parentComponent) {
                    DesignComponent dc = parentComponent.getDocument().createComponent(type);
                    dc.writeProperty(SVGComponentCD.PROP_ID, MidpTypes.createStringValue(getId()));
                    return dc;
                }
            };
        }
        private String myId;

        SVGFormComponent(String id) {
            myId = id;
        }

        abstract DesignComponent createComponent(DesignComponent parentComponent);

        protected String getId() {
            return myId;
        }
    }

    private static class NamedElementsContentHandler extends AbstractElementsContentHandler {

        private ArrayList<SVGFormComponent> foundElements;
        private Stack nodes;

        public NamedElementsContentHandler() {
            this.foundElements = new ArrayList<SVGFormComponent>();
            nodes = new Stack();
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
            if (value.startsWith(FORM_COMPONENT_ID_BUTTON)) {
                foundElements.add(SVGFormComponent.create(value, SVGButtonCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_CHECKBOX)) {
                foundElements.add(SVGFormComponent.create(value, SVGCheckBoxCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_COMBOBOX)) {
                foundElements.add(SVGFormComponent.create(value, SVGComboBoxCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_LABEL)) {
                foundElements.add(SVGFormComponent.create(value, SVGLabelCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_LIST)) {
                foundElements.add(SVGFormComponent.create(value, SVGListCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_RADIO)) {
                foundElements.add(SVGFormComponent.create(value,SVGRadioButtonCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_SLIDER)) {
                foundElements.add(SVGFormComponent.create(value, SVGSliderCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_SPINNER)) {
                foundElements.add(SVGFormComponent.create(value, SVGSpinnerCD.TYPEID));
            } else if (value.startsWith(FORM_COMPONENT_ID_TEXTFIELD)) {
                foundElements.add(SVGFormComponent.create(value, SVGTextFieldCD.TYPEID));
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
        }
    }
}

