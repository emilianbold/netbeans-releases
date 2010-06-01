/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.hl7.aip.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.encoder.hl7.aip.EncodingOption;
import org.netbeans.modules.encoder.hl7.aip.HL7EncodingConst;
import org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException;
import org.netbeans.modules.encoder.ui.basic.SchemaUtility;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.encoder.ui.basic.ValidationContext;
import org.netbeans.modules.encoder.ui.basic.ValidationException;
import org.netbeans.modules.encoder.ui.basic.XsdBasedEncoderValidator;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.NameableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.openide.util.NbBundle;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class that provides validation functionality for HL7 encoder.
 *
 * @author Jun Xu
 */
public class HL7EncoderValidator extends XsdBasedEncoderValidator {
    
    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/hl7/aip/validation/Bundle");
    private static final String PROP_LEVEL = "level";   //NOI18N
    //Maximum level should be 4 according to HL7 specification but in XML
    //representation the top element should not be counted in the level.
    private static final int MAX_LEVEL = 5;
    
    /** Creates a new instance of CustomEncoderValidator
     * @param model the schema model
     * @param handler a SAX error handler
     */
    public HL7EncoderValidator(SchemaModel model, ErrorHandler handler) {
        super(HL7EncodingConst.STYLE, model, handler);
    }
    
    protected void foundNoTopElements()
            throws ValidationException, SAXException {
        
        super.foundNoTopElements();
        
        _handler.warning(
                new SAXParseException(
                    _bundle.getString("hl7_validator.exp.no_top_element"),
                    ModelUtils.getPublicId(_model),
                    null /*ModelUtils.getSystemId(_model)*/,
                    -1,
                    -1));
    }
    
