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

package org.netbeans.modules.xml.xpath.ext.impl;

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.MultiCompSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SpecialSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.WildcardSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext.SchemaCompPair;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep.SsType;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathProblem;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

/**
 *
 * Be aware that many of visit() methods throws the StopResolutionException
 * which is a RuntimeException. It is done to escape visitor interface modification.
 *
 * @author Nikita Krjukov
 */
public class ReferenceResolutionVisitor extends XPathVisitorAdapter {

    /**
     * The schema context of the paretn XPath element.
     * It can be null, for examle, in case of the first step of
     * an absolute location path.
     */
    private XPathSchemaContext parentSchemaContext;
    private XPathModelImpl mModel;
    private ResourceCollector mResourceCollector;
    private int mStubCounter = 0;

    public ReferenceResolutionVisitor(XPathModelImpl model, XPathSchemaContext context) {
        mModel = model;
        parentSchemaContext = context;
        mResourceCollector = new ResourceCollector();
    }

    @Override
    public void visit(XPathLocationPath locationPath) {
        XPathSchemaContext lpInitialContext = parentSchemaContext;
        try {
            processLocationSteps(locationPath.getSteps(),
                    locationPath.getAbsolute());
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
        } finally {
            //
            // restore context
            parentSchemaContext = lpInitialContext;
        }
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
        XPathSchemaContext lpInitialContext = parentSchemaContext;
        try {
            XPathExpression rootExpr = expressionPath.getRootExpression();
            if (rootExpr != null) {
                rootExpr.accept(this);
            }
            //
            processLocationSteps(expressionPath.getSteps(), false);
            //
        } finally {
            //
            // restore context
            parentSchemaContext = lpInitialContext;
        }
    }

