/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
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
package org.netbeans.modules.wsdlextensions.sap.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.util.Set;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.sap.SAPComponent;
import org.netbeans.modules.wsdlextensions.sap.SAPFmOperation;
import org.netbeans.modules.wsdlextensions.sap.SAPMessage;
import org.netbeans.modules.wsdlextensions.sap.SAPBinding;
import org.netbeans.modules.wsdlextensions.sap.SAPAddress;
import org.netbeans.modules.wsdlextensions.sap.SAPAddressClient;
import org.netbeans.modules.wsdlextensions.sap.SAPAddressServer;
import org.netbeans.modules.wsdlextensions.sap.SAPIDocOperation;

/**
 * semantic validation, check WSDL elements & attributes values and
 * any relationship between;
 *
 * @author Sun Microsystems
 */

public class SAPComponentValidator
        implements Validator, SAPComponent.Visitor {

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.sap.validator.Bundle");

    private Validation mValidation;

    private ValidationType mValidationType;

    private ValidationResult mValidationResult;
            //Iterator<String> it = data.iterator();
    private static Set<ResultItem> resultItemSet = Collections.emptySet();
    private static Set<Model> modelSet = Collections.emptySet();
    public static final ValidationResult EMPTY_RESULT =
      new ValidationResult(resultItemSet, modelSet);

    private static final String REQUIRED_STRING="Provide value for this required attribute";

    public SAPComponentValidator() {}

    /**
     * Returns name of this validation service.
     */
    public String getName() {
        return getClass().getName();
    }

    /**
     * Validates given model.
     *
     * @param model model to validate.
     * @param validation reference to the validation context.
     * @param validationType the type of validation to perform
     * @return ValidationResult.
     */
    public ValidationResult validate(Model model, Validation validation,
            ValidationType validationType) {
        mValidation = validation;
        mValidationType = validationType;

        HashSet<ResultItem> results = new HashSet<ResultItem>();
        HashSet<Model> models = new HashSet<Model>();
        models.add(model);
        mValidationResult = new ValidationResult(results, models);

        // Traverse the model
        if (model instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel)model;

            if (model.getState() == State.NOT_WELL_FORMED) {
                return EMPTY_RESULT;
            }

            Definitions defs = wsdlModel.getDefinitions();
            Iterator<Binding> wsdlBindings = defs.getBindings().iterator();

            // Parse binding section
            //    <wsdl:binding name="flightBinding" type="tns:Z_FlightWSD">
            //        <sap:binding transactionalMode="Non-Transactional"/>
            //        <wsdl:operation name="FlightGetDetail">
            //            <sap:fmoperation functionName="BAPI_FLIGHT_GETDETAIL"/>
            //            <wsdl:input name="FlightGetDetailRequest">
            //                <sap:message/>
            //            </wsdl:input>
            //            <wsdl:output name="FlightGetDetailResponse">
            //                <sap:message/>
            //            </wsdl:output>
            //        </wsdl:operation>
            //    </wsdl:binding>
            while (wsdlBindings.hasNext()) {
                Binding wsdlBinding = wsdlBindings.next();
                if (wsdlBinding.getType() == null || wsdlBinding.getType().get() == null) {
                    continue;
                }

                //Get sap:binding
                int numSAPBindings = wsdlBinding.getExtensibilityElements(SAPBinding.class).size();

                if (numSAPBindings == 0) {
                    continue;
                }

                if (numSAPBindings > 0 && numSAPBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            wsdlBinding,
                            mMessages.getString("SAPBindingValidation.ONLY_ONE_SAP_BINDING_ALLOWED")));
                    continue;
                }

                Iterator<BindingOperation> wsdlOperations = wsdlBinding.getBindingOperations().iterator();
                boolean foundSAPOp = false;
                //Only one wsdl:operation is allowed per wsdl:binding
                while (wsdlOperations.hasNext()) {
                    // Get sap:fmoperation and sap:idocoperation
                    BindingOperation wsdlOperation = wsdlOperations.next();
                    List<SAPFmOperation> sapFMOpsList = wsdlOperation.getExtensibilityElements(SAPFmOperation.class);
                    List<SAPIDocOperation> sapIDocOpsList = wsdlOperation.getExtensibilityElements(SAPIDocOperation.class);

                    //No sap:fmoperation or sap:idocoperation found
                    if (sapFMOpsList.size() <= 0 && sapIDocOpsList.size() <= 0) {
                        // there is sap:binding but no sap:operation
                        results.add(
                                new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                wsdlBinding,
                                mMessages.getString("SAPBindingValidation.MISSING_SAP_OPERATION")));
                        break;
                    }

                    //Both sap:fmoperation and sap:idocoperation found
                    if (sapFMOpsList.size() > 0 && sapIDocOpsList.size() > 0) {
                        results.add(
                                new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                wsdlBinding,
                                mMessages.getString("SAPBindingValidation.ONLY_ONE_SAPOPERATION_ALLOWED")));
                        break;
                    }

                    if (sapFMOpsList.size() > 0) {
                        Iterator<SAPFmOperation> sapOps = sapFMOpsList.iterator();
                        sapOps.next().accept(this);
                    } else {
                        Iterator<SAPIDocOperation> sapOps = sapIDocOpsList.iterator();
                        sapOps.next().accept(this);
                    }
                    foundSAPOp = true;

                    BindingInput wsdlInput = wsdlOperation.getBindingInput();
                    if (wsdlInput != null) {
                        int inputMessageCnt = 0;
                        //Get sap:message for input
                        Iterator<SAPMessage> sapMessages =
                                wsdlInput.getExtensibilityElements(SAPMessage.class).iterator();
                        while (sapMessages.hasNext()) {
                            inputMessageCnt++;
                            SAPMessage sapMessage = sapMessages.next();
                            sapMessage.accept(this);
                        }
                        if ( inputMessageCnt > 1 ) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    wsdlBinding,
                                    mMessages.getString("SAPBindingValidation.ATMOST_ONE_MESSAGE_IN_INPUT") + " " + inputMessageCnt));
                        }
                    }

                    BindingOutput wsdlOutput = wsdlOperation.getBindingOutput();
                    if (wsdlOutput != null) {
                        int outputMessageCnt = 0;
                        //Get sap:message for output
                        Iterator<SAPMessage> sapMessages =
                                wsdlOutput.getExtensibilityElements(SAPMessage.class).iterator();
                        while (sapMessages.hasNext()) {
                            outputMessageCnt++;
                            SAPMessage sapMessage = sapMessages.next();
                            sapMessage.accept(this);
                        }
                        if ( outputMessageCnt > 1 ) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    wsdlBinding,
                                    mMessages.getString("SAPBindingValidation.ATMOST_ONE_MESSAGE_IN_OUTPUT") + " " + outputMessageCnt));
                        }
                    }
                }
                // there is no sap:binding but there are sap:operation
                if ( numSAPBindings == 0 && foundSAPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            wsdlBinding,
                            mMessages.getString("SAPBindingValidation.SAP_OPERATION_WO_SAP_BINDING")));
                }
            }

            Iterator<Service> services = defs.getServices().iterator();
            while (services.hasNext()) {
                Iterator<Port> ports = services.next().getPorts().iterator();
                while (ports.hasNext()) {
                    Port port = ports.next();
                    if(port.getBinding() != null) {
                        Binding binding = port.getBinding().get();
                        if(binding != null) {
                            int numRelatedSAPBindings = binding.getExtensibilityElements(SAPBinding.class).size();
                            // Get sap:address
                            //<wsdl:port name="flightServicePort" binding="tns:flightBinding">
                            //     <sap:address applicationServerHostname="sap50uni" clientNumber="800" systemNumber="00" systemID="EUC" user="DEMO" password="DEMO" language="EN" enableABAPDebugWindow="No" isSAPSystemUnicode="Yes" gatewayHostname="sap50uni" gatewayService="sapgw00" routerString="routerstr">
                            //        <sap:clientparams useLoadBalancing="No" applicationServerGroup="appServGroup" messageServerHostname="msgServHostName"/>
                            //        <sap:serverparams programID="Provide value for this required attribute"/>
                            //    </sap:address>
                            //</wsdl:port>


                            Iterator<SAPAddress> sapAddresses = port.getExtensibilityElements(SAPAddress.class).iterator();
                            if((numRelatedSAPBindings > 0) && (!sapAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("SAPExtValidation.MISSING_SAP_ADDRESS")));
                            }

                            if(port.getExtensibilityElements(SAPAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("SAPExtValidation.ONLY_ONE_SAPADDRESS_ALLOWED")));
                            }
                            while (sapAddresses.hasNext()) {
                                sapAddresses.next().accept(this);
                            }
                        }
                    }
                }
            }
        }

        // Clear out our state
        mValidation = null;
        mValidationType = null;

        return mValidationResult;
    }

    public void visit(SAPAddress target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results = mValidationResult.getValidationResult();

        //check for applicationServerHostname
        String appServerHostName = target.getApplicationServer();
        if (appServerHostName == null || appServerHostName.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_APPSERVERHOST)));
        }

        //check for clientNumber
        String clientNumber = target.getClientNumber();
        if (clientNumber == null || clientNumber.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_CLIENTNUM)));
        }

        //check for systemNumber
        String systemNumber = target.getSystemNumber();
        if (systemNumber == null || systemNumber.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_SYSNUM)));
        }

        //check for systemID
        String systemID = target.getSystemId();
        if (systemID == null || systemID.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_SYSID)));
        }

        //check for user
        String user = target.getUsername();
        if (user == null || user.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_USER)));
        }

        //check for password
        String password = target.getPassword();
        if (password == null || password.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDR_PW)));
        }

        // get sap:clientparams extensibility element
        Iterator<SAPAddressClient> sapAddressesClient = target.getExtensibilityElements(SAPAddressClient.class).iterator();
        while (sapAddressesClient.hasNext()) {
            sapAddressesClient.next().accept(this);
        }

        // get sap:serverparams extensibility element
        Iterator<SAPAddressServer> sapAddressesServer = target.getExtensibilityElements(SAPAddressServer.class).iterator();

        while (sapAddressesServer.hasNext()) {
            sapAddressesServer.next().accept(this);
        }
