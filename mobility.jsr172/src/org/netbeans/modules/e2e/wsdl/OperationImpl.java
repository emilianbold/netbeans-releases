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
import org.netbeans.modules.e2e.api.wsdl.Fault;
import org.netbeans.modules.e2e.api.wsdl.Input;
import org.netbeans.modules.e2e.api.wsdl.Operation;
import org.netbeans.modules.e2e.api.wsdl.Output;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 */
public class OperationImpl implements Operation {
    
    private String name;
    private Output output;
    private Input input;
    private Map<String, Fault> faults;
    private String documentation;
    private String myJavaName;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of OperationImpl */
    public OperationImpl(String name) {
        this.name = name;
        faults = new HashMap();
        extensibilityElements = new ArrayList();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJavaName() {
        if (myJavaName == null) {
            myJavaName = toJavaName(getName());
        }
        return myJavaName;
    }

    @Override
    public void setJavaName(String name) {
        myJavaName = name;
    }

    @Override
    public void setOutput(Output output) {
        this.output = output;
    }

    @Override
    public Output getOutput() {
        return output;
    }

    @Override
    public void setInput(Input input) {
        this.input = input;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public void addFault(Fault fault) {
        faults.put(fault.getName(), fault);
    }

    @Override
    public Fault getFault(String name) {
        return faults.get(name);
    }

    @Override
    public List<Fault> getFaults() {
        return Collections.unmodifiableList(new ArrayList(faults.values()));
    }

    @Override
    public void addExtensibilityElement(ExtensibilityElement extensibilityElement) {
        extensibilityElements.add(extensibilityElement);
    }

    @Override
    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList(extensibilityElements);
    }

    @Override
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public String getDocumentation() {
        return documentation;
    }

    public static String toJavaName(String name) {
        if (name.length() > 1) {
            return Character.toLowerCase(name.charAt(0))
                    + name.substring(1);
        }
        return name;
    }

}
