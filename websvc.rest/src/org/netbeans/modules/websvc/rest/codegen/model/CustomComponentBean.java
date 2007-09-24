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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData.Method;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData.Parameter;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Model bean for code generation of JAXWS operation wrapper resource class.
 * 
 * @author nam
 */
public class CustomComponentBean extends RestComponentBean {
    private String url;
    private RestComponentData data;
    private List<ParameterInfo> inputParams;
    
    public CustomComponentBean(RestComponentData data) throws IOException {
        super(deriveResourceName(data.getService().getName()), null, 
                deriveUriTemplate(data.getService().getName()), 
                new MimeType[]{MimeType.XML},
                new String[]{"java.lang.String"},       //NOI18N
                new HttpMethodType[]{HttpMethodType.GET});
    
        this.data = data;
       
        init();
    }

    private void init() throws IOException {
        inputParams = new ArrayList<ParameterInfo>();
      
        try {
            Method m = data.getService().getMethods().get(0);
            this.url = m.getUrl();
            List<Parameter> params = m.getInputParams();
            
            for (Parameter param : params) {
                Class type = findJavaType(param.getType());
                ParameterInfo paramInfo = new ParameterInfo(param.getName(), type);
                paramInfo.setDefaultValue(getDefaultValue(param.getDefaultValue(), type));
                
                inputParams.add(paramInfo);
            }
            
            MimeType mime = MimeType.find(m.getMediaType());
            if (mime == null) {
                mime = MimeType.TEXT;
            }

            this.setMimeTypes(new MimeType[] {mime});
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        }   
    }

    protected List<ParameterInfo> initInputParameters() {
        return inputParams;
    }
    
    public String getUrl() {
        return url;
    }

    @Override
    public String getResourceClassTemplate() {
        return url;
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
}