/*
            <sap:address user="Provide value for this required attribute" password="Provide value for this required attribute" isSAPSystemUnicode="No">
                <sap:clientparams useLoadBalancing="No"/>
                <sap:serverparams programID="Provide value for this required attribute"/>
            </sap:address>
 */
    }

    public void visit(SAPAddressClient target) {
        // for sap clientparams tag - nothing to validate at this point
    }

    public void visit(SAPAddressServer target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results = mValidationResult.getValidationResult();

        //check for programID
        String programID = target.getProgramID();
        if (programID == null || programID.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPADDRSERVER_PROGID)));
        }

    }

    public void visit(SAPBinding target) {
        // for sap binding tag - nothing to validate at this point
    }

    public void visit(SAPFmOperation target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results = mValidationResult.getValidationResult();

        //check for functionName
        String functionName = target.getFunctionName();
        if (functionName == null || functionName.equals(REQUIRED_STRING)) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    getMessage("SAPComponentValidator.REQUIRED_VALUE_NOT_FOUND", target.SAPFMOPER_FUNCTIONNAME)));
        }

    }

    public void visit(SAPIDocOperation target) {
        // for sap idocoperation tag - nothing to validate at this point
    }

    public void visit(SAPMessage target) {
        // for sap message tag - nothing to validate at this point
    }

    private String getMessage(String key, String param) {
        return getMessage(key, new Object[] { param });
    }

    private String getMessage(String key, Object[] params) {
        String fmt = mMessages.getString(key);
        if (params != null) {
            return MessageFormat.format(fmt, params);
        } else {
            return fmt;
        }
    }
}

