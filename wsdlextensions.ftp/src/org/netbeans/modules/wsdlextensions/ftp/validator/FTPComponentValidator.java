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
package org.netbeans.modules.wsdlextensions.ftp.validator;

import org.netbeans.modules.wsdlextensions.ftp.FTPComponentEncodable;
import org.netbeans.modules.wsdlextensions.ftp.FTPMessage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.ftp.FTPComponent;
import org.netbeans.modules.wsdlextensions.ftp.FTPOperation;
import org.netbeans.modules.wsdlextensions.ftp.FTPTransfer;
import org.netbeans.modules.wsdlextensions.ftp.FTPBinding;
import org.netbeans.modules.wsdlextensions.ftp.FTPAddress;

/**
 * semantic validation, check WSDL elements & attributes values and 
 * any relationship between;
 *
 * @author jfu
 */
public class FTPComponentValidator
        implements Validator, FTPComponent.Visitor {
    
    private static final String FTP_URL_PREFIX = "ftp://";
    private static final String FTP_URL_LOGIN_HOST_DELIM = "@";
    private static final String FTP_URL_COLON_DELIM = ":";
    private static final String FTP_URL_PATH_DELIM = "/";

    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public FTPComponentValidator() {}
    
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
            Iterator<Binding> bindings = defs.getBindings().iterator();
            
            while (bindings.hasNext()) {
                Binding binding = bindings.next();
                // bindings port type will be validated - generically by WSDL editor
                // so don't need to bother about it.
                
                // but need to check the parameters for an operation:
                // e.g.: 
                // if op1 requires input and output, then op1 binding 
                // should have input and output too, etc.
                int numFTPBindings = binding.getExtensibilityElements(FTPBinding.class).size();
                if (numFTPBindings > 0 && numFTPBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            Util.getMessage("FTPBindingValidation.ONLY_ONE_FTP_BINDING_ALLOWED")));
                }
                
                if ( numFTPBindings == 0 )
                    continue;
                
                if ( binding.getType() == null || binding.getType().get() == null )
                    continue;

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                boolean foundFTPOp = false;
                int transCntInput = 0;
                int msgCntInput = 0;
                int transCntOutput = 0;
                int msgCntOutput = 0;
                Object inputChild = null, outputChild = null;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();
                    List ftpOpsList = bindingOp.getExtensibilityElements(FTPOperation.class);
                    inputChild = outputChild = null;
                    if(ftpOpsList.size() == 1) {
                        Iterator<FTPOperation> ftpOps = ftpOpsList.iterator();

                        while (ftpOps.hasNext()) {
                            ftpOps.next().accept(this);
                        }
                        
                        foundFTPOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            transCntInput = 0;
                            msgCntInput = 0;
                            //msgModeCntInput = 0;
                            // now with the introduction of more extensibility elements
                            // that could be under <input> as child, need to do
                            // more validation

                            // assumption:
                            // under <input>, there could be one of the following:
                            // <ftp:message> or <ftp:transfer> or <ftp:messageActivePassive>
                            // but only one is allowed;
                            // 
                            Iterator<FTPTransfer> ftpTransfers =
                                    bindingInput.getExtensibilityElements(FTPTransfer.class).iterator();
                            if ( ftpTransfers != null ) {                                    
                                while (ftpTransfers.hasNext()) {
                                    transCntInput++;
                                    FTPTransfer ftpTransfer = ftpTransfers.next();
                                    ftpTransfer.accept(this);
                                    inputChild = ftpTransfer;
                                }
                                if ( transCntInput > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            binding,
                                            Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT", new Object[] {transCntInput})));
                                }
                            }

                            Iterator<FTPMessage> ftpMessages =
                                    bindingInput.getExtensibilityElements(FTPMessage.class).iterator();
                            if ( ftpMessages != null ) {                                    
                                while (ftpMessages.hasNext()) {
                                    msgCntInput++;
                                    FTPMessage ftpMessage = ftpMessages.next();
                                    ftpMessage.accept(this);
                                    inputChild = ftpMessage;
                                }
                                if ( msgCntInput > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            binding,
                                            Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT", new Object[] {msgCntInput})));
                                }
                            }
                        }
                        
                        if ( transCntInput + msgCntInput > 1 ) {
                            results.add(
                                    new Validator.ResultItem(this,
                                    Validator.ResultType.ERROR,
                                    binding,
                                    Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT", new Object[] {transCntInput + msgCntInput /*+ msgModeCntInput*/})));
                        }

                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        
                        if (bindingOutput != null) {
                            // reset and do output checking
                            transCntOutput = 0;
                            msgCntOutput = 0;
                            //msgModeCntOutput = 0;
                            Iterator<FTPTransfer> ftpTransfers =
                                    bindingOutput.getExtensibilityElements(FTPTransfer.class).iterator();
                            if ( ftpTransfers != null ) {
                                while (ftpTransfers.hasNext()) {
                                    transCntOutput++;
                                    FTPTransfer ftpTransfer = ftpTransfers.next();
                                    ftpTransfer.accept(this);
                                    outputChild = ftpTransfer;
                                }
                                if ( transCntOutput > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            binding,
                                            Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT", new Object[] {transCntOutput})));
                                }
                            }

                            Iterator<FTPMessage> ftpMessages =
                                    bindingOutput.getExtensibilityElements(FTPMessage.class).iterator();
                            if ( ftpMessages != null ) {
                                while (ftpMessages.hasNext()) {
                                    msgCntOutput++;
                                    FTPMessage ftpMessage = ftpMessages.next();
                                    ftpMessage.accept(this);
                                    outputChild = ftpMessage;
                                }
                                if ( msgCntOutput > 1 ) {
                                    results.add(
                                            new Validator.ResultItem(this,
                                            Validator.ResultType.ERROR,
                                            binding,
                                            Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT", new Object[] {msgCntOutput})));
                                }
                            }

                            if ( transCntOutput + msgCntOutput > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        Util.getMessage("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT", new Object[] {transCntOutput + msgCntOutput /*+ msgModeCntOutput*/})));
                            }
                            
                            if ( transCntOutput + msgCntOutput == 0 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        Util.getMessage("FTPBindingValidation.NO_EXTENSIBILITY_ELEMENT_FOUND_IN_OUTPUT")));
                            }
                        }
                        
                        // now with the introduction of solicit read
                        // it is OK to have empty <input>;
                        // as long as <output> has binding extensibiltiy element
                        if ( bindingOutput == null ) {
                            // when output is not null, it is a one way
                            // then it is invalid to have an empty <input>
                            if ( bindingInput != null && transCntInput + msgCntInput == 0 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        Util.getMessage("FTPBindingValidation.NO_EXTENSIBILITY_ELEMENT_FOUND_IN_INPUT")));
                            }
                        }

                        // inputChild and outputChild should be same type
                        // if input & output both present, they should have same type 
                        // of FTP BC ext elements as child
                        if ( inputChild != null && outputChild != null ) {
                            if ( inputChild.getClass().isAssignableFrom(outputChild.getClass())
                            && outputChild.getClass().isAssignableFrom(inputChild.getClass()) ) {
                                // fine
                            }
                            else {
                                // otherwise
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        Util.getMessage(
                                        "FTPBindingValidation.INPUT_OUTPUT_REQUIRE_SAME_TYPE_FTPBC_EXT",
                                        new Object[] {
                                        ((FTPComponent)inputChild).getQName().toString(),
                                        ((FTPComponent)outputChild).getQName().toString()
                                })));
                            }
                            
                        }
                    }
                    else if ( ftpOpsList.size() > 1 ) {
                        // at most one
                        foundFTPOp = true;
                        results.add(
                                new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                binding,
                                Util.getMessage("FTPBindingValidation.AT_MOST_ONE_FTP_OPERATION")));
                    }
                }
                // there is ftp:binding but no ftp:operation
                if ( numFTPBindings > 0 && !foundFTPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            Util.getMessage("FTPBindingValidation.MISSING_FTP_OPERATION")));
                }
                // there is no ftp:binding but there are ftp:operation
                if ( numFTPBindings == 0 && foundFTPOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            Util.getMessage("FTPBindingValidation.FTP_OPERATION_WO_FTP_BINDING")));
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
                            int numRelatedFTPBindings = binding.getExtensibilityElements(FTPBinding.class).size();
                            Iterator<FTPAddress> ftpAddresses = port.getExtensibilityElements(FTPAddress.class).iterator();
                            if((numRelatedFTPBindings > 0) && (!ftpAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        Util.getMessage("FTPExtValidation.MISSING_FTP_ADDRESS")));
                            }
                            
                            if(port.getExtensibilityElements(FTPAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        Util.getMessage("FTPExtValidation.ONLY_ONE_FTPADDRESS_ALLOWED")));
                            }
                            while (ftpAddresses.hasNext()) {
                                ftpAddresses.next().accept(this);
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

    private boolean checkSignature(BindingOperation bindingOp/*, Object inputChild, Object outputChild*/) {
        boolean result = true;
        Reference<Operation> opRef = bindingOp.getOperation();
        if ( opRef == null )
            return false;
        Operation op = opRef.get();
        if ( op == null )
            return false;
        if ( (op.getInput() == null && bindingOp.getBindingInput() == null /*&& inputChild == null*/)
         ||
             (op.getInput() != null && bindingOp.getBindingInput() != null /*&& inputChild != null*/)   ) {
            
        }
        else {
            result = false;
        }
            
        if ( (op.getOutput() == null && bindingOp.getBindingOutput() == null /*&& outputChild == null*/)
        ||
             (op.getOutput() != null && bindingOp.getBindingOutput() != null /*&& outputChild != null*/)   ) {
            
        }
        else {
            result = false;
        }
        return result;        
    }
    
    public void visit(FTPAddress target) {
        // validate the following:
        // (1) attribute 'url' has the right syntax: i.e. ftp://[ftp_user]:[ftp_password]@[ftp_host]:[ftp_port]
        // (2) if attribute 'useProxy' is true, also validate attribute 'proxy'
        // has the right syntax: [proxy protocol]://[proxy_user]:[proxy_password]@[proxy_host]:[proxy_port]

        // open jbi components - issue #586
        // impl FTP/TLS, need to also validate new attrs
        //
        // if securedFTP == 'ExplicitSSL' || securedFTP == 'ImplicitSSL'
        // trustStore must be specified (with password), 
        // keyStore must be specified (with password) if not - default to truststore location   
        //
        // if enableCCC == true, securedFTP must be 'ExplicitSSL'
        //
        //
        //
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        FTPAddressURL url = new FTPAddressURL(target.getFTPURL());
        
        url.parse(results, this, target);
        
        if ( target.getUseUserDefinedHeuristics() ) {
            String udn = target.getUserDefDirListStyle();
            String udloc = target.getUserDefDirListHeuristics();
            if ( udn == null || udn.trim().length() == 0 ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.MISSING_UD_DIRLSTSTYLE_NAME")));
            }
            if ( udloc == null || udloc.trim().length() == 0 ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.MISSING_UD_HEURISTICS_CFG_LOC")));
            }
        }

        if ( target.getEnableCCC() ) {
            if ( target.getSecureFTPType() == null || !target.getSecureFTPType().equals("ExplicitSSL") ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.INVALID_SECURE_FTP_TYPE", new Object[] {target.getSecureFTPType()})));
            }
        }

        if ( target.getSecureFTPType() != null && !target.getSecureFTPType().equals("None") ) {
            // key store location and trust store location can be fall back onto each other
            if ( target.getTrustStore() == null || target.getTrustStore().trim().length() == 0 ) {
                if ( target.getKeyStore() == null || target.getKeyStore().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPAddress.MISSING_KEY_TRUST_STORE_INFO", new Object[] {target.getSecureFTPType()})));
                } 
            } 
        }
        
        validateFTPChannelTimeout(target.getCmdChannelTimeout(), results, target);
        validateFTPChannelTimeout(target.getDataChannelTimeout(), results, target);
    }

    public void visit(FTPBinding target) {
        // for ftp binding tag - nothing to validate at this point
    }

    public void visit(FTPOperation target) {
        // for ftp operation tag - nothing to validate at this point
    }

    public void visit(FTPTransfer target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();

        if ( !doStructuralChecking(results, target) )
            return;
        
        String sendTo = null;
        String receiveFrom = null;
        
        // sendTo and receiveFrom can not be both NULL or blank
        // if sendTo is specified, then further validate its pre/post
        // if receiveFrom is specified, then further validate its pre/post

        sendTo = target.getSendTo();
        receiveFrom = target.getReceiveFrom();
        
        if ( (sendTo == null || sendTo.trim().length() == 0 ) 
        && (receiveFrom == null || receiveFrom.trim().length() == 0 ) ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPTransfer.BOTH_SENDTO_AND_RECEIVEFROM_ARE_NOT_SPECIFIED")));
        }

        if ( sendTo != null && sendTo.trim().length() > 0 ) {
            if ( !Util.hasMigrationEnvVar(sendTo) ) {
                if ( sendTo.endsWith("/")) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"sendTo", sendTo})));
                }
            }
            // validate sendTo related stuff
            if ( target.getPreSendCommand() != null 
                    && (target.getPreSendCommand().equals("RENAME")
            || target.getPreSendCommand().equals("COPY")) ) {
                if ( target.getPreSendLocation() == null 
                        || target.getPreSendLocation().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.PRE_POST_OPERATION_WO_LOCATION")));
                }
                if ( !Util.hasMigrationEnvVar(target.getPreSendLocation())) {
                    if ( target.getPreSendLocation() != null
                            && target.getPreSendLocation().endsWith("/")) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"preSendLocation", target.getPreSendLocation()})));
                    }
                }
            }
            
            if ( target.getPostSendCommand() != null
                    && (target.getPostSendCommand().equals("RENAME")
            || target.getPostSendCommand().equals("COPY")) ) {
                if ( target.getPostSendLocation() == null
                        || target.getPostSendLocation().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.PRE_POST_OPERATION_WO_LOCATION")));
                }
                if ( !Util.hasMigrationEnvVar(target.getPostSendLocation())) {
                    if ( target.getPostSendLocation() != null
                            && target.getPostSendLocation().endsWith("/")) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"postSendLocation", target.getPostSendLocation()})));
                    }
                }
            }
        }
        
        if ( receiveFrom != null && receiveFrom.trim().length() > 0 ) {
            if ( !Util.hasMigrationEnvVar(receiveFrom) ) {
                if ( receiveFrom.endsWith("/")) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"receiveFrom", receiveFrom})));
                }
            }
            // validate receiveFrom related stuff
            if ( target.getPreReceiveCommand() != null
                    && (target.getPreReceiveCommand().equals("RENAME")
            || target.getPreReceiveCommand().equals("COPY")) ) {
                if ( target.getPreReceiveLocation() == null
                        || target.getPreReceiveLocation().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.PRE_POST_OPERATION_WO_LOCATION")));
                }
                if ( !Util.hasMigrationEnvVar(target.getPreReceiveLocation()) ) {
                    if ( target.getPreReceiveLocation() != null
                            && target.getPreReceiveLocation().endsWith("/")) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"preReceiveLocation", target.getPreReceiveLocation()})));
                    }
                }
            }
            if ( target.getPostReceiveCommand() != null
                    && (target.getPostReceiveCommand().equals("RENAME")
            || target.getPostReceiveCommand().equals("COPY")) ) {
                if ( target.getPostReceiveLocation() == null 
                        || target.getPostReceiveLocation().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPTransfer.PRE_POST_OPERATION_WO_LOCATION")));
                }
                if ( !Util.hasMigrationEnvVar(target.getPostReceiveLocation()) ) {
                    if ( target.getPostReceiveLocation() != null
                            && target.getPostReceiveLocation().endsWith("/")) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPTransfer.A_PATH_POINTING_TO_FILE_REQUIRED", new Object[] {"postReceiveLocation", target.getPostReceiveLocation()})));
                    }
                }
            }
        }
        
        // validate use: if use="encoded", encodingStyle must be specified
        if ( !Util.hasMigrationEnvVar(target.getUse()) ) {
            if ( target.getUse() != null && target.getUse().equals("encoded") ) {
                    if ( target.getEncodingStyle() == null || target.getEncodingStyle().trim().length() == 0 ) {
                        results.add(new Validator.ResultItem(this,
                                Validator.ResultType.ERROR,
                                target,
                                Util.getMessage("FTPTransfer.MISSING_STYLE_WHEN_USE_ENCODED")));
                    }
            }
        }

        // check polling interval
        validatePollInterval(target.getPollInterval(), results, target);
    }

    public void visit(FTPMessage target) {
        String t = null;
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
        if ( !doStructuralChecking(results, target) )
            return;
        // validate that a messageRepository is specified
        if ( target.getMessageRepository() == null
                || target.getMessageRepository().trim().length() == 0 ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPMessage.A_PATH_POINTING_TO_MESSAGE_EXCHANGE_AREA_REQUIRED", "")));
        }
        // validate use: if use="encoded", encodingStyle must be specified
        if ( !Util.hasMigrationEnvVar(target.getUse()) ) {
            if ( target.getUse() != null && target.getUse().equals("encoded") ) {
                    if ( target.getEncodingStyle() == null || target.getEncodingStyle().trim().length() == 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPMessage.MISSING_STYLE_WHEN_USE_ENCODED")));
                    }
            }
        }
        // if messageName specified, and does not contain pattern chars (% escaped symbols)
        // give warning (message name usually contains patterns, especially %u)
        t = target.getMessageName();
        if ( t != null && t.trim().length() > 0 ) {
            if ( !Util.hasMigrationEnvVar(t) ) {
                if ( t.indexOf("%") < 0 ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.WARNING,
                            target,
                            Util.getMessage("FTPMessage.MSG_NAME_SPEC_DOES_NOT_INCLUDE_PATTERN")));
                }
            }
        }
        
        // if messageNamePrefixIB or messageNamePrefixOB specified, and contains pattern chars (% escaped symbols)
        // give warning (prefix must be a literal string)
        t = target.getMessageNamePrefixIB();
        if ( t != null && t.trim().length() > 0 ) {
            if ( t.indexOf("%") >= 0 ) {
                // this is not an accurate check, but just do not allow % in the prefix,
                // period!
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPMessage.IB_MSG_PREFIX_HAS_PATTERN")));
            }
        }
        t = target.getMessageNamePrefixOB();
        if ( t != null && t.trim().length() > 0 ) {
            if ( t.indexOf("%") >= 0 ) { 
                // this is not an accurate check, but just do not allow % in the prefix,
                // period!
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPMessage.OB_MSG_PREFIX_HAS_PATTERN")));
            }
        }

        // check polling interval
        validatePollInterval(target.getPollInterval(), results, target);
    }

    private boolean checkPartReference(BindingOperation bop, boolean isInputChild, String part) {
        boolean result = false;
        if ( bop != null ) {
            Reference<Operation> opRef = bop.getOperation();
            if ( opRef != null ) {
                Operation op = opRef.get();
                if ( op != null ) {
                    if ( isInputChild ) {
                        if ( op.getInput() != null ) {
                            result = hasPart(op.getInput().getMessage(), part);
                        }
                    }
                    else {
                        if ( op.getOutput() != null ) {
                            result = hasPart(op.getOutput().getMessage(), part);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private boolean hasPart(NamedComponentReference<Message> msgRef, String part) {
        boolean result = false;
        if ( msgRef != null ) {
            Message msg = msgRef.get();
            if ( msg != null ) {
                Part pt = null;
                Collection<Part> pts = msg.getParts();
                Iterator it = pts.iterator();
                while ( it.hasNext() ) {
                    pt = (Part)it.next();
                    if ( pt.getName().equals(part) ) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }
    
    private boolean doStructuralChecking(Collection<ResultItem> results, FTPComponentEncodable target) {
        Object parent = target.getParent();
        WSDLComponent wsdlComp = null;
        BindingOperation bop = null;
        if ( parent == null ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPTransfer.CAN_NOT_LOCATE_PARENT_WSDL_COMP_FOR_TRANSFER")));
            return false;
        }

        boolean asInputChild = true;
        
        if ( parent instanceof BindingInput ) {
            // <input>
            wsdlComp = ((BindingInput)parent).getParent();
        }
        else {
            // <output>
            asInputChild = false;
            wsdlComp = ((BindingOutput)parent).getParent();
        }
        
        if ( wsdlComp == null ) {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPTransfer.CAN_NOT_LOCATE_OPERATION_BINDING")));
            return false;
        }

        if ( wsdlComp instanceof BindingOperation ) {
            // if part is specified, need to resolve it here
            String t = target.getPart();
            if ( t != null && t.trim().length() > 0 ) {
                bop = (BindingOperation)wsdlComp;
                if ( !checkPartReference(bop, asInputChild, t) ) {
                    results.add(new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            target,
                            Util.getMessage("FTPExt.PART_REFERENCE_INVALID")));
                    return false;
                }
            }

            if ( ((BindingOperation)wsdlComp).getBindingInput() != null 
                    && ((BindingOperation)wsdlComp).getBindingOutput() != null ) {
                // in-out
            }
            else if (((BindingOperation)wsdlComp).getBindingInput() != null 
                    || ((BindingOperation)wsdlComp).getBindingOutput() != null) {
                // one-way
            }
            else {
                // no way
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPTransfer.OPERATION_BINDING_COMP_DOES_NOT_HAVE_INPUT_NOR_OUTPUT")));
                return false;
            }
        }  
        else {
            results.add(new Validator.ResultItem(this,
                    Validator.ResultType.ERROR,
                    target,
                    Util.getMessage("FTPTransfer.INVALID_PARENT_FOR_INPUT_OUTPUT_WSDL_COMP")));
            return false;
        }
        return true;
    }
    
    private void validateFTPChannelTimeout(String t, Collection<ResultItem> results, FTPAddress target) {
        if ( t != null && t.trim().length() > 0 ) {
            int timeout = getInt(t);
            if ( timeout < 0 ) {
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPAddress.INVALID_FTP_CH_TIMEOUT", new Object[] {t})));
            }
        }
    }

    /**
     * it can be left blank - runtime will take a default 5000 millis
     */
    private void validatePollInterval(String t, Collection<ResultItem> results,  FTPComponentEncodable target) {
        if ( t != null && t.trim().length() > 0 ) {
            if ( !Util.hasMigrationEnvVar(t) ) { // skip validation if is a env var
                int interval = getInt(t);
                if ( interval <= 0 )
                results.add(new Validator.ResultItem(this,
                        Validator.ResultType.ERROR,
                        target,
                        Util.getMessage("FTPExt.INVALID_POLL_INTERVAL", new Object[] {t})));
                }
            }
    }

    private int getInt(String t) {
        int val = -1;
        if ( t != null && t.trim().length() > 0 ) {
            try {
                val = Integer.parseInt(t);
            }
            catch (Exception e) {
            }
        }        
        return val;
    }
}
