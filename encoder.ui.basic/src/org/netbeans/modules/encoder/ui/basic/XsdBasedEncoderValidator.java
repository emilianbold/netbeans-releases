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

package org.netbeans.modules.encoder.ui.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
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
import org.netbeans.modules.xml.schema.model.SequenceDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A generic validator for XSD based encoder implementation.  With this
 * validator as base, to do local validation, the derived validator only
 * needs to overwrite some of the schema component specific
 * <code>validate</code> methods and doesn't need to worry about how the
 * navigation is done.
 * 
 * @author Jun Xu
 */
public class XsdBasedEncoderValidator {

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/basic/Bundle"); //NOI18N
    private static boolean _debug
            = "true".equals(System.getProperty("encoder.validator.debug")); //NOI18N
    private static Logger _logger;
    static {
        if (_debug) {
            _logger = Logger.getLogger(XsdBasedEncoderValidator.class.getName());
        }
    }
    
    protected final String _style;
    protected final SchemaModel _model;
    protected final ErrorHandler _handler;
    
    private final Map<QName, GlobalType> _globalTypeCache =
        new HashMap<QName, GlobalType>();

    /**
     * Creates a new instance of XsdBasedEncoderValidator.
     * 
     * @param model the schema model
     * @param handler a SAX error handler
     */
    public XsdBasedEncoderValidator(String style, SchemaModel model,
            ErrorHandler handler) {
        if (style == null) {
            throw new NullPointerException(
                    _bundle.getString("xsd_based_validator.exp.null_style")); //NOI18N
        }
        if (model == null) {
            throw new NullPointerException(
                    _bundle.getString("xsd_based_validator.exp.null_schema_model")); //NOI18N
        }
        if (handler == null) {
            throw new NullPointerException(
                    _bundle.getString("xsd_based_validator.exp.null_error_handler")); //NOI18N
        }
        _style = style;
        _model = model;
        _handler = handler;
    }
    
    /**
     * Do the validation.
     */
    public void validate() throws ValidationException, SAXException {
        if (!ModelUtils.isEncodedWith(_model, _style)) {
            return;
        }
        Collection<GlobalElement> topElements =
                ModelUtils.getTopElements(_model);
        if (topElements.isEmpty()) {
            foundNoTopElements();
            return;
        }
        
        for (GlobalElement topElem : topElements) {
            ValidationContext context =
                    ValidationContext.createContext(
                            new ArrayList<SchemaComponent>(),
                            new HashSet<NameableSchemaComponent>());
            context.setTopElementNCName(topElem.getName());
            prepareContext(context);
            validateChild(context, topElem);
        }
    }
    
