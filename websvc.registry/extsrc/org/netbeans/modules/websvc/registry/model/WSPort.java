/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.registry.model;

import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.java.JavaMethod;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is meant to hold the pertinent data from the class
 * com.sun.xml.rpc.processor.model.Port.  This class will serve as a
 * JavaBean.  This class will be persisted with the WebServiceData.
 * @author  David Botterill
 */
public class WSPort {

    private String name;
    private String address;
    private String javaInterfaceName;
    private ArrayList methods = new ArrayList();
    
    
    /** Creates a new instance of WSPort */
    public WSPort() {
    }
    
    public WSPort(Port inPort) {
        if(null == inPort) return;
        this.address = inPort.getAddress();
        this.javaInterfaceName = inPort.getJavaInterface().getRealName();
        this.name = inPort.getName().getLocalPart();
        Iterator opIter = inPort.getOperations();
        while(opIter.hasNext()){
            JavaMethod javaMethod = (JavaMethod)((Operation)opIter.next()).getJavaMethod();
            this.addMethod(javaMethod);
        }
        
    }
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Getter for property address.
     * @return Value of property address.
     */
    public java.lang.String getAddress() {
        return address;
    }
    
    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }
    
    /**
     * Getter for property javaInterfaceName.
     * @return Value of property javaInterfaceName.
     */
    public java.lang.String getJavaInterfaceName() {
        return javaInterfaceName;
    }
    
    /**
     * Setter for property javaInterfaceName.
     * @param javaInterfaceName New value of property javaInterfaceName.
     */
    public void setJavaInterfaceName(java.lang.String javaInterfaceName) {
        this.javaInterfaceName = javaInterfaceName;
    }
    
    public void addMethod(JavaMethod inMethod) {
        methods.add(inMethod);
    }
    /**
     * Getter for property methods.
     * @return Value of property methods.
     */
    public java.util.ArrayList getMethods() {
        return methods;
    }
    
    /**
     * Setter for property methods.
     * @param methods New value of property methods.
     */
    public void setMethods(java.util.ArrayList methods) {
        this.methods = methods;
    }
    
}
