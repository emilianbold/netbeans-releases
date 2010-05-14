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
package org.netbeans.modules.edm.model;

import java.util.List;

import org.netbeans.modules.edm.model.visitors.SQLVisitedObject;


/**
 * Defines methods required for a source table representation.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SourceTable extends SQLDBTable, Cloneable, Comparable, SQLVisitedObject {

    /** extraction condition tag */
    public static final String EXTRACTION_CONDITION = "extractionCondition";

    /**
     * get the extraction condition
     * 
     * @return SQLCondition to apply while doing extraction
     */
    public SQLCondition getFilterCondition();

    /**
     * get the extraction conidition text
     * 
     * @return sql condition
     */
    public String getFilterConditionText();
    
    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy();    
    
      /**
     * Indicates whether the fully-qualified form should be used whenever one resolves
     * this table's name.
     * 
     * @return true if fully-qualified form should be used, false otherwise
     */
    public boolean isUsingFullyQualifiedName();

    /**
     * check if distinct rows of a column needs to be selected
     * 
     * @return distinct
     */
    public boolean isSelectDistinct();

    /**
     * determine whether this table is used in a join view
     * 
     * @return boolean
     */
    public boolean isUsedInJoin();

    public void setUsingFullyQualifiedName(boolean usesFullName);
    
    /**
     * set the extraction condition
     * 
     * @param cond filter to appy while extraction
     */
    public void setFilterCondition(SQLCondition cond);

    /**
     * set the extraction condition text
     * 
     * @param cond extraction condition text
     */
    public void setFilterConditionText(String cond);

    /**
     * set wehether to select distinct rows of a column
     * 
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct);

    /**
     * set whether this table is used in join view
     * 
     * @param used boolean
     */
    public void setUsedInJoin(boolean used);
    
    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy);    

    /**
     * do source table validation
     * 
     * @return list of validation infos
     */
    public List validate();
}
