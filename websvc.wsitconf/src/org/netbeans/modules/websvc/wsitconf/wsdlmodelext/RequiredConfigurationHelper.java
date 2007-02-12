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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.*;

/**
 *
 * @author Martin Grebac
 */
public class RequiredConfigurationHelper {
    
    /**
     * @param c One of Binding, Operation
     * @param glassfish
     * @param jsr109
     * @param cbHandlerType One of usernameHandler, kerberosHandler, samlHandler, passwordHandler
     * @return Returns true if configuration of callbackhandler cbHandlerType is required for component
     */
    public static boolean isCallbackHandlerRequired(
            WSDLComponent c, boolean glassfish, boolean jsr109, String cbHandlerType) {
        return true;
    }

    /**
     * @param c One of Binding, Operation
     * @param glassfish
     * @param jsr109
     * @param validatorType One of usernameValidator, timestampValidator, certificateValidator, samlValidator
     * @return Returns true if configuration of validator validatorType is required for component
     */
    public static boolean isValidatorRequired(
            WSDLComponent c, boolean glassfish, boolean jsr109, String validatorType) {
        return true;
    }
    
    public static boolean isKeystoreRequired(
            WSDLComponent c, boolean client, boolean glassfish, boolean jsr109) {
        return true; 
    }

    public static boolean isTruststoreRequired(
            WSDLComponent c, boolean client, boolean glassfish, boolean jsr109) {
        return true;
    }
    
    public static boolean isSecureConversationParamRequired(
            WSDLComponent c) {

        boolean secConvEnabled = false;
        
        if (SecurityPolicyModelHelper.isSecurityEnabled(c)) {
            WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(c);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, ProtectionToken.class);
            WSDLComponent tokenType = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            secConvEnabled = (tokenType instanceof SecureConversationToken);
        }
        
        return secConvEnabled; 
    }

}
