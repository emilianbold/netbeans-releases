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

package org.netbeans.modules.wsdlextensions.ftp.validation.test;

import junit.framework.*;
import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.wsdlextensions.ftp.validator.FTPComponentValidator;
/**
 * @author jfu
 */
public class FTPComponentValidatorTest extends TestCase {
    private static final ResourceBundle mMessages =
        ResourceBundle.getBundle("org.netbeans.modules.wsdlextensions.ftp.validator.Bundle");

    public FTPComponentValidatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        //TestCatalogModel.getDefault().clearDocumentPool();
    }

    private ValidationResult validate(String relativePath) throws Exception {
        WSDLModel model = Util.loadWSDLModel(relativePath);
        Validation validation = new Validation();
        ValidationType validationType = Validation.ValidationType.COMPLETE;
        FTPComponentValidator instance = new FTPComponentValidator();
        ValidationResult result = 
            instance.validate(model, validation, validationType);
        return result;
    }
    
    private void validate(String relativePath, HashSet<String> expectedErrors)
        throws Exception {
        System.out.println(relativePath);
        ValidationResult result = validate(relativePath);
        Iterator<ResultItem> it = result.getValidationResult().iterator();
        while (it.hasNext()) {
            ResultItem item = it.next();
            System.out.println("    " + item.getDescription());
            assertTrue(item.getDescription(), match(item.getDescription(), expectedErrors));
        }
        if (result.getValidationResult().size() == 0 && expectedErrors.size() > 0) {
            fail("Expected at least " + expectedErrors.size() + " error(s).  Got 0 errors instead");
        }
    }
    
    private boolean match(String msg, HashSet<String> expectedErrors) {
        boolean result = false;
        if ( msg != null && expectedErrors != null ) {
            Iterator it = expectedErrors.iterator();
            while ( it.hasNext() ) {
                if ( msg.startsWith(it.next().toString()) ) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Test of getName method, of class org.netbeans.modules.wsdlextensions.ftp.validation.FTPComponentValidator.
     */
    public void testGetName() throws Exception {
        FTPComponentValidator instance = new FTPComponentValidator();
        
        String expResult = instance.getClass().getName();
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class org.netbeans.modules.wsdlextensions.ftp.validation.FTPComponentValidator.
     */
    public void testValidWSDLs() throws Exception {
        // Grab all our WSDL files to test using a known WSDL
        URI resource = Util.getResourceURI("data/valid/valid_ftpbc_wsdl_001.wsdl");
        File resourceFile = new File(resource);
        File[] wsdls = resourceFile.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".wsdl");
            }});
        for (int ii = 0; ii < wsdls.length; ii++) {
            String relativePath = 
                "data/valid/" + 
                wsdls[ii].getName();
            ValidationResult result = validate(relativePath);
            assertTrue(result.getValidationResult().size() == 0);
        }
    }
    
