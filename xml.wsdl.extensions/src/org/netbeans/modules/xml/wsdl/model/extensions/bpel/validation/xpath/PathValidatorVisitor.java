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
package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.text.MessageFormat;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.StepNodeTest;
import org.netbeans.modules.xml.xpath.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;
import org.openide.util.NbBundle;

/**
 * This visitor is intended to validate semantics of single XPath.
 * It can check reference integrity of variables, parts and types.
 *
 * @author nk160297
 */
public class PathValidatorVisitor extends AbstractXPathVisitor {
    
    private XPathExpression myInitialExpression = null;
    
    private PathValidationContext myContext;
    
    private transient SchemaComponent parentComponent;
    private transient boolean stopPathValidation = false;
    private transient boolean lookForGlobalObject = false;
    
    public PathValidatorVisitor(PathValidationContext context) {
        myContext = context;
    }
    
    public PathValidationContext getContext() {
        return myContext;
    }
    
    //========================================================
    // Do standard processing
    
    public void visit(XPathCoreFunction coreFunction) {
        visitChildren(coreFunction);
    }
    
    public void visit(XPathCoreOperation coreOperation) {
        visitChildren(coreOperation);
    }
    
    public void visit(XPathExtensionFunction extensionFunction) {
        visitChildren(extensionFunction);
    }
    
    //========================================================
    
    public void visit(XPathLocationPath locationPath) {
        if (myInitialExpression == null) {
            myInitialExpression = locationPath;
        } else {
            // Delegate processing of a new path expression to a new path validator
            PathValidationContext newContext = myContext.clone();
            newContext.setSchemaContextComponent(parentComponent);
            PathValidatorVisitor newPVVisitor = new PathValidatorVisitor(newContext);
            locationPath.accept(newPVVisitor);
            return;
        }
        //
        if (locationPath.getAbsolute()) {
            //
            // Process the first step of an absolute location path.
            //
            SchemaModel contextModel = myContext.getSchemaContextModel();
            SchemaComponent rootComp = myContext.getSchemaContextComponent();
            //
            if (rootComp instanceof GlobalType) {
                // Error. The location path must not be absolute if the global type is used.
                addResultItem(ResultType.ERROR, "ABSOLUTE_XPATH_WITH_TYPE"); // NOI18N
            }
            //
            assert rootComp.getModel() == contextModel;
            lookForGlobalObject = true;
            parentComponent = contextModel.getRootComponent();
        } else {
            parentComponent = myContext.getSchemaContextComponent();
        }
        //
        LocationStep[] steps = locationPath.getSteps();
        if ( steps != null ){
            for (LocationStep step : steps) {
                visit(step);
                if (stopPathValidation) {
                    break;
                }
            }
        }
        //
        if (stopPathValidation) {
            return;
        }
        //
        // The following check is intended specially for Property Aliases.
        // It check if the type of the last element of the Query is the same as
        // the type of the correlation property.
        GlobalType propType = null;
        //
        WSDLComponent comp = myContext.getWsdlContext();
        if (comp instanceof PropertyAlias) {
            PropertyAlias pa = (PropertyAlias)comp;
            propType = getPropertyType(pa);
        }
        //
        if (propType != null) {
            //
            // Here the parentComponent has to reference to the last element of the XPath
            GlobalType gType = getComponentType(parentComponent);

            if (gType == null) {
              return;
            }
            //
            // Check if the type of the last element of the XPath
            if (!propType.equals(gType)) {
                // Error. The type of the last XPath element differ from the type
                // of the correlaton property.
                addResultItem(ResultType.ERROR, "PROP_ALIAS_INCONSISTENT_TYPE",
                        gType.getName(), propType.getName()); // NOI18N
            }
        }
    }
    
    public void visit(XPathExpressionPath expressionPath) {
        addResultItem(ResultType.ERROR, "UNSUPPORTED_VARIABLE_EXPRESSION"); // NOI18N
        stopPathValidation = true;
        return;
    }
    