    protected void detectedRecursiveCondition(ValidationContext context,
            SchemaComponent schemaComponent)
            throws ValidationException, SAXException {
        
        super.detectedRecursiveCondition(context, schemaComponent);
        
        String msg = NbBundle.getMessage(HL7EncoderValidator.class,
                "hl7_validator.exp.recursive_condition",  //NOI18N
                context.getElementNCNamePath(schemaComponent));
        _handler.error(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(schemaComponent.getModel()),
                    ModelUtils.getSystemId(schemaComponent.getModel()),
                    -1,
                    -1));
    }
    
    protected void validate(ValidationContext context,
            GlobalElement globalElement)
            throws ValidationException, SAXException {
    
        super.validate(context, globalElement);
        
        validateLevel(context, globalElement);
        validateMetadataSyntax(context, globalElement);
        validateSupportedType(context, globalElement);
    }

    protected void validate(ValidationContext context,
            LocalElement localElement)
            throws ValidationException, SAXException {
        
        super.validate(context, localElement);
        
        validateLevel(context, localElement);
        validateMetadataSyntax(context, localElement);
        validateSupportedType(context, localElement);
    }

    protected void validate(ValidationContext context,
            GlobalComplexType globalComplexType)
            throws ValidationException, SAXException {
        
        super.validate(context, globalComplexType);
        
        validateMetadataSyntax(context, globalComplexType);
    }

    protected void validate(ValidationContext context,
            LocalComplexType localComplexType)
            throws ValidationException, SAXException {

        super.validate(context, localComplexType);
        
        validateMetadataSyntax(context, localComplexType);
    }

    protected void validate(ValidationContext context,
            GlobalSimpleType globalSimpleType)
            throws ValidationException, SAXException {

        super.validate(context, globalSimpleType);
        
        validateMetadataSyntax(context, globalSimpleType);
    }

    protected void validate(ValidationContext context,
            LocalSimpleType localSimpleType)
            throws ValidationException, SAXException {

        super.validate(context, localSimpleType);
        
        validateMetadataSyntax(context, localSimpleType);
    }

    protected void validate(ValidationContext context, Sequence sequence)
            throws ValidationException, SAXException {

        super.validate(context, sequence);
        
        checkGroupWithinGroup(context, sequence);
    }

    protected void validate(ValidationContext context, All all)
            throws ValidationException, SAXException {

        super.validate(context, all);
        
        checkGroupWithinGroup(context, all);
    }

    protected void validate(ValidationContext context, Choice choice)
            throws ValidationException, SAXException {

        super.validate(context, choice);
        
        checkGroupWithinGroup(context, choice);
    }
    
    protected void validate(ValidationContext context, GroupReference groupRef)
            throws ValidationException, SAXException {

        super.validate(context, groupRef);
        
        checkGroupWithinGroup(context, groupRef);
    }
    
    protected void prepareContext(ValidationContext context)
            throws ValidationException, SAXException  {
        super.prepareContext(context);
        
        if (context.getPath().isEmpty()) {
            context.setProperty(PROP_LEVEL, new Integer(0));
            return;
        }
        Integer objLevel = (Integer) context.getProperty(PROP_LEVEL);
        if (objLevel == null) {
            throw new ValidationException(
                    _bundle.getString("hl7_validator.exp.missing_level_prop"));
        }
        SchemaComponent sc = context.getPath().get(context.getPath().size() - 1);
        if (!(sc instanceof Element) || (sc instanceof ElementReference)) {
            return;
        }
        if (!(sc instanceof NameableSchemaComponent)) {
            return;
        }
        String name = ((NameableSchemaComponent) sc).getName();
        if (name.startsWith(context.getTopElementNCName() + ".")) {  //NOI18N
            //Element with name starts with top element name as prefix is
            //considered as group and will not account for level increase.
            return;
        }
        context.setProperty(PROP_LEVEL, new Integer(objLevel.intValue() + 1));
    }

    private void checkGroupWithinGroup(ValidationContext context,
            SchemaComponent group)
            throws ValidationException, SAXException {
        if (context.getPath().size() == 0) {
            return;
        }
        SchemaComponent sc =
                context.getPath().get(context.getPath().size() - 1);
        if ((sc instanceof LocalGroupDefinition)
                || (sc instanceof GroupReference)) {
            String name = null;
            for (int i = context.getPath().size() - 1; i >= 0; i--) {
                if (context.getPath().get(i)
                        instanceof NameableSchemaComponent) {
                    name = ((NameableSchemaComponent)
                                context.getPath().get(i)).getName();
                    sc = context.getPath().get(i);
                    break;
                }
            }
            if (name == null) {
                name = "";  //NOI18N
            }
            String msg = NbBundle.getMessage(HL7EncoderValidator.class,
                    "hl7_validator.exp.grp_in_grp_not_supported", name);  //NOI18N
            
            _handler.error(
                    new SAXParseException(
                        msg,
                        ModelUtils.getPublicId(sc.getModel()),
                        ModelUtils.getSystemId(sc.getModel()),
                        -1,
                        -1));
        }
    }
    
    private void validateLevel(ValidationContext context, Element elem)
            throws ValidationException, SAXException {
        Integer objLevel = (Integer) context.getProperty(PROP_LEVEL);
        if (objLevel == null) {
            throw new ValidationException(
                    _bundle.getString("hl7_validator.exp.missing_level_property"));
        }
        if (objLevel.intValue() < MAX_LEVEL) {
            return;
        }
        if (objLevel.intValue() > MAX_LEVEL) {
            //Already has too many levels. Don't need to do further check
            String msg =
                    NbBundle.getMessage(
                        HL7EncoderValidator.class,
                        "hl7_validator.exp.too_many_levels",  //NOI18N
                        context.getElementNCNamePath(elem),
                        MAX_LEVEL - 1,
                        objLevel.intValue());
            _handler.warning(
                    new SAXParseException(
                        msg,
                        ModelUtils.getPublicId(elem.getModel()),
                        null /*ModelUtils.getSystemId(elem.getModel())*/,
                        -1,
                        -1));
            return;
        }
        if (elem instanceof ElementReference) {
            return;
        }
        if (!(elem instanceof NameableSchemaComponent)) {
            return;
        }
        String name = ((NameableSchemaComponent) elem).getName();
        if (name.startsWith(context.getTopElementPrefix())) {
            //Element with name starts with top element name as prefix is
            //considered as group and will not account for level increase.
            return;
        }
        if (!(elem instanceof TypeContainer)) {
            return;
        }
        SchemaComponent type = getType(((TypeContainer) elem));
        if (type == null || (type instanceof GlobalType)
                && "escapeType".equals(((GlobalType) type).getName())) {  //NOI18N
            //element representing escape sequence is allowed as the fifth level element
            return;
        }
        String msg =
                NbBundle.getMessage(
                    HL7EncoderValidator.class,
                    "hl7_validator.exp.too_many_levels",  //NOI18N
                    context.getElementNCNamePath(elem),
                    MAX_LEVEL - 1,
                    objLevel.intValue());
        _handler.warning(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(elem.getModel()),
                    null /*ModelUtils.getSystemId(elem.getModel())*/,
                    -1,
                    -1));
    }
    
    private void validateMetadataSyntax(ValidationContext context, SchemaComponent sc)
            throws ValidationException, SAXException {
        if (sc.getAnnotation() == null) {
            return;
        }
        List<SchemaComponent> path =
                new ArrayList<SchemaComponent>(context.getPath());
        path.add(sc);
        path.add(sc.getAnnotation());
        EncodingOption encodingInfo;
        try {
            encodingInfo =
                    EncodingOption.createFromAppInfo(path);
        } catch (InvalidAppInfoException ex) {
            _handler.error(
                    new SAXParseException(
                        ex.getMessage(),
                        ModelUtils.getPublicId(sc.getModel()),
                        ModelUtils.getSystemId(sc.getModel()),
                        -1,
                        -1,
                        ex));
        }
    }
    
    private void validateSupportedType(ValidationContext context, Element elem)
            throws ValidationException, SAXException {
        if (!(elem instanceof TypeContainer)) {
            return;
        }
        if (SchemaUtility.isAnyType(getType((TypeContainer) elem))) {
            String msg =
                    NbBundle.getMessage(
                            HL7EncoderValidator.class,
                            "hl7_validator.exp.anytype_not_supported",  //NOI18N
                            context.getElementNCNamePath(elem));
            _handler.error(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(elem.getModel()),
                    null /*ModelUtils.getSystemId(elem.getModel())*/,
                    -1,
                    -1));
        }
    }
}
