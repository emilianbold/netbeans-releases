/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationVisitor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.metadata.UnknownExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class BPELExtensionXpathVisitor extends ValidationVisitor {
    
    public static final String XPATH_EXPRESSION_TYPE =
            "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"; // NOI18N
    
    private Validator mValidator;
    
    public BPELExtensionXpathVisitor(Validator validator) {
        this.mValidator = validator;
        init();
    }
    
    @Override
    public void visit(final PropertyAlias pa) {
        //
        // Check the property alias only if it has the Query
        //
        Query query = pa.getQuery();
        if (query == null) {
            return;
        }
        String queryText = query.getContent();
        if (queryText == null || queryText.length() == 0) {
            // Query is empty. Nothing to validate!
            return;
        }

        boolean hasNMPropertyAttr = pa.getAnyAttribute(new QName(
                "http://www.sun.com/wsbpel/2.0/process/executable/SUNExtension/NMProperty", // NOI18N
                "nmProperty")) != null; // NOI18N

        //
        //
        String qLanguage = query.getQueryLanguage();
        boolean isXPathExpr = (qLanguage == null ||
                XPATH_EXPRESSION_TYPE.equals(qLanguage));
        //
        // we can handle only xpath expressions.
        if (!isXPathExpr) {
            return;
        }
        //
        // Resolve context type for the query.
        final SchemaComponent contextComp = resolveContextSchemaComp(pa, query);
        if (contextComp == null) {
            // It doesn't make sense to continue without a schema context
            return;
        }
        //
        // Perform standard XPath validation here
        //
        XPathModelHelper helper = XPathModelHelper.getInstance();
        XPathModel model = helper.newXPathModel();
        // 
        // Initiate the XPath model
        //
        NamespaceContext nsContext = new WsdlNamespaceContext(pa);
        model.setNamespaceContext(nsContext);
        //
        PathValidationContext context = new PathValidationContext(
                model, mValidator, this, pa, query, nsContext);
        context.setSchemaContextComponent(contextComp);
        //
        model.setValidationContext(context);
        //
        // DON'T SPECIFY A VARIABLE OR EXT FUNCTION RESOLVER!
        //
        model.setExternalModelResolver(new ExternalModelResolver() {
            public Collection<SchemaModel> getModels(String schemaNamespaceUri) {
                WSDLModel wsdlModel = pa.getModel();
                List<Schema> schemaList = wsdlModel.findSchemas(schemaNamespaceUri);
                ArrayList<SchemaModel> result = 
                        new ArrayList<SchemaModel>(schemaList.size());
                for (Schema schema : schemaList) {
                    SchemaModel sModel = schema.getModel();
                    result.add(sModel);
                }
                return result;
            }

            public Collection<SchemaModel> getVisibleModels() {
                // TODO: waiting answer from Samaresh! 
                //
                // Returns the schema where the contextComp is declared for a while.
                // It means that in case when the root element isn't explicitly defined 
                // the variants will be looked only in the same model where the 
                // context component is declared.
                SchemaModel model = contextComp.getModel();
                return Collections.singleton(model);
            }

            public boolean isSchemaVisible(String schemaNamespaceUri) {
                WSDLModel wsdlModel = pa.getModel();
                List<Schema> schemaList = wsdlModel.findSchemas(schemaNamespaceUri);
                return (schemaList != null && schemaList.size() > 0);
            }
        });
        //
        XPathExpression xpath = null;
        try {
            xpath = model.parseExpression(queryText);
        } catch (XPathException e) {
            // Nothing to do here because of the validation context 
            // was specified before and it has to be populated 
            // with a set of problems.
        }
        //
        // Check if the expression is the Location Path.
        // Only Locatin Path is allowed as a content of Query!
        assert xpath != null;

        boolean
            isValidXPathFunction = (xpath instanceof XPathOperationOrFuntion) &&
                                   (! (xpath instanceof UnknownExtensionFunction));
        if (! ((xpath instanceof XPathLocationPath) || isValidXPathFunction)) {
            // Error. Query has to be a Location Path or XPath expression
            String str = NbBundle.getMessage(BPELExtensionXpathValidator.class,
                "LOCATION_PATH_OR_XPATH_EXPRESSION_REQUIRED" /*"LOCATION_PATH_REQUIRED"*/);
            if (! isValidXPathFunction) {
                str += " " + NbBundle.getMessage(BPELExtensionXpathValidator.class,
                    "UNKNOWN_XPATH_FUNCTION");
            }
            addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
            return;
        }
        //
        // Specifies the context schema component to the schema resolver be able 
        // to resolve a relative query. 
        SimpleSchemaContext schemaContext = new SimpleSchemaContext(contextComp);
        model.setSchemaContext(schemaContext);
        //
        // Common validation will be made here!
        if (!hasNMPropertyAttr && model.resolveExtReferences(true)) {
            //
            // Perform additional XPath validation here
            PathValidatorVisitor pathVVisitor = new PathValidatorVisitor(context);
            xpath.accept(pathVVisitor);
        }
    }
    
    /**
     * Resolve context type for the query.
     * It is defined by the Message Part.
     */
    private SchemaComponent resolveContextSchemaComp(
            PropertyAlias pa, Query query) {
        assert pa != null && query != null;
        //
        NamedComponentReference<GlobalElement> gElementRef = null;
        NamedComponentReference<GlobalType> gTypeRef = null;
        String queryText = query.getContent();
        //
        NamedComponentReference<Message> messageRef = pa.getMessageType();
        if (messageRef != null) {
            Message message = messageRef.get();
            if (message == null) {
                // Error. Can not resolve message type
                String str = constructMessage("UNRESOLVED_MESSAGE_TYPE",
                        messageRef.getRefString()); // NOI18N
                addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
                return null;
            }
            String partName = pa.getPart();
            Collection<Part> parts = message.getParts();
            Part part = null;
            for (Part aPart : parts) {
                if (aPart.getName().equals(partName)) {
                    part = aPart;
                }
            }
            //
            if (part == null) {
                // Error. Can not find a part with the specified name
                String str = constructMessage("UNKNOWN_MESSAGE_PART",
                        partName, message.getName()); // NOI18N
                addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
                return null;
            }
            //
            gElementRef = part.getElement();
            gTypeRef = part.getType();
        } else {
            gElementRef = pa.getElement();
            gTypeRef = pa.getType();
        }
        //
        if (gElementRef == null && gTypeRef == null) {
            // Error. Can not obtain the root type of the query
            String str = constructMessage("UNRESOLVED_QUERY_ROOT_TYPE",
                    queryText); // NOI18N
            addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
            return null;
        }
        //
        SchemaComponent contextSchemaComponent = null;
        if (gElementRef != null) {
            GlobalElement gElement = gElementRef.get();
            if (gElement != null) {
                contextSchemaComponent = gElement;
            }
        } else if (gTypeRef != null) {
            GlobalType gType = gTypeRef.get();
            if (gType != null) {
                contextSchemaComponent = gType;
            }
        }
        //
        if (contextSchemaComponent == null) {
            // Error. Can not obtain root type of the query
            String str = constructMessage("UNRESOLVED_QUERY_ROOT_TYPE",
                    queryText); // NOI18N
            addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
        }
        //
        return contextSchemaComponent;
    }
    
    /**
     * Fires to-do events to listeners.
     *
     * @param toDoEvent
     *            To-do event to fire.
     * @return <code>true</code> if more events can be accepted by the
     *         listener; <code>false</code> otherwise.
     */
    void addNewResultItem( Validator.ResultType type,
            Component component,
            String desc,
            String correction ) {
        ResultItem item = new Validator.ResultItem(mValidator,
                type,
                component,
                desc + correction);
        getResultItems().add(item);
    }
    
    public static String constructMessage(String bundleKey, Object... values) {
        String str = NbBundle.getMessage(BPELExtensionXpathValidator.class, bundleKey);
        if (values != null && values.length > 0) {
            str = MessageFormat.format(str, values);
        }
        return str;
    }
    
    private Throwable getInitialCause( Throwable throwable ) {
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
    
}
