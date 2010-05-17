/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.rest.wadl.validator.visitor;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.netbeans.modules.websvc.rest.wadl.model.*;
import org.netbeans.modules.websvc.rest.wadl.model.visitor.WadlVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;



/**
 * Visits the model nodes and validates them.
 *
 * @author  Ayub Khan
 */
public class WadlSemanticsVisitor implements WadlVisitor {
    
    private ValidateSupport mValidateSupport = null;
    /** Validate configuration singleton. */
    private static ValidateConfiguration mValConfig;
    
    /** Fault can not be thrown by one-way or notification type operation */
    public static final String VAL_FAULT_NOT_ALLOWED_IN_OPERATION = "VAL_FAULT_NOT_ALLOWED_IN_OPERATION";  // Not I18N
    
    /** Fix 'Fault can not be thrown by one-way or notification type operation'
     * by removing faults */
    public static final String FIX_FAULT_NOT_ALLOWED_IN_OPERATION = "FIX_FAULT_NOT_ALLOWED_IN_OPERATION";  // Not I18N
    
    /** Message not found for operation input */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT";
    
    /** Message not found for operation output */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT";
    
    /** Message not found for operation fault */
    public static final String VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT =
            "VAL_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT";
    
    /** Fix for message not found for operation input */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_INPUT";
    
    /** Fix for Message not found for operation output */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_OUTPUT";
    
    /** Fix for Message not found for operation fault */
    public static final String FIX_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT =
            "FIX_MESSAGE_NOT_FOUND_IN_OPERATION_FAULT";
    
    /** Schema in part not found */
    public static final String VAL_SCHEMA_DEFINED_NOT_FOUND = "VAL_SCHEMA_DEFINED_NOT_FOUND";
    
    /** Fix for Schema in part not found */
    public static final String FIX_SCHEMA_DEFINED_NOT_FOUND = "FIX_SCHEMA_DEFINED_NOT_FOUND";
    
    /** Schema is not defined in part */
    public static final String VAL_NO_SCHEMA_DEFINED = "VAL_NO_SCHEMA_DEFINED";
    
    /** Fix for Schema is not defined in part */
    public static final String FIX_NO_SCHEMA_DEFINED = "FIX_NO_SCHEMA_DEFINED";
    
    /** partnerLinkType portType does not exist in wadl file */
    public static final String VAL_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_Wadl = "VAL_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_Wadl";
    
    /** Fix for partnerLinkType portType does not exist in wadl file */
    public static final String FIX_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_Wadl = "FIX_NO_PARTNERLINKTYPE_PORTTYPE_DEFINED_IN_Wadl";
    
    /**Message has zero parts so it is valid as per wadl schema but we need a warning*/
    public static final String VAL_WARNING_WADL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED = "VAL_WARNING_WADL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED";
    
    /**Message has zero parts so it is valid as per wadl schema but we need a warning*/
    public static final String FIX_WARNING_WADL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED = "FIX_WARNING_WADL_MESSAGE_DOES_NOT_HAVE_ANY_PARTS_DEFINED";
    
    /** part does not have element or type attribute */
    public static final String VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "VAL_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part does not have element or type attribute */
    public static final String FIX_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "FIX_NO_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part has both element and type  attribute */
    public static final String VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "VAL_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    /** part has both element and type  attribute */
     public static final String FIX_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART = "FIX_BOTH_ELEMENT_OR_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART";
    
    
    /**part has element attribute but the referenced element object can not be located*/
    public static final String VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "VAL_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has element attribute but the referenced element object can not be located*/
    public static final String FIX_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "FIX_ELEMENT_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has type attribute but the referenced type object can not be located*/
    public static final String VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "VAL_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**part has element attribute but the referenced type object can not be located*/
    public static final String FIX_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID = "FIX_TYPE_ATTRIBUTE_DEFINED_IN_MESSAGE_PART_IS_NOT_VALID";
    
    /**Binding has wrong or missing PortType */
    public static final String VAL_MISSING_PORTTYPE_IN_BINDING = "VAL_MISSING_PORTTYPE_IN_BINDING";
    public static final String FIX_MISSING_PORTTYPE_IN_BINDING = "FIX_MISSING_PORTTYPE_IN_BINDING";
    
    /**Service Port has wrong or missing Binding */
    public static final String VAL_MISSING_BINDING_IN_SERVICE_PORT = "VAL_MISSING_BINDING_IN_SERVICE_PORT";
    public static final String FIX_MISSING_BINDING_IN_SERVICE_PORT = "FIX_MISSING_BINDING_IN_SERVICE_PORT";
    
    /**Import does not have imported document object */
    public static final String VAL_MISSING_IMPORTED_DOCUMENT = "VAL_MISSING_IMPORTED_DOCUMENT";
    public static final String FIX_MISSING_IMPORTED_DOCUMENT = "FIX_MISSING_IMPORTED_DOCUMENT";
    
    /** PortType operation input name should be unique across all operation inputs in a port type*/
    public static final String VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE = "VAL_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE";
    public static final String FIX_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE = "FIX_DUPLICATE_OPRATION_INPUT_NAME_IN_PORTTYPE";
    
