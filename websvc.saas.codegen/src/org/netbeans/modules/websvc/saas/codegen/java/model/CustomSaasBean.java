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
import org.netbeans.modules.websvc.saas.codegen.java.AbstractGenerator;
import org.netbeans.modules.websvc.saas.model.CustomSaasMethod;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifact;
import org.netbeans.modules.websvc.saas.model.jaxb.Artifacts;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Input;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output;
import org.netbeans.modules.websvc.saas.model.jaxb.Method.Output.Media;
import org.netbeans.modules.websvc.saas.model.jaxb.Params.Param;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasMetadata.CodeGen;

/**
 *
 * @author ayubkhan
 */
public class CustomSaasBean extends SaasBean {

    public static final String ARTIFACT_TYPE_TEMPLATE = "template";
    private String url;
    private CustomSaasMethod m;
    private List<ParameterInfo> inputParams;
    private List<String> templates;
    private List<String> libs;
    
    public CustomSaasBean(CustomSaasMethod m)  throws IOException {
        super(deriveResourceName(m), null, 
                deriveUriTemplate(m), new MimeType[]{MimeType.XML}, 
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.m = m;
        
        if(m.getHref() != null)
            setResourceClassTemplate(m.getHref());
        
        inputParams = new ArrayList<ParameterInfo>();
        List<MimeType> mimeTypes = new ArrayList<MimeType>();
        try {
            Input in = m.getInput();
            if(in != null && in.getParams() != null && in.getParams().getParam() != null) {
            List<Param> params = in.getParams().getParam();
            findParams(inputParams, params);
        }
        Output out = m.getOutput();
        findMediaType(mimeTypes, out.getMedia());

            if(mimeTypes.size() > 0)
                this.setMimeTypes(mimeTypes.toArray(new MimeType[mimeTypes.size()]));
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
        
        templates = new ArrayList<String>();
        libs = new ArrayList<String>();
        CodeGen codegen = m.getSaas().getSaasMetadata().getCodeGen();
        if(codegen != null) {
            List<Artifacts> artifactsList = codegen.getArtifacts();
            if(artifactsList != null) {
                for(Artifacts artifacts: artifactsList) {
                    List<Artifact> artifactList = artifacts.getArtifact();
                    if(artifactList != null) {
                        for(Artifact artifact: artifactList) {
                            if(artifact.getRequires() != null) {
                                //TODO
                            }
                            String type = artifact.getType();
                            if(type == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@type value is null.");
                            String artifactUrl = artifact.getUrl();
                            if(artifactUrl == null)
                                throw new IOException("saas-metadata/code-gen/artifacts/artifact/@url value is null.");
                            if(type.equals(ARTIFACT_TYPE_TEMPLATE)) {
                                templates.add(artifactUrl);
                                if(getResourceClassTemplate().equals(artifact.getId()))
                                    setResourceClassTemplate(artifactUrl);
                            } else if(type.equals(ARTIFACT_TYPE_TEMPLATE)) {
                                libs.add(artifactUrl);
                            }
                        }
                    }
                }
            }
        }
    }

    protected List<ParameterInfo> initInputParameters() {
        return inputParams;
    }
    
    public String getUrl() {
        return this.url;
    }

    protected static String deriveResourceName(CustomSaasMethod m) {
        String name = m.getName();
        if(m.getHref() != null && !m.getHref().trim().equals(""))
            name = m.getHref();
        return Inflector.getInstance().camelize(name + GenericResourceBean.RESOURCE_SUFFIX);
    }

    protected static String deriveUriTemplate(CustomSaasMethod m) {
        String name = m.getName();
        if(m.getHref() != null && !m.getHref().trim().equals(""))
            name = m.getHref();
        return Inflector.getInstance().camelize(name, true) + "/"; //NOI18N
    }

    private void findMediaType(List<MimeType> mimeTypes, Media media) {
        String mediaType = media.getType();
        String[] mTypes = mediaType.split(",");
        for(String m1:mTypes) {
            MimeType mType = MimeType.find(m1);
            if (mType != null) {
                mimeTypes.add(mType);
            }
        }
    }

    private void findParams(List<ParameterInfo> paramInfos, List<Param> params) {
        if (params != null) {
            for (Param param:params) {
                String paramName = param.getName();
                Class paramType = findJavaType(param.getType());
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
            
            if(schemaType.equalsIgnoreCase("string")) {     //NOI18N
                return String.class;
            } else if(schemaType.equals("int") || schemaType.equals("Integer")) {       //NOI18N
                return Integer.class;
            }
        }
        
        return String.class;
    }
    
    public List<String> getArtifactLibs() {
        return libs;
    }
    
    public void setArtifactLibs(List<String> libs) {
        this.libs = libs;
    }
    
    public List<String> getArtifactTemplates() {
        return templates;
    }
    
    public void setArtifactTemplates(List<String> templates) {
        this.templates = templates;
    }
}
