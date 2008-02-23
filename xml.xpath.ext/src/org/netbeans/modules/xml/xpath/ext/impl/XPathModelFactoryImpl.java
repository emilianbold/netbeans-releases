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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.metadata.UnknownExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;

/**
 *
 * @author nk160297
 */
public class XPathModelFactoryImpl implements XPathModelFactory {

    private XPathModel mModel;
    
    public XPathModelFactoryImpl(XPathModel model) {
        mModel = model;
    }
    
    
        /**
         * Instantiates a new XPathStringLiteral object.
         * @param value the value
         * @return a new XPathStringLiteral object instance
         */
        public XPathStringLiteral newXPathStringLiteral(String value) {
            return new XPathStringLiteralImpl(mModel, value);
        }

        /**
         * Instantiates a new XPathVariableReference object of the type variable.
         * @param value the value
         * @return a new XPathVariableReference object instance
         */
        public XPathVariableReference newXPathVariableReference(QName vReference) {
            return new XPathVariableReferenceImpl(mModel, vReference);
        }

        /**
         * Instantiates a new XPathPredicateExpression object for given expression.
         * @param expression which is a predicate expression
         * @return a new XPathPredicateExpression object instance
         */
        public XPathPredicateExpression newXPathPredicateExpression(
                XPathExpression expression) {
            return new XPathPredicateExpressionImpl(mModel, expression);
        }

        /**
         * Instantiates a new XPathNumericLiteral object.
         * @param value the value
         * @return a new XPathNumericLiteral object instance
         */
        public XPathNumericLiteral newXPathNumericLiteral(Number value) {
            return new XPathNumericLiteralImpl(mModel, value);
        }

        /**
         * Instantiates a new XPathCoreFunction object.
         * @param function the function code
         * @return a new XPathCoreFunction object instance
         */
        public XPathCoreFunction newXPathCoreFunction(CoreFunctionType functionType) {
            return new XPathCoreFunctionImpl(mModel, functionType);
        }

        /**
         * Instantiates a new XPathExtension Function object.
         * @param name the function name
         * @return a new XPathExtensionFunction object instance
         */
        public XPathExtensionFunction newXPathExtensionFunction(QName name) {
            XPathExtensionFunction result = null;
            //
            if (name == null) {
                return null;
            }
            //
            if (name.equals(StubExtFunction.STUB_FUNC_NAME)) {
                return new StubExtFunction(mModel);
            }
            //
            // Populate the namespace URI if necessary
            name = XPathUtils.resolvePrefix(mModel.getNamespaceContext(), name);
            //
            ExtensionFunctionResolver extFuncResolver = 
                    mModel.getExtensionFunctionResolver();
            if (extFuncResolver != null) {
                result = extFuncResolver.newInstance(mModel, name);
                if (result == null) {
                    ExtFunctionMetadata metadata = 
                            extFuncResolver.getFunctionMetadata(name);
                    if (metadata != null) {
                         result = new XPathExtensionFunction(
                                 mModel, metadata);
                    }
                }
            }
            //
            if (result == null) {
                result = new UnknownExtensionFunction(mModel, name);
            }
            //
            return result;
        }

        /**
         * Instantiates a new XPathCoreOperation object.
         * @param code the operation code
         * @return a new XPathCoreOperatoin object instance
         */
        public XPathCoreOperation newXPathCoreOperation(CoreOperationType opType) {
            return new XPathCoreOperationImpl(mModel, opType);
        }

        /**
         * Instantiates a new XPathLocationPath object.
         * @param steps the steps
         * @return a new XPathLocationPath object instance
         */
        public XPathLocationPath newXPathLocationPath(LocationStep[] steps) {
            return new XPathLocationPathImpl(mModel, steps);
        }

        /**
         * Instantiates a new XPathExpressionPath object.
         * @param rootExpression root expression if any
         * @param steps the steps
         * @return a new XPathLocationPath object instance
         */
        public XPathExpressionPath newXPathExpressionPath(
                XPathExpression rootExpression, LocationStep[] steps) {
            return new XPathExpressionPathImpl(mModel, rootExpression, steps, false);
        }

        public LocationStep newLocationStep(XPathAxis axis, 
                StepNodeTest nodeTest, XPathPredicateExpression[] predicates) {
            return new LocationStepImpl(mModel, axis, nodeTest, predicates);
        }
}
