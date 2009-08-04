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
package org.netbeans.modules.xml.xpath.ext.impl;

import org.netbeans.modules.xml.xpath.ext.schema.resolver.WildcardSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.MultiCompSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.Parser;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.CoreFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.OperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathMetadataUtils;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.xpath.ext.spi.VariableResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathProblem;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathModelTracerVisitor;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder.AttributeHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder.ElementHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;

/**
 * Implementation of the XPathModel interface using Apache's JXPath.
 * JXPath does not validate function arguments, i.e., whether the
 * number of arguments is correct, so we may have to do that validation
 * ourselves or wait for a later version.
 * <p>
 * We also implement extensions to handle
 * <a href="http://www-106.ibm.com/developerworks/webservices/library/ws-bpel/#Expressions">
 * expressions in BPEL4WS</a>.
 *
 * @author Enrico Lelina
 * @version
 */
public class XPathModelImpl implements XPathModel {

    private XPathModelFactory mFactory;
    private XPathExpression mRootXPathExpression;

    private boolean isInExprResolveMode = false;
    private boolean isInResolveMode = false;
    private boolean isResolved = false;

    private XPathCastResolver myXPathCastResolver;
    private VariableResolver mVarResilver;
    private ExternalModelResolver mExternalModelResolver;
    private NamespaceContext mNamespaceContext;
    private XPathSchemaContext mRootSchemaContext;
    private XPathValidationContext mValidationContext;
    private ExtensionFunctionResolver mExtFuncResolver;

    // The static instance is used because it is stateless
    private static FilInStubVisitor sFilInStubVisitor = new FilInStubVisitor();

    /** Instantiates a new object. */
    public XPathModelImpl() {
        mFactory = new XPathModelFactoryImpl(this);
    }

    public XPathModelFactory getFactory() {
        return mFactory;
    }

    /**
     * Parses an XPath expression.
     * @param expression the XPath expression to parse
     * @return an instance of XPathExpression
     * @throws XPathException for any parsing errors
     */
    public XPathExpression parseExpression(String expression) throws XPathException {
//ENABLE = expression.startsWith("$ItineraryIn.iti"); // todo r
//out();
//out();
//out();
//out();
//out("---------------------------");
//out("EXPression: " + expression);
        myWasFunctionOrOperation = false; // vlv

        try {
            Compiler compiler = new XPathTreeCompiler(this);
            Object expr = Parser.parseExpression(expression, compiler);
            if (expr instanceof XPathExpression) {
                mRootXPathExpression = (XPathExpression)expr;
                return mRootXPathExpression;
            } else {
                String errTmpl = XPathProblem.BAD_XPATH_EXPRESSION.getMsgTemplate();
                String errText = MessageFormat.format(errTmpl, expression);
                if (mValidationContext != null) {
                    mValidationContext.addResultItem(null, ResultType.ERROR,
                            XPathProblem.BAD_XPATH_EXPRESSION, errText);
                }
                XPathException xpe = new XPathException(errText);
                throw xpe;
            }
        } catch (JXPathException jxe) {
            if (mValidationContext != null) {
                Throwable throwable = getInitialCause(jxe);
                if ( throwable == null ) {
                    String errTmpl = XPathProblem.BAD_XPATH_EXPRESSION.
                            getMsgTemplate();
                    String errText = MessageFormat.format(errTmpl, expression);
                    mValidationContext.addResultItem(null, ResultType.ERROR,
                            XPathProblem.BAD_XPATH_EXPRESSION, errText);
                } else {
                    String errTmpl = XPathProblem.XPATH_PARSING_EXCEPTION.
                            getMsgTemplate();
                    String errText = MessageFormat.format(
                            errTmpl, expression, throwable.getMessage());
                    mValidationContext.addResultItem(null, ResultType.ERROR,
                            XPathProblem.XPATH_PARSING_EXCEPTION, errText);
                }
            }
            //
            throw new XPathException(jxe);
        }
    }

    public XPathExpression getRootExpression() {
        return mRootXPathExpression;
    }

    public void setRootExpression(XPathExpression newExpr) {
        mRootXPathExpression = newExpr;
    }

    public void fillInStubs(XPathExpression expr) {
        if (expr == null) {
            expr = getRootExpression();
        }
        //
        if (expr != null) {
            expr.accept(sFilInStubVisitor);
        }
    }

    //==========================================================================
    // SPI methods support
    //==========================================================================

    public VariableResolver getVariableResolver() {
        return mVarResilver;
    }

    public void setVariableResolver(VariableResolver resolver) {
        mVarResilver = resolver;
    }

    public ExternalModelResolver getExternalModelResolver() {
        return mExternalModelResolver;
    }

    public void setExternalModelResolver(ExternalModelResolver resolver) {
        mExternalModelResolver = resolver;
    }

    public NamespaceContext getNamespaceContext() {
        return mNamespaceContext;
    }

    public void setNamespaceContext(NamespaceContext newContext) {
        mNamespaceContext = newContext;
    }

    public XPathSchemaContext getSchemaContext() {
        return mRootSchemaContext;
    }

    public void setSchemaContext(XPathSchemaContext context) {
        mRootSchemaContext = context;
    }

    public XPathValidationContext getValidationContext() {
        return mValidationContext;
    }

    public void setValidationContext(XPathValidationContext vContext) {
        mValidationContext = vContext;
    }

    public ExtensionFunctionResolver getExtensionFunctionResolver() {
        return mExtFuncResolver;
    }

    public void setExtensionFunctionResolver(ExtensionFunctionResolver extFuncResolver) {
        mExtFuncResolver = extFuncResolver;
    }

    public XPathCastResolver getXPathCastResolver() {
        return myXPathCastResolver;
    }

    public void setXPathCastResolver(XPathCastResolver xpathCastResolver) {
        myXPathCastResolver = xpathCastResolver;
    }

    //==========================================================================

