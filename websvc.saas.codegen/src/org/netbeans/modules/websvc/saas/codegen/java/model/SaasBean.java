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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.saas.codegen.java.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.Constants;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.Authentication;

/**
 *
 * @author Peter Liu
 */
public abstract class SaasBean extends GenericResourceBean {

    public static final String RESOURCE_TEMPLATE = AbstractGenerator.TEMPLATES_SAAS+"WrapperResource.java"; //NOI18N
    private String outputWrapperName;
    private String wrapperPackageName;
    private List<ParameterInfo> inputParams;
    private List<ParameterInfo> queryParams;
    private String resourceTemplate;
    private SaasAuthenticationType authType;
    private SaasBean.SaasAuthentication auth;

    public SaasBean(String name, String packageName, String uriTemplate, MimeType[] mediaTypes, String[] representationTypes, HttpMethodType[] methodTypes) {
        super(name, packageName, uriTemplate, mediaTypes, representationTypes, methodTypes);
    }

    protected void setInputParameters(List<ParameterInfo> inputParams) {
        this.inputParams = inputParams;
    }

    @Override
    public List<ParameterInfo> getInputParameters() {
        if (inputParams == null) {
            inputParams = initInputParameters();
        }

        return inputParams;
    }

    public List<ParameterInfo> getHeaderParameters() {
        return Collections.emptyList();
    }

    protected abstract List<ParameterInfo> initInputParameters();

    @Override
    public List<ParameterInfo> getQueryParameters() {
        if (queryParams == null) {
            queryParams = new ArrayList<ParameterInfo>();

            for (ParameterInfo param : getInputParameters()) {
                if (param.getStyle() == ParamStyle.QUERY) {
                    queryParams.add(param);
                }
            }
        }
        return queryParams;
    }

    public String getOutputWrapperName() {
        if (outputWrapperName == null) {
            outputWrapperName = getName();

            if (outputWrapperName.endsWith(RESOURCE_SUFFIX)) {
                outputWrapperName = outputWrapperName.substring(0, outputWrapperName.length() - 8);
            }
            outputWrapperName += AbstractGenerator.CONVERTER_SUFFIX;
        }
        return outputWrapperName;
    }

    public String getOutputWrapperPackageName() {
        return wrapperPackageName;
    }

    public void setOutputWrapperPackageName(String packageName) {
        wrapperPackageName = packageName;
    }

    protected static String deriveResourceName(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name) + GenericResourceBean.RESOURCE_SUFFIX);
    }
    
    protected static String normailizeName(final String name) {
        String normalized = name;
        normalized = normalized.replaceAll("\\p{Punct}", "_");
        return normalized;
    }

    protected static String deriveUriTemplate(final String name) {
        return Inflector.getInstance().camelize(normailizeName(name), true) + "/"; //NOI18N
    }

    @Override
    public String[] getRepresentationTypes() {
        if (getMimeTypes().length == 1 && getMimeTypes()[0] == MimeType.HTML) {
            return new String[]{String.class.getName()};
        } else {
            String rep = getOutputWrapperPackageName() + "." + getOutputWrapperName();
            List<String> repList = new ArrayList<String>();
            for(MimeType m:getMimeTypes()) {//stuff rep with as much mimetype length
                repList.add(rep);
            }
            return repList.toArray(new String[0]);
        }
    }

    public String[] getOutputTypes() {
        String[] types = new String[]{"java.lang.String"}; //NOI18N
        return types;
    }

    public String getResourceClassTemplate() {
        return resourceTemplate;
    }
    
    protected void setResourceClassTemplate(String template) {
        this.resourceTemplate = template;
    }
    
    public SaasAuthenticationType getAuthenticationType() {
        return authType;
    }
    
    public void setAuthenticationType(SaasAuthenticationType authType) {
        this.authType = authType;
    }
    
    
    public SaasAuthentication getAuthentication() {
        return auth;
    }
    
    public void setAuthentication(SaasAuthentication auth) {
        this.auth = auth;
    }
    
    public void findAuthentication(SaasMethod m) {
        Authentication auth2 = m.getSaas().getSaasMetadata().getAuthentication();
        if(auth2.getHttpBasic() != null) {
            setAuthenticationType(SaasAuthenticationType.HTTP_BASIC);
            setAuthentication(new HttpBasicAuthentication());
        } else if(auth2.getCustom() != null) {
            setAuthenticationType(SaasAuthenticationType.CUSTOM);
            setAuthentication(new CustomAuthentication());
        } else if(auth2.getApiKey() != null) {
            setAuthenticationType(SaasAuthenticationType.API_KEY);
            setAuthentication(new ApiKeyAuthentication(auth2.getApiKey().getId()));
        }
    }
    
    public class SaasAuthentication {
        public SaasAuthentication() {
            
        }
    }
    
    public class HttpBasicAuthentication extends SaasAuthentication {
        public HttpBasicAuthentication() {
            
        }
    }
    
    public class ApiKeyAuthentication extends SaasAuthentication {
        private String keyName;
        
        public ApiKeyAuthentication(String keyName) {
            this.keyName = keyName;
        }
        
        public String getApiKeyName() {
            return keyName;
        }
    }
    
    public class CustomAuthentication extends SaasAuthentication {
        public CustomAuthentication() {
            
        }
    }

}