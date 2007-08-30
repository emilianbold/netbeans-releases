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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
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
        return Inflector.getInstance().camelize(name, true) + "/";     //NOI18N
    }

    public String[] getRepresentationTypes() {
        return new String[]{getOutputWrapperPackageName() + "." + getOutputWrapperName()}; //NOI18N
    }

    public String[] getOutputTypes() {
        String[] types = new String[]{"java.lang.String"}; //NOI18N
        return types;
    }
}