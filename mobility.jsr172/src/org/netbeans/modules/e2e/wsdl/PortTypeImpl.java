/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.PortType;

/**
 *
 * @author Michal Skvor
 */
public class PortTypeImpl implements PortType {
    
    private Map<String, Operation> operations;
    private QName myName;
    
    /** Creates a new instance of PortTypeImpl */
    public PortTypeImpl(QName name) {
        myName = name;
        operations = new HashMap<String, Operation>();
    }

    @Override
    public String getName() {
        return getQName().getLocalPart();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.e2e.api.wsdl.PortType#getQName()
     */
    @Override
    public QName getQName() {
        return myName;
    }

    @Override
    public void addOperation(Operation operation) {
        Operation op = operations.get(operation.getJavaName());
        if (op != null) {
            String name = operation.getName();
            if (name != null && name.length() != 0) {
                String newJavaName = generateJavaName(operation);
                operation.setJavaName(newJavaName);
            }
        }
        op = operations.get(getUpperCaseName(operation));
        if (op != null) {
            String name = op.getName();
            if (name != null && name.length() != 0) {
                String newJavaName = generateJavaName(op);
                op.setJavaName(newJavaName);
            }
        }
        operations.put(operation.getName(), operation);
    }

    private String generateJavaName(Operation op) {
        String name = op.getName();
        int count = 1;
        String newName = name + count;
        while (operations.containsKey(OperationImpl.toJavaName(newName))) {
            count++;
            newName = name + count;
        }
        return OperationImpl.toJavaName(newName);
    }

    @Override
    public List<Operation> getOperations() {
        return Collections.unmodifiableList(new ArrayList(operations.values()));
    }

    public String getUpperCaseName(Operation op) {
        String name = op.getName();
        if (name.length() > 1) {
            return Character.toUpperCase(name.charAt(0))
                    + name.substring(1);
        }
        return name;
    }

    public static class PortTypeReferenceImpl extends PortTypeImpl
            implements PortTypeReference {

        public PortTypeReferenceImpl(QName name) {
            super(name);
        }


        /* (non-Javadoc)
         * @see org.netbeans.modules.e2e.api.wsdl.PortType.PortTypeReference#isValid()
         */
        @Override
        public boolean isValid() {
            return false;
        }
    }

}
