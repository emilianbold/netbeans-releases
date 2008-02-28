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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import org.netbeans.modules.websvc.saas.model.wadl.Param;
import org.netbeans.modules.websvc.saas.model.wadl.RepresentationType;
import org.netbeans.modules.websvc.saas.model.wadl.Request;
import org.netbeans.modules.websvc.saas.model.wadl.Resource;
import org.netbeans.modules.websvc.saas.model.wadl.Response;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.JaxRsCodeGenerator;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;

/**
 *
 * @author ayubkhan
 */
public class WadlSaasBean extends GenericResourceBean {

    private static final String RESOURCE_TEMPLATE = "Templates/SaaSServices/WrapperResource.java"; //NOI18N
    private String outputWrapperName;
    private String wrapperPackageName;
    private List<ParameterInfo> queryParams;
    private String url;
    private WadlSaasMethod m;
    private List<ParameterInfo> inputParams;
    
    public WadlSaasBean(WadlSaasMethod m)  throws IOException {
        super(deriveResourceName(m.getName()), null, 
                deriveUriTemplate(m.getName()), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.m = m;
       
        inputParams = new ArrayList<ParameterInfo>();
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        try {
            Resource[] rArray = m.getResourcePath();
            Resource currResource = rArray[rArray.length-1];
            String url2 = "";//m.getSaas().getWadlModel().getResources().getBase();
            for(Resource r:rArray) {
                url2 += "/"+r.getPath();
            }
            this.url = url2;
            
            findParams(inputParams, currResource.getParam());
            Request req = m.getWadlMethod().getRequest();
            findParams(inputParams, req.getParam());
            Response response = m.getWadlMethod().getResponse();
            findMediaType(response, mimeTypes);

            if(mimeTypes.size() > 0)
                this.setMimeTypes(mimeTypes.toArray(new MimeType[mimeTypes.size()]));
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
    }

    protected List<ParameterInfo> initInputParameters() {
        return inputParams;
    }
    
    public String getUrl() {
        return this.url;
    }

    private void findMediaType(Response response, List<MimeType> mimeTypes) {
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

    private void findParams(List<ParameterInfo> paramInfos, List<Param> params) {
        if (params != null) {
            for (Param param:params) {
                String paramName = param.getName();
                Class paramType = findJavaType(param.getType().getLocalPart());
                Object defaultValue = param.getDefault();
                ParameterInfo paramInfo = new ParameterInfo(paramName, paramType);
                paramInfo.setDefaultValue(defaultValue);
                paramInfos.add(paramInfo);
            }
        }
    }
       
    private Class findJavaType(String schemaType) {       
        if(schemaType != null) {
            int index = schemaType.indexOf(":");        //NOI18N
            
            if(index != -1) {
                schemaType = schemaType.substring(index+1);
            }
            
            if(schemaType.equals("string")) {     //NOI18N
                return String.class;
            } else if(schemaType.equals("int")) {       //NOI18N
                return Integer.class;
            }
        }
        
        return String.class;
    }
    
    private Object getDefaultValue(String value, Class type) {
        if (type == String.class) {
            return value;
        } else if (type == Integer.class) {
            return new Integer(Integer.parseInt(value));
        }
        
        return null;
    }

    public void setInputParameters(List<ParameterInfo> inputParams) {
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

    @Override
    public List<ParameterInfo> getQueryParameters() {
        if (queryParams == null) {
            queryParams = new ArrayList<ParameterInfo>();

            for (ParameterInfo param : getInputParameters()) {
                if (param.isQueryParam()) {
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
            outputWrapperName += JaxRsCodeGenerator.CONVERTER_SUFFIX;
        }
        return outputWrapperName;
    }

    public String getOutputWrapperPackageName() {
        return wrapperPackageName;
    }

    public void setOutputWrapperPackageName(String packageName) {
        wrapperPackageName = packageName;
    }

    protected static String deriveResourceName(String componentName) {
        return Inflector.getInstance().camelize(componentName + GenericResourceBean.RESOURCE_SUFFIX);
    }

    protected static String deriveUriTemplate(String name) {
        return Inflector.getInstance().camelize(name, true) + "/"; //NOI18N
    }

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
        return RESOURCE_TEMPLATE;
    }
}