    public void visit(LocationStep locationStep) {
        assert parentComponent != null;
        //
        boolean isAttribute = locationStep.getAxis() == LocationStep.AXIS_ATTRIBUTE;
        if (!isAttribute && locationStep.getAxis() != LocationStep.AXIS_CHILD) {
            // The usage of any axis except the attribute or child can result in
            // loss of type context. It doesn't matter to check schema types any more.
            addResultItem(ResultType.WARNING,
                    "UNSUPPORTED_AXIS", locationStep.getString()); // NOI18N
            stopPathValidation = true;
            return;
        }
        //
        StepNodeTest nodeTest = locationStep.getNodeTest();
        assert nodeTest != null;
        if (nodeTest instanceof StepNodeNameTest) {
            // get the text of the step
            String nodeName = ((StepNodeNameTest)nodeTest).getNodeName();
            //
            // Extract namespace prefix
            String nsPrefix = null;
            int colonIndex = nodeName.indexOf(':');
            if (colonIndex != -1) {
                nsPrefix = nodeName.substring(0, colonIndex);
                nodeName = nodeName.substring(colonIndex + 1);
            }
            //
            // Obtain the namespace URI by the prefix
            // The absence of prefix means that the XPath step is unqualified.
            // The default namespace can't be used by XPath in BPEL!
            String nsUri = null;
            if (nsPrefix == null) {
                //
                // If the prefix isn't specified then the step component can
                // be considered as an unqualified schema object.
                // 
                // If the prefix isn't specified then the step component can
                // be considered as an unqualified schema object.
                // ATTENTION! The namaspace is indefinite in such case. 
                // It doesn't related to the namespace of the parent component 
                // because the child component can be defined in other schema 
                // with other target namespace! It can't be considered as 
                // default namespace as for global elements in such case. 
                // The child element has to be found among all children 
                // by name only. 
                if (parentComponent != null) {
                    nsUri = null; 
                }
            } else {
                WSDLComponent contentElement = myContext.getXpathContentElement();
                assert contentElement instanceof AbstractDocumentComponent;
                nsUri = ((AbstractDocumentComponent)contentElement).
                        lookupNamespaceURI(nsPrefix, true);
                //
                if (nsUri == null) {
                    addResultItem(ResultType.WARNING,
                            "UNKNOWN_NAMESPACE_PREFIX", nsPrefix); // NOI18N
                    stopPathValidation = true;
                    return;
                }
            }
            //
            SchemaComponent foundComponent = null;
            if (lookForGlobalObject) {
                SchemaModel contextModel = myContext.getSchemaContextModel();
                SchemaComponent rootComp = myContext.getSchemaContextComponent();
                //
                String name = null;
                String namespace = contextModel.getEffectiveNamespace(rootComp);
                //
                if (rootComp instanceof GlobalElement) {
                    name = ((GlobalElement)rootComp).getName();
                } else if (rootComp instanceof GlobalAttribute) {
                    name = ((GlobalAttribute)rootComp).getName();
                } else {
                    assert false : "The root component of an absolute " +
                            "location path has to be either GlobalElement " +
                            "or GlobalAttribute"; // NOI18N
                    stopPathValidation = true;
                    return;
                }
                //
                assert nsUri != null : "it can be null for local components only.";  // NOI18N
                if (nsUri.equals(namespace) && nodeName.equals(name)) {
                    foundComponent = rootComp;
                } else {
                    // Error. The XPath has to be started from another global object
                    String correctRootName =
                            (nsPrefix == null ? "" : nsPrefix + ":") + name;
                    if (isAttribute) {
                        addResultItem(ResultType.ERROR,
                                "WRONG_GLOBAL_ATTRIBUTE", correctRootName); // NOI18N
                    } else {
                        addResultItem(ResultType.ERROR,
                                "WRONG_GLOBAL_ELEMENT", correctRootName); // NOI18N
                    }
                    //
                    stopPathValidation = true;
                    return;
                }
                //
                // Look for local object next time.
                lookForGlobalObject = false;
            } else {
                FindChildSchemaVisitor visitor =
                        new FindChildSchemaVisitor(nodeName, nsUri, isAttribute);
                visitor.lookForSubcomponent(parentComponent);
                //
                if (visitor.isChildFound()) {
                    foundComponent = visitor.getFound();
                } else {
                    // Error. The child with the specified name isn't found
                    if (isAttribute) {
                        addResultItem(ResultType.ERROR,
                                "UNKNOWN_ATTRIBUTE", nodeName, nsUri); // NOI18N
                    } else {
                        addResultItem(ResultType.ERROR,
                                "UNKNOWN_ELEMENT", nodeName, nsUri); // NOI18N
                    }
                    //
                    // It doesn't matter to check schema types any more
                    stopPathValidation = true;
                    return;
                }
            }
            //
            assert foundComponent instanceof GlobalElement ||
                    foundComponent instanceof LocalElement ||
                    foundComponent instanceof Attribute;
            //
            checkNsPrefixes(foundComponent, nsPrefix, nsUri);
            //
            parentComponent = foundComponent;
        } else if (nodeTest instanceof StepNodeTypeTest) {
            // It doesn't matter to check schema types any more
            stopPathValidation = true;
            return;
        }
        //
        // Process nested predicates
        // IMPORTANT! This code has to be here because of it requires that 
        // the current step element has already calculated. 
        // The parentComponent variable points to it. 
        XPathPredicateExpression[] expressions = locationStep.getPredicates();
        if ( expressions!= null ){
            for (XPathPredicateExpression expression : expressions) {
                expression.accept( this );
            }
        }
    }
    
