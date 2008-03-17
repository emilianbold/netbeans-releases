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
package org.netbeans.modules.websvc.saas.codegen.java;

import org.netbeans.modules.websvc.saas.model.WadlSaasMethod;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.saas.codegen.java.Constants.SaasAuthenticationType;
import org.netbeans.modules.websvc.saas.codegen.java.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.java.model.WadlSaasBean;
import org.netbeans.modules.websvc.saas.codegen.java.support.Inflector;
import org.netbeans.modules.websvc.saas.codegen.java.support.Util;
import org.openide.filesystems.FileObject;

/**
 * Code generator for Accessing Saas services.
 *
 * @author ayubskhan
 */
public class JaxRsJspCodeGenerator extends JaxRsServletCodeGenerator {

    private JavaSource loginJS;
    private FileObject loginFile;
    private JavaSource callbackJS;
    private FileObject callbackFile;
    private Map<String, String> jspSpecialNamesMap = new HashMap<String, String>();
    
    public JaxRsJspCodeGenerator(JTextComponent targetComponent,
            FileObject targetFile, WadlSaasMethod m) throws IOException {
        super(targetComponent, targetFile, m);
        setSubresourceLocatorUriTemplate(findSubresourceLocatorUriTemplate());
        jspSpecialNamesMap.put("page", "page1");
    }
    
    /**
     *  Insert the Saas client call
     */
    @Override
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        Util.checkScanning();
        try {
            String code = "";
            code = "\n<%\n"; // NOI18n
            code += getCustomMethodBody()+"\n";
            code += "%>\n";// NOI18n
            insert(code, getTargetComponent(), true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String paramUse = "";
        String paramDecl = "";
        
        //Evaluate parameters (query(not fixed or apikey), header, template,...)
        List<ParameterInfo> filterParams = renameJspParameterNames(getServiceMethodParameters());//includes request, response also
        paramUse += Util.getHeaderOrParameterUsage(filterParams);
        filterParams = renameJspParameterNames(super.getServiceMethodParameters());
        paramDecl += getHeaderOrParameterDeclaration(filterParams);
        
        String methodBody = "";
        methodBody += "             try {\n";
        methodBody += paramDecl + "\n";
        methodBody += "                 String result = " + getSaasServicePackageName() + "." + getSaasServiceName() + "." + getSaasServiceMethodName() + "(" + paramUse + ");\n";
        methodBody += "                 System.out.println(\"The SaasService returned: \"+result);\n";
        methodBody += "             } catch (java.io.IOException ex) {\n";
        methodBody += "                 //java.util.logging.Logger.getLogger(this.getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);\n";
        methodBody += "                 ex.printStackTrace();\n";
        methodBody += "             }\n";
       
        return methodBody;
    }
    
    private List<ParameterInfo> renameJspParameterNames(List<ParameterInfo> params) {
        List<ParameterInfo> returnParams = new ArrayList<ParameterInfo>();
        int count = 1;
        for(ParameterInfo p:params) {
            String newName = jspSpecialNamesMap.get(getParameterName(p));
            if(newName != null) {
                ParameterInfo clone = clone(p, newName, p.getType());
                returnParams.add(clone);
            } else {
                returnParams.add(p);
            }
        }
        return returnParams;
    }
    
    private ParameterInfo clone(ParameterInfo p, String name, Class type) {
        ParameterInfo clone = new ParameterInfo(name, type);
        clone.setFixed(p.getFixed());
        clone.setStyle(p.getStyle());
        clone.setDefaultValue(p.getDefaultValue());
        clone.setIsApiKey(p.isApiKey());  
        clone.setId(p.getId());  
        clone.setIsRequired(p.isRequired());
        clone.setIsRepeating(p.isRepeating());
        clone.setIsSessionKey(p.isSessionKey());
        clone.setOption(p.getOption());
        return clone;
    }
    
    public String findSubresourceLocatorUriTemplate() {
        String subresourceLocatorUriTemplate = getAvailableUriTemplate();
        if (!subresourceLocatorUriTemplate.endsWith("/")) {
            //NOI18N
            subresourceLocatorUriTemplate += "/"; //NOI18N
        }
        return subresourceLocatorUriTemplate;
    }
    
    private String getAvailableUriTemplate() {
        String uriTemplate = Inflector.getInstance().camelize(getBean().getShortName(), true);
        return uriTemplate;
    }
}
