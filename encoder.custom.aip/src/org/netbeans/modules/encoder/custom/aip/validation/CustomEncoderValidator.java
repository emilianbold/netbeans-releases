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

package org.netbeans.modules.encoder.custom.aip.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.encoder.custom.aip.CustomEncodingConst;
import org.netbeans.modules.encoder.custom.aip.EncodingOption;
import org.netbeans.modules.encoder.ui.basic.SchemaUtility;
import org.netbeans.modules.encoder.ui.basic.InvalidAppInfoException;
import org.netbeans.modules.encoder.ui.basic.ModelUtils;
import org.netbeans.modules.encoder.ui.basic.ValidationContext;
import org.netbeans.modules.encoder.ui.basic.ValidationException;
import org.netbeans.modules.encoder.ui.basic.XsdBasedEncoderValidator;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
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
 * Class that provides validation functionality for custom encoder.
 *
 * @author Jun Xu
 */
public class CustomEncoderValidator extends XsdBasedEncoderValidator {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/custom/aip/validation/Bundle");
    
    /** Creates a new instance of CustomEncoderValidator
     * @param model the schema model
     * @param handler a SAX error handler
     */
    public CustomEncoderValidator(SchemaModel model, ErrorHandler handler) {
        super(CustomEncodingConst.STYLE, model, handler);
    }
    
    @Override
    protected void foundNoTopElements()
            throws ValidationException, SAXException {
        
        super.foundNoTopElements();
        
        _handler.warning(
                new SAXParseException(
                    _bundle.getString("validator.exp.no_top_element"),
                    ModelUtils.getPublicId(_model),
                    null /*ModelUtils.getSystemId(_model)*/,
                    -1,
                    -1));
    }
    
