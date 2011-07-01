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
package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;



/**
 * @author ads
 *
 */
@ServiceProvider(service=WLPolicyClientCodeGenerator.class)
public class WLWss11SamlOrUsernameGenerator extends DefaultWlClientCodeGenerator 
    implements WLPolicyClientCodeGenerator
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getPolicyId()
     */
    @Override
    public String getPolicyId() {
        return "wss11_saml_or_username_token_with_message_protection_service_policy";   // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getPolicyAccessCode(java.util.Map)
     */
    @Override
    public String getPolicyAccessCode(Map<String,Object> context) {
        return "WSBindingProvider wsbp = (WSBindingProvider)port;\n"+
            "Map<String, Object> requestContext = wsbp.getRequestContext();\n"+
            "//comment out for B16\n"+
            "//requestContext.put(ClientConstants.WSSEC_KEYSTORE_TYPE, \"JKS\");\n"+
            "//requestContext.put(ClientConstants.WSSEC_KEYSTORE_LOCATION, // need location here);\n"+
            "//requestContext.put(ClientConstants.WSSEC_KEYSTORE_PASSWORD, // need keystore password here);\n"+
            "//comment out for B16\n"+
            "\n//FOR SAML\n"+
            "//requestContext.put(ClientConstants.WSSEC_SIG_KEY_ALIAS, // need key alias here);  " +
            "// public key from server for signing\n"+
            "//requestContext.put(ClientConstants.WSSEC_SIG_KEY_PASSWORD, // need key password here);\n"+
            "//FOR SAML\n"+
            "// Override the endpoint - useful when switching target environments without regenerating the jax-ws client\n"+
            "requestContext.put(WSBindingProvider.ENDPOINT_ADDRESS_PROPERTY, \""+
            context.get( JaxWsCodeGenerator.WSDL_URL )+"\");\n"+
            "\n//B16\n"+
            "// requestContext.put(ClientConstants.WSSEC_RECIPIENT_KEY_ALIAS, // need key alias here)\n"+
            "//B16  \n"+
            "// Specify username\n"+
            "requestContext.put(WSBindingProvider.USERNAME_PROPERTY, null );\n"+
            "// For username token, specify password.  Not used for SAML security policy\n"+
            "requestContext.put(WSBindingProvider.PASSWORD_PROPERTY, null );\n";
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.DefaultWlClientCodeGenerator#getJarUrls(java.util.Map, org.netbeans.api.project.SourceGroup[])
     */
    @Override
    protected List<URL> getJarUrls( Map<String, Object> context,
            SourceGroup[] sgs )
    {
        List<URL> urls = super.getJarUrls(context, sgs);
        FileObject modules = (FileObject)context.get(
                WLWsClientMethodGeneratorStrategy.ORACLE_MODULES);
        for( FileObject dir :modules.getChildren() ){
            String name = dir.getName();
            if ( name.startsWith(ORACLE_WEBSERVICES)){        
                FileObject jar = dir.getFileObject("wsclient-rt",JAR); 
                if ( jar != null ){
                    addJar(urls, jar);
                }
            }
        }
        
        return urls;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getRequiredImports()
     */
    @Override
    public Collection<String> getRequiredImports() {
        Collection<String> result =  super.getRequiredImports();
        result.add("oracle.webservices.ClientConstants");                       // NOI18N
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getGeneratoinClientIds()
     */
    @Override
    public Collection<String> getGeneratoinClientIds() {
        Collection<String> result =  new ArrayList<String>(2);
        result.add("oracle/wss11_username_token_with_message_protection_client_policy");//NOI18N
        result.add("oracle/wss11_saml_token_with_message_protection_client_policy");    //NOI18N
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.core.jaxws.actions.WLPolicyClientCodeGenerator#getDefaultGenerationClientId()
     */
    @Override
    public String getDefaultGenerationClientId() {
        return "oracle/wss_username_token_client_policy";           // NOI18N
    }

}
