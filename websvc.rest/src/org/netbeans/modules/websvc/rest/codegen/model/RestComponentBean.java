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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.support.Inflector;

/**
 *
 * @author Peter Liu
 */
public abstract class RestComponentBean extends GenericResourceBean {

    private static final String RESOURCE_TEMPLATE = "Templates/WebServices/WrapperResource.java"; //NOI18N
    private String outputWrapperName;
    private String wrapperPackageName;
    private List<ParameterInfo> inputParams;
    private List<ParameterInfo> queryParams;

    public RestComponentBean(String name, String packageName, String uriTemplate, MimeType[] mediaTypes, String[] representationTypes, HttpMethodType[] methodTypes) {
        super(name, packageName, uriTemplate, mediaTypes, representationTypes, methodTypes);
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

    protected abstract List<ParameterInfo> initInputParameters();

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
            outputWrapperName += EntityResourcesGenerator.CONVERTER_SUFFIX;
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