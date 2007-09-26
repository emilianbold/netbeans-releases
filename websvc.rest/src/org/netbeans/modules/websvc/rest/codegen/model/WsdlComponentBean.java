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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.Constants.HttpMethodType;
import org.netbeans.modules.websvc.rest.codegen.Constants.MimeType;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData.Method;

/**
 * Model bean for code generation of JAXWS operation wrapper resource class.
 * 
 * @author nam
 */
public class WsdlComponentBean extends RestComponentBean {
    
    private JaxwsOperationInfo[] jaxwsInfos;
  
    public WsdlComponentBean(RestComponentData data, Project project) {
        this(deriveResourceName(data.getService().getName()), 
                toJaxwsOperationInfos(data, project));
    }
  
    /**
     * Create a resource model bean for wrapper resource generation.
     * Note that the last JAXWS info is the principal one from which resource name, 
     * URI template and representation class is derived from.
     * @param jaxwsInfos array of JAXWS info objects.
     * @param packageName name of package
     */ 
    private WsdlComponentBean(String name, JaxwsOperationInfo[] jaxwsInfos) {
        super(name, 
              null,
              deriveUriTemplate(jaxwsInfos[jaxwsInfos.length-1].getOperationName()),
              deriveMimeTypes(jaxwsInfos), 
              new String[] { jaxwsInfos[jaxwsInfos.length-1].getOutputType() }, 
              new HttpMethodType[] { HttpMethodType.GET });
        this.jaxwsInfos = jaxwsInfos;
    }
      
    private static JaxwsOperationInfo[] toJaxwsOperationInfos(RestComponentData data, Project project) {
        List<JaxwsOperationInfo> infos = new ArrayList<JaxwsOperationInfo>();
        
        for (Method m : data.getService().getMethods()) {
            String service = m.getServiceName();
            String port = m.getPortName();
            infos.add(new JaxwsOperationInfo(service, port, m.getName(), m.getUrl(), project));
        }
        
        return infos.toArray(new JaxwsOperationInfo[infos.size()]);
    }
    
    private static MimeType[] deriveMimeTypes(JaxwsOperationInfo[] operations) {
        if (String.class.getName().equals(operations[operations.length-1].getOperation().getReturnTypeName())) {
            return new MimeType[] { MimeType.HTML };
        } else {
            return new MimeType[] { MimeType.XML };//TODO  MimeType.JSON };
        }
    }
    
    protected List<ParameterInfo> initInputParameters() {
        List<ParameterInfo> inputParams = new ArrayList<ParameterInfo>();
        
        for(JaxwsOperationInfo info : jaxwsInfos) {
            String[] names = info.getInputParameterNames();
            Class[] types = info.getInputParameterTypes();
            
            for (int i=0; i<names.length; i++) {
                inputParams.add(new ParameterInfo(names[i], types[i]));
            }
        }
        
        return inputParams;
    }
    
    @Override
    public String[] getOutputTypes() {
        String[] types = new String[jaxwsInfos.length];
        for (int i=0; i<jaxwsInfos.length; i++) {
            types[i] = jaxwsInfos[i].getOutputType();
        }
        return types;
    }
    
    public JaxwsOperationInfo[] getOperationInfos() {
        return jaxwsInfos;
    }

    @Override
    public List<ParameterInfo> getHeaderParameters() {
        HashMap<QName,ParameterInfo> params = new HashMap<QName,ParameterInfo>();
        for (JaxwsOperationInfo info : getOperationInfos()) {
            for (ParameterInfo pinfo : info.getSoapHeaderParameters()) {
                params.put(pinfo.getQName(), pinfo);
            }
        }
        return new ArrayList<ParameterInfo>(params.values());
    }

    @Override
    public String getOutputWrapperName() {
        if (needsHtmlRepresentation()) {
            return null;
        }
        return super.getOutputWrapperName();
    }

    @Override
    public String getOutputWrapperPackageName() {
        if (needsHtmlRepresentation()) {
            return null;
        }
        return super.getOutputWrapperPackageName();
    }
    
    public boolean needsHtmlRepresentation() {
        return getOperationInfos().length > 0 && 
               String.class.getName().equals(lastOperationInfo().getOperation().getReturnTypeName());
    }
    
    public JaxwsOperationInfo lastOperationInfo() {
        return getOperationInfos()[getOperationInfos().length-1];
    }
    
}
