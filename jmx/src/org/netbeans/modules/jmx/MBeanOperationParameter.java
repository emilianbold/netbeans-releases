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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx;

import javax.lang.model.type.TypeMirror;

/**
 * Class which gives the structure of an MBean operation parameter
 *
 */
public class MBeanOperationParameter {

    private String paramName        = "";// NOI18N
    private String paramType        = "";// NOI18N
    private String paramDescription = "";// NOI18N
    private TypeMirror mirror;
    /**
     * Constructor
     * @param paramName the parameter name
     * @param paramType the parameter type
     * @param paramDescription the parameter description
     */
    public MBeanOperationParameter(String paramName, String paramType, 
            String paramDescription) {
        
        this(paramName,paramType, paramDescription, null);
    }
    /**
     * Constructor
     * @param paramName the parameter name
     * @param paramType the parameter type
     * @param paramDescription the parameter description
     */
    public MBeanOperationParameter(String paramName, String paramType, 
            String paramDescription, TypeMirror mirror) {
        
        this.paramName = paramName;
        this.paramType = paramType;
        this.paramDescription = paramDescription;
        this.mirror = mirror;
    }
    
    public TypeMirror getTypeMirror() {
        return mirror;
    }
    
    /**
     * Sets the parameter name
     * @param name the name to set for this parameter
     */
    public void setParamName(String name) {
        this.paramName = name;
    }
    
    /**
     * Method which returns the name of the parameter
     * @return String the name of the parameter
     *
     */
    public String getParamName() {
        return paramName;
    }
    
    /**
     * Sets the parameter type
     * @param type the parameter type to set
     */
    public void setParamType(String type) {
        this.paramType = type;
    }
    
    /**
     * Method which returns the type of the parameter
     * @return String the type of the parameter
     *
     */
    public String getParamType() {
        return paramType;
    }
    
    /**
     * Sets the parameter description
     * @param descr the parameter description to set
     */
    public void setParamDescription(String descr) {
        this.paramDescription = descr;
    }
    
    /**
     * Method which returns the description of the parameter
     * @return String the description of the parameter
     *
     */
    public String getParamDescription() {
        return paramDescription;
    }
    
}