    protected void foundNoTopElements()
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info(_bundle.getString("xsd_based_validator.msg.no_top_elem")); //NOI18N
        }
    }
    
    protected void detectedRecursiveCondition(ValidationContext context,
            SchemaComponent schemaComponent)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("recursive condition: " + schemaComponent.toString());
        }
    }
    
    protected void validate(ValidationContext context,
            ElementReference elementRef)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate element reference: " + elementRef.toString()
                    + ", ref: " + (elementRef.getRef() == null ?
                        "<null>" : elementRef.getRef().getQName()));
        }
    }

    protected void validate(ValidationContext context,
            GlobalElement globalElement)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate global element: " + globalElement.getName());
        }
    }

    protected void validate(ValidationContext context,
            LocalElement localElement)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate local element: " + localElement.getName());
        }
    }

    protected void validate(ValidationContext context,
            GlobalComplexType globalComplexType)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate global complex type: "
                    + globalComplexType.getName());
        }
    }

    protected void validate(ValidationContext context,
            LocalComplexType localComplexType)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate local complex type: "
                    + localComplexType.toString());
        }
    }

    protected void validate(ValidationContext context,
            GlobalSimpleType globalSimpleType)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate global simple type: "
                    + globalSimpleType.getName());
        }
    }

    protected void validate(ValidationContext context,
            LocalSimpleType localSimpleType)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate local simple type: "
                    + localSimpleType.toString());
        }
    }

    protected void validate(ValidationContext context,
            Sequence sequence)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate sequence group: " + sequence.toString());
        }
    }

    protected void validate(ValidationContext context, All all)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate all group: " + all.toString());
        }
    }

    protected void validate(ValidationContext context, Choice choice)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate choice group: " + choice.toString());
        }
    }
    
    protected void validate(ValidationContext context, GroupReference groupRef)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate group reference: "
                    + (groupRef.getRef() == null ?
                        "<null>" : groupRef.getRef().getQName()));
        }
    }

    protected void validate(ValidationContext context,
            ComplexContent complexContent)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate complex content: " + complexContent.toString());
        }
    }
    
    protected void validate(ValidationContext context,
            SimpleContent simpleContent)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate simple content: " + simpleContent.toString());
        }
    }
    
    protected void validate(ValidationContext context, AnyElement anyElement)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate any element (wildcard): "
                    + anyElement.toString());
        }
    }
    
    protected void validate(ValidationContext context, GlobalGroup globalGroup)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate global group: " + globalGroup.toString());
        }
    }
    
    protected void validate(ValidationContext context,
            ComplexContentRestriction ccRes)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate complex content restriction: "
                    + ccRes.toString());
        }
    }
    
    protected void validate(ValidationContext context, ComplexExtension cxExt)
            throws ValidationException, SAXException {
        if (_debug) {
            _logger.info("level: " + context.getPath().size());
            _logger.info("validate complex content extension: "
                    + cxExt.toString());
        }
    }
    
    protected void prepareContext(ValidationContext context)
            throws ValidationException, SAXException  {
        
    }

    protected SchemaComponent getType(TypeContainer tContainer) {
        NamedComponentReference<? extends GlobalType> typeRef = tContainer.getType();
        if (typeRef != null) {
            GlobalType globalType = _globalTypeCache.get(typeRef.getQName());
            if (globalType != null) {
                return globalType;
            }
            globalType = typeRef.get();
            if (globalType != null) {
                _globalTypeCache.put(typeRef.getQName(), globalType);
                return globalType;
            }
        }
        return tContainer.getInlineType();
    }
    
    private void validateChild(ValidationContext context,
            SchemaComponent schemaComponent)
            throws ValidationException, SAXException {
        if (context.getNameables().contains(schemaComponent)) {
            detectedRecursiveCondition(context, schemaComponent);
            return;
        }
        ValidationContext newContext = context.clone();
        newContext.getPath().add(schemaComponent);
        if (schemaComponent instanceof NameableSchemaComponent) {
            newContext.getNameables().add(
                    (NameableSchemaComponent) schemaComponent);
        }
        prepareContext(newContext);
        if (schemaComponent instanceof Element) {
            if (schemaComponent instanceof ElementReference) {
                validate(context, ((ElementReference) schemaComponent));
            } else if (schemaComponent instanceof GlobalElement) {
                validate(context, ((GlobalElement) schemaComponent));
            } else if (schemaComponent instanceof LocalElement) {
                validate(context, ((LocalElement) schemaComponent));
            }
            if (schemaComponent instanceof ElementReference) {
                validateChild(newContext,
                        ((ElementReference) schemaComponent).getRef().get());
            } else if (schemaComponent instanceof TypeContainer) {
                validateChild(newContext, getType((TypeContainer) schemaComponent));
            }
        } else if (schemaComponent instanceof ComplexType) {
            if (schemaComponent instanceof GlobalComplexType) {
                validate(context, (GlobalComplexType) schemaComponent);
            } else if (schemaComponent instanceof LocalComplexType) {
                validate(context, (LocalComplexType) schemaComponent);
            }
            ComplexTypeDefinition definition =
                    ((ComplexType) schemaComponent).getDefinition();
            if (definition != null) {
                validateChild(newContext, definition);
            }
        } else if (schemaComponent instanceof SimpleType) {
            if (schemaComponent instanceof GlobalSimpleType) {
                validate(context, (GlobalSimpleType) schemaComponent);
            } else if (schemaComponent instanceof LocalSimpleType) {
                validate(context, (LocalSimpleType) schemaComponent);
            }
        } else if (schemaComponent instanceof Sequence) {
            validate(context, (Sequence) schemaComponent);
            List<SequenceDefinition> content =
                    ((Sequence) schemaComponent).getContent();
            for (SequenceDefinition se : content) {
                validateChild(newContext, se);
            }
        } else if (schemaComponent instanceof All) {
            validate(context, (All) schemaComponent);
            Collection<ElementReference> elemRefs =
                    ((All) schemaComponent).getElementReferences();
            for (ElementReference ef : elemRefs) {
                validateChild(newContext, ef);
            }
            Collection<LocalElement> elems =
                    ((All) schemaComponent).getElements();
            for (LocalElement le : elems) {
                validateChild(newContext, le);
            }
        } else if (schemaComponent instanceof Choice) {
            validate(context, (Choice) schemaComponent);
            Collection<ElementReference> elemRefs =
                    ((Choice) schemaComponent).getElementReferences();
            for (ElementReference ef : elemRefs) {
                validateChild(newContext, ef);
            }
            Collection<LocalElement> elems =
                    ((Choice) schemaComponent).getLocalElements();
            for (LocalElement le : elems) {
                validateChild(newContext, le);
            }
            Collection<AnyElement> anyElems =
                    ((Choice) schemaComponent).getAnys();
            for (AnyElement ae : anyElems) {
                validateChild(newContext, ae);
            }
            Collection<Choice> choices =
                    ((Choice) schemaComponent).getChoices();
            for (Choice ch : choices) {
                validateChild(newContext, ch);
            }
            Collection<GroupReference> groupRefs =
                    ((Choice) schemaComponent).getGroupReferences();
            for (GroupReference grpRef : groupRefs) {
                validateChild(newContext, grpRef);
            }
            Collection<Sequence> sequences =
                    ((Choice) schemaComponent).getSequences();
            for (Sequence sq : sequences) {
                validateChild(newContext, sq);
            }
        } else if (schemaComponent instanceof GroupReference) {
            validate(context, (GroupReference) schemaComponent);
            GlobalGroup globalGroup =
                    ((GroupReference) schemaComponent).getRef().get();
            if (globalGroup != null) {
                validateChild(newContext, globalGroup);
            }
        } else if (schemaComponent instanceof ComplexContent) {
            validate(context, (ComplexContent) schemaComponent);
            ComplexContentDefinition contentDef = 
                    ((ComplexContent) schemaComponent).getLocalDefinition();
            if (contentDef != null) {
                validateChild(newContext, contentDef);
            }
        } else if (schemaComponent instanceof SimpleContent) {
            validate(context, (SimpleContent) schemaComponent);
        } else if (schemaComponent instanceof AnyElement) {
            validate(context, (AnyElement) schemaComponent);
        } else if (schemaComponent instanceof GlobalGroup) {
            validate(context, (GlobalGroup) schemaComponent);
            LocalGroupDefinition lgDef =
                    ((GlobalGroup) schemaComponent).getDefinition();
            if (lgDef != null) {
                validateChild(newContext, lgDef);
            }
        } else if (schemaComponent instanceof ComplexContentRestriction) {
            validate(context, (ComplexContentRestriction) schemaComponent);
            GlobalComplexType gct =
                    ((ComplexContentRestriction) schemaComponent).getBase().get();
            if (gct != null) {
                validateChild(newContext, gct);
            }
            ComplexTypeDefinition ctDef =
                    ((ComplexContentRestriction) schemaComponent).getDefinition();
            if (ctDef != null) {
                validateChild(newContext, ctDef);
            }
        } else if (schemaComponent instanceof ComplexExtension) {
            validate(context, (ComplexExtension) schemaComponent);
            GlobalType gt =
                    ((ComplexExtension) schemaComponent).getBase().get();
            if (gt != null) {
                validateChild(newContext, gt);
            }
            ComplexExtensionDefinition ceDef =
                ((ComplexExtension) schemaComponent).getLocalDefinition();
            if (ceDef != null) {
                validateChild(newContext, ceDef);
            }
        }
    }

    /**
     * The instance of this class can be used to record validation infoset.
     * It is half done right now and isn't used anywhere.
     */
    private static class ValidationInfoSet {
        
        private final SchemaComponent _schemaComponent;
        private final List<SchemaComponent> _path;
        private Set<SAXParseException> _validationErrors;
        
        public ValidationInfoSet(List<SchemaComponent> parentPath,
                SchemaComponent schemaComponent) {
            if (schemaComponent == null) {
                throw new NullPointerException(
                        _bundle.getString("xsd_based_validator.exp.null_schemaComponent")); //NOI18N
            }
            if (parentPath == null || parentPath.isEmpty()) {
                _path = new ArrayList<SchemaComponent>();
            } else {
                _path = new ArrayList<SchemaComponent>(parentPath);
            }
            _path.add(schemaComponent);
            _schemaComponent = schemaComponent;
        }
        
        public SchemaComponent getSchemaComponent() {
            return _schemaComponent;
        }
        
        public SchemaComponent[] getPathInArray() {
            SchemaComponent[] pathArray = new SchemaComponent[_path.size()];
            _path.toArray(pathArray);
            return pathArray;
        }
        
        public boolean isValid() {
            return _validationErrors == null || _validationErrors.isEmpty();
        }
        
        public Collection<SAXParseException> getValidationErrors() {
            if (_validationErrors == null) {
                return Collections.emptySet();
            }
            return Collections.unmodifiableSet(_validationErrors);
        }
    }
}