    /**
     * Takes the namespace URI from the QName.
     * If it isn't specifies, then try resolve it by the namespace prefix.
     * If prefix isn't specified then consider it as the default namespace
     * (for global objects only!).
     *
     * Returns the namespace URI or null.
     * The null result value means that the namespace is unknown.
     *
     * Parameter isGlobal indicates if the object is considered as global or not.
     *
     * This method should be used only if a schema component for the required
     * object is not resolved yet! It the corresponding schema component is already
     * known, then the following approach has to be used:
     *   schemaComp.getModel().getEffectiveNamespace(schemaComp);
     */
    private String resolveNamespace(QName qName, boolean isGlobal)
            throws StopResolutionException {
        String nsUri = qName.getNamespaceURI();
        String prefix = null;
        //
        if (nsUri == null || nsUri.length() == 0) {
            prefix = qName.getPrefix();
            //
            if (!isGlobal && (prefix == null || prefix.length() == 0)) {
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
                return null;
            } else {
                //
                NamespaceContext nsContext = getNamespaceContext();
                if (nsContext == null) {
                    // namespace context isn't specified
                    throw new RuntimeException("A NamespaceContext has to be specified for the XPath model"); // NOI18N
                }
                //
                // If the prefix is empty string, then the default namespace has to be returned.
                // If the prefix is unknown, then the null has to be returned.
                nsUri = nsContext.getNamespaceURI(prefix);
            }
        }
        if (nsUri == null) {
            if (mValidationContext != null) {
                mValidationContext.addResultItem(getRootExpression(), ResultType.ERROR,
                        XPathProblem.UNKNOWN_NAMESPACE_PREFIX, prefix);
                //
                // Throw exception to interrupt following resolvement
                throw new StopResolutionException(
                        "Unknown namespace prefix: " + prefix); // NOI18N
            }
        }
        return nsUri;
    }

    /**
     * Looks a children elements or attributes in the current context by the QName.
     * Returns the set of found schema components or null.
     * The schema componets are wrapped to the SchemaCompPair object!
     * The context can be null!
     */
    private Set<SchemaCompPair> resolveChildComponents(
            XPathSchemaContext parentContext, QName qName,
            boolean isAttribute, boolean isGlobal) {
        assert qName != null;
        //
        String nsUri = resolveNamespace(qName, isGlobal);
        //
        String nodeName = qName.getLocalPart();
        HashSet<SchemaCompPair> foundCompPairSet = new HashSet<SchemaCompPair>();
        myLastSchemaComponent = null;
//ENABLE = qName.toString().equals("ReservationItems");
//out();
//out();
//out("RESOLVE: " + qName);
//out();
        //
        if (!isGlobal) {
            //
            // Look for a local schema objects
            Set<SchemaCompPair> parentCompPairs = parentContext.getSchemaCompPairs();
            switch (parentCompPairs.size()) {
            case 0:
                assert false : "Parent context must always contain parent schema component!"; // NOI18N
                break;
            case 1:
                //
                // Only one parent component is implied here
                SchemaCompPair parentCompPair = parentCompPairs.iterator().next();
                SchemaCompHolder parentCompHolder = parentCompPair.getCompHolder();

                if (parentCompHolder != null) {
//                    // vlv
//                    SchemaComponent castType = getCastType(parentContext);
//                    // XPathCast cast = getCast(parentContext);
////out();
////out("CAST TYPE: " + castType);
////out();
//                    if (castType != null) {
//                        parentComponent = castType;
//                    }
                    //
                    List<SchemaCompHolder> found = XPathUtils.getChildren(
                            this, parentContext,
                            parentCompHolder.getSchemaComponent(),
                            nodeName, nsUri, isAttribute);
                    //
                    if (found != null) {
                        for (SchemaCompHolder compH : found) {
                            SchemaCompPair newPair =
                                    new SchemaCompPair(compH, parentCompHolder);
                            addPair(foundCompPairSet, newPair);
                        }
                    }
                }
                break;
            default:
                //
                // Multiple parent components is implied here
                for (SchemaCompPair parentCPair : parentCompPairs) {
                    SchemaCompHolder parentCH = parentCPair.getCompHolder();
                    List<SchemaCompHolder> found = XPathUtils.getChildren(
                            this, parentContext,
                            parentCH.getSchemaComponent(),
                            nodeName, nsUri, isAttribute);
                    for (SchemaCompHolder sCompHolder : found) {
                        SchemaCompPair newPair =
                                new SchemaCompPair(sCompHolder, parentCH);
                        addPair(foundCompPairSet, newPair);
                    }
                }
            }
        } else {
            //
            // Look for a global schema objects
            ExternalModelResolver emr = getExternalModelResolver();
            if (emr != null) {
                Collection<SchemaModel> models = null;
                if (nsUri == null || nsUri.length() == 0) {
                    models = emr.getVisibleModels();
                } else {
                    models = emr.getModels(nsUri);
                }
                //
                if (models == null) {
                    throw new StopResolutionException(
                            "It didn't manage to find any external schema model " +
                            "to rosolve the \"" + nodeName + "\" step."); // NOI18N
                }
                //
                for (SchemaModel model : models) {
                    Schema schema = model.getSchema();
                    List<SchemaCompHolder> found = XPathUtils.getChildren(
                            this, parentContext, schema,
                            nodeName, nsUri, isAttribute);
                    for (SchemaCompHolder foundCompHolder : found) {
                        assert foundCompHolder instanceof ElementHolder ||
                                foundCompHolder instanceof AttributeHolder;
                        SchemaCompPair newPair =
                                new SchemaCompPair(foundCompHolder, null);
                        addPair(foundCompPairSet, newPair);
                    }
                }
            }
        }
        // Perform additional validations if a validation context is specified
        if (mValidationContext != null) {
            if (foundCompPairSet.isEmpty()) {
                String name = XPathUtils.qNameObjectToString(qName);

                if (isAttribute) {
                    if (nsUri == null || nsUri.length() == 0) {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.ERROR,
                                XPathProblem.UNKNOWN_ATTRIBUTE, name);
                    } else {
                          mValidationContext.addResultItem(getRootExpression(),
                                  ResultType.ERROR,
                                  XPathProblem.UNKNOWN_ATTRIBUTE_WITH_NAMESPACE,
                                  name, nsUri);
                    }
                }
                else {
                    if (nsUri == null || nsUri.length() == 0) {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.ERROR,
                                XPathProblem.UNKNOWN_ELEMENT, name);
                    }
                    else {
                        mValidationContext.addResultItem(getRootExpression(), ResultType.ERROR, XPathProblem.UNKNOWN_ELEMENT_WITH_NAMESPACE, name, nsUri);
                    }
                }
            }
        }
        return foundCompPairSet;
    }

    // vlv
    private void addPair(HashSet<SchemaCompPair> set, SchemaCompPair pair) {
        set.add(pair);
        myLastSchemaComponent = pair.getCompHolder().getSchemaComponent();
    }

