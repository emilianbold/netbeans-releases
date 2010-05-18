/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

/*
 *    This class is modified based on {@link org.chiba.tools.schemabuilder.BaseSchemaFormBuilder} from chiba
 *    (http://chiba.sourceforge.net/).
 *    
 *    The modifications made on AbstractSchemaFormBuilder are :
 *    
 * 1. Add new constructor to differentiate controls generated for readonly xforms
 * 2. Generate textarea control for certain captions
 * 3. Add new constructors for setting default vlaue in xform
 */

import org.apache.xerces.xs.*;
import org.chiba.tools.schemabuilder.SchemaFormBuilder;
import org.chiba.tools.schemabuilder.StringUtil;
import org.chiba.tools.schemabuilder.WrapperElementsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.transform.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/*
 * Search for TODO for things remaining to-do in this implementation.
 *
 * TODO: i18n/l10n of messages, hints, captions. Possibly leverage org.chiba.i18n classes.
 * TODO: When Chiba supports itemset, use schema keyref and key constraints for validation.
 * TODO: Add support for default and fixed values.
 * TODO: Add support for use=prohibited.
 */
import javax.xml.namespace.QName;

/**
 * A concrete base implementation of the SchemaFormBuilder interface allowing
 * an XForm to be automatically generated for an XML Schema definition.
 *
 * @author Brian Dueck
 * @version $Id$
 */
