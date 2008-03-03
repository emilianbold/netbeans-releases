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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.saas.codegen.java.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.WadlSaasResource;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo.ParamStyle;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.netbeans.modules.websvc.saas.model.wadl.Method;

/**
 *
 * @author ayubkhan
 */
public class WadlSaasBean extends SaasBean {

    public static final String SAAS_SERVICE_TEMPLATE = AbstractGenerator.TEMPLATES_SAAS+"SaasService.java"; //NOI18N
    private String url;
    private WadlSaasMethod m;
    
    public WadlSaasBean(WadlSaasMethod m)  throws IOException {
        super(Util.deriveResourceName(m.getName()), null, 
                Util.deriveUriTemplate(m.getName()), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.m = m;
        setResourceClassTemplate(RESOURCE_TEMPLATE);
        init();
    }

    public WadlSaasMethod getMethod() {
        return m;
    }
    
    private void init() {    
        findAuthentication(m);
        initUrl();
        getInputParameters();//init parameters
        initMimeTypes();
    }
    
    private void initUrl() {
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        try {
            Resource[] rArray = m.getResourcePath();
            if(rArray == null || rArray.length == 0)
                throw new IllegalArgumentException("Method do not belong to any resource in the WADL.");
            Resource currResource = rArray[rArray.length-1];
            String url2 = m.getSaas().getWadlModel().getResources().getBase();
            url2 = url2.replace("://", "  ");//replace now, add :// later
            for(int i=0;i<rArray.length;i++){
                String path = rArray[i].getPath();
                if(path != null && path.trim().length() > 0) {
                    url2 += "/" + rArray[i].getPath();
                }
            }
            url2 = url2.replace("//", "/");
            url2 = url2.replace("  ", "://");//put back ://
            this.url = url2;
        } catch (Exception ex) {
        } 
    }
    
    protected List<ParameterInfo> initInputParameters() {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        try {
            Resource[] rArray = m.getResourcePath();
            if(rArray == null || rArray.length == 0)
                throw new IllegalArgumentException("Method do not belong to any resource in the WADL.");
            inputParams.addAll(findWadlParams(m));
        } catch (Exception ex) {
        } 
        
        //Further differentiate fixed, api-key for query parameters
        String apiKeyName = null;
        String sessionKeyName = null;
        boolean checkApiKey = false;
        boolean isSessionKey = getAuthenticationType() == SaasAuthenticationType.SESSION_KEY;
        if(isSessionKey)
            sessionKeyName = ((SessionKeyAuthentication)getAuthentication()).getSessionKeyName();
        if(getAuthenticationType() == SaasAuthenticationType.API_KEY)
            apiKeyName = ((ApiKeyAuthentication)getAuthentication()).getApiKeyName();
        if(isSessionKey)
            apiKeyName = ((SessionKeyAuthentication)getAuthentication()).getApiKeyName();
        if(apiKeyName != null)
            checkApiKey = true;
        for (ParameterInfo param : inputParams) {
            String paramName = param.getName();
            if(param.getStyle() == ParamStyle.QUERY) {
                if(checkApiKey && paramName.equals(apiKeyName)) {
                    param.setIsApiKey(true);
                }
                if(isSessionKey && paramName.equals(sessionKeyName)) {
                    param.setIsSessionKey(true);
                }
            }
        }
        return inputParams;
    }
    
    public static ArrayList<ParameterInfo> findWadlParams(WadlSaasMethod wsm) {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        inputParams.addAll(findWadlParams(wsm.getWadlMethod()));
        WadlSaasResource wParentResource = wsm.getParentResource();
        Resource parentResource = null;
        if(wParentResource != null) {
            parentResource = wParentResource.getResource();
        } else {
            Resource[] rPaths = wsm.getResourcePath();
            if(rPaths != null && rPaths.length > 0)
                parentResource = rPaths[rPaths.length-1];
        }
        if(parentResource != null)
            findWadlParams(inputParams, parentResource.getParam());
        return inputParams;
    }
    
    public static ArrayList<ParameterInfo> findWadlParams(Method wm) {
        ArrayList<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        Request req = wm.getRequest();
        findWadlParams(inputParams, req.getParam());
        List<RepresentationType> reps = req.getRepresentation();
        for(RepresentationType rep:reps) {
            findWadlParams(inputParams, rep.getParam());
        }
        return inputParams;
    }

    private void initMimeTypes() {
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        try {
            Response response = m.getWadlMethod().getResponse();
            findMediaType(response, mimeTypes);
            if(mimeTypes.size() > 0)
                this.setMimeTypes(mimeTypes.toArray(new MimeType[mimeTypes.size()]));
        } catch (Exception ex) {
        } 
    }
    
    public String getUrl() {
        return this.url;
    }

    public static void findMediaType(Response response, List<MimeType> mimeTypes) {
        List repOrFaults = response.getRepresentationOrFault();
        for(Object repOrFault: repOrFaults) {
            if(repOrFault instanceof RepresentationType) {
                RepresentationType rep = (RepresentationType) repOrFault;
                String mediaType = rep.getMediaType();
                String[] mTypes = mediaType.split(",");
                for(String m:mTypes) {
                    MimeType mType = MimeType.find(m);
                    if (mType != null) {
                        mimeTypes.add(mType);
                    }
                }
            }
        }
    }

    public static void findWadlParams(List<ParameterInfo> paramInfos, List<Param> params) {
        if (params != null) {
            for (Param param:params) {
                //<param name="replace" type="xsd:boolean" style="query" required="false" default="some value">
                String paramName = param.getName();
                Class paramType = findJavaType(param.getType().getLocalPart());
                Object defaultValue = param.getDefault();
                ParameterInfo paramInfo = new ParameterInfo(paramName, paramType);
                paramInfo.setStyle(ParamStyle.fromValue(param.getStyle().value()));
                paramInfo.setIsRequired(param.isRequired());
                paramInfo.setIsRepeating(param.isRepeating());
                paramInfo.setFixed(param.getFixed());
                paramInfo.setOption(param.getOption());
                paramInfo.setDefaultValue(defaultValue);
                paramInfos.add(paramInfo);
            }
        }
    }

    public String getSaasServiceTemplate() {
        return SAAS_SERVICE_TEMPLATE;
    }
}