    //========================================================
    
    /**
     * Obtains the type of the schema component. 
     * It works only with components which can have a type. 
     */ 
    private GlobalType getComponentType(SchemaComponent comp) {
        NamedComponentReference<? extends GlobalType> gTypeRef = null;

        if (comp instanceof TypeContainer) {
            gTypeRef = ((TypeContainer)comp).getType();
        } else if (comp instanceof LocalAttribute) {
            gTypeRef = ((LocalAttribute)comp).getType();
        } else if (comp instanceof GlobalAttribute) {
            gTypeRef = ((GlobalAttribute)comp).getType();
        } else if (comp instanceof ElementReference) {
          return null;
        } else {
            // Error. Can not resolve type of the last location path element.
            addResultItem(ResultType.ERROR, "UNRESOLVED_XPATH_TAIL",
                    myInitialExpression.getExpressionString()); // NOI18N
            return null;
        }
        //
        if (gTypeRef == null) {
            // Error. A global type has to be specified for the last element (attribute)
            // of the Location path.
            String lastElementName = ((Named)comp).getName();
            addResultItem(ResultType.ERROR, "XPATH_TAIL_NOT_GLOBAL_TYPE",
                    lastElementName); // NOI18N
            return null;
        } else {
            GlobalType gType = gTypeRef.get();
            if (gType == null) {
                // Error. Can not resolve the global type
                addResultItem(ResultType.ERROR, "UNRESOLVED_GLOBAL_TYPE",
                        gTypeRef.getRefString()); // NOI18N
            }
            //
            return gType;
        }
    }
    
    private GlobalType getPropertyType(PropertyAlias pa) {
        NamedComponentReference<CorrelationProperty> cPropRef =
                pa.getPropertyName();
        if (cPropRef == null) {
            // Warning. The property has not specified yet.
            addResultItem(ResultType.WARNING, "CPROP_NOT_SPECIFIED"); // NOI18N
            return null;
        } else {
            CorrelationProperty cProp = cPropRef.get();
            if (cProp == null) {
                // Error. Can not resolve the Correlation Property
                addResultItem(ResultType.ERROR, "UNRESOLVED_CPROP",
                        cProp.getName()); // NOI18N
                return null;
            }
            //
            GlobalType result = null;
            //
            NamedComponentReference<GlobalType> propTypeRef = cProp.getType();
            if (propTypeRef != null) {
                result = propTypeRef.get();
            } else {
                NamedComponentReference<GlobalElement> propElementRef =
                        cProp.getElement();
                if (propElementRef != null) {
                    GlobalElement propElement = propElementRef.get();
                    if (propElement != null) {
                        NamedComponentReference<? extends GlobalType> typeRef =
                                propElement.getType();
                        if (typeRef != null) {
                            result = typeRef.get();
                        }
                    }
                }
            }
            //
            if (result == null) {
                // Error. Can not resolve the type of Correlation Property
                addResultItem(ResultType.ERROR, "UNRESOLVED_CPROP_TYPE",
                        cProp.getName(), propTypeRef.getRefString()); // NOI18N
            }
            return result;
        }
    }
    
