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


package org.netbeans.modules.websvc.wsitmodelext.security.tokens;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum TokensQName {
    WSSKERBEROSV5APREQTOKEN11(createSecurityPolicyQName("WssKerberosV5ApReqToken11")),  //NOI18N
    WSSGSSKERBEROSV5APREQTOKEN11(createSecurityPolicyQName("WssGssKerberosV5ApReqToken11")),  //NOI18N

    WSSX509V1TOKEN10(createSecurityPolicyQName("WssX509V1Token10")),  //NOI18N
    WSSX509V3TOKEN10(createSecurityPolicyQName("WssX509V3Token10")),  //NOI18N
    WSSX509PKCS7TOKEN10(createSecurityPolicyQName("WssX509Pkcs7Token10")),  //NOI18N
    WSSX509PKIPATHV1TOKEN10(createSecurityPolicyQName("WssX509PkiPathV1Token10")),  //NOI18N
    WSSX509V1TOKEN11(createSecurityPolicyQName("WssX509V1Token11")),  //NOI18N
    WSSX509V3TOKEN11(createSecurityPolicyQName("WssX509V3Token11")),  //NOI18N
    WSSX509PKCS7TOKEN11(createSecurityPolicyQName("WssX509Pkcs7Token11")),  //NOI18N
    WSSX509PKIPATHV1TOKEN11(createSecurityPolicyQName("WssX509PkiPathV1Token11")),  //NOI18N

    REQUIREKEYIDENTIFIERREFERENCE(createSecurityPolicyQName("RequireKeyIdentifierReference")),  //NOI18N
    REQUIREISSUERSERIALREFERENCE(createSecurityPolicyQName("RequireIssuerSerialReference")),  //NOI18N
    REQUIREEMBEDDEDTOKENREFERENCE(createSecurityPolicyQName("RequireEmbeddedTokenReference")),  //NOI18N
    REQUIRETHUMBPRINTREFERENCE(createSecurityPolicyQName("RequireThumbprintReference")),  //NOI18N
    REQUIREEXTERNALURIREFERENCE(createSecurityPolicyQName("RequireExternalUriReference")),  //NOI18N
    SC10SECURITYCONTEXTTOKEN(createSecurityPolicyQName("SC10SecurityContextToken")),  //NOI18N

    REQUIREINTERNALREFERENCE(createSecurityPolicyQName("RequireInternalReference")),  //NOI18N
    REQUIREEXTERNALREFERENCE(createSecurityPolicyQName("RequireExternalReference")),  //NOI18N
    REQUIREDERIVEDKEYS(createSecurityPolicyQName("RequireDerivedKeys")),  //NOI18N
    ISSUER(createSecurityPolicyQName("Issuer")),  //NOI18N
    
    WSSUSERNAMETOKEN10(createSecurityPolicyQName("WssUsernameToken10")),  //NOI18N
    WSSUSERNAMETOKEN11(createSecurityPolicyQName("WssUsernameToken11")),  //NOI18N

    WSSSAMLV10TOKEN10(createSecurityPolicyQName("WssSamlV10Token10")),  //NOI18N
    WSSSAMLV11TOKEN10(createSecurityPolicyQName("WssSamlV11Token10")),  //NOI18N
    WSSSAMLV10TOKEN11(createSecurityPolicyQName("WssSamlV10Token11")),  //NOI18N
    WSSSAMLV11TOKEN11(createSecurityPolicyQName("WssSamlV11Token11")),  //NOI18N
    WSSSAMLV20TOKEN11(createSecurityPolicyQName("WssSamlV20Token11")),  //NOI18N
    
    WSSRELV10TOKEN10(createSecurityPolicyQName("WssRelV10Token10")),  //NOI18N
    WSSRELV20TOKEN10(createSecurityPolicyQName("WssRelV20Token10")),  //NOI18N
    WSSRELV10TOKEN11(createSecurityPolicyQName("WssRelV10Token11")),  //NOI18N
    WSSRELV20TOKEN11(createSecurityPolicyQName("WssRelV20Token11")),  //NOI18N

    INCLUDETOKENATTRIBUTE(createSecurityPolicyQName("IncludeToken")),  //NOI18N
    
    USERNAMETOKEN(createSecurityPolicyQName("UsernameToken")),  //NOI18N
    X509TOKEN(createSecurityPolicyQName("X509Token")),  //NOI18N
    KERBEROSTOKEN(createSecurityPolicyQName("KerberosToken")),  //NOI18N
    SPNEGOCONTEXTTOKEN(createSecurityPolicyQName("SpnegoContextToken")),  //NOI18N
    SECURITYCONTEXTTOKEN(createSecurityPolicyQName("SecurityContextToken")),  //NOI18N
    SECURECONVERSATIONTOKEN(createSecurityPolicyQName("SecureConversationToken")),  //NOI18N
    PROTECTIONTOKEN(createSecurityPolicyQName("ProtectionToken")),  //NOI18N
    TRANSPORTTOKEN(createSecurityPolicyQName("TransportToken")),  //NOI18N
    SUPPORTINGTOKENS(createSecurityPolicyQName("SupportingTokens")),  //NOI18N
    SIGNEDSUPPORTINGTOKENS(createSecurityPolicyQName("SignedSupportingTokens")),  //NOI18N
    ENDORSINGSUPPORTINGTOKENS(createSecurityPolicyQName("EndorsingSupportingTokens")),  //NOI18N
    SIGNEDENDORSINGSUPPORTINGTOKENS(createSecurityPolicyQName("SignedEndorsingSupportingTokens")),  //NOI18N
    SIGNATURETOKEN(createSecurityPolicyQName("SignatureToken")),  //NOI18N
    ENCRYPTIONTOKEN(createSecurityPolicyQName("EncryptionToken")),  //NOI18N
    INITIATORTOKEN(createSecurityPolicyQName("InitiatorToken")),  //NOI18N
    RECIPIENTTOKEN(createSecurityPolicyQName("RecipientToken")),  //NOI18N
    SAMLTOKEN(createSecurityPolicyQName("SamlToken")),  //NOI18N
    RELTOKEN(createSecurityPolicyQName("RelToken")),  //NOI18N
    HTTPSTOKEN(createSecurityPolicyQName("HttpsToken")),  //NOI18N
    ISSUEDTOKEN(createSecurityPolicyQName("IssuedToken"));  //NOI18N

    public static final String SECPOLICY_UTILITY = 
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy"; //NOI18N
    public static final String SECPOLICY_UTILITY_NS_PREFIX = "sp";         //NOI18N
            
    public static QName createSecurityPolicyQName(String localName){
        return new QName(SECPOLICY_UTILITY, localName, SECPOLICY_UTILITY_NS_PREFIX);
    }
    
    TokensQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (TokensQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

}
