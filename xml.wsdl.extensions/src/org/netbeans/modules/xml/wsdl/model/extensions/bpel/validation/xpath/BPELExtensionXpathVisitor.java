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
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
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
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathException;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathLocationPath;
import org.netbeans.modules.xml.xpath.XPathModel;
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
    
    public void visit(PropertyAlias pa) {
        //
        // Check the property alias only if it has the Query
        //
        Query query = pa.getQuery();
        if (query == null) {
            return;
        }
        String queryText = query.getContent();
        if (queryText == null || queryText.length() == 0) {
            return;
        }
        //
        String qLanguage = query.getQueryLanguage();
        if (qLanguage == null || XPATH_EXPRESSION_TYPE.equals(qLanguage)) {
            AbstractXPathModelHelper helper= AbstractXPathModelHelper.getInstance();
            XPathModel model = helper.newXPathModel();
            XPathExpression xpath = null;
            try {
                xpath = model.parseExpression(queryText);
            } catch (XPathException e) {
                Throwable initialThrowable = getInitialCause(e);
                String msg = initialThrowable.getMessage();
                addNewResultItem(ResultType.ERROR, pa, msg, "");
            }
            assert xpath != null;
            if (!(xpath instanceof XPathLocationPath)) {
                // Error. Query has to be a Location Path expression
                String str = NbBundle.getMessage(BPELExtensionXpathValidator.class,
                        "LOCATION_PATH_REQUIRED");
                addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
                return;
            }
            //
            NamedComponentReference<GlobalElement> gElementRef = null;
            NamedComponentReference<GlobalType> gTypeRef = null;
            //
            NamedComponentReference<Message> messageRef = pa.getMessageType();
            if (messageRef != null) {
                Message message = messageRef.get();
                if (message == null) {
                    // Error. Can not resolve message type
                    String str = constructMessage("UNRESOLVED_MESSAGE_TYPE",
                            messageRef.getRefString()); // NOI18N
                    addNewResultItem(ResultType.ERROR, query, str, ""); // NOI18N
                    return;
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
                    return;
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
                return;
            }
            //
            SchemaComponent contextSchemaComponent = null;
            XPathLocationPath locationPath = (XPathLocationPath)xpath;
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
                return;
            }
            //
            PathValidationContext context =
                    new PathValidationContext(mValidator, this, pa, query);
            context.setSchemaContextComponent(contextSchemaComponent);
            context.setSchemaContextModel(contextSchemaComponent.getModel());
            //
            PathValidatorVisitor pathVVisitor = new PathValidatorVisitor(context);
            pathVVisitor.visit(locationPath);
        }
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