public class BaseSchemaFormBuilder
        extends AbstractSchemaFormBuilder
        implements SchemaFormBuilder {
    /**
     * Creates a new instance of BaseSchemaForBuilder
     */
    public BaseSchemaFormBuilder(QName rootElName, QName rootTypeName) {
        super(rootElName, rootTypeName);
    }
    
    public BaseSchemaFormBuilder(QName rootElName, QName rootTypeName, boolean addSubmit, Map defaultValues) {
        super(rootElName, rootTypeName, addSubmit, defaultValues);
    }
   

    /**
     * Creates a new BaseSchemaFormBuilder object.
     *
     * @param rootTagName    __UNDOCUMENTED__
     * @param instanceSource __UNDOCUMENTED__
     * @param action         __UNDOCUMENTED__
     * @param submitMethod   __UNDOCUMENTED__
     * @param wrapper        __UNDOCUMENTED__
     * @param stylesheet     __UNDOCUMENTED__
     */
    public BaseSchemaFormBuilder(QName rootTagName,
                                 Source instanceSource,
                                 String action,
                                 String submitMethod,
                                 WrapperElementsBuilder wrapper,
                                 String stylesheet,
                                 String base,
                                 boolean userSchemaTypes,
                                 boolean addSubmit,
                                 Map defaultValues) {
        super(rootTagName,
                instanceSource,
                action,
                submitMethod,
                wrapper,
                stylesheet,
                base,
                userSchemaTypes,
                addSubmit, defaultValues);
    }

    /**
     * Creates a new BaseSchemaFormBuilder object.
     *
     * @param rootTagName  __UNDOCUMENTED__
     * @param instanceHref __UNDOCUMENTED__
     * @param action       __UNDOCUMENTED__
     * @param submitMethod __UNDOCUMENTED__
     * @param wrapper      __UNDOCUMENTED__
     * @param stylesheet   __UNDOCUMENTED__
     */
    public BaseSchemaFormBuilder(QName rootTagName,
                                 String instanceHref,
                                 String action,
                                 String submitMethod,
                                 WrapperElementsBuilder wrapper,
                                 String stylesheet,
                                 String base,
                                 boolean userSchemaTypes,
                                 boolean addSubmit,
                                 Map defaultValues) {
        super(rootTagName,
                instanceHref,
                action,
                submitMethod,
                wrapper,
                stylesheet,
                base,
                userSchemaTypes,
                addSubmit, defaultValues);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param text __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(String text) {
        return StringUtil.capitalizeIdentifier(text);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param attribute __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSAttributeDeclaration attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getName());
    }

    public String createCaption(XSAttributeUse attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getAttrDeclaration().getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSElementDeclaration element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(element.getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSObject element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        if (element instanceof XSElementDeclaration) {
            return createCaption(((XSElementDeclaration) element).getName());
        } else if (element instanceof XSAttributeDeclaration) {
            return createCaption(((XSAttributeDeclaration) element).getName());
        } else if (element instanceof XSAttributeUse) {
            return createCaption(((XSAttributeUse) element).getAttrDeclaration().getName());
        } else
//            LOGGER.warn("WARNING: createCaption: element is not an attribute nor an element: "
//                    + element.getClass().getName());

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAnyType(Document xForm,
                                           String caption,
                                           XSTypeDefinition controlType) {
        Element control =
                xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "textarea");
        this.setXFormsId(control);
        control.setAttributeNS(CHIBA_NS, getChibaNSPrefix() + "height", "3");

        //label
        Element captionElement =
                (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                        getXFormsNSPrefix() + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAtomicType(Document xForm,
                                              String caption,
                                              XSSimpleTypeDefinition controlType) {
        Element control;

        //remove while select1 do not work correctly in repeats
        if ((controlType.getName() != null)
                && controlType.getName().equals("boolean")) {
            control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select1");
            control.setAttribute( "appearance",
                    "full");
            this.setXFormsId(control);

            Element item_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item_true);
            Element label_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
             this.setXFormsId(label_true);
            Text label_true_text = xForm.createTextNode("True");
            label_true.appendChild(label_true_text);
            item_true.appendChild(label_true);

            Element value_true =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value_true);
            Text value_true_text = xForm.createTextNode("true()");
            value_true.appendChild(value_true_text);
            item_true.appendChild(value_true);
            control.appendChild(item_true);

            Element item_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item_false);
            Element label_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
            this.setXFormsId(label_false);
            Text label_false_text = xForm.createTextNode("False");
            label_false.appendChild(label_false_text);
            item_false.appendChild(label_false);

            Element value_false =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value_false);
            Text value_false_text = xForm.createTextNode("false()");
            value_false.appendChild(value_false_text);
            item_false.appendChild(value_false);
            control.appendChild(item_false);
        } else {
            String lowerCap = caption.toLowerCase();
            if (lowerCap.contains("description") || lowerCap.contains("comments")
                    || lowerCap.contains("feedback")) {
                control = xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "textarea");
            } else {
                control = xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "input");
                this.setXFormsId(control);
            }
        }

        //label
        Element captionElement =
                (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                        getXFormsNSPrefix() + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForEnumerationType(Document xForm,
                                                   XSSimpleTypeDefinition controlType,
                                                   String caption,
                                                   Element bindElement) {
        // TODO: Figure out an intelligent or user determined way to decide between
        // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
        // Possibly look for special appInfo section in the schema and if not present default to comboBox...
        //
        // For now, use radio if enumValues < DEFAULT_LONG_LIST_MAX_SIZE otherwise use combobox
        //
       StringList enumFacets = null;
       final String [] BOOLEAN_LIST = {"true", "false"};
       final String [] BOOLEAN_VAL_LIST = {"true", "false"};
       int nbFacets = 0;
       if ( getDataTypeName(getBuiltInType(controlType)).equals("boolean")) {
           nbFacets = 2;
       } else {
          enumFacets = controlType.getLexicalEnumeration();
          nbFacets = enumFacets.getLength();
       }
        if (nbFacets > 0) {
            List<TextValue> textVals = new ArrayList<TextValue> ();

            Element control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select1");
            this.setXFormsId(control);

            //label
            Element captionElement1 =
                    (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "label"));
            this.setXFormsId(captionElement1);
            captionElement1.appendChild(xForm.createTextNode(caption));

            Element choices =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "choices");
            this.setXFormsId(choices);
            
            if (enumFacets != null) {
                for (int i = 0; i < nbFacets; i++) {
                    String facet = enumFacets.item(i);
                    TextValue textVal = new TextValue (createCaption(facet), facet);
                    textVals.add(textVal);
                }
            } else {
                for (int i = 0; i < nbFacets; i++) {
                    String facet = BOOLEAN_LIST[i];
                    TextValue textVal = new TextValue (facet, BOOLEAN_VAL_LIST[i]);
                    textVals.add(textVal);
                }  
                
            }

            if (nbFacets
                    < Long.parseLong(getProperty(SELECTONE_LONG_LIST_SIZE_PROP))) {
                control.setAttribute(
                        "appearance",
                        getProperty(SELECTONE_UI_CONTROL_SHORT_PROP));
            } else {
                control.setAttribute(
                        "appearance",
                        getProperty(SELECTONE_UI_CONTROL_LONG_PROP));

                // add the "Please select..." instruction item for the combobox
                // and set the isValid attribute on the bind element to check for the "Please select..."
                // item to indicate that is not a valid value
                //
                {
                    String pleaseSelect = "[Select1 " + caption + "]";
                    Element item =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "item");
                    this.setXFormsId(item);
                    choices.appendChild(item);

                    Element captionElement =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "label");
                    this.setXFormsId(captionElement);
                    item.appendChild(captionElement);
                    captionElement.appendChild(xForm.createTextNode(pleaseSelect));

                    Element value =
                            xForm.createElementNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "value");
                    this.setXFormsId(value);
                    item.appendChild(value);
                    value.appendChild(xForm.createTextNode(pleaseSelect));

                    // not(purchaseOrder/state = '[Choose State]')
                    //String isValidExpr = "not(" + bindElement.getAttributeNS(XFORMS_NS,"nodeset") + " = '" + pleaseSelect + "')";
                    // ->no, not(. = '[Choose State]')
                    String isValidExpr = "not( . = '" + pleaseSelect + "')";

                    //check if there was a constraint
                    String constraint =
                            bindElement.getAttributeNS(XFORMS_NS, "constraint");

                    if ((constraint != null) && !constraint.equals("")) {
                        constraint = constraint + " and " + isValidExpr;
                    } else {
                        constraint = isValidExpr;
                    }

                    bindElement.setAttributeNS(XFORMS_NS,
                            getXFormsNSPrefix() + "constraint",
                            constraint);
                }
            }

            control.appendChild(choices);

            addChoicesForSelectControl(xForm, choices, textVals);

            return control;
        } else {
            return null;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param listType    __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForListType(Document xForm,
                                            XSSimpleTypeDefinition listType,
                                            String caption,
                                            Element bindElement) {
        XSSimpleTypeDefinition controlType = listType.getItemType();

        StringList enumFacets = controlType.getLexicalEnumeration();
        int nbFacets = enumFacets.getLength();
        if (nbFacets > 0) {
            Element control =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "select");
            this.setXFormsId(control);

            //label
            Element captionElement =
                    (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "label"));
            this.setXFormsId(captionElement);
            captionElement.appendChild(xForm.createTextNode(caption));

            List<TextValue> textVals = new ArrayList<TextValue> ();
            for (int i = 0; i < nbFacets; i++) {
                String facet = enumFacets.item(i);
                TextValue textVal = new TextValue (facet, facet);
                textVals.add(textVal);
            }

            // TODO: Figure out an intelligent or user determined way to decide between
            // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
            // Possibly look for special appInfo section in the schema and if not present default to checkBox...
            //
            // For now, use checkbox if there are < DEFAULT_LONG_LIST_MAX_SIZE items, otherwise use long control
            //
            if (textVals.size()
                    < Long.parseLong(getProperty(SELECTMANY_LONG_LIST_SIZE_PROP))) {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTMANY_UI_CONTROL_SHORT_PROP));
            } else {
                control.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "appearance",
                        getProperty(SELECTMANY_UI_CONTROL_LONG_PROP));
            }

            Element choices =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "choices");
            this.setXFormsId(choices);
            control.appendChild(choices);

            addChoicesForSelectControl(xForm, choices, textVals);

            return control;
        } else {
            return null;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm __UNDOCUMENTED__
     * @param node  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createHint(Document xForm, XSObject node) {
        XSAnnotation annotation = null;
        try {
        if (node instanceof XSElementDeclaration)
            annotation = ((XSElementDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeDeclaration)
            annotation = ((XSAttributeDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeUse)
            annotation =
                    ((XSAttributeUse) node).getAttrDeclaration().getAnnotation();

        if (annotation != null)
            return addHintFromDocumentation(xForm, annotation);
        else
            return null;
        }catch (Exception e) {
            return null;
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     */
    public void endBindElement(Element bindElement) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     */
    public void endFormControl(Element controlElement,
                               XSTypeDefinition controlType,
                               int minOccurs,
                               int maxOccurs) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement __UNDOCUMENTED__
     */
    public void endFormGroup(Element groupElement,
                             XSTypeDefinition controlType,
                             int minOccurs,
                             int maxOccurs,
                             Element modelSection) {
        return;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param minOccurs   __UNDOCUMENTED__
     * @param maxOccurs   __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startBindElement(Element bindElement,
                                    XSTypeDefinition controlType,
                                    int minOccurs,
                                    int maxOccurs) {
        // START WORKAROUND
        // Due to a Chiba bug, anyType is not a recognized type name.
        // so, if this is an anyType, then we'll just skip the type
        // setting.
        //
        // type.getName() may be 'null' for anonymous types, so compare against
        // static string (see bug #1172541 on sf.net)
        if (!("anyType").equals(controlType.getName())) {
            Element enveloppe = bindElement.getOwnerDocument().getDocumentElement();
            String typeName = this.getXFormsTypeName(enveloppe, controlType);
            if (typeName != null && !typeName.equals(""))
                bindElement.setAttributeNS(XFORMS_NS,
                        getXFormsNSPrefix() + "type",
                        typeName);
        }

        if (minOccurs == 0) {
            bindElement.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "required",
                    "false()");
        } else {
            bindElement.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "required",
                    "true()");
        }

        //no more minOccurs & maxOccurs element: add a constraint if maxOccurs>1:
        //count(.) <= maxOccurs && count(.) >= minOccurs
        String minConstraint = null;
        String maxConstraint = null;

        if (minOccurs > 1) {
            //if 0 or 1 -> no constraint (managed by "required")
            minConstraint = "count(.) >= " + minOccurs;
        }

        if (maxOccurs > 1) {
            //if 1 or unbounded -> no constraint
            maxConstraint = "count(.) <= " + maxOccurs;
        }

        String constraint = null;

        if ((minConstraint != null) && (maxConstraint != null)) {
            constraint = minConstraint + " and " + maxConstraint;
        } else if (minConstraint != null) {
            constraint = minConstraint;
        } else {
            constraint = maxConstraint;
        }

        if ((constraint != null) && !constraint.equals("")) {
            bindElement.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "constraint",
                    constraint);
        }

        /*if (minOccurs != 1) {
           bindElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "minOccurs",String.valueOf(minOccurs));
           }
           if (maxOccurs != 1) {
               bindElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "maxOccurs",maxOccurs == -1 ? "unbounded" : String.valueOf((maxOccurs)));
           }*/
        return bindElement;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormControl(Element controlElement,
                                    XSTypeDefinition controlType) {
        return controlElement;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement  __UNDOCUMENTED__
     * @param schemaElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormGroup(Element groupElement,
                                  XSElementDeclaration schemaElement) {
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-align",getProperty(GROUP_BOX_ALIGN_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-orient",getProperty(GROUP_BOX_ORIENT_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "caption-width",getProperty(GROUP_CAPTION_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "width",getProperty(GROUP_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "border",getProperty(GROUP_BORDER_PROP));
        return groupElement;
    }
}
