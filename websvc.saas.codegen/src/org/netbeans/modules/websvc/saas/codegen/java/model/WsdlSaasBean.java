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
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.HttpMethodType;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.MimeType;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;

/**
 * Model bean for code generation of JAXWS operation wrapper resource class.
 * 
 * @author nam
 */
public class WsdlSaasBean extends SaasBean {
    
    private JaxwsOperationInfo[] jaxwsInfos;
  
    public WsdlSaasBean(WsdlSaasMethod m, Project project) {
        this(deriveResourceName(m.getName()), 
                toJaxwsOperationInfos(m, project));
    }
  
    /**
     * Create a resource model bean for wrapper resource generation.
     * Note that the last JAXWS info is the principal one from which resource name, 
     * URI template and representation class is derived from.
     * @param jaxwsInfos array of JAXWS info objects.
     * @param packageName name of package
     */ 
    private WsdlSaasBean(String name, JaxwsOperationInfo[] jaxwsInfos) {
        super(name, 
              null,
              deriveUriTemplate(jaxwsInfos[jaxwsInfos.length-1].getOperationName()),
              deriveMimeTypes(jaxwsInfos), 
              new String[] { jaxwsInfos[jaxwsInfos.length-1].getOutputType() }, 
              new HttpMethodType[] { HttpMethodType.GET });
        this.jaxwsInfos = jaxwsInfos;
    }
      
    private static JaxwsOperationInfo[] toJaxwsOperationInfos(WsdlSaasMethod m, 
            Project project) {
        List<JaxwsOperationInfo> infos = new ArrayList<JaxwsOperationInfo>();
        infos.add(new JaxwsOperationInfo(m, project));
        
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

    public String getResourceClassTemplate() {
        return RESOURCE_TEMPLATE;
    }
}