    /**
     * Checks if the prefix required or redundant. 
     * Check if the prefix is correct.
     * Check if the external schema is imported and prefix is defined.
     */ 
    private void checkNsPrefixes(SchemaComponent sComp, String nsPrefix, String nsUri) {
        Form form = null;
        boolean isGlobal = false;
        if (sComp instanceof LocalElement){
            form = ((LocalElement) sComp).getFormEffective();
        } else if (sComp instanceof LocalAttribute){
            form = ((LocalAttribute) sComp).getFormEffective();
        } else {
            form = Form.QUALIFIED; // by default for global components
            isGlobal = true;
        }
        //
        if (Form.UNQUALIFIED.equals(form) && nsPrefix != null) {
            // Error. It should be without a prefix
            if (sComp instanceof LocalElement){
                String elementName = ((LocalElement)sComp).getName();
                addResultItem(ResultType.WARNING,
                        "ELEMENT_UNNECESSARY_PREFIX", elementName); // NOI18N
            } else if (sComp instanceof LocalAttribute){
                String attrName = ((LocalAttribute)sComp).getName();
                addResultItem(ResultType.WARNING,
                        "ATTRIBUTE_UNNECESSARY_PREFIX", attrName); // NOI18N
            }
        } else if (Form.QUALIFIED.equals(form) && nsPrefix == null) {
            // Error. It should be qualified.
            //
            // Check if the prefix is declared for the namespace URI
            String preferredPrefix = null;
            if (nsPrefix == null) {
                preferredPrefix = getPrefixByNsUri(nsUri);
                //
                if (preferredPrefix == null) {
                    // Error. The required prefix isn't declared
                    addResultItem(ResultType.WARNING,
                            "MISSING_NAMESPACE_PREFIX", nsUri); // NOI18N
                }
            }
            //
            String name = ((Named)sComp).getName();
            if (isGlobal) {
                if (sComp instanceof Element){
                    if (preferredPrefix == null) {
                        addResultItem(ResultType.WARNING,
                                "GLOBAL_ELEMENT_PREFIX_REQUIRED", name); // NOI18N
                    }
                } else if (sComp instanceof Attribute){
                    if (preferredPrefix == null) {
                        addResultItem(ResultType.WARNING,
                                "GLOBAL_ATTRIBUTE_PREFIX_REQUIRED", name); // NOI18N
                    } else {
                        addResultItem(ResultType.WARNING,
                                "GLOBAL_ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED",
                                name, preferredPrefix); // NOI18N
                    }
                }
            } else {
                if (sComp instanceof Element){
                    if (preferredPrefix == null) {
                        addResultItem(ResultType.WARNING,
                                "ELEMENT_PREFIX_REQUIRED", name); // NOI18N
                    } else {
                        addResultItem(ResultType.WARNING,
                                "ELEMENT_SPECIFIC_PREFIX_REQUIRED",
                                name, preferredPrefix); // NOI18N
                    }
                } else if (sComp instanceof Attribute){
                    if (preferredPrefix == null) {
                        addResultItem(ResultType.WARNING,
                                "ATTRIBUTE_PREFIX_REQUIRED", name); // NOI18N
                    } else {
                        addResultItem(ResultType.WARNING,
                                "ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED",
                                name, preferredPrefix); // NOI18N
                    }
                }
            }
        }
    }
    
    private void addResultItem(ResultType resultType, String bundleKey,
            Object... values){
        //
        String str = NbBundle.getMessage(BPELExtensionXpathValidator.class, bundleKey);
        if (values != null && values.length > 0) {
            str = MessageFormat.format(str, values);
        }
        //
        if (myInitialExpression != null) {
            str = str + " Expression: \"" + myInitialExpression + "\"";
        }
        //
        ResultItem resultItem = new ResultItem(
                myContext.getValidator(),
                resultType,
                myContext.getXpathContentElement(),
                str);
        myContext.getVVisitor().getResultItems().add(resultItem);
    }
    
    private String getNsUriByPrefix(String nsPrefix) {
        WSDLComponent xPathOwner = myContext.getXpathContentElement();
        //
        assert xPathOwner instanceof AbstractDocumentComponent;
        String nsUri = ((AbstractDocumentComponent)xPathOwner).
                lookupNamespaceURI(nsPrefix, true);
        //
        return nsUri;
    }
    
    private String getPrefixByNsUri(String nsUri) {
        WSDLComponent xPathOwner = myContext.getXpathContentElement();
        //
        assert xPathOwner instanceof AbstractDocumentComponent;
        String nsPrefix = ((AbstractDocumentComponent)xPathOwner).lookupPrefix(nsUri);
        //
        return nsPrefix;
    }
    
}
