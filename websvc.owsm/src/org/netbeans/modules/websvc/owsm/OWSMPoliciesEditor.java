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
package org.netbeans.modules.websvc.owsm;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.javaee.specs.support.api.JaxWsPoliciesSupport;
import org.netbeans.modules.websvc.api.wseditor.InvalidDataException;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class OWSMPoliciesEditor implements WSEditor {
    
    private static final String ORACLE = "oracle/";     // NOI18N
    
    OWSMPoliciesEditor( JaxWsPoliciesSupport support , Lookup lookup){
        mySupport = support;
        myFileObject = lookup.lookup( FileObject.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#createWSEditorComponent(org.openide.nodes.Node)
     */
    @Override
    public JComponent createWSEditorComponent( Node node )
            throws InvalidDataException
    {
        if ( mySupport == null || myFileObject == null ){
            JComponent component = new JPanel();
            component.setLayout( new FlowLayout());
            component.add( new JLabel(NbBundle.getMessage( OWSMPoliciesEditor.class, 
                    "ERR_NoPoliciesSupport")));             // NOI18N
            return component;
        }
        List<String> securityPolicies = filterSecurityPolicies( );
        return new PoliciesVisualPanel( securityPolicies );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#getTitle()
     */
    @Override
    public String getTitle() {
        return NbBundle.getMessage( OWSMPoliciesEditor.class, "TXT_OWSMEditorTitle");   // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#save(org.openide.nodes.Node)
     */
    @Override
    public void save( Node node ) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#cancel(org.openide.nodes.Node)
     */
    @Override
    public void cancel( Node node ) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.api.wseditor.WSEditor#getDescription()
     */
    @Override
    public String getDescription() {
        return NbBundle.getMessage( OWSMPoliciesEditor.class, "TXT_PanelDescription");  // NOI18N
    }
    
    private List<String> filterSecurityPolicies() {
        List<String> list = mySupport.getServicePolicyIds();
        Set<String> set = new LinkedHashSet<String>( SECURITY_POLICIES );
        set.retainAll( list );
        
        List<String> result = new ArrayList<String>( list.size() );
        for (String id : set) {
            result.add( ORACLE + id);
        }
        return result;
    }
    
    private static Set<String> SECURITY_POLICIES = new LinkedHashSet<String>();
    private JaxWsPoliciesSupport mySupport;
    private FileObject myFileObject;

    static {
        SECURITY_POLICIES.add("binding_authorization_denyall_policy");      // NOI18N
        SECURITY_POLICIES.add("binding_authorization_permitall_policy");  // NOI18N
        SECURITY_POLICIES.add("binding_permission_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authentication_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_authorization_service_policy");  // NOI18N
        SECURITY_POLICIES.add("no_messageprotection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("sts_trust_config_service_policy");  // NOI18N
        SECURITY_POLICIES.add("whitelist_authorization_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_http_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_or_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_saml20_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_sts_issued_saml_bearer_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_over_ssl_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss_username_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_hok_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_integrity_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_id_propagation_with_msg_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_username_token_with_message_protection_ski_basic256_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss10_x509_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_basic128_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_kerberos_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_or_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_saml20_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_sts_issued_saml_hok_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_username_token_with_message_protection_service_policy");  // NOI18N
        SECURITY_POLICIES.add("wss11_x509_token_with_message_protection_service_policy");  // NOI18N
    }
}