    @Override
    public void visit(XPathVariableReference vReference) {
        SchemaComponent varType = vReference.getType();

        if (varType == null) {
            throw new StopResolutionException(
                    "It didn't manage to resolve a type of the variable: " +
                    vReference); // NOI18N
        } else {
            XPathCastResolver castResolver = mModel.getXPathCastResolver();
            //
            XPathSchemaContext schemaContext = new VariableSchemaContext(vReference);
            if (castResolver != null) {
                XPathCast cast = castResolver.getCast(schemaContext);
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
        visitChildren(coreOperation);
        //
        // Warn the Union operator "|" isn't supported by the runtime
        XPathValidationContext validationCtxt = mModel.getValidationContext();
        if (validationCtxt != null &&
                coreOperation.getOperationType() == CoreOperationType.OP_UNION) {
            XPathExpression rootExpression = mModel.getRootExpression();
            validationCtxt.addResultItem(rootExpression,
                    ResultType.WARNING,
                    XPathProblem.RUNTIME_NOT_SUPPORT_OPERATION,
                    CoreOperationType.OP_UNION.getMetadata().getName());
        }
    }

    @Override
    public void visit(XPathCoreFunction coreFunction) {
        visitChildren(coreFunction);
    }

    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
        if (StubExtFunction.STUB_FUNC_NAME.equals(
                extensionFunction.getName())) {
            mStubCounter++;
            // The srub() function doesn't require following processing
            return;
        }
        //
        // Show the error if an unknown extension function is used.
        XPathValidationContext validationCtxt = mModel.getValidationContext();
        ExtensionFunctionResolver extFuncResolver =
                mModel.getExtensionFunctionResolver();
        if (validationCtxt != null) {
            mModel.checkExtFunction(extensionFunction);
            //
            if (extFuncResolver != null) {
                extFuncResolver.validateFunction(
                        extensionFunction, validationCtxt);
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
        if (schemaContext != null) {
            return schemaContext; // sContext already resolved
        }
        //
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
                            parentSchemaContext, mModel, SsType.ALL_ATTRIBUTES);
                    break;
                case CHILD:
                    schemaContext = new WildcardSchemaContext(
                            parentSchemaContext, mModel, SsType.ALL_ELEMENTS);
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
                    Set<SchemaCompPair> stepComponents =
                            mModel.resolveChildComponents(
                            parentSchemaContext, nodeQName,
                            isAttribute, isGlobal);
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
                                    parentSchemaContext, stepComponents,
                                    isAttribute);
                            break;
                        }
                    }
                    break;
                default:
                    // The usage of any axis except the attribute or child can result in
                    // loss of type context. It doesn't matter to check schema types any more.
                    XPathValidationContext validationCtxt =
                            mModel.getValidationContext();
                    if (validationCtxt != null) {
                        XPathExpression rootExpression =
                                mModel.getRootExpression();
                        validationCtxt.addResultItem(rootExpression,
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
                    XPathValidationContext validationCtxt =
                            mModel.getValidationContext();
                    schemaContext = parentSchemaContext.getParentContext();
                    if (schemaContext == null && validationCtxt != null) {
                        XPathExpression rootExpression =
                                mModel.getRootExpression();
                        validationCtxt.addResultItem(rootExpression,
                                ResultType.ERROR,
                                XPathProblem.ATTEMPT_GO_UPPER_THAN_ROOT);
                    }
                    break;
                case CHILD:
                    // it means that the location step is "node()"
                    //
                    schemaContext = new WildcardSchemaContext(
                            parentSchemaContext, mModel, SsType.NODE);
                    break;
                default:
                    assert false : "The axis " + axis +
                            " isn't supported in such context"; // NOI18N
                }
                break;
            case NODETYPE_COMMENT:
                schemaContext = new SpecialSchemaContext(parentSchemaContext,
                        SsType.COMMENT);
                break;
            case NODETYPE_PI:
                schemaContext = new SpecialSchemaContext(parentSchemaContext,
                        SsType.PROCESSING_INSTR);
                break;
            case NODETYPE_TEXT:
                schemaContext = new SpecialSchemaContext(parentSchemaContext,
                        SsType.TEXT);
                break;
            }
        }
        //
        // END of calculation of the schema context
        //
        if (schemaContext != null) {
            //
            // If there is a type cast for the current step, then replace
            // the context to a CastSchemaContext
            XPathCastResolver castResolver = mModel.getXPathCastResolver();
            if (castResolver != null) {
                XPathCast cast = castResolver.getCast(schemaContext);
                if (cast != null) {
                    CastSchemaContext castContext =
                            new CastSchemaContext(schemaContext, cast);
                    schemaContext = castContext;
                }
            }
            //
            XPathPredicateExpression[] predArr = locationStep.getPredicates();
            if (predArr != null && predArr.length > 0) {
                for (XPathPredicateExpression pred : predArr) {
                    //
                    // The predicated node has predicated and base context.
                    // Base context doesn't have information about predicate.
                    // In only refers to step component.
                    // The predicated context is a wrapper context, which
                    // contains the base context and reference to predicate expression.
                    // It is important to specify the base context to all the
                    // child expressions in the predicates!!!
                    pred.setSchemaContext(schemaContext);
                    parentSchemaContext = schemaContext;
                    pred.accept(this);
                }
                schemaContext = new PredicatedSchemaContext(schemaContext, predArr);
            }
            //
            locationStep.setSchemaContext(schemaContext);
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
                XPathValidationContext validationCtxt =
                        mModel.getValidationContext();
                if (validationCtxt != null) {
                    XPathExpression rootExpression =
                            mModel.getRootExpression();
                    validationCtxt.addResultItem(rootExpression,
                            ResultType.ERROR,
                            XPathProblem.MISSING_PARENT_SCHEMA_CONTEXT);
                }
                //
                throw new StopResolutionException(
                    "A parent schema context must be specified to resolve a relative location path."); // NOI18N
            }
        }
        //
        for (LocationStep step : steps) {
            XPathSchemaContext stepContext =
                    processLocationStep(step, isGlobalStep);
            if (!(stepContext instanceof SimpleSchemaContext)) {
                hasComplexContext = true;
            }
            isGlobalStep = false; // only first step can be global
            lastResolvedStep = step;
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
                mModel.setUsedComponents(prevStepContext, schemaComp);
            }
        }
        //
        // Post validation
        boolean isGlobalStep = isAbsolute;
        XPathValidationContext validationCtxt = mModel.getValidationContext();
        if (validationCtxt != null) {
            for (LocationStep step : stepArr) {
                boolean isLastStep = (step == lastResolvedStep);
                mModel.checkResolvedSchemaContext(
                        step, isGlobalStep, isLastStep, mResourceCollector);
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
