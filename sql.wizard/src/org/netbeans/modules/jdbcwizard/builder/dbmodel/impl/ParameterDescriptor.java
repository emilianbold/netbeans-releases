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

/*
 *
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

public class ParameterDescriptor extends DBObjectImpl {
    private int ordinalPosition = 0;

    private String paramType = "";

    public String getJavaType() {
        return this.javaType;
    }

    private String javaType = "";

    /**
     * * constructor
     */
    public ParameterDescriptor() {
    }

    /**
     * getter for paramType; *
     * 
     * @return paramType;
     */
    public String getParamType() {
        return this.paramType;
    }

    /**
     * setter for paramType; *
     * 
     * @param paramType parameter type;
     */
    public void setParamType(final String paramType) {
        this.paramType = paramType;
    }

    /**
     * getter for ordinalPosition; *
     * 
     * @return ordinalPosition;
     */
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }

    /**
     * setter for ordinalPosition; *
     * 
     * @param ordinalPosition position of the parameter;
     */
    public void setOrdinalPosition(final int ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public void setJavaType(final String s) {
        this.javaType = s;
    }

    public void setNumericScale(final int i) {
    }

    public void setSQLType(final String sqlType) {
    }
}