    /** PortType operation output name should be unique across all operation outputs in a port type*/
    public static final String VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE = "VAL_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE";
    public static final String FIX_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE = "FIX_DUPLICATE_OPRATION_OUTPUT_NAME_IN_PORTTYPE";
    
    
    /** operation falut name should be unique across all operation faults*/
    public static final String VAL_DUPLICATE_OPRATION_FAULT_NAME = "VAL_DUPLICATE_OPRATION_FAULT_NAME";
    public static final String FIX_DUPLICATE_OPRATION_FAULT_NAME = "FIX_DUPLICATE_OPRATION_FAULT_NAME";
    
    /** binding operation name does not match name of portType operations*/
    public static final String VAL_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_EXIST_IN_PORT_TYPE";
    
    private static final String VAL_ERROR_WADL_DEFINITIONS_NO_TARGETNAMESPACE = "VAL_ERROR_WADL_DEFINITIONS_NO_TARGETNAMESPACE";
    
    private static final String FIX_ERROR_WADL_DEFINITIONS_NO_TARGETNAMESPACE = "FIX_ERROR_WADL_DEFINITIONS_NO_TARGETNAMESPACE";
    
    private static final String VAL_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "VAL_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String FIX_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "FIX_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String VAL_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "VAL_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    private static final String FIX_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST = "FIX_IMPORT_SCHEMA_TARGETNAMESPACE_DOES_NOT_EXIST";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_INPUT_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_IN_PORT_TYPE";
    
     public static final String VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_INPUT_NAME_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_OUTPUT_NAME_IN_PORT_TYPE";
    
    public static final String VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE = "VAL_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE";
    public static final String FIX_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE = "FIX_OPERATION_DOES_NOT_MATCH_FAULTS_IN_PORT_TYPE";
    
    public static final String VAL_MULTIPLE_TYPES_IN_DEFINITION = "VAL_MULTIPLE_TYPES_IN_DEFINITION";
    public static final String FIX_MULTIPLE_TYPES_IN_DEFINITION = "FIX_MULTIPLE_TYPES_IN_DEFINITION";
   
    public static final String VAL_PARMETER_ORDER_CHECK_PART_EXISTENCE = "VAL_PARMETER_ORDER_CHECK_PART_EXISTENCE";
    public static final String FIX_PARMETER_ORDER_CHECK_PART_EXISTENCE = "FIX_PARMETER_ORDER_CHECK_PART_EXISTENCE";
   
    public static final String VAL_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING = "VAL_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING";
    public static final String FIX_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING = "FIX_PARMETER_ORDER_CHECK_AT_MOST_ONE_OUTPUT_MESSAGE_PART_MISSING";
   
    public List<ResultItem> mResultItems;
    private Validation mValidation;
    private List<Model> mValidatedModels;
    private Validator mValidator;
    
    /** Creates a new instance of WadlSchemaVisitor */
    public WadlSemanticsVisitor(Validator validator, Validation validation, List<Model> validatedModels) {
        
        Properties defaults = new Properties();
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ATTRIB_REQUIRED, "true");
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ATTRIB_QNAME, "true");
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ATTRIB_NCNAME, "false");
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ATTRIB_BOOLEAN, "true");
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ELEM_MIN, "true");
        defaults.setProperty(ValidateConfiguration.WADL_SYNTAX_ELEM_REQUIRED, "true");
        
        synchronized (this.getClass()) {
            mValConfig = new ValidateConfiguration(defaults);
            mResultItems = new Vector<ResultItem>();
        }
        
        mValidator = validator;
        mValidation = validation;
        mValidatedModels = validatedModels;
        
         getValidateSupport().setValidator(mValidator);
         getValidateSupport().setResultItems(mResultItems);
    }
    
    public List<ResultItem> getResultItems() {
        return mResultItems;
    }
    
    /** Gets the validate visitor support.
     * @return  Visitor support.
     */
    public ValidateSupport getValidateSupport() {
        if (null == mValidateSupport) {
            mValidateSupport = new ValidateSupport(mValConfig);
        }
        return mValidateSupport;
    }
    
    public Validation getValidation() {
        return mValidation;
    }
    
    public void setValidation(Validation validation) {
        this.mValidation = validation;
    }
    
    private void visitChildren(WadlComponent w) {
        Collection coll = w.getChildren();
        if (coll != null) {
            Iterator iter = coll.iterator();
            while (iter.hasNext()) {
                WadlComponent component = (WadlComponent) iter.next();
                component.accept(this);
            }
        }
    }

    public void visit(Doc doc) {
    }

    public void visit(Grammars grammars) {
        visitChildren(grammars);
    }

    public void visit(Include include) {
        visitChildren(include);
    }

    public void visit(Link link) {
        visitChildren(link);
    }

    public void visit(Method method) {
        visitChildren(method);
    }

    public void visit(Option option) {
        visitChildren(option);
    }

    public void visit(Param param) {
        visitChildren(param);
    }

    public void visit(Representation rep) {
        visitChildren(rep);
    }

    public void visit(Request req) {
        visitChildren(req);
    }

    public void visit(Resource resource) {
        visitChildren(resource);
    }

    public void visit(Resources resources) {
        visitChildren(resources);
    }

    public void visit(Response response) {
        visitChildren(response);
    }

    public void visit(Application app) {
        visitChildren(app);
    }

    public void visit(Fault fault) {
        visitChildren(fault);
    }
    
    public void visit(ResourceType resourceType) {
        visitChildren(resourceType);
    }

    public void visit(ExtensibilityElement ee) {
        visitChildren(ee);
    }
}
