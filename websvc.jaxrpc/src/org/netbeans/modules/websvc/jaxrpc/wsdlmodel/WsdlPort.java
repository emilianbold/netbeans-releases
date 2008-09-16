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
import com.sun.xml.rpc.processor.model.Port;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSOperation;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSPort;

/**
 *
 * @author Roderico Cruz
 */
public class WsdlPort implements WSPort{

    private Port port;
    
    public WsdlPort(Port port){
        this.port = port;
    }
    public Object getInternalJAXWSPort() {
        return port;
    }

    public List<WSOperation> getOperations() {
        List<WSOperation> wsOperations = new ArrayList<WSOperation>();
        List<Operation> operations = port.getOperationsList();
        for(Operation operation : operations){
            wsOperations.add(new WsdlOperation(operation));
        }
        return wsOperations;
    }

    public String getName() {
        return port.getName().getLocalPart();
    }

    public String getNamespaceURI() {
        return port.getName().getNamespaceURI();
    }

    public String getJavaName() {
        return port.getJavaInterface().getName();
    }

    public String getPortGetter() {
        return "get" + camelize(getName(), false);
    }

    public String getSOAPVersion() {
        return "soap1.1";
    }

    public String getStyle() {
        List<Operation> operations = port.getOperationsList();
        if(operations.size() > 0){
            Operation operation = operations.get(0);
            return operation.getStyle().toString();
        }
        return null; 
    }

    public boolean isProvider() {
        return false;
    }

    public String getAddress() {
        return port.getAddress();
    }

    private String camelize(String word, boolean flag) {
        if (word.length() == 0) return word;

        StringBuffer sb = new StringBuffer(word.length());
        if (flag) {
            sb.append(Character.toLowerCase(word.charAt(0)));
        } else {
            sb.append(Character.toUpperCase(word.charAt(0)));
        }
        boolean capitalize = false;
        for (int i = 1; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (capitalize) {
                sb.append(Character.toUpperCase(ch));
                capitalize = false;
            } else if (ch == '_') {
                capitalize = true;
            } else if (ch == '/') {
                capitalize = true;
                sb.append('.');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();

    }

}
