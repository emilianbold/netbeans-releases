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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientOperationInfo;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;
import org.netbeans.modules.websvc.saas.codegen.model.SoapClientSaasBean;
import org.openide.filesystems.FileObject;

/**
 * Code generator for REST services wrapping WSDL-based web service.
 *
 * @author ayubkhan
 */
public class SoapClientPojoCodeGenerator extends SoapClientRestResourceCodeGenerator {
        
    public SoapClientPojoCodeGenerator() {
        super();
    }

    @Override
    public SoapClientSaasBean getBean() {
        return (SoapClientSaasBean) super.getBean();
    }
    
    @Override
    public Set<FileObject> generate() throws IOException {
        preGenerate();
        
        insertSaasServiceAccessCode(isInBlock(getTargetDocument()));
        //addImportsToTargetFile();
        
        finishProgressReporting();

        return new HashSet<FileObject>(Collections.EMPTY_LIST);
    }
    
    @Override
    protected String getCustomMethodBody() throws IOException {
        String methodBody = INDENT + "try {\n";
        for (ParameterInfo param : getBean().getQueryParameters()) {
            String name = param.getName();
            methodBody += INDENT_2 + param.getType().getName() + " " + name + " = "+
                    resolveInitValue(param)+"\n";
        }
        SoapClientOperationInfo[] operations = getBean().getOperationInfos();
        for (SoapClientOperationInfo info : operations) {
            methodBody += getWSInvocationCode(info);
        }
        methodBody += INDENT + "} catch (Exception ex) {\n";
        methodBody += INDENT_2 + "ex.printStackTrace();\n";
        methodBody += INDENT + "}\n";
        return methodBody;
    }
    
    /**
     *  Insert the Saas client call
     */
    protected void insertSaasServiceAccessCode(boolean isInBlock) throws IOException {
        try {
            String code = "";
            if(isInBlock) {
                code = getCustomMethodBody();
            } else {
                code = "\nprivate String call"+getBean().getName()+"Service() {\n";
                code += getCustomMethodBody()+"\n";
                code += "return result;\n";
                code += "}\n";
            }
            insert(code, true);
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
}
