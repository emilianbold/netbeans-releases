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

/**
 * This class is required since the OTD seed is generated as Procedure1, Procedure2
 * and inside WSDLGenerator there is no way of distinguishing between PreparedStatement and stored procedure DBObjects.
 * When adding the objects inside WSDLGenerator, specific types are added and later checked for.
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel.impl;

import org.netbeans.modules.jdbcwizard.builder.dbmodel.DBObject;
import java.util.ArrayList;
import java.util.List;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class DBProcObject extends DBObjectImpl {

    private ArrayList parameters = null;

    private int numParameters = 0;

    // added by anupam
    private ArrayList procResultSetDesc = null;

    private int numResultSets = 0;

    public static final String OTDSEED_RESULTSETID_PATTERN = "Procedure{0}$ResultSets{1}";

    public static final String OTDSEED_PROCRSCOLID_PATTERN = "Procedure{0}$ResultSets{1}$Column_{2}";

    // added till here

    public static final String OTDSEED_PROCID_PATTERN = "Procedure{0}";

    public static final String OTDSEED_PROCPARAMID_PATTERN = "Procedure{0}Param{1}";

    /* No-arg constructor; initializes Collections-related member variables. */
    public DBProcObject() {
        this.parameters = new ArrayList();
        this.procResultSetDesc = new ArrayList();
    }

    /**
     * Creates a new instance of DBTableImpl, cloning the contents of the given DBTable
     * implementation instance.
     * 
     * @param src DBTable instance to be cloned
     */
    public DBProcObject(final DBObject src) {
        this();
        if (src == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(DBProcObject.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_DBTABLE") + "ERROR_NULL_DBTABLE");// NO
            // i18n
        }

        super.copyFrom(src);
    }

    public DBProcObject(final String objectName, final String schema, final String catalog) {
        super(objectName, schema, catalog);
    }

    public void setParameters(final ArrayList parameters) {
        this.parameters = parameters;
    }

    public List getParameters() {
        return this.parameters;
    }

    public Object getProcOutputDesc() {
        return null;
    }

    public void setNumParameters(final int numParameters) {
        this.numParameters = numParameters;
    }

    public int getNumParameters() {
        return this.numParameters;
    }

    /**
     * returns the list of ResultSet descriptors
     * 
     * @return procResultSetDesc
     */
    public ArrayList getProcResultSetDesc() {
        return this.procResultSetDesc;
    }

    /**
     * anupam added sets resultSet descriptor.
     * 
     * @param procrsd List containing the ResultSetDescriptor
     */
    public void setProcResultSetDesc(final ArrayList procrsd) {
        this.procResultSetDesc = procrsd;
    }

    /**
     * added anupam Sets the count of Result Set
     * 
     * @param numResultSets count of ResultSet
     */
    public void setNumResultSets(final int numResultSets) {
        this.numResultSets = numResultSets;
    }

    /**
     * added anupam returns the number of ResultSets
     * 
     * @return numResultSets
     */
    public int getNumResultSets() {
        return this.numResultSets;
    }

    // added till here
}