    @Override
    protected void detectedRecursiveCondition(ValidationContext context,
            SchemaComponent schemaComponent)
            throws ValidationException, SAXException {
        
        super.detectedRecursiveCondition(context, schemaComponent);
        
        String msg = NbBundle.getMessage(CustomEncoderValidator.class,
                "validator.exp.recursive_condition",   //NOI18N
                context.getElementNCNamePath(schemaComponent));
        _handler.warning(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(schemaComponent.getModel()),
                    ModelUtils.getSystemId(schemaComponent.getModel()),
                    -1,
                    -1));
    }
    
    @Override
    protected void validate(ValidationContext context,
            GlobalElement globalElement)
            throws ValidationException, SAXException {
        
        super.validate(context, globalElement);
        
        validateElement(context, globalElement);
    }

    @Override
    protected void validate(ValidationContext context,
            LocalElement localElement)
            throws ValidationException, SAXException {
        
        super.validate(context, localElement);
        
        validateElement(context, localElement);
    }

    @Override
    protected void validate(ValidationContext context, Sequence sequence)
            throws ValidationException, SAXException {
        
        super.validate(context, sequence);
        
        validateGroup(context, sequence);
    }

    @Override
    protected void validate(ValidationContext context, All all)
            throws ValidationException, SAXException {
        
        super.validate(context, all);
        
        validateGroup(context, all);
    }

    @Override
    protected void validate(ValidationContext context, Choice choice)
            throws ValidationException, SAXException {
        
        super.validate(context, choice);
        
        validateGroup(context, choice);
    }
    
    @Override
    protected void validate(ValidationContext context, GroupReference groupRef)
            throws ValidationException, SAXException {
        
        super.validate(context, groupRef);
        
        validateGroup(context, groupRef);
    }

    @Override
    protected void validate(ValidationContext context, AnyElement anyElement)
            throws ValidationException, SAXException {
        
        super.validate(context, anyElement);
        
        String name = null;
        SchemaComponent sc = anyElement;
        for (int i = context.getPath().size() - 1; i >= 0; i--) {
            if (context.getPath().get(i)
                    instanceof NameableSchemaComponent) {
                name = ((NameableSchemaComponent)
                            context.getPath().get(i)).getName();
                sc = context.getPath().get(i);
                break;
            }
        }
        String msg;
        if (name != null) {
            msg = NbBundle.getMessage(CustomEncoderValidator.class,
                    "validator.exp.wildcard_not_supported", name); //NOI18N
        } else {
            msg = _bundle.getString("validator.exp.wildcard_not_supported_no_name");
        }
        _handler.error(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(sc.getModel()),
                    ModelUtils.getSystemId(sc.getModel()),
                    -1,
                    -1));
    }
    
    private void validateGroup(ValidationContext context,
            SchemaComponent group)
            throws ValidationException, SAXException {
        String name = null;
        SchemaComponent sc = null;
        for (int i = context.getPath().size() - 1; i >= 0; i--) {
            if (context.getPath().get(i)
                    instanceof NameableSchemaComponent) {
                name = ((NameableSchemaComponent)
                            context.getPath().get(i)).getName();
                sc = context.getPath().get(i);
                break;
            }
        }
        if (group instanceof LocalGroupDefinition) {
            List<SchemaComponent> children = 
                    group.getChildren(SchemaComponent.class);
            if (children == null || children.isEmpty()) {
                String msg;
                if (name != null) {
                    msg = NbBundle.getMessage(CustomEncoderValidator.class,
                            "validator.exp.empty_grp_not_supported", name); //NOI18N
                } else {
                    msg = _bundle.getString("validator.exp.empty_grp_not_supported_no_nm");
                }
                if (sc == null) {
                    sc = group;
                }
                _handler.error(
                        new SAXParseException(
                            msg,
                            ModelUtils.getPublicId(sc.getModel()),
                            ModelUtils.getSystemId(sc.getModel()),
                            -1,
                            -1));
            }
        }
        if (context.getPath().size() == 0) {
            return;
        }
        sc = context.getPath().get(context.getPath().size() - 1);
        if ((sc instanceof LocalGroupDefinition)
                || (sc instanceof GroupReference)) {
            String msg;
            if (name != null) {
                msg = NbBundle.getMessage(CustomEncoderValidator.class,
                        "validator.exp.grp_in_grp_not_supported", name); //NOI18N
            } else {
                msg = _bundle.getString("validator.exp.grp_in_grp_not_supported_no_nm");
            }
            _handler.error(
                    new SAXParseException(
                        msg,
                        ModelUtils.getPublicId(sc.getModel()),
                        ModelUtils.getSystemId(sc.getModel()),
                        -1,
                        -1));
        }
    }
    
    private void validateElement(ValidationContext context, Element elem)
            throws ValidationException, SAXException {
        String msg;
        if ((elem instanceof TypeContainer)
                && SchemaUtility.isAnyType(getType((TypeContainer) elem))) {
            msg = NbBundle.getMessage(CustomEncoderValidator.class,
                    "validator.exp.anytype_not_supported",  //NOI18N
                    context.getElementNCNamePath(elem));
            _handler.error(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(elem.getModel()),
                    null /*ModelUtils.getSystemId(elem.getModel())*/,
                    -1,
                    -1));
        }
        if (elem.getAnnotation() == null) {
            msg = NbBundle.getMessage(CustomEncoderValidator.class,
                    "validator.exp.missing_encoding_info",  //NOI18N
                    context.getElementNCNamePath(elem));
            _handler.warning(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(elem.getModel()),
                    null /*ModelUtils.getSystemId(elem.getModel())*/,
                    -1,
                    -1));
            return;
        }
        List<SchemaComponent> path =
                new ArrayList<SchemaComponent>(context.getPath());
        path.add(elem);
        path.add(elem.getAnnotation());
        EncodingOption encodingInfo;
        try {
            encodingInfo =
                    EncodingOption.createFromAppInfo(path, false);
        } catch (InvalidAppInfoException ex) {
            _handler.error(
                    new SAXParseException(
                        ex.getMessage(),
                        ModelUtils.getPublicId(elem.getModel()),
                        null /*ModelUtils.getSystemId(elem.getModel())*/,
                        -1,
                        -1,
                        ex));
            return;
        }
        if (encodingInfo == null) {
            msg = NbBundle.getMessage(CustomEncoderValidator.class,
                    "validator.exp.missing_encoding_info",  //NOI18N
                    context.getElementNCNamePath(elem));
            _handler.warning(
                new SAXParseException(
                    msg,
                    ModelUtils.getPublicId(elem.getModel()),
                    null /*ModelUtils.getSystemId(elem.getModel())*/,
                    -1,
                    -1));
            return;
        }
        encodingInfo.validate(_handler);
    }
}
