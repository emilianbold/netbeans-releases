/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.websvc.jaxrpc.wsdlmodel;

import com.sun.xml.rpc.processor.model.java.JavaException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaType;


/**
 *
 * @author rico
 */
public class WsdlJavaMethod implements JavaMethod{
   private com.sun.xml.rpc.processor.model.java.JavaMethod method;

   public WsdlJavaMethod(com.sun.xml.rpc.processor.model.java.JavaMethod method){
       this.method = method;
   }
    public Object getInternalJAXWSJavaMethod() {
        return method;
    }

    public String getName() {
        return method.getName();
    }

    public JavaType getReturnType() {
        com.sun.xml.rpc.processor.model.java.JavaType javaType = method.getReturnType();
        return new WsdlJavaType(javaType);
    }

    public boolean hasParameter(String paramName) {
        return method.hasParameter(paramName);
    }

    public JavaParameter getParameter(String paramName) {
        List<com.sun.xml.rpc.processor.model.java.JavaParameter> javaParameters = method.getParametersList();
        for(com.sun.xml.rpc.processor.model.java.JavaParameter javaParameter : javaParameters){
            if(javaParameter.getName().equals(paramName)){
                return new WsdlJavaParameter(javaParameter);
            }
        }
        return null;
    }

    public List<JavaParameter> getParametersList() {
        List<JavaParameter> javaParameters = new ArrayList<JavaParameter>();
        List<com.sun.xml.rpc.processor.model.java.JavaParameter> parameters = method.getParametersList();
        for(com.sun.xml.rpc.processor.model.java.JavaParameter parameter : parameters){
             javaParameters.add(new WsdlJavaParameter(parameter));
        }
        return javaParameters;
    }

    public Iterator getExceptions() {
        List<String> exceptions = new ArrayList<String>();
        List<JavaException> javaExceptions = method.getExceptionsList();
        for(JavaException javaException : javaExceptions){
            exceptions.add(javaException.getName());
        }
        return exceptions.iterator();
    }
}