/**
 FTPExtValidation.MISSING_FTP_ADDRESS=Missing ftp:address in service binding
FTPExtValidation.ONLY_ONE_FTPADDRESS_ALLOWED=Only one ftp:address allowed
FTPBindingValidation.ONLY_ONE_FTP_BINDING_ALLOWED=Only one ftp:binding allowed
FTPAddress.MISSING_FTP_URL=Missing FTP URL in ftp binding address
FTPAddress.MISSING_PROXY_URL=Missing proxy URL in ftp binding address
FTPAddress.INVALID_FTP_URL_PREFIX=Invalid FTP url, not starting with ftp://, url=
FTPAddress.MALFORMED_FTP_URL=Malformed FTP url, url=
FTPAddress.INVALID_FTP_URL_PATH_NOT_ALLOWED=Invalid FTP url, path not allowed for a url as FTP endpoint, url=
FTPAddress.MALFORMED_FTP_URL_HOST_REQUIRED=Invalid FTP url, host required, url=

FTPAddress.INVALID_PROXY_URL_PREFIX=Invalid proxy url, not starting with socks4:// or socks5://, url value=
FTPAddress.MALFORMED_PROXY_URL=Malformed proxy url, url=
FTPAddress.INVALID_PROXY_URL_PATH_NOT_ALLOWED=Invalid proxy url, path not allowed for a proxy url
FTPAddress.MALFORMED_PROXY_URL_HOST_REQUIRED=Invalid proxy url, host required, url=
FTPAddress.REPLACE_FTP_URL_PLACEHOLDER_WITH_REAL_URL=FTP url is still a ftp url placeholder, please specify a concrete FTP address.
FTPAddress.REPLACE_PROXY_URL_PLACEHOLDER_WITH_REAL_URL=Proxy url is still a placeholder, please specify a concrete proxy address.
FTPAddress.INVALID_PORT_IN_URL=Invalid port in URL, must be a positive number following host name as in localhost:21, url=

FTPTransfer.MISSING_TARGET_FILE=Missing Target File Name for ftp:transfer.
FTPTransfer.MISSING_UD_HEURISTICS_CFG_LOC=Location for user defined heuristics not specified.
FTPTransfer.MISSING_UD_DIRLSTSTYLE_NAME=User defined directory listing style name not specified.

FTPBindingValidation.MISSING_FTP_OPERATION=Missing ftp:operation in ftp:binding
FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT=At most one ftp:transfer allowed in one <input> binding, found:
FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT=At most one ftp:transfer allowed in one <output> binding, found:
FTPBindingValidation.FTP_OPERATION_WO_FTP_BINDING=ftp:operation found without corresponding ftp:binding
*/    
    public void testInvalidFTPWSDL001() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPTransfer.MISSING_TARGET_FILE"));
        expectedErrors.add(mMessages.getString("FTPAddress.MALFORMED_FTP_URL"));
        validate("data/invalid/invalid_ftpbc_wsdl_001.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL002() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPBindingValidation.MISSING_FTP_OPERATION"));
        validate("data/invalid/invalid_ftpbc_wsdl_002.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL003() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.REPLACE_PROXY_URL_PLACEHOLDER_WITH_REAL_URL"));
        validate("data/invalid/invalid_ftpbc_wsdl_003.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL004() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPTransfer.MISSING_UD_HEURISTICS_CFG_LOC"));
        validate("data/invalid/invalid_ftpbc_wsdl_004.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL005() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPBindingValidation.FTP_OPERATION_WO_FTP_BINDING"));
        validate("data/invalid/invalid_ftpbc_wsdl_005.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL006() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.REPLACE_FTP_URL_PLACEHOLDER_WITH_REAL_URL"));
        expectedErrors.add(mMessages.getString("FTPTransfer.MISSING_TARGET_FILE"));
        validate("data/invalid/invalid_ftpbc_wsdl_006.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL007() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.MISSING_FTP_URL"));
        expectedErrors.add(mMessages.getString("FTPAddress.MISSING_PROXY_URL"));
        validate("data/invalid/invalid_ftpbc_wsdl_007.wsdl"
                , expectedErrors);        
    }

    public void testInvalidFTPWSDL008() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPExtValidation.MISSING_FTP_ADDRESS"));
        validate("data/invalid/invalid_ftpbc_wsdl_008.wsdl"
                , expectedErrors);        
    }
    
    public void testInvalidFTPWSDL009() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.INVALID_FTP_URL_PREFIX"));
        validate("data/invalid/invalid_ftpbc_wsdl_009.wsdl"
                , expectedErrors);        
    }
    public void testInvalidFTPWSDL010() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.INVALID_PORT_IN_URL"));
        validate("data/invalid/invalid_ftpbc_wsdl_010.wsdl"
                , expectedErrors);        
    }
    public void testInvalidFTPWSDL011() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPAddress.MALFORMED_PROXY_URL_HOST_REQUIRED"));
        validate("data/invalid/invalid_ftpbc_wsdl_011.wsdl"
                , expectedErrors);        
    }
    public void testInvalidFTPWSDL012() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_INPUT"));
        expectedErrors.add(mMessages.getString("FTPBindingValidation.ATMOST_ONE_TRANSFER_IN_OUTPUT"));
        validate("data/invalid/invalid_ftpbc_wsdl_012.wsdl"
                , expectedErrors);        
    }
    public void testInvalidFTPWSDL013() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPBindingValidation.ONLY_ONE_FTP_BINDING_ALLOWED"));
        validate("data/invalid/invalid_ftpbc_wsdl_013.wsdl"
                , expectedErrors);        
    }
    public void testInvalidFTPWSDL014() throws Exception {
        HashSet<String> expectedErrors = new HashSet<String>();
        expectedErrors.add(mMessages.getString("FTPExtValidation.ONLY_ONE_FTPADDRESS_ALLOWED"));
        validate("data/invalid/invalid_ftpbc_wsdl_014.wsdl"
                , expectedErrors);        
    }
}
