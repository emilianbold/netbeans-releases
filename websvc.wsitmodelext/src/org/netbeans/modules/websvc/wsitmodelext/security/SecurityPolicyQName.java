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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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


package org.netbeans.modules.websvc.wsitmodelext.security;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum SecurityPolicyQName {
    TRUST10(createSecurityPolicyQName("Trust10")),                        //NOI18N
    MUSTSUPPORTISSUEDTOKENS(createSecurityPolicyQName("MustSupportIssuedTokens")),  //NOI18N
    MUSTSUPPORTCLIENTCHALLENGE(createSecurityPolicyQName("MustSupportClientChallenge")),  //NOI18N
    MUSTSUPPORTSERVERCHALLENGE(createSecurityPolicyQName("MustSupportServerChallenge")),  //NOI18N
    REQUIRECLIENTENTROPY(createSecurityPolicyQName("RequireClientEntropy")),  //NOI18N
    REQUIRESERVERENTROPY(createSecurityPolicyQName("RequireServerEntropy")),  //NOI18N

    WSS11(createSecurityPolicyQName("Wss11")),                        //NOI18N
    WSS10(createSecurityPolicyQName("Wss10")),                        //NOI18N
    MUSTSUPPORTREFKEYIDENTIFIER(createSecurityPolicyQName("MustSupportRefKeyIdentifier")),  //NOI18N
    MUSTSUPPORTREFISSUERSERIAL(createSecurityPolicyQName("MustSupportRefIssuerSerial")),  //NOI18N
    MUSTSUPPORTREFTHUMBPRINT(createSecurityPolicyQName("MustSupportRefThumbprint")),  //NOI18N
    MUSTSUPPORTREFENCRYPTEDKEY(createSecurityPolicyQName("MustSupportRefEncryptedKey")),  //NOI18N
    MUSTSUPPORTREFEXTERNALURI(createSecurityPolicyQName("MustSupportRefExternalURI")),  //NOI18N
    MUSTSUPPORTREFEMBEDDEDTOKEN(createSecurityPolicyQName("MustSupportRefEmbeddedToken")),  //NOI18N
    REQUIRESIGNATURECONFIRMATION(createSecurityPolicyQName("RequireSignatureConfirmation")),  //NOI18N
    REQUESTSECURITYTOKENTEMPLATE(createSecurityPolicyQName("RequestSecurityTokenTemplate")),  //NOI18N

    SIGNEDPARTS(createSecurityPolicyQName("SignedParts")),  //NOI18N
    SIGNEDELEMENTS(createSecurityPolicyQName("SignedElements")),  //NOI18N
    ENCRYPTEDPARTS(createSecurityPolicyQName("EncryptedParts")),  //NOI18N
    ENCRYPTEDELEMENTS(createSecurityPolicyQName("EncryptedElements")),  //NOI18N
    REQUIREDELEMENTS(createSecurityPolicyQName("RequiredElements")),  //NOI18N
    XPATH(createSecurityPolicyQName("XPath")),  //NOI18N
    BODY(createSecurityPolicyQName("Body")),  //NOI18N
    HEADER(createSecurityPolicyQName("Header")),  //NOI18N

    TRANSPORTBINDING(createSecurityPolicyQName("TransportBinding")),  //NOI18N
    SYMMETRICBINDING(createSecurityPolicyQName("SymmetricBinding")),  //NOI18N
    ASYMMETRICBINDING(createSecurityPolicyQName("AsymmetricBinding")),  //NOI18N
    BOOTSTRAPPOLICY(createSecurityPolicyQName("BootstrapPolicy")),  //NOI18N
    
    INCLUDETIMESTAMP(createSecurityPolicyQName("IncludeTimestamp")),  //NOI18N
    ENCRYPTBEFORESIGNING(createSecurityPolicyQName("EncryptBeforeSigning")),  //NOI18N
    ENCRYPTSIGNATURE(createSecurityPolicyQName("EncryptSignature")),  //NOI18N
    PROTECTTOKENS(createSecurityPolicyQName("ProtectTokens")),  //NOI18N
    ONLYSIGNENTIREHEADERSANDBODY(createSecurityPolicyQName("OnlySignEntireHeadersAndBody")),  //NOI18N

    LAYOUT(createSecurityPolicyQName("Layout")),  //NOI18N
    STRICT(createSecurityPolicyQName("Strict")),  //NOI18N
    LAX(createSecurityPolicyQName("Lax")),  //NOI18N
    LAXTSFIRST(createSecurityPolicyQName("LaxTsFirst")),  //NOI18N
    LAXTSLAST(createSecurityPolicyQName("LaxTsLast"));  //NOI18N

    public static final String SECPOLICY_UTILITY = 
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"; //NOI18N
    public static final String SECPOLICY_UTILITY_NS_PREFIX = "sp";         //NOI18N
            
    public static QName createSecurityPolicyQName(String localName){
        return new QName(SECPOLICY_UTILITY, localName, SECPOLICY_UTILITY_NS_PREFIX);
    }
    
    SecurityPolicyQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (SecurityPolicyQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
