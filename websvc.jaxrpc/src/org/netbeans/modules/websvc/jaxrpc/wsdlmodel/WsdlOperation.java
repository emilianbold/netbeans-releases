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

import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.java.JavaException;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.jaxwsmodelapi.java.JavaMethod;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlOperation implements WSOperation{

    private Operation operation;
    
    public WsdlOperation(Operation operation){
        this.operation = operation;
    }
    public Object getInternalJAXWSOperation() {
        return operation;
    }

    public JavaMethod getJavaMethod() {
        com.sun.xml.rpc.processor.model.java.JavaMethod javaMethod = operation.getJavaMethod();
        if(javaMethod != null){
            return new WsdlJavaMethod(javaMethod);
        }
        return null;
    }

    public String getName() {
        return operation.getName().getLocalPart();
    }

    public String getJavaName() {
        return operation.getJavaMethod().getName();
    }

    public String getReturnTypeName() {
        return operation.getJavaMethod().getReturnType().getName();
    }

    public List<WSParameter> getParameters() {
        List<WSParameter> wsParameters = new ArrayList<WSParameter>();
        List<JavaParameter> javaParameters = operation.getJavaMethod().getParametersList();
        for(JavaParameter javaParameter : javaParameters){
            wsParameters.add(new WsdlParameter(javaParameter));
        }
        return wsParameters;
    }

    public Iterator<String> getExceptions() {
        List<String> exceptions = new ArrayList<String>();
        List<JavaException> javaExceptions = operation.getJavaMethod().getExceptionsList();
        for(JavaException javaException : javaExceptions){
            exceptions.add(javaException.getFormalName());
        }
        return exceptions.iterator();
    }

    public int getOperationType() {
        return TYPE_NORMAL;
    }

    public String getOperationName() {
        return operation.getName().getLocalPart();
    }

}