//    // vlv
//    private SchemaComponent getCastType(XPathSchemaContext context) {
////out();
////out("GET cast type");
////out();
//      if (myXPathCastResolver == null) {
//        return null;
//      }
//      List<XPathCast> casts = myXPathCastResolver.getXPathCasts();
////out("  1");
//
//      if (casts == null) {
//        return null;
//      }
//      String path = context.toString();
////out("  2    : " + path + " " + context.getClass().getName());
//      for (XPathCast cast : casts) {
////out("    see: " + cast.getPath());
//        if (removePrefix(path).equals(removePrefix(cast.getPathText()))) {
//          return cast.getCastTo();
//        }
//      }
////out("  4");
//      return null;
//    }
//
//    // vlv
//    private String removePrefix(String value) {
//      if (value == null) {
//        return null;
//      }
//      StringBuffer buffer = new StringBuffer();
//      boolean skip = false;
//
//      for (int i=value.length()-1; i >= 0; i--) {
//        char c = value.charAt(i);
//
//        if (c == ':') {
//          skip = true;
//          continue;
//        }
//        if (skip && c != '/') {
//          continue;
//        }
//        if (skip && c == '/') {
//          skip = false;
//        }
//        buffer.insert(0, c);
//      }
//      return buffer.toString();
//    }

    public SchemaComponent getLastSchemaComponent() {
      if (myWasFunctionOrOperation) {
//System.out.println("!!! WAS myWasFunctionOrOperation");
        return null;
      }
//System.out.println("myLastSchemaComponent: " + myLastSchemaComponent);
      return myLastSchemaComponent;
    }

    private boolean myWasFunctionOrOperation;
    private SchemaComponent myLastSchemaComponent;

    /**
     * Performs postvalidation of the resolved LocationStep.
     * The attribute isGlobalStep is true only for the first step of an absolute path
     */
    @SuppressWarnings("fallthrough")
    private void checkResolvedSchemaContext(LocationStep locationStep,
            boolean isGlobalStep, boolean isLastInChain,
            ResourceCollector resourceCollector) {
        if (mValidationContext == null) {
            return;
        }
        XPathSchemaContext schemaContext = locationStep.getSchemaContext();
        if (schemaContext == null) {
            return;
        }
        //
        // Check prefixes (QUALIFIED vs UNQUALIFIED)
        Set<SchemaCompPair> compPairSet = null;
        if (isLastInChain) {
            compPairSet = schemaContext.getSchemaCompPairs();
        } else {
            compPairSet = schemaContext.getUsedSchemaCompPairs();
        }
        //
        if (compPairSet.isEmpty()) {
            return;
        }
        //
        // Resolve prefix and namespace Uri
        StepNodeTest stepNodeTest = locationStep.getNodeTest();
        XPathAxis axis = locationStep.getAxis();
        QName stepQName = null;
        boolean isAttribute = false;
        //
        // Indicates if the step represents a schema component
        // (not the wildcard, not an abbreviated step,
        // not a comment, text or processing instruction).
        boolean isSchemaCompStep = false;
        //
        if (stepNodeTest instanceof StepNodeNameTest) {
            StepNodeNameTest snnt = (StepNodeNameTest)stepNodeTest;
            if (!snnt.isWildcard()) {
                //
                isSchemaCompStep = true;
                // Only steps with element or attribute are checked here!
                switch (axis) {
                case ATTRIBUTE:
                    isAttribute = true;
                    // There isn't break here intentionally
                case CHILD:
                    stepQName = snnt.getNodeName();
                    break;
                default:
                    // The usage of any axis except the attribute or child can result in
                    // loss of type context. It doesn't matter to check schema types any more.
                    //
                    // TO DO: The list of supported AXIS can be extended later
                    //
                }
            }
        }
        //
        // Start validation
        if (stepQName != null) {
            if (compPairSet.size() == 1) {
                SchemaCompPair compPair = compPairSet.iterator().next();
                SchemaComponent sComp = compPair.getCompHolder().getSchemaComponent();
                //
                checkNsPrefixes(sComp, stepQName.getPrefix(),
                        resourceCollector, schemaContext);
            } else {
                // more then one schema components are found
                //
                String prefix = stepQName.getPrefix();
                if ((prefix == null || prefix.length() == 0) &&
                        isGlobalStep && isSchemaCompStep) {
                    // Specific case. The ERROR should be shown here instead of a warning!
                    mValidationContext.addResultItem(getRootExpression(),
                            ResultType.ERROR,
                            XPathProblem.AMBIGUOUS_ABSOLUTE_PATH_BEGINNING,
                            stepQName.getLocalPart());
                } else {
                    checkMultiNsPrefixes(compPairSet, stepQName, isAttribute,
                            schemaContext);
                }
            }
        }
    }

    /**
     * Checks prefix of a Location Step which has single allowed Schema component.
     * Preforms the following checks:
     *  -- if the prefix required or redundant.
     *  -- if the external schema is imported and prefix is defined.
     */
    private void checkNsPrefixes(SchemaComponent sComp,
            String nsPrefix, ResourceCollector resourceCollector,
            XPathSchemaContext schemaContext) {
        assert mValidationContext != null;
        //
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
        String nsUri = null;
        Set<String> nsSet = XPathSchemaContext.Utilities.
                getEffectiveNamespaces(sComp, schemaContext.getParentContext());
        if (nsSet.size() == 1) {
            nsUri = nsSet.iterator().next();
        } else {
            // Something is wrong
            return;
        }
        //
        if (Form.UNQUALIFIED.equals(form) &&
                nsPrefix != null && nsPrefix.length() != 0) {
            // Error. It should be without a prefix
            if (sComp instanceof LocalElement){
                String elementName = ((LocalElement)sComp).getName();

                // vlv
                mValidationContext.addResultItem(getRootExpression(),
                        ResultType.ERROR,
                        XPathProblem.ELEMENT_UNNECESSARY_PREFIX, elementName);
            }
            else if (sComp instanceof LocalAttribute){
                String attrName = ((LocalAttribute)sComp).getName();
                mValidationContext.addResultItem(getRootExpression(),
                        ResultType.WARNING,
                        XPathProblem.ATTRIBUTE_UNNECESSARY_PREFIX, attrName);
            }
        } else if (Form.QUALIFIED.equals(form)) {
            if (nsPrefix != null && nsPrefix.length() != 0) {
                //
                // If there is a prefix, then namespace is required!
                resourceCollector.addRequiredImport(nsUri);
            } else {
                // Error. It should be qualified.
                //
                // Check if the prefix is declared for the namespace URI
                String preferredPrefix = null;
                if (nsUri != null && nsUri.length() != 0) {
                    NamespaceContext nsContext = getNamespaceContext();
                    if (nsContext != null) {
                        preferredPrefix = nsContext.getPrefix(nsUri);
                        //
                        if (preferredPrefix == null) {
                            // Error. The required prefix isn't declared
                            resourceCollector.addRequiredPrefixForUri(nsUri);
                        }
                    }
                }
                //
                String name = ((Named)sComp).getName();
                if (isGlobal) {
                    if (sComp instanceof Element){
                        if (preferredPrefix == null) {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.GLOBAL_ELEMENT_PREFIX_REQUIRED, name);
                        } else {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.GLOBAL_ELEMENT_SPECIFIC_PREFIX_REQUIRED,
                                    name, preferredPrefix);
                        }
                    } else if (sComp instanceof Attribute){
                        if (preferredPrefix == null) {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.GLOBAL_ATTRIBUTE_PREFIX_REQUIRED, name);
                        } else {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.GLOBAL_ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED,
                                    name, preferredPrefix);
                        }
                    }
                } else {
                    if (sComp instanceof Element){
                        if (preferredPrefix == null) {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.ELEMENT_PREFIX_REQUIRED, name);
                        } else {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.ELEMENT_SPECIFIC_PREFIX_REQUIRED,
                                    name, preferredPrefix);
                        }
                    } else if (sComp instanceof Attribute){
                        if (preferredPrefix == null) {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.ATTRIBUTE_PREFIX_REQUIRED, name);
                        } else {
                            mValidationContext.addResultItem(getRootExpression(),
                                    ResultType.WARNING,
                                    XPathProblem.ATTRIBUTE_SPECIFIC_PREFIX_REQUIRED,
                                    name, preferredPrefix);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks prefix of a Location Step which has multiple allowed Schema components.
     * Preforms the following checks over the Location Step:
     * If there is not a prefix:
     *  -- if all elemens are qualified, then warning that some
     *     of possible prefixes is required.
     *  -- if some elements are qualified, then warning that
     *     maybe some of possible prefixes is required.
     *  -- if the external schema is imported and prefix is defined.
     * If there is a prefix:
     *  -- if all elemens are unqualified, then warning that
     *     prefix is redundant.
     */
    private void checkMultiNsPrefixes(Set<SchemaCompPair> compPairSet,
            QName qName, boolean isAttribute, XPathSchemaContext schemaContext) {
        //
        boolean hasGlobalComponents = false;
        boolean hasQualifiedComponents = false;
        boolean hasUnqualifiedComponents = false;
        HashSet<QName> usedNamespaces = new HashSet<QName>();
        //
        for (SchemaCompPair compPair : compPairSet) {
            SchemaComponent sComp = compPair.getCompHolder().getSchemaComponent();
            //
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
            if (isGlobal) {
                hasGlobalComponents = true;
            }
            if (form == Form.QUALIFIED) {
                hasQualifiedComponents = true;
            }
            if (form == Form.UNQUALIFIED) {
                hasUnqualifiedComponents = true;
            }
            //
            // Collect used namespaces
            Set<String> nsSet = XPathSchemaContext.Utilities.
                    getEffectiveNamespaces(sComp, schemaContext.getParentContext());
            for (String namespace : nsSet) {
                String prefix = mNamespaceContext.getPrefix(namespace);
                QName newQName = null;
                if (prefix == null || prefix.length() == 0) {
                    newQName = new QName(namespace, "aaa"); // NOI18N
                } else {
                    newQName = new QName(namespace, "aaa", prefix); // NOI18N
                }
                usedNamespaces.add(newQName);
            }
        }
        //
        String nsPrefix = qName.getPrefix();
        String name = qName.getLocalPart();
        if (nsPrefix == null || nsPrefix.length() == 0) {
            // Prefix isn't specified
            if (hasQualifiedComponents || hasGlobalComponents) {
                //
                // Prepare list of namespaces.
                String nsList = prepareNamespaceList(usedNamespaces);

                if (!hasUnqualifiedComponents) {
                    //
                    // If all elemens are qualified, then warning that some
                    // of possible prefixes is required.
                    if (isAttribute) {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.WARNING,
                                XPathProblem.ATTR_PREFIX_FROM_LIST_REQUIRED,
                                name, nsList);
                    } else {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.WARNING,
                                XPathProblem.ELEM_PREFIX_FROM_LIST_REQUIRED,
                                name, nsList);
                    }
                } else if (hasQualifiedComponents){
                    //
                    // If some elements are qualified, then warning that
                    //    maybe some of possible prefixes is required.
                    if (isAttribute) {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.WARNING,
                                XPathProblem.ATTR_MAYBE_PREFIX_FROM_LIST_REQUIRED,
                                name, nsList);
                    } else {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.WARNING,
                                XPathProblem.ELEM_MAYBE_PREFIX_FROM_LIST_REQUIRED,
                                name, nsList);
                    }
                }
            }
            // If the external schema is imported and prefix is defined.
        } else {
            // Prefix is specified
            if (!(hasQualifiedComponents || hasGlobalComponents)) {
                // If all elemens are unqualified, then warning that prefix is redundant.
                if (isAttribute) {
                    mValidationContext.addResultItem(getRootExpression(),
                            ResultType.WARNING,
                            XPathProblem.ATTR_PREFIX_REDUNDANT, name);
                } else {
                    mValidationContext.addResultItem(getRootExpression(),
                            ResultType.WARNING,
                            XPathProblem.ELEM_PREFIX_REDUNDANT, name);
                }
            }
        }
    }

    /**
     * If parent context has many variants of possible schema elements
     * then it can be narrowed according to set of collected children
     * schema components.
     */
    private void setUsedComponents(XPathSchemaContext parentContext,
            Set<SchemaCompPair> childCompPairs) {
        //
        if (parentContext == null || childCompPairs == null) {
            return;
        }
        //
        Set<SchemaCompHolder> usedByChildComp = new HashSet<SchemaCompHolder>();
        for (SchemaCompPair childPair : childCompPairs) {
            SchemaCompHolder childCompHolder = childPair.getParetnCompHolder();
            usedByChildComp.add(childCompHolder);
        }
        //
        if (!usedByChildComp.isEmpty()) {
            parentContext.setUsedSchemaCompH(usedByChildComp);
        }
        //
        // Go to the next context in the chain
        setUsedComponents(parentContext.getParentContext(),
                parentContext.getUsedSchemaCompPairs());
    }

    /**
     * Return boolean flag which indicates if the specified function is valid
     */
    public boolean checkExtFunction(XPathExtensionFunction extensionFunction) {
        if (mValidationContext == null ||
                mExtFuncResolver == null ||
                mNamespaceContext == null) {
            // These above component are required to perform the check
            return false;
        }
        //
        QName funcQName = extensionFunction.getName();
        //
        // Check prefix
        String nsUri = funcQName.getNamespaceURI();
        String nsPrefix = funcQName.getPrefix();
        String funcName = funcQName.getLocalPart();
        if (nsUri == null || nsUri.length() == 0) {
            //
            nsUri = mNamespaceContext.getNamespaceURI(nsPrefix);
            //
            if (nsUri == null) {
                //
                // The specified prefix is not found
                mValidationContext.addResultItem(mRootXPathExpression, ResultType.ERROR,
                        XPathProblem.UNKNOWN_NAMESPACE_PREFIX, nsPrefix);
                return false;
            }
        }
        //
        Collection<QName> supportedFunc = mExtFuncResolver.getSupportedExtFunctions();
        ArrayList<QName> sameNameOtherPrefix = new ArrayList<QName>();
        for (QName suppFuncName : supportedFunc) {
            if (suppFuncName.getLocalPart().equals(funcName)) {
                if (nsUri == null) {
                    sameNameOtherPrefix.add(suppFuncName);
                } else {
                    if (suppFuncName.getNamespaceURI().equals(nsUri)) {
                        //
                        // The corresponding function is found
                        return true;
                    } else {
                        sameNameOtherPrefix.add(suppFuncName);
                    }
                }
            }
        }
        //
        if (sameNameOtherPrefix.isEmpty()) {
            // The function with the required name isn't found

            // vlv
            // why stringToBytes, bytesToString, convert are not recognized?
            // TO DO FIX IT.
            //
            String name = XPathUtils.qNameObjectToString(funcQName);
            boolean hotFix =
              name.equals("stringToBytes") ||
              name.equals("bytesToString") ||
              name.equals("convert");

            mValidationContext.addResultItem(mRootXPathExpression,
                    hotFix ? ResultType.WARNING : ResultType.ERROR,
                    XPathProblem.UNKNOWN_EXTENSION_FUNCTION,
                    XPathUtils.qNameObjectToString(funcQName));
        } else {
            // The function with the required name is found, but in other namespace
            //
            // Prepare text with the list of alternative namespaces
            String nsList = prepareNamespaceList(sameNameOtherPrefix);
            //
            if (nsPrefix.length() == 0) {
                // vlv
                // why current-date, current-dateTime, current-time are not recognized?
                // TO DO FIX IT.
                //
                boolean hotFix =
                  funcName.equals("current-date") ||
                  funcName.equals("current-dateTime") ||
                  funcName.equals("current-time");

                mValidationContext.addResultItem(mRootXPathExpression,
                        hotFix ? ResultType.WARNING : ResultType.ERROR,
                        XPathProblem.PREFIX_REQUIRED_FOR_EXT_FUNCTION,
                        funcName, nsList);
            } else {
                mValidationContext.addResultItem(mRootXPathExpression, ResultType.ERROR,
                        XPathProblem.OTHER_PREFIX_REQUIRED_FOR_EXT_FUNCTION,
                        funcName, nsUri, nsList);
            }
        }
        //
        return false;
    }

    /**
     * Prepare text with the list of alternative namespaces
     */
    private String prepareNamespaceList(Collection<QName> namespaces) {
        boolean isFirst = true;
        StringBuilder sb = new StringBuilder();
        //
        for (QName otherFunc : namespaces) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("; ");
            }
            String altNsUri = otherFunc.getNamespaceURI();
            String altPrefix = mNamespaceContext.getPrefix(altNsUri);
            //
            sb.append("{").append(altNsUri).append("}");
            if (altPrefix != null && altPrefix.length() != 0) {
                sb.append(altPrefix);
            }
        }
        return sb.toString();
    }

    /**
     * Warn about missing resources (schema imports or prefixes)
     */
    private void warnResourceAbsence(ResourceCollector rColl) {
        if (mValidationContext == null || rColl == null) {
            return;
        }
        //
        for (String nsUri : rColl.getRequiredImports()) {
            if (mExternalModelResolver != null
                    && nsUri != null && nsUri.length() > 0 &&
                    !(mExternalModelResolver.isSchemaVisible(nsUri))) {
                // Error. The required schema isn't imported
//                mValidationContext.addResultItem(getRootExpression(),
//                        ResultType.WARNING,
//                        XPathProblem.MISSING_SCHEMA_IMPORT, nsUri);
            }
        }
        //
        for (String nsUri : rColl.getPrefixRequiredForUri()) {
            mValidationContext.addResultItem(getRootExpression(),
                    ResultType.WARNING,
                    XPathProblem.MISSING_NAMESPACE_PREFIX, nsUri);
        }
    }

    private void warnStub(int counter) {
        if (mValidationContext == null || counter == 0) {
            return;
        }
        //
        mValidationContext.addResultItem(getRootExpression(),
                ResultType.ERROR,
                XPathProblem.EXPR_CONTAINS_STUB, counter);
    }

    public synchronized void discardResolvedStatus() {
        isResolved = false;
    }

    public synchronized void resolveExtReferences(boolean again) {
        if (isResolved && !again) {
            return;
        }
        //
        if (mRootXPathExpression != null && !isInResolveMode) {
            isResolved = false;
            isInResolveMode = true;
            try {
                ReferenceResolutionVisitor visitor =
                        new ReferenceResolutionVisitor(getSchemaContext());
                mRootXPathExpression.accept(visitor);
                //
                warnResourceAbsence(visitor.getResourceCollector());
                warnStub(visitor.getStubCounter());
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            } finally {
                isInResolveMode = false;
                isResolved = true;
            }
        }
    }

    public synchronized void resolveExpressionExtReferences(XPathExpression expr) {
        //
        if (expr != null && !isInExprResolveMode) {
            isInExprResolveMode = true;
            try {
                ReferenceResolutionVisitor visitor =
                        new ReferenceResolutionVisitor(getSchemaContext());
                expr.accept(visitor);
                //
                warnResourceAbsence(visitor.getResourceCollector());
                warnStub(visitor.getStubCounter());
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            } finally {
                isInExprResolveMode = false;
            }
        }
    }

    /**
     * An utility method.
     */
    private static Throwable getInitialCause( Throwable throwable ) {
        if ( throwable == null ) {
            return null;
        }
        Throwable cause = throwable.getCause();
        if ( cause == null ) {
            return throwable;
        } else {
            return getInitialCause( cause );
        }
    }

    private class ReferenceResolutionVisitor extends XPathVisitorAdapter {

        /**
         * The schema context of the paretn XPath element.
         * It can be null, for examle, in case of the first step of
         * an absolute location path.
         */
        private XPathSchemaContext parentSchemaContext;
        private ResourceCollector mResourceCollector;
        private int mStubCounter = 0;

        public ReferenceResolutionVisitor(XPathSchemaContext context) {
            parentSchemaContext = context;
            mResourceCollector = new ResourceCollector();
        }

        @Override
        public void visit(XPathLocationPath locationPath) {
            XPathSchemaContext lpInitialContext = parentSchemaContext;
            try {
                processLocationSteps(locationPath.getSteps(),
                        locationPath.getAbsolute());
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            } finally {
                //
                // restor context
                parentSchemaContext = lpInitialContext;
            }
        }

        @Override
        public void visit(LocationStep locationStep) {
            XPathSchemaContext lpInitialContext = parentSchemaContext;
            try {
                boolean isGlobal = parentSchemaContext == null;
                processLocationStep(locationStep, isGlobal);
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            } finally {
                //
                // restore context
                parentSchemaContext = lpInitialContext;
            }
        }

        @Override
        public void visit(XPathExpressionPath expressionPath) {
//System.out.println("expressionPath: " + expressionPath);
            if (expressionPath != null) {
              String path = expressionPath.toString();

              if (path != null && path.endsWith("]")) {
                myWasFunctionOrOperation = true; // vlv
              }
            }
            XPathSchemaContext lpInitialContext = parentSchemaContext;
            try {
                XPathExpression rootExpr = expressionPath.getRootExpression();
                if (rootExpr != null) {
                    rootExpr.accept(this);
                }
                //
                processLocationSteps(expressionPath.getSteps(), false);
                //
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            } finally {
                //
                // restore context
                parentSchemaContext = lpInitialContext;
            }
        }

        @Override
        public void visit(XPathVariableReference vReference) {
            SchemaComponent varType = vReference.getType();
            myLastSchemaComponent = varType; // vlv

            if (varType == null) {
                throw new StopResolutionException(
                        "It didn't manage to resolve a type of the variable: " +
                        vReference); // NOI18N
            } else {
                XPathSchemaContext schemaContext = new VariableSchemaContext(vReference);
                if (myXPathCastResolver != null) {
                    XPathCast cast = myXPathCastResolver.getCast(schemaContext);
                    if (cast != null) {
                        CastSchemaContext castContext =
                                new CastSchemaContext(schemaContext, cast);
                        schemaContext = castContext;
                    }
                }
                vReference.setSchemaContext(schemaContext);
                //
                parentSchemaContext = schemaContext;
            }
        }

        @Override
        public void visit(XPathCoreOperation coreOperation) {
//System.out.println();
//System.out.println("VISIT coreOperation: " + coreOperation);
            myWasFunctionOrOperation = true; // vlv
            visitChildren(coreOperation);
            //
            // Warn the Union operator "|" isn't supported by the runtime
            if (mValidationContext != null &&
                    coreOperation.getOperationType() == CoreOperationType.OP_UNION) {
                mValidationContext.addResultItem(mRootXPathExpression,
                        ResultType.WARNING,
                        XPathProblem.RUNTIME_NOT_SUPPORT_OPERATION,
                        CoreOperationType.OP_UNION.getMetadata().getName());
            }
        }

        @Override
        public void visit(XPathCoreFunction coreFunction) {
//System.out.println();
//System.out.println("VISIT coreFunction: " + coreFunction);
            myWasFunctionOrOperation = true; // vlv
            visitChildren(coreFunction);
        }

        @Override
        public void visit(XPathExtensionFunction extensionFunction) {
//System.out.println();
//System.out.println("VISIT extensionFunction: " + extensionFunction);
            myWasFunctionOrOperation = true; // vlv

            if (StubExtFunction.STUB_FUNC_NAME.equals(
                    extensionFunction.getName())) {
                mStubCounter++;
                // The srub() function doesn't require following processing
                return;
            }
            //
            // Show the error if an unknown extension function is used.
            if (mValidationContext != null) {
                checkExtFunction(extensionFunction);
                //
                if (mExtFuncResolver != null) {
                    mExtFuncResolver.validateFunction(
                            extensionFunction, mValidationContext);
                }
            }
            //
            visitChildren(extensionFunction);
        }

        //======================================================================

        /**
         * Parameter isGlobal == true if the LocationStep is the first in the
         * absolute location path.
         */
        public XPathSchemaContext processLocationStep(
                LocationStep locationStep, boolean isGlobal) {
            //
            // Initialize the schema context
            XPathSchemaContext schemaContext = locationStep.getSchemaContext();
            if (schemaContext == null) {
                StepNodeTest stepNodeTest = locationStep.getNodeTest();
                XPathAxis axis = locationStep.getAxis();
                //
                if (stepNodeTest instanceof StepNodeNameTest) {
                    StepNodeNameTest snnt = (StepNodeNameTest)stepNodeTest;
                    //
                    if (snnt.isWildcard()) {
                        switch(axis) {
                        case ATTRIBUTE:
                            schemaContext = new WildcardSchemaContext(
                                    parentSchemaContext, XPathModelImpl.this,
                                    false, true);
                            break;
                        case CHILD:
                            schemaContext = new WildcardSchemaContext(
                                    parentSchemaContext, XPathModelImpl.this,
                                    true, false);
                            break;
                        default:
                            assert false : "Only the Attribute and Child axis is allowed with wildcard"; // NOI18N
                        }
                    } else {
                        switch (axis) {
                        case ATTRIBUTE:
                        case CHILD:
                            boolean isAttribute = (axis == XPathAxis.ATTRIBUTE);
                            //
                            QName nodeQName = snnt.getNodeName();
                            //
                            Set<SchemaCompPair> stepComponents = resolveChildComponents(
                                    parentSchemaContext, nodeQName, isAttribute, isGlobal);
                            if (stepComponents != null) {
                                switch(stepComponents.size()) {
                                case 0:
                                    break;
                                case 1:
                                    SchemaCompPair stepComp = stepComponents.iterator().next();
                                    schemaContext = new SimpleSchemaContext(
                                            parentSchemaContext, stepComp);
                                    break;
                                default:
                                    schemaContext = new MultiCompSchemaContext(
                                            parentSchemaContext, stepComponents);
                                    break;
                                }
                            }
                            break;
                        default:
                            // The usage of any axis except the attribute or child can result in
                            // loss of type context. It doesn't matter to check schema types any more.
                            if (mValidationContext != null) {
                                mValidationContext.addResultItem(getRootExpression(),
                                        ResultType.ERROR,
                                        XPathProblem.UNSUPPORTED_AXIS, axis);
                            }
                            throw new StopResolutionException(
                                    "Unsupported axis: " + axis); // NOI18N
                        }
                    }
                } else if (stepNodeTest instanceof StepNodeTypeTest) {
                    StepNodeTypeTest sntt = (StepNodeTypeTest)stepNodeTest;
                    switch (sntt.getNodeType()) {
                    case NODETYPE_NODE:
                        switch (axis) {
                        case SELF:
                            // it means that the location step is abbreviated step "."
                            //
                            // remain schema context intact
                            schemaContext = parentSchemaContext;
                            break;
                        case PARENT:
                            // it means that the location step is abbreviated step ".."
                            //
                            // move context ahad
                            schemaContext = parentSchemaContext.getParentContext();
                            if (schemaContext == null && mValidationContext != null) {
                                mValidationContext.addResultItem(getRootExpression(),
                                        ResultType.ERROR,
                                        XPathProblem.ATTEMPT_GO_UPPER_THAN_ROOT);
                            }
                            break;
                        case CHILD:
                            // it means that the location step is "node()"
                            //
                            schemaContext = new WildcardSchemaContext(
                                    parentSchemaContext, XPathModelImpl.this,
                                    true, true);
                            break;
                        default:
                            assert false : "The axis " + axis +
                                    " isn't supported in such context"; // NOI18N
                        }
                        break;
                    case NODETYPE_COMMENT:
                    case NODETYPE_PI:
                    case NODETYPE_TEXT:
                        // It doesn't matter to check schema types any more
                        //
                        // TO DO maybe it worth to set context to Schema text type
                        // because of the text and comment has such type.
                        //
                        throw new StopResolutionException(
                                "Unsupported node type test: " +
                                sntt.getNodeType()); // NOI18N
                    }
                }
                //
                // END of calculation of the schema context
            }
            //
            if (schemaContext != null) {
                //
                // If there is a type cast for the current step, then replace
                // the context to a CastSchemaContext
                if (myXPathCastResolver != null) {
                    XPathCast cast = myXPathCastResolver.getCast(schemaContext);
                    if (cast != null) {
                        CastSchemaContext castContext =
                                new CastSchemaContext(schemaContext, cast);
                        schemaContext = castContext;
                    }
                }
                //
                locationStep.setSchemaContext(schemaContext);
                //
                // If there is a schema context for current step, then go on trying
                // to resolve schema context for predicates.
                XPathPredicateExpression[] predArr = locationStep.getPredicates();
                if (predArr != null) {
                    for (XPathPredicateExpression pred : predArr) {
                        // reset current context
                        parentSchemaContext = schemaContext;
                        pred.setSchemaContext(schemaContext);
                        pred.accept(this);
                    }
                }
                //
                parentSchemaContext = schemaContext;
            } else {
                // It doesn't matter to check schema types any more
                throw new StopResolutionException(
                        "Didn't manage to resolve schema context for: " +
                        locationStep); // NOI18N
            }
            return schemaContext;
        }

        /**
         * The common part for process the XPathLocationPath and XPathExpressionPath.
         */
        private void processLocationSteps(LocationStep[] steps, boolean isAbsolute) {
            LocationStep lastResolvedStep = null;
            //
            // indicates if the location path has a complex context at any step
            boolean hasComplexContext = false;
            //
            boolean isGlobalStep = false;
            if (isAbsolute) {
                parentSchemaContext = null;
                isGlobalStep = true;
            } else {
                if (parentSchemaContext == null) {
                    if (mValidationContext != null) {
                        mValidationContext.addResultItem(getRootExpression(),
                                ResultType.ERROR,
                                XPathProblem.MISSING_PARENT_SCHEMA_CONTEXT);
                    }
                    //
                    throw new StopResolutionException(
                        "A parent schema context must be specified to resolve a relative location path."); // NOI18N
                }
            }
            //
            try {
                for (LocationStep step : steps) {
                    XPathSchemaContext stepContext =
                            processLocationStep(step, isGlobalStep);
                    if (!(stepContext instanceof SimpleSchemaContext)) {
                        hasComplexContext = true;
                    }
                    isGlobalStep = false; // only first step can be global
                    lastResolvedStep = step;
                }
            } catch (StopResolutionException ex) {
                // Do nothing here
                // ex.printStackTrace();
            }
            //
            // Perform postpocessing of location path
            if (lastResolvedStep != null) {
                postProcessLocationPath(steps, lastResolvedStep,
                        hasComplexContext, isAbsolute);
            }
        }

        /**
         * Performs postprocessing of location path.
         * The hasComplexContext parameter is used for optimization.
         */
        private void postProcessLocationPath(LocationStep[] stepArr,
                LocationStep lastResolvedStep, boolean hasComplexContext,
                boolean isAbsolute) {
            //
            if (hasComplexContext) {
                // Only if location steps chain contains at list one step with a complex context
                // Narrow schema contexts of location steps
                XPathSchemaContext lastStepContext = lastResolvedStep.getSchemaContext();
                if (lastStepContext == null) {
                    return;
                }
                lastStepContext.setLastInChain(true);
                //
                XPathSchemaContext prevStepContext = lastStepContext.getParentContext();
                if (prevStepContext != null) {
                    Set<SchemaCompPair> schemaComp = lastStepContext.getSchemaCompPairs();
                    setUsedComponents(prevStepContext, schemaComp);
                }
            }
            //
            // Post validation
            boolean isGlobalStep = isAbsolute;
            if (mValidationContext != null) {
                for (LocationStep step : stepArr) {
                    boolean isLastStep = (step == lastResolvedStep);
                    checkResolvedSchemaContext(step, isGlobalStep, isLastStep,
                            mResourceCollector);
                    isGlobalStep = false; // only first step can be global
                    if (isLastStep) {
                        break;
                    }
                }
            }
        }

        public ResourceCollector getResourceCollector() {
            return mResourceCollector;
        }

        public int getStubCounter() {
            return mStubCounter;
        }
    }

    /**
     * This private exception is used only internally to interrupt Schema resulution.
     * It must never go out of this class.
     */
    private class StopResolutionException extends RuntimeException {

        public StopResolutionException() {
            super();
        }

        public StopResolutionException(String msg) {
            super(msg);
        }
    }

    /**
     * Collects requierd imports and prefixes
     */
    private class ResourceCollector {

        private HashSet<String> mRequiredNsImports = new HashSet<String>();
        private HashSet<String> mPrefixRequiredForUri = new HashSet<String>();

        public void addRequiredImport(String nsUri) {
            mRequiredNsImports.add(nsUri);
        }

        public void addRequiredPrefixForUri(String nsUri) {
            mPrefixRequiredForUri.add(nsUri);
        }

        public Set<String> getRequiredImports() {
            return mRequiredNsImports;
        }

        public Set<String> getPrefixRequiredForUri() {
            return mPrefixRequiredForUri;
        }
    }

    /**
     * Traverses an XPath model and adds stub() function in places where it
     * is required. The class is stateless!
     */
    private static class FilInStubVisitor extends XPathModelTracerVisitor {

        @Override
        public void visit(XPathCoreFunction coreFunction) {
            visitChildren(coreFunction);
            //
            CoreFunctionMetadata metadata = coreFunction.getFunctionType().getMetadata();
            checkChildren(coreFunction, metadata);
        }

        @Override
        public void visit(XPathCoreOperation coreOperation) {
            visitChildren(coreOperation);
            //
            OperationMetadata metadata = coreOperation.getOperationType().getMetadata();
            checkChildren(coreOperation, metadata);
        }

        @Override
        public void visit(XPathExtensionFunction extensionFunction) {
            visitChildren(extensionFunction);
            //
            ExtFunctionMetadata metadata = extensionFunction.getMetadata();
            checkChildren(extensionFunction, metadata);
        }

        private void checkChildren(XPathOperationOrFuntion func,
                GeneralFunctionMetadata metadata) {
            if (metadata == null) {
                return;
            }
            //
            List<AbstractArgument> argList = metadata.getArguments();
            if (argList == null || argList.size() == 0) {
                return;
            }
            //
            List<ArgumentDescriptor> argDescrList =
                    XPathMetadataUtils.getArgDescriptorsList(argList, true);
            if (argDescrList == null || argDescrList.size() == 0) {
                return;
            }
            //
            int childIndex = -1;
            XPathModelFactory factory = null;
            //
            for (ArgumentDescriptor argDescr : argDescrList) {
                int minOccurs = argDescr.getMinOccurs();
                for (int index = 0; index < minOccurs; index ++) {
                    //
                    childIndex++;
                    //
                    XPathExpression argumentExpr = null;
                    if (childIndex < func.getChildCount()) {
                        argumentExpr = func.getChild(childIndex);
                    }
                    //
                    if (argumentExpr != null) {
                        continue;
                    }
                    //
                    if (argDescr.isMandatory()) {
                        if (factory == null) {
                            // lazy initialization
                            factory = func.getModel().getFactory();
                        }
                        //
                        XPathExtensionFunction newStub =
                                factory.newXPathExtensionFunction(
                                StubExtFunction.STUB_FUNC_NAME);
                        func.insertChild(childIndex, newStub);
                    }
                }
            }
        }
    }

    private boolean ENABLE;

    private void out() {
      if (ENABLE) {
        System.out.println();
      }
    }

    private void out(Object object) {
      if (ENABLE) {
        System.out.println("*** " + object);
      }
    }
}
