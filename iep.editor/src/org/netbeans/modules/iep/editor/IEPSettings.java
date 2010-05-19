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
package org.netbeans.modules.iep.editor;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.modules.iep.model.share.SharedConstants;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;

/**
 * Manages the IEP editor options.
 *
 * 
 */
public class IEPSettings implements SharedConstants {

    /** Singleton instance of SchemaSettings */
    private static IEPSettings INSTANCE = new IEPSettings();
    /** Name of the operator property. */
    public static final String PROP_DISABLED_OPERATOR_LIST = "disabled.operator.list";
    public static final String PROP_DISABLED_OPERATOR_CATEGORY_LIST = "disabled.operator.category.list";

    private IEPSettings() {
        setDefaults();
    }

    /**
     * Returns the single instance of this class.
     *
     * @return  the instance.
     */
    public static IEPSettings getDefault() {
        return INSTANCE;
    }

    public static BeanNode createViewNode() throws IntrospectionException {
        return new BeanNode(getDefault());
    }

    public void setDisabledOperators(String operators) {
        putProperty(PROP_DISABLED_OPERATOR_LIST, operators);
    }

    public String getDisabledOperators() {
        String operatorList = getProperty(PROP_DISABLED_OPERATOR_LIST);
        if (operatorList != null) {
            return operatorList;
        }

        return "";
    }

    public void setDisabledOperatorCategories(String operatorCategories) {
        putProperty(PROP_DISABLED_OPERATOR_CATEGORY_LIST, operatorCategories);
    }

    public String getDisabledOperatorCategories() {
        String operatorCategoryList = getProperty(PROP_DISABLED_OPERATOR_CATEGORY_LIST);
        if (operatorCategoryList != null) {
            return operatorCategoryList;
        }

        return "";
    }

    /**
     * Retrieves the view mode value.
     *
     * @return  view mode.
     */
    public List<String> getDisabledOperatorsAsList() {
        List<String> operators = new ArrayList<String>();
        String operatorList = getDisabledOperators();
        if (operatorList != null) {
            StringTokenizer st = new StringTokenizer(operatorList, ",");
            while (st.hasMoreTokens()) {
                String operatorName = st.nextToken();
                operators.add(operatorName);
            }
        }
        return operators;
    }

    public List<String> getDisabledOperatorCategoriesAsList() {
        List<String> operatorCategories = new ArrayList<String>();
        String operatorCategoryList = getDisabledOperatorCategories();
        if (operatorCategoryList != null) {
            StringTokenizer st = new StringTokenizer(operatorCategoryList, ",");
            while (st.hasMoreTokens()) {
                String operatorName = st.nextToken();
                operatorCategories.add(operatorName);
            }
        }
        return operatorCategories;
    }

    /**
     * For those properties that have null values, set them to the default.
     */
    private void setDefaults() {
        // Enable streamOperator 
        //                -> invoke service
        //                -> merge
        // To disable them, uncomment the following section
        if (getProperty(PROP_DISABLED_OPERATOR_LIST) == null) {
            putProperty(PROP_DISABLED_OPERATOR_LIST, OP_INVOKE_SERVICE);
        }

        /*
        if (getProperty(PROP_DISABLED_OPERATOR_CATEGORY_LIST) == null) {
            putProperty(PROP_DISABLED_OPERATOR_CATEGORY_LIST, "streamOperator");
        }
        */

    }

    protected final void putProperty(String key, String value) {
        if (value != null) {
            NbPreferences.forModule(IEPSettings.class).put(key, value);
        } else {
            NbPreferences.forModule(IEPSettings.class).remove(key);
        }

    }

    protected final String getProperty(String key) {
        return NbPreferences.forModule(IEPSettings.class).get(key, null);
    }
}
