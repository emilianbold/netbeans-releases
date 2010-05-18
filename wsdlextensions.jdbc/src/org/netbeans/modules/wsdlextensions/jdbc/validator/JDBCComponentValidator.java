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
package org.netbeans.modules.wsdlextensions.jdbc.validator;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.netbeans.modules.wsdlextensions.jdbc.JDBCComponent;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperation;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationInput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCOperationOutput;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCBinding;
import org.netbeans.modules.wsdlextensions.jdbc.JDBCAddress;


/**
 * semantic validation, check WSDL elements & attributes values and 
 * any relationship between;
 *
 * @author 
 */
public class JDBCComponentValidator
        implements Validator, JDBCComponent.Visitor {
    
    private static final String JDBC_URL_PREFIX = "jdbc";
    private static final String JDBC_URL_LOGIN_HOST_DELIM = "@";
    private static final String JDBC_URL_COLON_DELIM = ":";
    private static final String JDBC_URL_PATH_DELIM = "/";

    private static final ResourceBundle mMessages =
            ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.jdbc.validator.Bundle");
    
    private Validation mValidation;
    private ValidationType mValidationType;
    private ValidationResult mValidationResult;
    
    private String mopType = null;
    
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, 
                Collections.EMPTY_SET);
    
    public JDBCComponentValidator() {}
    
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
               
			    if (binding.getType() == null || binding.getType().get() == null) {
                    continue;
                }
                
               int numJDBCBindings = binding.getExtensibilityElements(JDBCBinding.class).size();
               if (numJDBCBindings == 0) {
                    continue;
               }
                
				if (numJDBCBindings > 0 && numJDBCBindings != 1) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("JDBCBindingValidation.ONLY_ONE_JDBC_BINDING_ALLOWED")));
                }

                Iterator<BindingOperation> bindingOps =
                        binding.getBindingOperations().iterator();
                
                boolean foundJDBCOp = false;
                while (bindingOps.hasNext()) {
                    BindingOperation bindingOp = bindingOps.next();                    
                    List jdbcOpsList = bindingOp.getExtensibilityElements(JDBCOperation.class);
                    Iterator<JDBCOperation> jdbcOps =
                            jdbcOpsList.iterator();                    
                    while (jdbcOps.hasNext()) {
                        jdbcOps.next().accept(this);
                    }                    
                    if(jdbcOpsList.size() > 0) {
                        foundJDBCOp = true;
                        BindingInput bindingInput = bindingOp.getBindingInput();
                        if (bindingInput != null) {
                            int inputCnt = 0;
                            Iterator<JDBCOperationInput> jdbcInput =
                                    bindingInput.getExtensibilityElements(JDBCOperationInput.class).iterator();
                            while (jdbcInput.hasNext()) {
                                inputCnt++;
                                JDBCOperationInput jdbcinput = jdbcInput.next();
                                jdbcinput.accept(this);									
                            }
                            if ( inputCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("JDBCBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT") + inputCnt));
                            }
                        }
                        
                        BindingOutput bindingOutput = bindingOp.getBindingOutput();
                        if (bindingOutput != null) {
                            int outputCnt = 0;
                            Iterator<JDBCOperationOutput> jdbcOuput =
                                    bindingOutput.getExtensibilityElements(JDBCOperationOutput.class).iterator();
                            while (jdbcOuput.hasNext()) {
                                outputCnt++;
                                JDBCOperationOutput jdbcoutput = jdbcOuput.next();
                                jdbcoutput.accept(this);
                            }
                            if ( outputCnt > 1 ) {
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        binding,
                                        mMessages.getString("JDBCBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT") + outputCnt));
                            }
                        }
                    }
                }
                // there is jdbc:binding but no jdbc:operation
                if ( numJDBCBindings > 0 && !foundJDBCOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("JDBCBindingValidation.MISSING_JDBC_OPERATION")));
                }
                // there is no jdbc:binding but there are jdbc:operation
                if ( numJDBCBindings == 0 && foundJDBCOp ) {
                    results.add(
                            new Validator.ResultItem(this,
                            Validator.ResultType.ERROR,
                            binding,
                            mMessages.getString("JDBCBindingValidation.JDBC_OPERATION_WO_JDBC_BINDING")));
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
                            int numRelatedJDBCBindings = binding.getExtensibilityElements(JDBCBinding.class).size();
                            Iterator<JDBCAddress> jdbcAddresses = port.getExtensibilityElements(JDBCAddress.class).iterator();
                            if((numRelatedJDBCBindings > 0) && (!jdbcAddresses.hasNext())){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("JDBCExtValidation.MISSING_JDBC_ADDRESS")));
                            }
                            
                            if(port.getExtensibilityElements(JDBCAddress.class).size() > 1){
                                results.add(
                                        new Validator.ResultItem(this,
                                        Validator.ResultType.ERROR,
                                        port,
                                        mMessages.getString("JDBCExtValidation.ONLY_ONE_JDBCADDRESS_ALLOWED")));
                            }
                            while (jdbcAddresses.hasNext()) {
                                jdbcAddresses.next().accept(this);
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

    public void visit(JDBCAddress target) {
        // validate the following:
        // (1) attribute 'url' has the right syntax: i.e. jdbc://[jdbc_user]:[jdbc_password]@[jdbc_host]:[jdbc_port]
        // (2) if attribute 'useProxy' is true, also validate attribute 'proxy'
        // has the right syntax: [proxy protocol]://[proxy_user]:[proxy_password]@[proxy_host]:[proxy_port]
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();        
        JDBCAddressURL url = new JDBCAddressURL(target.getJDBCURL()); 
        url.parse(results, this, target);       
    }

    public void visit(JDBCBinding target) {
        // for jdbc binding tag - nothing to validate at this point
    }

    public void visit(JDBCOperation target) {
        // for jdbc operation tag - nothing to validate at this point
    }

    public void visit(JDBCOperationInput target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();

		String pkname = target.getPKName();	  
		String markColName = target.getMarkColumnName();
		String markColvalue = target.getMarkColumnValue();
		String pollProcess = target.getPollingPostProcessing();
		String tableName = target.getTableName();
		String moverowtable = target.getMoveRowToTableName();
		String paramOrder = target.getParamOrder();
		String sql = target.getSql();
		int noOfRecords = target.getNumberOfRecords(); 
		int noOfParams = 0;
		for(int cnt=0;cnt< sql.length();cnt++){
			
			if(sql.charAt(cnt) == '?'){
				noOfParams++;
			}
		}
		final String PARAM_SEPARATOR = ",";
		int tokenCnt = 0;
		if(noOfParams > 0 && paramOrder == null){
			 results.add(new Validator.ResultItem(this,
						Validator.ResultType.ERROR,
						target,
						mMessages.getString("JDBCOperationInput.PARAM_ORDER_NULL_ERROR")));
		}
	    if(paramOrder != null){
			StringTokenizer params = new StringTokenizer(paramOrder, PARAM_SEPARATOR);
		    while (params.hasMoreTokens()) {
		    	 String param = params.nextToken().trim();
		    	 tokenCnt++;
		    	 if(param == ""){
		    		 results.add(new Validator.ResultItem(this,
								Validator.ResultType.ERROR,
								target,
								mMessages.getString("JDBCOperationInput.PARAM_ORDER_MISMATCH")));
		    	 }
		    }
		    if(noOfParams == 0 && tokenCnt > 0){
		    	 results.add(new Validator.ResultItem(this,
							Validator.ResultType.ERROR,
							target,
							mMessages.getString("JDBCOperationInput.NOPARAMS_ERROR")));
		    }
	    }
		mopType = target.getOperationType();
		
		if(mopType != null && mopType != ""){

			if ( mopType == null || mopType.trim().length() == 0 ) {
				results.add(new Validator.ResultItem(this,
						Validator.ResultType.ERROR,
						target,
						mMessages.getString("JDBCOperationInput.MISSING_OPERATION_TYPE")));
			}	  
			  
			  if(sql == null || sql == ""){
				   results.add(new Validator.ResultItem(this,
						  Validator.ResultType.ERROR,
						  target,
						  mMessages.getString("JDBCOperationInput.MISSING_SQL")+ " for Operation " + mopType));
			  }
			
			  /*
			  if(tableName == null || tableName == ""){
				   results.add(new Validator.ResultItem(this,
						Validator.ResultType.ERROR,
						target,
						mMessages.getString("JDBCOperationInput.MISSING_TABLENAME")+ "for Operation " + mopType));
			  }
			  */

			  if(noOfRecords<0){
				   results.add(new Validator.ResultItem(this,
						  Validator.ResultType.ERROR,
						  target,
						  mMessages.getString("JDBCOperationInput.NEGATIVE_NOOFRECORDS")+ " for Operation " + mopType));
			  }
			  
			  if(mopType.equalsIgnoreCase("poll")){		
				  /*
				  if(pkname == null || pkname == ""){
				   results.add(new Validator.ResultItem(this,
						  Validator.ResultType.ERROR,
						  target,
						  mMessages.getString("JDBCOperationInput.MISSING_PKNAME")+ "for Operation " + mopType));
			      }
				  */

				  if(pollProcess == null || pollProcess == ""){
						  results.add(new Validator.ResultItem(this,
									 Validator.ResultType.ERROR,
									 target,
									 mMessages.getString("JDBCOperationInput.MISSING_POLL_POST_PROCESS")
									 +"for Operation " + mopType));			  					  
				  }
				  if(pollProcess.equalsIgnoreCase("MarkColumn")){
					  if(markColName == "" || markColName == null || markColvalue == "" || markColvalue == null){
						  results.add(new Validator.ResultItem(this,
									 Validator.ResultType.ERROR,
									 target,
									 mMessages.getString("JDBCOperationInput.MISSING_MARKCOLUMN_NAME_VALUE")
									 +"for Operation " + mopType));			  
					  }
				  }else if(pollProcess.equalsIgnoreCase("MoveRow")){
					  if(moverowtable == "" || moverowtable == null){
						  results.add(new Validator.ResultItem(this,
									 Validator.ResultType.ERROR,
									 target,
									 mMessages.getString("JDBCOperationInput.MISSING_MOVEROW_VALUE")
									 +"for Operation " + mopType));			  
					  }
				  }	else if(pollProcess.equalsIgnoreCase("CopyRow")){
					  if(moverowtable == "" || moverowtable == null){
						  results.add(new Validator.ResultItem(this,
									 Validator.ResultType.ERROR,
									 target,
									 mMessages.getString("JDBCOperationInput.MISSING_COPYROW_VALUE")
									 +"for Operation " + mopType));			  
					  }
				  }					  				  
			   }//if poll		
		}else{
			 results.add(new Validator.ResultItem(this,
									 Validator.ResultType.ERROR,
									 target,
									 mMessages.getString("JDBCOperationInput.MISSING_OPERATION_VALUE")
									 +"for Operation " + mopType));		
		}
    }//input


	public void visit(JDBCOperationOutput target) {
        // check the values and relations of/between all the attributes
        Collection<ResultItem> results =
                mValidationResult.getValidationResult();
		

    }//output
}
