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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 * Meta model for generic REST resource class.
 *
 * @author nam
 */
public class GenericResourceBean {
    public static final String RESOURCE_SUFFIX = "Resource";
    
    public static final MimeType[] supportedMimeTypes = new MimeType[] {
        MimeType.XML,  // first one is default
        MimeType.JSON,
        MimeType.TEXT,
        MimeType.HTML
    };
    
    public static final HttpMethodType[] CONTAINER_METHODS = new HttpMethodType[] {
        HttpMethodType.GET, HttpMethodType.POST
    };
    
    public static final HttpMethodType[] ITEM_METHODS = new HttpMethodType[] {
        HttpMethodType.GET, HttpMethodType.PUT, HttpMethodType.DELETE
    };
    
    public static final HttpMethodType[] STAND_ALONE_METHODS = new HttpMethodType[] {
        HttpMethodType.GET, HttpMethodType.PUT 
    };
    
    public static final HttpMethodType[] CLIENT_CONTROL_METHODS = new HttpMethodType[] {
        HttpMethodType.GET, HttpMethodType.PUT, HttpMethodType.DELETE
    };
    
    private final String name;
    private String packageName;
    
    private final String uriTemplate;
    private String[] queryParams;
    private String[] queryParamTypes;
    private MimeType[] mimeTypes;
    private String[] representationTypes;
    private Set<HttpMethodType> methodTypes;
    private boolean privateFieldForQueryParam;
    private boolean generateUriTemplate = true;
    
    private List<GenericResourceBean> subResources;
    
    public GenericResourceBean(String name, String packageName, String uriTemplate) {
        this(name, packageName, uriTemplate, supportedMimeTypes, HttpMethodType.values());
    }
    
    public GenericResourceBean(String name, String packageName, String uriTemplate,
            MimeType[] mediaTypes, HttpMethodType[] methodTypes) {
        this(name, packageName, uriTemplate, mediaTypes, null, methodTypes);
    }
    
    public GenericResourceBean(String name, String packageName, String uriTemplate,
            MimeType[] mediaTypes, String[] representationTypes,
            HttpMethodType[] methodTypes) {
        this.name = name;
        this.packageName = packageName;
        this.uriTemplate = uriTemplate;
        this.methodTypes = new HashSet(Arrays.asList(methodTypes));
        this.subResources = new ArrayList<GenericResourceBean>();
        
        if (representationTypes == null) {
            representationTypes = new String[mediaTypes.length];
            for (int i=0; i<representationTypes.length; i++) {
                representationTypes[i] = String.class.getName();
            }
        }
        if (mediaTypes.length != representationTypes.length) {
            throw new IllegalArgumentException("Unmatched media types and representation types");
        }
        this.mimeTypes = mediaTypes;
        this.representationTypes = representationTypes == null ? new String[0] : representationTypes;
    }
    
    public static MimeType[] getSupportedMimeTypes() {
        return supportedMimeTypes;
    }
    
    public static String getDefaultRepresetationClass(MimeType mime) {
        if (mime == MimeType.XML ||
                mime == MimeType.TEXT ||
                mime == MimeType.HTML ||
                mime == MimeType.JSON) {
            return String.class.getName();
        }
        return String.class.getName();
    }
    
    public String getName() {
        return name;
    }
    
    public String getShortName() {
        return getShortName(name);
    }
    
    public static String getShortName(String name) {
        if (name.endsWith(RESOURCE_SUFFIX)) {
            return name.substring(0, name.length()-8);
        }
        return name;
    }
    
    public String getUriWhenUsedAsSubResource() {
        return Util.lowerFirstChar(getShortName())+"/";
    }
    
    public void setPackageName(String name) {
        packageName = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public MimeType[] getMimeTypes() {
        return mimeTypes;
    }
    
    public String[] getRepresentationTypes() {
        return representationTypes;
    }
    
    public Set<HttpMethodType> getMethodTypes() {
        return methodTypes;
    }
    
    public void setMethodTypes(HttpMethodType[] types) {
        methodTypes = new HashSet(Arrays.asList(types));
    }
    
    private String[] uriParams = null;
    public String[] getUriParams() {
        if (uriParams == null) {
            uriParams = getUriParams(uriTemplate);
        }
        return uriParams;
    }
    
    public static String[] getUriParams(String template) {
        if (template == null) {
            return new String[0];
        }
        String[] segments = template.split("/");
        List<String> res = new ArrayList<String>();
        for (String segment : segments) {
            if (segment.startsWith("{")) {
                if (segment.length() > 2 && segment.endsWith("}")) {
                    res.add(segment.substring(1, segment.length()-1));
                } else {
                    throw new IllegalArgumentException(template);
                }
            }
        }
        return res.toArray(new String[res.size()]);
    }
    
    public String getQualifiedClassName() {
        return getPackageName() + "." + getName();
    }
    
    public String[] getQueryParams() {
        if (queryParams == null) {
            queryParams = new String[0];
        }
        return queryParams;
    }
    
    public String[] getQueryParamTypes() {
        if (queryParamTypes == null) {
            queryParamTypes = new String[0];
        }
        return queryParamTypes;
    }
    
    public String[] getConstantParams() {
        return new String[0];
    }
    
    public String[] getConstantParamTypes() {
        return new String[0];
    }
    
    public Object[] getConstantParamValues() {
        return new Object[0];
    }
    
    public void setQueryParams(String[] queryParams, String[] types) {
        this.queryParams = queryParams;
        if (types == null) {
            queryParamTypes = new String[queryParams.length];
            for (int i=0; i< queryParams.length; i++) {
                queryParamTypes[i] = String.class.getName();
            }
        } else {
            if (types.length != queryParams.length) {
                throw new IllegalArgumentException("Unmatched arrays of parameter names and types");
            }
            this.queryParamTypes = types;
        }
    }
    
    public void addSubResource(GenericResourceBean bean) {
        this.subResources.add(bean);
    }
    
    public List<GenericResourceBean> getSubResources() {
        return subResources;
    }
    
    public boolean isPrivateFieldForQueryParam() {
        return privateFieldForQueryParam;
    }
    
    public void setPrivateFieldForQueryParam(boolean privateFieldForQueryParam) {
        this.privateFieldForQueryParam = privateFieldForQueryParam;
    }
    
    public boolean isGenerateUriTemplate() {
        return generateUriTemplate;
    }
    
    public void setGenerateUriTemplate(boolean flag) {
        this.generateUriTemplate = flag;
    }
}
